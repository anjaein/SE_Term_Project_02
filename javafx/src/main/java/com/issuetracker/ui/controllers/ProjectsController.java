package com.issuetracker.ui.controllers;

import com.issuetracker.ui.App;
import com.issuetracker.ui.BackendFacade;
import com.issuetracker.ui.UI;
import com.issuetracker.ui.domain.Project;
import com.issuetracker.ui.domain.Status;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;

public class ProjectsController {
    @FXML private ScrollPane root;
    @FXML private HBox header;
    @FXML private Button newProjectButton;
    @FXML private FlowPane projectsGrid;
    @FXML private Line divider;

    private BackendFacade facade;
    private App app;

    public void init(BackendFacade facade, App app){
        this.facade=facade;
        this.app=app;

        divider.setEndX(800);
        divider.getStyleClass().add("scribble-divider");
        divider.getStrokeDashArray().setAll(4d, 4d);

        newProjectButton.setVisible(true);
        newProjectButton.setManaged(true);

        rebuildProjects();
        UI.fadeUp(header, 0);
        UI.fadeUp(projectsGrid, 80);
    }

    @FXML
    private void handleCreateProject(){
        openCreateDialog();
    }

    private void rebuildProjects(){
        projectsGrid.getChildren().clear();
        for (Project p : facade.projects) {
            projectsGrid.getChildren().add(buildCard(p));
        }
    }

    private VBox buildCard(Project p){
        VBox card=UI.cardTinted();
        card.setMinWidth(310);
        card.setPrefWidth(310);
        card.setMaxWidth(310);

        long issueCount=facade.issuesOf(p.getProjectId()).size();
        long openCount=facade.issuesOf(p.getProjectId()).stream()
            .filter(i -> i.getStatus()!=Status.CLOSED && i.getStatus()!=Status.RESOLVED).count();
        long memberCount=facade.membersOf(p.getProjectId()).size();

        HBox head=new HBox();
        VBox idTitle=new VBox(4);
        Label id=new Label("P-"+String.format("%03d", p.getProjectId()));
        id.getStyleClass().addAll("mono", "small", "dim");
        Label name=new Label(p.getName());
        name.getStyleClass().add("h3");
        idTitle.getChildren().addAll(id, name);
        Region sp=new Region();
        HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
        Label arrow=new Label("→");
        arrow.getStyleClass().add("hand");
        head.getChildren().addAll(idTitle, sp, arrow);
        head.setAlignment(Pos.TOP_LEFT);

        HBox stats=new HBox(12);
        stats.setAlignment(Pos.CENTER_LEFT);
        Label openCnt=new Label(String.valueOf(openCount));
        openCnt.getStyleClass().add("h3");
        Label openLbl=new Label(" open");
        openLbl.getStyleClass().add("dim");
        Label slash=new Label("/");
        slash.getStyleClass().add("dim");
        Label total=new Label(issueCount+" total");
        total.getStyleClass().add("dim");
        Region s2=new Region();
        HBox.setHgrow(s2, javafx.scene.layout.Priority.ALWAYS);
        Label members=new Label(memberCount+" members");
        members.getStyleClass().addAll("dim", "small");
        stats.getChildren().addAll(openCnt, openLbl, slash, total, s2, members);

        card.getChildren().addAll(head, stats);
        card.setOnMouseClicked(e -> {
            facade.currentProjectId.set(p.getProjectId());
            app.switchView(App.View.BOARD, null);
        });
        UI.hoverLift(card);
        card.setCursor(Cursor.HAND);
        return card;
    }

    private void openCreateDialog(){
        Dialog<ButtonType> d=new Dialog<>();
        d.setTitle("New project");
        d.setHeaderText(null);
        TextField name=new TextField();
        name.setPromptText("e.g. Payments Service");
        VBox box=new VBox(10, UI.eyebrow("NAME"), name);
        box.setPadding(new Insets(10));
        box.setMinWidth(420);
        d.getDialogPane().setContent(box);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        d.getDialogPane().getStylesheets().setAll(root.getScene().getStylesheets());

        d.showAndWait().filter(b -> b==ButtonType.OK).ifPresent(b -> {
            if (name.getText().trim().isEmpty()) return;
            UI.runWithErrorSticker(root, () -> {
                Project p=facade.createProject(name.getText().trim());
                facade.currentProjectId.set(p.getProjectId());
                app.switchView(App.View.BOARD, null);
            });
        });
    }
}
