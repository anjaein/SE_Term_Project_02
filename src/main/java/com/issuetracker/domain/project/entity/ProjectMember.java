package com.issuetracker.domain.project.entity;

import com.issuetracker.domain.account.enums.Role;

public class ProjectMember {
    private Long projectId;
    private Long accountId;
    private Role role;

    public ProjectMember(Long projectId, Long accountId, Role role) {
        this.projectId = projectId;
        this.accountId = accountId;
        this.role = role;
    }

    public Long getProjectId() {
        return projectId;
    }
    public Long getAccountId() {
        return accountId;
    }
    public Role getRole() {
        return role;
    }
}
