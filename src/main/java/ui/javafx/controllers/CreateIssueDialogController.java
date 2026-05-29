package ui.javafx.controllers;

import ui.javafx.BackendFacade;
import ui.javafx.UI;
import ui.javafx.domain.Issue;
import ui.javafx.domain.Priority;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.function.Consumer;

public class CreateIssueDialogController {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Priority> priorityCombo;

    private BackendFacade facade;
    private Consumer<Issue> onCreate;

    public void init(BackendFacade facade, Consumer<Issue> onCreate){
        this.facade=facade;
        this.onCreate=onCreate;
        priorityCombo.setItems(FXCollections.observableArrayList(Priority.values()));
        priorityCombo.getSelectionModel().select(Priority.MAJOR);
        priorityCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Priority p){
                return p==null ? "" : p.name();
            }

            @Override public Priority fromString(String s){
                return null;
            }
        });
    }

    public boolean createIssue(){
        String t=titleField.getText().trim();
        if (t.isEmpty()) return false;
        try {
            Issue issue=facade.createIssue(
                facade.currentProjectId.get(),
                t,
                descriptionArea.getText().trim().isEmpty() ? "설명 없음" : descriptionArea.getText().trim(),
                priorityCombo.getValue());
            if (onCreate!=null) onCreate.accept(issue);
            return true;
        } catch (IllegalStateException ex) {
            UI.showErrorSticker(titleField, ex.getMessage());
            return false;
        }
    }
}
