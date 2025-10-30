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
        String txnId = SyncManager.startSync("savings_goal");
        String sql = "INSERT INTO savings_goal (name, target_amount, current_amount, start_date, end_date, delete_flag) VALUES (?, ?, ?, ?, ?, 0)";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, g.getName());
            ps.setDouble(2, g.getTargetAmount());
            ps.setDouble(3, g.getCurrentAmount());
            ps.setString(4, g.getStartDate().toString());
            ps.setString(5, g.getEndDate().toString());
            ps.executeUpdate();

            syncToOracle(g);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
            throw new SQLException("Add goal failed: " + e.getMessage());
        }
    }

    // Update + log
    public void updateGoal(SavingsGoal g) throws SQLException {
        String txnId = SyncManager.startSync("savings_goal");
        String sql = "UPDATE savings_goal SET name=?, target_amount=?, current_amount=?, start_date=?, end_date=? WHERE goal_id=?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, g.getName());
            ps.setDouble(2, g.getTargetAmount());
            ps.setDouble(3, g.getCurrentAmount());
            ps.setString(4, g.getStartDate().toString());
            ps.setString(5, g.getEndDate().toString());
            ps.setInt(6, g.getGoalId());
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
        String txnId = SyncManager.startSync("savings_goal");
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

    // Fetch all goals
    public List<SavingsGoal> getAllGoals() throws SQLException {
        List<SavingsGoal> list = new ArrayList<>();
        String sql = "SELECT * FROM savings_goal WHERE delete_flag = 0 ORDER BY goal_id DESC";
        try (Connection conn = SQLiteConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                SavingsGoal g = new SavingsGoal();
                g.setGoalId(rs.getInt("goal_id"));
                g.setName(rs.getString("name"));
                g.setTargetAmount(rs.getDouble("target_amount"));
                g.setCurrentAmount(rs.getDouble("current_amount"));
                g.setStartDate(LocalDate.parse(rs.getString("start_date")));
                g.setEndDate(LocalDate.parse(rs.getString("end_date")));
                g.setDeleteFlag(rs.getBoolean("delete_flag"));
                list.add(g);
            }
        }
        return list;
    }

    // Oracle Sync
    public void syncToOracle(SavingsGoal g) throws SQLException {
        String sql = """
            MERGE INTO savings_goal_central t
            USING (SELECT ? AS goal_id, ? AS name, ? AS target_amount, ? AS current_amount, ? AS start_date, ? AS end_date, ? AS delete_flag FROM dual) s
            ON (t.goal_id = s.goal_id)
            WHEN MATCHED THEN
                UPDATE SET t.name=s.name, t.target_amount=s.target_amount, t.current_amount=s.current_amount,
                           t.start_date=s.start_date, t.end_date=s.end_date, t.delete_flag=s.delete_flag
            WHEN NOT MATCHED THEN
                INSERT (goal_id, name, target_amount, current_amount, start_date, end_date, delete_flag)
                VALUES (s.goal_id, s.name, s.target_amount, s.current_amount, s.start_date, s.end_date, s.delete_flag)
        """;
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, g.getGoalId());
            ps.setString(2, g.getName());
            ps.setDouble(3, g.getTargetAmount());
            ps.setDouble(4, g.getCurrentAmount());
            ps.setDate(5, Date.valueOf(g.getStartDate()));
            ps.setDate(6, Date.valueOf(g.getEndDate()));
            ps.setInt(7, g.isDeleteFlag() ? 1 : 0);
            ps.executeUpdate();
        }
    }
}
