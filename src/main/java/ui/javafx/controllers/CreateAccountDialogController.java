package ui.javafx.controllers;

import ui.javafx.BackendFacade;
import ui.javafx.UI;
import ui.javafx.domain.Account;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

public class CreateAccountDialogController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private BackendFacade facade;
    private Consumer<Account> onCreate;

    public void init(BackendFacade facade, Consumer<Account> onCreate){
        this.facade=facade;
        this.onCreate=onCreate;
    }

    public boolean createAccount(){
        String u=usernameField.getText()==null ? "" : usernameField.getText().trim();
        String p=passwordField.getText()==null ? "" : passwordField.getText();
        if (u.isEmpty()) { UI.showErrorSticker(usernameField, "아이디를 입력하세요."); return false; }
        if (p.isEmpty()) { UI.showErrorSticker(passwordField, "비밀번호를 입력하세요."); return false; }
        try {
            Account created=facade.createAccount(u, p);
            if (onCreate!=null) onCreate.accept(created);
            return true;
        } catch (IllegalStateException ex) {
            UI.showErrorSticker(usernameField, ex.getMessage());
            return false;
        }
    }
}
