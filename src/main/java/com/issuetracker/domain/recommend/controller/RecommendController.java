package com.issuetracker.domain.recommend.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.recommend.service.RecommendService;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RecommendController {
    private final RecommendService recommendService;
    private final AccountRepository accountRepository;
    private final SessionManager sessionManager;

    public Response<List<Account>> getRecommendedAssignees(Long projectId, String title, String description) {
        Account currentUser = sessionManager.getLoggedInAccount();
        if (currentUser == null) {
            return Response.fail("You are not logged in.");
        }
        List<Account> result = recommendService.recommendAssignees(projectId, title, description).stream()
                .map(accountRepository::findById)
                .filter(account -> account != null)
                .collect(Collectors.toList());
        return Response.success("Recommended assignees retrieved.", result);
    }
}