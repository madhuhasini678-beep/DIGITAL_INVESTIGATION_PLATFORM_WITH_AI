package com.investigation.ui.view;

import com.investigation.ui.api.ApiClient;
import com.investigation.ui.model.CaseModel;
import com.investigation.ui.model.CaseState;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CasesView extends VBox {

    private final ApiClient apiClient;
    private final TableView<CaseModel> casesTable = new TableView<>();
    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final ComboBox<String> statusFilter = new ComboBox<>();

    public CasesView(ApiClient apiClient) {
        this.apiClient = apiClient;

        setSpacing(18);
        setPadding(new Insets(24));
        getStyleClass().add("bg-base");

        buildUI();
        refresh();
    }

    private void buildUI() {
        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Case Management");
        title.getStyleClass().add("text-h1");
        Label subtitle = new Label("Create, manage, and investigate active and historical forensic dockets");
        subtitle.getStyleClass().add("text-secondary");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> refresh());

        Button createBtn = new Button("+ Create New Case");
        createBtn.getStyleClass().add("btn-primary");
        createBtn.setOnAction(e -> showCreateCaseDialog());

        header.getChildren().addAll(titleBox, spacer, refreshBtn, createBtn);

        // Filter Bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("bg-surface");
        filterBar.setPadding(new Insets(12, 16, 12, 16));

        searchField.setPromptText("Search by case title, number, or description...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, o, n) -> refresh());

        categoryFilter.getItems().addAll("ALL CATEGORIES", "FINANCIAL_FRAUD", "HEIST_THEFT", "INDUSTRIAL_DISASTER", "KIDNAPPING_ABDUCTION", "CYBER_INCIDENT", "GENERAL_FORENSIC");
        categoryFilter.setValue("ALL CATEGORIES");
        categoryFilter.setOnAction(e -> refresh());

        statusFilter.getItems().addAll("ALL STATUSES", "ACTIVE", "UNDER_REVIEW", "CLOSED", "ARCHIVED");
        statusFilter.setValue("ALL STATUSES");
        statusFilter.setOnAction(e -> refresh());

        filterBar.getChildren().addAll(new Label("Filter:"), searchField, categoryFilter, statusFilter);

        // Cases Table
        configureTable();

        VBox tableWrapper = new VBox(casesTable);
        VBox.setVgrow(casesTable, Priority.ALWAYS);
        VBox.setVgrow(tableWrapper, Priority.ALWAYS);

        getChildren().addAll(header, filterBar, tableWrapper);
    }

    private void configureTable() {
        TableColumn<CaseModel, String> numCol = new TableColumn<>("Case #");
        numCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCaseNumber()));
        numCol.setPrefWidth(140);

        TableColumn<CaseModel, String> titleCol = new TableColumn<>("Case Title");
        titleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        titleCol.setPrefWidth(240);

        TableColumn<CaseModel, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory()));
        catCol.setPrefWidth(160);

        TableColumn<CaseModel, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setPrefWidth(110);

        TableColumn<CaseModel, String> evCountCol = new TableColumn<>("Evidence");
        evCountCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getEvidenceCount())));
        evCountCol.setPrefWidth(80);

        TableColumn<CaseModel, String> anomCountCol = new TableColumn<>("Anomalies");
        anomCountCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getAnomalyCount())));
        anomCountCol.setPrefWidth(90);

        TableColumn<CaseModel, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isHistoricalDemo() ? "Historical Demo" : "Active Case"));
        typeCol.setPrefWidth(130);

        TableColumn<CaseModel, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(220);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button selectBtn = new Button("Set Active");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox btnBox = new HBox(6, selectBtn, editBtn, deleteBtn);

            {
                selectBtn.getStyleClass().add("btn-secondary");
                editBtn.getStyleClass().add("btn-secondary");
                deleteBtn.getStyleClass().add("btn-danger");

                selectBtn.setOnAction(e -> {
                    CaseModel item = getTableView().getItems().get(getIndex());
                    CaseState.getInstance().setActiveCase(item);
                });

                editBtn.setOnAction(e -> {
                    CaseModel item = getTableView().getItems().get(getIndex());
                    showEditCaseDialog(item);
                });

                deleteBtn.setOnAction(e -> {
                    CaseModel item = getTableView().getItems().get(getIndex());
                    confirmDeleteCase(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnBox);
            }
        });

        casesTable.getColumns().addAll(numCol, titleCol, catCol, statusCol, evCountCol, anomCountCol, typeCol, actionsCol);
        casesTable.setPlaceholder(new Label("No investigation cases found matching filter criteria."));
    }

    public void refresh() {
        String search = searchField.getText().trim();
        String cat = categoryFilter.getValue();
        if ("ALL CATEGORIES".equals(cat)) cat = "";

        String stat = statusFilter.getValue();
        if ("ALL STATUSES".equals(stat)) stat = "";

        apiClient.getCases(search, cat, stat).thenAccept(cases -> {
            Platform.runLater(() -> casesTable.getItems().setAll(cases));
        }).exceptionally(ex -> null);
    }

    private void showCreateCaseDialog() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Create New Investigation Case");
        dialog.setHeaderText("Enter details for new forensic case record");

        dialog.getDialogPane().getStylesheets().addAll(getScene().getStylesheets());
        dialog.getDialogPane().getStyleClass().add("bg-surface");

        ButtonType createButtonType = new ButtonType("Create Case", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleInput = new TextField();
        titleInput.setPromptText("e.g. Operation Nightfall");

        TextField numberInput = new TextField();
        numberInput.setPromptText("Optional (Auto-generated if empty)");

        TextArea descInput = new TextArea();
        descInput.setPromptText("Case summary and investigation scope...");
        descInput.setPrefRowCount(3);

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("FINANCIAL_FRAUD", "HEIST_THEFT", "INDUSTRIAL_DISASTER", "KIDNAPPING_ABDUCTION", "CYBER_INCIDENT", "GENERAL_FORENSIC");
        catBox.setValue("GENERAL_FORENSIC");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("ACTIVE", "UNDER_REVIEW", "CLOSED");
        statusBox.setValue("ACTIVE");

        grid.add(new Label("Case Title:"), 0, 0);
        grid.add(titleInput, 1, 0);
        grid.add(new Label("Case Number:"), 0, 1);
        grid.add(numberInput, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(catBox, 1, 2);
        grid.add(new Label("Initial Status:"), 0, 3);
        grid.add(statusBox, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descInput, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                if (titleInput.getText().trim().isEmpty()) {
                    return null;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("title", titleInput.getText().trim());
                if (!numberInput.getText().trim().isEmpty()) {
                    map.put("caseNumber", numberInput.getText().trim());
                }
                map.put("description", descInput.getText().trim());
                map.put("category", catBox.getValue());
                map.put("status", statusBox.getValue());
                map.put("isHistoricalDemo", false);
                map.put("incidentDate", Instant.now().toString());
                return map;
            }
            return null;
        });

        Optional<Map<String, Object>> result = dialog.showAndWait();
        result.ifPresent(map -> {
            apiClient.createCase(map).thenAccept(created -> {
                Platform.runLater(() -> {
                    CaseState.getInstance().setActiveCase(created);
                    refresh();
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to create case: " + ex.getMessage());
                    alert.show();
                });
                return null;
            });
        });
    }

    private void showEditCaseDialog(CaseModel caseModel) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Edit Case: " + caseModel.getCaseNumber());
        dialog.setHeaderText("Modify case parameters");

        dialog.getDialogPane().getStylesheets().addAll(getScene().getStylesheets());
        dialog.getDialogPane().getStyleClass().add("bg-surface");

        ButtonType saveButtonType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleInput = new TextField(caseModel.getTitle());
        TextArea descInput = new TextArea(caseModel.getDescription() != null ? caseModel.getDescription() : "");
        descInput.setPrefRowCount(3);

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("FINANCIAL_FRAUD", "HEIST_THEFT", "INDUSTRIAL_DISASTER", "KIDNAPPING_ABDUCTION", "CYBER_INCIDENT", "GENERAL_FORENSIC");
        catBox.setValue(caseModel.getCategory());

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("ACTIVE", "UNDER_REVIEW", "CLOSED", "ARCHIVED");
        statusBox.setValue(caseModel.getStatus());

        grid.add(new Label("Case Title:"), 0, 0);
        grid.add(titleInput, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(catBox, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusBox, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descInput, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Map<String, Object> map = new HashMap<>();
                map.put("title", titleInput.getText().trim());
                map.put("description", descInput.getText().trim());
                map.put("category", catBox.getValue());
                map.put("status", statusBox.getValue());
                return map;
            }
            return null;
        });

        Optional<Map<String, Object>> result = dialog.showAndWait();
        result.ifPresent(map -> {
            apiClient.updateCase(caseModel.getId(), map).thenAccept(updated -> {
                Platform.runLater(() -> {
                    if (CaseState.getInstance().getActiveCase() != null &&
                            CaseState.getInstance().getActiveCase().getId().equals(updated.getId())) {
                        CaseState.getInstance().setActiveCase(updated);
                    }
                    refresh();
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update case: " + ex.getMessage());
                    alert.show();
                });
                return null;
            });
        });
    }

    private void confirmDeleteCase(CaseModel caseModel) {
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete case '" + caseModel.getTitle() + "'?\n\nThis will also remove all associated evidence files, timeline events, and intelligence records.",
                ButtonType.YES, ButtonType.NO
        );
        confirm.setTitle("Confirm Case Deletion");
        confirm.setHeaderText("Delete Investigation Case");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            apiClient.deleteCase(caseModel.getId()).thenRun(() -> {
                Platform.runLater(() -> {
                    if (CaseState.getInstance().getActiveCase() != null &&
                            CaseState.getInstance().getActiveCase().getId().equals(caseModel.getId())) {
                        CaseState.getInstance().setActiveCase(null);
                    }
                    refresh();
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to delete case: " + ex.getMessage());
                    alert.show();
                });
                return null;
            });
        }
    }
}
