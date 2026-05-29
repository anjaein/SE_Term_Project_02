package com.issuetracker.domain.issue.service;

import com.issuetracker.domain.project.enums.Role;
import com.issuetracker.domain.project.entity.ProjectMember;
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

    // 요청자가 해당 프로젝트의 PL인지 검증
    public String checkRequesterIsProjectLead(Long projectId, Long requesterId) {
        ProjectMember requester = projectMemberRepository.findByProjectIdAndAccountId(projectId, requesterId);
        if (requester == null || requester.getRole() != Role.PL) {
            return "Only a PL can perform this action.";
        }
        return null;
    }

    // 지정 대상이 해당 프로젝트의 DEV인지 검증
    public String checkAssigneeIsDev(Long projectId, Long assigneeId) {
        ProjectMember assignee = projectMemberRepository.findByProjectIdAndAccountId(projectId, assigneeId);
        if (assignee == null || assignee.getRole() != Role.DEV) {
            return "Assignee must be a DEV of the project.";
        }
        return null;
    }

    // 요청자가 이슈의 assignee인지 검증
    public String checkRequesterIsAssignee(Long assigneeId, Long requesterId) {
        if (!requesterId.equals(assigneeId)) {
            return "Only the assignee can fix the issue.";
        }
        return null;
    }

    // 요청자가 이슈의 reporter인지 검증
    public String checkRequesterIsReporter(Long reporterId, Long requesterId) {
        if (!requesterId.equals(reporterId)) {
            return "Only the reporter can resolve the issue.";
        }
        return null;
    }
}