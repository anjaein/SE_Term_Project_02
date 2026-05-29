package ui.swing;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.global.common.Response;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private final AccountController accountController;
    private final Runnable onLoginSuccess;

    public LoginPanel(AccountController accountController, Runnable onLoginSuccess) {
        super(new GridBagLayout());
        this.accountController = accountController;
        this.onLoginSuccess = onLoginSuccess;

        initPanel();
    }

    private void initPanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JTextField userField = new JTextField(20);
        JPasswordField passField = new JPasswordField(20);
        JButton loginBtn = new JButton("Login");

        gbc.gridx = 0;gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);gbc.gridx = 1;
        add(userField, gbc);
        gbc.gridx = 0;gbc.gridy = 1;add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;add(passField, gbc);
        gbc.gridx = 1;gbc.gridy = 2;add(loginBtn, gbc);
        loginBtn.addActionListener(e -> login(userField, passField));
    }

    private void login(JTextField userField, JPasswordField passField) {
        String username = userField.getText();
        String password = new String(passField.getPassword());
        Response<Account> response = accountController.login(username, password);

        if (response.isSuccess()) {
            onLoginSuccess.run();
        } else {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
