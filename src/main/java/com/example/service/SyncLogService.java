package com.example.service;

import com.example.dao.SyncLogDao;
import com.example.db.SQLiteConnection;
import com.example.model.SyncLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SyncLogService {
    private final SyncLogDao dao = new SyncLogDao();

    public void recordNewSync(SyncLog log) {
        try {
            dao.addSyncLog(log);
        } catch (SQLException e) {
            System.err.println("[SyncLogService] Error adding sync log: " + e.getMessage());
        }
    }

    public void recordNewSync(String tableName) {
        SyncLog log = new SyncLog();
        log.setDeviceTxnId(java.util.UUID.randomUUID().toString());
        log.setTableName(tableName);
        log.setStatus("PENDING");
        log.setLastAttempt(LocalDateTime.now());
        log.setRetries(0);
        recordNewSync(log);
    }

    // Update status (SUCCESS or FAILED)
    public void updateSyncStatus(String deviceTxnId, String status) {
        try {
            dao.updateStatus(deviceTxnId, status, LocalDateTime.now());
        } catch (SQLException e) {
            System.err.println("[SyncLogService] Error updating sync status: " + e.getMessage());
        }
    }

    // Increment retry counter on failure
    public void incrementRetryCount(String deviceTxnId) {
        try {
            dao.incrementRetries(deviceTxnId);
        } catch (SQLException e) {
            System.err.println("[SyncLogService] Error incrementing retry count: " + e.getMessage());
        }
    }

    // Load all logs for report/UI
    public List<SyncLog> getAllLogs() {
        try {
            return dao.getAllLogs();
        } catch (SQLException e) {
            System.err.println("[SyncLogService] Error reading sync logs: " + e.getMessage());
            return List.of();
        }
    }

    // UPDATED: Improved device_txn_id update with UNIQUE constraint handling
    public void updateDeviceTxnId(String oldTxnId, String newTxnId) {
        try {
            // First, check if a record with the newTxnId already exists
            boolean recordExists = checkIfRecordExists(newTxnId);

            if (recordExists) {
                // If record exists, update the existing record instead of changing device_txn_id
                updateExistingRecord(oldTxnId, newTxnId);
            } else {
                // If no record exists, safely update the device_txn_id
                updateDeviceTxnIdSafely(oldTxnId, newTxnId);
            }

        } catch (SQLException e) {
            System.err.println("[SyncLogService] Error updating device_txn_id: " + e.getMessage());
        }
    }

    private boolean checkIfRecordExists(String txnId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sync_log WHERE device_txn_id = ?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txnId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void updateDeviceTxnIdSafely(String oldTxnId, String newTxnId) throws SQLException {
        String sql = "UPDATE sync_log SET device_txn_id = ? WHERE device_txn_id = ?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newTxnId);
            ps.setString(2, oldTxnId);
            int rowsUpdated = ps.executeUpdate();
            System.out.println("[SyncLogService] Successfully updated device_txn_id from " + oldTxnId + " to " + newTxnId + " (" + rowsUpdated + " rows)");

            // Sync the updated record to Oracle
            syncUpdatedRecordToOracle(newTxnId);
        }
    }

    private void updateExistingRecord(String oldTxnId, String newTxnId) throws SQLException {
        // Get the data from the old record
        String selectSql = "SELECT * FROM sync_log WHERE device_txn_id = ?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
            selectPs.setString(1, oldTxnId);
            try (ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    // Update the existing record with new data
                    String updateSql = "UPDATE sync_log SET table_name = ?, status = ?, last_attempt = ?, retries = ? WHERE device_txn_id = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setString(1, rs.getString("table_name"));
                        updatePs.setString(2, rs.getString("status"));
                        updatePs.setTimestamp(3, rs.getTimestamp("last_attempt"));
                        updatePs.setInt(4, rs.getInt("retries"));
                        updatePs.setString(5, newTxnId);
                        updatePs.executeUpdate();
                    }

                    // Delete the old record
                    String deleteSql = "DELETE FROM sync_log WHERE device_txn_id = ?";
                    try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                        deletePs.setString(1, oldTxnId);
                        deletePs.executeUpdate();
                    }

                    System.out.println("[SyncLogService] Merged and updated existing record: " + newTxnId);

                    // Sync the updated record to Oracle
                    syncUpdatedRecordToOracle(newTxnId);
                }
            }
        }
    }

    private void syncUpdatedRecordToOracle(String newTxnId) {
        try {
            // Get the updated log from SQLite
            SyncLog updatedLog = dao.getLogByTxnId(newTxnId);
            if (updatedLog != null) {
                // Sync to Oracle using the existing syncToOracle method
                dao.syncToOracle(updatedLog);
                System.out.println("[SyncLogService] Successfully synced updated device_txn_id to Oracle: " + newTxnId);
            } else {
                System.err.println("[SyncLogService] Could not find updated log with txnId: " + newTxnId);
            }
        } catch (Exception e) {
            System.err.println("[SyncLogService] Error syncing updated record to Oracle: " + e.getMessage());
        }
    }
}