package com.example.controller;

import com.example.service.UserService;
import com.example.Main;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Button btnRegister;
    @FXML private Label lblStatus;

    @FXML
    public void initialize() {
        // Clear status message
        lblStatus.setText("");
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        try {
            boolean success = UserService.loginUser(username, password);
            if (success) {
                showSuccess("Login successful!");
                // Navigate to main dashboard
                Main.showDashboard();
            } else {
                showError("Invalid username or password.");
            }
        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        Main.showRegister();
    }

    private void showError(String message) {
        lblStatus.setText("❌ " + message);
        lblStatus.setStyle("-fx-text-fill: #E53935; -fx-font-weight: bold;");
    }

    private void showSuccess(String message) {
        lblStatus.setText("✅ " + message);
        lblStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
    }
}