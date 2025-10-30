package com.example.db;

import com.example.model.SyncLog;
import com.example.service.SyncLogService;

import java.time.LocalDateTime;
import java.util.UUID;

public class SyncManager {

    private static final SyncLogService logService = new SyncLogService();

    public static String startSync(String tableName) {
        String txnId = UUID.randomUUID().toString();
        SyncLog log = new SyncLog();
        log.setDeviceTxnId(txnId);
        log.setTableName(tableName);
        log.setStatus("PENDING");
        log.setLastAttempt(LocalDateTime.now());
        log.setRetries(0);
        logService.recordNewSync(tableName);
        return txnId;
    }

    public static void markSuccess(String txnId) {
        logService.updateSyncStatus(txnId, "SUCCESS");
    }

    public static void markFailure(String txnId, String error) {
        logService.updateSyncStatus(txnId, "FAILED");
        System.err.println("[SyncManager] Sync failed: " + error);
    }
}
