package com.issuetracker.domain.issue.dto;

public record AssignIssueRequest(
        Long issueId,
        Long assigneeId
) {
}
