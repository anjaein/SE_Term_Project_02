package com.issuetracker.domain.issue.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.service.IssueService;
import com.issuetracker.global.common.SessionManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IssueController {
    private final IssueService issueService;
    private final SessionManager sessionManager;

    public void createIssue(Long projectId, String title, String description, Long reporterId){
        Account currentUser = sessionManager.getLoggedInAccount();
        if(currentUser == null){
            notifyError("You are not logged in.");
            return;
        } else if(!currentUser.getAccountId().equals(reporterId)){
            notifyError("You are not authorized to perform this action.");
            return;
        }
        if(issueService.createIssue(projectId, title, description, reporterId)){
            notifySuccess("Issue created.");
        } else {
            notifyError("Failed to create the Issue.");
        }
    }

    public void printIssueDetail(Long issueId){
        Account currentUser = sessionManager.getLoggedInAccount();
        if(currentUser == null){
            notifyError("You are not logged in.");
            return;
        }

        Issue issue = issueService.getIssueById(issueId);
        if(issue == null){
            notifyError("Issue does not exist.");
            return;
        }

        printIssueInfo(issue);
    }

    public void assignIssue(Long issueId, Long assigneeId){
        Account currentUser = sessionManager.getLoggedInAccount();
        if(currentUser == null){
            notifyError("You are not logged in.");
            return;
        }

        if(issueService.assignIssue(issueId, assigneeId, currentUser.getAccountId())){
            notifySuccess("Issue assigned.");
        } else {
            notifyError("Failed to assign the Issue.");
        }
    }

    private void printIssueInfo(Issue issue){
        System.out.println("[INFO] Issue detail");
        System.out.println("  - issueId: " + issue.getIssueId());
        System.out.println("  - projectId: " + issue.getProjectId());
        System.out.println("  - title: " + issue.getTitle());
        System.out.println("  - description: " + issue.getDescription());
        System.out.println("  - reporterId: " + issue.getReporterId());
        System.out.println("  - assigneeId: " + issue.getAssigneeId());
        System.out.println("  - fixerId: " + issue.getFixerId());
        System.out.println("  - priority: " + issue.getPriority());
        System.out.println("  - status: " + issue.getStatus());
        System.out.println("  - reportedDate: " + issue.getReportedDate());
        System.out.println("  - fixedDate: " + issue.getFixedDate());
        System.out.println("  - resolvedDate: " + issue.getResolvedDate());
        System.out.println("  - closedDate: " + issue.getClosedDate());
    }

    private void notifySuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    private void notifyError(String message) {
        System.out.println("[ERROR] " + message);
    }
}
