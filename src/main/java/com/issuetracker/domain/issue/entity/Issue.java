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
    private Account reporter;
    private Account assignee;
    private Account fixer;
    private LocalDateTime reportedDate;
    private LocalDateTime fixedDate;
    private LocalDateTime resolvedDate;
    private LocalDateTime closedDate;

}
