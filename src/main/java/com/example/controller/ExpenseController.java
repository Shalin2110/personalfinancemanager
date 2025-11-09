package com.example.controller;

import com.example.model.Expense;
import com.example.service.ExpenseService;
import com.example.service.CategoryService;
import com.example.service.AccountService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.geometry.Insets;

import java.time.LocalDate;
import java.util.Map;

public class ExpenseController {

    @FXML private ComboBox<String> cmbCategory;
    @FXML private ComboBox<String> cmbAccount;
    @FXML private TextField txtAmount;
    @FXML private TextField txtCurrency;
    @FXML private TextField txtDescription;
    @FXML private DatePicker dpDate;
    @FXML private CheckBox chkRecurring;

    @FXML private TableView<Expense> tblExpense;
    @FXML private TableColumn<Expense, Integer> colId;
    @FXML private TableColumn<Expense, String> colCategory;
    @FXML private TableColumn<Expense, String> colAccount;
    @FXML private TableColumn<Expense, Double> colAmount;
    @FXML private TableColumn<Expense, String> colCurrency;
    @FXML private TableColumn<Expense, LocalDate> colDate;
    @FXML private TableColumn<Expense, String> colDescription;
    @FXML private TableColumn<Expense, String> colRecurring;
    @FXML private TableColumn<Expense, Void> colActions;

    @FXML private Button btnAdd;
    private boolean editMode = false;
    private Expense selectedExpense = null;

    private final ExpenseService service = new ExpenseService();
    private final CategoryService categoryService = new CategoryService();
    private final AccountService accountService = new AccountService();
    private final ObservableList<Expense> data = FXCollections.observableArrayList();
    private Map<String, Integer> categoryMap;
    private Map<String, Integer> accountMap;

    @FXML
    public void initialize() {
        setupColumns();
        setupDropdowns();
        loadExpenses();
        setupRowSelection();
        addActionButtons();
    }

    private void setupColumns() {
        colId.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getExpenseId()).asObject());
        colCategory.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getCategoryName()));
        colAccount.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getAccountName()));
        colAmount.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getAmount()).asObject());
        colCurrency.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getCurrency()));
        colDate.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getExpenseDate()));
        colDescription.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getDescription()));
        colRecurring.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getRecurringFlag() == 1 ? "Yes" : "No"
        ));
    }

    private void setupDropdowns() {
        categoryMap = categoryService.getCategoryMap();
        cmbCategory.setItems(FXCollections.observableArrayList(categoryMap.keySet()));

        accountMap = accountService.getAccountMap();
        cmbAccount.setItems(FXCollections.observableArrayList(accountMap.keySet()));

        // Set default currency
        txtCurrency.setText("LKR");
    }

    private void setupRowSelection() {
        tblExpense.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && !editMode) {
                cmbCategory.setValue(newSel.getCategoryName());
                cmbAccount.setValue(newSel.getAccountName());
                txtAmount.setText(String.valueOf(newSel.getAmount()));
                txtCurrency.setText(newSel.getCurrency());
                dpDate.setValue(newSel.getExpenseDate());
                txtDescription.setText(newSel.getDescription());
                chkRecurring.setSelected(newSel.getRecurringFlag() == 1);
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
                    selectedExpense = getTableView().getItems().get(getIndex());
                    editMode = true;
                    cmbCategory.setValue(selectedExpense.getCategoryName());
                    cmbAccount.setValue(selectedExpense.getAccountName());
                    txtAmount.setText(String.valueOf(selectedExpense.getAmount()));
                    txtCurrency.setText(selectedExpense.getCurrency());
                    dpDate.setValue(selectedExpense.getExpenseDate());
                    txtDescription.setText(selectedExpense.getDescription());
                    chkRecurring.setSelected(selectedExpense.getRecurringFlag() == 1);
                    tblExpense.getSelectionModel().select(selectedExpense);
                    btnAdd.setText("💾 Save Changes");
                    btnAdd.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                });

                btnDelete.setOnAction(e -> {
                    Expense selected = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete expense: " + selected.getDescription() + "?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait();
                    if (confirm.getResult() == ButtonType.YES) {
                        service.deleteExpense(selected.getExpenseId());
                        loadExpenses();
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
    public void loadExpenses() {
        data.setAll(service.getAllExpenses());
        tblExpense.setItems(data);
    }

    @FXML
    public void addOrUpdateExpense() {
        try {
            String selectedCategory = cmbCategory.getValue();
            String selectedAccount = cmbAccount.getValue();

            if (selectedCategory == null) {
                showError("Please select a category.");
                return;
            }
            if (selectedAccount == null) {
                showError("Please select an account.");
                return;
            }
            if (txtAmount.getText().isEmpty()) {
                showError("Please enter an amount.");
                return;
            }
            if (dpDate.getValue() == null) {
                showError("Please select a date.");
                return;
            }

            if (editMode && selectedExpense != null) {
                // Update existing
                selectedExpense.setCategoryId(categoryMap.get(selectedCategory));
                selectedExpense.setCategoryName(selectedCategory);
                selectedExpense.setAccountId(accountMap.get(selectedAccount));
                selectedExpense.setAccountName(selectedAccount);
                selectedExpense.setAmount(Double.parseDouble(txtAmount.getText()));
                selectedExpense.setCurrency(txtCurrency.getText());
                selectedExpense.setExpenseDate(dpDate.getValue());
                selectedExpense.setDescription(txtDescription.getText());
                selectedExpense.setRecurringFlag(chkRecurring.isSelected() ? 1 : 0);

                service.updateExpense(selectedExpense);
                showInfo("Expense updated successfully.");
            } else {
                // Add new
                Expense expense = new Expense();
                expense.setUserId(1); // Same as Budget pattern
                expense.setDeviceTxnId(java.util.UUID.randomUUID().toString());
                expense.setCategoryId(categoryMap.get(selectedCategory));
                expense.setCategoryName(selectedCategory);
                expense.setAccountId(accountMap.get(selectedAccount));
                expense.setAccountName(selectedAccount);
                expense.setAmount(Double.parseDouble(txtAmount.getText()));
                expense.setCurrency(txtCurrency.getText());
                expense.setExpenseDate(dpDate.getValue());
                expense.setDescription(txtDescription.getText());
                expense.setRecurringFlag(chkRecurring.isSelected() ? 1 : 0);
                expense.setDeleteFlag(false);

                service.addExpense(expense);
                showInfo("Expense added successfully.");
            }

            loadExpenses();
            clearForm();

        } catch (Exception e) {
            showError("Error saving expense: " + e.getMessage());
        }
    }

    @FXML
    public void clearForm() {
        cmbCategory.setValue(null);
        cmbAccount.setValue(null);
        txtAmount.clear();
        txtCurrency.setText("LKR");
        dpDate.setValue(null);
        txtDescription.clear();
        chkRecurring.setSelected(false);
        tblExpense.getSelectionModel().clearSelection();
        editMode = false;
        selectedExpense = null;
        btnAdd.setText("➕ Add Expense");
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