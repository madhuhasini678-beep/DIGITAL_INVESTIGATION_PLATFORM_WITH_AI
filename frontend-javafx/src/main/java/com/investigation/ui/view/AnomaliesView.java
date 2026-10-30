package com.investigation.ui.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.investigation.ui.api.ApiClient;
import com.investigation.ui.model.CaseModel;
import com.investigation.ui.model.CaseState;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class AnomaliesView extends VBox {

    private final ApiClient apiClient;
    private final Label headerLabel = new Label("Anomaly Analysis");
    private final Label subLabel = new Label("Statistical outliers and sequence deviations flagged with calibrated confidence");

    // Metric Summary Cards
    private final Label totalCountLabel = new Label("0");
    private final Label highCountLabel = new Label("0");
    private final Label medCountLabel = new Label("0");
    private final Label lowCountLabel = new Label("0");
    private final Label avgConfLabel = new Label("0%");

    // Filter controls
    private final ComboBox<String> severityFilter = new ComboBox<>();
    private final TextField searchField = new TextField();
    private final Label scanStatusLabel = new Label("");

    // Cards list
    private final VBox cardsContainer = new VBox(12);
    private final ScrollPane scrollPane = new ScrollPane();
    private final List<JsonNode> allAnomalies = new ArrayList<>();

    public AnomaliesView(ApiClient apiClient) {
        this.apiClient = apiClient;

        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("bg-base");

        // Top Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        headerLabel.getStyleClass().add("text-h1");
        subLabel.getStyleClass().add("text-secondary");
        titleBox.getChildren().addAll(headerLabel, subLabel);

        Button scanBtn = new Button("⚡ Run Anomaly Detection");
        scanBtn.getStyleClass().add("btn-primary");
        scanBtn.setOnAction(e -> runDetection());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> refresh());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        scanStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00e5ff;");

        header.getChildren().addAll(titleBox, spacer, scanStatusLabel, refreshBtn, scanBtn);

        // Metric Overview Bar
        HBox metricsRow = createMetricsRow();

        // Filter Bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("bg-surface");
        filterBar.setPadding(new Insets(10, 14, 10, 14));

        severityFilter.getItems().addAll("ALL SEVERITIES", "HIGH", "MEDIUM", "LOW");
        severityFilter.setValue("ALL SEVERITIES");
        severityFilter.getStyleClass().add("btn-secondary");
        severityFilter.setOnAction(e -> applyFilter());

        searchField.setPromptText("Search anomalies, metrics, entities...");
        searchField.setPrefWidth(260);
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());

        filterBar.getChildren().addAll(new Label("Filter:"), severityFilter, searchField);

        // Cards stream
        cardsContainer.setPadding(new Insets(6));
        scrollPane.setContent(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("bg-base");
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, metricsRow, filterBar, scrollPane);

        CaseState.getInstance().activeCaseProperty().addListener((obs, o, n) -> refresh());
        refresh();
    }

    private HBox createMetricsRow() {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        HBox card1 = createMetricCard("TOTAL ANOMALIES", totalCountLabel, "#00e5ff");
        HBox card2 = createMetricCard("HIGH SEVERITY", highCountLabel, "#ff1744");
        HBox card3 = createMetricCard("MEDIUM SEVERITY", medCountLabel, "#ffab00");
        HBox card4 = createMetricCard("LOW SEVERITY", lowCountLabel, "#2979ff");
        HBox card5 = createMetricCard("AVG CONFIDENCE", avgConfLabel, "#00e676");

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);
        HBox.setHgrow(card5, Priority.ALWAYS);

        row.getChildren().addAll(card1, card2, card3, card4, card5);
        return row;
    }

    private HBox createMetricCard(String title, Label valueLbl, String accentHex) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("bg-surface");
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle(card.getStyle() + "; -fx-border-color: #1a273b; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        VBox box = new VBox(2);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #8b9bb4; -fx-letter-spacing: 0.5px;");

        valueLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + accentHex + ";");

        box.getChildren().addAll(titleLbl, valueLbl);
        card.getChildren().add(box);
        return card;
    }

    private void runDetection() {
        CaseModel current = CaseState.getInstance().getActiveCase();
        if (current == null) {
            scanStatusLabel.setText("Please select an active case first.");
            return;
        }

        scanStatusLabel.setText("Running statistical & sequence anomaly scanner...");
        apiClient.detectAnomalies(current.getId()).thenAccept(node -> {
            Platform.runLater(() -> {
                scanStatusLabel.setText("✓ Scanner complete (" + (node.isArray() ? node.size() : 0) + " flagged)");
                refresh();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> scanStatusLabel.setText("Scan failed: " + ex.getMessage()));
            return null;
        });
    }

    public void refresh() {
        CaseModel current = CaseState.getInstance().getActiveCase();
        if (current == null) {
            headerLabel.setText("Anomaly Analysis (No Case Selected)");
            allAnomalies.clear();
            applyFilter();
            return;
        }

        headerLabel.setText("Anomaly Analysis: " + current.getTitle());
        apiClient.getAnomalies(current.getId()).thenAccept(node -> {
            Platform.runLater(() -> {
                allAnomalies.clear();
                if (node.isArray()) {
                    for (JsonNode anom : node) {
                        allAnomalies.add(anom);
                    }
                }
                updateMetrics();
                applyFilter();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                allAnomalies.clear();
                updateMetrics();
                applyFilter();
            });
            return null;
        });
    }

    private void updateMetrics() {
        int total = allAnomalies.size();
        int high = 0, med = 0, low = 0;
        double confSum = 0;

        for (JsonNode a : allAnomalies) {
            String s = a.path("severity").asText("MEDIUM").toUpperCase();
            if (s.contains("HIGH") || s.contains("CRITICAL")) high++;
            else if (s.contains("MED")) med++;
            else low++;
            confSum += a.path("confidenceScore").asDouble(0.75);
        }

        totalCountLabel.setText(String.valueOf(total));
        highCountLabel.setText(String.valueOf(high));
        medCountLabel.setText(String.valueOf(med));
        lowCountLabel.setText(String.valueOf(low));
        avgConfLabel.setText(total > 0 ? Math.round((confSum / total) * 100) + "%" : "0%");
    }

    private void applyFilter() {
        cardsContainer.getChildren().clear();
        String selSev = severityFilter.getValue();
        String search = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";

        int count = 0;
        for (JsonNode anom : allAnomalies) {
            String sev = anom.path("severity").asText("MEDIUM").toUpperCase();
            if (!"ALL SEVERITIES".equals(selSev) && !sev.startsWith(selSev)) {
                continue;
            }

            String title = anom.path("title").asText("");
            String desc = anom.path("description").asText("");
            String entity = anom.path("flaggedEntity").asText("");
            String type = anom.path("anomalyType").asText("");

            if (!search.isEmpty()) {
                String full = (title + " " + desc + " " + entity + " " + type).toLowerCase();
                if (!full.contains(search)) continue;
            }

            cardsContainer.getChildren().add(createAnomalyCard(anom));
            count++;
        }

        if (count == 0) {
            VBox empty = new VBox(10);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40));
            Label l = new Label(allAnomalies.isEmpty()
                    ? "No anomalies recorded for this case yet. Click 'Run Anomaly Detection' above."
                    : "No anomalies match the selected filters.");
            l.setStyle("-fx-text-fill: #8b9bb4; -fx-font-size: 13px;");
            empty.getChildren().add(l);
            cardsContainer.getChildren().add(empty);
        }
    }

    private VBox createAnomalyCard(JsonNode anom) {
        VBox card = new VBox(10);
        card.getStyleClass().add("bg-surface");
        card.setPadding(new Insets(16));

        String title = anom.path("title").asText();
        String desc = anom.path("description").asText();
        String sev = anom.path("severity").asText("MEDIUM").toUpperCase();
        String type = anom.path("anomalyType").asText("OUTLIER");
        double conf = anom.path("confidenceScore").asDouble(0.8);
        String metrics = anom.path("underlyingMetrics").asText("");
        String entity = anom.path("flaggedEntity").asText("");

        // Border highlighting based on severity
        String borderCol = sev.contains("HIGH") ? "#ff1744" : (sev.contains("MED") ? "#ffab00" : "#2979ff");
        card.setStyle(card.getStyle() + "; -fx-border-color: " + borderCol + "; -fx-border-width: 1px 1px 1px 4px; -fx-border-radius: 4px;");

        // Top Row: Title + Severity Badge + Type Badge
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label("⚠ " + title);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #ffffff;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sevBadge = new Label(sev);
        sevBadge.setStyle(getSeverityBadgeStyle(sev));

        Label typeBadge = new Label(type);
        typeBadge.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-text-fill: #b0bec5; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px;");

        topRow.getChildren().addAll(titleLbl, spacer, typeBadge, sevBadge);

        // Description Narrative
        Label descLbl = new Label(desc);
        descLbl.setWrapText(true);
        descLbl.setStyle("-fx-text-fill: #cfd8dc; -fx-font-size: 12px;");

        // Confidence Meter + Math metrics bar
        HBox meterRow = new HBox(12);
        meterRow.setAlignment(Pos.CENTER_LEFT);

        Label confLabel = new Label("Calibrated Confidence: " + Math.round(conf * 100) + "%");
        confLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b9bb4; -fx-font-weight: bold;");

        ProgressBar confBar = new ProgressBar(conf);
        confBar.setPrefWidth(120);
        confBar.setStyle("-fx-accent: " + borderCol + ";");

        meterRow.getChildren().addAll(confLabel, confBar);

        if (metrics != null && !metrics.isBlank()) {
            Label metricLbl = new Label("📊 Math / Rule Basis: " + metrics);
            metricLbl.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-text-fill: #00e5ff;");
            meterRow.getChildren().addAll(new Region(), metricLbl);
        }

        if (entity != null && !entity.isBlank()) {
            Label entityLbl = new Label("🎯 Flagged Entity: " + entity);
            entityLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #e0e6ed; -fx-font-weight: bold;");
            meterRow.getChildren().add(entityLbl);
        }

        card.getChildren().addAll(topRow, descLbl, meterRow);
        return card;
    }

    private String getSeverityBadgeStyle(String sev) {
        if (sev.contains("HIGH") || sev.contains("CRITICAL")) {
            return "-fx-background-color: rgba(255, 23, 68, 0.25); -fx-text-fill: #ff1744; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px;";
        } else if (sev.contains("MED")) {
            return "-fx-background-color: rgba(255, 171, 0, 0.25); -fx-text-fill: #ffab00; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px;";
        } else {
            return "-fx-background-color: rgba(41, 121, 255, 0.25); -fx-text-fill: #2979ff; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px;";
        }
    }
}
