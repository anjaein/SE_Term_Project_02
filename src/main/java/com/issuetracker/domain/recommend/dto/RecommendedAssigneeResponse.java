package com.issuetracker.domain.recommend.dto;

import com.issuetracker.domain.account.enums.Role;

public record RecommendedAssigneeResponse(
        Long accountId,
        String username,
        Role role
) {
}
