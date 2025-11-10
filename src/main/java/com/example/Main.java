package com.example;

import com.example.dao.SyncLogDao;
import com.example.db.OracleConnection;
import com.example.db.SQLiteConnection;
import com.example.sync.AutoSyncWorker;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Start background services
        AutoSyncWorker.start();
        testDatabaseConnections();

        // Configure stage for better window management
        configureStage();

        // Start with login screen instead of dashboard
        showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Configure stage properties for better window management
    private void configureStage() {
        primaryStage.setMaximized(true); // Start maximized
        primaryStage.centerOnScreen();   // Center on screen

        // Optional: Set minimum size
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
    }

    // Test database connections on startup
    private void testDatabaseConnections() {
        System.out.println("\n🔍 Testing Database Connections...");

        boolean sqliteOK = SQLiteConnection.testConnection();
        boolean oracleOK = OracleConnection.testConnection();

        System.out.println("\n📊 Connection Test Results:");
        System.out.println("SQLite: " + (sqliteOK ? "✅ SUCCESS" : "❌ FAILED"));
        System.out.println("Oracle: " + (oracleOK ? "✅ SUCCESS" : "❌ FAILED"));

        if (!sqliteOK && !oracleOK) {
            System.err.println("🚨 CRITICAL: No database connections available!");
        }
    }

    // Navigation methods for authentication
    public static void showLogin() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            primaryStage.setTitle("Personal Finance Manager - Login");
            primaryStage.setScene(scene);

            // Center login window (smaller than fullscreen)
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading login screen: " + e.getMessage());
            // Fallback to dashboard if login screen fails
            showDashboard();
        }
    }

    public static void showRegister() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/register.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            primaryStage.setTitle("Personal Finance Manager - Register");
            primaryStage.setScene(scene);

            // Center register window
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading register screen: " + e.getMessage());
            showLogin(); // Fallback to login
        }
    }

    public static void showDashboard() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/dashboard.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
            primaryStage.setTitle("Personal Finance Manager");
            primaryStage.setScene(scene);

            // Dashboard opens maximized and centered
            primaryStage.setMaximized(true);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Optional: Method to handle logout
    public static void handleLogout() {
        com.example.service.UserService.logoutUser();
        showLogin();
    }

    // Override stop method to clean up resources
    @Override
    public void stop() throws Exception {
        // Stop background services
        AutoSyncWorker.stop();
        super.stop();
        System.out.println("Application shutting down...");
    }
}