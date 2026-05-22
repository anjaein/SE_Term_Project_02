package com.issuetracker.domain.project.repository;

import com.issuetracker.domain.project.entity.Project;

import java.util.List;

public interface ProjectRepository {
    List<Project> findAll();
    Project findByProjectId(Long projectId);
    boolean save(Project project);
}