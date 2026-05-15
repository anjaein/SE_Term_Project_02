package com.issuetracker.domain.comment.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @Test
    @DisplayName("Comment 생성 시 이슈 ID, 작성자 ID, 내용 및 생성/수정 시간이 올바르게 초기화된다")
    void constructorSetsIssueAuthorContentAndTimestamps() {
        //given
        LocalDateTime beforeCreate = LocalDateTime.now();

        //when
        Comment comment = new Comment(10L, 1L, "content");
        LocalDateTime afterCreate = LocalDateTime.now();

        //then
        assertNull(comment.getCommentId());
        assertEquals(10L, comment.getIssueId());
        assertEquals(1L, comment.getAuthorId());
        assertEquals("content", comment.getContent());
        assertFalse(comment.getCreatedDate().isBefore(beforeCreate));
        assertFalse(comment.getCreatedDate().isAfter(afterCreate));
        assertFalse(comment.getUpdatedDate().isBefore(beforeCreate));
        assertFalse(comment.getUpdatedDate().isAfter(afterCreate));
    }

    @Test
    @DisplayName("댓글 내용을 수정하면 내용이 변경되고 수정 시간이 갱신된다")
    void updateContentChangesContentAndRefreshesUpdatedDate() {
        //given
        Comment comment = new Comment(1L, 10L, "old content");
        //isAfter 검증 시 컴퓨터의 연산 속도가 너무 빠르므로 '동시성' 문제가 발생할 수 있으므로 이를 방지하기 위해 1나노초 과거로 설정
        LocalDateTime oldUpdatedDate = comment.getUpdatedDate().minusNanos(1);

        //when
        comment.updateContent("new content");

        //then
        assertEquals("new content", comment.getContent());
        assertTrue(comment.getUpdatedDate().isAfter(oldUpdatedDate));
    }
}
