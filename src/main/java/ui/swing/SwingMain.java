package ui.swing;

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

import com.issuetracker.global.common.SessionManager;
import com.issuetracker.domain.recommend.controller.RecommendController;
import com.issuetracker.domain.recommend.service.RecommendService;

import javax.swing.*;

public class SwingMain {
    public static void main(String[] args) {
        AccountRepository accountRepository = new JsonAccountRepository();
        ProjectRepository projectRepository = new JsonProjectRepository();
        ProjectMemberRepository projectMemberRepository = new JsonProjectMemberRepository();
        IssueRepository issueRepository = new JsonIssueRepository();
        CommentRepository commentRepository = new JsonCommentRepository();

        SessionManager sessionManager = new SessionManager();

        AccountValidator accountValidator = new AccountValidator(accountRepository);
        ProjectValidator projectValidator = new ProjectValidator(projectRepository);
        IssueValidator issueValidator = new IssueValidator(projectMemberRepository, projectRepository);
        CommentValidator commentValidator = new CommentValidator(issueRepository, projectMemberRepository);

        AccountService accountService = new AccountService(accountRepository, accountValidator);
        ProjectService projectService = new ProjectService(projectRepository, projectMemberRepository, projectValidator);
        IssueService issueService = new IssueService(issueRepository, issueValidator);
        CommentService commentService = new CommentService(commentRepository, commentValidator);

        AccountController accountController = new AccountController(accountService, sessionManager);
        ProjectController projectController = new ProjectController(projectService, accountService, sessionManager);
        IssueController issueController = new IssueController(issueService, sessionManager);
        CommentController commentController = new CommentController(commentService, sessionManager);

        RecommendService recommendService = new RecommendService(issueRepository);
        RecommendController recommendController = new RecommendController(recommendService, accountRepository);

        IssueStatisticsValidator issueStatisticsValidator = new IssueStatisticsValidator();
        IssueStatisticsService issueStatisticsService = new IssueStatisticsService(issueRepository, issueStatisticsValidator);
        IssueStatisticsController issueStatisticsController =
                new IssueStatisticsController(issueStatisticsService, sessionManager);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            MainFrame mainFrame = new MainFrame(
                    accountController, projectController, issueController, commentController,
                    issueStatisticsController, sessionManager, recommendController
            );
            mainFrame.setVisible(true);
        });
    }
}
