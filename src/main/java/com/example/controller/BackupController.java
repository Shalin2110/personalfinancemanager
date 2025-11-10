package com.example.controller;

import com.example.util.BackupManager;
import com.example.util.CryptoUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.Optional;

public class BackupController {

    @FXML private ListView<String> backupListView;
    @FXML private VBox statusBox;
    @FXML private Label statusMessage;

    @FXML
    public void initialize() {
        refreshBackupList();
    }

    @FXML
    private void createBackup() {
        try {
            boolean success = BackupManager.backupSQLiteDatabase();
            if (success) {
                showStatus("✅ Backup created successfully!", "success");
                refreshBackupList();
            } else {
                showStatus("❌ Backup failed. Check console for details.", "error");
            }
        } catch (Exception e) {
            showStatus("❌ Error creating backup: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void showRestoreDialog() {
        String selectedBackup = backupListView.getSelectionModel().getSelectedItem();

        if (selectedBackup == null) {
            showStatus("⚠️ Please select a backup file to restore.", "warning");
            return;
        }

        // Confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Restore");
        alert.setHeaderText("Restore from Backup");
        alert.setContentText("Are you sure you want to restore from:\n" + selectedBackup +
                "\n\nThis will replace your current database!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            restoreBackup(selectedBackup);
        }
    }

    private void restoreBackup(String backupFilename) {
        try {
            boolean success = BackupManager.restoreSQLiteDatabase(backupFilename);
            if (success) {
                showStatus("✅ Database restored successfully from " + backupFilename, "success");
                refreshBackupList();

                // Show restart recommendation
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Restore Complete");
                infoAlert.setHeaderText("Database Restored");
                infoAlert.setContentText("The database has been restored successfully.\n" +
                        "For best results, restart the application.");
                infoAlert.showAndWait();
            } else {
                showStatus("❌ Restore failed. Check console for details.", "error");
            }
        } catch (Exception e) {
            showStatus("❌ Error restoring backup: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void testEncryption() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Test Encryption");
        dialog.setHeaderText("Test AES Encryption");
        dialog.setContentText("Enter text to encrypt:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            String originalText = result.get();

            try {
                String encrypted = CryptoUtil.encrypt(originalText);
                String decrypted = CryptoUtil.decrypt(encrypted);

                // Show results
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Encryption Test");
                alert.setHeaderText("Encryption/Decryption Results");
                alert.setContentText(
                        "Original: " + originalText + "\n" +
                                "Encrypted: " + encrypted + "\n" +
                                "Decrypted: " + decrypted + "\n" +
                                "Success: " + originalText.equals(decrypted)
                );
                alert.showAndWait();

            } catch (Exception e) {
                showStatus("❌ Encryption test failed: " + e.getMessage(), "error");
            }
        }
    }

    private void refreshBackupList() {
        String[] backups = BackupManager.getAvailableBackups();
        backupListView.getItems().clear();
        if (backups.length > 0) {
            backupListView.getItems().addAll(backups);
        }
    }

    private void showStatus(String message, String type) {
        statusMessage.setText(message);

        switch (type) {
            case "success":
                statusBox.setStyle("-fx-background-color: #E8F5E8; -fx-border-color: #27AE60;");
                statusMessage.setStyle("-fx-text-fill: #27AE60;");
                break;
            case "error":
                statusBox.setStyle("-fx-background-color: #FFE8E6; -fx-border-color: #E74C3C;");
                statusMessage.setStyle("-fx-text-fill: #E74C3C;");
                break;
            case "warning":
                statusBox.setStyle("-fx-background-color: #FFF3CD; -fx-border-color: #FFC107;");
                statusMessage.setStyle("-fx-text-fill: #856404;");
                break;
        }

        statusBox.setVisible(true);

        // Auto-hide success messages after 5 seconds
        if ("success".equals(type)) {
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    javafx.application.Platform.runLater(() -> statusBox.setVisible(false));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
}