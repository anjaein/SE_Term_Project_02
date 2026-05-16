package com.issuetracker.domain.issue.entity;

import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class Issue {
    @Setter
    private Long issueId;
    private Long projectId;
    @Setter
    private String title;
    @Setter
    private String description;
    @Setter
    private Priority priority;
    private Status status;
    private Long reporterId;
    @Setter
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

    public void assignTo(Long assigneeId) {
        this.assigneeId = assigneeId;
        this.status = Status.ASSIGNED;
    }


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
