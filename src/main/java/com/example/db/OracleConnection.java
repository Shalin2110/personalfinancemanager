package com.example.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleConnection {

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !isConnectionValid(connection)) {
                Class.forName(DBConfig.ORACLE_DRIVER);
                connection = DriverManager.getConnection(
                        DBConfig.ORACLE_URL,
                        DBConfig.ORACLE_USER,
                        DBConfig.ORACLE_PASSWORD
                );

                // Validate the connection
                if (isConnectionValid(connection)) {
                    DatabaseMetaData meta = connection.getMetaData();
                    System.out.println("✅ Connected to Oracle: " + meta.getDatabaseProductName() +
                            " " + meta.getDatabaseProductVersion());
                } else {
                    System.err.println("❌ Oracle connection is invalid");
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Oracle JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Oracle connection failed: " + e.getMessage());
        }
        return connection;
    }

    private static boolean isConnectionValid(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(DBConfig.CONNECTION_TIMEOUT);
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean testConnection() {
        Connection testConn = null;
        try {
            Class.forName(DBConfig.ORACLE_DRIVER);
            testConn = DriverManager.getConnection(
                    DBConfig.ORACLE_URL,
                    DBConfig.ORACLE_USER,
                    DBConfig.ORACLE_PASSWORD
            );
            return isConnectionValid(testConn);
        } catch (Exception e) {
            System.err.println("❌ Oracle connection test failed: " + e.getMessage());
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
                System.out.println("🔌 Oracle connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error closing Oracle connection: " + e.getMessage());
        }
    }
}