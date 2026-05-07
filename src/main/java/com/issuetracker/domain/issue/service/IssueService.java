package com.issuetracker.domain.issue.service;

import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;

public class IssueService {
    private IssueRepository issueRepository = new IssueRepository();
    private ProjectMemberRepository projectMemberRepository  = new ProjectMemberRepository();

    public boolean createIssue(Long projectId, String title, String description, Long reporterId){
        if(projectMemberRepository.findByProjectIdAndAccountId(projectId, reporterId) == null){
            return false;
        }

        Issue issue = new Issue(projectId, title, description, reporterId);
        return issueRepository.save(issue);
    }
}
