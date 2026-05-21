package ui;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.account.service.AccountService;
import com.issuetracker.domain.comment.controller.CommentController;
import com.issuetracker.domain.comment.repository.CommentRepository;
import com.issuetracker.domain.comment.service.CommentService;
import com.issuetracker.domain.issue.controller.IssueController;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.issue.service.IssueService;
import com.issuetracker.domain.project.controller.ProjectController;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectRepository;
import com.issuetracker.domain.project.service.ProjectService;
import com.issuetracker.global.common.SessionManager;

import javax.swing.*;

public class SwingMain {
    public static void main(String[] args) {
        AccountRepository accountRepository = new AccountRepository();
        ProjectRepository projectRepository = new ProjectRepository();
        ProjectMemberRepository projectMemberRepository = new ProjectMemberRepository();
        IssueRepository issueRepository = new IssueRepository();
        CommentRepository commentRepository = new CommentRepository();

        SessionManager sessionManager = new SessionManager();

        AccountService accountService = new AccountService(accountRepository);
        ProjectService projectService = new ProjectService(projectRepository, projectMemberRepository);
        IssueService issueService = new IssueService(issueRepository, projectMemberRepository);
        CommentService commentService = new CommentService(commentRepository, accountRepository, issueRepository);

        AccountController accountController = new AccountController(accountService, sessionManager);
        ProjectController projectController = new ProjectController(projectService, accountController, sessionManager);
        IssueController issueController = new IssueController(issueService, sessionManager);
        CommentController commentController = new CommentController(commentService, sessionManager);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            MainFrame mainFrame = new MainFrame(
                    accountController, projectController, issueController, commentController,
                    sessionManager, accountService, projectService, issueService, commentService,
                    projectRepository
            );
            mainFrame.setVisible(true);
        });
    }
}

