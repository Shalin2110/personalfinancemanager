package com.example.controller;

import com.example.service.ReportService;
import com.example.db.OracleConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import java.sql.Connection;

public class ReportController {

    private final ReportService reportService = new ReportService();

    @FXML
    public void generateBudgetReport() {
        try (Connection conn = OracleConnection.getConnection()) {
            reportService.generateBudgetAdherenceReport(conn);
        } catch (Exception e) {
            showError("Failed to generate Budget report: " + e.getMessage());
        }
    }

    @FXML
    public void generateSavingsReport() {
        try (Connection conn = OracleConnection.getConnection()) {
            reportService.generateSavingsProgressReport(conn);
        } catch (Exception e) {
            showError("Failed to generate Savings report: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Report Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
