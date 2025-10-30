package com.example.controller;

import com.example.model.SavingsGoal;
import com.example.service.SavingsGoalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class SavingsGoalController {

    @FXML private TextField txtName, txtTarget, txtCurrent;
    @FXML private DatePicker dpStart, dpEnd;
    @FXML private TableView<SavingsGoal> tblGoals;
    @FXML private TableColumn<SavingsGoal, Integer> colId;
    @FXML private TableColumn<SavingsGoal, String> colName, colStatus;
    @FXML private TableColumn<SavingsGoal, Double> colTarget, colCurrent, colProgress;

    private final SavingsGoalService service = new SavingsGoalService();
    private final ObservableList<SavingsGoal> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getGoalId()).asObject());
        colName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getName()));
        colTarget.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getTargetAmount()).asObject());
        colCurrent.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getCurrentAmount()).asObject());
        colProgress.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getProgressPercent()).asObject());
        colStatus.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getStatus()));

        loadGoals();
    }

    @FXML
    public void loadGoals() {
        data.setAll(service.getAllGoals());
        tblGoals.setItems(data);
    }

    @FXML
    public void addGoal() {
        try {
            SavingsGoal g = new SavingsGoal();
            g.setName(txtName.getText());
            g.setTargetAmount(Double.parseDouble(txtTarget.getText()));
            g.setCurrentAmount(Double.parseDouble(txtCurrent.getText()));
            g.setStartDate(dpStart.getValue());
            g.setEndDate(dpEnd.getValue());
            g.setDeleteFlag(false);
            service.addGoal(g);
            loadGoals();
        } catch (Exception e) {
            showError("Error adding goal: " + e.getMessage());
        }
    }

    @FXML
    public void updateGoal() {
        SavingsGoal selected = tblGoals.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a goal to update.");
            return;
        }
        try {
            selected.setName(txtName.getText());
            selected.setTargetAmount(Double.parseDouble(txtTarget.getText()));
            selected.setCurrentAmount(Double.parseDouble(txtCurrent.getText()));
            selected.setStartDate(dpStart.getValue());
            selected.setEndDate(dpEnd.getValue());
            service.updateGoal(selected);
            loadGoals();
        } catch (Exception e) {
            showError("Error updating goal: " + e.getMessage());
        }
    }

    @FXML
    public void deleteGoal() {
        SavingsGoal selected = tblGoals.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a goal to delete.");
            return;
        }
        service.deleteGoal(selected.getGoalId());
        loadGoals();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
