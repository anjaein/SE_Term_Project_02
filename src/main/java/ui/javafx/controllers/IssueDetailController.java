package ui.javafx.controllers;

import ui.javafx.App;
import ui.javafx.BackendFacade;
import ui.javafx.UI;
import ui.javafx.domain.*;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.StringConverter;

import java.util.List;

public class IssueDetailController {
    private static final List<Status> FLOW=List.of(
        Status.NEW, Status.ASSIGNED, Status.FIXED, Status.RESOLVED, Status.CLOSED
    );

    @FXML private ScrollPane root;
    @FXML private HBox layout;
    @FXML private VBox main;
    @FXML private VBox sidebar;

    private BackendFacade facade;
    private App app;
    private long issueId;

    public void init(BackendFacade facade, long issueId, App app){
        this.facade=facade;
        this.issueId=issueId;
        this.app=app;
        layout.setPadding(new Insets(24, 32, 80, 32));
        rebuild();
        facade.issues.addListener((ListChangeListener<Issue>) c -> rebuild());
        facade.comments.addListener((ListChangeListener<Comment>) c -> rebuild());
    }

    private void rebuild(){
        Issue issue=facade.issueById(issueId);
        main.getChildren().clear();
        sidebar.getChildren().clear();
        if (issue==null) {
            main.getChildren().add(new Label("이슈를 찾을 수 없음."));
            return;
        }
        Account reporter=facade.accountById(issue.getReporterId());
        Account assignee=facade.accountById(issue.getAssigneeId());
        Account fixer=facade.accountById(issue.getFixerId());
        Account me=facade.currentUser.get();

        Button back=new Button("← back to board");
        back.getStyleClass().add("btn-link");
        back.setOnAction(e -> app.switchView(App.View.BOARD, null));

        HBox idBar=new HBox(10);
        idBar.setAlignment(Pos.CENTER_LEFT);
        Label idLbl=new Label("ISSUE #"+issue.getIssueId());
        idLbl.getStyleClass().addAll("mono", "small", "dim");
        idBar.getChildren().addAll(idLbl, UI.statusBadge(issue.getStatus()));
        if (issue.getStatus()==Status.CLOSED) {
            Label check=new Label("✓");
            check.getStyleClass().add("check-closed");
            idBar.getChildren().add(check);
        }

        Label title=UI.hand(issue.getTitle());
        title.getStyleClass().add("h1");

        VBox descCard=UI.cardTinted();
        Label descText=UI.hand(issue.getDescription());
        descText.getStyleClass().add("body");
        descCard.getChildren().addAll(UI.eyebrow("DESCRIPTION"), descText);

        Label flowHead=new Label("Status flow");
        flowHead.getStyleClass().add("h3");
        HBox flow=buildStatusFlow(issue);

        Role role=facade.currentProjectRole();
        VBox actionsBox=UI.cardTinted();
        Label ah=UI.eyebrow("YOUR ACTIONS · "+role);
        FlowPane btnRow=new FlowPane(8, 8);
        for (Status n : List.of(Status.FIXED, Status.RESOLVED, Status.CLOSED, Status.REOPENED)) {
            Button b=new Button("→ "+n);
            b.getStyleClass().add("btn-accent");
            b.setOnAction(e -> {
                UI.runWithErrorSticker(root, () ->
                    facade.changeStatus(issue.getIssueId(), n));
            });
            btnRow.getChildren().add(b);
        }
        actionsBox.getChildren().addAll(ah, btnRow);

        VBox commentsBox=buildCommentsBox(issue);

        main.getChildren().addAll(back, idBar, title, descCard, flowHead, flow);
        main.getChildren().add(actionsBox);
        main.getChildren().add(commentsBox);

        sidebar.getChildren().addAll(
            sidebarBlock("ASSIGNEE", buildAssigneeBlock(issue, assignee)),
            sidebarBlock("REPORTER", buildPersonBlock(reporter, UI.timeAgo(issue.getReportedDate()))),
            fixer!=null ? sidebarBlock("FIXED BY", buildPersonBlock(fixer, UI.timeAgo(issue.getFixedDate()))) : new VBox(),
            sidebarBlock("PRIORITY", buildPriorityBlock(issue))
        );

        UI.fadeUp(main, 0);
        UI.fadeUp(sidebar, 80);
    }

