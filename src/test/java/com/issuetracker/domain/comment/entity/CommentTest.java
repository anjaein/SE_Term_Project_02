package com.issuetracker.domain.comment.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @Test
    @DisplayName("댓글 생성 성공: issueId/authorId/content 저장 및 createdDate/updatedDate 자동 기록")
    void constructorInitializesFieldsAndTimestamps() {
        LocalDateTime before = LocalDateTime.now();

        Comment comment = new Comment(10L, 1L, "content");

        LocalDateTime after = LocalDateTime.now();
        assertNull(comment.getCommentId());
        assertEquals(10L, comment.getIssueId());
        assertEquals(1L, comment.getAuthorId());
        assertEquals("content", comment.getContent());
        assertFalse(comment.getCreatedDate().isBefore(before));
        assertFalse(comment.getCreatedDate().isAfter(after));
        assertFalse(comment.getUpdatedDate().isBefore(before));
        assertFalse(comment.getUpdatedDate().isAfter(after));
    }

    @Test
    @DisplayName("댓글 내용 수정 성공: trim 적용 및 updatedDate 갱신")
    void updateContentTrimsAndRefreshesUpdatedDate() {
        Comment comment = new Comment(1L, 10L, "old content");
        LocalDateTime oldUpdatedDate = comment.getUpdatedDate().minusNanos(1);

        comment.updateContent("  new content  ");

        assertEquals("new content", comment.getContent());
        assertTrue(comment.getUpdatedDate().isAfter(oldUpdatedDate));
    }
}
