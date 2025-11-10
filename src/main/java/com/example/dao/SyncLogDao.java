// My SyncLogDao
package com.example.dao;

import com.example.db.OracleConnection;
import com.example.model.SyncLog;
import com.example.db.SQLiteConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class SyncLogDao {

    // Robust method to parse ANY timestamp format
    private LocalDateTime safeParseTimestamp(Object timestampValue) {
        if (timestampValue == null) {
            return null;
        }

        try {
            // If it's already a Timestamp
            if (timestampValue instanceof Timestamp) {
                return ((Timestamp) timestampValue).toLocalDateTime();
            }

            // If it's a String, try multiple formats
            if (timestampValue instanceof String) {
                String timestampStr = ((String) timestampValue).trim();
                if (timestampStr.isEmpty()) {
                    return null;
                }

                // Special handling for date-only strings (without time)
                if (timestampStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    try {
                        LocalDate localDate = LocalDate.parse(timestampStr);
                        return localDate.atStartOfDay(); // Convert to LocalDateTime at start of day
                    } catch (DateTimeParseException e) {
                        // Continue to other fallbacks
                    }
                }

                // Try common SQLite timestamp formats
                DateTimeFormatter[] formatters = {
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                        // ADDED DATE-ONLY FORMATTERS:
                        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                        DateTimeFormatter.ISO_LOCAL_DATE
                };

                for (DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDateTime.parse(timestampStr, formatter);
                    } catch (DateTimeParseException e) {
                        // Try next formatter
                    }
                }

                // Final fallback - use JDBC
                try {
                    return Timestamp.valueOf(timestampStr).toLocalDateTime();
                } catch (Exception e) {
                    System.err.println("[SyncLogDao] Cannot parse timestamp: " + timestampStr);
                }
            }
        } catch (Exception e) {
            System.err.println("[SyncLogDao] Error parsing timestamp: " + e.getMessage());
        }

        return null;
    }

    // Retrieve all logs - BULLETPROOF VERSION
    public List<SyncLog> getAllLogs() throws SQLException {
        List<SyncLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM sync_log ORDER BY last_attempt DESC";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                try {
                    SyncLog log = new SyncLog();
                    log.setSyncId(rs.getInt("sync_id"));
                    log.setDeviceTxnId(rs.getString("device_txn_id"));
                    log.setTableName(rs.getString("table_name"));
                    log.setStatus(rs.getString("status"));

                    // BULLETPROOF: Get the raw object and parse safely
                    Object timestampObj = rs.getObject("last_attempt");
                    log.setLastAttempt(safeParseTimestamp(timestampObj));

                    log.setRetries(rs.getInt("retries"));
                    logs.add(log);
                } catch (Exception e) {
                    System.err.println("[SyncLogDao] Error processing row: " + e.getMessage());
                    // Continue with next row instead of failing completely
                }
            }
        }
        return logs;
    }

    // Get both FAILED and PENDING logs for auto-sync
    public List<SyncLog> getFailedAndPendingLogs() throws SQLException {
        List<SyncLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM sync_log WHERE status IN ('FAILED','PENDING') ORDER BY last_attempt ASC";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                try {
                    SyncLog log = new SyncLog();
                    log.setSyncId(rs.getInt("sync_id"));
                    log.setDeviceTxnId(rs.getString("device_txn_id"));
                    log.setTableName(rs.getString("table_name"));
                    log.setStatus(rs.getString("status"));

                    Object timestampObj = rs.getObject("last_attempt");
                    log.setLastAttempt(safeParseTimestamp(timestampObj));

                    log.setRetries(rs.getInt("retries"));
                    logs.add(log);
                } catch (Exception e) {
                    System.err.println("[SyncLogDao] Error processing failed/pending log row: " + e.getMessage());
                }
            }
        }
        return logs;
    }

    // Get only FAILED logs (for specific use cases)
    public List<SyncLog> getFailedLogsOnly() throws SQLException {
        List<SyncLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM sync_log WHERE status = 'FAILED'";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                try {
                    SyncLog log = new SyncLog();
                    log.setSyncId(rs.getInt("sync_id"));
                    log.setDeviceTxnId(rs.getString("device_txn_id"));
                    log.setTableName(rs.getString("table_name"));
                    log.setStatus(rs.getString("status"));

                    Object timestampObj = rs.getObject("last_attempt");
                    log.setLastAttempt(safeParseTimestamp(timestampObj));

                    log.setRetries(rs.getInt("retries"));
                    logs.add(log);
                } catch (Exception e) {
                    System.err.println("[SyncLogDao] Error processing failed log row: " + e.getMessage());
                }
            }
        }
        return logs;
    }

    // Apply the same safe approach to other methods
    public SyncLog getLogByTxnId(String txnId) throws SQLException {
        String sql = "SELECT * FROM sync_log WHERE device_txn_id = ?";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SyncLog log = new SyncLog();
                    log.setSyncId(rs.getInt("sync_id"));
                    log.setDeviceTxnId(rs.getString("device_txn_id"));
                    log.setTableName(rs.getString("table_name"));
                    log.setStatus(rs.getString("status"));

                    Object timestampObj = rs.getObject("last_attempt");
                    log.setLastAttempt(safeParseTimestamp(timestampObj));

                    log.setRetries(rs.getInt("retries"));
                    return log;
                }
            }
        }
        return null;
    }

    // Keep all your other methods the same (addSyncLog, updateStatus, incrementRetries, etc.)
    public void addSyncLog(SyncLog log) throws SQLException {
        String sql = "INSERT INTO sync_log (device_txn_id, table_name, status, last_attempt, retries) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, log.getDeviceTxnId());
            ps.setString(2, log.getTableName());
            ps.setString(3, log.getStatus());
            ps.setTimestamp(4, log.getLastAttempt() != null ?
                    Timestamp.valueOf(log.getLastAttempt()) : null);
            ps.setInt(5, log.getRetries());
            ps.executeUpdate();
        }

        syncToOracle(log);
    }

    public void updateStatus(String deviceTxnId, String status, LocalDateTime lastAttempt) throws SQLException {
        String sql = "UPDATE sync_log SET status = ?, last_attempt = ? WHERE device_txn_id = ?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setTimestamp(2, lastAttempt != null ? Timestamp.valueOf(lastAttempt) : null);
            ps.setString(3, deviceTxnId);
            ps.executeUpdate();
        }

        SyncLog log = getLogByTxnId(deviceTxnId);
        if (log != null) {
            log.setStatus(status);
            log.setLastAttempt(lastAttempt);
            syncToOracle(log);
        }
    }

    public void incrementRetries(String deviceTxnId) throws SQLException {
        String sql = "UPDATE sync_log SET retries = retries + 1, last_attempt = CURRENT_TIMESTAMP " +
                "WHERE device_txn_id = ?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, deviceTxnId);
            ps.executeUpdate();
        }

        SyncLog log = getLogByTxnId(deviceTxnId);
        if (log != null) {
            log.setRetries(log.getRetries() + 1);
            log.setLastAttempt(LocalDateTime.now());
            syncToOracle(log);
        }
    }

    public void syncToOracle(SyncLog log) {
        String sql = "{ call system.proc_sync_sync_log(?, ?, ?, ?, ?) }";

        try (Connection conn = OracleConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, log.getDeviceTxnId());
            cs.setString(2, log.getTableName());
            cs.setString(3, log.getStatus());
            cs.setTimestamp(4, log.getLastAttempt() != null ? Timestamp.valueOf(log.getLastAttempt()) : null);
            cs.setInt(5, log.getRetries());

            cs.execute();
        } catch (Exception e) {
            System.err.println("[SyncLogDao] Oracle sync failed: " + e.getMessage());
        }
    }

    // NEW: Improved resync that actually verifies data sync
    public boolean attemptResyncRecord(SyncLog log) {
        try {
            System.out.println("[SyncLogDao] Attempting to resync: " + log.getDeviceTxnId() + " (table: " + log.getTableName() + ")");

            // First, try to sync the actual data record (not just the sync log)
            boolean dataSyncSuccess = syncActualDataRecord(log);

            if (dataSyncSuccess) {
                // Only mark as SUCCESS if the actual data sync worked
                updateStatus(log.getDeviceTxnId(), "SUCCESS", LocalDateTime.now());
                System.out.println("[SyncLogDao] ✅ Data sync successful for: " + log.getDeviceTxnId());
                return true;
            } else {
                // Mark as FAILED if data sync failed
                updateStatus(log.getDeviceTxnId(), "FAILED", LocalDateTime.now());
                incrementRetries(log.getDeviceTxnId());
                System.out.println("[SyncLogDao] ❌ Data sync failed for: " + log.getDeviceTxnId());
                return false;
            }

        } catch (Exception e) {
            System.err.println("[SyncLogDao] Error during resync: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // NEW: Sync the actual data record based on table name
    private boolean syncActualDataRecord(SyncLog log) {
        try {
            // Convert to uppercase for case-insensitive comparison
            String tableName = log.getTableName().toUpperCase();
            System.out.println("[SyncLogDao] Normalized table name: " + tableName + " (original: " + log.getTableName() + ")");

            // Based on the table name, call the appropriate sync procedure
            switch (tableName) {
                case "USER":
                    return syncUserRecord(log.getDeviceTxnId());
                case "ACCOUNT":
                    return syncAccountRecord(log.getDeviceTxnId());
                case "CATEGORY":
                    return syncCategoryRecord(log.getDeviceTxnId());
                case "EXPENSE":
                    return syncExpenseRecord(log.getDeviceTxnId());
                case "BUDGET":
                    return syncBudgetRecord(log.getDeviceTxnId());
                case "SAVINGS_GOAL":
                    return syncSavingsGoalRecord(log.getDeviceTxnId());
                default:
                    System.err.println("[SyncLogDao] Unknown table for sync: " + log.getTableName() + " (normalized: " + tableName + ")");
                    return false;
            }
        } catch (Exception e) {
            System.err.println("[SyncLogDao] Error syncing data record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Replace all the sync methods with these fixed versions:

    // FIXED: Sync user record to Oracle - device_txn_id is user_id
    private boolean syncUserRecord(String deviceTxnId) {
        String getSql = "SELECT * FROM user WHERE user_id = ?";
        String syncSql = "{ call system.proc_sync_user(?, ?, ?, ?, ?, ?) }";

        try (Connection sqliteConn = SQLiteConnection.getConnection();
             PreparedStatement getPs = sqliteConn.prepareStatement(getSql);
             Connection oracleConn = OracleConnection.getConnection();
             CallableStatement syncCs = oracleConn.prepareCall(syncSql)) {

            // For user table, device_txn_id is the user_id
            int userId = Integer.parseInt(deviceTxnId);
            getPs.setInt(1, userId);

            try (ResultSet rs = getPs.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[SyncLogDao] Found user record to sync: " + rs.getInt("user_id"));

                    syncCs.setInt(1, rs.getInt("user_id"));
                    syncCs.setString(2, rs.getString("username"));
                    syncCs.setString(3, rs.getString("password_hash"));
                    syncCs.setString(4, rs.getString("email"));
                    Object createdAtObj = rs.getObject("created_at");
                    LocalDateTime createdAt = safeParseTimestamp(createdAtObj);
                    if (createdAt != null) {
                        syncCs.setTimestamp(5, Timestamp.valueOf(createdAt));
                    } else {
                        syncCs.setNull(5, java.sql.Types.TIMESTAMP);
                    }
                    syncCs.setInt(6, rs.getInt("delete_flag"));

                    syncCs.execute();
                    System.out.println("[SyncLogDao] User sync completed successfully");
                    return true;
                } else {
                    System.err.println("[SyncLogDao] No user record found for user_id: " + userId);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("[SyncLogDao] Invalid user_id in device_txn_id: " + deviceTxnId);
        } catch (Exception e) {
            System.err.println("[SyncLogDao] Error syncing user record: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // FIXED: Sync account record to Oracle - device_txn_id is account_id
    private boolean syncAccountRecord(String deviceTxnId) {
        String getSql = "SELECT * FROM account WHERE account_id = ?";
        String syncSql = "{ call system.proc_sync_account(?, ?, ?, ?, ?, ?) }";

        try (Connection sqliteConn = SQLiteConnection.getConnection();
             PreparedStatement getPs = sqliteConn.prepareStatement(getSql);
             Connection oracleConn = OracleConnection.getConnection();
             CallableStatement syncCs = oracleConn.prepareCall(syncSql)) {

            // For account table, device_txn_id is the account_id
            int accountId = Integer.parseInt(deviceTxnId);
            getPs.setInt(1, accountId);

            try (ResultSet rs = getPs.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[SyncLogDao] Found account record to sync: " + rs.getInt("account_id"));

                    syncCs.setInt(1, rs.getInt("account_id"));
                    syncCs.setInt(2, rs.getInt("user_id"));
                    syncCs.setString(3, rs.getString("name"));
                    syncCs.setString(4, rs.getString("currency"));
                    syncCs.setDouble(5, rs.getDouble("opening_balance"));
                    syncCs.setInt(6, rs.getInt("delete_flag"));

                    syncCs.execute();
                    System.out.println("[SyncLogDao] Account sync completed successfully");
                    return true;
                } else {
                    System.err.println("[SyncLogDao] No account record found for account_id: " + accountId);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("[SyncLogDao] Invalid account_id in device_txn_id: " + deviceTxnId);
        } catch (Exception e) {
            System.err.println("[SyncLogDao] Error syncing account record: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // FIXED: Sync category record to Oracle - device_txn_id is category_id
    private boolean syncCategoryRecord(String deviceTxnId) {
        String getSql = "SELECT * FROM category WHERE category_id = ?";
        String syncSql = "{ call system.proc_sync_category(?, ?, ?, ?, ?, ?) }";

        try (Connection sqliteConn = SQLiteConnection.getConnection();
             PreparedStatement getPs = sqliteConn.prepareStatement(getSql);
             Connection oracleConn = OracleConnection.getConnection();
             CallableStatement syncCs = oracleConn.prepareCall(syncSql)) {

            // For category table, device_txn_id is the category_id
            int categoryId = Integer.parseInt(deviceTxnId);
            getPs.setInt(1, categoryId);

            try (ResultSet rs = getPs.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[SyncLogDao] Found category record to sync: " + rs.getInt("category_id"));

                    syncCs.setInt(1, rs.getInt("category_id"));
                    syncCs.setInt(2, rs.getInt("user_id"));
                    syncCs.setString(3, rs.getString("name"));
                    syncCs.setString(4, rs.getString("type"));

                    // Handle nullable parent_category_id
                    int parentCategoryId = rs.getInt("parent_category_id");
                    if (rs.wasNull()) {
                        syncCs.setNull(5, java.sql.Types.INTEGER);
                    } else {
                        syncCs.setInt(5, parentCategoryId);
                    }

                    syncCs.setInt(6, rs.getInt("delete_flag"));

                    syncCs.execute();
                    System.out.println("[SyncLogDao] Category sync completed successfully");
                    return true;
                } else {
                    System.err.println("[SyncLogDao] No category record found for category_id: " + categoryId);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("[SyncLogDao] Invalid category_id in device_txn_id: " + deviceTxnId);
        } catch (Exception e) {
            System.err.println("[SyncLogDao] Error syncing category record: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // FIXED: Sync expense record to Oracle - device_txn_id is the actual UUID
    private boolean syncExpenseRecord(String deviceTxnId) {
        String getSql = "SELECT * FROM expense WHERE device_txn_id = ? OR expense_id = ?";
        String syncSql = "{ call system.proc_sync_expense(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";

        try (Connection sqliteConn = SQLiteConnection.getConnection();
             PreparedStatement getPs = sqliteConn.prepareStatement(getSql);
             Connection oracleConn = OracleConnection.getConnection();
             CallableStatement syncCs = oracleConn.prepareCall(syncSql)) {

            getPs.setString(1, deviceTxnId);

            // Try to parse as expense_id if it's numeric (fallback)
            try {
                int expenseId = Integer.parseInt(deviceTxnId);
                getPs.setInt(2, expenseId);
            } catch (NumberFormatException e) {
                getPs.setString(2, deviceTxnId); // Use the UUID as-is
            }

            try (ResultSet rs = getPs.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[SyncLogDao] Found expense record to sync: " + rs.getInt("expense_id"));

                    syncCs.setInt(1, rs.getInt("expense_id"));
                    syncCs.setString(2, rs.getString("device_txn_id"));
                    syncCs.setInt(3, rs.getInt("user_id"));
                    syncCs.setInt(4, rs.getInt("account_id"));
                    syncCs.setInt(5, rs.getInt("category_id"));
                    syncCs.setDouble(6, rs.getDouble("amount"));
                    syncCs.setString(7, rs.getString("currency"));
                    Object dateObj = rs.getObject("date");
                    LocalDateTime date = safeParseTimestamp(dateObj);
                    if (date != null) {
                        syncCs.setDate(8, java.sql.Date.valueOf(date.toLocalDate()));
                    } else {
                        syncCs.setNull(8, java.sql.Types.DATE);
                    }
                    syncCs.setString(9, rs.getString("description"));
                    syncCs.setInt(10, rs.getInt("recurring_flag"));
                    syncCs.setString(11, rs.getString("sync_status"));
                    syncCs.setTimestamp(12, rs.getTimestamp("created_at"));

                    // Handle nullable modified_at
                    Timestamp modifiedAt = rs.getTimestamp("modified_at");
                    if (rs.wasNull()) {
                        syncCs.setNull(13, java.sql.Types.TIMESTAMP);
                    } else {
                        syncCs.setTimestamp(13, modifiedAt);
                    }

                    syncCs.setInt(14, rs.getInt("delete_flag"));

                    syncCs.execute();
                    System.out.println("[SyncLogDao] Expense sync completed successfully");
                    return true;
                } else {
                    System.err.println("[SyncLogDao] No expense record found for device_txn_id: " + deviceTxnId);
                }
            }
        } catch (Exception e) {
            System.err.println("[SyncLogDao] Error syncing expense record: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // FIXED: Sync budget record to Oracle - device_txn_id is budget_id
    private boolean syncBudgetRecord(String deviceTxnId) {
        String getSql = "SELECT * FROM budget WHERE budget_id = ?";
        String syncSql = "{ call system.proc_sync_budget(?, ?, ?, ?, ?, ?, ?, ?) }";

        try (Connection sqliteConn = SQLiteConnection.getConnection();
             PreparedStatement getPs = sqliteConn.prepareStatement(getSql);
             Connection oracleConn = OracleConnection.getConnection();
             CallableStatement syncCs = oracleConn.prepareCall(syncSql)) {

            // For budget table, device_txn_id is the budget_id
            int budgetId = Integer.parseInt(deviceTxnId);
            getPs.setInt(1, budgetId);

            try (ResultSet rs = getPs.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[SyncLogDao] Found budget record to sync: " + rs.getInt("budget_id"));

                    syncCs.setInt(1, rs.getInt("budget_id"));
                    syncCs.setInt(2, rs.getInt("user_id"));
                    syncCs.setInt(3, rs.getInt("category_id"));

                    // FIX: Use safe timestamp parsing for dates
                    Object startDateObj = rs.getObject("start_date");
                    Object endDateObj = rs.getObject("end_date");

                    LocalDateTime startDate = safeParseTimestamp(startDateObj);
                    LocalDateTime endDate = safeParseTimestamp(endDateObj);

                    // Convert to java.sql.Date for the stored procedure
                    if (startDate != null) {
                        syncCs.setDate(4, java.sql.Date.valueOf(startDate.toLocalDate()));
                    } else {
                        syncCs.setNull(4, java.sql.Types.DATE);
                    }

                    if (endDate != null) {
                        syncCs.setDate(5, java.sql.Date.valueOf(endDate.toLocalDate()));
                    } else {
                        syncCs.setNull(5, java.sql.Types.DATE);
                    }

                    syncCs.setDouble(6, rs.getDouble("amount"));
                    syncCs.setDouble(7, rs.getDouble("alert_threshold_pct"));
                    syncCs.setInt(8, rs.getInt("delete_flag"));

                    syncCs.execute();
                    System.out.println("[SyncLogDao] Budget sync completed successfully");
                    return true;
                } else {
                    System.err.println("[SyncLogDao] No budget record found for budget_id: " + budgetId);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("[SyncLogDao] Invalid budget_id in device_txn_id: " + deviceTxnId);
        } catch (Exception e) {
            System.err.println("[SyncLogDao] Error syncing budget record: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // FIXED: Sync savings goal record to Oracle - device_txn_id is goal_id
    // FIXED: Sync savings goal record to Oracle - device_txn_id is goal_id
    private boolean syncSavingsGoalRecord(String deviceTxnId) {
        String getSql = "SELECT * FROM savings_goal WHERE goal_id = ?";
        String syncSql = "{ call system.proc_sync_savings_goal(?, ?, ?, ?, ?, ?, ?, ?, ?) }";

        try (Connection sqliteConn = SQLiteConnection.getConnection();
             PreparedStatement getPs = sqliteConn.prepareStatement(getSql);
             Connection oracleConn = OracleConnection.getConnection();
             CallableStatement syncCs = oracleConn.prepareCall(syncSql)) {

            // For savings_goal table, device_txn_id is the goal_id
            int goalId = Integer.parseInt(deviceTxnId);
            getPs.setInt(1, goalId);

            try (ResultSet rs = getPs.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[SyncLogDao] Found savings goal record to sync: " + rs.getInt("goal_id"));

                    syncCs.setInt(1, rs.getInt("goal_id"));
                    syncCs.setInt(2, rs.getInt("user_id"));
                    syncCs.setString(3, rs.getString("name"));
                    syncCs.setDouble(4, rs.getDouble("target_amount"));
                    syncCs.setDouble(5, rs.getDouble("current_amount"));

                    // FIX: Use safe timestamp parsing for dates
                    Object startDateObj = rs.getObject("start_date");
                    Object targetDateObj = rs.getObject("target_date");

                    LocalDateTime startDate = safeParseTimestamp(startDateObj);
                    LocalDateTime targetDate = safeParseTimestamp(targetDateObj);

                    // Convert to java.sql.Date for the stored procedure
                    if (startDate != null) {
                        syncCs.setDate(6, java.sql.Date.valueOf(startDate.toLocalDate()));
                    } else {
                        syncCs.setNull(6, java.sql.Types.DATE);
                    }

                    if (targetDate != null) {
                        syncCs.setDate(7, java.sql.Date.valueOf(targetDate.toLocalDate()));
                    } else {
                        syncCs.setNull(7, java.sql.Types.DATE);
                    }

                    syncCs.setString(8, rs.getString("status"));
                    syncCs.setInt(9, rs.getInt("delete_flag"));

                    syncCs.execute();
                    System.out.println("[SyncLogDao] Savings goal sync completed successfully");
                    return true;
                } else {
                    System.err.println("[SyncLogDao] No savings goal record found for goal_id: " + goalId);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("[SyncLogDao] Invalid goal_id in device_txn_id: " + deviceTxnId);
        } catch (Exception e) {
            System.err.println("[SyncLogDao] Error syncing savings goal record: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // OLD: Keep for backward compatibility, but it's now unused
    public void resyncRecord(SyncLog log) {
        attemptResyncRecord(log);
    }

    public int countPending() {
        String sql = "SELECT COUNT(*) FROM sync_log WHERE status = 'PENDING'";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}