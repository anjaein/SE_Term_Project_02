package com.issuetracker.domain.issue.service;

import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectRepository;
import com.issuetracker.global.common.Validator;
import lombok.RequiredArgsConstructor;

// Issue 도메인용 Validator
@RequiredArgsConstructor
public class IssueValidator implements Validator {
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;

    // Issue가 속할 Project가 존재하는지 검증
    public String checkProjectExists(Long projectId) {
        if (projectRepository.findByProjectId(projectId) == null) {
            return "Project does not exist.";
        }
        return null;
    }

    // 작업 대상 accountId가 Project의 멤버인지 검증
    public String checkProjectMember(Long projectId, Long accountId) {
        if (projectMemberRepository.findByProjectIdAndAccountId(projectId, accountId) == null) {
            return "Reporter is not a member of the project.";
        }
        return null;
    }
}