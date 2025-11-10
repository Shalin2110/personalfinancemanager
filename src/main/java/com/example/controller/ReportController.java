package com.example.controller;

import com.example.db.OracleConnection;
import com.example.service.ReportService;
import com.example.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import java.sql.Connection;

public class ReportController {

    private final ReportService reportService = new ReportService();

    @FXML
    private void handleMonthlyExpenditureReport() {
        run((conn, userId) -> {
            JasperPrint print = reportService.showMonthlyExpenditureReport(conn, userId);
            if (print.getPages().isEmpty()) {
                showInfoMessage("No Data Available",
                        "No expense data found for monthly expenditure analysis.\n\n" +
                                "Please add some expenses to generate this report.");
            }
        });
    }

    @FXML
    private void handleBudgetAdherenceReport() {
        run((conn, userId) -> {
            JasperPrint print = reportService.showBudgetAdherenceReport(conn, userId);
            if (print.getPages().isEmpty()) {
                showInfoMessage("No Data Available",
                        "No budget or expense data found for budget adherence tracking.\n\n" +
                                "Please set up budgets and add expenses to generate this report.");
            }
        });
    }

    @FXML
    private void handleSavingsGoalProgressReport() {
        run((conn, userId) -> {
            JasperPrint print = reportService.showSavingsGoalProgressReport(conn, userId);
            if (print.getPages().isEmpty()) {
                showInfoMessage("No Data Available",
                        "No savings goals found for progress tracking.\n\n" +
                                "Please create savings goals to generate this report.");
            }
        });
    }

    @FXML
    private void handleCategoryExpenseDistributionReport() {
        run((conn, userId) -> {
            JasperPrint print = reportService.showCategoryExpenseDistributionReport(conn, userId);
            if (print.getPages().isEmpty()) {
                showInfoMessage("No Data Available",
                        "No category expense data found for distribution analysis.\n\n" +
                                "Please add expenses with categories to generate this report.");
            }
        });
    }

    @FXML
    private void handleForecastSavingsReport() {
        run((conn, userId) -> {
            JasperPrint print = reportService.showForecastSavingsReport(conn, userId);
            if (print.getPages().isEmpty()) {
                showInfoMessage("No Data Available",
                        "Insufficient data for savings forecasting.\n\n" +
                                "Please add historical expense data to generate forecast reports.");
            }
        });
    }

    @FXML
    private void handleSaveMonthlyExpenditurePDF() {
        run((conn, userId) -> {
            boolean success = reportService.saveMonthlyExpenditureReport(conn, userId);
            if (success) {
                showSuccessMessage("Report Saved", "Monthly expenditure report saved successfully as PDF!");
            } else {
                showInfoMessage("No Data Available",
                        "No expense data found to save monthly expenditure report.\n\n" +
                                "Please add some expenses first.");
            }
        });
    }

    @FXML
    private void handleSaveBudgetAdherencePDF() {
        run((conn, userId) -> {
            boolean success = reportService.saveBudgetAdherenceReport(conn, userId);
            if (success) {
                showSuccessMessage("Report Saved", "Budget adherence report saved successfully as PDF!");
            } else {
                showInfoMessage("No Data Available",
                        "No budget data found to save budget adherence report.\n\n" +
                                "Please set up budgets and expenses first.");
            }
        });
    }

    @FXML
    private void handleSaveSavingsGoalProgressPDF() {
        run((conn, userId) -> {
            boolean success = reportService.saveSavingsGoalProgressReport(conn, userId);
            if (success) {
                showSuccessMessage("Report Saved", "Savings goal progress report saved successfully as PDF!");
            } else {
                showInfoMessage("No Data Available",
                        "No savings goals found to save progress report.\n\n" +
                                "Please create savings goals first.");
            }
        });
    }

    @FXML
    private void handleSaveCategoryExpenseDistributionPDF() {
        run((conn, userId) -> {
            boolean success = reportService.saveCategoryExpenseDistributionReport(conn, userId);
            if (success) {
                showSuccessMessage("Report Saved", "Category expense distribution report saved successfully as PDF!");
            } else {
                showInfoMessage("No Data Available",
                        "No category data found to save distribution report.\n\n" +
                                "Please add expenses with categories first.");
            }
        });
    }

    @FXML
    private void handleSaveForecastSavingsPDF() {
        run((conn, userId) -> {
            boolean success = reportService.saveForecastSavingsReport(conn, userId);
            if (success) {
                showSuccessMessage("Report Saved", "Forecast savings report saved successfully as PDF!");
            } else {
                showInfoMessage("No Data Available",
                        "Insufficient data to save forecast report.\n\n" +
                                "Please add historical expense data first.");
            }
        });
    }

    private void run(ReportAction action) {
        try (Connection conn = OracleConnection.getConnection()) {

            int userId = UserService.getCurrentUserId();
            if (userId <= 0) {
                showErrorMessage("Not Logged In",
                        "Please login before generating reports.");
                return;
            }

            action.execute(conn, userId);

        } catch (JRException e) {
            showErrorMessage("Report Generation Error",
                    "Unable to generate the report.");
        } catch (Exception e) {
            showErrorMessage("System Error",
                    "An unexpected error occurred while generating the report.");
        }
    }

    private void showInfoMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FunctionalInterface
    interface ReportAction {
        void execute(Connection conn, int userId) throws JRException;
    }
}
