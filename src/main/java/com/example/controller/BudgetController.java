package com.example.controller;

import com.example.model.Budget;
import com.example.service.BudgetService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class BudgetController {

    @FXML private TextField txtCategoryId, txtAmount;
    @FXML private DatePicker dpStart, dpEnd;
    @FXML private TableView<Budget> tblBudget;
    @FXML private TableColumn<Budget, Integer> colId;
    @FXML private TableColumn<Budget, String> colCategory;
    @FXML private TableColumn<Budget, Double> colAmount;
    @FXML private TableColumn<Budget, LocalDate> colStart;
    @FXML private TableColumn<Budget, LocalDate> colEnd;

    private final BudgetService service = new BudgetService();
    private final ObservableList<Budget> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getBudgetId()).asObject());
        colCategory.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getCategoryName()));
        colAmount.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getAmount()).asObject());
        colStart.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getStartDate()));
        colEnd.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getEndDate()));
        loadBudgets();
    }

    @FXML
    public void loadBudgets() {
        data.setAll(service.getAllBudgets());
        tblBudget.setItems(data);
    }

    @FXML
    public void addBudget() {
        try {
            Budget b = new Budget();
            b.setCategoryId(Integer.parseInt(txtCategoryId.getText()));
            b.setAmount(Double.parseDouble(txtAmount.getText()));
            b.setStartDate(dpStart.getValue());
            b.setEndDate(dpEnd.getValue());
            b.setDeleteFlag(false);
            service.addBudget(b);
            loadBudgets();
        } catch (Exception e) {
            showError("Error adding budget: " + e.getMessage());
        }
    }

    @FXML
    public void updateBudget() {
        Budget selected = tblBudget.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a budget to update.");
            return;
        }
        try {
            selected.setAmount(Double.parseDouble(txtAmount.getText()));
            selected.setStartDate(dpStart.getValue());
            selected.setEndDate(dpEnd.getValue());
            service.updateBudget(selected);
            loadBudgets();
        } catch (Exception e) {
            showError("Error updating: " + e.getMessage());
        }
    }

    @FXML
    public void deleteBudget() {
        Budget selected = tblBudget.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a budget to delete.");
            return;
        }
        service.deleteBudget(selected.getBudgetId());
        loadBudgets();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
