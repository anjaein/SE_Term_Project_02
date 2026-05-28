package ui.swing;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.comment.controller.CommentController;
import com.issuetracker.domain.issue.controller.IssueController;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.project.controller.ProjectController;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;
import com.issuetracker.domain.recommend.controller.RecommendController;
import com.issuetracker.domain.issue.controller.IssueStatisticsController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

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
        if (curuser.getRole() == Role.ADMIN) tabs.addTab("Admin Console", createAdminPanel());
        tabs.addTab("Projects & Members", createProjectPanel());
        tabs.addTab("Issue Management", createIssuePanel());

        dashboard.add(tabs, BorderLayout.CENTER);
        mainContainer.add(dashboard, "DASHBOARD");
        cardLayout.show(mainContainer, "DASHBOARD");
    }

    private JPanel createAdminPanel() {
        JPanel adminPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        adminPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel accountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accountPanel.setBorder(BorderFactory.createTitledBorder("Add Account"));
        JTextField usernameField = new JTextField(10); JTextField passwordField = new JTextField(10);
        JComboBox<Role> rolebox = new JComboBox<>(Role.values());
        JButton createAccountbtn = new JButton("Create Account");
        createAccountbtn.addActionListener(e -> {
            Response<Account> resp = accountController.createAccount(usernameField.getText(), passwordField.getText(), (Role)rolebox.getSelectedItem());
            if (resp.isSuccess()) {
                JOptionPane.showMessageDialog(this, resp.getMessage());
            } else {
                JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        accountPanel.add(new JLabel("User:")); accountPanel.add(usernameField); accountPanel.add(new JLabel("Password:")); accountPanel.add(passwordField);
        accountPanel.add(new JLabel("Role:")); accountPanel.add(rolebox); accountPanel.add(createAccountbtn);

        JPanel projectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        projectPanel.setBorder(BorderFactory.createTitledBorder("Add Project"));
        JTextField projectNameField = new JTextField(20);
        JButton createProjectbtn = new JButton("Create Project");
        createProjectbtn.addActionListener(e -> {
            Response<Project> resp = projectController.createProject(projectNameField.getText());

            if (resp.isSuccess()) {
                JOptionPane.showMessageDialog(this, resp.getMessage());
            } else {
                JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        projectPanel.add(new JLabel("Name:")); projectPanel.add(projectNameField); projectPanel.add(createProjectbtn);

        adminPanel.add(accountPanel); adminPanel.add(projectPanel);
        return adminPanel;
    }

    private JPanel createProjectPanel() {
        JPanel ProjectPanel = new JPanel(new BorderLayout(10, 10));
        ProjectPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] projectCols = {"Project ID", "Project Name"};
        DefaultTableModel projectTableModel = new DefaultTableModel(projectCols, 0);
        JTable projectTable = new JTable(projectTableModel);

        projectTable.getColumnModel().getColumn(0).setMinWidth(0);
        projectTable.getColumnModel().getColumn(0).setMaxWidth(0);
        projectTable.getColumnModel().getColumn(0).setWidth(0);

        String[] memberCols = {"User name", "Member Role"};
        DefaultTableModel memberTableModel = new DefaultTableModel(memberCols, 0);
        JTable mTable = new JTable(memberTableModel);

        JButton refresh = new JButton("Refresh Project List");
        refresh.addActionListener(e -> {
            projectTableModel.setRowCount(0);
            memberTableModel.setRowCount(0);
            Long myAccountId = sessionManager.getLoggedInAccount().getAccountId();
            Response<List<Project>> projectResp = projectController.getAllProjects();
            if (projectResp.isSuccess()) {
                for (Project pr : projectResp.getData()) {
                    Long pid = pr.getProjectId();
                    Response<List<ProjectMember>> memberResp = projectController.listProjectMembers(pid);
                    if (memberResp.isSuccess()) {
                        boolean isMyProject = false;
                        for (ProjectMember m : memberResp.getData()) {
                            if (m.getAccountId().equals(myAccountId)) {
                                isMyProject = true; 
                                break;
                            }
                        }
                        if (isMyProject) {
                            projectTableModel.addRow(new Object[]{pr.getProjectId(), pr.getName()});
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, projectResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        projectTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = projectTable.getSelectedRow();
            if (row >= 0) {
                memberTableModel.setRowCount(0);
                Long pid = (Long) projectTable.getValueAt(row, 0);
                Response<List<ProjectMember>> resp = projectController.listProjectMembers(pid);
                if (resp.isSuccess()) {
                    for (ProjectMember m : resp.getData()) {
                        Response<Account> accResp = accountController.getAccountById(m.getAccountId());
                        String username = "Unknown"; 
                        if (accResp.isSuccess() && accResp.getData() != null) {
                            username = accResp.getData().getUsername(); 
                        }
                        memberTableModel.addRow(new Object[]{username, m.getRole()});
                    }
                } else {
                    JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel split = new JPanel(new GridLayout(1, 2, 10, 10));
        split.add(new JScrollPane(projectTable)); split.add(new JScrollPane(mTable));
        ProjectPanel.add(refresh, BorderLayout.NORTH); ProjectPanel.add(split, BorderLayout.CENTER);
        
        if (sessionManager.getLoggedInAccount().getRole() == Role.ADMIN) {
            JPanel addMember = new JPanel(new FlowLayout(FlowLayout.LEFT));
            addMember.setBorder(BorderFactory.createTitledBorder("Add Member to Selected Project"));
            JTextField usernameField = new JTextField(10); JComboBox<Role> rolebox = new JComboBox<>(Role.values());
            JButton addMemBtn = new JButton("Add Member");
            addMemBtn.addActionListener(e -> {
                int row = projectTable.getSelectedRow();
                if (row >= 0) {
                    Response<ProjectMember> resp = projectController.addProjectMember((Long)projectTable.getValueAt(row, 0), usernameField.getText(), (Role)rolebox.getSelectedItem());
                    if (resp.isSuccess()) {
                        JOptionPane.showMessageDialog(this, resp.getMessage());
                    } else {
                        JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else JOptionPane.showMessageDialog(this, "Please select a project first.");
            });
            addMember.add(new JLabel("User:")); addMember.add(usernameField); addMember.add(new JLabel("Role:")); addMember.add(rolebox); addMember.add(addMemBtn);
            ProjectPanel.add(addMember, BorderLayout.SOUTH);
        }
        return ProjectPanel;
    }

    private JPanel createIssuePanel() {
        JPanel createIssuePanel = new JPanel(new BorderLayout(10, 10));
        createIssuePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<Status> statusbox = new JComboBox<>(Status.values());
        statusbox.insertItemAt(null, 0); statusbox.setSelectedIndex(0);
        JComboBox<Priority> prioritybox = new JComboBox<>(Priority.values());
        prioritybox.insertItemAt(null, 0); prioritybox.setSelectedIndex(0);

        JComboBox<Comboitem> projectbox = createProjectComboBoxForCurrentUser(true);
        projectbox.setSelectedIndex(0);
        JButton applyFilter = new JButton("Search Issue");
        JButton mine = new JButton("Assigned to Me");
        JButton reported = new JButton("Reported by Me");
        JButton resolved = new JButton("Resolved Issue");

        filter.add(new JLabel("Status:")); filter.add(statusbox);
        filter.add(new JLabel("Priority:")); filter.add(prioritybox);
        filter.add(new JLabel("Project:")); filter.add(projectbox);
        filter.add(applyFilter);

        Runnable updateProjectRoleButtons = () -> {
            filter.remove(mine);
            filter.remove(reported);
            filter.remove(resolved);

            Role selectedProjectRole = getSelectedProjectRole(projectbox);
            if (selectedProjectRole == Role.DEV) {
                filter.add(mine);
            }
            if (selectedProjectRole == Role.TESTER) {
                filter.add(reported);
            }
            if (selectedProjectRole == Role.PL) {
                filter.add(resolved);
            }

            filter.revalidate();
            filter.repaint();
        };
        projectbox.addActionListener(e -> updateProjectRoleButtons.run());
        updateProjectRoleButtons.run();
        createIssuePanel.add(filter, BorderLayout.NORTH);

        String[] cols = {"issueid", "projectid","Title", "Status", "Reporter", "Assignee"};
        DefaultTableModel model = new DefaultTableModel(cols, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        JTable table = new JTable(model);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setWidth(0);
        createIssuePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable refreshAll = () -> {
            model.setRowCount(0);
            Response<List<Issue>> issueresp = issueController.getAllIssues();
            if (issueresp.isSuccess()) {
                Comboitem selectedProjectItem = (Comboitem) projectbox.getSelectedItem();
                Long selectedProjectId = (selectedProjectItem != null) ? selectedProjectItem.getId() : null;
                for (Issue i : issueresp.getData()) {
                    if (selectedProjectId != null && !selectedProjectId.equals(i.getProjectId())) {
                        continue;
                    }
                    addIssueRow(model, i);
                }
            } else {
                JOptionPane.showMessageDialog(this, issueresp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };
        applyFilter.addActionListener(e -> {
            model.setRowCount(0);
            Status status = (Status) statusbox.getSelectedItem();
            Priority priority = (Priority) prioritybox.getSelectedItem();
            Response<List<Issue>> issueresp = issueController.getAllIssues();
            Comboitem selectedProject = (Comboitem) projectbox.getSelectedItem();
            if (issueresp.isSuccess()) {
                for (Issue i : issueresp.getData()) {
                    boolean statusMatch = (status == null || i.getStatus() == status);
                    boolean priorityMatch = (priority == null || i.getPriority() == priority);
                    if (statusMatch && priorityMatch) {
                        if (selectedProject != null && Objects.equals(selectedProject.getId(),i.getProjectId())) {
                            addIssueRow(model, i);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, issueresp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        mine.addActionListener(e -> {
            model.setRowCount(0);
            Response<List<Issue>> resp = issueController.getIssuesByAssigneeId(sessionManager.getLoggedInAccount().getAccountId());
            if (resp.isSuccess()) {
                Comboitem selectedProjectItem = (Comboitem) projectbox.getSelectedItem();
                Long selectedProjectId = (selectedProjectItem != null) ? selectedProjectItem.getId() : null;
                for (Issue i : resp.getData()) {
                    
                    if (selectedProjectId != null && !Objects.equals(selectedProjectId,i.getProjectId())) {
                        continue;
                    }
                    if (i.getStatus() == Status.ASSIGNED) addIssueRow(model, i);
                }
            } else {
                JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        reported.addActionListener(e -> {
            model.setRowCount(0);
            Response<List<Issue>> resp = issueController.getIssuesByReporterId(sessionManager.getLoggedInAccount().getAccountId());
            if (resp.isSuccess()) {
                Comboitem selectedProjectItem = (Comboitem) projectbox.getSelectedItem();
                Long selectedProjectId = (selectedProjectItem != null) ? selectedProjectItem.getId() : null;
                for (Issue i : resp.getData()) {
                    
                    if (selectedProjectId != null && !Objects.equals(selectedProjectId,i.getProjectId())) {
                        continue;
                    }
                    if (i.getStatus() == Status.FIXED) addIssueRow(model, i);
                }
            } else {
                JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        resolved.addActionListener(e -> {
            model.setRowCount(0);
            Response<List<Issue>> resp = issueController.getAllIssues();
            if (resp.isSuccess()) {
                Comboitem selectedProjectItem = (Comboitem) projectbox.getSelectedItem();
                Long selectedProjectId = (selectedProjectItem != null) ? selectedProjectItem.getId() : null;
                for (Issue i : resp.getData()) {
                    
                    if (selectedProjectId != null && !Objects.equals(selectedProjectId,i.getProjectId())) {
                        continue;
                    }
                    if (i.getStatus() == Status.RESOLVED) addIssueRow(model, i);
                }
            } else {
                JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel bottom = new JPanel(new BorderLayout());
        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton statistics = new JButton("Issue Statistics");
        statistics.addActionListener(e -> {
            JComboBox<Comboitem> createProjectBox = createProjectComboBoxForCurrentUser(false);
            if (createProjectBox.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No project is available for issue statistics.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JSpinner monthsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 12, 1));
            Object[] input = {
                    "Project Name:", createProjectBox, 
                    "Months:", monthsSpinner
            };
            int result = JOptionPane.showConfirmDialog(
                    this, input, "Issue Statistics", JOptionPane.OK_CANCEL_OPTION
            );
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            Comboitem selectedProject = (Comboitem) createProjectBox.getSelectedItem();
            int months = (Integer) monthsSpinner.getValue();


            Long targetProjectId = (selectedProject != null) ? selectedProject.getId() : null;
            if (targetProjectId == null) {
                JOptionPane.showMessageDialog(this, "Please select a project.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Response<Map<YearMonth, Long>> monthlyReportedResp =
                    issueStatisticsController.getMonthlyReportedTrend(targetProjectId, months);
            Response<Map<YearMonth, Long>> monthlyResolvedResp =
                    issueStatisticsController.getMonthlyResolvedTrend(targetProjectId, months);
            Response<Map<YearMonth, Double>> averageClosedResp =
                    issueStatisticsController.getMonthlyAverageClosedDays(targetProjectId, months);
            Response<Map<LocalDate, Map<Priority, Long>>> dailyPriorityResp =
                    issueStatisticsController.getDailyPriorityDistribution(targetProjectId);
            if (!monthlyReportedResp.isSuccess()) {
                JOptionPane.showMessageDialog(this, monthlyReportedResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!monthlyResolvedResp.isSuccess()) {
                JOptionPane.showMessageDialog(this, monthlyResolvedResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!averageClosedResp.isSuccess()) {
                JOptionPane.showMessageDialog(this, averageClosedResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!dailyPriorityResp.isSuccess()) {
                JOptionPane.showMessageDialog(this, dailyPriorityResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            StringBuilder message = new StringBuilder();
            message.append("[Monthly Reported Issues]\n");
            for (Map.Entry<YearMonth, Long> entry : new TreeMap<>(monthlyReportedResp.getData()).entrySet()) {
                message.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
            }
            message.append("\n[Monthly Resolved Issues]\n");
            for (Map.Entry<YearMonth, Long> entry : new TreeMap<>(monthlyResolvedResp.getData()).entrySet()) {
                message.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
            }
            message.append("\n[Monthly Average Closed Days]\n");
            for (Map.Entry<YearMonth, Double> entry : new TreeMap<>(averageClosedResp.getData()).entrySet()) {
                message.append(entry.getKey())
                        .append(" : ")
                        .append(String.format("%.2f days", entry.getValue()))
                        .append("\n");
            }
            message.append("\n[Daily Priority Distribution]\n");
            for (Map.Entry<LocalDate, Map<Priority, Long>> entry : new TreeMap<>(dailyPriorityResp.getData()).entrySet()) {
                message.append(entry.getKey()).append(" : ");
                for (Priority priority : Priority.values()) {
                    Long count = entry.getValue().getOrDefault(priority, 0L);
                    message.append(priority).append("=").append(count).append(" ");
                }
                message.append("\n");
            }

            JTextArea textArea = new JTextArea(message.toString(), 24, 50);
            textArea.setEditable(false);
            textArea.setCaretPosition(0);
            JOptionPane.showMessageDialog(
                    this,
                    new JScrollPane(textArea),
                    "Issue Statistics",
                    JOptionPane.INFORMATION_MESSAGE
            );


        });
        JButton create = new JButton("New Issue");
        JButton detail = new JButton("View Detail & Actions");
        create.addActionListener(e -> {
            JComboBox<Comboitem> createProjectBox = createProjectComboBoxForCurrentUser(false);
            if (createProjectBox.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No project is available for issue creation.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JTextField title = new JTextField();
            JTextArea description = new JTextArea(5, 20);
            JComboBox<Priority> priorityComboBox = new JComboBox<>(Priority.values());
            
            priorityComboBox.setSelectedItem(Priority.MAJOR);
            Object[] msg = {"Project Name:", createProjectBox, "Issue Title:", title, "Description:", new JScrollPane(description), "Priority:", priorityComboBox};
            if (JOptionPane.showConfirmDialog(this, msg, "Create Issue", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                Comboitem selectedProject = (Comboitem) createProjectBox.getSelectedItem();
                if (selectedProject == null) {
                    JOptionPane.showMessageDialog(this, "Please select a project.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (getProjectRole(selectedProject.getId()) != Role.TESTER) {
                    JOptionPane.showMessageDialog(this, "Only TESTER can create an issue in the selected project.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Response<Issue> resp = issueController.createIssue(selectedProject.getId(), title.getText(), description.getText(), (Priority) priorityComboBox.getSelectedItem());
                if (resp.isSuccess()) {
                    JOptionPane.showMessageDialog(this, resp.getMessage());
                    for (int i = 0; i < projectbox.getItemCount(); i++) {
                        Comboitem item = projectbox.getItemAt(i);
                        if (item != null && Objects.equals(item.getId(), selectedProject.getId())) {
                            projectbox.setSelectedIndex(i);
                            break;
                        }
                    }
                    refreshAll.run();
                } else {
                    JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        detail.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                Object idObj = table.getValueAt(r, 0);
                Long issueId = Long.parseLong(idObj.toString());
                showIssueDetail(issueId, refreshAll);
                refreshAll.run();
            }
        });
        btns.add(create);
        leftBtns.add(statistics);
        btns.add(detail);
        bottom.add(leftBtns, BorderLayout.WEST);
        bottom.add(btns, BorderLayout.EAST);
        createIssuePanel.add(bottom, BorderLayout.SOUTH);
        return createIssuePanel;
    }

    private JComboBox<Comboitem> createProjectComboBoxForCurrentUser(boolean includeAll) {
        JComboBox<Comboitem> box = new JComboBox<>();

        if (includeAll) {
            box.addItem(new Comboitem(null, "Select Project"));
        }

        Response<List<Project>> projectResp = projectController.getAllProjects();
        if (!projectResp.isSuccess()) {
            JOptionPane.showMessageDialog(this, projectResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return box;
        }

        Long currentAccountId = sessionManager.getLoggedInAccount().getAccountId();

        for (Project project : projectResp.getData()) {
            Response<List<ProjectMember>> memberResp =
                    projectController.listProjectMembers(project.getProjectId());

            if (memberResp.isSuccess()
                    && memberResp.getData().stream()
                    .anyMatch(pm -> pm.getAccountId().equals(currentAccountId))) {
                box.addItem(new Comboitem(project.getProjectId(), project.getName()));
            }
        }

        return box;
    }

    private void addIssueRow(DefaultTableModel model, Issue issue) {
        String reporterName = getUsernameOrUnknown(issue.getReporterId());
        String assigneeName = getUsernameOrUnknown(issue.getAssigneeId());

        model.addRow(new Object[]{
                issue.getIssueId(),
                issue.getProjectId(),
                issue.getTitle(),
                issue.getStatus(),
                reporterName,
                assigneeName
        });
    }

    private String getUsernameOrUnknown(Long accountId) {
        Response<Account> accountResp = accountController.getAccountById(accountId);
        if (accountResp.isSuccess() && accountResp.getData() != null) {
            return accountResp.getData().getUsername();
        }
        return "";
    }

    private Role getSelectedProjectRole(JComboBox<Comboitem> projectbox) {
        Comboitem selectedProject = (Comboitem) projectbox.getSelectedItem();
        Long projectId = (selectedProject != null) ? selectedProject.getId() : null;
        return getProjectRole(projectId);
    }

    private Role getProjectRole(Long projectId) {
        if (projectId == null || sessionManager.getLoggedInAccount() == null) {
            return null;
        }

        Long currentAccountId = sessionManager.getLoggedInAccount().getAccountId();
        Response<List<ProjectMember>> memberResp = projectController.listProjectMembers(projectId);

        if (!memberResp.isSuccess() || memberResp.getData() == null) {
            return null;
        }

        return memberResp.getData().stream()
                .filter(member -> member.getAccountId().equals(currentAccountId))
                .map(ProjectMember::getRole)
                .findFirst()
                .orElse(null);
    }


    private void showIssueDetail(Long issueId, Runnable refreshTable) {
        IssueDetailDialog issueDetailDialog = new IssueDetailDialog(
                this,
                issueId,
                refreshTable,
                accountController,
                projectController,
                issueController,
                commentController,
                recommendController,
                sessionManager
        );
        if (issueDetailDialog.isInitialized()) {
            issueDetailDialog.setVisible(true);
        }
    }
}
