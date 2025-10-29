package com.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {

    private static Connection connection = null;

    // Create connection
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DBConfig.SQLITE_DRIVER);
                connection = DriverManager.getConnection(DBConfig.SQLITE_URL);
                System.out.println("✅ Connected to SQLite database.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ SQLite connection failed: " + e.getMessage());
        }
        return connection;
    }

    // Close connection
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔌 SQLite connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error closing SQLite connection: " + e.getMessage());
        }
    }
}
