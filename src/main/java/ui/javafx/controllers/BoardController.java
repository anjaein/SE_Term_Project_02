package ui.javafx.controllers;

import ui.javafx.App;
import ui.javafx.BackendFacade;
import ui.javafx.UI;
import ui.javafx.domain.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;

import java.util.*;

public class BoardController {
    private static final List<Status> COLUMNS=List.of(
        Status.NEW, Status.ASSIGNED, Status.FIXED, Status.RESOLVED, Status.CLOSED, Status.REOPENED
    );

    private static final Map<Status, String> COL_DESC=Map.of(
        Status.NEW, "새로 들어온",
        Status.ASSIGNED, "담당 배정",
        Status.FIXED, "수정 완료",
        Status.RESOLVED, "검증 통과",
        Status.CLOSED, "종료",
        Status.REOPENED, "재오픈"
    );

    private static final DataFormat DF_ISSUE=new DataFormat("application/x-issue-id");

    @FXML private HBox header;
    @FXML private Label eyebrowLabel;
    @FXML private Label titleLabel;
    @FXML private Button newIssueButton;
    @FXML private TextField searchField;
    @FXML private HBox columnsRow;
    @FXML private Line divider;
    @FXML private ScrollPane scroller;

    private final SimpleStringProperty search=new SimpleStringProperty("");
    private BackendFacade facade;
    private App app;

    public void init(BackendFacade facade, App app){
        this.facade=facade;
        this.app=app;

        divider.setEndX(900);
        divider.getStyleClass().add("scribble-divider");
        divider.getStrokeDashArray().setAll(4d, 4d);

        Project project=facade.projectById(facade.currentProjectId.get());
        long visibleIssueCount=facade.issuesOf(project.getProjectId()).size();
        eyebrowLabel.setText("P-"+String.format("%03d", project.getProjectId())+" · "+visibleIssueCount+" ISSUES");
        titleLabel.setText(project.getName());
        newIssueButton.setVisible(true);
        newIssueButton.setManaged(true);

        searchField.textProperty().addListener((o, w, n) -> search.set(n));
        facade.issues.addListener((ListChangeListener<Issue>) c -> rebuildColumns());
        search.addListener((o, w, n) -> rebuildColumns());

        rebuildColumns();
        UI.fadeUp(header, 0);
        UI.fadeUp(scroller, 100);
    }

    @FXML
    private void handleCreateIssue(){
        app.openCreateIssueDialog();
    }

    private void rebuildColumns(){
        columnsRow.getChildren().clear();
        Map<Status, List<Issue>> grouped=new EnumMap<>(Status.class);
        for (Status s : COLUMNS) grouped.put(s, new ArrayList<>());

        for (Issue i : facade.issuesOf(facade.currentProjectId.get())) {
            if (!matches(i)) continue;
            grouped.computeIfAbsent(i.getStatus(), k -> new ArrayList<>()).add(i);
        }

        for (Status s : COLUMNS) {
            columnsRow.getChildren().add(buildColumn(s, grouped.get(s)));
        }
    }

    private boolean matches(Issue i){
        String q=search.get();
        if (q==null || q.isBlank()) return true;

        List<String> textTerms=new ArrayList<>();
        for (String token : q.trim().split("\\s+")) {
            int colon=token.indexOf(':');
            if (colon<=0 || colon>=token.length() - 1) {
                textTerms.add(token);
                continue;
            }
            String key=token.substring(0, colon).toLowerCase();
            String val=token.substring(colon+1).toLowerCase();
            switch (key) {
                case "assignee":
                    if (!usernameMatches(i.getAssigneeId(), val)) return false;
                    break;
                case "reporter":
                    if (!usernameMatches(i.getReporterId(), val)) return false;
                    break;
                case "fixer":
                    if (!usernameMatches(i.getFixerId(), val)) return false;
                    break;
                case "status":
                case "is":
                    if (!i.getStatus().name().toLowerCase().equals(val)) return false;
                    break;
                case "priority":
                    if (!i.getPriority().name().toLowerCase().equals(val)) return false;
                    break;
                default:
                    textTerms.add(token);
            }
        }
        if (!textTerms.isEmpty()) {
            String hay=(i.getTitle()+" "+i.getDescription()).toLowerCase();
            for (String term : textTerms) {
                if (!hay.contains(term.toLowerCase())) return false;
            }
        }
        return true;
    }

    private boolean usernameMatches(Long accountId, String value){
        Account a=facade.accountById(accountId);
        return a!=null && a.getUsername().toLowerCase().equals(value);
    }

    private boolean canDropToStatus(Status status){
        return status==Status.FIXED || status==Status.RESOLVED || status==Status.CLOSED || status==Status.REOPENED;
    }

