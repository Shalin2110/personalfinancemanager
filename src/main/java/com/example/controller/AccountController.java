package com.example.controller;

import com.example.model.Account;
import com.example.service.AccountService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.geometry.Insets;

public class AccountController {

    @FXML private TextField txtName;
    @FXML private TextField txtCurrency;
    @FXML private TextField txtOpeningBalance;
    @FXML private TableView<Account> tblAccount;
    @FXML private TableColumn<Account, Integer> colId;
    @FXML private TableColumn<Account, String> colName;
    @FXML private TableColumn<Account, String> colCurrency;
    @FXML private TableColumn<Account, Double> colBalance;
    @FXML private TableColumn<Account, Void> colActions;

    @FXML private Button btnAdd;
    private boolean editMode = false;
    private Account selectedAccount = null;

    private final AccountService service = new AccountService();
    private final ObservableList<Account> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        loadAccounts();
        setupRowSelection();
        addActionButtons();

        // Set default currency
        txtCurrency.setText("LKR");
    }

    private void setupColumns() {
        colId.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getAccountId()).asObject());
        colName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getName()));
        colCurrency.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getCurrency()));
        colBalance.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getOpeningBalance()).asObject());
    }

    private void setupRowSelection() {
        tblAccount.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && !editMode) {
                txtName.setText(newSel.getName());
                txtCurrency.setText(newSel.getCurrency());
                txtOpeningBalance.setText(String.valueOf(newSel.getOpeningBalance()));
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
                    selectedAccount = getTableView().getItems().get(getIndex());
                    editMode = true;
                    txtName.setText(selectedAccount.getName());
                    txtCurrency.setText(selectedAccount.getCurrency());
                    txtOpeningBalance.setText(String.valueOf(selectedAccount.getOpeningBalance()));
                    tblAccount.getSelectionModel().select(selectedAccount);
                    btnAdd.setText("💾 Save Changes");
                    btnAdd.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                });

                btnDelete.setOnAction(e -> {
                    Account selected = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete account: " + selected.getName() + "?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait();
                    if (confirm.getResult() == ButtonType.YES) {
                        service.deleteAccount(selected.getAccountId());
                        loadAccounts();
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
    public void loadAccounts() {
        data.setAll(service.getAllAccounts());
        tblAccount.setItems(data);
    }

    @FXML
    public void addOrUpdateAccount() {
        try {
            if (txtName.getText().isEmpty()) {
                showError("Please enter account name.");
                return;
            }
            if (txtCurrency.getText().isEmpty()) {
                showError("Please enter currency.");
                return;
            }

            if (editMode && selectedAccount != null) {
                // Update existing
                selectedAccount.setName(txtName.getText());
                selectedAccount.setCurrency(txtCurrency.getText());
                selectedAccount.setOpeningBalance(Double.parseDouble(txtOpeningBalance.getText()));

                service.updateAccount(selectedAccount);
                showInfo("Account updated successfully.");
            } else {
                // Add new
                Account account = new Account();
                account.setUserId(1); // Same as Budget pattern
                account.setName(txtName.getText());
                account.setCurrency(txtCurrency.getText());
                account.setOpeningBalance(Double.parseDouble(txtOpeningBalance.getText()));
                account.setDeleteFlag(false);

                service.addAccount(account);
                showInfo("Account added successfully.");
            }

            loadAccounts();
            clearForm();

        } catch (Exception e) {
            showError("Error saving account: " + e.getMessage());
        }
    }

    @FXML
    public void clearForm() {
        txtName.clear();
        txtCurrency.setText("LKR");
        txtOpeningBalance.clear();
        tblAccount.getSelectionModel().clearSelection();
        editMode = false;
        selectedAccount = null;
        btnAdd.setText("➕ Add Account");
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