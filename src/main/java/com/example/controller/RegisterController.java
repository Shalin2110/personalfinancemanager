package com.example.controller;

import com.example.service.UserService;
import com.example.Main;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtEmail;
    @FXML private Button btnRegister;
    @FXML private Button btnBackToLogin;
    @FXML private Label lblStatus;

    @FXML
    public void initialize() {
        lblStatus.setText("");
    }

    @FXML
    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        String email = txtEmail.getText().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters long.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters long.");
            return;
        }

        try {
            boolean success = UserService.registerUser(username, password, email);
            if (success) {
                showSuccess("Registration successful! Please login.");
                clearForm();
                Main.showLogin();
            } else {
                showError("Registration failed. Username may already exist.");
            }
        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        Main.showLogin();
    }

    private void clearForm() {
        txtUsername.clear();
        txtPassword.clear();
        txtConfirmPassword.clear();
        txtEmail.clear();
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