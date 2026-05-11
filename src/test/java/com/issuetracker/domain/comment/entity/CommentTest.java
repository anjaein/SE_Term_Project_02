package com.issuetracker.domain.comment.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    // Comment 생성자가 이슈 ID, 작성자 ID, 내용, 생성/수정 시간을 올바르게 초기화하는지 검증한다.
    @Test
    void constructorSetsIssueAuthorContentAndTimestamps() {
        LocalDateTime beforeCreate = LocalDateTime.now();

        Comment comment = new Comment(10L, 1L, "content");

        LocalDateTime afterCreate = LocalDateTime.now();
        assertNull(comment.getCommentId());
        assertEquals(10L, comment.getIssueId());
        assertEquals(1L, comment.getAuthorId());
        assertEquals("content", comment.getContent());
        assertFalse(comment.getCreatedDate().isBefore(beforeCreate));
        assertFalse(comment.getCreatedDate().isAfter(afterCreate));
        assertFalse(comment.getUpdatedDate().isBefore(beforeCreate));
        assertFalse(comment.getUpdatedDate().isAfter(afterCreate));
    }

    // updateContent가 댓글 내용을 변경하고 updatedDate를 새 시각으로 갱신하는지 검증한다.
    @Test
    void updateContentChangesContentAndRefreshesUpdatedDate() {
        LocalDateTime oldUpdatedDate = LocalDateTime.now().minusDays(1);
        Comment comment = new Comment(1L, 10L, 1L, "old content", LocalDateTime.now().minusDays(2), oldUpdatedDate);

        comment.updateContent("new content");

        assertEquals("new content", comment.getContent());
        assertTrue(comment.getUpdatedDate().isAfter(oldUpdatedDate));
    }
}
