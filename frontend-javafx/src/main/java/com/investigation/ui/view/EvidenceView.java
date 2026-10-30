package com.investigation.ui.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.investigation.ui.api.ApiClient;
import com.investigation.ui.model.CaseModel;
import com.investigation.ui.model.CaseState;
import com.investigation.ui.model.EvidenceModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

public class EvidenceView extends VBox {

    private final ApiClient apiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Label headerLabel = new Label("Evidence Repository");
    private final Label subLabel = new Label("Cryptographic SHA-256 verified forensic files and datasets");
    private final TableView<EvidenceModel> evidenceTable = new TableView<>();
    private final ObservableList<EvidenceModel> evidenceData = FXCollections.observableArrayList();
    private final CheckBox simulatedCheckBox = new CheckBox("Mark as Simulated Demo Record [DEMO / SIMULATED RECORD]");
    private final Label uploadStatusLabel = new Label("");

    public EvidenceView(ApiClient apiClient) {
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

        Button refreshBtn = new Button("🔄 Refresh Evidence");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> refresh());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleBox, spacer, refreshBtn);

        // Upload Zone (Drag-and-Drop + FileChooser)
        VBox dropZone = createDropZone();

        // Table
        setupEvidenceTable();
        VBox tableContainer = new VBox(12);
        tableContainer.getStyleClass().add("bg-surface");
        tableContainer.setPadding(new Insets(16));
        tableContainer.getChildren().add(evidenceTable);
        VBox.setVgrow(evidenceTable, Priority.ALWAYS);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        getChildren().addAll(header, dropZone, tableContainer);

        CaseState.getInstance().activeCaseProperty().addListener((obs, o, n) -> refresh());
        refresh();
    }

    private VBox createDropZone() {
        VBox dropZone = new VBox(10);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setPadding(new Insets(20));
        dropZone.setStyle("-fx-border-color: #26354f; -fx-border-style: dashed; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-color: #0d131f; -fx-background-radius: 8px;");

        Label iconLbl = new Label("📥");
        iconLbl.setStyle("-fx-font-size: 28px;");

        Label promptLbl = new Label("Drag & Drop forensic CSV files here, or browse from disk");
        promptLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e0e6ed;");

        simulatedCheckBox.setStyle("-fx-text-fill: #8b9bb4; -fx-font-size: 12px;");

        Button browseBtn = new Button("📁 Browse CSV File");
        browseBtn.getStyleClass().add("btn-primary");
        browseBtn.setOnAction(e -> handleBrowseFile());

        uploadStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00e5ff;");

        HBox controls = new HBox(16, browseBtn, simulatedCheckBox, uploadStatusLabel);
        controls.setAlignment(Pos.CENTER);

        dropZone.getChildren().addAll(iconLbl, promptLbl, controls);

        // Drag & Drop event handlers
        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                dropZone.setStyle("-fx-border-color: #00e5ff; -fx-border-style: dashed; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-color: #121c2e; -fx-background-radius: 8px;");
            }
            event.consume();
        });

        dropZone.setOnDragExited(event -> {
            dropZone.setStyle("-fx-border-color: #26354f; -fx-border-style: dashed; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-color: #0d131f; -fx-background-radius: 8px;");
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                if (!files.isEmpty()) {
                    File file = files.get(0);
                    if (file.getName().toLowerCase().endsWith(".csv")) {
                        uploadFile(file);
                        success = true;
                    } else {
                        showUploadStatus("Only CSV files are supported for ingestion.", true);
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        return dropZone;
    }

    private void handleBrowseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Forensic Evidence CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Evidence Files (*.csv)", "*.csv"));
        File file = fileChooser.showOpenDialog(getScene() != null ? getScene().getWindow() : null);
        if (file != null) {
            uploadFile(file);
        }
    }

    private void uploadFile(File file) {
        CaseModel current = CaseState.getInstance().getActiveCase();
        if (current == null) {
            showUploadStatus("Please select an active case before uploading evidence.", true);
            return;
        }

        showUploadStatus("Calculating SHA-256 and uploading: " + file.getName() + "...", false);
        boolean isSimulated = simulatedCheckBox.isSelected();

        apiClient.uploadEvidence(current.getId(), file, isSimulated).thenAccept(res -> {
            Platform.runLater(() -> {
                showUploadStatus("✓ Successfully ingested " + file.getName() + " (" + res.path("recordCount").asInt() + " records)", false);
                refresh();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showUploadStatus("Upload failed: " + ex.getMessage(), true);
            });
            return null;
        });
    }

    private void showUploadStatus(String message, boolean isError) {
        uploadStatusLabel.setText(message);
        if (isError) {
            uploadStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff5252;");
        } else {
            uploadStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00e5ff;");
        }
    }

    private void setupEvidenceTable() {
        TableColumn<EvidenceModel, String> nameCol = new TableColumn<>("File Name");
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty("📄 " + cellData.getValue().getFileName()));
        nameCol.setPrefWidth(240);

        TableColumn<EvidenceModel, String> recordsCol = new TableColumn<>("Records");
        recordsCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getRecordCount())));
        recordsCol.setPrefWidth(90);

        TableColumn<EvidenceModel, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(cellData -> new SimpleStringProperty(formatBytes(cellData.getValue().getFileSizeBytes())));
        sizeCol.setPrefWidth(90);

        TableColumn<EvidenceModel, String> hashCol = new TableColumn<>("SHA-256 Hash");
        hashCol.setCellValueFactory(cellData -> cellData.getValue().sha256HashProperty());
        hashCol.setCellFactory(col -> new TableCell<>() {
            private final Button copyBtn = new Button("📋 Copy");
            private final Label label = new Label();
            private final HBox box = new HBox(8, label, copyBtn);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                label.setStyle("-fx-font-family: 'Consolas', monospace; -fx-text-fill: #00e5ff; -fx-font-size: 11px;");
                copyBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6; -fx-background-color: #1a273b; -fx-text-fill: #e0e6ed;");
                copyBtn.setOnAction(e -> {
                    String hash = getItem();
                    if (hash != null) {
                        ClipboardContent content = new ClipboardContent();
                        content.putString(hash);
                        Clipboard.getSystemClipboard().setContent(content);
                        copyBtn.setText("✓ Copied");
                        Platform.runLater(() -> {
                            new Thread(() -> {
                                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                                Platform.runLater(() -> copyBtn.setText("📋 Copy"));
                            }).start();
                        });
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item.length() > 20 ? item.substring(0, 18) + "..." : item);
                    setGraphic(box);
                }
            }
        });
        hashCol.setPrefWidth(220);

        TableColumn<EvidenceModel, String> statusCol = new TableColumn<>("Verification Type");
        statusCol.setCellValueFactory(cellData -> {
            boolean sim = cellData.getValue().isIsSimulated();
            return new SimpleStringProperty(sim ? "DEMO / SIMULATED" : "FORENSIC RECORD");
        });
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if (item.contains("SIMULATED")) {
                        badge.setStyle("-fx-background-color: rgba(255, 171, 0, 0.2); -fx-text-fill: #ffab00; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: rgba(0, 229, 255, 0.2); -fx-text-fill: #00e5ff; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        statusCol.setPrefWidth(160);

        TableColumn<EvidenceModel, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button previewBtn = new Button("👁 Preview");
            private final Button deleteBtn = new Button("🗑");
            private final HBox btnBox = new HBox(6, previewBtn, deleteBtn);
            {
                btnBox.setAlignment(Pos.CENTER_LEFT);
                previewBtn.getStyleClass().add("btn-secondary");
                previewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
                previewBtn.setOnAction(e -> {
                    EvidenceModel em = getTableView().getItems().get(getIndex());
                    new EvidencePreviewDialog(apiClient, em).showAndWait();
                });

                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8; -fx-background-color: #3b1a1a; -fx-text-fill: #ff5252;");
                deleteBtn.setOnAction(e -> {
                    EvidenceModel em = getTableView().getItems().get(getIndex());
                    confirmDelete(em);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnBox);
            }
        });
        actionsCol.setPrefWidth(130);

        evidenceTable.getColumns().setAll(nameCol, recordsCol, sizeCol, hashCol, statusCol, actionsCol);
        evidenceTable.setItems(evidenceData);
        evidenceTable.setPlaceholder(new Label("No evidence files uploaded for this case yet. Drop a CSV file above."));
    }

    private void confirmDelete(EvidenceModel em) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Evidence Record");
        alert.setHeaderText("Delete Evidence: " + em.getFileName());
        alert.setContentText("Are you sure you want to remove this evidence file? Associated timeline events and graph relations will be refreshed.");
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("bg-base");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                apiClient.deleteEvidence(em.getId()).thenRun(() -> {
                    Platform.runLater(this::refresh);
                }).exceptionally(ex -> {
                    Platform.runLater(() -> showUploadStatus("Delete failed: " + ex.getMessage(), true));
                    return null;
                });
            }
        });
    }

    public void refresh() {
        CaseModel current = CaseState.getInstance().getActiveCase();
        if (current == null) {
            headerLabel.setText("Evidence Repository (No Case Selected)");
            evidenceData.clear();
            return;
        }

        headerLabel.setText("Evidence Repository: " + current.getTitle());
        apiClient.getEvidence(current.getId()).thenAccept(node -> {
            Platform.runLater(() -> {
                try {
                    List<EvidenceModel> list = objectMapper.convertValue(node, new TypeReference<List<EvidenceModel>>() {});
                    evidenceData.setAll(list);
                } catch (Exception e) {
                    evidenceData.clear();
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(evidenceData::clear);
            return null;
        });
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(bytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
