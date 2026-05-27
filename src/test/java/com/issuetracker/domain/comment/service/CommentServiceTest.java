package com.issuetracker.domain.comment.service;

import com.google.gson.reflect.TypeToken;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.domain.comment.repository.CommentRepository;
import com.issuetracker.domain.comment.repository.JsonCommentRepository;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.issue.repository.JsonIssueRepository;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.JsonProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.global.common.JsonFileManager;
import com.issuetracker.global.common.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentServiceTest {

    private static final Path COMMENTS_FILE = Path.of("data", "comments.json");
    private static final Path ISSUES_FILE = Path.of("data", "issues.json");
    private static final Path PROJECT_MEMBERS_FILE = Path.of("data", "project_members.json");

    private static final Long PROJECT_ID = 1L;
    private static final Long ISSUE_ID = 10L;
    private static final Long AUTHOR_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    private CommentRepository commentRepository;
    private IssueRepository issueRepository;
    private ProjectMemberRepository projectMemberRepository;
    private CommentService commentService;

    private String originalCommentsJson;
    private String originalIssuesJson;
    private String originalProjectMembersJson;

    @BeforeEach
    void setUp() throws IOException {
        originalCommentsJson = readOriginal(COMMENTS_FILE);
        originalIssuesJson = readOriginal(ISSUES_FILE);
        originalProjectMembersJson = readOriginal(PROJECT_MEMBERS_FILE);
        resetJsonFile(COMMENTS_FILE);
        resetJsonFile(ISSUES_FILE);
        resetJsonFile(PROJECT_MEMBERS_FILE);

        commentRepository = new JsonCommentRepository();
        issueRepository = new JsonIssueRepository();
        projectMemberRepository = new JsonProjectMemberRepository();
        commentService = new CommentService(
                commentRepository,
                new CommentValidator(issueRepository, projectMemberRepository)
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        restoreJsonFile(COMMENTS_FILE, originalCommentsJson);
        restoreJsonFile(ISSUES_FILE, originalIssuesJson);
        restoreJsonFile(PROJECT_MEMBERS_FILE, originalProjectMembersJson);
    }

    @Test
    @DisplayName("댓글 생성 성공: 프로젝트 멤버가 유효한 입력으로 작성")
    void createCommentSucceeds() {
        seedIssue(ISSUE_ID, PROJECT_ID);
        seedProjectMember(PROJECT_ID, AUTHOR_ID, Role.DEV);

        Response<Comment> result = commentService.createComment(ISSUE_ID, AUTHOR_ID, "content");

        assertTrue(result.isSuccess());
        Comment saved = result.getData();
        assertNotNull(saved.getCommentId());
        assertEquals(ISSUE_ID, saved.getIssueId());
        assertEquals(AUTHOR_ID, saved.getAuthorId());
        assertEquals("content", saved.getContent());
    }

    @Test
    @DisplayName("댓글 생성 실패: 필수 파라미터(issueId/authorId/content) 중 하나라도 null")
    void createCommentFailsWhenRequiredParamIsNull() {
        Response<Comment> nullIssueId = commentService.createComment(null, AUTHOR_ID, "content");
        Response<Comment> nullAuthorId = commentService.createComment(ISSUE_ID, null, "content");
        Response<Comment> nullContent = commentService.createComment(ISSUE_ID, AUTHOR_ID, null);

        assertFalse(nullIssueId.isSuccess());
        assertTrue(nullIssueId.getMessage().contains("Required parameter is missing"));
        assertFalse(nullAuthorId.isSuccess());
        assertTrue(nullAuthorId.getMessage().contains("Required parameter is missing"));
        assertFalse(nullContent.isSuccess());
        assertTrue(nullContent.getMessage().contains("Required parameter is missing"));
    }

    @Test
    @DisplayName("댓글 생성 실패: content가 blank")
    void createCommentFailsWhenContentIsBlank() {
        Response<Comment> result = commentService.createComment(ISSUE_ID, AUTHOR_ID, "   ");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("cannot be empty"));
    }

    @Test
    @DisplayName("댓글 생성 실패: 이슈 존재하지 않음")
    void createCommentFailsWhenIssueDoesNotExist() {
        Response<Comment> result = commentService.createComment(999L, AUTHOR_ID, "content");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Issue does not exist"));
    }

    @Test
    @DisplayName("댓글 생성 실패: 작성자가 프로젝트 멤버 아님")
    void createCommentFailsWhenAuthorIsNotMember() {
        seedIssue(ISSUE_ID, PROJECT_ID);

        Response<Comment> result = commentService.createComment(ISSUE_ID, AUTHOR_ID, "content");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not a member"));
    }

    @Test
    @DisplayName("댓글 이슈별 조회 성공: 해당 이슈의 댓글 목록 반환")
    void getCommentsByIssueIdReturnsList() {
        commentRepository.save(new Comment(ISSUE_ID, AUTHOR_ID, "c1"));
        commentRepository.save(new Comment(ISSUE_ID, AUTHOR_ID, "c2"));

        Response<List<Comment>> result = commentService.getCommentsByIssueId(ISSUE_ID);

        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
    }

    @Test
    @DisplayName("댓글 단건 조회 성공: 존재하는 commentId")
    void getCommentByIdSucceeds() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "content");
        commentRepository.save(comment);

        Response<Comment> result = commentService.getCommentById(comment.getCommentId());

        assertTrue(result.isSuccess());
        assertEquals(comment.getCommentId(), result.getData().getCommentId());
    }

    @Test
    @DisplayName("댓글 단건 조회 실패: 존재하지 않는 commentId")
    void getCommentByIdFailsWhenNotFound() {
        Response<Comment> result = commentService.getCommentById(999L);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Comment not found"));
    }

    @Test
    @DisplayName("댓글 수정 성공: 작성자 본인이 수정")
    void updateCommentSucceeds() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "old");
        commentRepository.save(comment);

        Response<Comment> result = commentService.updateComment(comment.getCommentId(), AUTHOR_ID, "new");

        assertTrue(result.isSuccess());
        assertEquals("new", commentRepository.findByCommentId(comment.getCommentId()).getContent());
    }

    @Test
    @DisplayName("댓글 수정 실패: 필수 파라미터(commentId/userId/newContent) 중 하나라도 null")
    void updateCommentFailsWhenRequiredParamIsNull() {
        assertFalse(commentService.updateComment(null, AUTHOR_ID, "new").isSuccess());
        assertFalse(commentService.updateComment(1L, null, "new").isSuccess());
        assertFalse(commentService.updateComment(1L, AUTHOR_ID, null).isSuccess());
    }

    @Test
    @DisplayName("댓글 수정 실패: newContent가 blank")
    void updateCommentFailsWhenContentIsBlank() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "old");
        commentRepository.save(comment);

        Response<Comment> result = commentService.updateComment(comment.getCommentId(), AUTHOR_ID, "   ");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("cannot be empty"));
    }

    @Test
    @DisplayName("댓글 수정 실패: 존재하지 않는 commentId")
    void updateCommentFailsWhenCommentDoesNotExist() {
        Response<Comment> result = commentService.updateComment(999L, AUTHOR_ID, "new");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Comment not found"));
    }

    @Test
    @DisplayName("댓글 수정 실패: 작성자가 아닌 사용자")
    void updateCommentFailsWhenUserIsNotAuthor() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "old");
        commentRepository.save(comment);

        Response<Comment> result = commentService.updateComment(comment.getCommentId(), OTHER_USER_ID, "hacked");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("only update your own"));
        assertEquals("old", commentRepository.findByCommentId(comment.getCommentId()).getContent());
    }

    @Test
    @DisplayName("댓글 삭제 성공: 작성자 본인이 삭제")
    void deleteCommentSucceedsForAuthor() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "content");
        commentRepository.save(comment);

        Response<Comment> result = commentService.deleteComment(comment.getCommentId(), AUTHOR_ID, false);

        assertTrue(result.isSuccess());
        assertNull(commentRepository.findByCommentId(comment.getCommentId()));
    }

    @Test
    @DisplayName("댓글 삭제 성공: 작성자가 아니어도 ADMIN이면 삭제")
    void deleteCommentSucceedsForAdmin() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "content");
        commentRepository.save(comment);

        Response<Comment> result = commentService.deleteComment(comment.getCommentId(), OTHER_USER_ID, true);

        assertTrue(result.isSuccess());
        assertNull(commentRepository.findByCommentId(comment.getCommentId()));
    }

    @Test
    @DisplayName("댓글 삭제 실패: 필수 파라미터(commentId/userId) 중 하나라도 null")
    void deleteCommentFailsWhenRequiredParamIsNull() {
        assertFalse(commentService.deleteComment(null, AUTHOR_ID, false).isSuccess());
        assertFalse(commentService.deleteComment(1L, null, false).isSuccess());
    }

    @Test
    @DisplayName("댓글 삭제 실패: 존재하지 않는 commentId")
    void deleteCommentFailsWhenCommentDoesNotExist() {
        Response<Comment> result = commentService.deleteComment(999L, AUTHOR_ID, false);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Comment not found"));
    }

    @Test
    @DisplayName("댓글 삭제 실패: 작성자도 ADMIN도 아닌 사용자")
    void deleteCommentFailsForNonAuthorNonAdmin() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "content");
        commentRepository.save(comment);

        Response<Comment> result = commentService.deleteComment(comment.getCommentId(), OTHER_USER_ID, false);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("only delete your own"));
        assertNotNull(commentRepository.findByCommentId(comment.getCommentId()));
    }

    private void seedIssue(Long issueId, Long projectId) {
        List<Issue> issues = JsonFileManager.readList(ISSUES_FILE.toString(),
                new TypeToken<List<Issue>>(){}.getType());
        if (issues == null) issues = new ArrayList<>();
        Issue issue = new Issue(projectId, "title", "desc", Priority.MAJOR, 999L);
        issue.setIssueId(issueId);
        issues.add(issue);
        JsonFileManager.writeList(ISSUES_FILE.toString(), issues);
    }

    private void seedProjectMember(Long projectId, Long accountId, Role role) {
        List<ProjectMember> members = JsonFileManager.readList(PROJECT_MEMBERS_FILE.toString(),
                new TypeToken<List<ProjectMember>>(){}.getType());
        if (members == null) members = new ArrayList<>();
        members.add(new ProjectMember(projectId, accountId, role));
        JsonFileManager.writeList(PROJECT_MEMBERS_FILE.toString(), members);
    }

    private String readOriginal(Path path) throws IOException {
        return Files.exists(path) ? Files.readString(path, StandardCharsets.UTF_8) : null;
    }

    private void resetJsonFile(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, "[]", StandardCharsets.UTF_8);
    }

    private void restoreJsonFile(Path path, String originalJson) throws IOException {
        if (originalJson == null) {
            Files.deleteIfExists(path);
        } else {
            Files.writeString(path, originalJson, StandardCharsets.UTF_8);
        }
    }
}