    private VBox buildCommentsBox(Issue issue){
        VBox commentsBox=new VBox(12);
        Label ch=new Label("Comments");
        ch.getStyleClass().add("h3");
        List<Comment> cmts=facade.commentsOf(issue.getIssueId());
        Label countL=new Label(" ("+cmts.size()+")");
        countL.getStyleClass().add("dim");
        HBox chHead=new HBox(ch, countL);
        chHead.setAlignment(Pos.BASELINE_LEFT);

        VBox cmtList=new VBox(10);
        for (Comment c : cmts) cmtList.getChildren().add(buildComment(c));
        if (cmts.isEmpty()) {
            Label empty=UI.hand("아직 코멘트가 없어요. 첫 코멘트를 남겨보세요.");
            empty.getStyleClass().add("dim");
            empty.setPadding(new Insets(16));
            cmtList.getChildren().add(empty);
        }

        HBox compose=new HBox(10);
        Account me=facade.currentUser.get();
        TextArea ta=new TextArea();
        ta.setPromptText("댓글 쓰기...");
        ta.setPrefRowCount(2);
        ta.getStyleClass().add("input");
        HBox.setHgrow(ta, javafx.scene.layout.Priority.ALWAYS);
        VBox composeRight=new VBox(8, ta);
        Button send=new Button("Post");
        send.getStyleClass().add("btn-primary");
        send.setOnAction(e -> {
            String t=ta.getText().trim();
            if (t.isEmpty()) return;
            UI.runWithErrorSticker(root, () -> {
                facade.addComment(issue.getIssueId(), t);
                ta.clear();
            });
        });
        HBox sendRow=new HBox(send);
        sendRow.setAlignment(Pos.CENTER_RIGHT);
        composeRight.getChildren().add(sendRow);
        compose.getChildren().addAll(UI.avatar(me, facade.currentProjectRole(), 36), composeRight);

        commentsBox.getChildren().addAll(chHead, cmtList, compose);
        return commentsBox;
    }

    private VBox sidebarBlock(String label, javafx.scene.Node content){
        VBox b=new VBox(8);
        b.setPadding(new Insets(14, 0, 14, 0));
        b.getStyleClass().add("sidebar-block");
        b.getChildren().addAll(UI.eyebrow(label), content);
        return b;
    }

    private javafx.scene.Node buildAssigneeBlock(Issue issue, Account assignee){
        VBox v=new VBox(10);
        if (assignee!=null) {
            HBox row=new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Role assigneeRole=facade.roleOf(assignee, issue.getProjectId());
            Label name=new Label(UI.accountRoleName(assignee, assigneeRole));
            name.getStyleClass().add("hand");
            Label r=new Label(UI.accountHandle(assignee));
            r.getStyleClass().addAll("mono", "small", "dim");
            VBox txt=new VBox(2, name, r);
            row.getChildren().addAll(UI.avatar(assignee, assigneeRole, 34), txt);
            v.getChildren().add(row);
        } else {
            Label none=UI.hand("unassigned");
            none.getStyleClass().add("dim");
            v.getChildren().add(none);
        }
        List<ProjectMember> candidates=facade.membersOf(issue.getProjectId()).stream()
            .filter(m -> m.getRole()==Role.DEV || m.getRole()==Role.PL)
            .toList();

        ComboBox<ProjectMember> picker=new ComboBox<>();
        picker.getItems().addAll(candidates);
        picker.setPromptText("담당자 선택...");
        picker.setMaxWidth(Double.MAX_VALUE);
        picker.setConverter(new StringConverter<>() {
            @Override public String toString(ProjectMember member){
                Account account=member==null ? null : facade.accountById(member.getAccountId());
                return account==null ? "" : UI.accountRoleName(account, member.getRole())+" "+UI.accountHandle(account);
            }
            @Override public ProjectMember fromString(String s){ return null; }
        });

        Button assignBtn=new Button("Assign");
        assignBtn.getStyleClass().add("btn-default");
        assignBtn.setOnAction(e -> {
            ProjectMember chosen=picker.getValue();
            if (chosen!=null) {
                UI.runWithErrorSticker(root, () ->
                    facade.assignIssue(issue.getIssueId(), chosen.getAccountId()));
            }
        });
        HBox pickRow=new HBox(8, picker, assignBtn);
        pickRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(picker, javafx.scene.layout.Priority.ALWAYS);
        v.getChildren().add(pickRow);

        Button btn=new Button("+ Recommend assignee");
        btn.getStyleClass().add("btn-accent");
        btn.setOnAction(e -> showRecommendDialog(issue));
        v.getChildren().add(btn);
        return v;
    }

    private javafx.scene.Node buildPersonBlock(Account a, String ago){
        if (a==null) return new Label("—");
        Role role=facade.roleOf(a, facade.currentProjectId.get());
        HBox row=new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label name=new Label(UI.accountRoleName(a, role));
        name.getStyleClass().add("hand");
        Label time=new Label(UI.accountHandle(a)+" · "+ago);
        time.getStyleClass().addAll("mono", "small", "dim");
        VBox txt=new VBox(2, name, time);
        row.getChildren().addAll(UI.avatar(a, role, 28), txt);
        return row;
    }

