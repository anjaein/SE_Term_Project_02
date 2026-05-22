package com.issuetracker.domain.issue.repository;

import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;

import java.util.List;

public interface IssueRepository {
    List<Issue> findAll();
    List<Issue> findByProjectId(Long projectId);
    List<Issue> findByAssigneeId(Long assigneeId);
    List<Issue> findByReporterId(Long reporterId);
    List<Issue> findByStatus(Status status);
    List<Issue> findByPriority(Priority priority);
    Issue findByIssueId(Long issueId);
    boolean save(Issue issue);
    boolean update(Issue issue);
}