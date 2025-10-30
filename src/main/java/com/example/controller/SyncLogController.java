package com.example.controller;

import com.example.model.SyncLog;
import com.example.service.SyncLogService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SyncLogController {

    @FXML private TableView<SyncLog> tblLogs;
    @FXML private TableColumn<SyncLog, Integer> colId, colRetries;
    @FXML private TableColumn<SyncLog, String> colTxn, colTable, colStatus, colTime;

    private final SyncLogService service = new SyncLogService();
    private final ObservableList<SyncLog> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getSyncId()).asObject());
        colTxn.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getDeviceTxnId()));
        colTable.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getTableName()));
        colStatus.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getStatus()));
        colTime.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getLastAttempt().toString()));
        colRetries.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getRetries()).asObject());
        loadLogs();
    }

    @FXML
    public void loadLogs() {
        data.setAll(service.getAllLogs());
        tblLogs.setItems(data);
    }
}
