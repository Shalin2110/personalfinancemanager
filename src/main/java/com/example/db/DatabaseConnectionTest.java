package com.example.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnectionTest {

    public static void main(String[] args) {
        testSQLiteConnection();
        testOracleConnection();
    }

    public static void testSQLiteConnection() {
        System.out.println("\n=== Testing SQLite Connection ===");
        Connection conn = null;
        try {
            conn = SQLiteConnection.getConnection();

            if (conn != null && !conn.isClosed()) {
                // Test 1: Basic connection check
                System.out.println("✅ SQLite Connection established successfully");

                // Test 2: Get database metadata
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("📊 SQLite Database: " + metaData.getDatabaseProductName());
                System.out.println("🔢 SQLite Version: " + metaData.getDatabaseProductVersion());
                System.out.println("👤 SQLite Driver: " + metaData.getDriverName());

                // Test 3: Execute a simple query
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1 as test_value");
                if (rs.next()) {
                    System.out.println("✅ SQLite test query executed successfully");
                }
                rs.close();
                stmt.close();

                // Test 4: Check if tables exist (optional)
                checkSQLiteTables(conn);

            } else {
                System.out.println("❌ SQLite Connection is null or closed");
            }

        } catch (Exception e) {
            System.err.println("❌ SQLite Connection test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("⚠️ Error closing SQLite connection: " + e.getMessage());
                }
            }
        }
    }

    public static void testOracleConnection() {
        System.out.println("\n=== Testing Oracle Connection ===");
        Connection conn = null;
        try {
            conn = OracleConnection.getConnection();

            if (conn != null && !conn.isClosed()) {
                // Test 1: Basic connection check
                System.out.println("✅ Oracle Connection established successfully");

                // Test 2: Get database metadata
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("📊 Oracle Database: " + metaData.getDatabaseProductName());
                System.out.println("🔢 Oracle Version: " + metaData.getDatabaseProductVersion());
                System.out.println("👤 Oracle Driver: " + metaData.getDriverName());
                System.out.println("🔗 Oracle URL: " + metaData.getURL());
                System.out.println("👤 Oracle User: " + metaData.getUserName());

                // Test 3: Execute a simple query
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL");
                if (rs.next()) {
                    System.out.println("✅ Oracle test query executed successfully");
                }
                rs.close();
                stmt.close();

                // Test 4: Check Oracle instance details
                checkOracleInstance(conn);

            } else {
                System.out.println("❌ Oracle Connection is null or closed");
            }

        } catch (Exception e) {
            System.err.println("❌ Oracle Connection test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("⚠️ Error closing Oracle connection: " + e.getMessage());
                }
            }
        }
    }

    private static void checkSQLiteTables(Connection conn) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            System.out.println("📋 Existing SQLite Tables:");
            boolean hasTables = false;
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("   - " + tableName);
                hasTables = true;
            }
            if (!hasTables) {
                System.out.println("   No tables found in SQLite database");
            }
            tables.close();
        } catch (SQLException e) {
            System.out.println("ℹ️ Could not retrieve table information: " + e.getMessage());
        }
    }

    private static void checkOracleInstance(Connection conn) {
        try {
            Statement stmt = conn.createStatement();

            // Check Oracle instance name
            ResultSet rs = stmt.executeQuery("SELECT INSTANCE_NAME, STATUS FROM V$INSTANCE");
            if (rs.next()) {
                System.out.println("🏢 Oracle Instance: " + rs.getString("INSTANCE_NAME"));
                System.out.println("📈 Instance Status: " + rs.getString("STATUS"));
            }
            rs.close();

            // Check available tablespaces (optional)
            rs = stmt.executeQuery(
                    "SELECT TABLESPACE_NAME, STATUS FROM USER_TABLESPACES WHERE TABLESPACE_NAME IN ('SYSTEM', 'USERS')"
            );
            System.out.println("💾 Key Tablespaces:");
            while (rs.next()) {
                System.out.println("   - " + rs.getString("TABLESPACE_NAME") +
                        " (" + rs.getString("STATUS") + ")");
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.out.println("ℹ️ Could not retrieve Oracle instance details: " + e.getMessage());
        }
    }
}