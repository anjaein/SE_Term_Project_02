package com.issuetracker.domain.project.entity;

import com.issuetracker.domain.issue.entity.Issue;

import java.time.LocalDateTime;

public class Project {
    private Long projectId;
    private String name;
    private Long createdBy;
    private LocalDateTime createdDate;

    public Project(String name, Long createdBy) {
        this.name = name;
        this.createdBy = createdBy;
        this.createdDate = LocalDateTime.now();
    }

    public Long getProjectId() {
        return projectId;
    }
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

}
