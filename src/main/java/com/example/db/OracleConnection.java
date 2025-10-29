package com.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleConnection {

    private static Connection connection = null;

    // Create connection
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DBConfig.ORACLE_DRIVER);
                connection = DriverManager.getConnection(
                        DBConfig.ORACLE_URL,
                        DBConfig.ORACLE_USER,
                        DBConfig.ORACLE_PASSWORD
                );
                System.out.println("✅ Connected to Oracle database.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Oracle JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Oracle connection failed: " + e.getMessage());
        }
        return connection;
    }

    // Close connection
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔌 Oracle connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error closing Oracle connection: " + e.getMessage());
        }
    }
}
