package ui;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.account.service.AccountService;
import com.issuetracker.domain.comment.controller.CommentController;
import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.domain.comment.service.CommentService;
import com.issuetracker.domain.issue.controller.IssueController;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.service.IssueService;
import com.issuetracker.domain.project.controller.ProjectController;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.ProjectRepository;
import com.issuetracker.domain.project.service.ProjectService;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {
    private final AccountController accountController;
    private final ProjectController projectController;
    private final IssueController issueController;
    private final CommentController commentController;
    private final SessionManager sessionManager;
    private final AccountService accountService;
    private final ProjectService projectService;
    private final IssueService issueService;
    private final CommentService commentService;
    private final ProjectRepository projectRepository;

    private CardLayout cardLayout;
    private JPanel mainContainer;

    public MainFrame(AccountController accountController, ProjectController projectController,
                     IssueController issueController, CommentController commentController,
                     SessionManager sessionManager, AccountService accountService,
                     ProjectService projectService, IssueService issueService,
                     CommentService commentService, ProjectRepository projectRepository) {
        this.accountController = accountController;
        this.projectController = projectController;
        this.issueController = issueController;
        this.commentController = commentController;
        this.sessionManager = sessionManager;
        this.accountService = accountService;
        this.projectService = projectService;
        this.issueService = issueService;
        this.commentService = commentService;
        this.projectRepository = projectRepository;

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
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JTextField userField = new JTextField(20);
        JPasswordField passField = new JPasswordField(20);
        JButton loginBtn = new JButton("Login");

        gbc.gridx = 0; gbc.gridy = 0; loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; loginPanel.add(userField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; loginPanel.add(passField, gbc);
        gbc.gridx = 1; gbc.gridy = 2; loginPanel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            Response<Account> response = accountController.login(username, password);
            if (response.isSuccess()) {
                showDashboard();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        mainContainer.add(loginPanel, "LOGIN");
    }

    private void showDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout());
        Account current = sessionManager.getLoggedInAccount();

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(230, 230, 230));
        topBar.add(new JLabel("  User: " + current.getUsername() + " | Role: " + current.getRole()), BorderLayout.WEST);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            accountController.logout();
            cardLayout.show(mainContainer, "LOGIN");
        });
        topBar.add(logoutBtn, BorderLayout.EAST);
        dashboard.add(topBar, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        if (current.getRole() == Role.ADMIN) tabs.addTab("Admin Console", createAdminPanel());
        tabs.addTab("Projects & Members", createProjectPanel());
        tabs.addTab("Issue Management", createIssuePanel());

        dashboard.add(tabs, BorderLayout.CENTER);
        mainContainer.add(dashboard, "DASHBOARD");
        cardLayout.show(mainContainer, "DASHBOARD");
    }

    private JPanel createAdminPanel() {
        JPanel p = new JPanel(new GridLayout(2, 1, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel acc = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acc.setBorder(BorderFactory.createTitledBorder("Add Account"));
        JTextField u = new JTextField(10); JTextField pw = new JTextField(10);
        JComboBox<Role> r = new JComboBox<>(Role.values());
        JButton b = new JButton("Create Account");
        b.addActionListener(e -> {
            Response<Account> resp = accountController.createAccount(u.getText(), pw.getText(), (Role)r.getSelectedItem());
            if (resp.isSuccess()) {
                JOptionPane.showMessageDialog(this, resp.getMessage());
            } else {
                JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        acc.add(new JLabel("User:")); acc.add(u); acc.add(new JLabel("Pass:")); acc.add(pw);
        acc.add(new JLabel("Role:")); acc.add(r); acc.add(b);

        JPanel proj = new JPanel(new FlowLayout(FlowLayout.LEFT));
        proj.setBorder(BorderFactory.createTitledBorder("Add Project"));
        JTextField pn = new JTextField(20);
        JButton pb = new JButton("Create Project");
        pb.addActionListener(e -> {
            Response<Project> resp = projectController.createProject(pn.getText());
            if (resp.isSuccess()) {
                JOptionPane.showMessageDialog(this, resp.getMessage());
            } else {
                JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        proj.add(new JLabel("Name:")); proj.add(pn); proj.add(pb);

        p.add(acc); p.add(proj);
        return p;
    }

    private JPanel createProjectPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] pCols = {"Project ID"};
        DefaultTableModel pModel = new DefaultTableModel(pCols, 0);
        JTable pTable = new JTable(pModel);

        String[] mCols = {"Account ID", "Member Role"};
        DefaultTableModel mModel = new DefaultTableModel(mCols, 0);
        JTable mTable = new JTable(mModel);

        JButton refresh = new JButton("Refresh Project List");
        refresh.addActionListener(e -> {
            pModel.setRowCount(0);
            for (Project pr : projectRepository.findAll()) {
                pModel.addRow(new Object[]{pr.getProjectId()});
            }
        });

        pTable.getSelectionModel().addListSelectionListener(e -> {
            int row = pTable.getSelectedRow();
            if (row >= 0) {
                mModel.setRowCount(0);
                Long pid = (Long) pTable.getValueAt(row, 0);
                Response<List<ProjectMember>> resp = projectService.getProjectMembers(pid);
                if (resp.isSuccess()) {
                    for (ProjectMember m : resp.getData()) {
                        mModel.addRow(new Object[]{m.getAccountId(), m.getRole()});
                    }
                } else {
                    JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel split = new JPanel(new GridLayout(1, 2, 10, 10));
        split.add(new JScrollPane(pTable)); split.add(new JScrollPane(mTable));
        p.add(refresh, BorderLayout.NORTH); p.add(split, BorderLayout.CENTER);

        if (sessionManager.getLoggedInAccount().getRole() == Role.ADMIN) {
            JPanel addM = new JPanel(new FlowLayout(FlowLayout.LEFT));
            addM.setBorder(BorderFactory.createTitledBorder("Add Member to Selected Project"));
            JTextField un = new JTextField(10); JComboBox<Role> r = new JComboBox<>(Role.values());
            JButton b = new JButton("Add Member");
            b.addActionListener(e -> {
                int row = pTable.getSelectedRow();
                if (row >= 0) {
                    Response<ProjectMember> resp = projectController.addProjectMember((Long)pTable.getValueAt(row, 0), un.getText(), (Role)r.getSelectedItem());
                    if (resp.isSuccess()) {
                        JOptionPane.showMessageDialog(this, resp.getMessage());
                    } else {
                        JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else JOptionPane.showMessageDialog(this, "Please select a project first.");
            });
            addM.add(new JLabel("User:")); addM.add(un); addM.add(new JLabel("Role:")); addM.add(r); addM.add(b);
            p.add(addM, BorderLayout.SOUTH);
        }
        return p;
    }

    private JPanel createIssuePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<Status> sf = new JComboBox<>(Status.values());
        sf.insertItemAt(null, 0); sf.setSelectedIndex(0);
        JComboBox<Priority> pf = new JComboBox<>(Priority.values());
        pf.insertItemAt(null, 0); pf.setSelectedIndex(0);
        JButton applyFilter = new JButton("Apply Filter");
        JButton all = new JButton("All Issues");
        JButton mine = new JButton("Assigned to Me");
        JButton reported = new JButton("Reported by Me (FIXED)");
        JButton resolved = new JButton("Resolved Issue");

        filter.add(new JLabel("Status:")); filter.add(sf);
        filter.add(new JLabel("Priority:")); filter.add(pf);
        filter.add(applyFilter);
        filter.add(all); filter.add(mine);
        if (sessionManager.getLoggedInAccount().getRole() == Role.TESTER) {
            filter.add(reported);
        }
        if (sessionManager.getLoggedInAccount().getRole() == Role.PL) {
            filter.add(resolved);
        }
        p.add(filter, BorderLayout.NORTH);

        String[] cols = {"ID", "PID", "Title", "Status", "Reporter", "Assignee"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable refreshAll = () -> {
            model.setRowCount(0);
            Response<List<Issue>> resp = issueService.getAllIssues();
            if (resp.isSuccess()) {
                for (Issue i : resp.getData()) {
                    model.addRow(new Object[]{i.getIssueId(), i.getProjectId(), i.getTitle(), i.getStatus(), i.getReporterId(), i.getAssigneeId()});
                }
            } else {
                JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        applyFilter.addActionListener(e -> {
            model.setRowCount(0);
            Status status = (Status) sf.getSelectedItem();
            Priority priority = (Priority) pf.getSelectedItem();
            Response<List<Issue>> resp = issueService.getAllIssues();
            if (resp.isSuccess()) {
                for (Issue i : resp.getData()) {
                    boolean statusMatch = (status == null || i.getStatus() == status);
                    boolean priorityMatch = (priority == null || i.getPriority() == priority);
                    if (statusMatch && priorityMatch) {
                        model.addRow(new Object[]{i.getIssueId(), i.getProjectId(), i.getTitle(), i.getStatus(), i.getReporterId(), i.getAssigneeId()});
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        all.addActionListener(e -> {
            sf.setSelectedIndex(0);
            pf.setSelectedIndex(0);
            refreshAll.run();
        });
        mine.addActionListener(e -> {
            model.setRowCount(0);
            Response<List<Issue>> resp = issueService.getIssuesByAssigneeId(sessionManager.getLoggedInAccount().getAccountId());
            if (resp.isSuccess()) {
                for (Issue i : resp.getData()) {
                    if (i.getStatus() == Status.ASSIGNED) model.addRow(new Object[]{i.getIssueId(), i.getProjectId(), i.getTitle(), i.getStatus(), i.getReporterId(), i.getAssigneeId()});
                }
            } else {
                JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        reported.addActionListener(e -> {
            model.setRowCount(0);
            Response<List<Issue>> resp = issueService.getIssuesByReporterId(sessionManager.getLoggedInAccount().getAccountId());
            if (resp.isSuccess()) {
                for (Issue i : resp.getData()) {
                    if (i.getStatus() == Status.FIXED) model.addRow(new Object[]{i.getIssueId(), i.getProjectId(), i.getTitle(), i.getStatus(), i.getReporterId(), i.getAssigneeId()});
                }
            } else {
                JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        resolved.addActionListener(e -> {
            model.setRowCount(0);
            Response<List<Issue>> resp = issueService.getAllIssues();
            if (resp.isSuccess()) {
                for (Issue i : resp.getData()) {
                    if (i.getStatus() == Status.RESOLVED) model.addRow(new Object[]{i.getIssueId(), i.getProjectId(), i.getTitle(), i.getStatus(), i.getReporterId(), i.getAssigneeId()});
                }
            } else {
                JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton statistics = new JButton("Issue Statistics");
        JButton create = new JButton("New Issue");
        JButton detail = new JButton("View Detail & Actions");
        create.addActionListener(e -> {
            JTextField pid = new JTextField(); JTextField title = new JTextField(); JTextArea desc = new JTextArea(5, 20);
            Object[] msg = {"Project ID:", pid, "Title:", title, "Description:", new JScrollPane(desc)};
            if (JOptionPane.showConfirmDialog(this, msg, "Create Issue", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                Response<Issue> resp = issueController.createIssue(Long.parseLong(pid.getText()), title.getText(), desc.getText());
                if (resp.isSuccess()) {
                    JOptionPane.showMessageDialog(this, resp.getMessage());
                    refreshAll.run();
                } else {
                    JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        detail.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) showIssueDetail((Long)table.getValueAt(r, 0), refreshAll);
        });
        if (sessionManager.getLoggedInAccount().getRole() == Role.TESTER) {
            btns.add(create);
        }
        leftBtns.add(statistics);
        btns.add(detail);
        bottom.add(leftBtns, BorderLayout.WEST);
        bottom.add(btns, BorderLayout.EAST);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    private void showIssueDetail(Long issueId, Runnable refreshTable) {
        Response<Issue> issueResp = issueService.getIssueById(issueId);
        if (!issueResp.isSuccess()) {
            JOptionPane.showMessageDialog(this, issueResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Issue issue = issueResp.getData();

        JDialog d = new JDialog(this, "Issue Detail #" + issueId, true);
        d.setSize(700, 600); d.setLayout(new BorderLayout(10, 10));

        JPanel info = new JPanel(new GridLayout(0, 1));
        info.setBorder(BorderFactory.createTitledBorder("Information"));
        info.add(new JLabel("Title: " + issue.getTitle()));
        info.add(new JLabel("Status: " + issue.getStatus()));
        info.add(new JLabel("Description: " + issue.getDescription()));
        info.add(new JLabel("Reporter ID: " + issue.getReporterId()));
        info.add(new JLabel("Assignee ID: " + issue.getAssigneeId()));
        d.add(info, BorderLayout.NORTH);

        DefaultListModel<Comment> cm = new DefaultListModel<>();
        JList<Comment> cl = new JList<>(cm);
        cl.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel("User " + value.getAuthorId() + ": " + value.getContent());
            label.setOpaque(true);
            label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return label;
        });
        Runnable refreshC = () -> {
            cm.clear();
            Response<List<Comment>> resp = commentService.getCommentsByIssueId(issueId);
            if (resp.isSuccess()) {
                for (Comment c : resp.getData()) cm.addElement(c);
            } else {
                JOptionPane.showMessageDialog(d, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };
        refreshC.run();
        d.add(new JScrollPane(cl), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addC = new JButton("Add Comment");
        addC.addActionListener(e -> {
            String c = JOptionPane.showInputDialog("Comment content:");
            if (c != null && !c.trim().isEmpty()) {
                Response<Comment> resp = commentController.createComment(issueId, c);
                if (resp.isSuccess()) {
                    refreshC.run();
                } else {
                    JOptionPane.showMessageDialog(d, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        actions.add(addC);

        Account cur = sessionManager.getLoggedInAccount();
        JButton updateC = new JButton("Update Comment");
        updateC.addActionListener(e -> {
            Comment selected = cl.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(d, "Please select a comment first.");
                return;
            }
            String updated = JOptionPane.showInputDialog(d, "Update comment:", selected.getContent());
            if (updated != null && !updated.trim().isEmpty()) {
                Response<Comment> resp = commentController.updateComment(selected.getCommentId(), updated);
                if (resp.isSuccess()) {
                    refreshC.run();
                } else {
                    JOptionPane.showMessageDialog(d, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        actions.add(updateC);

        if (issue.getStatus() == Status.NEW) {
            JButton assign = new JButton("Assign (PL)");
            JButton recommend = new JButton("Recommend");
            assign.addActionListener(e -> {
                String tid = JOptionPane.showInputDialog("Target Dev ID:"); String msg = JOptionPane.showInputDialog("Assignment Message:");
                if (tid != null && msg != null) {
                    commentController.createComment(issueId, msg);
                    Response<Issue> resp = issueController.assignIssue(issueId, Long.parseLong(tid));
                    if (resp.isSuccess()) {
                        d.dispose();
                        refreshTable.run();
                    } else {
                        JOptionPane.showMessageDialog(d, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            if (cur.getRole() == Role.PL) {
                actions.add(assign);
                actions.add(recommend);
            }
        } else if (issue.getStatus() == Status.ASSIGNED && cur.getAccountId().equals(issue.getAssigneeId())) {
            JButton fix = new JButton("Fix (Dev)");
            fix.addActionListener(e -> {
                String msg = JOptionPane.showInputDialog("Fixing Message:");
                if (msg != null) {
                    commentController.createComment(issueId, msg);
                    Response<Issue> resp = issueController.fixIssue(issueId);
                    if (resp.isSuccess()) {
                        d.dispose();
                        refreshTable.run();
                    } else {
                        JOptionPane.showMessageDialog(d, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            actions.add(fix);
        } else if (issue.getStatus() == Status.FIXED && cur.getAccountId().equals(issue.getReporterId())) {
            JButton resolve = new JButton("Resolve");
            resolve.addActionListener(e -> {
                Response<Issue> resp = issueController.resolveIssue(issueId);
                if (resp.isSuccess()) {
                    d.dispose();
                    refreshTable.run();
                } else {
                    JOptionPane.showMessageDialog(d, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            actions.add(resolve);
        } else if (issue.getStatus() == Status.RESOLVED && cur.getRole() == Role.PL) {
            JButton close = new JButton("Close (PL)");
            close.addActionListener(e -> {
                Response<Issue> resp = issueController.closeIssue(issueId);
                if (resp.isSuccess()) {
                    d.dispose();
                    refreshTable.run();
                } else {
                    JOptionPane.showMessageDialog(d, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            actions.add(close);
        }

        d.add(actions, BorderLayout.SOUTH); d.setLocationRelativeTo(this); d.setVisible(true);
    }
}
