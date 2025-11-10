package com.example.db;

import com.example.model.SyncLog;
import com.example.service.SyncLogService;

import java.time.LocalDateTime;
import java.util.UUID;

public class SyncManager {

    private static final SyncLogService logService = new SyncLogService();

    public static String startSync(String tableName, String existingTxnId) {
        String txnId;

        if (existingTxnId != null) {
            // Use existing transaction ID if provided
            txnId = existingTxnId;
        } else if ("expense".equalsIgnoreCase(tableName)) {
            // Only expense table uses UUIDs
            txnId = UUID.randomUUID().toString();
        } else {
            // For other tables, we need the record ID - this will be set later
            // For now, use a placeholder that will be updated when we have the actual ID
            txnId = "PENDING_" + tableName.toUpperCase();
        }

        SyncLog log = new SyncLog();
        log.setDeviceTxnId(txnId);
        log.setTableName(tableName);
        log.setStatus("PENDING");
        log.setLastAttempt(LocalDateTime.now());
        log.setRetries(0);

        logService.recordNewSync(log);
        return txnId;
    }

    // NEW METHOD: Update the device_txn_id with the actual record ID
    public static void updateDeviceTxnId(String oldTxnId, String newTxnId, String tableName) {
        if (!"expense".equalsIgnoreCase(tableName)) {
            // Only update for non-expense tables (they should use record IDs, not UUIDs)
            logService.updateDeviceTxnId(oldTxnId, newTxnId);
            System.out.println("[SyncManager] Updated device_txn_id from " + oldTxnId + " to " + newTxnId + " for " + tableName);
        }
    }

    public static void markSuccess(String txnId) {
        logService.updateSyncStatus(txnId, "SUCCESS");
        System.out.println("[SyncManager] Sync successful for " + txnId);
    }

    public static void markFailure(String txnId, String error) {
        logService.incrementRetryCount(txnId);
        logService.updateSyncStatus(txnId, "FAILED");
        System.err.println("[SyncManager] Sync failed for " + txnId + ": " + error);
    }
}