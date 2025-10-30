package com.example.model;

import java.time.LocalDateTime;

public class SyncLog {
    private int syncId;
    private String deviceTxnId;
    private String tableName;
    private String status;
    private LocalDateTime lastAttempt;
    private int retries;

    public SyncLog() {}

    public SyncLog(int syncId, String deviceTxnId, String tableName, String status, LocalDateTime lastAttempt, int retries) {
        this.syncId = syncId;
        this.deviceTxnId = deviceTxnId;
        this.tableName = tableName;
        this.status = status;
        this.lastAttempt = lastAttempt;
        this.retries = retries;
    }

    public int getSyncId() { return syncId; }
    public void setSyncId(int syncId) { this.syncId = syncId; }

    public String getDeviceTxnId() { return deviceTxnId; }
    public void setDeviceTxnId(String deviceTxnId) { this.deviceTxnId = deviceTxnId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLastAttempt() { return lastAttempt; }
    public void setLastAttempt(LocalDateTime lastAttempt) { this.lastAttempt = lastAttempt; }

    public int getRetries() { return retries; }
    public void setRetries(int retries) { this.retries = retries; }
}
