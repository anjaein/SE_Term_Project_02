package com.issuetracker.domain.issue.entity;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.project.entity.Project;

import java.time.LocalDateTime;

public class Issue {
    private Long issueId;
    private Long projectId;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private Long reporterId;
    private Long assigneeId;
    private Long fixerId;
    private LocalDateTime reportedDate;
    private LocalDateTime fixedDate;
    private LocalDateTime resolvedDate;
    private LocalDateTime closedDate;

    public Issue(Long projectId, String title, String description, Long reporterId){
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.priority = Priority.MAJOR;
        this.status = Status.NEW;
        this.reporterId = reporterId;
        this.reportedDate = LocalDateTime.now();
    }

    public Long getIssueId(){
        return issueId;
    }
    public void setIssueId(Long issueId){
        this.issueId = issueId;
    }

    // 아래 코드는 상태 변경 시 자동 기록하는 코드
    // markAsFixed, markAsResolved, markAsClosed
    public void markAsFixed(Long fixerId) {
        this.status = Status.FIXED;
        this.fixerId = fixerId;
        this.fixedDate = LocalDateTime.now();
    }

    public void markAsResolved() {
        this.status = Status.RESOLVED;
        this.resolvedDate = LocalDateTime.now();
    }

    public void markAsClosed() {
        this.status = Status.CLOSED;
        this.closedDate = LocalDateTime.now();
    }

}
