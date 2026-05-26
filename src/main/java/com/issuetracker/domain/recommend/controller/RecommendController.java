package com.issuetracker.domain.recommend.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.recommend.service.IRecommendService;
import com.issuetracker.global.common.Response;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RecommendController implements IRecommendController {
    private final IRecommendService recommendService;
    private final AccountRepository accountRepository;

    // RecommendedAssignees 반환
    @Override
    public Response<List<Account>> getRecommendedAssignees(Long projectId, String title, String description) {
        Response<List<Long>> recommendResult = recommendService.recommendAssignees(projectId, title, description);
        if (!recommendResult.isSuccess()) {
            return Response.fail(recommendResult.getMessage());
        }

        List<Account> result = recommendResult.getData().stream()
                .map(accountRepository::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return Response.success("Recommended assignees retrieved.", result);
    }
}
