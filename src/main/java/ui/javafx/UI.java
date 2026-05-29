package ui.javafx;

import ui.javafx.domain.*;
import ui.javafx.domain.Priority;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class UI {
    public static final double AVATAR_INITIAL_FONT_SCALE=0.45;

    public static double avatarInitialFontSize(double avatarSize){
        return avatarSize * AVATAR_INITIAL_FONT_SCALE;
    }

    public static String accountName(Account a){
        return a==null ? "?" : a.getUsername();
    }

    public static String accountHandle(Account a){
        return a==null ? "" : "@"+a.getUsername();
    }

    public static String accountRoleName(Account a){
        return a==null || a.getRole()==null ? "UNKNOWN" : a.getRole().name();
    }

    public static String accountToneClass(Account a){
        if (a==null || a.getRole()==null) return "account-unknown";
        return "account-"+a.getRole().name().toLowerCase();
    }

    public static String statusToneClass(Status s){
        return "status-"+s.name().toLowerCase();
    }

    public static void applyAccountTone(Node node, Account a){
        node.getStyleClass().removeAll(List.of(
            "account-admin",
            "account-pl",
            "account-dev",
            "account-tester",
            "account-unknown"
        ));
        node.getStyleClass().add(accountToneClass(a));
    }

    public static String accountInitial(Account a){
        String n=accountName(a);
        return n.isEmpty() ? "?" : n.substring(0, 1);
    }

    //── 아바타 (원형 색상 이니셜) ─────────────────────────
    public static StackPane avatar(Account a, double size){
        StackPane p=new StackPane();
        p.setMinSize(size, size); p.setMaxSize(size, size);
        Circle c=new Circle(size / 2);
        c.getStyleClass().add("avatar-circle");
        applyAccountTone(c, a);
        c.setStrokeWidth(1.4);
        Label lbl=new Label(accountInitial(a));
        lbl.getStyleClass().add("avatar-initial");
        applyAccountTone(lbl, a);
        lbl.setStyle("-fx-font-size: "+avatarInitialFontSize(size)+"px;");
        p.getChildren().addAll(c, lbl);
        return p;
    }

    //── 뱃지 (Status / Priority) ─────────────────────────────
    public static Label statusBadge(Status s){
        Label l=new Label(s.name());
        l.getStyleClass().addAll("badge", "badge-status-"+s.name().toLowerCase());
        return l;
    }
    public static Label priorityBadge(Priority p){
        Label l=new Label(p.name());
        l.getStyleClass().addAll("badge", "badge-priority-"+p.name().toLowerCase());
        return l;
    }

    //── 카드 (살짝 그림자+종이톤 배경) ─────────────────
    public static VBox card(){
        VBox v=new VBox();
        v.getStyleClass().add("card");
        v.setPadding(new Insets(16));
        v.setSpacing(8);
        return v;
    }

    public static VBox cardTinted(){
        VBox v=card();
        v.getStyleClass().add("card-tinted");
        return v;
    }

    //── 헤딩 ────────────────────────────
    public static Label h1(String s){
        Label l=new Label(s);
        l.getStyleClass().add("h1");
        return l;
    }
    public static Label h2(String s){
        Label l=new Label(s);
        l.getStyleClass().add("h2");
        return l;
    }
    public static Label eyebrow(String s){
        Label l=new Label(s);
        l.getStyleClass().add("eyebrow");
        return l;
    }
    public static Label hand(String s){
        Label l=new Label(s);
        l.getStyleClass().add("hand");
        l.setWrapText(true);
        return l;
    }

    //── 절취선 (대시 라인) ─────────────────────────────
    public static Line scribbleLine(double width){
        Line l=new Line(0, 0, width, 0);
        l.getStyleClass().add("scribble-divider");
        l.setStrokeWidth(1);
        l.getStrokeDashArray().addAll(4d, 4d);
        return l;
    }

    //── 시간 포매팅 ───────────────────────────────────
    private static final DateTimeFormatter DAY_FMT=DateTimeFormatter.ofPattern("M월 d일");
    public static String timeAgo(LocalDateTime t){
        if (t==null) return "";
        long s=ChronoUnit.SECONDS.between(t, LocalDateTime.now());
        if (s<60) return "now";
        if (s<3600) return (s / 60)+"m ago";
        if (s<86400) return (s / 3600)+"h ago";
        return (s / 86400)+"d ago";
    }
    public static String dayLabel(LocalDateTime t){
        if (t==null) return "—";
        return t.format(DAY_FMT);
    }

    //── fade-up 애니메이션 ────────────────────────────
    public static void fadeUp(Node n, double delayMs){
        n.setOpacity(0);
        n.setTranslateY(8);
        FadeTransition ft=new FadeTransition(Duration.millis(420), n);
        ft.setFromValue(0); ft.setToValue(1);
        TranslateTransition tt=new TranslateTransition(Duration.millis(420), n);
        tt.setFromY(8); tt.setToY(0);
        ParallelTransition pt=new ParallelTransition(ft, tt);
        pt.setDelay(Duration.millis(delayMs));
        pt.play();
    }

    //── pop-in 애니메이션 ─────────────────────────────
    public static void popIn(Node n){
        n.setScaleX(0.6); n.setScaleY(0.6); n.setOpacity(0);
        ScaleTransition st=new ScaleTransition(Duration.millis(300), n);
        st.setFromX(0.6); st.setToX(1); st.setFromY(0.6); st.setToY(1);
        FadeTransition ft=new FadeTransition(Duration.millis(300), n);
        ft.setFromValue(0); ft.setToValue(1);
        new ParallelTransition(st, ft).play();
    }

    //── 호버 살짝 떠오르는 효과 ────────────────────
    public static <T extends Node> T hoverLift(T n){
        n.setOnMouseEntered(e -> {
            TranslateTransition t=new TranslateTransition(Duration.millis(120), n);
            t.setToY(-2); t.play();
            n.setRotate(-0.4);
        });
        n.setOnMouseExited(e -> {
            TranslateTransition t=new TranslateTransition(Duration.millis(120), n);
            t.setToY(0); t.play();
            n.setRotate(0);
        });
        return n;
    }

    public static void showErrorSticker(Node anchor, String message){
        if (anchor==null || anchor.getScene()==null) return;
        Window owner=anchor.getScene().getWindow();
        if (owner==null) return;

        Label sticker=new Label(message==null || message.isBlank() ? "문제가 생겼습니다." : message);
        sticker.getStyleClass().add("error-sticker");
        sticker.setWrapText(true);
        sticker.setMaxWidth(360);

        Popup popup=new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.getContent().add(sticker);

        popup.show(owner, owner.getX()+owner.getWidth() - 400, owner.getY()+72);
        sticker.setOpacity(0);
        sticker.setTranslateY(-8);

        FadeTransition fadeIn=new FadeTransition(Duration.millis(120), sticker);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        TranslateTransition drop=new TranslateTransition(Duration.millis(120), sticker);
        drop.setFromY(-8);
        drop.setToY(0);

        PauseTransition wait=new PauseTransition(Duration.seconds(2.4));
        FadeTransition fadeOut=new FadeTransition(Duration.millis(180), sticker);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> popup.hide());

        new SequentialTransition(new ParallelTransition(fadeIn, drop), wait, fadeOut).play();
    }

    public static void runWithErrorSticker(Node anchor, Runnable action){
        try {
            action.run();
        } catch (IllegalStateException ex) {
            showErrorSticker(anchor, ex.getMessage());
        }
    }

}
