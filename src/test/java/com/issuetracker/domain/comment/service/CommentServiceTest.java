package com.issuetracker.domain.comment.service;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.domain.comment.repository.CommentRepository;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.global.common.JsonFileManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentServiceTest {

    private static final Path ACCOUNTS_FILE = Path.of("data", "accounts.json");
    private static final Path COMMENTS_FILE = Path.of("data", "comments.json");
    private static final Path ISSUES_FILE = Path.of("data", "issues.json");

    private final CommentService commentService = new CommentService();
    private final CommentRepository commentRepository = CommentRepository.getInstance();

    private String originalAccountsJson;
    private String originalCommentsJson;
    private String originalIssuesJson;

    @BeforeEach
    void setUp() throws IOException {
        originalAccountsJson = readOriginal(ACCOUNTS_FILE);
        originalCommentsJson = readOriginal(COMMENTS_FILE);
        originalIssuesJson = readOriginal(ISSUES_FILE);

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

    // 댓글 생성에 필요한 issueId, authorId, content가 유효하지 않으면 생성을 거부하는지 검증한다.
    @Test
    void createCommentRejectsInvalidInput() {
        assertFalse(commentService.createComment(null, 1L, "content"));
        assertFalse(commentService.createComment(10L, null, "content"));
        assertFalse(commentService.createComment(10L, 1L, null));
        assertFalse(commentService.createComment(10L, 1L, "   "));
        assertTrue(commentRepository.findAll().isEmpty());
    }

    //  댓글 작성자(accountId)가 존재하지 않으면 댓글 생성을 거부하는지 검증한다.
    @Test
    void createCommentRejectsMissingAuthor() {
        seedIssue(10L);

        assertFalse(commentService.createComment(10L, 1L, "content"));
        assertTrue(commentRepository.findAll().isEmpty());
    }

    // 댓글이 달릴 이슈(issueId)가 존재하지 않으면 댓글 생성을 거부하는지 검증한다.
    @Test
    void createCommentRejectsMissingIssue() {
        seedAccount(1L, Role.DEV);

        assertFalse(commentService.createComment(10L, 1L, "content"));
        assertTrue(commentRepository.findAll().isEmpty());
    }


    // 유효한 댓글 생성 요청이 저장소에 실제 댓글로 저장되는지 검증한다.
    @Test
    void createCommentSavesValidComment() {
        seedAccount(1L, Role.DEV);
        seedIssue(10L);

        assertTrue(commentService.createComment(10L, 1L, "content"));

        Comment saved = commentRepository.findByCommentId(1L);
        assertNotNull(saved);
        assertEquals(10L, saved.getIssueId());
        assertEquals(1L, saved.getAuthorId());
        assertEquals("content", saved.getContent());
    }


    // 댓글 작성자만 댓글을 수정할 수 있고 다른 사용자의 수정 요청은 거부되는지 검증한다.
    @Test
    void updateCommentSucceedsOnlyForAuthor() {
        assertTrue(commentRepository.save(comment(1L, 10L, 1L, "old content")));

        assertTrue(commentService.updateComment(1L, 1L, "new content"));
        assertFalse(commentService.updateComment(1L, 2L, "hacked content"));

        assertEquals("new content", commentRepository.findByCommentId(1L).getContent());
    }

    // 존재하지 않는 댓글을 수정하려고 하면 false를 반환하는지 검증한다.
    @Test
    void updateCommentReturnsFalseWhenCommentDoesNotExist() {
        assertFalse(commentService.updateComment(99L, 1L, "new content"));
    }

    // 댓글 작성자 또는 관리자는 댓글을 삭제할 수 있는지 검증한다.
    @Test
    void deleteCommentSucceedsForAuthorOrAdmin() {
        assertTrue(commentRepository.save(comment(1L, 10L, 1L, "author comment")));
        assertTrue(commentRepository.save(comment(2L, 10L, 1L, "admin deletable comment")));

        assertTrue(commentService.deleteComment(1L, 1L, false));
        assertTrue(commentService.deleteComment(2L, 99L, true));

        assertTrue(commentRepository.findAll().isEmpty());
    }

    // 작성자도 관리자도 아닌 사용자의 댓글 삭제 요청은 거부되고 댓글이 유지되는지 검증한다.
    @Test
    void deleteCommentFailsForDifferentNonAdminUser() {
        assertTrue(commentRepository.save(comment(1L, 10L, 1L, "content")));

        assertFalse(commentService.deleteComment(1L, 2L, false));

        assertNotNull(commentRepository.findByCommentId(1L));
    }

    // 존재하지 않는 댓글을 삭제하려고 하면 false를 반환하는지 검증한다.
    @Test
    void deleteCommentReturnsFalseWhenCommentDoesNotExist() {
        assertFalse(commentService.deleteComment(99L, 1L, true));
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

    private Comment comment(Long commentId, Long issueId, Long authorId, String content) {
        return new Comment(commentId, issueId, authorId, content, LocalDateTime.now(), LocalDateTime.now());
    }
}
