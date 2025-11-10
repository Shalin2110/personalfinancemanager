package com.example.controller;

import com.example.Main;
import com.example.sync.AutoSyncWorker;
import com.example.sync.ConnectionMonitor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DashboardController {

    @FXML private Label lblConnectionStatus;
    @FXML private StackPane contentArea;
    @FXML private Circle connectionIndicator;

    private ConnectionMonitor connectionMonitor;
    private static DashboardController currentInstance;

    @FXML
    public void initialize() {
        currentInstance = this;

        // Initialize connection monitor
        connectionMonitor = ConnectionMonitor.getInstance();

        // Remove the binding and use manual updates instead
        connectionMonitor.isOnlineProperty().addListener((observable, oldValue, newValue) -> {
            updateConnectionStatus(newValue);
        });

        // Initial style setup
        updateConnectionStatus(connectionMonitor.isOnline());

        openDashboard();
    }

    private void updateConnectionStatus(boolean isOnline) {
        if (isOnline) {
            lblConnectionStatus.setText("Online");
            lblConnectionStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            connectionIndicator.setFill(Color.LIMEGREEN);
        } else {
            lblConnectionStatus.setText("Offline");
            lblConnectionStatus.setStyle("-fx-text-fill: #E53935; -fx-font-weight: bold;");
            connectionIndicator.setFill(Color.RED);
        }
    }

    // Remove the old checkConnectionStatus method since we're now using real-time monitoring

    private void loadPage(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            contentArea.getChildren().setAll(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not load " + fxml + ": " + e.getMessage());
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
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not load dashboard: " + e.getMessage());
        }
    }

    @FXML
    public void openBudgets() {
        loadPage("budget.fxml");
    }

    @FXML
    public void openGoals() {
        loadPage("savings_goal.fxml");
    }

    @FXML
    public void openExpenses() {
        loadPage("expense.fxml");
    }

    @FXML
    public void openAccounts() {
        loadPage("account.fxml");
    }

    @FXML
    public void openCategories() {
        loadPage("category.fxml");
    }

    @FXML
    public void openReports() {
        loadPage("reports.fxml");
    }

    @FXML
    public void openSyncLogs() {
        loadPage("sync_log.fxml");
    }

    @FXML
    public void openBackup() {
        loadPage("backup.fxml");
    }

    @FXML
    public void retrySync() {
        // Manually trigger auto-sync reattempt
        AutoSyncWorker.forceRetryNow();
        openSyncLogs(); // refresh table
    }

    @FXML
    public void handleLogout() {
        // Stop monitoring when logging out
        if (connectionMonitor != null) {
            connectionMonitor.stopMonitoring();
        }

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

    // Cleanup when controller is destroyed
    public void shutdown() {
        if (connectionMonitor != null) {
            connectionMonitor.stopMonitoring();
        }
    }

    // Helper method for showing alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Static method to get current instance for cleanup
    public static DashboardController getCurrentInstance() {
        return currentInstance;
    }
}