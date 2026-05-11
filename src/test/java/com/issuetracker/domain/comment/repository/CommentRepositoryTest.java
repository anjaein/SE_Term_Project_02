package com.issuetracker.domain.comment.repository;

import com.issuetracker.domain.comment.entity.Comment;
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

class CommentRepositoryTest {

    private static final Path COMMENTS_FILE = Path.of("data", "comments.json");

    private final CommentRepository commentRepository = CommentRepository.getInstance();

    private String originalCommentsJson;

    @BeforeEach
    void setUp() throws IOException {
        originalCommentsJson = Files.exists(COMMENTS_FILE)
                ? Files.readString(COMMENTS_FILE, StandardCharsets.UTF_8)
                : null;

        Files.createDirectories(COMMENTS_FILE.getParent());
        Files.writeString(COMMENTS_FILE, "[]", StandardCharsets.UTF_8);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (originalCommentsJson == null) {
            Files.deleteIfExists(COMMENTS_FILE);
        } else {
            Files.writeString(COMMENTS_FILE, originalCommentsJson, StandardCharsets.UTF_8);
        }
    }

    // 댓글을 저장할 때 다음 ID가 자동으로 부여되고 파일 기반 저장소에 보존되는지 검증한다.
    @Test
    void saveAssignsNextIdAndPersistsComment() {
        assertTrue(commentRepository.save(new Comment(10L, 1L, "first")));
        assertTrue(commentRepository.save(new Comment(10L, 2L, "second")));

        List<Comment> comments = commentRepository.findAll();
        assertEquals(2, comments.size());
        assertEquals(1L, comments.get(0).getCommentId());
        assertEquals(2L, comments.get(1).getCommentId());
        assertEquals("first", comments.get(0).getContent());
        assertEquals("second", comments.get(1).getContent());
    }

    // findByIssueId가 요청한 이슈에 속한 댓글만 반환하는지 검증한다.
    @Test
    void findByIssueIdReturnsOnlyMatchingIssueComments() {
        Comment issue10Comment = comment(1L, 10L, 1L, "issue 10");
        Comment issue20Comment = comment(2L, 20L, 1L, "issue 20");
        assertTrue(commentRepository.save(issue10Comment));
        assertTrue(commentRepository.save(issue20Comment));

        List<Comment> comments = commentRepository.findByIssueId(10L);

        assertEquals(1, comments.size());
        assertEquals("issue 10", comments.get(0).getContent());
    }

    // 기존 댓글을 수정하면 같은 ID의 댓글 데이터가 새 내용으로 교체되는지 검증한다.
    @Test
    void updateReplacesExistingComment() {
        assertTrue(commentRepository.save(comment(1L, 10L, 1L, "old content")));

        Comment updated = commentRepository.findByCommentId(1L);
        updated.updateContent("new content");

        assertTrue(commentRepository.update(updated));
        assertEquals("new content", commentRepository.findByCommentId(1L).getContent());
    }

    // 존재하지 않는 댓글을 수정하려고 하면 false를 반환하고 저장소가 변경되지 않는지 검증한다.
    @Test
    void updateReturnsFalseWhenCommentDoesNotExist() {
        Comment missingComment = comment(99L, 10L, 1L, "missing");

        assertFalse(commentRepository.update(missingComment));
        assertTrue(commentRepository.findAll().isEmpty());
    }

    // 기존 댓글을 삭제하면 저장소에서 해당 댓글이 제거되는지 검증한다.
    @Test
    void deleteRemovesExistingComment() {
        assertTrue(commentRepository.save(comment(1L, 10L, 1L, "content")));

        assertTrue(commentRepository.delete(1L));

        assertTrue(commentRepository.findAll().isEmpty());
    }

    // 존재하지 않는 댓글을 삭제하려고 하면 false를 반환하는지 검증한다.
    @Test
    void deleteReturnsFalseWhenCommentDoesNotExist() {
        assertFalse(commentRepository.delete(99L));
    }

    private Comment comment(Long commentId, Long issueId, Long authorId, String content) {
        return new Comment(commentId, issueId, authorId, content, LocalDateTime.now(), LocalDateTime.now());
    }
}
