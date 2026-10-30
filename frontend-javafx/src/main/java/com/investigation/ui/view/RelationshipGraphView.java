package com.investigation.ui.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.investigation.ui.api.ApiClient;
import com.investigation.ui.model.CaseModel;
import com.investigation.ui.model.CaseState;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.*;

public class RelationshipGraphView extends VBox {

    private final ApiClient apiClient;
    private final Label headerLabel = new Label("Relationship Graph");
    private final Label subLabel = new Label("Interactive entity connection mapping: persons, accounts, organizations, locations");

    // Canvas / Graph container
    private final Pane graphPane = new Pane();
    private final Group zoomGroup = new Group(graphPane);
    private double zoomFactor = 1.0;

    // Inspector Drawer
    private final VBox inspectorBox = new VBox(12);
    private final Label inspectorTitle = new Label("Entity Inspector");
    private final Label selectedNodeName = new Label("Select an entity node to inspect");
    private final Label selectedNodeType = new Label("");
    private final VBox connectionsList = new VBox(6);

    // Data structures for interactive graph
    private final Map<String, NodeVisual> visualNodes = new HashMap<>();
    private final List<EdgeVisual> visualEdges = new ArrayList<>();

    public RelationshipGraphView(ApiClient apiClient) {
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

        Button zoomInBtn = new Button("🔍+");
        zoomInBtn.getStyleClass().add("btn-secondary");
        zoomInBtn.setOnAction(e -> applyZoom(1.15));

        Button zoomOutBtn = new Button("🔍-");
        zoomOutBtn.getStyleClass().add("btn-secondary");
        zoomOutBtn.setOnAction(e -> applyZoom(0.85));

        Button resetLayoutBtn = new Button("⟲ Reset Layout");
        resetLayoutBtn.getStyleClass().add("btn-secondary");
        resetLayoutBtn.setOnAction(e -> arrangeCircularLayout());

        Button refreshBtn = new Button("🔄 Refresh Graph");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> refresh());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleBox, spacer, zoomInBtn, zoomOutBtn, resetLayoutBtn, refreshBtn);

        // Legend bar
        HBox legendBar = createLegendBar();

        // Main Workspace: Graph Pane (Center) + Inspector (Right)
        HBox workArea = new HBox(16);
        VBox.setVgrow(workArea, Priority.ALWAYS);

        // Graph area wrapped in styled container
        StackPane graphContainer = new StackPane();
        graphContainer.getStyleClass().add("bg-surface");
        graphContainer.setStyle(graphContainer.getStyle() + "; -fx-border-color: #26354f; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-overflow: hidden;");
        HBox.setHgrow(graphContainer, Priority.ALWAYS);

        graphPane.setPrefSize(1200, 800);
        graphPane.setStyle("-fx-background-color: #0b101b;");
        graphContainer.getChildren().add(zoomGroup);

        // Pan capability on graphContainer
        setupPanning(graphContainer);

        // Inspector Sidebar
        setupInspector();
        workArea.getChildren().addAll(graphContainer, inspectorBox);

        getChildren().addAll(header, legendBar, workArea);

        CaseState.getInstance().activeCaseProperty().addListener((obs, o, n) -> refresh());
        refresh();
    }

    private HBox createLegendBar() {
        HBox bar = new HBox(16);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 12, 6, 12));
        bar.setStyle("-fx-background-color: #0d131f; -fx-background-radius: 6px; -fx-border-color: #1a273b; -fx-border-radius: 6px;");

        Label title = new Label("ENTITY TYPES:");
        title.setStyle("-fx-text-fill: #8b9bb4; -fx-font-weight: bold; -fx-font-size: 11px;");

        bar.getChildren().addAll(
                title,
                createLegendItem("PERSON", "#00e5ff"),
                createLegendItem("ORGANIZATION", "#e040fb"),
                createLegendItem("ACCOUNT", "#00e676"),
                createLegendItem("LOCATION", "#ffab00"),
                createLegendItem("VEHICLE / ASSET", "#ff9100")
        );
        return bar;
    }

    private HBox createLegendItem(String name, String hexColor) {
        Circle c = new Circle(5, Color.web(hexColor));
        Label l = new Label(name);
        l.setStyle("-fx-text-fill: #e0e6ed; -fx-font-size: 11px;");
        HBox item = new HBox(6, c, l);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private void setupInspector() {
        inspectorBox.setPrefWidth(280);
        inspectorBox.setMinWidth(260);
        inspectorBox.getStyleClass().add("bg-surface");
        inspectorBox.setPadding(new Insets(16));

        inspectorTitle.getStyleClass().add("text-h2");
        selectedNodeName.setWrapText(true);
        selectedNodeName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #ffffff;");

        selectedNodeType.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        Label connsHeader = new Label("CONNECTED RELATIONSHIPS");
        connsHeader.setStyle("-fx-text-fill: #8b9bb4; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 8 0 0 0;");

        ScrollPane connsScroll = new ScrollPane(connectionsList);
        connsScroll.setFitToWidth(true);
        connsScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(connsScroll, Priority.ALWAYS);

        inspectorBox.getChildren().addAll(inspectorTitle, selectedNodeName, selectedNodeType, connsHeader, connsScroll);
    }

    private void applyZoom(double delta) {
        zoomFactor *= delta;
        zoomFactor = Math.max(0.4, Math.min(zoomFactor, 2.5));
        zoomGroup.setScaleX(zoomFactor);
        zoomGroup.setScaleY(zoomFactor);
    }

    private double dragStartX, dragStartY;
    private void setupPanning(StackPane pane) {
        pane.setOnMousePressed(e -> {
            dragStartX = e.getX() - graphPane.getTranslateX();
            dragStartY = e.getY() - graphPane.getTranslateY();
        });
        pane.setOnMouseDragged(e -> {
            graphPane.setTranslateX(e.getX() - dragStartX);
            graphPane.setTranslateY(e.getY() - dragStartY);
        });
    }

    public void refresh() {
        CaseModel current = CaseState.getInstance().getActiveCase();
        if (current == null) {
            headerLabel.setText("Relationship Graph (No Case Selected)");
            graphPane.getChildren().clear();
            visualNodes.clear();
            visualEdges.clear();
            selectedNodeName.setText("Please select an active case in the Cases view.");
            selectedNodeType.setText("");
            connectionsList.getChildren().clear();
            return;
        }

        headerLabel.setText("Relationship Graph: " + current.getTitle());
        apiClient.getGraph(current.getId()).thenAccept(node -> {
            Platform.runLater(() -> renderGraph(node));
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                graphPane.getChildren().clear();
                selectedNodeName.setText("No graph data available.");
            });
            return null;
        });
    }

    private void renderGraph(JsonNode graphData) {
        graphPane.getChildren().clear();
        visualNodes.clear();
        visualEdges.clear();

        JsonNode nodesJson = graphData.path("nodes");
        JsonNode edgesJson = graphData.path("edges");

        if (!nodesJson.isArray() || nodesJson.isEmpty()) {
            Text emptyText = new Text("No entity relationships recorded for this case yet.");
            emptyText.setFill(Color.web("#8b9bb4"));
            emptyText.setFont(Font.font(14));
            emptyText.setX(40);
            emptyText.setY(50);
            graphPane.getChildren().add(emptyText);
            return;
        }

        // 1. Create Nodes
        for (JsonNode n : nodesJson) {
            String id = n.path("id").asText();
            String label = n.path("label").asText();
            String type = n.path("type").asText("OTHER");
            NodeVisual nv = new NodeVisual(id, label, type);
            visualNodes.put(id, nv);
        }

        // 2. Create Edges
        if (edgesJson.isArray()) {
            for (JsonNode e : edgesJson) {
                String sourceId = e.path("sourceNodeId").asText();
                String targetId = e.path("targetNodeId").asText();
                String sourceLabel = e.path("sourceLabel").asText("");
                String targetLabel = e.path("targetLabel").asText("");
                String rel = e.path("relationship").asText("ASSOCIATED_WITH");
                double weight = e.path("weight").asDouble(1.0);

                NodeVisual sourceNode = visualNodes.get(sourceId);
                NodeVisual targetNode = visualNodes.get(targetId);

                // Fallback matching by label if ID lookup misses
                if (sourceNode == null) {
                    sourceNode = visualNodes.values().stream().filter(v -> v.label.equalsIgnoreCase(sourceLabel)).findFirst().orElse(null);
                }
                if (targetNode == null) {
                    targetNode = visualNodes.values().stream().filter(v -> v.label.equalsIgnoreCase(targetLabel)).findFirst().orElse(null);
                }

                if (sourceNode != null && targetNode != null) {
                    EdgeVisual edge = new EdgeVisual(sourceNode, targetNode, rel, weight);
                    visualEdges.add(edge);
                    sourceNode.adjacentEdges.add(edge);
                    targetNode.adjacentEdges.add(edge);
                }
            }
        }

        // 3. Arrange Layout (Circular Layout around center)
        arrangeCircularLayout();

        // 4. Add Edges to Pane first so lines stay under nodes
        for (EdgeVisual edge : visualEdges) {
            graphPane.getChildren().addAll(edge.line, edge.labelNode);
        }

        // 5. Add Nodes to Pane
        for (NodeVisual node : visualNodes.values()) {
            graphPane.getChildren().addAll(node.container);
        }

        // Auto select first node
        if (!visualNodes.isEmpty()) {
            selectNode(visualNodes.values().iterator().next());
        }
    }

    private void arrangeCircularLayout() {
        double centerX = 420;
        double centerY = 320;
        double radius = Math.min(centerX, centerY) - 70;

        int count = visualNodes.size();
        int i = 0;
        for (NodeVisual node : visualNodes.values()) {
            double angle = 2 * Math.PI * i / Math.max(1, count);
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            node.setPosition(x, y);
            i++;
        }

        for (EdgeVisual edge : visualEdges) {
            edge.updatePositions();
        }
    }

    private void selectNode(NodeVisual node) {
        for (NodeVisual n : visualNodes.values()) {
            n.setSelected(false);
        }
        node.setSelected(true);

        selectedNodeName.setText(node.label);
        selectedNodeType.setText(node.type);
        selectedNodeType.setStyle("-fx-text-fill: " + node.colorHex + "; -fx-font-weight: bold; -fx-font-size: 11px;");

        connectionsList.getChildren().clear();
        for (EdgeVisual edge : node.adjacentEdges) {
            boolean isSource = edge.source == node;
            NodeVisual other = isSource ? edge.target : edge.source;
            String arrow = isSource ? "──▶" : "◀──";

            VBox card = new VBox(2);
            card.setPadding(new Insets(6, 8, 6, 8));
            card.setStyle("-fx-background-color: #121c2e; -fx-background-radius: 4px; -fx-border-color: #1e2c44; -fx-border-radius: 4px;");

            Label relLbl = new Label(edge.relationship + " " + arrow);
            relLbl.setStyle("-fx-font-family: 'Consolas', monospace; -fx-text-fill: #00e5ff; -fx-font-size: 10px;");

            Label otherLbl = new Label(other.label + " (" + other.type + ")");
            otherLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-size: 11px;");

            card.getChildren().addAll(relLbl, otherLbl);
            connectionsList.getChildren().add(card);
        }

        if (node.adjacentEdges.isEmpty()) {
            Label noConns = new Label("No connected relationships for this entity.");
            noConns.setStyle("-fx-text-fill: #8b9bb4; -fx-font-size: 11px;");
            connectionsList.getChildren().add(noConns);
        }
    }

    // Inner class representing visual entity node
    private class NodeVisual {
        final String id;
        final String label;
        final String type;
        final String colorHex;
        final StackPane container = new StackPane();
        final Circle circle;
        final Circle glowCircle;
        final Text text;
        final List<EdgeVisual> adjacentEdges = new ArrayList<>();

        double posX, posY;
        double mouseAnchorX, mouseAnchorY;

        NodeVisual(String id, String label, String type) {
            this.id = id;
            this.label = label;
            this.type = type;
            this.colorHex = getNodeColor(type);

            glowCircle = new Circle(28);
            glowCircle.setFill(Color.TRANSPARENT);
            glowCircle.setStroke(Color.web("#00e5ff"));
            glowCircle.setStrokeWidth(2.5);
            glowCircle.setVisible(false);

            circle = new Circle(22);
            circle.setFill(Color.web("#0e1726"));
            circle.setStroke(Color.web(colorHex));
            circle.setStrokeWidth(2.5);

            text = new Text(truncate(label, 12));
            text.setFill(Color.WHITE);
            text.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));

            container.getChildren().addAll(glowCircle, circle, text);
            container.setCursor(javafx.scene.Cursor.HAND);

            // Dragging interactions
            container.setOnMousePressed(e -> {
                mouseAnchorX = e.getX();
                mouseAnchorY = e.getY();
                selectNode(this);
                e.consume();
            });

            container.setOnMouseDragged(e -> {
                double newX = container.getLayoutX() + e.getX() - mouseAnchorX;
                double newY = container.getLayoutY() + e.getY() - mouseAnchorY;
                setPosition(newX, newY);
                for (EdgeVisual edge : adjacentEdges) {
                    edge.updatePositions();
                }
                e.consume();
            });
        }

        void setPosition(double x, double y) {
            this.posX = x;
            this.posY = y;
            container.setLayoutX(x - 22);
            container.setLayoutY(y - 22);
        }

        void setSelected(boolean selected) {
            glowCircle.setVisible(selected);
            circle.setStroke(selected ? Color.web("#00e5ff") : Color.web(colorHex));
            circle.setStrokeWidth(selected ? 3.5 : 2.5);
        }

        private String truncate(String val, int max) {
            if (val == null) return "";
            return val.length() > max ? val.substring(0, max - 2) + ".." : val;
        }

        private String getNodeColor(String t) {
            return switch (t.toUpperCase()) {
                case "PERSON" -> "#00e5ff";
                case "ORGANIZATION", "COMPANY" -> "#e040fb";
                case "ACCOUNT", "BANK" -> "#00e676";
                case "LOCATION", "ADDRESS" -> "#ffab00";
                case "VEHICLE", "TRANSACTION" -> "#ff9100";
                default -> "#90a4ae";
            };
        }
    }

    // Inner class representing visual connection edge
    private class EdgeVisual {
        final NodeVisual source;
        final NodeVisual target;
        final String relationship;
        final double weight;
        final Line line = new Line();
        final Label labelNode = new Label();

        EdgeVisual(NodeVisual source, NodeVisual target, String relationship, double weight) {
            this.source = source;
            this.target = target;
            this.relationship = relationship;
            this.weight = weight;

            line.setStroke(Color.web("#2a3b5c"));
            line.setStrokeWidth(Math.max(1.5, Math.min(weight * 1.5, 4.0)));

            labelNode.setText(relationship);
            labelNode.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 9px; -fx-text-fill: #607d8b; -fx-background-color: #080d17; -fx-padding: 1 4; -fx-background-radius: 3;");
        }

        void updatePositions() {
            line.setStartX(source.posX);
            line.setStartY(source.posY);
            line.setEndX(target.posX);
            line.setEndY(target.posY);

            double midX = (source.posX + target.posX) / 2;
            double midY = (source.posY + target.posY) / 2;
            labelNode.setLayoutX(midX - 25);
            labelNode.setLayoutY(midY - 8);
        }
    }
}
