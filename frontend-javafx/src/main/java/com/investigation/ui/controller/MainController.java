package com.investigation.ui.controller;

import com.investigation.ui.api.ApiClient;
import com.investigation.ui.model.CaseModel;
import com.investigation.ui.model.CaseState;
import com.investigation.ui.view.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainController {

    private final ApiClient apiClient = new ApiClient();
    private final BorderPane rootLayout = new BorderPane();
    private final StackPane contentArea = new StackPane();

    private final ToggleGroup navGroup = new ToggleGroup();
    private final Map<String, ToggleButton> navButtons = new LinkedHashMap<>();
    private final Map<String, Node> viewCache = new LinkedHashMap<>();

    private final Label activeCasePill = new Label("NO ACTIVE CASE");
    private final Label statusIndicator = new Label("● API CONNECTING");

    public MainController() {
        buildShell();
        bindState();
        checkApiStatus();
    }

    public BorderPane getRootLayout() {
        return rootLayout;
    }

    private void buildShell() {
        rootLayout.getStyleClass().add("bg-base");

        // 1. Top Header Bar
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-header");

        Label brand = new Label("DIGITAL INVESTIGATION PLATFORM");
        brand.getStyleClass().add("header-title");

        Label slash = new Label("//");
        slash.getStyleClass().add("text-cyan");

        Label sub = new Label("FORENSIC INTELLIGENCE SUITE");
        sub.getStyleClass().add("nav-brand-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        activeCasePill.getStyleClass().add("active-case-badge");

        statusIndicator.getStyleClass().add("badge-active");
        statusIndicator.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px;");

        topBar.getChildren().addAll(brand, slash, sub, spacer, activeCasePill, statusIndicator);
        rootLayout.setTop(topBar);

        // 2. Left Navigation Rail (7 Permanent Sections)
        VBox navRail = new VBox(6);
        navRail.getStyleClass().add("nav-rail");
        navRail.setPrefWidth(220);
        navRail.setPadding(new Insets(16, 8, 16, 8));

        Label navSectionLabel = new Label("NAVIGATION");
        navSectionLabel.getStyleClass().add("nav-brand-subtitle");
        navSectionLabel.setPadding(new Insets(4, 12, 8, 12));
        navRail.getChildren().add(navSectionLabel);

        // The 7 Mandatory Sections
        addNavButton(navRail, "Dashboard", "📊  1. Dashboard");
        addNavButton(navRail, "Cases", "📁  2. Cases");
        addNavButton(navRail, "Evidence", "📦  3. Evidence");
        addNavButton(navRail, "Timeline", "⏱  4. Timeline");
        addNavButton(navRail, "Relationship Graph", "🕸  5. Relationship Graph");
        addNavButton(navRail, "Anomalies", "⚠  6. Anomalies");
        addNavButton(navRail, "AI Summary", "🧠  7. AI Summary");

        rootLayout.setLeft(navRail);

        // 3. Center Content Area
        contentArea.getStyleClass().add("bg-base");
        rootLayout.setCenter(contentArea);

        // Select Dashboard initially
        switchSection("Dashboard");
    }

    private void addNavButton(VBox rail, String key, String labelText) {
        ToggleButton btn = new ToggleButton(labelText);
        btn.setToggleGroup(navGroup);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);

        btn.setOnAction(e -> {
            if (!btn.isSelected()) {
                btn.setSelected(true);
            }
            switchSection(key);
        });

        navButtons.put(key, btn);
        rail.getChildren().add(btn);
    }

    public void switchSection(String sectionKey) {
        ToggleButton btn = navButtons.get(sectionKey);
        if (btn != null && !btn.isSelected()) {
            btn.setSelected(true);
        }

        // Apply active class styling
        navButtons.forEach((k, b) -> {
            if (k.equals(sectionKey)) {
                if (!b.getStyleClass().contains("nav-button-active")) {
                    b.getStyleClass().add("nav-button-active");
                }
            } else {
                b.getStyleClass().remove("nav-button-active");
            }
        });

        Node view = viewCache.computeIfAbsent(sectionKey, this::createView);
        contentArea.getChildren().setAll(view);

        // Trigger refresh if view supports it
        if (view instanceof DashboardView dv) dv.refresh();
        if (view instanceof CasesView cv) cv.refresh();
        if (view instanceof EvidenceView ev) ev.refresh();
        if (view instanceof TimelineView tv) tv.refresh();
        if (view instanceof RelationshipGraphView gv) gv.refresh();
        if (view instanceof AnomaliesView av) av.refresh();
        if (view instanceof AiSummaryView sv) sv.refresh();
    }

    private Node createView(String sectionKey) {
        return switch (sectionKey) {
            case "Dashboard" -> new DashboardView(apiClient, this::switchSection);
            case "Cases" -> new CasesView(apiClient);
            case "Evidence" -> new EvidenceView(apiClient);
            case "Timeline" -> new TimelineView(apiClient);
            case "Relationship Graph" -> new RelationshipGraphView(apiClient);
            case "Anomalies" -> new AnomaliesView(apiClient);
            case "AI Summary" -> new AiSummaryView(apiClient);
            default -> new Label("Unknown View: " + sectionKey);
        };
    }

    private void bindState() {
        CaseState.getInstance().activeCaseProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                if (newVal != null) {
                    activeCasePill.setText(newVal.getCaseNumber() + " - " + newVal.getTitle());
                } else {
                    activeCasePill.setText("NO ACTIVE CASE");
                }
            });
        });
    }

    private void checkApiStatus() {
        apiClient.checkBackendHealth().thenAccept(online -> {
            Platform.runLater(() -> {
                if (online) {
                    statusIndicator.setText("● API ONLINE");
                    statusIndicator.setStyle("-fx-background-color: #064e3b; -fx-text-fill: #34d399; -fx-font-size: 11px; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-font-weight: bold;");
                } else {
                    statusIndicator.setText("○ API OFFLINE");
                    statusIndicator.setStyle("-fx-background-color: #451a03; -fx-text-fill: #fbbf24; -fx-font-size: 11px; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-font-weight: bold;");
                }
            });
        });
    }
}
