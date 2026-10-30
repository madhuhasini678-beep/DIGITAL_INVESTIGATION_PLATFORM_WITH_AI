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

public class TimelineView extends VBox {

    private final ApiClient apiClient;
    private final Label headerLabel = new Label("Chronological Timeline");
    private final Label subLabel = new Label("Sequenced events extracted via java.time engine with anomaly detection");

    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final TextField searchField = new TextField();
    private final CheckBox anomaliesOnlyCheck = new CheckBox("⚠ Only Out-of-Sequence Anomalies");
    private final Label statsLabel = new Label("0 events");

    private final VBox timelineStream = new VBox(14);
    private final ScrollPane scrollPane = new ScrollPane();
    private final List<JsonNode> allEvents = new ArrayList<>();

    public TimelineView(ApiClient apiClient) {
        this.apiClient = apiClient;

        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("bg-base");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        headerLabel.getStyleClass().add("text-h1");
        subLabel.getStyleClass().add("text-secondary");
        titleBox.getChildren().addAll(headerLabel, subLabel);

        Button refreshBtn = new Button("🔄 Refresh Timeline");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> refresh());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleBox, spacer, refreshBtn);

        // Filter Bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("bg-surface");
        filterBar.setPadding(new Insets(12));

        categoryFilter.getItems().addAll("ALL CATEGORIES", "FINANCIAL", "COMMUNICATION", "PHYSICAL", "SYSTEM_LOG", "FORENSIC");
        categoryFilter.setValue("ALL CATEGORIES");
        categoryFilter.getStyleClass().add("btn-secondary");
        categoryFilter.setOnAction(e -> applyFilter());

        searchField.setPromptText("Search events, titles, entities...");
        searchField.setPrefWidth(260);
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());

        anomaliesOnlyCheck.setStyle("-fx-text-fill: #ffab00; -fx-font-weight: bold; -fx-font-size: 12px;");
        anomaliesOnlyCheck.setOnAction(e -> applyFilter());

        statsLabel.setStyle("-fx-text-fill: #8b9bb4; -fx-font-size: 12px;");

        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);

        filterBar.getChildren().addAll(categoryFilter, searchField, anomaliesOnlyCheck, filterSpacer, statsLabel);

        // Timeline Stream container
        timelineStream.setPadding(new Insets(12));
        timelineStream.setStyle("-fx-background-color: transparent;");

        scrollPane.setContent(timelineStream);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("bg-base");
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, filterBar, scrollPane);

        CaseState.getInstance().activeCaseProperty().addListener((obs, o, n) -> refresh());
        refresh();
    }

    public void refresh() {
        CaseModel current = CaseState.getInstance().getActiveCase();
        if (current == null) {
            headerLabel.setText("Chronological Timeline (No Case Selected)");
            timelineStream.getChildren().setAll(createEmptyState("Please select an active case in the Cases view."));
            statsLabel.setText("0 events");
            return;
        }

        headerLabel.setText("Chronological Timeline: " + current.getTitle());
        apiClient.getTimeline(current.getId()).thenAccept(node -> {
            Platform.runLater(() -> {
                allEvents.clear();
                if (node.isArray()) {
                    for (JsonNode item : node) {
                        allEvents.add(item);
                    }
                }
                applyFilter();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                allEvents.clear();
                applyFilter();
            });
            return null;
        });
    }

    private void applyFilter() {
        timelineStream.getChildren().clear();
        String cat = categoryFilter.getValue();
        String search = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        boolean anomaliesOnly = anomaliesOnlyCheck.isSelected();

        int matchedCount = 0;
        int anomalyCount = 0;

        for (JsonNode item : allEvents) {
            String itemCat = item.path("category").asText("FORENSIC");
            boolean isAnomaly = item.path("anomaly").asBoolean(false) || item.path("isAnomaly").asBoolean(false);
            if (isAnomaly) anomalyCount++;

            if (!"ALL CATEGORIES".equals(cat) && !itemCat.equalsIgnoreCase(cat)) {
                continue;
            }
            if (anomaliesOnly && !isAnomaly) {
                continue;
            }

            String title = item.path("title").asText("");
            String desc = item.path("description").asText("");
            String time = item.path("eventTimestamp").asText("");
            String source = item.path("sourceEvidence").asText("");

            if (!search.isEmpty()) {
                String fullText = (title + " " + desc + " " + time + " " + source + " " + itemCat).toLowerCase();
                if (!fullText.contains(search)) {
                    continue;
                }
            }

            timelineStream.getChildren().add(createTimelineCard(item));
            matchedCount++;
        }

        statsLabel.setText(String.format("Showing %d of %d events (%d flagged anomalies)", matchedCount, allEvents.size(), anomalyCount));

        if (matchedCount == 0) {
            timelineStream.getChildren().add(createEmptyState("No events match the selected criteria or no timeline recorded yet."));
        }
    }

    private HBox createTimelineCard(JsonNode item) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.TOP_LEFT);

        String time = item.path("eventTimestamp").asText();
        String title = item.path("title").asText();
        String desc = item.path("description").asText();
        String category = item.path("category").asText("FORENSIC");
        String source = item.path("sourceEvidence").asText("");
        boolean isAnomaly = item.path("anomaly").asBoolean(false) || item.path("isAnomaly").asBoolean(false);

        // Left timestamp column
        VBox timeCol = new VBox(2);
        timeCol.setPrefWidth(160);
        timeCol.setAlignment(Pos.TOP_RIGHT);
        Label timeLbl = new Label(time.replace("T", " "));
        timeLbl.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-weight: bold; -fx-text-fill: #00e5ff; -fx-font-size: 11px;");
        timeCol.getChildren().add(timeLbl);

        // Center spine node
        VBox spineCol = new VBox();
        spineCol.setAlignment(Pos.TOP_CENTER);
        spineCol.setPrefWidth(24);

        Label marker = new Label(isAnomaly ? "⚠" : "●");
        if (isAnomaly) {
            marker.setStyle("-fx-font-size: 16px; -fx-text-fill: #ff1744; -fx-font-weight: bold;");
        } else {
            marker.setStyle("-fx-font-size: 14px; -fx-text-fill: #00e5ff;");
        }
        Region line = new Region();
        line.setPrefWidth(2);
        line.setPrefHeight(60);
        line.setStyle("-fx-background-color: " + (isAnomaly ? "#ff1744;" : "#26354f;"));
        VBox.setVgrow(line, Priority.ALWAYS);
        spineCol.getChildren().addAll(marker, line);

        // Right Content Card
        VBox card = new VBox(8);
        card.getStyleClass().add("bg-surface");
        card.setPadding(new Insets(14));
        card.setStyle(card.getStyle() + (isAnomaly ? "; -fx-border-color: #ff1744; -fx-border-width: 1.5px;" : ""));
        HBox.setHgrow(card, Priority.ALWAYS);

        // Card Top: Title + Category Pill + Anomaly Pill
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Region cardSpacer = new Region();
        HBox.setHgrow(cardSpacer, Priority.ALWAYS);

        Label catBadge = new Label(category);
        catBadge.setStyle(getCategoryBadgeStyle(category));

        topRow.getChildren().addAll(titleLbl, cardSpacer, catBadge);

        if (isAnomaly) {
            Label anomBadge = new Label("OUT-OF-SEQUENCE");
            anomBadge.setStyle("-fx-background-color: rgba(255, 23, 68, 0.2); -fx-text-fill: #ff1744; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4;");
            topRow.getChildren().add(anomBadge);
        }

        // Anomaly Alert Banner if anomaly
        if (isAnomaly) {
            Label alertBanner = new Label("⚠ Chronological Discrepancy: Event occurs out-of-order relative to adjacent records.");
            alertBanner.setStyle("-fx-background-color: rgba(255, 23, 68, 0.15); -fx-text-fill: #ff5252; -fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4;");
            card.getChildren().add(alertBanner);
        }

        // Description
        Label descLbl = new Label(desc);
        descLbl.setWrapText(true);
        descLbl.setStyle("-fx-text-fill: #b0bec5; -fx-font-size: 12px;");

        // Footer with Source reference
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);
        if (source != null && !source.isBlank()) {
            Label srcLbl = new Label("📄 Source: " + source);
            srcLbl.setStyle("-fx-text-fill: #8b9bb4; -fx-font-size: 11px; -fx-font-family: 'Consolas', monospace;");
            footer.getChildren().add(srcLbl);
        }

        card.getChildren().addAll(topRow, descLbl, footer);

        row.getChildren().addAll(timeCol, spineCol, card);
        return row;
    }

    private String getCategoryBadgeStyle(String cat) {
        return switch (cat.toUpperCase()) {
            case "FINANCIAL" -> "-fx-background-color: rgba(0, 230, 118, 0.2); -fx-text-fill: #00e676; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold;";
            case "COMMUNICATION" -> "-fx-background-color: rgba(41, 121, 255, 0.2); -fx-text-fill: #2979ff; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold;";
            case "PHYSICAL" -> "-fx-background-color: rgba(255, 171, 0, 0.2); -fx-text-fill: #ffab00; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold;";
            case "SYSTEM_LOG" -> "-fx-background-color: rgba(224, 64, 251, 0.2); -fx-text-fill: #e040fb; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold;";
            default -> "-fx-background-color: rgba(0, 229, 255, 0.2); -fx-text-fill: #00e5ff; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold;";
        };
    }

    private VBox createEmptyState(String message) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        Label lbl = new Label(message);
        lbl.setStyle("-fx-text-fill: #8b9bb4; -fx-font-size: 14px;");
        box.getChildren().add(lbl);
        return box;
    }
}
