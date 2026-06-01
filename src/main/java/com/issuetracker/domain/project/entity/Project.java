package com.issuetracker.domain.project.entity;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
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
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
