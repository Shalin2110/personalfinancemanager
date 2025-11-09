package com.example.service;

import com.example.dao.AccountDao;
import com.example.model.Account;

import java.sql.SQLException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class AccountService {
    private final AccountDao dao = new AccountDao();

    // Add new account
    public void addAccount(Account account) {
        try {
            // Set the current user ID before adding
            account.setUserId(UserService.getCurrentUserId());
            dao.addAccount(account);
            System.out.println("[AccountService] Account added successfully.");
        } catch (SQLException e) {
            System.err.println("[AccountService] Error adding account: " + e.getMessage());
        }
    }

    // Retrieve all accounts for current user
    public List<Account> getAllAccounts() {
        try {
            int userId = UserService.getCurrentUserId();
            if (userId == -1) return List.of(); // No user logged in
            return dao.getAccountsByUser(userId);
        } catch (SQLException e) {
            System.err.println("[AccountService] Error fetching accounts: " + e.getMessage());
            return List.of();
        }
    }

    // Update account
    public void updateAccount(Account account) {
        try {
            dao.updateAccount(account);
            System.out.println("[AccountService] Account updated successfully.");
        } catch (SQLException e) {
            System.err.println("[AccountService] Error updating account: " + e.getMessage());
        }
    }

    // Soft delete account
    public void deleteAccount(int id) {
        try {
            dao.deleteAccount(id);
            System.out.println("[AccountService] Account deleted successfully.");
        } catch (SQLException e) {
            System.err.println("[AccountService] Error deleting account: " + e.getMessage());
        }
    }

    // Get account map for dropdowns (like CategoryService)
    public Map<String, Integer> getAccountMap() {
        Map<String, Integer> accountMap = new HashMap<>();
        try {
            List<Account> accounts = getAllAccounts(); // Use getAllAccounts which filters by user
            for (Account account : accounts) {
                accountMap.put(account.getName(), account.getAccountId());
            }
        } catch (Exception e) {
            System.err.println("[AccountService] Error fetching account map: " + e.getMessage());
        }
        return accountMap;
    }

    public int countAccounts() {
        return getAllAccounts().size();
    }
}