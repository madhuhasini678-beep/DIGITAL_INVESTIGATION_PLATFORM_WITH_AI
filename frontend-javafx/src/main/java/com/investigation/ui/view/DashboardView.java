package com.investigation.ui.view;

import com.investigation.ui.api.ApiClient;
import com.investigation.ui.model.CaseModel;
import com.investigation.ui.model.CaseState;
import com.investigation.ui.model.DashboardSummaryModel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.Consumer;

public class DashboardView extends VBox {

    private final ApiClient apiClient;
    private final Consumer<String> navigationHandler;

    private final Label totalCasesVal = new Label("-");
    private final Label activeCasesVal = new Label("-");
    private final Label totalEvidenceVal = new Label("-");
    private final Label totalAnomaliesVal = new Label("-");
    private final Label totalEventsVal = new Label("-");

    private final Label activeCaseTitle = new Label("No Active Case Selected");
    private final Label activeCaseMeta = new Label("Select a case from the Cases section to begin investigation.");
    private final TableView<CaseModel> recentCasesTable = new TableView<>();

    public DashboardView(ApiClient apiClient, Consumer<String> navigationHandler) {
        this.apiClient = apiClient;
        this.navigationHandler = navigationHandler;

        setSpacing(20);
        setPadding(new Insets(24));
        getStyleClass().add("bg-base");

        buildUI();
        bindState();
        refresh();
    }

    private void buildUI() {
        // Section Title & Actions Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Investigation Dashboard");
        title.getStyleClass().add("text-h1");
        Label subtitle = new Label("Real-time telemetry, active dockets, and evidentiary status overview");
        subtitle.getStyleClass().add("text-secondary");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄 Refresh Dashboard");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> refresh());

        Button newCaseBtn = new Button("+ New Case");
        newCaseBtn.getStyleClass().add("btn-primary");
        newCaseBtn.setOnAction(e -> navigationHandler.accept("Cases"));

        header.getChildren().addAll(titleBox, spacer, refreshBtn, newCaseBtn);

        // Active Case Hero Card
        VBox activeCard = new VBox(10);
        activeCard.getStyleClass().add("bg-surface-elevated");
        activeCard.setPadding(new Insets(18));

        HBox activeHeader = new HBox(12);
        activeHeader.setAlignment(Pos.CENTER_LEFT);
        Label activeTag = new Label("CURRENT WORKING CASE");
        activeTag.getStyleClass().add("active-case-badge");
        activeHeader.getChildren().add(activeTag);

        activeCaseTitle.getStyleClass().add("text-h2");
        activeCaseMeta.getStyleClass().add("text-secondary");

        HBox activeActions = new HBox(12);
        activeActions.setAlignment(Pos.CENTER_LEFT);
        Button exploreEvidenceBtn = new Button("📂 Evidence Repository");
        exploreEvidenceBtn.getStyleClass().add("btn-secondary");
        exploreEvidenceBtn.setOnAction(e -> navigationHandler.accept("Evidence"));

        Button viewTimelineBtn = new Button("⏱ Event Timeline");
        viewTimelineBtn.getStyleClass().add("btn-secondary");
        viewTimelineBtn.setOnAction(e -> navigationHandler.accept("Timeline"));

        Button viewGraphBtn = new Button("🕸 Relationship Graph");
        viewGraphBtn.getStyleClass().add("btn-secondary");
        viewGraphBtn.setOnAction(e -> navigationHandler.accept("Relationship Graph"));

        Button viewAnomaliesBtn = new Button("⚠ Anomaly Analysis");
        viewAnomaliesBtn.getStyleClass().add("btn-secondary");
        viewAnomaliesBtn.setOnAction(e -> navigationHandler.accept("Anomalies"));

        Button viewAiSummaryBtn = new Button("🧠 AI Summary Report");
        viewAiSummaryBtn.getStyleClass().add("btn-secondary");
        viewAiSummaryBtn.setOnAction(e -> navigationHandler.accept("AI Summary"));

        activeActions.getChildren().addAll(exploreEvidenceBtn, viewTimelineBtn, viewGraphBtn, viewAnomaliesBtn, viewAiSummaryBtn);
        activeCard.getChildren().addAll(activeHeader, activeCaseTitle, activeCaseMeta, activeActions);

        // Metrics Grid (5 KPI Cards)
        HBox statsGrid = new HBox(16);
        statsGrid.getChildren().addAll(
                createKpiCard("TOTAL CASES", totalCasesVal, "All forensic dockets"),
                createKpiCard("ACTIVE INVESTIGATIONS", activeCasesVal, "Currently under review"),
                createKpiCard("EVIDENCE FILES", totalEvidenceVal, "SHA-256 verified objects"),
                createKpiCard("FLAGGED ANOMALIES", totalAnomaliesVal, "Statistical deviations"),
                createKpiCard("TIMELINE EVENTS", totalEventsVal, "Sequenced records")
        );

        // Recent Cases Section
        VBox tableContainer = new VBox(12);
        tableContainer.getStyleClass().add("bg-surface");
        tableContainer.setPadding(new Insets(16));

        Label tableTitle = new Label("Recent Historical & Active Cases");
        tableTitle.getStyleClass().add("text-h2");

        configureRecentCasesTable();
        tableContainer.getChildren().addAll(tableTitle, recentCasesTable);
        VBox.setVgrow(recentCasesTable, Priority.ALWAYS);

        getChildren().addAll(header, activeCard, statsGrid, tableContainer);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
    }