    private javafx.scene.Node buildPriorityBlock(Issue issue){
        return UI.priorityBadge(issue.getPriority());
    }

    private HBox buildStatusFlow(Issue issue){
        HBox box=new HBox(0);
        box.setAlignment(Pos.TOP_LEFT);
        int currentIdx=FLOW.indexOf(issue.getStatus());
        boolean reopened=issue.getStatus()==Status.REOPENED;

        for (int i=0; i<FLOW.size(); i++) {
            Status s=FLOW.get(i);
            boolean done=!reopened && i<=currentIdx;
            boolean active=!reopened && i==currentIdx;

            VBox step=new VBox(6);
            step.setAlignment(Pos.CENTER);
            step.setMinWidth(96);

            StackPane bullet=new StackPane();
            Circle outer=new Circle(15);
            outer.getStyleClass().addAll("flow-step-circle", UI.statusToneClass(s));
            if (done) {
                outer.getStyleClass().add("flow-step-circle-done");
            }
            outer.setStrokeWidth(2);
            Label num=new Label(active ? String.valueOf(i+1) : (done ? "✓" : ""));
            num.getStyleClass().addAll("flow-step-num",
                done ? "flow-step-num-done" : "flow-step-num-"+s.name().toLowerCase());
            bullet.getChildren().addAll(outer, num);

            Label nm=new Label(s.name());
            nm.getStyleClass().addAll("flow-step-name",
                done ? "flow-step-name-done-"+s.name().toLowerCase() : "flow-step-name-pending");

            step.getChildren().addAll(bullet, nm, new LocalDateTimeLabel(stampOf(issue, s)));

            box.getChildren().add(step);
            if (i<FLOW.size() - 1) {
                Line line=new Line(0, 0, 30, 0);
                line.getStyleClass().add("flow-step-line");
                if (i<currentIdx && !reopened) {
                    line.getStyleClass().add(UI.statusToneClass(FLOW.get(i+1)));
                }
                line.setStrokeWidth(2);
                line.getStrokeDashArray().setAll(i<currentIdx && !reopened ? List.of() : List.of(3d, 3d));
                StackPane lineWrap=new StackPane(line);
                lineWrap.setPadding(new Insets(15, 0, 0, 0));
                lineWrap.setPrefWidth(40);
                HBox.setHgrow(lineWrap, javafx.scene.layout.Priority.ALWAYS);
                box.getChildren().add(lineWrap);
            }
        }
        if (reopened) {
            Label tag=new Label("REOPENED!");
            tag.getStyleClass().add("reopened-stamp");
            tag.setRotate(-3);
            HBox wrap=new HBox(12, box, tag);
            wrap.setAlignment(Pos.CENTER_LEFT);
            HBox out=new HBox();
            out.getChildren().add(wrap);
            return out;
        }
        return box;
    }

    private java.time.LocalDateTime stampOf(Issue i, Status s){
        switch (s) {
            case NEW:
                return i.getReportedDate();
            case FIXED:
                return i.getFixedDate();
            case RESOLVED:
                return i.getResolvedDate();
            case CLOSED:
                return i.getClosedDate();
            case ASSIGNED:
            case REOPENED:
            default:
                return null;
        }
    }

    private static class LocalDateTimeLabel extends Label {
        LocalDateTimeLabel(java.time.LocalDateTime t){
            super(t==null ? "" : UI.dayLabel(t));
            getStyleClass().addAll("mono", "small", "dim");
        }
    }

    private HBox buildComment(Comment c){
        Account author=facade.accountById(c.getAuthorId());
        Account me=facade.currentUser.get();
        boolean canEdit=me!=null && me.getAccountId().equals(c.getAuthorId());
        boolean canDelete=me!=null && (me.getAccountId().equals(c.getAuthorId()) || facade.isCurrentUserAdmin());
        Role authorRole=facade.roleOf(author, facade.currentProjectId.get());

        HBox row=new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

        VBox right=new VBox(4);
        HBox meta=new HBox(8);
        meta.setAlignment(Pos.BASELINE_LEFT);
        Label name=new Label(UI.accountRoleName(author, authorRole));
        name.getStyleClass().add("hand");
        Label r=new Label(UI.accountHandle(author));
        r.getStyleClass().addAll("mono", "small", "dim");
        Region s=new Region();
        HBox.setHgrow(s, javafx.scene.layout.Priority.ALWAYS);
        Label time=new Label(UI.timeAgo(c.getCreatedDate()));
        time.getStyleClass().addAll("mono", "small", "dim");
        meta.getChildren().addAll(name, r, s, time);

        VBox bubble=UI.cardTinted();
        Label body=UI.hand(c.getContent());
        body.getStyleClass().add("body");
        bubble.getChildren().add(body);

        if (canEdit || canDelete) {
            HBox actions=new HBox(6);
            actions.setAlignment(Pos.CENTER_RIGHT);
            if (canEdit) {
                Button edit=new Button("수정");
                edit.getStyleClass().addAll("btn-icon", "small");
                edit.setOnAction(e -> startEditComment(c, bubble, body));
                actions.getChildren().add(edit);
            }
            if (canDelete) {
                Button del=new Button("삭제");
                del.getStyleClass().addAll("btn-icon", "small");
                del.setOnAction(e -> confirmDeleteComment(c));
                actions.getChildren().add(del);
            }
            bubble.getChildren().add(actions);
        }

        right.getChildren().addAll(meta, bubble);
        HBox.setHgrow(right, javafx.scene.layout.Priority.ALWAYS);
        row.getChildren().addAll(UI.avatar(author, authorRole, 36), right);
        return row;
    }

