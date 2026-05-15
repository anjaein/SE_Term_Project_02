package com.issuetracker.domain.comment.repository;

import com.issuetracker.domain.comment.entity.Comment;
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

    @Test
    @DisplayName("댓글 저장 시 ID가 순차적으로 부여되고 저장소에 데이터가 유지된다")
    void saveAssignsNextIdAndPersistsComment() {
        //given
        assertTrue(commentRepository.save(new Comment(10L, 1L, "first")));
        assertTrue(commentRepository.save(new Comment(10L, 2L, "second")));

        //when
        List<Comment> comments = commentRepository.findAll();

        //then
        assertEquals(2, comments.size());
        assertEquals(1L, comments.get(0).getCommentId());
        assertEquals(2L, comments.get(1).getCommentId());
        assertEquals("first", comments.get(0).getContent());
        assertEquals("second", comments.get(1).getContent());
    }

    @Test
    @DisplayName("이슈 ID로 조회 시 해당 이슈에 속한 댓글만 반환된다")
    void findByIssueIdReturnsOnlyMatchingIssueComments() {
        //given
        Comment issue10Comment = new Comment(10L, 1L, "issue 10");
        Comment issue20Comment = new Comment(20L, 1L, "issue 20");
        assertTrue(commentRepository.save(issue10Comment));
        assertTrue(commentRepository.save(issue20Comment));

        //when
        List<Comment> comments = commentRepository.findByIssueId(10L);

        //then
        assertEquals(1, comments.size());
        assertEquals("issue 10", comments.get(0).getContent());
    }

    @Test
    @DisplayName("기존 댓글을 수정하면 같은 ID의 댓글 데이터가 새 내용으로 교체된다")
    void updateReplacesExistingComment() {
        //given
        Comment old_Content = new Comment(10L, 1L, "old content");
        assertTrue(commentRepository.save(old_Content));
        long comment_id = old_Content.getCommentId();

        //when
        Comment updated_Content = commentRepository.findByCommentId(comment_id);
        updated_Content.updateContent("new content");
        assertTrue(commentRepository.update(updated_Content));

        //then
        assertEquals("new content", commentRepository.findByCommentId(comment_id).getContent());
    }

    @Test
    @DisplayName("존재하지 않는 댓글을 수정하려고 하면 false를 반환하고 저장소는 변경되지 않는다")
    void updateReturnsFalseWhenCommentDoesNotExist() {
        //given
        Comment missingComment = new Comment( 10L, 1L, "missing");

        //when
        boolean result = commentRepository.update(missingComment);

        //then
        assertFalse(result);
        assertTrue(commentRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("기존 댓글을 삭제하면 저장소에서 해당 댓글이 제거된다")
    void deleteRemovesExistingComment() {
        //given
        Comment comment = new Comment(10L, 1L, "content");
        assertTrue(commentRepository.save(comment));

        //when
        boolean isDeleted = commentRepository.delete(comment.getCommentId());

        //then
        assertTrue(isDeleted);
        assertTrue(commentRepository.findAll().isEmpty());
    }


    @Test
    @DisplayName("존재하지 않는 댓글 ID로 삭제를 시도하면 false를 반환한다")
    void deleteReturnsFalseWhenCommentDoesNotExist() {

        // given
        Long nonExistentId = 99L;

        // when
        boolean result = commentRepository.delete(nonExistentId);

        // then
        assertFalse(result);
    }


}
