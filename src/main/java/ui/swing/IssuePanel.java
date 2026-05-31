package ui.swing;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.project.enums.Role;
import com.issuetracker.domain.comment.controller.CommentController;
import com.issuetracker.domain.issue.controller.IssueController;
import com.issuetracker.domain.issue.controller.IssueStatisticsController;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.project.controller.ProjectController;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.recommend.controller.RecommendController;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.List;

public class IssuePanel extends JPanel {
    private final JFrame owner;
    private final AccountController accountController;
    private final ProjectController projectController;
    private final IssueController issueController;
    private final CommentController commentController;
    private final IssueStatisticsController issueStatisticsController;
    private final RecommendController recommendController;
    private final SessionManager sessionManager;

    public IssuePanel(
            JFrame owner,
            AccountController accountController,
            ProjectController projectController,
            IssueController issueController,
            CommentController commentController,
            IssueStatisticsController issueStatisticsController,
            RecommendController recommendController,
            SessionManager sessionManager
    ) {
        super(new BorderLayout(10, 10));
        this.owner = owner;
        this.accountController = accountController;
        this.projectController = projectController;
        this.issueController = issueController;
        this.commentController = commentController;
        this.issueStatisticsController = issueStatisticsController;
        this.recommendController = recommendController;
        this.sessionManager = sessionManager;

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initPanel();
    }

