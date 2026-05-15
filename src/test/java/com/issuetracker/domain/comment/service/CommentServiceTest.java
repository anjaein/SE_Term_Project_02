package com.issuetracker.domain.comment.service;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.domain.comment.repository.CommentRepository;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.global.common.JsonFileManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentServiceTest {

    private static final Path ACCOUNTS_FILE = Path.of("data", "accounts.json");
    private static final Path COMMENTS_FILE = Path.of("data", "comments.json");
    private static final Path ISSUES_FILE = Path.of("data", "issues.json");

    private CommentService commentService;
    private final CommentRepository commentRepository = CommentRepository.getInstance();
    private AccountRepository accountRepository;
    private IssueRepository issueRepository;

    private String originalAccountsJson;
    private String originalCommentsJson;
    private String originalIssuesJson;

    @BeforeEach
    void setUp() throws IOException {
        originalAccountsJson = readOriginal(ACCOUNTS_FILE);
        originalCommentsJson = readOriginal(COMMENTS_FILE);
        originalIssuesJson = readOriginal(ISSUES_FILE);
        // 1. 매번 새로운 레포지토리를 생성 (데이터 0개인 상태)
        accountRepository = new AccountRepository();
        issueRepository = new IssueRepository();
        // 2. 이 레포지토리들을 주입해서 서비스를 생성
        commentService = new CommentService(commentRepository, accountRepository, issueRepository);

        resetJsonFile(ACCOUNTS_FILE);
        resetJsonFile(COMMENTS_FILE);
        resetJsonFile(ISSUES_FILE);
    }

    @AfterEach
    void tearDown() throws IOException {
        restoreJsonFile(ACCOUNTS_FILE, originalAccountsJson);
        restoreJsonFile(COMMENTS_FILE, originalCommentsJson);
        restoreJsonFile(ISSUES_FILE, originalIssuesJson);
    }

    @Test
    @DisplayName("댓글 생성 시 필수 입력값(이슈 ID, 작성자 ID, 내용)이 누락되거나 공백이면 생성이 거부된다")
    void createCommentRejectsInvalidInput() {
        //when
        assertFalse(commentService.createComment(null, 1L, "content"));
        assertFalse(commentService.createComment(10L, null, "content"));
        assertFalse(commentService.createComment(10L, 1L, null));
        assertFalse(commentService.createComment(10L, 1L, "   "));

        //then
        assertTrue(commentRepository.findAll().isEmpty());
    }


    @Test
    @DisplayName("존재하지 않는 이슈 ID로 댓글 생성을 시도하면 거부된다")
    void createCommentRejectsMissingAuthor() {
        //given
        seedAccount(1L, Role.DEV);

        // when
        boolean result = commentService.createComment(10L, 1L, "content");

        // then
        assertFalse(result);
        assertTrue(commentRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 작성자 ID로 댓글 생성을 시도하면 거부된다")
    void createCommentRejectsMissingIssue() {
        //given
        seedIssue(10L);

        // when
        boolean result = commentService.createComment(10L, 1L, "content");

        // then
        assertFalse(result);
        assertTrue(commentRepository.findAll().isEmpty());
    }


    @Test
    @DisplayName("유효한 댓글 생성 요청이 들어오면 저장소에 댓글이 정상적으로 저장된다")
    void createCommentSavesValidComment() {
        //given
        seedAccount(1L, Role.DEV);
        seedIssue(10L);

        // when
        boolean isCreated = commentService.createComment(10L, 1L, "content");

        // then
        assertTrue(isCreated);

        Comment saved = commentRepository.findByCommentId(1L);
        assertNotNull(saved);
        assertEquals(10L, saved.getIssueId());
        assertEquals(1L, saved.getAuthorId());
        assertEquals("content", saved.getContent());
    }


    @Test
    @DisplayName("댓글 작성자만 내용을 수정할 수 있으며, 타인의 수정 요청은 거부된다")
    void updateCommentSucceedsOnlyForAuthor() {
        //given
        Comment new_comment = new Comment(10L, 1L, "old content");
        assertTrue(commentRepository.save(new_comment));

        //when
        assertTrue(commentService.updateComment(new_comment.getCommentId(), 1L, "new content"));
        assertFalse(commentService.updateComment(new_comment.getCommentId(), 2L, "hacked content"));

        //then
        assertEquals("new content", commentRepository.findByCommentId(new_comment.getCommentId()).getContent());
    }

    @Test
    @DisplayName("존재하지 않는 댓글 ID로 수정을 시도하면 false를 반환한다")
    void updateCommentReturnsFalseWhenCommentDoesNotExist() {
        // given
        Long nonExistentCommentId = 99L;
        Long authorId = 1L;
        String content = "new content";

        // when
        boolean result = commentService.updateComment(nonExistentCommentId, authorId, content);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("댓글 작성자 본인 또는 관리자는 댓글을 삭제할 수 있다")
    void deleteCommentSucceedsForAuthorOrAdmin() {
        //given
        Comment author_comment = new Comment(10L, 1L, "author comment");
        assertTrue(commentRepository.save(author_comment));
        Comment admin_comment = new Comment(10L, 2L, "admin deletable comment");
        assertTrue(commentRepository.save(admin_comment));

        //when
        assertTrue(commentService.deleteComment(1L, 1L, false));
        assertTrue(commentService.deleteComment(2L, 99L, true));

        //then
        assertTrue(commentRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("작성자도 관리자도 아닌 사용자의 댓글 삭제 요청은 거부되고 댓글이 유지된다")
    void deleteCommentFailsForDifferentNonAdminUser() {
        //given
        Comment new_comment = new Comment(10L, 1L, "content");
        assertTrue(commentRepository.save(new_comment));

        // when
        boolean result = commentService.deleteComment(new_comment.getCommentId(), 2L, false);

        // then
        assertFalse(result);
        assertNotNull(commentRepository.findByCommentId(1L));
    }

    @Test
    @DisplayName("존재하지 않는 댓글 ID로 삭제를 시도하면 false를 반환한다")
    void deleteCommentReturnsFalseWhenCommentDoesNotExist() {
        // given
        Long nonExistentCommentId = 99L;

        // when
        boolean result = commentService.deleteComment(nonExistentCommentId, 1L, true);

        // then
        assertFalse(result);
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

    private void seedAccount(Long accountId, Role role) {
        Account account = new Account("user" + accountId, "password", role);
        account.setAccountId(accountId);
        JsonFileManager.writeList(ACCOUNTS_FILE.toString(), List.of(account));
    }

    private void seedIssue(Long issueId) {
        Issue issue = new Issue(1L, "issue title", "description", 1L);
        issue.setIssueId(issueId);
        JsonFileManager.writeList(ISSUES_FILE.toString(), List.of(issue));
    }

}
