package com.example.dao;

import com.example.model.Account;
import com.example.db.SQLiteConnection;
import com.example.db.OracleConnection;
import com.example.db.SyncManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDao {

    // Add new Account + auto sync + log
    public void addAccount(Account account) throws SQLException {
        String txnId = SyncManager.startSync("account", null);

        String sql = "INSERT INTO account (user_id, name, currency, opening_balance, delete_flag) VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, account.getUserId());
            ps.setString(2, account.getName());
            ps.setString(3, account.getCurrency());
            ps.setDouble(4, account.getOpeningBalance());
            ps.executeUpdate();

            // Retrieve new ID
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int accountId = rs.getInt(1);
                    account.setAccountId(accountId);

                    // UPDATE: Use the actual account_id as device_txn_id instead of UUID
                    String newTxnId = String.valueOf(accountId);
                    SyncManager.updateDeviceTxnId(txnId, newTxnId, "account");
                    txnId = newTxnId; // Update local reference
                }
            }

            syncToOracle(account);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
            throw new SQLException("Add account failed: " + e.getMessage());
        }
    }

    // Fetch all accounts (for all users - ADMIN only)
    public List<Account> getAllAccounts() throws SQLException {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM account WHERE delete_flag = 0 ORDER BY account_id ASC";

        try (Connection conn = SQLiteConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Account account = new Account();
                account.setAccountId(rs.getInt("account_id"));
                account.setUserId(rs.getInt("user_id"));
                account.setName(rs.getString("name"));
                account.setCurrency(rs.getString("currency"));
                account.setOpeningBalance(rs.getDouble("opening_balance"));
                account.setDeleteFlag(rs.getBoolean("delete_flag"));
                list.add(account);
            }
        }
        return list;
    }

    // Fetch accounts for specific user
    public List<Account> getAccountsByUser(int userId) throws SQLException {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM account WHERE user_id = ? AND delete_flag = 0 ORDER BY account_id ASC";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Account account = new Account();
                account.setAccountId(rs.getInt("account_id"));
                account.setUserId(rs.getInt("user_id"));
                account.setName(rs.getString("name"));
                account.setCurrency(rs.getString("currency"));
                account.setOpeningBalance(rs.getDouble("opening_balance"));
                account.setDeleteFlag(rs.getBoolean("delete_flag"));
                list.add(account);
            }
        }
        return list;
    }

    // Update + auto sync + log
    public void updateAccount(Account account) throws SQLException {
        // Use the account_id as device_txn_id for updates
        String txnId = String.valueOf(account.getAccountId());
        SyncManager.startSync("account", txnId);

        String sql = "UPDATE account SET name=?, currency=?, opening_balance=? WHERE account_id=?";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, account.getName());
            ps.setString(2, account.getCurrency());
            ps.setDouble(3, account.getOpeningBalance());
            ps.setInt(4, account.getAccountId());
            ps.executeUpdate();

            syncToOracle(account);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
            throw new SQLException("Update account failed: " + e.getMessage());
        }
    }

    // Soft Delete + auto sync + log
    public void deleteAccount(int id) throws SQLException {
        // Use the account_id as device_txn_id for deletes
        String txnId = String.valueOf(id);
        SyncManager.startSync("account", txnId);

        String sql = "UPDATE account SET delete_flag = 1 WHERE account_id = ?";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            Account dummy = new Account();
            dummy.setAccountId(id);
            dummy.setDeleteFlag(true);
            syncToOracle(dummy);

            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
        }
    }

    // Oracle Sync Logic
    private void syncToOracle(Account account) throws SQLException {
        String sql = "{ call system.proc_sync_account(?, ?, ?, ?, ?, ?) }";

        try (Connection conn = OracleConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, account.getAccountId());
            cs.setInt(2, account.getUserId());
            cs.setString(3, account.getName());
            cs.setString(4, account.getCurrency());
            cs.setDouble(5, account.getOpeningBalance());
            cs.setInt(6, account.isDeleteFlag() ? 1 : 0);

            cs.execute();
        } catch (SQLException e) {
            System.err.println("[AccountDao] Oracle sync failed: " + e.getMessage());
            throw e;
        }
    }
}