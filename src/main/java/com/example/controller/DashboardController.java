package com.example.controller;

import com.example.Main;
import com.example.sync.AutoSyncWorker;
import com.example.sync.ConnectivityChecker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class DashboardController {

    @FXML private Label lblConnectionStatus;
    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        checkConnectionStatus();
        openDashboard();
    }

    private void checkConnectionStatus() {
        boolean online = ConnectivityChecker.isOracleOnline();
        if (online) {
            lblConnectionStatus.setText("Online");
            lblConnectionStatus.setStyle("-fx-text-fill: #4CAF50;");
        } else {
            lblConnectionStatus.setText("Offline");
            lblConnectionStatus.setStyle("-fx-text-fill: #E53935;");
        }
    }

    private void loadPage(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            contentArea.getChildren().setAll(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard_home.fxml"));
            Parent root = loader.load();

            // Get the controller and pass a reference to this dashboard controller
            DashboardHomeController homeController = loader.getController();
            homeController.setDashboardController(this);

            contentArea.getChildren().setAll(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML public void openBudgets() { loadPage("budget.fxml"); }
    @FXML public void openGoals() { loadPage("savings_goal.fxml"); }
    @FXML public void openExpenses() { loadPage("expense.fxml"); }
    @FXML public void openAccounts() { loadPage("account.fxml"); }
    @FXML public void openCategories() { loadPage("category.fxml"); }
    @FXML public void openReports() { loadPage("reports.fxml"); }
    @FXML public void openSyncLogs() { loadPage("sync_log.fxml"); }

    @FXML
    public void retrySync() {
        // Manually trigger auto-sync reattempt
        AutoSyncWorker.forceRetryNow();
        checkConnectionStatus();
        openSyncLogs(); // refresh table
    }

    @FXML
    public void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Confirm Logout");
        confirm.setContentText("Are you sure you want to logout?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Main.handleLogout();
            }
        });
    }
}