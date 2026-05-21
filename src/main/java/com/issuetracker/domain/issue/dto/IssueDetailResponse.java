package com.issuetracker.domain.issue.dto;

import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;

import java.time.LocalDateTime;

public record IssueDetailResponse(
        Long issueId,
        Long projectId,
        String title,
        String description,
        Long reporterId,
        Long assigneeId,
        Long fixerId,
        Priority priority,
        Status status,
        LocalDateTime reportedDate,
        LocalDateTime fixedDate,
        LocalDateTime resolvedDate,
        LocalDateTime closedDate
) {
}
