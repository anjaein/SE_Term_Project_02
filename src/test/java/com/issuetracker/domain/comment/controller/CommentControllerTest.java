package com.issuetracker.domain.comment.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.domain.comment.repository.CommentRepository;
import com.issuetracker.domain.comment.repository.JsonCommentRepository;
import com.issuetracker.domain.comment.service.CommentService;
import com.issuetracker.domain.comment.service.CommentValidator;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.issue.repository.JsonIssueRepository;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.JsonProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.global.common.JsonFileManager;

import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentControllerTest {

    private static final Path ACCOUNTS_FILE = Path.of("data", "accounts.json");
    private static final Path COMMENTS_FILE = Path.of("data", "comments.json");
    private static final Path ISSUES_FILE = Path.of("data", "issues.json");
    private static final Path PROJECT_MEMBERS_FILE = Path.of("data", "project_members.json");

    private CommentController commentController;
    private CommentRepository commentRepository;
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() throws IOException {
        // 1. 파일 시스템 초기화 (빈 배열로 초기화)
        Files.createDirectories(ACCOUNTS_FILE.getParent());
        Files.writeString(ACCOUNTS_FILE, "[]", StandardCharsets.UTF_8);
        Files.writeString(COMMENTS_FILE, "[]", StandardCharsets.UTF_8);
        Files.writeString(ISSUES_FILE, "[]", StandardCharsets.UTF_8);
        Files.writeString(PROJECT_MEMBERS_FILE, "[]", StandardCharsets.UTF_8);

        // 2. 의존성 객체 생성 및 주입
        this.sessionManager = new SessionManager();
        this.commentRepository = new JsonCommentRepository();
        IssueRepository issueRepository = new JsonIssueRepository();
        ProjectMemberRepository projectMemberRepository = new JsonProjectMemberRepository();

        CommentValidator commentValidator = new CommentValidator(issueRepository, projectMemberRepository);

        // 서비스 생성 시 필요한 레포지토리 주입
        CommentService commentService = new CommentService(
                commentRepository,
                commentValidator
        );

        // 컨트롤러 주입
        this.commentController = new CommentController(commentService, sessionManager);

        sessionManager.logout();
    }

    @Test
    @DisplayName("성공: 로그인한 사용자가 유효한 이슈에 댓글을 작성한다")
    void createComment_Success() {
        // given
        Long userId = 1L;
        Long issueId = 10L;
        seedAccount(userId, Role.DEV);
        seedIssue(issueId);
        seedProjectMember(1L, userId, Role.DEV);
        loginAs(userId, Role.DEV);

        // when
        Response<Comment> response = commentController.createComment(issueId, "Hello World");

        // then
        assertTrue(response.isSuccess());
        List<Comment> all = commentRepository.findAll();
        assertEquals(1, all.size());
        assertEquals("Hello World", all.get(0).getContent());
    }

    @Test
    @DisplayName("실패: 로그인하지 않은 경우 댓글 작성이 거부된다")
    void createComment_Fail_NotLoggedIn() {
        // when
        Response<Comment> response = commentController.createComment(10L, "Not logged in");

        // then
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("You are not logged in."));
        assertTrue(commentRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 이슈에 댓글을 달 수 없다")
    void createComment_Fail_InvalidIssue() {
        // given
        loginAs(1L, Role.DEV);
        seedAccount(1L, Role.DEV);

        // when
        Response<Comment> response = commentController.createComment(999L, "Ghost issue");

        // then
        assertFalse(response.isSuccess());
    }

    @Test
    @DisplayName("성공: 본인이 작성한 댓글을 수정한다")
    void updateComment_Success() {
        // given
        Long userId = 1L;
        loginAs(userId, Role.DEV);
        Comment existing = new Comment(10L, userId, "Old Content");
        commentRepository.save(existing);

        // when
        Response<Comment> response = commentController.updateComment(1L, "New Content");

        // then
        assertTrue(response.isSuccess());
        assertEquals("New Content", commentRepository.findByCommentId(1L).getContent());
    }

    @Test
    @DisplayName("실패: 다른 사람의 댓글은 수정할 수 없다")
    void updateComment_Fail_Forbidden() {
        // given
        loginAs(2L, Role.DEV);
        Comment othersComment = new Comment(10L, 1L, "Author is User 1");
        commentRepository.save(othersComment);

        // when
        Response<Comment> response = commentController.updateComment(1L, "Hacked!");

        // then
        assertFalse(response.isSuccess());
        assertEquals("Author is User 1", commentRepository.findByCommentId(1L).getContent());
    }

    @Test
    @DisplayName("성공: 관리자는 다른 사람의 댓글을 삭제할 수 있다")
    void deleteComment_Admin_Success() {
        // given
        loginAs(99L, Role.ADMIN);
        Comment userComment = new Comment(10L, 1L, "User's comment");
        commentRepository.save(userComment);

        // when
        Response<Comment> response = commentController.deleteComment(1L);

        // then
        assertTrue(response.isSuccess());
        assertTrue(commentRepository.findAll().isEmpty());
    }

    private void loginAs(Long id, Role role) {
        Account account = new Account("testUser", "pw", role);
        account.setAccountId(id);
        sessionManager.login(account);
    }

    private void seedAccount(Long id, Role role) {
        Account account = new Account("testUser", "pw", role);
        account.setAccountId(id);
        JsonFileManager.writeList(ACCOUNTS_FILE.toString(), List.of(account));
    }

    private void seedIssue(Long id) {
        Issue issue = new Issue(1L, "title", "desc", 1L);
        issue.setIssueId(id);
        JsonFileManager.writeList(ISSUES_FILE.toString(), List.of(issue));
    }

    private void seedProjectMember(Long projectId, Long accountId, Role role) {
        ProjectMember member = new ProjectMember(projectId, accountId, role);
        JsonFileManager.writeList(PROJECT_MEMBERS_FILE.toString(), List.of(member));
    }
}
