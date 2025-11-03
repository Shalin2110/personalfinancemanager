package com.example.service;

import com.example.dao.SyncLogDao;
import com.example.model.SyncLog;

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
}
