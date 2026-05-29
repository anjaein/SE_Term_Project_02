package ui.swing;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.project.controller.ProjectController;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProjectPanel extends JPanel {
    private final AccountController accountController;
    private final ProjectController projectController;
    private final SessionManager sessionManager;
    private final DefaultTableModel projectTableModel;
    private final DefaultTableModel memberTableModel;
    private final JTable projectTable;

    public ProjectPanel(
            AccountController accountController,
            ProjectController projectController,
            SessionManager sessionManager
    ) {
        super(new BorderLayout(10, 10));
        this.accountController = accountController;
        this.projectController = projectController;
        this.sessionManager = sessionManager;

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] projectCols = {"Project ID", "Project Name"};
        projectTableModel = new DefaultTableModel(projectCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 프로젝트 테이블 수정 불가
            }
        };
        projectTable = new JTable(projectTableModel);
        hideColumn(projectTable, 0);

        String[] memberCols = {"User name", "Member Role"};
        memberTableModel = new DefaultTableModel(memberCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 멤버 테이블 수정 불가
            }
        };
        JTable memberTable = new JTable(memberTableModel);
        initLayout(memberTable);
    }

    private void initLayout(JTable memberTable) {
        JButton refresh = new JButton("Refresh Project List");
        refresh.addActionListener(e -> refreshProjects());

        projectTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            refreshSelectedProjectMembers();
        });

        JPanel split = new JPanel(new GridLayout(1, 2, 10, 10));
        split.add(new JScrollPane(projectTable));
        split.add(new JScrollPane(memberTable));

        add(refresh, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        if (sessionManager.getLoggedInAccount().getRole() == Role.ADMIN) {
            add(createAddMemberPanel(), BorderLayout.SOUTH);
        }
    }

    private void refreshProjects() {
        projectTableModel.setRowCount(0);
        memberTableModel.setRowCount(0);

        Long myAccountId = sessionManager.getLoggedInAccount().getAccountId();
        Response<List<Project>> projectResp = projectController.getAllProjects();
        if (!projectResp.isSuccess()) {
            JOptionPane.showMessageDialog(this, projectResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (Project project : projectResp.getData()) {
            Response<List<ProjectMember>> memberResp = projectController.listProjectMembers(project.getProjectId());
            if (!memberResp.isSuccess()) {
                continue;
            }

            boolean isMyProject = memberResp.getData().stream()
                    .anyMatch(member -> member.getAccountId().equals(myAccountId));
            if (isMyProject) {
                projectTableModel.addRow(new Object[]{project.getProjectId(), project.getName()});
            }
        }
    }

    private void refreshSelectedProjectMembers() {
        int row = projectTable.getSelectedRow();
        if (row < 0) {
            return;
        }

        memberTableModel.setRowCount(0);
        Long projectId = (Long) projectTable.getValueAt(row, 0);
        Response<List<ProjectMember>> resp = projectController.listProjectMembers(projectId);
        if (!resp.isSuccess()) {
            JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (ProjectMember member : resp.getData()) {
            memberTableModel.addRow(new Object[]{
                    getUsernameOrUnknown(member.getAccountId()),
                    member.getRole()
            });
        }
    }

    private JPanel createAddMemberPanel() {
        JPanel addMember = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addMember.setBorder(BorderFactory.createTitledBorder("Add Member to Selected Project"));

        JTextField usernameField = new JTextField(10);
        JComboBox<Role> rolebox = new JComboBox<>(Role.values());
        JButton addMemBtn = new JButton("Add Member");

        addMemBtn.addActionListener(e -> {
            int row = projectTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a project first.");
                return;
            }

            Response<ProjectMember> resp = projectController.addProjectMember(
                    (Long) projectTable.getValueAt(row, 0),
                    usernameField.getText(),
                    (Role) rolebox.getSelectedItem()
            );

            if (resp.isSuccess()) {
                JOptionPane.showMessageDialog(this, resp.getMessage());
                refreshSelectedProjectMembers();
            } else {
                JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        addMember.add(new JLabel("User:"));
        addMember.add(usernameField);
        addMember.add(new JLabel("Role:"));
        addMember.add(rolebox);
        addMember.add(addMemBtn);
        return addMember;
    }

    private String getUsernameOrUnknown(Long accountId) {
        Response<Account> accountResp = accountController.getAccountById(accountId);
        if (accountResp.isSuccess() && accountResp.getData() != null) {
            return accountResp.getData().getUsername();
        }
        return "Unknown";
    }

    private void hideColumn(JTable table, int columnIndex) {
        table.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        table.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        table.getColumnModel().getColumn(columnIndex).setWidth(0);
    }
}
