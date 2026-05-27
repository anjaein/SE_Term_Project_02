package com.issuetracker.global.common;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.comment.controller.CommentController;
import com.issuetracker.domain.issue.controller.IssueController;
import com.issuetracker.domain.issue.controller.IssueStatisticsController;
import com.issuetracker.domain.project.controller.ProjectController;
import com.issuetracker.domain.recommend.controller.IRecommendController;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Backend {
    public final AccountController accountController;
    public final ProjectController projectController;
    public final IssueController issueController;
    public final CommentController commentController;
    public final IssueStatisticsController issueStatisticsController;
    public final IRecommendController recommendController;
}