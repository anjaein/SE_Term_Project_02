package ui.swing;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.project.controller.ProjectController;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.global.common.Response;

import javax.swing.*;
import java.awt.*;

public class AdminPanel extends JPanel {
    private final AccountController accountController;
    private final ProjectController projectController;

    public AdminPanel(AccountController accountController, ProjectController projectController) {
        super(new GridLayout(2, 1, 10, 10));
        this.accountController = accountController;
        this.projectController = projectController;

        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(createAccountPanel());
        add(createProjectPanel());
    }

    private JPanel createAccountPanel() {
        JPanel accountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accountPanel.setBorder(BorderFactory.createTitledBorder("Add Account"));

        JTextField usernameField = new JTextField(10);
        JTextField passwordField = new JTextField(10);
        JComboBox<Role> rolebox = new JComboBox<>(Role.values());
        JButton createAccountBtn = new JButton("Create Account");

        createAccountBtn.addActionListener(e -> {
            Response<Account> resp = accountController.createAccount(
                    usernameField.getText(),
                    passwordField.getText(),
                    (Role) rolebox.getSelectedItem()
            );
            showResponse(resp);
        });

        accountPanel.add(new JLabel("User:"));
        accountPanel.add(usernameField);
        accountPanel.add(new JLabel("Password:"));
        accountPanel.add(passwordField);
        accountPanel.add(new JLabel("Role:"));
        accountPanel.add(rolebox);
        accountPanel.add(createAccountBtn);
        return accountPanel;
    }

    private JPanel createProjectPanel() {
        JPanel projectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        projectPanel.setBorder(BorderFactory.createTitledBorder("Add Project"));

        JTextField projectNameField = new JTextField(20);
        JButton createProjectBtn = new JButton("Create Project");

        createProjectBtn.addActionListener(e -> {
            Response<Project> resp = projectController.createProject(projectNameField.getText());
            showResponse(resp);
        });

        projectPanel.add(new JLabel("Name:"));
        projectPanel.add(projectNameField);
        projectPanel.add(createProjectBtn);
        return projectPanel;
    }

    private void showResponse(Response<?> resp) {
        if (resp.isSuccess()) {
            JOptionPane.showMessageDialog(this, resp.getMessage());
        } else {
            JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
