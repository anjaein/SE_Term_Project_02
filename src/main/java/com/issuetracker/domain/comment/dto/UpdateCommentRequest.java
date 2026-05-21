package com.issuetracker.domain.comment.dto;

public record UpdateCommentRequest(
        Long commentId,
        String newContent
) {
}
