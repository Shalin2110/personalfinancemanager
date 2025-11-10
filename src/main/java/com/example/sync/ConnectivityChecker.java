package com.example.sync;

import com.example.db.OracleConnection;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class ConnectivityChecker {

    public static boolean isOracleOnline() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // Get a fresh connection each time to test real-time status
            conn = OracleConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                return false;
            }

            // Test with a simple query to ensure connection is actually working
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT 1 FROM DUAL");
            return rs.next(); // If we get a result, connection is truly alive

        } catch (Exception e) {
            System.out.println("❌ Oracle connectivity check failed: " + e.getMessage());
            return false;
        } finally {
            // Close resources properly
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                // Don't close conn here - let connection pool handle it
            } catch (Exception e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}