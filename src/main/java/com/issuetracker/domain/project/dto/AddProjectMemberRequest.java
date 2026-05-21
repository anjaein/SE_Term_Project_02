package com.issuetracker.domain.project.dto;

import com.issuetracker.domain.account.enums.Role;

public record AddProjectMemberRequest(
        Long projectId,
        String username,
        Role role
) {
}
