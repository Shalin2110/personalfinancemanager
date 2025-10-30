package com.example.dao;

import com.example.model.SyncLog;
import com.example.db.SQLiteConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SyncLogDao {

    public void addSyncLog(SyncLog log) throws SQLException {
        String sql = "INSERT INTO sync_log (device_txn_id, table_name, status, last_attempt, retries) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getDeviceTxnId());
            ps.setString(2, log.getTableName());
            ps.setString(3, log.getStatus());
            ps.setString(4, log.getLastAttempt().toString());
            ps.setInt(5, log.getRetries());
            ps.executeUpdate();
        }
    }

    public void updateStatus(String deviceTxnId, String status) throws SQLException {
        String sql = "UPDATE sync_log SET status = ?, last_attempt = CURRENT_TIMESTAMP, retries = retries + 1 WHERE device_txn_id = ?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, deviceTxnId);
            ps.executeUpdate();
        }
    }

    public List<SyncLog> getAllLogs() throws SQLException {
        List<SyncLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM sync_log ORDER BY last_attempt DESC";
        try (Connection conn = SQLiteConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                SyncLog log = new SyncLog();
                log.setSyncId(rs.getInt("sync_id"));
                log.setDeviceTxnId(rs.getString("device_txn_id"));
                log.setTableName(rs.getString("table_name"));
                log.setStatus(rs.getString("status"));
                log.setLastAttempt(LocalDateTime.parse(rs.getString("last_attempt")));
                log.setRetries(rs.getInt("retries"));
                logs.add(log);
            }
        }
        return logs;
    }
}
