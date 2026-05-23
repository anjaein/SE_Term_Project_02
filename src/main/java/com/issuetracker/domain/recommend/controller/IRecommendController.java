package com.issuetracker.domain.recommend.controller;

import com.issuetracker.domain.account.entity.Account;

import java.util.List;

public interface IRecommendController {
    List<Account> getRecommendedAssignees(Long projectId, String title, String description);
}
