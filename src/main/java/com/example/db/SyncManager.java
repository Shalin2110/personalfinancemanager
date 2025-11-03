package com.example.db;

import com.example.model.SyncLog;
import com.example.service.SyncLogService;

import java.time.LocalDateTime;
import java.util.UUID;

public class SyncManager {

    private static final SyncLogService logService = new SyncLogService();

    public static String startSync(String tableName, String existingTxnId) {
        String txnId = (existingTxnId != null) ? existingTxnId : UUID.randomUUID().toString();

        SyncLog log = new SyncLog();
        log.setDeviceTxnId(txnId);
        log.setTableName(tableName);
        log.setStatus("PENDING");
        log.setLastAttempt(LocalDateTime.now());
        log.setRetries(0);

        logService.recordNewSync(log);
        return txnId;
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
