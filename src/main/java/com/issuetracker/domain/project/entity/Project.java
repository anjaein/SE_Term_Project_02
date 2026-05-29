package com.issuetracker.domain.project.entity;

import java.time.LocalDateTime;
import lombok.Getter;

public class Project {
    private Long projectId;
    @Getter
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

    public String getName() {
        return name;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

}
