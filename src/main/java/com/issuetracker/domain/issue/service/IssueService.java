package com.issuetracker.domain.issue.service;

import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.enums.Priority;


import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public boolean createIssue(Long projectId, String title, String description, Long reporterId){

        // 요청에 누락은 없는지
        if(projectId == null || title == null || description == null || reporterId == null){
            return false;
        }

        // title 필수값 검증
        if(title.trim().isEmpty()){
            return false;
        }

        // description 필수값 검증
        if(description.trim().isEmpty()){
            return false;
        }

        // Issue 생성을 요청한 reporterId가 project의 member인지 확인
        if(projectMemberRepository.findByProjectIdAndAccountId(projectId, reporterId) == null){
            return false;
        }

        Issue issue = new Issue(projectId, title, description, reporterId);
        return issueRepository.save(issue);
    }

    public List<Issue> getAllIssues(){
        return issueRepository.findAll();
    }

    public List<Issue> getIssuesByProjectId(Long projectId){
        if(projectId == null) return List.of();
        return issueRepository.findByProjectId(projectId);
    }

    public List<Issue> getIssuesByAssigneeId(Long assigneeId){
        if(assigneeId == null) return List.of();
        return issueRepository.findByAssigneeId(assigneeId);
    }

    public List<Issue> getIssuesByReporterId(Long reporterId){
        if(reporterId == null) return List.of();
        return issueRepository.findByReporterId(reporterId);
    }

    public List<Issue> getIssuesByStatus(Status status){
        if(status == null) return List.of();
        return issueRepository.findByStatus(status);
    }

    public List<Issue> getIssuesByPriority(Priority priority){
        if(priority == null) return List.of();
        return issueRepository.findByPriority(priority);
    }


}
