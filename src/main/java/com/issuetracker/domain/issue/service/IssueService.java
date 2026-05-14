package com.issuetracker.domain.issue.service;

import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;

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
}
