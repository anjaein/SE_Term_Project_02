package ui.swing;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.comment.controller.CommentController;
import com.issuetracker.domain.issue.controller.IssueController;
import com.issuetracker.domain.project.controller.ProjectController;
import com.issuetracker.global.common.SessionManager;
import com.issuetracker.domain.recommend.controller.RecommendController;
import com.issuetracker.domain.issue.controller.IssueStatisticsController;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final AccountController accountController;
    private final ProjectController projectController;
    private final IssueController issueController;
    private final CommentController commentController;
    private final SessionManager sessionManager;
    private final RecommendController recommendController;
    private final IssueStatisticsController issueStatisticsController;

    private CardLayout cardLayout;
    private JPanel mainContainer;

    public MainFrame(AccountController accountController, ProjectController projectController,
                     IssueController issueController, CommentController commentController,  IssueStatisticsController issueStatisticsController,
                     SessionManager sessionManager, RecommendController recommendController) {
        this.accountController = accountController;
        this.projectController = projectController;
        this.issueController = issueController;
        this.commentController = commentController;
        this.sessionManager = sessionManager;
        this.recommendController = recommendController;
        this.issueStatisticsController = issueStatisticsController;

        setTitle("Issue Tracking System (Swing UI)");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        initLoginPanel();

        add(mainContainer);
        cardLayout.show(mainContainer, "LOGIN");
    }

    private void initLoginPanel() {
        mainContainer.add(new LoginPanel(accountController, this::showDashboard), "LOGIN");
    }

    private void showDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout());
        Account curuser = sessionManager.getLoggedInAccount();

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(230, 230, 230));
        topBar.add(new JLabel("  User: " + curuser.getUsername() + " | Role: " + curuser.getRole()), BorderLayout.WEST);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            accountController.logout();
            cardLayout.show(mainContainer, "LOGIN");
        });
        topBar.add(logoutBtn, BorderLayout.EAST);
        dashboard.add(topBar, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        if (curuser.getRole() == Role.ADMIN) {
            tabs.addTab("Admin Console", new AdminPanel(accountController, projectController));
        }
        tabs.addTab("Projects & Members", new ProjectPanel(
                accountController,
                projectController,
                sessionManager
        ));
        tabs.addTab("Issue Management", new IssuePanel(
                this,
                accountController,
                projectController,
                issueController,
                commentController,
                issueStatisticsController,
                recommendController,
                sessionManager
        ));

        dashboard.add(tabs, BorderLayout.CENTER);
        mainContainer.add(dashboard, "DASHBOARD");
        cardLayout.show(mainContainer, "DASHBOARD");
    }

}
