package com.example.service;

import java.util.UUID;

public class SyncHelper {

    /**
     * Generates the correct device_txn_id based on table name
     * For expense: uses UUID
     * For other tables: uses the provided record ID
     */
    public static String generateDeviceTxnId(String tableName, int recordId) {
        if ("expense".equalsIgnoreCase(tableName)) {
            // Only expense table uses UUIDs
            return UUID.randomUUID().toString();
        } else {
            // All other tables use their primary key ID
            return String.valueOf(recordId);
        }
    }
}