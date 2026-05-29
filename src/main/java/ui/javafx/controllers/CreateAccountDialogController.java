package ui.javafx.controllers;

import ui.javafx.BackendFacade;
import ui.javafx.UI;
import ui.javafx.domain.Account;
import ui.javafx.domain.Role;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.function.Consumer;

public class CreateAccountDialogController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleCombo;

    private BackendFacade facade;
    private Consumer<Account> onCreate;

    public void init(BackendFacade facade, Consumer<Account> onCreate){
        this.facade=facade;
        this.onCreate=onCreate;
        roleCombo.setItems(FXCollections.observableArrayList(Role.values()));
        roleCombo.getSelectionModel().select(Role.DEV);
        roleCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Role r){ return r==null ? "" : r.label()+" ("+r.name()+")"; }
            @Override public Role fromString(String s){ return null; }
        });
    }

    public boolean createAccount(){
        String u=usernameField.getText()==null ? "" : usernameField.getText().trim();
        String p=passwordField.getText()==null ? "" : passwordField.getText();
        Role role=roleCombo.getValue();
        if (u.isEmpty()) { UI.showErrorSticker(usernameField, "아이디를 입력하세요."); return false; }
        if (p.isEmpty()) { UI.showErrorSticker(passwordField, "비밀번호를 입력하세요."); return false; }
        if (role==null) { UI.showErrorSticker(roleCombo, "역할을 선택하세요."); return false; }
        try {
            Account created=facade.createAccount(u, p, role);
            if (onCreate!=null) onCreate.accept(created);
            return true;
        } catch (IllegalStateException ex) {
            UI.showErrorSticker(usernameField, ex.getMessage());
            return false;
        }
    }
}
