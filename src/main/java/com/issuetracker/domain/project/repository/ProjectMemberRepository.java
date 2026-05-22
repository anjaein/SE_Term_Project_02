package com.issuetracker.domain.project.repository;

import com.issuetracker.domain.project.entity.ProjectMember;

import java.util.List;

public interface ProjectMemberRepository {
    List<ProjectMember> findAll();
    List<ProjectMember> findByProjectId(Long projectId);
    ProjectMember findByProjectIdAndAccountId(Long projectId, Long accountId);
    boolean save(ProjectMember projectMember);
}