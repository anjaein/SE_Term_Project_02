package ui.javafx.controllers;

import ui.javafx.App;
import ui.javafx.BackendFacade;
import ui.javafx.UI;
import ui.javafx.domain.Account;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.util.EnumMap;
import java.util.Map;

public class AppController {
    private static final double USER_MENU_AVATAR_SIZE=28;

    @FXML private StackPane contentHost;
    @FXML private Label projectLabel;
    @FXML private Button projectsButton;
    @FXML private Button boardButton;
    @FXML private Button statsButton;
    @FXML private Button membersButton;
    @FXML private Button newIssueButton;
    @FXML private Menu switchUserMenu;
    @FXML private Circle avatarCircle;
    @FXML private Label avatarInitial;
    @FXML private Label usernameLabel;
    @FXML private Label roleChipLabel;

    private final Map<App.View, Button> navButtons=new EnumMap<>(App.View.class);
    private BackendFacade facade;
    private App app;

    public void init(BackendFacade facade, App app){
        this.facade=facade;
        this.app=app;

        avatarInitial.getStyleClass().add("avatar-initial");

        navButtons.put(App.View.PROJECTS, projectsButton);
        navButtons.put(App.View.BOARD, boardButton);
        navButtons.put(App.View.STATS, statsButton);
        navButtons.put(App.View.MEMBERS, membersButton);

        projectLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            var p=facade.projectById(facade.currentProjectId.get());
            return p!=null ? p.getName() : "";
        }, facade.currentProjectId));

        switchUserMenu.setOnShowing(e -> refreshSwitchUserItems());
        refreshUserMenuLabel();
        refreshSwitchUserItems();
        applyPermissions();
    }

    public void setCenter(Node node){
        contentHost.getChildren().setAll(node);
    }

    public void updateNavHighlight(App.View currentView){
        navButtons.forEach((view, btn) -> {
            boolean active=view==currentView
                || (currentView==App.View.ISSUE && view==App.View.BOARD);
            btn.getStyleClass().remove("nav-btn-active");
            if (active) btn.getStyleClass().add("nav-btn-active");
        });
    }

    @FXML private void handleProjects(){ app.switchView(App.View.PROJECTS, null); }
    @FXML private void handleBoard(){ app.switchView(App.View.BOARD, null); }
    @FXML private void handleStats(){ app.switchView(App.View.STATS, null); }
    @FXML private void handleMembers(){ app.switchView(App.View.MEMBERS, null); }
    @FXML private void handleCreateIssue(){ app.openCreateIssueDialog(); }
    @FXML private void handleSignOut(){
        UI.runWithErrorSticker(contentHost, () -> facade.logout());
    }

    private void refreshUserMenuLabel(){
        Account me=facade.currentUser.get();
        usernameLabel.setText(UI.accountName(me));
        roleChipLabel.setText(me==null ? "" : me.getRole().name());
        avatarInitial.setText(UI.accountInitial(me));
        avatarInitial.setStyle("-fx-font-size: "+UI.avatarInitialFontSize(USER_MENU_AVATAR_SIZE)+"px;");
        if (!avatarCircle.getStyleClass().contains("avatar-circle")) {
            avatarCircle.getStyleClass().add("avatar-circle");
        }
        UI.applyAccountTone(avatarCircle, me);
        UI.applyAccountTone(avatarInitial, me);
    }

    private void refreshSwitchUserItems(){
        switchUserMenu.getItems().clear();
        for (var mem : facade.membersOf(facade.currentProjectId.get())) {
            Account a=facade.accountById(mem.getAccountId());
            if (a==null) continue;
            MenuItem item=new MenuItem(UI.accountName(a)+" ("+a.getRole()+")");
            item.getStyleClass().add("user-menu-item");
            item.setOnAction(e -> UI.runWithErrorSticker(contentHost, () ->
                facade.login(a.getUsername(), a.getPassword())));
            switchUserMenu.getItems().add(item);
        }
        if (switchUserMenu.getItems().isEmpty()) {
            MenuItem empty=new MenuItem("팀원이 없습니다");
            empty.setDisable(true);
            switchUserMenu.getItems().add(empty);
        }
    }

    private void applyPermissions(){
        newIssueButton.setVisible(true);
        newIssueButton.setManaged(true);
    }
}
