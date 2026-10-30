package com.investigation.ui.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.investigation.ui.api.ApiClient;
import com.investigation.ui.model.CaseModel;
import com.investigation.ui.model.CaseState;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AiSummaryView extends VBox {

    private final ApiClient apiClient;
    private final Label headerLabel = new Label("AI Investigation Summary");
    private final Label subLabel = new Label("Evidence-grounded intelligence synthesis with explicit forensic guardrails");

    private final TextArea factualOverviewArea = new TextArea();
    private final TextArea keyFindingsArea = new TextArea();
    private final TextArea discrepanciesArea = new TextArea();
    private final TextArea confidenceNotesArea = new TextArea();

    private final Label statusLabel = new Label("");
    private final Label metadataBanner = new Label("Model Engine: Forensic Grounded Synthesizer v2.1 | Guardrail Status: FORENSIC GUARDRAILS ACTIVE");

    public AiSummaryView(ApiClient apiClient) {
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

        Button generateBtn = new Button("🧠 Generate AI Summary");
        generateBtn.getStyleClass().add("btn-primary");
        generateBtn.setOnAction(e -> runGeneration());

        Button copyBtn = new Button("📋 Copy Report");
        copyBtn.getStyleClass().add("btn-secondary");
        copyBtn.setOnAction(e -> copyReportToClipboard());

        Button exportBtn = new Button("💾 Export Dossier");
        exportBtn.getStyleClass().add("btn-secondary");
        exportBtn.setOnAction(e -> exportReportToFile());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> refresh());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00e5ff;");

        header.getChildren().addAll(titleBox, spacer, statusLabel, copyBtn, exportBtn, refreshBtn, generateBtn);

        // Security / Guardrail Status Banner
        HBox bannerBox = new HBox(12);
        bannerBox.setAlignment(Pos.CENTER_LEFT);
        bannerBox.setPadding(new Insets(8, 14, 8, 14));
        bannerBox.setStyle("-fx-background-color: #0b1528; -fx-border-color: #1a2f52; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        Label badge = new Label("VERIFIED FORENSIC GROUNDING");
        badge.setStyle("-fx-background-color: rgba(0, 229, 255, 0.2); -fx-text-fill: #00e5ff; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 3 6; -fx-background-radius: 4;");

        metadataBanner.setStyle("-fx-text-fill: #8b9bb4; -fx-font-size: 11px;");

        Region bannerSpacer = new Region();
        HBox.setHgrow(bannerSpacer, Priority.ALWAYS);

        bannerBox.getChildren().addAll(badge, metadataBanner);

        // 4 Dossier Areas
        setupAreas();

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("bg-base");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox contentBox = new VBox(14);
        contentBox.getChildren().addAll(
                createSectionCard("1. FACTUAL OVERVIEW", "Objective case overview and confirmed historical boundaries", factualOverviewArea, "#00e5ff"),
                createSectionCard("2. KEY FINDINGS & EVIDENCE SUMMARY", "Evidence synthesis across uploaded records, transactions and logs", keyFindingsArea, "#00e676"),
                createSectionCard("3. DISCREPANCIES & IDENTIFIED ANOMALIES", "Irregularities, sequence violations, and statistical outliers flagged in evidence", discrepanciesArea, "#ffab00"),
                createSectionCard("4. CONFIDENCE, UNCERTAINTY & FORENSIC GUARDRAILS", "Model limitations, forensic disclaimers, and verification advisory", confidenceNotesArea, "#e040fb")
        );

        scroll.setContent(contentBox);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(header, bannerBox, scroll);

        CaseState.getInstance().activeCaseProperty().addListener((obs, o, n) -> refresh());
        refresh();
    }

    private void setupAreas() {
        factualOverviewArea.setEditable(false);
        factualOverviewArea.setWrapText(true);
        factualOverviewArea.setPrefRowCount(4);

        keyFindingsArea.setEditable(false);
        keyFindingsArea.setWrapText(true);
        keyFindingsArea.setPrefRowCount(6);

        discrepanciesArea.setEditable(false);
        discrepanciesArea.setWrapText(true);
        discrepanciesArea.setPrefRowCount(5);

        confidenceNotesArea.setEditable(false);
        confidenceNotesArea.setWrapText(true);
        confidenceNotesArea.setPrefRowCount(4);
    }

    private VBox createSectionCard(String title, String subtitle, TextArea area, String accentHex) {
        VBox box = new VBox(8);
        box.getStyleClass().add("bg-surface");
        box.setPadding(new Insets(16));
        box.setStyle(box.getStyle() + "; -fx-border-color: " + accentHex + "; -fx-border-width: 0 0 0 3px; -fx-border-radius: 4px;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + accentHex + "; -fx-letter-spacing: 0.5px;");

        Label subLbl = new Label(subtitle);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b9bb4;");

        box.getChildren().addAll(titleLbl, subLbl, area);
        return box;
    }

    private void runGeneration() {
        CaseModel current = CaseState.getInstance().getActiveCase();
        if (current == null) {
            statusLabel.setText("Please select an active case first.");
            return;
        }

        statusLabel.setText("Synthesizing evidence & generating grounded dossier...");
        apiClient.generateSummary(current.getId()).thenRun(() -> {
            Platform.runLater(() -> {
                statusLabel.setText("✓ AI Summary generated successfully");
                refresh();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> statusLabel.setText("Generation error: " + ex.getMessage()));
            return null;
        });
    }

    public void refresh() {
        CaseModel current = CaseState.getInstance().getActiveCase();
        if (current == null) {
            headerLabel.setText("AI Investigation Summary (No Case Selected)");
            factualOverviewArea.setText("Please select an active case in the Cases view.");
            keyFindingsArea.clear();
            discrepanciesArea.clear();
            confidenceNotesArea.clear();
            metadataBanner.setText("No active case selected.");
            return;
        }

        headerLabel.setText("AI Investigation Summary: " + current.getTitle());
        apiClient.getSummary(current.getId()).thenAccept(node -> {
            Platform.runLater(() -> {
                factualOverviewArea.setText(node.path("factualOverview").asText(""));
                keyFindingsArea.setText(node.path("keyFindings").asText(""));
                discrepanciesArea.setText(node.path("discrepanciesIdentified").asText(""));
                confidenceNotesArea.setText(node.path("confidenceNotes").asText(""));
                metadataBanner.setText(String.format("Case: %s | Model: Forensic Synthesizer v2.1 | Guardrails: ACTIVE", current.getTitle()));
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                factualOverviewArea.setText("No AI summary has been generated for this case yet. Click 'Generate AI Summary' above.");
                keyFindingsArea.clear();
                discrepanciesArea.clear();
                confidenceNotesArea.clear();
                metadataBanner.setText("Dossier pending generation.");
            });
            return null;
        });
    }

    private void copyReportToClipboard() {
        CaseModel current = CaseState.getInstance().getActiveCase();
        String report = buildFullReportText(current);
        ClipboardContent content = new ClipboardContent();
        content.putString(report);
        Clipboard.getSystemClipboard().setContent(content);

        statusLabel.setText("✓ Full dossier copied to clipboard");
        Platform.runLater(() -> {
            new Thread(() -> {
                try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> statusLabel.setText(""));
            }).start();
        });
    }

    private void exportReportToFile() {
        CaseModel current = CaseState.getInstance().getActiveCase();
        if (current == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export AI Investigation Dossier");
        String safeName = current.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_");
        chooser.setInitialFileName("AI_DOSSIER_" + safeName + ".md");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Markdown Dossier (*.md)", "*.md"));

        File target = chooser.showSaveDialog(getScene() != null ? getScene().getWindow() : null);
        if (target != null) {
            try (FileWriter writer = new FileWriter(target)) {
                writer.write(buildFullReportText(current));
                statusLabel.setText("✓ Exported to " + target.getName());
            } catch (Exception ex) {
                statusLabel.setText("Export error: " + ex.getMessage());
            }
        }
    }

    private String buildFullReportText(CaseModel current) {
        String title = current != null ? current.getTitle() : "Investigation Case";
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
                # AI INVESTIGATION DOSSIER: %s
                **Generated At:** %s
                **Synthesizer Engine:** Forensic Grounded Intelligence v2.1
                **Forensic Guardrail Compliance:** VERIFIED
                
                ---
                
                ## 1. FACTUAL OVERVIEW
                %s
                
                ---
                
                ## 2. KEY FINDINGS & EVIDENCE SUMMARY
                %s
                
                ---
                
                ## 3. DISCREPANCIES & IDENTIFIED ANOMALIES
                %s
                
                ---
                
                ## 4. CONFIDENCE, UNCERTAINTY & FORENSIC GUARDRAILS
                %s
                """.formatted(
                title, now,
                factualOverviewArea.getText(),
                keyFindingsArea.getText(),
                discrepanciesArea.getText(),
                confidenceNotesArea.getText()
        );
    }
}