    private VBox buildColumn(Status status, List<Issue> issues){
        VBox col=new VBox(10);
        col.setMinWidth(232);
        col.setPrefWidth(232);
        col.setPadding(new Insets(10));
        col.getStyleClass().add("kanban-col");

        HBox head=new HBox();
        VBox titleBox=new VBox(2);
        Label name=new Label(status.name());
        name.getStyleClass().addAll("kanban-col-name", "status-text-"+status.name().toLowerCase());
        Label desc=new Label(COL_DESC.get(status));
        desc.getStyleClass().addAll("hand", "dim", "small");
        titleBox.getChildren().addAll(name, desc);
        Region sp=new Region();
        HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
        Label count=new Label(String.valueOf(issues.size()));
        count.getStyleClass().add("h3");
        head.getChildren().addAll(titleBox, sp, count);
        col.getChildren().add(head);

        for (int idx=0; idx<issues.size(); idx++) {
            Issue i=issues.get(idx);
            VBox card=buildCard(i);
            UI.fadeUp(card, idx * 30);
            col.getChildren().add(card);
        }
        if (issues.isEmpty()) {
            Label empty=UI.hand("비어있음");
            empty.getStyleClass().addAll("dim", "small");
            empty.setPadding(new Insets(40, 8, 40, 8));
            col.getChildren().add(empty);
        }

        col.setOnDragOver(e -> {
            if (e.getDragboard().hasContent(DF_ISSUE)) {
                long issueId=(Long) e.getDragboard().getContent(DF_ISSUE);
                Issue dragged=facade.issueById(issueId);
                if (dragged!=null && dragged.getStatus()!=status && canDropToStatus(status)) {
                    e.acceptTransferModes(TransferMode.MOVE);
                    col.getStyleClass().removeAll("kanban-col-drag-ok", "kanban-col-drag-deny");
                    col.getStyleClass().add("kanban-col-drag-ok");
                }
                e.consume();
            }
        });
        col.setOnDragExited(e -> col.getStyleClass().removeAll("kanban-col-drag-ok", "kanban-col-drag-deny"));
        col.setOnDragDropped(e -> {
            col.getStyleClass().removeAll("kanban-col-drag-ok", "kanban-col-drag-deny");
            if (e.getDragboard().hasContent(DF_ISSUE)) {
                long issueId=(Long) e.getDragboard().getContent(DF_ISSUE);
                Issue dragged=facade.issueById(issueId);
                if (dragged==null) { e.consume(); return; }
                if (dragged.getStatus()==status) { e.setDropCompleted(false); e.consume(); return; }
                if (!canDropToStatus(status)) { e.setDropCompleted(false); e.consume(); return; }
                try {
                    facade.changeStatus(issueId, status);
                    e.setDropCompleted(true);
                } catch (IllegalStateException ex) {
                    UI.showErrorSticker(scroller, ex.getMessage());
                    e.setDropCompleted(false);
                }
            }
            e.consume();
        });

        return col;
    }

    private VBox buildCard(Issue i){
        VBox card=UI.card();
        card.getStyleClass().add("issue-card");
        card.setMinHeight(96);
        card.setSpacing(6);
        card.setCursor(Cursor.HAND);

        HBox top=new HBox();
        Label id=new Label("#"+i.getIssueId());
        id.getStyleClass().addAll("mono", "small", "dim");
        Region sp=new Region();
        HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
        top.getChildren().addAll(id, sp, UI.priorityBadge(i.getPriority()));

        Label t=UI.hand(i.getTitle());
        t.getStyleClass().add("issue-title");
        t.setMaxWidth(200);

        HBox bottom=new HBox();
        bottom.setAlignment(Pos.CENTER_LEFT);
        Account assignee=facade.accountById(i.getAssigneeId());
        if (assignee!=null) {
            bottom.getChildren().add(UI.avatar(assignee, facade.roleOf(assignee, i.getProjectId()), 22));
        } else {
            Label u=new Label("unassigned");
            u.getStyleClass().addAll("hand", "dim", "small");
            bottom.getChildren().add(u);
        }
        Region sp2=new Region();
        HBox.setHgrow(sp2, javafx.scene.layout.Priority.ALWAYS);
        Label ago=new Label(UI.timeAgo(i.getReportedDate()));
        ago.getStyleClass().addAll("mono", "small", "dim");
        bottom.getChildren().addAll(sp2, ago);

        card.getChildren().addAll(top, t, bottom);
        card.setOnMouseClicked(e -> app.openIssue(i.getIssueId()));
        card.setOnDragDetected(e -> {
            Dragboard db=card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent c=new ClipboardContent();
            c.put(DF_ISSUE, i.getIssueId());
            card.setOpacity(0.5);
            db.setContent(c);
            e.consume();
        });
        card.setOnDragDone(e -> card.setOpacity(1));

        UI.hoverLift(card);
        return card;
    }
}
