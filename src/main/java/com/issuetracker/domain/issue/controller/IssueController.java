package com.issuetracker.domain.issue.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.issue.service.IssueService;
import com.issuetracker.global.common.SessionManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IssueController {
    private final IssueService issueService;
    private final SessionManager sessionManager;

    public void createIssue(Long projectId, String title, String description, Long reporterId){
        Account currentUser = sessionManager.getLoggedInAccount();
        if (title == null || title.trim().isEmpty()) {  //issue title이 null이거나 공백이면 예외처리
            notifyError("Issue title cannot be empty.");
            return;
        }
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

    private void notifySuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    private void notifyError(String message) {
        System.out.println("[ERROR] " + message);
    }
}
