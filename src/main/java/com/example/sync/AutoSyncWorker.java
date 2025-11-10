package com.example.sync;

import com.example.dao.SyncLogDao;
import com.example.model.SyncLog;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AutoSyncWorker {

    private static final SyncLogDao dao = new SyncLogDao();
    private static Timer timer;

    public static void start() {
        if (timer != null) {
            timer.cancel(); // Cancel existing timer if any
        }

        timer = new Timer(true); // runs in background
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (ConnectivityChecker.isOracleOnline()) {
                    retryFailedAndPendingSync();
                }
            }
        }, 0, 10_000); // check every 10 seconds
    }

    public static void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private static void retryFailedAndPendingSync() {
        try {
            // Get both FAILED and PENDING logs
            List<SyncLog> pending = dao.getFailedAndPendingLogs();
            if (!pending.isEmpty()) {
                System.out.println("[AutoSyncWorker] Found " + pending.size() + " pending/failed syncs");
            }

            for (SyncLog log : pending) {
                boolean success = dao.attemptResyncRecord(log);
                if (success) {
                    System.out.println("[AutoSyncWorker] ✅ Successfully synced: " + log.getDeviceTxnId());
                } else {
                    System.out.println("[AutoSyncWorker] ❌ Failed to sync: " + log.getDeviceTxnId());
                }
            }
        } catch (Exception e) {
            System.err.println("[AutoSyncWorker] Error during sync: " + e.getMessage());
        }
    }

    public static void forceRetryNow() {
        if (ConnectivityChecker.isOracleOnline()) {
            retryFailedAndPendingSync();
        } else {
            System.out.println("[AutoSyncWorker] Oracle is offline - cannot sync");
        }
    }
}