package ui.javafx.controllers;

import ui.javafx.BackendFacade;
import ui.javafx.UI;
import ui.javafx.domain.*;
import ui.javafx.views.CreateAccountDialog;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.util.StringConverter;

public class MembersController {
    @FXML private ScrollPane root;
    @FXML private Button addMemberButton;
    @FXML private Button createAccountButton;
    @FXML private FlowPane membersGrid;
    @FXML private Line divider;

    private BackendFacade facade;

    public void init(BackendFacade facade){
        this.facade=facade;
        divider.setEndX(800);
        divider.getStyleClass().add("scribble-divider");
        divider.getStrokeDashArray().setAll(4d, 4d);
        rebuild();
        facade.members.addListener((ListChangeListener<ProjectMember>) c -> rebuild());
    }

    @FXML
    private void handleAddMember(){
        openAddDialog();
    }

    @FXML
    private void handleCreateAccount(){
        new CreateAccountDialog(facade, root.getScene()==null ? null : root.getScene().getWindow(),
            account -> { }).show();
    }

    private void rebuild(){
        addMemberButton.setVisible(true);
        addMemberButton.setManaged(true);
        boolean isAdmin=facade.currentUser.get()!=null && facade.currentUser.get().getRole()==Role.ADMIN;
        createAccountButton.setVisible(isAdmin);
        createAccountButton.setManaged(isAdmin);

        membersGrid.getChildren().clear();
        for (ProjectMember m : facade.membersOf(facade.currentProjectId.get())) {
            membersGrid.getChildren().add(buildCard(m));
        }
        UI.fadeUp(membersGrid, 60);
    }

    private VBox buildCard(ProjectMember m){
        Account a=facade.accountById(m.getAccountId());
        VBox card=UI.cardTinted();
        card.setMinWidth(280);
        card.setMaxWidth(280);

        HBox top=new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);
        VBox who=new VBox(2);
        Label name=new Label(UI.accountRoleName(a));
        name.getStyleClass().addAll("hand", "member-name");
        Label uname=new Label(UI.accountHandle(a));
        uname.getStyleClass().addAll("mono", "small", "dim");
        who.getChildren().addAll(name, uname);
        top.getChildren().addAll(UI.avatar(a, 44), who);
        Region sp=new Region();
        HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
        top.getChildren().add(sp);

        card.getChildren().add(top);
        return card;
    }

    private void openAddDialog(){
        var nonMembers=facade.accounts.stream()
            .filter(a -> facade.membersOf(facade.currentProjectId.get()).stream()
                .noneMatch(m -> m.getAccountId().equals(a.getAccountId()))).toList();
        if (nonMembers.isEmpty()) {
            UI.showErrorSticker(root, "추가할 수 있는 계정이 없습니다. 모든 계정이 이미 이 프로젝트의 멤버입니다.");
            return;
        }

        ComboBox<Account> combo=new ComboBox<>();
        combo.getItems().addAll(nonMembers);
        combo.getSelectionModel().selectFirst();
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Account a){
                return a==null ? "" : UI.accountName(a)+" ("+a.getRole()+")";
            }
            @Override public Account fromString(String s){ return null; }
        });

        Dialog<ButtonType> d=new Dialog<>();
        d.setTitle("Add member");
        d.setHeaderText("Select an account");
        VBox box=new VBox(10, UI.eyebrow("ACCOUNT"), combo);
        box.setPadding(new Insets(10));
        box.setMinWidth(420);
        d.getDialogPane().setContent(box);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        if (root.getScene()!=null) d.getDialogPane().getStylesheets().setAll(root.getScene().getStylesheets());

        d.showAndWait().filter(b -> b==ButtonType.OK).ifPresent(b -> {
            Account chosen=combo.getValue();
            if (chosen==null) {
                UI.showErrorSticker(root, "추가할 계정을 선택하세요.");
                return;
            }
            UI.runWithErrorSticker(root, () ->
                facade.addMember(facade.currentProjectId.get(), chosen.getAccountId(), chosen.getRole()));
        });
    }
}