    private void startEditComment(Comment c, VBox bubble, Label body){
        TextArea ta=new TextArea(c.getContent());
        ta.getStyleClass().add("input");
        ta.setPrefRowCount(3);
        ta.setWrapText(true);
        Button save=new Button("저장");
        save.getStyleClass().add("btn-primary");
        Button cancel=new Button("취소");
        cancel.getStyleClass().add("btn-icon");
        HBox actions=new HBox(6, cancel, save);
        actions.setAlignment(Pos.CENTER_RIGHT);

        bubble.getChildren().setAll(ta, actions);
        ta.requestFocus();

        cancel.setOnAction(e -> rebuild());
        save.setOnAction(e -> {
            String t=ta.getText()==null ? "" : ta.getText().trim();
            if (t.isEmpty()) { UI.showErrorSticker(ta, "내용을 입력하세요."); return; }
            UI.runWithErrorSticker(root, () -> facade.updateComment(c.getCommentId(), t));
        });
    }

    private void confirmDeleteComment(Comment c){
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("댓글 삭제");
        alert.setHeaderText(null);
        alert.setContentText("이 댓글을 삭제할까요?");
        if (root.getScene()!=null) {
            alert.getDialogPane().getStylesheets().setAll(root.getScene().getStylesheets());
        }
        alert.showAndWait().filter(b -> b==ButtonType.OK).ifPresent(b ->
            UI.runWithErrorSticker(root, () -> facade.deleteComment(c.getCommentId())));
    }

    private void showRecommendDialog(Issue issue){
        Dialog<Void> d=new Dialog<>();
        d.setTitle("Assignee 추천");
        d.setHeaderText(null);
        d.getDialogPane().setMinWidth(560);
        d.getDialogPane().getStylesheets().setAll(root.getScene().getStylesheets());

        VBox content=new VBox(14);
        content.setPadding(new Insets(10));
        Label hint=UI.hand("과거에 비슷한 키워드의 이슈를 해결한 멤버를 보여드려요.");
        hint.getStyleClass().add("dim");

        VBox list=new VBox(10);
        List<BackendFacade.Recommendation> recs=facade.recommendAssignees(issue);
        for (int idx=0; idx<recs.size(); idx++) {
            BackendFacade.Recommendation r=recs.get(idx);
            Role devRole=facade.roleOf(r.dev(), issue.getProjectId());
            boolean top=idx==0 && r.score()>0;
            VBox card=top ? UI.cardTinted() : UI.card();
            HBox row=new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            VBox info=new VBox(4);
            HBox name=new HBox(8);
            name.setAlignment(Pos.BASELINE_LEFT);
            Label nm=new Label(UI.accountRoleName(r.dev(), devRole));
            nm.getStyleClass().add("hand");
            Label rl=new Label(UI.accountHandle(r.dev()));
            rl.getStyleClass().addAll("mono", "small", "dim");
            name.getChildren().addAll(nm, rl);
            if (top) {
                Label stamp=new Label("TOP MATCH");
                stamp.getStyleClass().add("top-match-stamp");
                stamp.setRotate(-6);
                name.getChildren().add(stamp);
            }
            Label stats=UI.hand("과거 해결 "+r.historyCount()+"건 · 매칭 점수 "+r.score());
            stats.getStyleClass().add("dim");
            info.getChildren().addAll(name, stats);
            Region sp=new Region();
            HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
            Button assign=new Button("Assign");
            assign.getStyleClass().add(top ? "btn-primary" : "btn-default");
            assign.setOnAction(e -> {
                UI.runWithErrorSticker(root, () -> {
                    facade.assignIssue(issue.getIssueId(), r.dev().getAccountId());
                    d.close();
                });
            });
            row.getChildren().addAll(UI.avatar(r.dev(), devRole, 42), info, sp, assign);
            card.getChildren().add(row);
            list.getChildren().add(card);
        }

        content.getChildren().addAll(hint, list);
        d.getDialogPane().setContent(content);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        d.showAndWait();
    }
}