    private VBox createKpiCard(String label, Label valueLabel, String subtitle) {
        VBox card = new VBox(6);
        card.getStyleClass().add("bg-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("text-muted");
        lbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");

        valueLabel.getStyleClass().add("text-metric");

        Label sub = new Label(subtitle);
        sub.getStyleClass().add("text-secondary");
        sub.setStyle("-fx-font-size: 11px;");

        card.getChildren().addAll(lbl, valueLabel, sub);
        return card;
    }

    private void configureRecentCasesTable() {
        TableColumn<CaseModel, String> numCol = new TableColumn<>("Case #");
        numCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCaseNumber()));
        numCol.setPrefWidth(140);

        TableColumn<CaseModel, String> titleCol = new TableColumn<>("Case Title");
        titleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        titleCol.setPrefWidth(260);

        TableColumn<CaseModel, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory()));
        catCol.setPrefWidth(180);

        TableColumn<CaseModel, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setPrefWidth(120);

        TableColumn<CaseModel, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button selectBtn = new Button("Set Active");

            {
                selectBtn.getStyleClass().add("btn-secondary");
                selectBtn.setOnAction(e -> {
                    CaseModel item = getTableView().getItems().get(getIndex());
                    CaseState.getInstance().setActiveCase(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : selectBtn);
            }
        });

        recentCasesTable.getColumns().addAll(numCol, titleCol, catCol, statusCol, actionCol);
        recentCasesTable.setPlaceholder(new Label("No recent cases found."));
        recentCasesTable.setPrefHeight(250);
    }

    private void bindState() {
        CaseState.getInstance().activeCaseProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                if (newVal != null) {
                    activeCaseTitle.setText(newVal.getTitle() + " (" + newVal.getCaseNumber() + ")");
                    activeCaseMeta.setText("Category: " + newVal.getCategory() + " | Status: " + newVal.getStatus() +
                            " | Evidence: " + newVal.getEvidenceCount() + " | Anomalies: " + newVal.getAnomalyCount());
                } else {
                    activeCaseTitle.setText("No Active Case Selected");
                    activeCaseMeta.setText("Select a case from the Cases section to begin investigation.");
                }
            });
        });
    }

    public void refresh() {
        apiClient.getDashboardSummary().thenAccept(sum -> {
            Platform.runLater(() -> {
                totalCasesVal.setText(String.valueOf(sum.getTotalCases()));
                activeCasesVal.setText(String.valueOf(sum.getActiveCases()));
                totalEvidenceVal.setText(String.valueOf(sum.getTotalEvidenceFiles()));
                totalAnomaliesVal.setText(String.valueOf(sum.getTotalAnomaliesDetected()));
                totalEventsVal.setText(String.valueOf(sum.getTotalTimelineEvents()));
            });
        }).exceptionally(ex -> null);

        apiClient.getCases("", "", "").thenAccept(cases -> {
            Platform.runLater(() -> {
                recentCasesTable.getItems().setAll(cases);
                if (CaseState.getInstance().getActiveCase() == null && !cases.isEmpty()) {
                    CaseState.getInstance().setActiveCase(cases.get(0));
                }
            });
        }).exceptionally(ex -> null);
    }
}
