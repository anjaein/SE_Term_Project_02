package com.issuetracker.ui.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
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

    public Issue(Long issueId, Long projectId, String title, String description, Long reporterId){
        this.issueId=issueId;
        this.projectId=projectId;
        this.title=title;
        this.description=description;
        this.reporterId=reporterId;
        this.priority=Priority.MAJOR;
        this.status=Status.NEW;
        this.reportedDate=LocalDateTime.now();
    }
}
