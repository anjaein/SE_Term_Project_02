package com.issuetracker.domain.issue.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.service.IssueService;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IssueController {
    private final IssueService issueService;
    private final SessionManager sessionManager;

    public Response<Issue> createIssue(Long projectId, String title, String description){
        Account currentUser = sessionManager.getLoggedInAccount();
        if(currentUser == null){
            return Response.fail("You are not logged in.");
        }
        // reporter는 항상 로그인한 사용자 본인
        return issueService.createIssue(projectId, title, description, currentUser.getAccountId());
    }

    public Response<Issue> getIssueDetail(Long issueId){
        Account currentUser = sessionManager.getLoggedInAccount();
        if(currentUser == null){
            return Response.fail("You are not logged in.");
        }
        return issueService.getIssueById(issueId);
    }

    public Response<Issue> assignIssue(Long issueId, Long assigneeId){
        Account currentUser = sessionManager.getLoggedInAccount();
        if(currentUser == null){
            return Response.fail("You are not logged in.");
        }
        return issueService.assignIssue(issueId, assigneeId, currentUser.getAccountId());
    }

    public Response<Issue> fixIssue(Long issueId){
        Account currentUser = sessionManager.getLoggedInAccount();
        if(currentUser == null){
            return Response.fail("You are not logged in.");
        }
        return issueService.fixIssue(issueId, currentUser.getAccountId());
    }

    public Response<Issue> resolveIssue(Long issueId){
        Account currentUser = sessionManager.getLoggedInAccount();
        if(currentUser == null){
            return Response.fail("You are not logged in.");
        }
        return issueService.resolveIssue(issueId, currentUser.getAccountId());
    }

    public Response<Issue> closeIssue(Long issueId){
        Account currentUser = sessionManager.getLoggedInAccount();
        if(currentUser == null){
            return Response.fail("You are not logged in.");
        }
        return issueService.closeIssue(issueId, currentUser.getAccountId());
    }
}