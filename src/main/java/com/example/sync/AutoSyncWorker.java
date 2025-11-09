package com.example.sync;

import com.example.dao.SyncLogDao;
import com.example.model.SyncLog;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AutoSyncWorker {

    private static final SyncLogDao dao = new SyncLogDao();

    public static void start() {
        Timer timer = new Timer(true); // runs in background
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (ConnectivityChecker.isOracleOnline()) {
                    retryFailedSync();
                }
            }
        }, 0, 10_000); // check every 10 seconds
    }

    private static void retryFailedSync() {
        try {
            List<SyncLog> pending = dao.getFailedLogs();
            for (SyncLog log : pending) {
                dao.resyncRecord(log);
            }
        } catch (Exception ignored) {}
    }

    public static void forceRetryNow() {
        retryFailedSync();
    }
}
