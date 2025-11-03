package com.example.controller;

import com.example.model.Budget;
import com.example.service.BudgetService;
import com.example.service.CategoryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.geometry.Insets;

import java.time.LocalDate;
import java.util.Map;

public class BudgetController {

    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField txtAmount;
    @FXML private DatePicker dpStart, dpEnd;
    @FXML private TableView<Budget> tblBudget;
    @FXML private TableColumn<Budget, Integer> colId;
    @FXML private TableColumn<Budget, String> colCategory;
    @FXML private TableColumn<Budget, Double> colAmount;
    @FXML private TableColumn<Budget, LocalDate> colStart;
    @FXML private TableColumn<Budget, LocalDate> colEnd;
    @FXML private TableColumn<Budget, Void> colActions;

    @FXML private Button btnAdd; // reference to main add button (we’ll rename it dynamically)
    private boolean editMode = false;
    private Budget selectedBudget = null;

    private final BudgetService service = new BudgetService();
    private final CategoryService categoryService = new CategoryService();
    private final ObservableList<Budget> data = FXCollections.observableArrayList();
    private Map<String, Integer> categoryMap;

    @FXML
    public void initialize() {
        setupColumns();
        setupCategoryDropdown();
        loadBudgets();
        setupRowSelection();
        addActionButtons();
    }

    private void setupColumns() {
        colId.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getBudgetId()).asObject());
        colCategory.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getCategoryName()));
        colAmount.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getAmount()).asObject());
        colStart.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getStartDate()));
        colEnd.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getEndDate()));
    }

    private void setupCategoryDropdown() {
        categoryMap = categoryService.getCategoryMap();
        cmbCategory.setItems(FXCollections.observableArrayList(categoryMap.keySet()));
    }

    private void setupRowSelection() {
        tblBudget.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && !editMode) { // only populate if not editing another row
                cmbCategory.setValue(newSel.getCategoryName());
                txtAmount.setText(String.valueOf(newSel.getAmount()));
                dpStart.setValue(newSel.getStartDate());
                dpEnd.setValue(newSel.getEndDate());
            }
        });
    }

    private void addActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏ Edit");
            private final Button btnDelete = new Button("🗑 Delete");
            private final HBox pane = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: #E53935; -fx-text-fill: white;");
                btnEdit.setFont(Font.font(13));
                btnDelete.setFont(Font.font(13));
                pane.setPadding(new Insets(4, 0, 4, 0));

                btnEdit.setOnAction(e -> {
                    selectedBudget = getTableView().getItems().get(getIndex());
                    editMode = true;
                    cmbCategory.setValue(selectedBudget.getCategoryName());
                    txtAmount.setText(String.valueOf(selectedBudget.getAmount()));
                    dpStart.setValue(selectedBudget.getStartDate());
                    dpEnd.setValue(selectedBudget.getEndDate());
                    tblBudget.getSelectionModel().select(selectedBudget);
                    btnAdd.setText("💾 Save Changes");
                    btnAdd.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                });

                btnDelete.setOnAction(e -> {
                    Budget selected = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete this budget for category " + selected.getCategoryName() + "?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait();
                    if (confirm.getResult() == ButtonType.YES) {
                        service.deleteBudget(selected.getBudgetId());
                        loadBudgets();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML
    public void loadBudgets() {
        data.setAll(service.getAllBudgets());
        tblBudget.setItems(data);
    }

    @FXML
    public void addOrUpdateBudget() {
        try {
            String selectedCategory = cmbCategory.getValue();
            if (selectedCategory == null) {
                showError("Please select a category.");
                return;
            }

            if (editMode && selectedBudget != null) {
                // 🔹 Update existing
                selectedBudget.setCategoryId(categoryMap.get(selectedCategory));
                selectedBudget.setCategoryName(selectedCategory);
                selectedBudget.setAmount(Double.parseDouble(txtAmount.getText()));
                selectedBudget.setStartDate(dpStart.getValue());
                selectedBudget.setEndDate(dpEnd.getValue());

                service.updateBudget(selectedBudget);
                showInfo("Budget updated successfully.");
            } else {
                // 🔹 Add new
                Budget b = new Budget();
                b.setCategoryId(categoryMap.get(selectedCategory));
                b.setCategoryName(selectedCategory);
                b.setAmount(Double.parseDouble(txtAmount.getText()));
                b.setStartDate(dpStart.getValue());
                b.setEndDate(dpEnd.getValue());
                b.setDeleteFlag(false);
                service.addBudget(b);
                showInfo("Budget added successfully.");
            }

            loadBudgets();
            clearForm();

        } catch (Exception e) {
            showError("Error saving budget: " + e.getMessage());
        }
    }

    @FXML
    public void clearForm() {
        cmbCategory.setValue(null);
        txtAmount.clear();
        dpStart.setValue(null);
        dpEnd.setValue(null);
        tblBudget.getSelectionModel().clearSelection();
        editMode = false;
        selectedBudget = null;
        btnAdd.setText("Add");
        btnAdd.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
