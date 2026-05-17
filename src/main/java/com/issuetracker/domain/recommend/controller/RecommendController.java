package com.issuetracker.domain.recommend.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class RecommendController {
    private final RecommendService recommendService;
    private final AccountRepository accountRepository;

    // Recommended Assignees 출력 테스트용
    public void printRecommendedAssignees(Long projectId, String title, String description) {
        List<Long> recommendations = recommendService.recommendAssignees(projectId, title, description);
        if (recommendations.isEmpty()) {
            System.out.println("[INFO] No assignee recommendations available.");
            return;
        }

        // Recommended Assignees 정보 출력
        System.out.println("[INFO] Recommended assignees:");
        for (int i = 0; i < recommendations.size(); i++) {
            Long accountId = recommendations.get(i);
            Account account = accountRepository.findById(accountId);
            String username = account != null ? account.getUsername() : "unknown";
            System.out.println("  " + (i + 1) + ". " + username + " (id: " + accountId + ")");
        }
    }
}
