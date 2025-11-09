package com.example.dao;

import com.example.model.Expense;
import com.example.db.SQLiteConnection;
import com.example.db.OracleConnection;
import com.example.db.SyncManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDao {
    // Add Expense + auto sync + log
    public void addExpense(Expense expense) throws SQLException {
        String txnId = SyncManager.startSync("expense", null);
        String sql = """
            INSERT INTO expense (
                device_txn_id, user_id, account_id, category_id,
                amount, currency, expense_date, description,
                recurring_flag, sync_status, delete_flag
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', 0)
        """;

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, expense.getDeviceTxnId());
            ps.setInt(2, expense.getUserId());
            ps.setInt(3, expense.getAccountId());
            ps.setInt(4, expense.getCategoryId());
            ps.setDouble(5, expense.getAmount());
            ps.setString(6, expense.getCurrency());
            ps.setString(7, expense.getExpenseDate().toString());
            ps.setString(8, expense.getDescription());
            ps.setInt(9, expense.getRecurringFlag());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) expense.setExpenseId(rs.getInt(1));

            syncToOracle(expense);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
            throw new SQLException("Add expense failed: " + e.getMessage());
        }
    }

    // Update Expense + auto sync + log
    public void updateExpense(Expense expense) throws SQLException {
        String txnId = SyncManager.startSync("expense", null);
        String sql = """
            UPDATE expense
            SET account_id=?, category_id=?, amount=?, currency=?, expense_date=?, description=?, recurring_flag=?, modified_at=CURRENT_TIMESTAMP
            WHERE expense_id=? AND delete_flag=0
        """;

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, expense.getAccountId());
            ps.setInt(2, expense.getCategoryId());
            ps.setDouble(3, expense.getAmount());
            ps.setString(4, expense.getCurrency());
            ps.setString(5, expense.getExpenseDate().toString());
            ps.setString(6, expense.getDescription());
            ps.setInt(7, expense.getRecurringFlag());
            ps.setInt(8, expense.getExpenseId());
            ps.executeUpdate();

            syncToOracle(expense);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
            throw new SQLException("Update expense failed: " + e.getMessage());
        }
    }

    // Soft Delete + auto sync + log
    public void deleteExpense(int expenseId) throws SQLException {
        String txnId = SyncManager.startSync("expense", null);
        String sql = "UPDATE expense SET delete_flag=1 WHERE expense_id=?";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.executeUpdate();

            Expense dummy = new Expense();
            dummy.setExpenseId(expenseId);
            dummy.setDeleteFlag(true);

            syncToOracle(dummy);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
        }
    }

    // Get all expenses (for user)
    public List<Expense> getExpensesByUser(int userId) throws SQLException {
        List<Expense> list = new ArrayList<>();
        String sql = """
            SELECT e.expense_id, e.device_txn_id, e.account_id, e.category_id,
                   e.amount, e.currency, e.expense_date, e.description,
                   e.recurring_flag, e.sync_status, e.created_at, e.modified_at,
                   c.name AS category_name, a.name AS account_name
            FROM expense e
            JOIN category c ON e.category_id = c.category_id
            JOIN account a ON e.account_id = a.account_id
            WHERE e.user_id = ? AND e.delete_flag = 0
            ORDER BY e.expense_date DESC
        """;

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Expense e = new Expense();
                e.setExpenseId(rs.getInt("expense_id"));
                e.setDeviceTxnId(rs.getString("device_txn_id"));
                e.setUserId(userId);
                e.setAccountId(rs.getInt("account_id"));
                e.setCategoryId(rs.getInt("category_id"));
                e.setAmount(rs.getDouble("amount"));
                e.setCurrency(rs.getString("currency"));
                e.setExpenseDate(LocalDate.parse(rs.getString("expense_date")));
                e.setDescription(rs.getString("description"));
                e.setRecurringFlag(rs.getInt("recurring_flag"));
                e.setSyncStatus(rs.getString("sync_status"));
                e.setCreatedAt(rs.getTimestamp("created_at"));
                e.setModifiedAt(rs.getTimestamp("modified_at"));
                e.setCategoryName(rs.getString("category_name"));
                e.setAccountName(rs.getString("account_name"));
                list.add(e);
            }
        }
        return list;
    }

    // Filter by expenseDate range (for summary)
    public List<Expense> getExpensesByDateRange(int userId, LocalDate start, LocalDate end) throws SQLException {
        List<Expense> list = new ArrayList<>();
        String sql = """
            SELECT * FROM expense
            WHERE user_id=? AND expense_date BETWEEN ? AND ? AND delete_flag=0
            ORDER BY expenseDate ASC
        """;
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, start.toString());
            ps.setString(3, end.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Expense e = new Expense();
                e.setExpenseId(rs.getInt("expense_id"));
                e.setDeviceTxnId(rs.getString("device_txn_id"));
                e.setUserId(rs.getInt("user_id"));
                e.setAccountId(rs.getInt("account_id"));
                e.setCategoryId(rs.getInt("category_id"));
                e.setAmount(rs.getDouble("amount"));
                e.setCurrency(rs.getString("currency"));
                e.setExpenseDate(LocalDate.parse(rs.getString("expense_date")));
                e.setDescription(rs.getString("description"));
                e.setRecurringFlag(rs.getInt("recurring_flag"));
                list.add(e);
            }
        }
        return list;
    }

    // Oracle Sync Logic - Updated to match your actual stored procedure
    public void syncToOracle(Expense expense) throws SQLException {
        String sql = "{ call proc_sync_expense(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";

        try (Connection conn = OracleConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, expense.getExpenseId());
            cs.setString(2, expense.getDeviceTxnId());
            cs.setInt(3, expense.getUserId());
            cs.setInt(4, expense.getAccountId());
            cs.setInt(5, expense.getCategoryId());
            cs.setDouble(6, expense.getAmount());
            cs.setString(7, expense.getCurrency());
            cs.setDate(8, expense.getExpenseDate() != null ? Date.valueOf(expense.getExpenseDate()) : null);
            cs.setString(9, expense.getDescription());
            cs.setInt(10, expense.getRecurringFlag());
            cs.setString(11, expense.getSyncStatus()); // p_sync_status
            cs.setTimestamp(12, expense.getCreatedAt()); // p_created_at
            cs.setTimestamp(13, expense.getModifiedAt()); // p_modified_at (was missing)
            cs.setInt(14, expense.isDeleteFlag() ? 1 : 0);

            cs.execute();
        } catch (SQLException e) {
            System.err.println("[ExpenseDao] Oracle sync failed: " + e.getMessage());
            throw e;
        }
    }
}