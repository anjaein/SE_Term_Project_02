package com.issuetracker.domain.recommend.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.global.common.Response;

import java.util.List;

public interface IRecommendController {
    Response<List<Account>> getRecommendedAssignees(Long projectId, String title, String description);
}
