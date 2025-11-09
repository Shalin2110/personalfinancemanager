package com.example.controller;

import com.example.model.SavingsGoal;
import com.example.service.SavingsGoalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.text.Font;

public class SavingsGoalController {

    @FXML private TextField txtName, txtTarget, txtCurrent;
    @FXML private DatePicker dpStart, dpTarget;
    @FXML private Button btnAdd;
    @FXML private TableView<SavingsGoal> tblGoals;
    @FXML private TableColumn<SavingsGoal, Integer> colId;
    @FXML private TableColumn<SavingsGoal, String> colName, colStatus;
    @FXML private TableColumn<SavingsGoal, Double> colTarget, colCurrent, colProgress;
    @FXML private TableColumn<SavingsGoal, Void> colActions;

    private final SavingsGoalService service = new SavingsGoalService();
    private final ObservableList<SavingsGoal> data = FXCollections.observableArrayList();

    private boolean editMode = false;
    private SavingsGoal selectedGoal = null;

    @FXML
    public void initialize() {
        setupColumns();
        addActionButtons();
        setupRowSelection();
        loadGoals();
    }

    private void setupColumns() {
        colId.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getGoalId()).asObject());
        colName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getName()));
        colTarget.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getTargetAmount()).asObject());
        colCurrent.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getCurrentAmount()).asObject());

        // Format progress to whole number or two decimals cleanly
        colProgress.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(
                Math.round(cd.getValue().getProgressPercent() * 100.0) / 100.0
        ).asObject());

        // Status always calculated via model (ACHIEVED / IN PROGRESS)
        colStatus.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getStatus()));
    }

    private void setupRowSelection() {
        tblGoals.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && !editMode) {
                txtName.setText(newSel.getName());
                txtTarget.setText(String.valueOf(newSel.getTargetAmount()));
                txtCurrent.setText(String.valueOf(newSel.getCurrentAmount()));
                dpStart.setValue(newSel.getStartDate());
                dpTarget.setValue(newSel.getTargetDate());
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
                    selectedGoal = getTableView().getItems().get(getIndex());
                    editMode = true;

                    txtName.setText(selectedGoal.getName());
                    txtTarget.setText(String.valueOf(selectedGoal.getTargetAmount()));
                    txtCurrent.setText(String.valueOf(selectedGoal.getCurrentAmount()));
                    dpStart.setValue(selectedGoal.getStartDate());
                    dpTarget.setValue(selectedGoal.getTargetDate());

                    btnAdd.setText("💾 Save Changes");
                    btnAdd.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                });

                btnDelete.setOnAction(e -> {
                    SavingsGoal g = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete savings goal: " + g.getName() + "?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait();
                    if (confirm.getResult() == ButtonType.YES) {
                        service.deleteGoal(g.getGoalId());
                        loadGoals();
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
    public void loadGoals() {
        data.setAll(service.getAllGoals());
        tblGoals.setItems(data);
    }

    @FXML
    public void addOrUpdateGoal() {
        try {
            if (txtName.getText().isEmpty()) {
                showError("Goal name is required.");
                return;
            }

            if (editMode && selectedGoal != null) {
                selectedGoal.setName(txtName.getText());
                selectedGoal.setTargetAmount(Double.parseDouble(txtTarget.getText()));
                selectedGoal.setCurrentAmount(Double.parseDouble(txtCurrent.getText()));
                selectedGoal.setStartDate(dpStart.getValue());
                selectedGoal.setTargetDate(dpTarget.getValue());

                service.updateGoal(selectedGoal);
                showInfo("Goal updated successfully.");
            } else {
                SavingsGoal g = new SavingsGoal();
                g.setUserId(1); // same pattern used in BudgetController
                g.setName(txtName.getText());
                g.setTargetAmount(Double.parseDouble(txtTarget.getText()));
                g.setCurrentAmount(Double.parseDouble(txtCurrent.getText()));
                g.setStartDate(dpStart.getValue());
                g.setTargetDate(dpTarget.getValue());
                g.setDeleteFlag(false);

                service.addGoal(g);
                showInfo("Goal added successfully.");
            }

            loadGoals();
            clearForm();
        } catch (Exception e) {
            showError("Error saving goal: " + e.getMessage());
        }
    }

    @FXML
    public void clearForm() {
        txtName.clear();
        txtTarget.clear();
        txtCurrent.clear();
        dpStart.setValue(null);
        dpTarget.setValue(null);
        tblGoals.getSelectionModel().clearSelection();
        editMode = false;
        selectedGoal = null;
        btnAdd.setText("Add");
        btnAdd.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
