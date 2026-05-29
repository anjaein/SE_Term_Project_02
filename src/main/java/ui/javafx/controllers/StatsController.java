package ui.javafx.controllers;

import ui.javafx.BackendFacade;
import ui.javafx.UI;
import ui.javafx.domain.*;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;

import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.util.*;

public class StatsController {
    @FXML private GridPane kpis;
    @FXML private GridPane charts;
    @FXML private Line divider;

    private BackendFacade facade;

    public void init(BackendFacade facade){
        this.facade=facade;
        divider.setEndX(900);
        divider.getStyleClass().add("scribble-divider");
        divider.getStrokeDashArray().setAll(4d, 4d);
        rebuild();
        facade.issues.addListener((ListChangeListener<Issue>) c -> rebuild());
    }

    private void rebuild(){
        if (facade.currentUser.get()==null) {
            return;
        }

        long projectId=facade.currentProjectId.get();

        kpis.getChildren().clear();
        kpis.getColumnConstraints().clear();
        kpis.setVisible(false);
        kpis.setManaged(false);

        charts.getChildren().clear();
        charts.getColumnConstraints().clear();
        charts.add(chartCard("Monthly reports", "월별 신규 이슈 등록 추이", buildMonthlyReportedChart(projectId)), 0, 0, 1, 1);
        charts.add(chartCard("Monthly resolved", "월별 해결 이슈 추이", buildMonthlyResolvedChart(projectId)), 1, 0, 1, 1);
        charts.add(chartCard("Priority by month", "월별 우선순위 분포", buildMonthlyPriorityChart(projectId)), 0, 1, 1, 1);
        charts.add(chartCard("Monthly close time", "월별 평균 종료 소요일", buildMonthlyAverageClosedDaysChart(projectId)), 1, 1, 1, 1);
        for (int i=0; i<2; i++) {
            ColumnConstraints cc=new ColumnConstraints();
            cc.setPercentWidth(50);
            charts.getColumnConstraints().add(cc);
        }

        UI.fadeUp(charts, 100);
    }

    private VBox chartCard(String title, String subtitle, javafx.scene.Node body){
        VBox v=UI.cardTinted();
        Label t=new Label(title);
        t.getStyleClass().add("h3");
        Label sub=UI.hand(subtitle);
        sub.getStyleClass().add("dim");
        VBox head=new VBox(2, t, sub);
        v.getChildren().addAll(head, body);
        return v;
    }

    private LineChart<String, Number> buildMonthlyReportedChart(long projectId){
        CategoryAxis x=new CategoryAxis();
        NumberAxis y=new NumberAxis();
        x.setTickLabelRotation(-30);
        LineChart<String, Number> ch=new LineChart<>(x, y);
        ch.setLegendVisible(false);
        ch.setAnimated(false);
        ch.setPrefHeight(240);
        ch.setCreateSymbols(true);

        XYChart.Series<String, Number> s=new XYChart.Series<>();
        DateTimeFormatter f=DateTimeFormatter.ofPattern("yyyy-MM");
        facade.monthlyReportedTrend(projectId, 6).entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> s.getData().add(new XYChart.Data<>(entry.getKey().format(f), entry.getValue())));
        ch.getData().add(s);
        return ch;
    }

    private LineChart<String, Number> buildMonthlyResolvedChart(long projectId){
        CategoryAxis x=new CategoryAxis();
        NumberAxis y=new NumberAxis();
        x.setTickLabelRotation(-30);
        LineChart<String, Number> ch=new LineChart<>(x, y);
        ch.setLegendVisible(false);
        ch.setAnimated(false);
        ch.setPrefHeight(240);
        ch.setCreateSymbols(true);

        XYChart.Series<String, Number> s=new XYChart.Series<>();
        DateTimeFormatter f=DateTimeFormatter.ofPattern("yyyy-MM");
        facade.monthlyResolvedTrend(projectId, 6).entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> s.getData().add(new XYChart.Data<>(entry.getKey().format(f), entry.getValue())));
        ch.getData().add(s);
        return ch;
    }

    private StackedBarChart<String, Number> buildMonthlyPriorityChart(long projectId){
        CategoryAxis x=new CategoryAxis();
        NumberAxis y=new NumberAxis();
        x.setTickLabelRotation(-30);
        StackedBarChart<String, Number> ch=new StackedBarChart<>(x, y);
        ch.setAnimated(false);
        ch.setPrefHeight(260);
        ch.setLegendVisible(true);

        Map<YearMonth, Map<ui.javafx.domain.Priority, Long>> distribution=
            facade.monthlyPriorityDistribution(projectId, 6);
        DateTimeFormatter f=DateTimeFormatter.ofPattern("yyyy-MM");
        for (ui.javafx.domain.Priority priority : ui.javafx.domain.Priority.values()) {
            XYChart.Series<String, Number> series=new XYChart.Series<>();
            series.setName(priority.name());
            distribution.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> series.getData().add(
                    new XYChart.Data<>(entry.getKey().format(f), entry.getValue().getOrDefault(priority, 0L))));
            ch.getData().add(series);
        }
        return ch;
    }

    private BarChart<String, Number> buildMonthlyAverageClosedDaysChart(long projectId){
        CategoryAxis x=new CategoryAxis();
        NumberAxis y=new NumberAxis();
        x.setTickLabelRotation(-30);
        BarChart<String, Number> ch=new BarChart<>(x, y);
        ch.setAnimated(false);
        ch.setLegendVisible(false);
        ch.setPrefHeight(260);

        XYChart.Series<String, Number> series=new XYChart.Series<>();
        DateTimeFormatter f=DateTimeFormatter.ofPattern("yyyy-MM");
        facade.monthlyAverageClosedDays(projectId, 6).entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey().format(f), entry.getValue())));
        ch.getData().add(series);
        return ch;
    }
}
