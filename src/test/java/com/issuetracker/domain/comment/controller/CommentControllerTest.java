package com.issuetracker.domain.comment.controller;

import com.google.gson.reflect.TypeToken;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.domain.comment.repository.CommentRepository;
import com.issuetracker.domain.comment.repository.JsonCommentRepository;
import com.issuetracker.domain.comment.service.CommentService;
import com.issuetracker.domain.comment.service.CommentValidator;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.issue.repository.JsonIssueRepository;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.JsonProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.global.common.JsonFileManager;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;
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

class CommentControllerTest {

    private static final Path COMMENTS_FILE = Path.of("data", "comments.json");
    private static final Path ISSUES_FILE = Path.of("data", "issues.json");
    private static final Path PROJECT_MEMBERS_FILE = Path.of("data", "project_members.json");

    private static final Long PROJECT_ID = 1L;
    private static final Long ISSUE_ID = 10L;
    private static final Long AUTHOR_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long ADMIN_ID = 99L;

    private CommentRepository commentRepository;
    private SessionManager sessionManager;
    private CommentController commentController;

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
        IssueRepository issueRepository = new JsonIssueRepository();
        ProjectMemberRepository projectMemberRepository = new JsonProjectMemberRepository();
        CommentService commentService = new CommentService(
                commentRepository,
                new CommentValidator(issueRepository, projectMemberRepository)
        );

        sessionManager = new SessionManager();
        commentController = new CommentController(commentService, sessionManager);

        seedIssue(ISSUE_ID, PROJECT_ID);
        seedProjectMember(PROJECT_ID, AUTHOR_ID, Role.DEV);

        sessionManager.logout();
    }

    @AfterEach
    void tearDown() throws IOException {
        restoreJsonFile(COMMENTS_FILE, originalCommentsJson);
        restoreJsonFile(ISSUES_FILE, originalIssuesJson);
        restoreJsonFile(PROJECT_MEMBERS_FILE, originalProjectMembersJson);
    }

    @Test
    @DisplayName("댓글 생성 성공: 로그인한 프로젝트 멤버가 작성")
    void createCommentSucceeds() {
        loginAs(AUTHOR_ID, Role.DEV);

        Response<Comment> result = commentController.createComment(ISSUE_ID, "hello");

        assertTrue(result.isSuccess());
        assertEquals(1, commentRepository.findAll().size());
    }

    @Test
    @DisplayName("댓글 생성 실패: 비로그인 상태")
    void createCommentFailsWhenNotLoggedIn() {
        Response<Comment> result = commentController.createComment(ISSUE_ID, "hello");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    @Test
    @DisplayName("댓글 이슈별 조회 성공: 로그인 상태에서 목록 반환")
    void listCommentsSucceeds() {
        commentRepository.save(new Comment(ISSUE_ID, AUTHOR_ID, "c1"));
        loginAs(AUTHOR_ID, Role.DEV);

        Response<List<Comment>> result = commentController.listComments(ISSUE_ID);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("댓글 이슈별 조회 실패: 비로그인 상태")
    void listCommentsFailsWhenNotLoggedIn() {
        Response<List<Comment>> result = commentController.listComments(ISSUE_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    @Test
    @DisplayName("댓글 수정 성공: 작성자 본인이 로그인 상태에서 수정")
    void updateCommentSucceeds() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "old");
        commentRepository.save(comment);
        loginAs(AUTHOR_ID, Role.DEV);

        Response<Comment> result = commentController.updateComment(comment.getCommentId(), "new");

        assertTrue(result.isSuccess());
        assertEquals("new", commentRepository.findByCommentId(comment.getCommentId()).getContent());
    }

    @Test
    @DisplayName("댓글 수정 실패: 비로그인 상태")
    void updateCommentFailsWhenNotLoggedIn() {
        Response<Comment> result = commentController.updateComment(1L, "new");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    @Test
    @DisplayName("댓글 삭제 성공: 작성자 본인이 로그인 상태에서 삭제")
    void deleteCommentSucceedsForAuthor() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "content");
        commentRepository.save(comment);
        loginAs(AUTHOR_ID, Role.DEV);

        Response<Comment> result = commentController.deleteComment(comment.getCommentId());

        assertTrue(result.isSuccess());
        assertNull(commentRepository.findByCommentId(comment.getCommentId()));
    }

    @Test
    @DisplayName("댓글 삭제 성공: ADMIN으로 로그인하여 타인 댓글 삭제")
    void deleteCommentSucceedsForAdmin() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "content");
        commentRepository.save(comment);
        loginAs(ADMIN_ID, Role.ADMIN);

        Response<Comment> result = commentController.deleteComment(comment.getCommentId());

        assertTrue(result.isSuccess());
        assertNull(commentRepository.findByCommentId(comment.getCommentId()));
    }

    @Test
    @DisplayName("댓글 삭제 실패: 비로그인 상태")
    void deleteCommentFailsWhenNotLoggedIn() {
        Response<Comment> result = commentController.deleteComment(1L);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    @Test
    @DisplayName("댓글 삭제 실패: 작성자도 ADMIN도 아닌 사용자가 로그인")
    void deleteCommentFailsForNonAuthorNonAdmin() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "content");
        commentRepository.save(comment);
        loginAs(OTHER_USER_ID, Role.DEV);

        Response<Comment> result = commentController.deleteComment(comment.getCommentId());

        assertFalse(result.isSuccess());
        assertNotNull(commentRepository.findByCommentId(comment.getCommentId()));
    }

    private void loginAs(Long id, Role role) {
        Account account = new Account("user" + id, "pw", role);
        account.setAccountId(id);
        sessionManager.login(account);
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
