package com.investigation.ui.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.investigation.ui.api.ApiClient;
import com.investigation.ui.model.EvidenceModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;

import java.util.ArrayList;
import java.util.List;

public class EvidencePreviewDialog extends Dialog<Void> {

    private final ApiClient apiClient;
    private final EvidenceModel evidence;
    private int currentPage = 0;
    private int totalPages = 1;
    private final int pageSize = 50;

    private final TableView<ObservableList<String>> previewTable = new TableView<>();
    private final Label pageInfoLabel = new Label("Page 1 of 1");
    private final Button prevBtn = new Button("◀ Previous");
    private final Button nextBtn = new Button("Next ▶");

    public EvidencePreviewDialog(ApiClient apiClient, EvidenceModel evidence) {
        this.apiClient = apiClient;
        this.evidence = evidence;

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Forensic Dataset Preview: " + evidence.getFileName());

        DialogPane pane = getDialogPane();
        pane.getStyleClass().add("bg-base");
        pane.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
        pane.setPrefSize(950, 650);

        VBox content = new VBox(14);
        content.setPadding(new Insets(16));

        // Header info
        HBox infoBox = new HBox(12);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.getStyleClass().add("bg-surface");
        infoBox.setPadding(new Insets(12));

        VBox metaBox = new VBox(4);
        Label titleLbl = new Label("📄 " + evidence.getFileName() + " (" + evidence.getRecordCount() + " records)");
        titleLbl.getStyleClass().add("text-h2");
        Label hashLbl = new Label("SHA-256: " + evidence.getSha256Hash());
        hashLbl.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-text-fill: #00e5ff; -fx-font-size: 11px;");
        metaBox.getChildren().addAll(titleLbl, hashLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        prevBtn.getStyleClass().add("btn-secondary");
        nextBtn.getStyleClass().add("btn-secondary");
        prevBtn.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadPage();
            }
        });
        nextBtn.setOnAction(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadPage();
            }
        });

        HBox pagControls = new HBox(8, prevBtn, pageInfoLabel, nextBtn);
        pagControls.setAlignment(Pos.CENTER_RIGHT);

        infoBox.getChildren().addAll(metaBox, spacer, pagControls);

        // Table setup
        previewTable.getStyleClass().add("table-view");
        previewTable.setPlaceholder(new Label("Loading dataset rows..."));
        VBox.setVgrow(previewTable, Priority.ALWAYS);

        content.getChildren().addAll(infoBox, previewTable);
        pane.setContent(content);
        pane.getButtonTypes().add(ButtonType.CLOSE);

        loadPage();
    }

    private void loadPage() {
        apiClient.getEvidencePreview(evidence.getId(), currentPage, pageSize).thenAccept(data -> {
            Platform.runLater(() -> {
                JsonNode headersNode = data.path("headers");
                JsonNode rowsNode = data.path("rows");
                int totalRows = data.path("totalRows").asInt(evidence.getRecordCount());
                totalPages = Math.max(1, (int) Math.ceil((double) totalRows / pageSize));

                pageInfoLabel.setText(String.format("Page %d of %d (%d total rows)", currentPage + 1, totalPages, totalRows));
                prevBtn.setDisable(currentPage <= 0);
                nextBtn.setDisable(currentPage >= totalPages - 1);

                // Rebuild columns if first load or changed
                if (previewTable.getColumns().isEmpty() && headersNode.isArray()) {
                    List<String> headers = new ArrayList<>();
                    for (int i = 0; i < headersNode.size(); i++) {
                        final int colIndex = i;
                        String colName = headersNode.get(i).asText();
                        headers.add(colName);
                        TableColumn<ObservableList<String>, String> col = new TableColumn<>(colName);
                        col.setCellValueFactory(param -> {
                            if (param.getValue() != null && colIndex < param.getValue().size()) {
                                return new SimpleStringProperty(param.getValue().get(colIndex));
                            }
                            return new SimpleStringProperty("");
                        });
                        col.setPrefWidth(140);
                        previewTable.getColumns().add(col);
                    }
                }

                // Populate rows
                ObservableList<ObservableList<String>> rowList = FXCollections.observableArrayList();
                if (rowsNode.isArray()) {
                    for (JsonNode rowJson : rowsNode) {
                        ObservableList<String> row = FXCollections.observableArrayList();
                        if (rowJson.isArray()) {
                            for (JsonNode cell : rowJson) {
                                row.add(cell.asText(""));
                            }
                        }
                        rowList.add(row);
                    }
                }
                previewTable.setItems(rowList);
                if (rowList.isEmpty()) {
                    previewTable.setPlaceholder(new Label("No rows available on this page."));
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                previewTable.setPlaceholder(new Label("Failed to load records: " + ex.getMessage()));
            });
            return null;
        });
    }
}
