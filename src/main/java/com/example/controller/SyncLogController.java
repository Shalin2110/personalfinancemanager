package com.example.controller;

import com.example.model.SyncLog;
import com.example.service.SyncLogService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.time.format.DateTimeFormatter;

public class SyncLogController {

    @FXML private TableView<SyncLog> tblLogs;
    @FXML private TableColumn<SyncLog, Integer> colId, colRetries;
    @FXML private TableColumn<SyncLog, String> colTxn, colTable, colStatus, colTime;

    private final SyncLogService service = new SyncLogService();
    private final ObservableList<SyncLog> data = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getSyncId()).asObject());
        colTxn.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getDeviceTxnId()));
        colTable.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getTableName()));

        // Status with color based on state
        colStatus.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getStatus()));
        colStatus.setCellFactory(getStatusColorCellFactory());

        // Format display time
        colTime.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getLastAttempt() != null ? cd.getValue().getLastAttempt().format(formatter) : "---"
        ));

        colRetries.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getRetries()).asObject());

        loadLogs();
    }

    @FXML
    public void loadLogs() {
        data.setAll(service.getAllLogs());
        tblLogs.setItems(data);
    }

    // Cell Factory: Color code the status column
    private Callback<TableColumn<SyncLog, String>, TableCell<SyncLog, String>> getStatusColorCellFactory() {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(status);

                switch (status) {
                    case "SUCCESS" -> setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    case "FAILED" -> setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    case "PENDING" -> setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        };
    }
}
