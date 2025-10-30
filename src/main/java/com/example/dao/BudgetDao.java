package com.example.dao;

import com.example.model.Budget;
import com.example.db.SQLiteConnection;
import com.example.db.OracleConnection;
import com.example.db.SyncManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetDao {

    // Add new Budget + auto sync + log
    public void addBudget(Budget budget) throws SQLException {
        String txnId = SyncManager.startSync("budget");
        String sql = "INSERT INTO budget (category_id, amount, start_date, end_date, delete_flag) VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, budget.getCategoryId());
            ps.setDouble(2, budget.getAmount());
            ps.setString(3, budget.getStartDate().toString());
            ps.setString(4, budget.getEndDate().toString());
            ps.executeUpdate();

            syncToOracle(budget);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
            throw new SQLException("Add budget failed: " + e.getMessage());
        }
    }

    // Fetch all budgets
    public List<Budget> getAllBudgets() throws SQLException {
        List<Budget> list = new ArrayList<>();
        String sql = """
            SELECT b.budget_id, b.category_id, c.name AS category_name, b.amount, b.start_date, b.end_date, b.delete_flag
            FROM budget b
            JOIN category c ON b.category_id = c.category_id
            WHERE b.delete_flag = 0
            ORDER BY b.budget_id DESC
        """;
        try (Connection conn = SQLiteConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Budget b = new Budget();
                b.setBudgetId(rs.getInt("budget_id"));
                b.setCategoryId(rs.getInt("category_id"));
                b.setCategoryName(rs.getString("category_name"));
                b.setAmount(rs.getDouble("amount"));
                b.setStartDate(LocalDate.parse(rs.getString("start_date")));
                b.setEndDate(LocalDate.parse(rs.getString("end_date")));
                b.setDeleteFlag(rs.getBoolean("delete_flag"));
                list.add(b);
            }
        }
        return list;
    }

    // Update + auto sync + log
    public void updateBudget(Budget budget) throws SQLException {
        String txnId = SyncManager.startSync("budget");
        String sql = "UPDATE budget SET category_id=?, amount=?, start_date=?, end_date=? WHERE budget_id=?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, budget.getCategoryId());
            ps.setDouble(2, budget.getAmount());
            ps.setString(3, budget.getStartDate().toString());
            ps.setString(4, budget.getEndDate().toString());
            ps.setInt(5, budget.getBudgetId());
            ps.executeUpdate();

            syncToOracle(budget);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
            throw new SQLException("Update budget failed: " + e.getMessage());
        }
    }

    // Soft Delete + auto sync + log
    public void deleteBudget(int id) throws SQLException {
        String txnId = SyncManager.startSync("budget");
        String sql = "UPDATE budget SET delete_flag = 1 WHERE budget_id = ?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            // Simulate deletion sync
            Budget dummy = new Budget();
            dummy.setBudgetId(id);
            dummy.setDeleteFlag(true);
            syncToOracle(dummy);

            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
        }
    }

    // Oracle Sync Logic
    public void syncToOracle(Budget budget) throws SQLException {
        String sql = """
            MERGE INTO budget_central t
            USING (SELECT ? AS budget_id, ? AS category_id, ? AS amount, ? AS start_date, ? AS end_date, ? AS delete_flag FROM dual) s
            ON (t.budget_id = s.budget_id)
            WHEN MATCHED THEN
                UPDATE SET t.category_id=s.category_id, t.amount=s.amount, t.start_date=s.start_date, t.end_date=s.end_date, t.delete_flag=s.delete_flag
            WHEN NOT MATCHED THEN
                INSERT (budget_id, category_id, amount, start_date, end_date, delete_flag)
                VALUES (s.budget_id, s.category_id, s.amount, s.start_date, s.end_date, s.delete_flag)
        """;
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, budget.getBudgetId());
            ps.setInt(2, budget.getCategoryId());
            ps.setDouble(3, budget.getAmount());
            ps.setDate(4, Date.valueOf(budget.getStartDate()));
            ps.setDate(5, Date.valueOf(budget.getEndDate()));
            ps.setInt(6, budget.isDeleteFlag() ? 1 : 0);
            ps.executeUpdate();
        }
    }
}
