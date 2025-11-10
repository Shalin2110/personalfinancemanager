package com.example.dao;

import com.example.model.SavingsGoal;
import com.example.db.SQLiteConnection;
import com.example.db.OracleConnection;
import com.example.db.SyncManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SavingsGoalDao {

    // Add + log
    public void addGoal(SavingsGoal g) throws SQLException {
        String txnId = SyncManager.startSync("savings_goal", null);
        String sql = "INSERT INTO savings_goal (user_id, name, target_amount, current_amount, start_date, target_date, status, delete_flag) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, g.getUserId());
            ps.setString(2, g.getName());
            ps.setDouble(3, g.getTargetAmount());
            ps.setDouble(4, g.getCurrentAmount());
            ps.setString(5, g.getStartDate().toString());
            ps.setString(6, g.getTargetDate().toString());
            ps.setString(7, g.getStatus());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int goalId = rs.getInt(1);
                    g.setGoalId(goalId);

                    // UPDATE: Use the actual goal_id as device_txn_id instead of UUID
                    String newTxnId = String.valueOf(goalId);
                    SyncManager.updateDeviceTxnId(txnId, newTxnId, "savings_goal");
                    txnId = newTxnId; // Update local reference
                }
            }

            syncToOracle(g);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
            throw new SQLException("Add goal failed: " + e.getMessage());
        }
    }

    // Update + log
    public void updateGoal(SavingsGoal g) throws SQLException {
        // Use the goal_id as device_txn_id for updates
        String txnId = String.valueOf(g.getGoalId());
        SyncManager.startSync("savings_goal", txnId);

        String sql = "UPDATE savings_goal SET name=?, target_amount=?, current_amount=?, start_date=?, target_date=? , status=? WHERE goal_id=?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, g.getName());
            ps.setDouble(2, g.getTargetAmount());
            ps.setDouble(3, g.getCurrentAmount());
            ps.setString(4, g.getStartDate().toString());
            ps.setString(5, g.getTargetDate().toString());
            ps.setString(6, g.getStatus());
            ps.setInt(7, g.getGoalId());
            ps.executeUpdate();

            syncToOracle(g);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
            throw new SQLException("Update goal failed: " + e.getMessage());
        }
    }

    // Soft delete + log
    public void deleteGoal(int id) throws SQLException {
        // Use the goal_id as device_txn_id for deletes
        String txnId = String.valueOf(id);
        SyncManager.startSync("savings_goal", txnId);

        String sql = "UPDATE savings_goal SET delete_flag=1 WHERE goal_id=?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            SavingsGoal dummy = new SavingsGoal();
            dummy.setGoalId(id);
            dummy.setDeleteFlag(true);
            syncToOracle(dummy);

            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
        }
    }

    // Fetch all goals (for all users - ADMIN only) - KEEP AS IS
    public List<SavingsGoal> getAllGoals() throws SQLException {
        List<SavingsGoal> list = new ArrayList<>();
        String sql = "SELECT * FROM savings_goal WHERE delete_flag = 0 ORDER BY goal_id DESC";
        try (Connection conn = SQLiteConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                SavingsGoal g = new SavingsGoal();
                g.setGoalId(rs.getInt("goal_id"));
                g.setUserId(rs.getInt("user_id"));
                g.setName(rs.getString("name"));
                g.setTargetAmount(rs.getDouble("target_amount"));
                g.setCurrentAmount(rs.getDouble("current_amount"));
                String startStr = rs.getString("start_date");
                String targetStr = rs.getString("target_date");
                g.setStartDate(startStr != null && !startStr.isEmpty() ? LocalDate.parse(startStr) : null);
                g.setTargetDate(targetStr != null && !targetStr.isEmpty() ? LocalDate.parse(targetStr) : null);
                g.setDeleteFlag(rs.getBoolean("delete_flag"));
                list.add(g);
            }
        }
        return list;
    }

    // Fetch goals for specific user - KEEP AS IS
    public List<SavingsGoal> getGoalsByUser(int userId) throws SQLException {
        List<SavingsGoal> list = new ArrayList<>();
        String sql = "SELECT * FROM savings_goal WHERE user_id = ? AND delete_flag = 0 ORDER BY goal_id DESC";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SavingsGoal g = new SavingsGoal();
                g.setGoalId(rs.getInt("goal_id"));
                g.setUserId(rs.getInt("user_id"));
                g.setName(rs.getString("name"));
                g.setTargetAmount(rs.getDouble("target_amount"));
                g.setCurrentAmount(rs.getDouble("current_amount"));
                String startStr = rs.getString("start_date");
                String targetStr = rs.getString("target_date");
                g.setStartDate(startStr != null && !startStr.isEmpty() ? LocalDate.parse(startStr) : null);
                g.setTargetDate(targetStr != null && !targetStr.isEmpty() ? LocalDate.parse(targetStr) : null);
                g.setDeleteFlag(rs.getBoolean("delete_flag"));
                list.add(g);
            }
        }
        return list;
    }

    // Oracle Sync - KEEP AS IS
    private void syncToOracle(SavingsGoal g) throws SQLException {
        String sql = "{ call system.proc_sync_savings_goal(?, ?, ?, ?, ?, ?, ?, ?, ?) }";

        try (Connection conn = OracleConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, g.getGoalId());
            cs.setInt(2, g.getUserId());
            cs.setString(3, g.getName());
            cs.setDouble(4, g.getTargetAmount());
            cs.setDouble(5, g.getCurrentAmount());
            cs.setDate(6, g.getStartDate() != null ? Date.valueOf(g.getStartDate()) : null);
            cs.setDate(7, g.getTargetDate() != null ? Date.valueOf(g.getTargetDate()) : null);
            cs.setString(8, g.getStatus());
            cs.setInt(9, g.isDeleteFlag() ? 1 : 0);
            cs.execute();
        }
    }
}