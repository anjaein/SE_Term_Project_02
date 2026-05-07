package com.issuetracker.domain.issue.controller;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.issue.service.IssueService;

public class IssueController {
    private IssueService issueService = new IssueService();
    private AccountController accountController;

    public IssueController(AccountController accountController){
        this.accountController = accountController;
    }

    public void createIssue(Long projectId, String title, String description, Long reporterId){
        Account currentUser = accountController.getLoggedInAccount();
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
