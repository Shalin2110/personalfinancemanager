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
        String txnId = SyncManager.startSync("expense");
        String sql = """
            INSERT INTO expense (
                device_txn_id, user_id, account_id, category_id,
                amount, currency, date, description,
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
            ps.setString(7, expense.getDate().toString());
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
        String txnId = SyncManager.startSync("expense");
        String sql = """
            UPDATE expense
            SET account_id=?, category_id=?, amount=?, currency=?, date=?, description=?, recurring_flag=?, modified_at=CURRENT_TIMESTAMP
            WHERE expense_id=? AND delete_flag=0
        """;

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, expense.getAccountId());
            ps.setInt(2, expense.getCategoryId());
            ps.setDouble(3, expense.getAmount());
            ps.setString(4, expense.getCurrency());
            ps.setString(5, expense.getDate().toString());
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
        String txnId = SyncManager.startSync("expense");
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
                   e.amount, e.currency, e.date, e.description,
                   e.recurring_flag, e.sync_status, e.created_at, e.modified_at,
                   c.name AS category_name, a.name AS account_name
            FROM expense e
            JOIN category c ON e.category_id = c.category_id
            JOIN account a ON e.account_id = a.account_id
            WHERE e.user_id = ? AND e.delete_flag = 0
            ORDER BY e.date DESC
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
                e.setDate(LocalDate.parse(rs.getString("date")));
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

    // Filter by date range (for summary)
    public List<Expense> getExpensesByDateRange(int userId, LocalDate start, LocalDate end) throws SQLException {
        List<Expense> list = new ArrayList<>();
        String sql = """
            SELECT * FROM expense
            WHERE user_id=? AND date BETWEEN ? AND ? AND delete_flag=0
            ORDER BY date ASC
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
                e.setDate(LocalDate.parse(rs.getString("date")));
                e.setDescription(rs.getString("description"));
                e.setRecurringFlag(rs.getInt("recurring_flag"));
                list.add(e);
            }
        }
        return list;
    }

    // Sync to Oracle (MERGE logic)
    public void syncToOracle(Expense expense) throws SQLException {
        String sql = """
            MERGE INTO expense_central t
            USING (
                SELECT ? AS expense_id, ? AS device_txn_id, ? AS user_id, ? AS account_id, ? AS category_id,
                       ? AS amount, ? AS currency, ? AS date, ? AS description, ? AS recurring_flag, ? AS delete_flag
                FROM dual
            ) s
            ON (t.expense_id = s.expense_id)
            WHEN MATCHED THEN
                UPDATE SET
                    t.device_txn_id = s.device_txn_id,
                    t.user_id = s.user_id,
                    t.account_id = s.account_id,
                    t.category_id = s.category_id,
                    t.amount = s.amount,
                    t.currency = s.currency,
                    t.date = s.date,
                    t.description = s.description,
                    t.recurring_flag = s.recurring_flag,
                    t.delete_flag = s.delete_flag
            WHEN NOT MATCHED THEN
                INSERT (expense_id, device_txn_id, user_id, account_id, category_id, amount, currency, date, description, recurring_flag, delete_flag)
                VALUES (s.expense_id, s.device_txn_id, s.user_id, s.account_id, s.category_id, s.amount, s.currency, s.date, s.description, s.recurring_flag, s.delete_flag)
        """;

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, expense.getExpenseId());
            ps.setString(2, expense.getDeviceTxnId());
            ps.setInt(3, expense.getUserId());
            ps.setInt(4, expense.getAccountId());
            ps.setInt(5, expense.getCategoryId());
            ps.setDouble(6, expense.getAmount());
            ps.setString(7, expense.getCurrency());
            ps.setDate(8, Date.valueOf(expense.getDate()));
            ps.setString(9, expense.getDescription());
            ps.setInt(10, expense.getRecurringFlag());
            ps.setInt(11, expense.isDeleteFlag() ? 1 : 0);
            ps.executeUpdate();
        }
    }
}
