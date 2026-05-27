package com.issuetracker.domain.comment.repository;

import com.issuetracker.domain.comment.entity.Comment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentRepositoryTest {

    private static final Path COMMENTS_FILE = Path.of("data", "comments.json");
    private static final Long ISSUE_ID = 10L;
    private static final Long OTHER_ISSUE_ID = 20L;
    private static final Long AUTHOR_ID = 1L;

    private final CommentRepository commentRepository = new JsonCommentRepository();

    private String originalJson;

    @BeforeEach
    void setUp() throws IOException {
        originalJson = Files.exists(COMMENTS_FILE)
                ? Files.readString(COMMENTS_FILE, StandardCharsets.UTF_8)
                : null;

        Files.createDirectories(COMMENTS_FILE.getParent());
        Files.writeString(COMMENTS_FILE, "[]", StandardCharsets.UTF_8);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (originalJson == null) {
            Files.deleteIfExists(COMMENTS_FILE);
        } else {
            Files.writeString(COMMENTS_FILE, originalJson, StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("댓글 저장 성공: ID가 순차 부여되고 저장소에 유지")
    void saveAssignsSequentialIds() {
        assertTrue(commentRepository.save(new Comment(ISSUE_ID, AUTHOR_ID, "first")));
        assertTrue(commentRepository.save(new Comment(ISSUE_ID, AUTHOR_ID, "second")));

        List<Comment> comments = commentRepository.findAll();
        assertEquals(2, comments.size());
        assertEquals(1L, comments.get(0).getCommentId());
        assertEquals(2L, comments.get(1).getCommentId());
    }

    @Test
    @DisplayName("댓글 단건 조회 성공: commentId로 정확히 반환")
    void findByCommentIdReturnsComment() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "content");
        commentRepository.save(comment);

        Comment found = commentRepository.findByCommentId(comment.getCommentId());

        assertNotNull(found);
        assertEquals("content", found.getContent());
    }

    @Test
    @DisplayName("댓글 이슈별 조회 성공: 해당 이슈 댓글만 반환")
    void findByIssueIdReturnsOnlyMatching() {
        commentRepository.save(new Comment(ISSUE_ID, AUTHOR_ID, "issue 10"));
        commentRepository.save(new Comment(OTHER_ISSUE_ID, AUTHOR_ID, "issue 20"));

        List<Comment> comments = commentRepository.findByIssueId(ISSUE_ID);

        assertEquals(1, comments.size());
        assertEquals("issue 10", comments.get(0).getContent());
    }

    @Test
    @DisplayName("댓글 수정 성공: 변경된 내용이 저장소에 반영")
    void updatePersistsChanges() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "old content");
        commentRepository.save(comment);
        Long commentId = comment.getCommentId();
        Comment loaded = commentRepository.findByCommentId(commentId);
        loaded.updateContent("new content");

        assertTrue(commentRepository.update(loaded));

        assertEquals("new content", commentRepository.findByCommentId(commentId).getContent());
    }

    @Test
    @DisplayName("댓글 삭제 성공: 저장소에서 제거")
    void deleteRemovesComment() {
        Comment comment = new Comment(ISSUE_ID, AUTHOR_ID, "content");
        commentRepository.save(comment);

        assertTrue(commentRepository.delete(comment.getCommentId()));

        assertTrue(commentRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("댓글 전체 조회 성공: 저장된 모든 댓글 반환")
    void findAllReturnsAllComments() {
        commentRepository.save(new Comment(ISSUE_ID, AUTHOR_ID, "c1"));
        commentRepository.save(new Comment(OTHER_ISSUE_ID, AUTHOR_ID, "c2"));

        List<Comment> all = commentRepository.findAll();

        assertEquals(2, all.size());
    }
}