    private void initPanel() {
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<Status> statusbox = new JComboBox<>(Status.values());
        statusbox.insertItemAt(null, 0);
        statusbox.setSelectedIndex(0);

        JComboBox<Priority> prioritybox = new JComboBox<>(Priority.values());
        prioritybox.insertItemAt(null, 0);
        prioritybox.setSelectedIndex(0);

        JComboBox<Comboitem> projectbox = createProjectComboBoxForCurrentUser(true);
        projectbox.setSelectedIndex(0);

        JButton applyFilter = new JButton("Search Issue");
        JButton mine = new JButton("Assigned to Me");
        JButton reported = new JButton("Reported by Me(Fixed)");
        JButton resolved = new JButton("Resolved Issue");

        filter.add(new JLabel("Status:"));
        filter.add(statusbox);
        filter.add(new JLabel("Priority:"));
        filter.add(prioritybox);
        filter.add(new JLabel("Project:"));
        filter.add(projectbox);
        filter.add(applyFilter);

        final Map<Role, JButton> roleButtonMap = new EnumMap<>(Role.class);
        roleButtonMap.put(Role.DEV, mine);
        roleButtonMap.put(Role.TESTER, reported);
        roleButtonMap.put(Role.PL, resolved);

        Runnable updateProjectRoleButtons = () -> {
            // 1. 모든 버튼 숨기기
            roleButtonMap.values().forEach(filter::remove);
            // 2. 권한에 맞는 버튼만 가져와서 렌더링하기
            Role role = getSelectedProjectRole(projectbox);
            JButton buttonToShow = roleButtonMap.get(role);
            if (buttonToShow != null) {
                filter.add(buttonToShow);
            }
            filter.revalidate();
            filter.repaint();
        };
        projectbox.addActionListener(e -> updateProjectRoleButtons.run());
        updateProjectRoleButtons.run();
        add(filter, BorderLayout.NORTH);

        String[] cols = {"issueid", "projectid", "Title", "Status", "Reporter", "Assignee"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
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
        add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable refreshAll = () -> refreshIssues(model, projectbox);

        applyFilter.addActionListener(e -> applyIssueFilter(model, statusbox, prioritybox, projectbox));
        mine.addActionListener(e -> showAssignedToMe(model, projectbox));
        reported.addActionListener(e -> showReportedFixedByMe(model, projectbox));
        resolved.addActionListener(e -> showResolvedIssues(model, projectbox));

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton statistics = new JButton("Issue Statistics");
        JButton create = new JButton("New Issue");
        JButton detail = new JButton("View Detail & Actions");

        statistics.addActionListener(e -> showStatisticsDialog());
        create.addActionListener(e -> showCreateIssueDialog(projectbox, refreshAll));
        detail.addActionListener(e -> showIssueDetail(table, projectbox, refreshAll));


        leftBtns.add(statistics);
        btns.add(create);
        btns.add(detail);
        bottom.add(leftBtns, BorderLayout.WEST);
        bottom.add(btns, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        projectbox.addActionListener(e -> {
            Role role = getSelectedProjectRole(projectbox);
            // 권한이 TESTER일 때만 'New Issue' 버튼이 보이도록 설정
            create.setVisible(role == Role.TESTER);
        });
        create.setVisible(getSelectedProjectRole(projectbox) == Role.TESTER);
    }

    private void refreshIssues(DefaultTableModel model, JComboBox<Comboitem> projectbox) {
        model.setRowCount(0);
        Long selectedProjectId = getSelectedProjectId(projectbox);
        if(selectedProjectId != null){
            Response<List<Issue>> issueresp = issueController.listIssuesByProject(selectedProjectId);
            if (!issueresp.isSuccess()) {
                JOptionPane.showMessageDialog(this, issueresp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (Issue issue : issueresp.getData()) {
                addIssueRow(model, issue);
            }
        }





    }

    private void applyIssueFilter(
            DefaultTableModel model,
            JComboBox<Status> statusbox,
            JComboBox<Priority> prioritybox,
            JComboBox<Comboitem> projectbox
    ) {

        Long selectedProjectId = getSelectedProjectId(projectbox);
        if (selectedProjectId == null) {
            JOptionPane.showMessageDialog(this, "Please select a project.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Response<List<Issue>> issueresp = issueController.listIssuesByProject(selectedProjectId);
        if (!issueresp.isSuccess()) {
            JOptionPane.showMessageDialog(this, issueresp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        model.setRowCount(0);
        Status status = (Status) statusbox.getSelectedItem();
        Priority priority = (Priority) prioritybox.getSelectedItem();
        for (Issue issue : issueresp.getData()) {
            boolean statusMatch = status == null || issue.getStatus() == status;
            boolean priorityMatch = priority == null || issue.getPriority() == priority;

            if (statusMatch && priorityMatch ) {
                addIssueRow(model, issue);
            }
        }






    }

    private void showAssignedToMe(DefaultTableModel model, JComboBox<Comboitem> projectbox) {
        model.setRowCount(0);
        Response<List<Issue>> resp = issueController.getIssuesByAssigneeId(
                sessionManager.getLoggedInAccount().getAccountId()
        );
        if (!resp.isSuccess()) {
            JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long selectedProjectId = getSelectedProjectId(projectbox);
        for (Issue issue : resp.getData()) {
            if (selectedProjectId != null && !Objects.equals(selectedProjectId, issue.getProjectId())) {
                continue;
            }
            if (issue.getStatus() == Status.ASSIGNED || issue.getStatus() == Status.REOPENED) {
                addIssueRow(model, issue);
            }
        }
    }

    private void showReportedFixedByMe(DefaultTableModel model, JComboBox<Comboitem> projectbox) {
        model.setRowCount(0);
        Response<List<Issue>> resp = issueController.getIssuesByReporterId(
                sessionManager.getLoggedInAccount().getAccountId()
        );
        if (!resp.isSuccess()) {
            JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long selectedProjectId = getSelectedProjectId(projectbox);
        for (Issue issue : resp.getData()) {
            if (selectedProjectId != null && !Objects.equals(selectedProjectId, issue.getProjectId())) {
                continue;
            }
            if (issue.getStatus() == Status.FIXED) {
                addIssueRow(model, issue);
            }
        }
    }

    private void showResolvedIssues(DefaultTableModel model, JComboBox<Comboitem> projectbox) {
        model.setRowCount(0);

        Long selectedProjectId = getSelectedProjectId(projectbox);
        if(selectedProjectId != null){
            Response<List<Issue>> issueresp = issueController.listIssuesByProject(selectedProjectId);
            if (!issueresp.isSuccess()) {
                JOptionPane.showMessageDialog(this, issueresp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (Issue issue : issueresp.getData()) {
                if (issue.getStatus() == Status.RESOLVED) {
                    addIssueRow(model, issue);
                }
            }
        }

    }

    private void showStatisticsDialog() {
        JComboBox<Comboitem> projectBox = createProjectComboBoxForCurrentUser(false);
        if (projectBox.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "No project is available for issue statistics.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JSpinner monthsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 12, 1));
        Object[] input = {
                "Project Name:", projectBox,
                "Months:", monthsSpinner
        };

        int result = JOptionPane.showConfirmDialog(this, input, "Issue Statistics", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        Comboitem selectedProject = (Comboitem) projectBox.getSelectedItem();
        Long targetProjectId = selectedProject != null ? selectedProject.getId() : null;
        if (targetProjectId == null) {
            JOptionPane.showMessageDialog(this, "Please select a project.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int months = (Integer) monthsSpinner.getValue();
        Response<Map<YearMonth, Long>> monthlyReportedResp =
                issueStatisticsController.getMonthlyReportedTrend(targetProjectId, months);
        Response<Map<YearMonth, Long>> monthlyResolvedResp =
                issueStatisticsController.getMonthlyResolvedTrend(targetProjectId, months);
        Response<Map<LocalDate, Long>> dailyReportedResp =
                issueStatisticsController.getDailyReportedTrend(targetProjectId);
        Response<Map<LocalDate, Long>> dailyResolvedResp =
                issueStatisticsController.getDailyResolvedTrend(targetProjectId);
        Response<Map<YearMonth, Map<Priority, Long>>> monthlyPriorityResp =
                issueStatisticsController.getMonthlyPriorityDistribution(targetProjectId, months);
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
        if (!dailyReportedResp.isSuccess()) {
            JOptionPane.showMessageDialog(this, dailyReportedResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!dailyResolvedResp.isSuccess()) {
            JOptionPane.showMessageDialog(this, dailyResolvedResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!monthlyPriorityResp.isSuccess()) {
            JOptionPane.showMessageDialog(this, monthlyPriorityResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        message.append("\n[Daily Reported Issues]\n");
        for (Map.Entry<LocalDate, Long> entry : new TreeMap<>(dailyReportedResp.getData()).entrySet()) {
            message.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        message.append("\n[Daily Resolved Issues]\n");
        for (Map.Entry<LocalDate, Long> entry : new TreeMap<>(dailyResolvedResp.getData()).entrySet()) {
            message.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        message.append("\n[Monthly Priority Distribution]\n");
        for (Map.Entry<YearMonth, Map<Priority, Long>> entry : new TreeMap<>(monthlyPriorityResp.getData()).entrySet()) {
            message.append(entry.getKey()).append(" : ");
            appendPriorityCounts(message, entry.getValue());
            message.append("\n");
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
            appendPriorityCounts(message, entry.getValue());
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
    }

    private void appendPriorityCounts(StringBuilder message, Map<Priority, Long> counts) {
        for (Priority priority : Priority.values()) {
            Long count = counts.getOrDefault(priority, 0L);
            message.append(priority).append("=").append(count).append(" ");
        }
    }

    private void showCreateIssueDialog(JComboBox<Comboitem> projectbox, Runnable refreshAll) {
        Comboitem selectedProject = (Comboitem) projectbox.getSelectedItem();
        if (selectedProject == null || selectedProject.getId() == null) {
            JOptionPane.showMessageDialog(this, "Please select a project.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (getProjectRole(selectedProject.getId()) != Role.TESTER) {
            JOptionPane.showMessageDialog(this, "Only TESTER can create an issue in the selected project.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField title = new JTextField();
        JTextArea description = new JTextArea(5, 20);
        JComboBox<Priority> priorityComboBox = new JComboBox<>(Priority.values());
        priorityComboBox.setSelectedItem(Priority.MAJOR);
        Object[] msg = {
                "Project Name:", new JLabel(selectedProject.toString()),
                "Issue Title:", title,
                "Description:", new JScrollPane(description),
                "Priority:", priorityComboBox
        };

        if (JOptionPane.showConfirmDialog(this, msg, "Create Issue", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        Response<Issue> resp = issueController.createIssue(
                selectedProject.getId(),
                title.getText(),
                description.getText(),
                (Priority) priorityComboBox.getSelectedItem()
        );
        if (resp.isSuccess()) {
            JOptionPane.showMessageDialog(this, resp.getMessage());
            selectProject(projectbox, selectedProject.getId());
            refreshAll.run();
        } else {
            JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showIssueDetail(JTable table, JComboBox<Comboitem> projectbox, Runnable refreshAll) {
        if (getSelectedProjectId(projectbox) == null&& table.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Please select a project.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an issue.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object idObj = table.getValueAt(row, 0);
        Long issueId = Long.parseLong(idObj.toString());
        IssueDetailDialog issueDetailDialog = new IssueDetailDialog(
                owner,
                issueId,
                refreshAll,
                accountController,
                projectController,
                issueController,
                commentController,
                recommendController,
                sessionManager
        );
        if (issueDetailDialog.isInitialized()) {
            issueDetailDialog.setVisible(true);
            refreshAll.run();
        }
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
        Long projectId = getSelectedProjectId(projectbox);
        return getProjectRole(projectId);
    }

    private Long getSelectedProjectId(JComboBox<Comboitem> projectbox) {
        Comboitem selectedProject = (Comboitem) projectbox.getSelectedItem();
        return selectedProject != null ? selectedProject.getId() : null;
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

    private void selectProject(JComboBox<Comboitem> projectbox, Long projectId) {
        for (int i = 0; i < projectbox.getItemCount(); i++) {
            Comboitem item = projectbox.getItemAt(i);
            if (item != null && Objects.equals(item.getId(), projectId)) {
                projectbox.setSelectedIndex(i);
                return;
            }
        }
    }
}
