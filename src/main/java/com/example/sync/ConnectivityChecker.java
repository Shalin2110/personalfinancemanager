package com.example.sync;

import com.example.db.OracleConnection;
import java.sql.Connection;

public class ConnectivityChecker {

    public static boolean isOracleOnline() {
        try (Connection conn = OracleConnection.getConnection()) {
            return conn != null; // If connection works, Oracle is online
        } catch (Exception e) {
            return false;
        }
    }
}