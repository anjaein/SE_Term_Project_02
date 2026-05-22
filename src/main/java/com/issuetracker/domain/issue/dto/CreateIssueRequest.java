package com.issuetracker.domain.issue.dto;

public record CreateIssueRequest(
        Long projectId,
        String title,
        String description,
        Long reporterId
) {
}
