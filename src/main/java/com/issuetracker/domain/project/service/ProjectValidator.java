package com.issuetracker.domain.project.service;

import com.issuetracker.domain.project.repository.ProjectRepository;
import com.issuetracker.global.common.Validator;
import lombok.RequiredArgsConstructor;

// Project 도메인용 Validator
@RequiredArgsConstructor
public class ProjectValidator implements Validator {
    private final ProjectRepository projectRepository;

    // Project가 존재하는지 검증
    public String checkProjectExists(Long projectId) {
        if (projectRepository.findByProjectId(projectId) == null) {
            return "Project does not exist.";
        }
        return null;
    }
}