package com.issuetracker.domain.issue.dto;

import com.issuetracker.domain.issue.enums.Status;

public record ChangeIssueStateRequest(
        Long issueId,
        Status targetStatus
) {
}
