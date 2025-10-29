package com.example;

import com.example.db.OracleConnection;
import com.example.db.SQLiteConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        testDatabaseConnections();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Personal Finance Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
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
}
