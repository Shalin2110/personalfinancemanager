package com.example.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !isConnectionValid(connection)) {
                Class.forName(DBConfig.SQLITE_DRIVER);
                connection = DriverManager.getConnection(DBConfig.SQLITE_URL);

                // Validate the connection
                if (isConnectionValid(connection)) {
                    DatabaseMetaData meta = connection.getMetaData();
                    System.out.println("✅ Connected to SQLite: " + meta.getDatabaseProductName() +
                            " " + meta.getDatabaseProductVersion());
                } else {
                    System.err.println("❌ SQLite connection is invalid");
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ SQLite connection failed: " + e.getMessage());
        }
        return connection;
    }

    private static boolean isConnectionValid(Connection conn) {
        try {
            if (conn == null || conn.isClosed()) {
                return false;
            }
            // For SQLite, try a simple query
            conn.createStatement().execute("SELECT 1");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean testConnection() {
        Connection testConn = null;
        try {
            Class.forName(DBConfig.SQLITE_DRIVER);
            testConn = DriverManager.getConnection(DBConfig.SQLITE_URL);
            return isConnectionValid(testConn);
        } catch (Exception e) {
            System.err.println("❌ SQLite connection test failed: " + e.getMessage());
            return false;
        } finally {
            if (testConn != null) {
                try { testConn.close(); } catch (SQLException e) { }
            }
        }
    }

    // Close connection method remains the same
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