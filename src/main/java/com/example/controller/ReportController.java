package com.example.controller;

import com.example.db.OracleConnection;
import com.example.service.ReportService;
import javafx.fxml.FXML;
import net.sf.jasperreports.engine.JRException;

import java.sql.Connection;

public class ReportController {

    private final ReportService reportService = new ReportService();

    @FXML
    private void handleMonthlyExpenditureReport() {
        run(conn -> reportService.showMonthlyExpenditureReport(conn));
    }

    @FXML
    private void handleBudgetAdherenceReport() {
        run(conn -> reportService.showBudgetAdherenceReport(conn));
    }

    @FXML
    private void handleSavingsGoalProgressReport() {
        run(conn -> reportService.showSavingsGoalProgressReport(conn));
    }

    @FXML
    private void handleCategoryExpenseDistributionReport() {
        run(conn -> reportService.showCategoryExpenseDistributionReport(conn));
    }

    @FXML
    private void handleForecastSavingsReport() {
        run(conn -> reportService.showForecastSavingsReport(conn));
    }

    @FXML
    private void handleSaveMonthlyExpenditurePDF() {
        run(conn -> reportService.saveMonthlyExpenditureReport(conn));
    }

    @FXML
    private void handleSaveBudgetAdherencePDF() {
        run(conn -> reportService.saveBudgetAdherenceReport(conn));
    }

    @FXML
    private void handleSaveSavingsGoalProgressPDF() {
        run(conn -> reportService.saveSavingsGoalProgressReport(conn));
    }

    @FXML
    private void handleSaveCategoryExpenseDistributionPDF() {
        run(conn -> reportService.saveCategoryExpenseDistributionReport(conn));
    }

    @FXML
    private void handleSaveForecastSavingsPDF() {
        run(conn -> reportService.saveForecastSavingsReport(conn));
    }

    private void run(ReportAction action) {
        try (Connection conn = OracleConnection.getConnection()) {
            action.execute(conn);
        } catch (JRException e) {
            System.err.println("❌ Jasper Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Report Error: " + e.getMessage());
        }
    }

    @FunctionalInterface
    interface ReportAction {
        void execute(Connection conn) throws JRException;
    }
}
