package ui.javafx.controllers;

import ui.javafx.BackendFacade;
import ui.javafx.UI;
import ui.javafx.domain.Account;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class LoginController {
    @FXML private StackPane root;
    @FXML private VBox brand;
    @FXML private VBox form;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private BackendFacade facade;
    private Consumer<Account> onLogin;

    public void init(BackendFacade facade, Consumer<Account> onLogin){
        this.facade=facade;
        this.onLogin=onLogin;
        root.setPadding(new Insets(40));
        form.setPadding(new Insets(36));
        UI.fadeUp(brand, 50);
        UI.fadeUp(form, 150);
    }

    @FXML
    private void handleSignIn(){
        String u=usernameField.getText().trim();
        String p=passwordField.getText();
        try {
            Account account=facade.login(u, p);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            onLogin.accept(account);
        } catch (IllegalStateException ex) {
            UI.showErrorSticker(root, ex.getMessage());
        }
    }
}
