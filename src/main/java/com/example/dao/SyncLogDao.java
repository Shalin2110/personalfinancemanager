package com.example.dao;

import com.example.db.OracleConnection;
import com.example.model.SyncLog;
import com.example.db.SQLiteConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class SyncLogDao {

    // Robust method to parse ANY timestamp format
    private LocalDateTime safeParseTimestamp(Object timestampValue) {
        if (timestampValue == null) {
            return null;
        }

        try {
            // If it's already a Timestamp
            if (timestampValue instanceof Timestamp) {
                return ((Timestamp) timestampValue).toLocalDateTime();
            }

            // If it's a String, try multiple formats
            if (timestampValue instanceof String) {
                String timestampStr = ((String) timestampValue).trim();
                if (timestampStr.isEmpty()) {
                    return null;
                }

                // Try common SQLite timestamp formats
                DateTimeFormatter[] formatters = {
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                };

                for (DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDateTime.parse(timestampStr, formatter);
                    } catch (DateTimeParseException e) {
                        // Try next formatter
                    }
                }

                // Final fallback - use JDBC
                try {
                    return Timestamp.valueOf(timestampStr).toLocalDateTime();
                } catch (Exception e) {
                    System.err.println("[SyncLogDao] Cannot parse timestamp: " + timestampStr);
                }
            }
        } catch (Exception e) {
            System.err.println("[SyncLogDao] Error parsing timestamp: " + e.getMessage());
        }

        return null;
    }

    // Retrieve all logs - BULLETPROOF VERSION
    public List<SyncLog> getAllLogs() throws SQLException {
        List<SyncLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM sync_log ORDER BY last_attempt DESC";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                try {
                    SyncLog log = new SyncLog();
                    log.setSyncId(rs.getInt("sync_id"));
                    log.setDeviceTxnId(rs.getString("device_txn_id"));
                    log.setTableName(rs.getString("table_name"));
                    log.setStatus(rs.getString("status"));

                    // BULLETPROOF: Get the raw object and parse safely
                    Object timestampObj = rs.getObject("last_attempt");
                    log.setLastAttempt(safeParseTimestamp(timestampObj));

                    log.setRetries(rs.getInt("retries"));
                    logs.add(log);
                } catch (Exception e) {
                    System.err.println("[SyncLogDao] Error processing row: " + e.getMessage());
                    // Continue with next row instead of failing completely
                }
            }
        }
        return logs;
    }

    // Apply the same safe approach to other methods
    private SyncLog getLogByTxnId(String txnId) throws SQLException {
        String sql = "SELECT * FROM sync_log WHERE device_txn_id = ?";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SyncLog log = new SyncLog();
                    log.setSyncId(rs.getInt("sync_id"));
                    log.setDeviceTxnId(rs.getString("device_txn_id"));
                    log.setTableName(rs.getString("table_name"));
                    log.setStatus(rs.getString("status"));

                    Object timestampObj = rs.getObject("last_attempt");
                    log.setLastAttempt(safeParseTimestamp(timestampObj));

                    log.setRetries(rs.getInt("retries"));
                    return log;
                }
            }
        }
        return null;
    }

    // Get logs that need retry (PENDING or FAILED)
    public List<SyncLog> getFailedLogs() throws SQLException {
        List<SyncLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM sync_log WHERE status IN ('FAILED','PENDING')";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                try {
                    SyncLog log = new SyncLog();
                    log.setSyncId(rs.getInt("sync_id"));
                    log.setDeviceTxnId(rs.getString("device_txn_id"));
                    log.setTableName(rs.getString("table_name"));
                    log.setStatus(rs.getString("status"));

                    Object timestampObj = rs.getObject("last_attempt");
                    log.setLastAttempt(safeParseTimestamp(timestampObj));

                    log.setRetries(rs.getInt("retries"));
                    logs.add(log);
                } catch (Exception e) {
                    System.err.println("[SyncLogDao] Error processing failed log row: " + e.getMessage());
                }
            }
        }
        return logs;
    }

    // Keep all your other methods the same (addSyncLog, updateStatus, incrementRetries, etc.)
    public void addSyncLog(SyncLog log) throws SQLException {
        String sql = "INSERT INTO sync_log (device_txn_id, table_name, status, last_attempt, retries) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, log.getDeviceTxnId());
            ps.setString(2, log.getTableName());
            ps.setString(3, log.getStatus());
            ps.setTimestamp(4, log.getLastAttempt() != null ?
                    Timestamp.valueOf(log.getLastAttempt()) : null);
            ps.setInt(5, log.getRetries());
            ps.executeUpdate();
        }

        syncToOracle(log);
    }

    public void updateStatus(String deviceTxnId, String status, LocalDateTime lastAttempt) throws SQLException {
        String sql = "UPDATE sync_log SET status = ?, last_attempt = ? WHERE device_txn_id = ?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setTimestamp(2, lastAttempt != null ? Timestamp.valueOf(lastAttempt) : null);
            ps.setString(3, deviceTxnId);
            ps.executeUpdate();
        }

        SyncLog log = getLogByTxnId(deviceTxnId);
        if (log != null) {
            log.setStatus(status);
            log.setLastAttempt(lastAttempt);
            syncToOracle(log);
        }
    }

    public void incrementRetries(String deviceTxnId) throws SQLException {
        String sql = "UPDATE sync_log SET retries = retries + 1, last_attempt = CURRENT_TIMESTAMP " +
                "WHERE device_txn_id = ?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, deviceTxnId);
            ps.executeUpdate();
        }

        SyncLog log = getLogByTxnId(deviceTxnId);
        if (log != null) {
            log.setRetries(log.getRetries() + 1);
            log.setLastAttempt(LocalDateTime.now());
            syncToOracle(log);
        }
    }

    public void syncToOracle(SyncLog log) {
        String sql = "{ call proc_sync_sync_log(?, ?, ?, ?, ?) }";

        try (Connection conn = OracleConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, log.getDeviceTxnId());
            cs.setString(2, log.getTableName());
            cs.setString(3, log.getStatus());
            cs.setTimestamp(4, log.getLastAttempt() != null ? Timestamp.valueOf(log.getLastAttempt()) : null);
            cs.setInt(5, log.getRetries());

            cs.execute();
        } catch (Exception e) {
            System.err.println("[SyncLogDao] Oracle sync failed: " + e.getMessage());
        }
    }

    public void resyncRecord(SyncLog log) {
        try {
            syncToOracle(log);
            updateStatus(log.getDeviceTxnId(), "SUCCESS", LocalDateTime.now());
            System.out.println("[AutoSyncWorker] Resynced: " + log.getDeviceTxnId());
        } catch (Exception e) {
            System.err.println("[AutoSyncWorker] Retry failed for " + log.getDeviceTxnId());
        }
    }

    public int countPending() {
        String sql = "SELECT COUNT(*) FROM sync_log WHERE status = 'PENDING'";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}