package com.example.controller;

import com.example.model.Category;
import com.example.service.CategoryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.geometry.Insets;

public class CategoryController {

    @FXML private TextField txtName;
    @FXML private ComboBox<String> cmbType;
    @FXML private ComboBox<String> cmbParentCategory;
    @FXML private TableView<Category> tblCategory;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TableColumn<Category, String> colType;
    @FXML private TableColumn<Category, String> colParent;
    @FXML private TableColumn<Category, Void> colActions;

    @FXML private Button btnAdd;
    private boolean editMode = false;
    private Category selectedCategory = null;

    private final CategoryService service = new CategoryService();
    private final ObservableList<Category> data = FXCollections.observableArrayList();
    private ObservableList<String> parentCategories = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        setupDropdowns();
        loadCategories();
        setupRowSelection();
        addActionButtons();
    }

    private void setupColumns() {
        colId.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getCategoryId()).asObject());
        colName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getName()));
        colType.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getType()));
        colParent.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                getParentCategoryName(cd.getValue().getParentCategoryId())
        ));
    }

    private void setupDropdowns() {
        // Setup type dropdown
        cmbType.setItems(FXCollections.observableArrayList("EXPENSE", "INCOME"));

        // Setup parent category dropdown
        updateParentCategoryDropdown();
    }

    private void updateParentCategoryDropdown() {
        parentCategories.clear();
        parentCategories.add("None"); // For no parent
        service.getAllCategories().forEach(category ->
                parentCategories.add(category.getName())
        );
        cmbParentCategory.setItems(parentCategories);
    }

    private String getParentCategoryName(Integer parentCategoryId) {
        if (parentCategoryId == null) return "None";
        return service.getAllCategories().stream()
                .filter(c -> c.getCategoryId() == parentCategoryId)
                .map(Category::getName)
                .findFirst()
                .orElse("Unknown");
    }

    private void setupRowSelection() {
        tblCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && !editMode) {
                txtName.setText(newSel.getName());
                cmbType.setValue(newSel.getType());
                cmbParentCategory.setValue(getParentCategoryName(newSel.getParentCategoryId()));
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
                    selectedCategory = getTableView().getItems().get(getIndex());
                    editMode = true;
                    txtName.setText(selectedCategory.getName());
                    cmbType.setValue(selectedCategory.getType());
                    cmbParentCategory.setValue(getParentCategoryName(selectedCategory.getParentCategoryId()));
                    tblCategory.getSelectionModel().select(selectedCategory);
                    btnAdd.setText("💾 Save Changes");
                    btnAdd.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                });

                btnDelete.setOnAction(e -> {
                    Category selected = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete category: " + selected.getName() + "?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait();
                    if (confirm.getResult() == ButtonType.YES) {
                        service.deleteCategory(selected.getCategoryId());
                        loadCategories();
                        updateParentCategoryDropdown();
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
    public void loadCategories() {
        data.setAll(service.getAllCategories());
        tblCategory.setItems(data);
    }

    @FXML
    public void addOrUpdateCategory() {
        try {
            if (txtName.getText().isEmpty()) {
                showError("Please enter category name.");
                return;
            }
            if (cmbType.getValue() == null) {
                showError("Please select category type.");
                return;
            }

            if (editMode && selectedCategory != null) {
                // Update existing
                selectedCategory.setName(txtName.getText());
                selectedCategory.setType(cmbType.getValue());
                selectedCategory.setParentCategoryId(getParentCategoryIdFromName(cmbParentCategory.getValue()));

                service.updateCategory(selectedCategory);
                showInfo("Category updated successfully.");
            } else {
                // Add new
                Category category = new Category();
                category.setUserId(1); // Same as other modules
                category.setName(txtName.getText());
                category.setType(cmbType.getValue());
                category.setParentCategoryId(getParentCategoryIdFromName(cmbParentCategory.getValue()));
                category.setDeleteFlag(false);

                service.addCategory(category);
                showInfo("Category added successfully.");
            }

            loadCategories();
            updateParentCategoryDropdown();
            clearForm();

        } catch (Exception e) {
            showError("Error saving category: " + e.getMessage());
        }
    }

    private Integer getParentCategoryIdFromName(String parentName) {
        if (parentName == null || "None".equals(parentName)) {
            return null;
        }
        return service.getAllCategories().stream()
                .filter(c -> c.getName().equals(parentName))
                .map(Category::getCategoryId)
                .findFirst()
                .orElse(null);
    }

    @FXML
    public void clearForm() {
        txtName.clear();
        cmbType.setValue(null);
        cmbParentCategory.setValue("None");
        tblCategory.getSelectionModel().clearSelection();
        editMode = false;
        selectedCategory = null;
        btnAdd.setText("➕ Add Category");
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