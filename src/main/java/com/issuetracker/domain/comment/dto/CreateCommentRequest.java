package com.issuetracker.domain.comment.dto;

public record CreateCommentRequest(
        Long issueId,
        String content
) {
}
