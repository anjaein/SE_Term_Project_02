package com.issuetracker.domain.recommend.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.recommend.service.IRecommendService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RecommendController implements IRecommendController {
    private final IRecommendService recommendService;
    private final AccountRepository accountRepository;

    // RecommendedAssignees 반환
    public List<Account> getRecommendedAssignees(Long projectId, String title, String description) {
        return recommendService.recommendAssignees(projectId, title, description).stream()
                .map(accountRepository::findById)
                .filter(account -> account != null)
                .collect(Collectors.toList());
    }
}
