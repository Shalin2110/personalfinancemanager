package com.example.service;

import com.example.dao.SyncLogDao;
import com.example.model.SyncLog;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SyncLogService {
    private final SyncLogDao dao = new SyncLogDao();

    // ✅ Add new sync attempt
    public void recordNewSync(String tableName) {
        try {
            SyncLog log = new SyncLog();
            log.setDeviceTxnId(UUID.randomUUID().toString());
            log.setTableName(tableName);
            log.setStatus("PENDING");
            log.setLastAttempt(LocalDateTime.now());
            log.setRetries(0);
            dao.addSyncLog(log);
        } catch (SQLException e) {
            System.err.println("Error recording sync: " + e.getMessage());
        }
    }

    // ✅ Update status (SUCCESS/FAILED)
    public void updateSyncStatus(String deviceTxnId, String status) {
        try {
            dao.updateStatus(deviceTxnId, status);
        } catch (SQLException e) {
            System.err.println("Error updating sync log: " + e.getMessage());
        }
    }

    // ✅ Load all logs
    public List<SyncLog> getAllLogs() {
        try {
            return dao.getAllLogs();
        } catch (SQLException e) {
            System.err.println("Error reading sync logs: " + e.getMessage());
            return List.of();
        }
    }
}
