package com.issuetracker;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.account.repository.JsonAccountRepository;
import com.issuetracker.domain.account.service.AccountService;
import com.issuetracker.domain.account.service.AccountValidator;
import com.issuetracker.domain.comment.controller.CommentController;
import com.issuetracker.domain.comment.repository.CommentRepository;
import com.issuetracker.domain.comment.repository.JsonCommentRepository;
import com.issuetracker.domain.comment.service.CommentService;
import com.issuetracker.domain.comment.service.CommentValidator;
import com.issuetracker.domain.issue.controller.IssueController;
import com.issuetracker.domain.issue.controller.IssueStatisticsController;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.issue.repository.JsonIssueRepository;
import com.issuetracker.domain.issue.service.IssueService;
import com.issuetracker.domain.issue.service.IssueStatisticsService;
import com.issuetracker.domain.issue.service.IssueStatisticsValidator;
import com.issuetracker.domain.issue.service.IssueValidator;
import com.issuetracker.domain.project.controller.ProjectController;
import com.issuetracker.domain.project.repository.JsonProjectMemberRepository;
import com.issuetracker.domain.project.repository.JsonProjectRepository;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectRepository;
import com.issuetracker.domain.project.service.ProjectService;
import com.issuetracker.domain.project.service.ProjectValidator;
import com.issuetracker.domain.recommend.controller.IRecommendController;
import com.issuetracker.domain.recommend.controller.RecommendController;
import com.issuetracker.domain.recommend.service.IRecommendService;
import com.issuetracker.domain.recommend.service.RecommendService;
import com.issuetracker.global.common.Backend;
import com.issuetracker.global.common.SessionManager;

public class Main {
    public static void main(String[] args) {
        Backend backend = Backend();
    }

    public static Backend Backend() {
        AccountRepository accountRepository = new JsonAccountRepository();
        ProjectRepository projectRepository = new JsonProjectRepository();
        ProjectMemberRepository projectMemberRepository = new JsonProjectMemberRepository();
        IssueRepository issueRepository = new JsonIssueRepository();
        CommentRepository commentRepository = new JsonCommentRepository();

        SessionManager sessionManager = new SessionManager();

        AccountValidator accountValidator = new AccountValidator(accountRepository);
        ProjectValidator projectValidator = new ProjectValidator(projectRepository);
        IssueValidator issueValidator = new IssueValidator(projectMemberRepository, projectRepository);
        IssueStatisticsValidator issueStatisticsValidator = new IssueStatisticsValidator();
        CommentValidator commentValidator = new CommentValidator(issueRepository, projectMemberRepository);

        AccountService accountService = new AccountService(accountRepository, accountValidator);
        ProjectService projectService = new ProjectService(projectRepository, projectMemberRepository, projectValidator);
        IssueService issueService = new IssueService(issueRepository, issueValidator);
        CommentService commentService = new CommentService(commentRepository, commentValidator);
        IssueStatisticsService issueStatisticsService = new IssueStatisticsService(issueRepository, issueStatisticsValidator);
        IRecommendService recommendService = new RecommendService(issueRepository);

        AccountController accountController = new AccountController(accountService, sessionManager);
        ProjectController projectController = new ProjectController(projectService, accountService, sessionManager);
        IssueController issueController = new IssueController(issueService, sessionManager);
        CommentController commentController = new CommentController(commentService, sessionManager);
        IssueStatisticsController issueStatisticsController = new IssueStatisticsController(issueStatisticsService, sessionManager);
        IRecommendController recommendController = new RecommendController(recommendService, accountRepository);

        return new Backend(
                accountController,
                projectController,
                issueController,
                commentController,
                issueStatisticsController,
                recommendController
        );
    }
}