package com.example.service;

import com.example.dao.ExpenseDao;
import com.example.model.Expense;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ExpenseService {
    private final ExpenseDao dao = new ExpenseDao();

    // Add new Expense
    public void addExpense(Expense expense) {
        try {
            // Set the current user ID before adding
            expense.setUserId(UserService.getCurrentUserId());
            dao.addExpense(expense);
            System.out.println("[ExpenseService] Expense added successfully.");
        } catch (SQLException ex) {
            System.err.println("[ExpenseService] Error adding expense: " + ex.getMessage());
        }
    }

    // Update Expense
    public void updateExpense(Expense expense) {
        try {
            dao.updateExpense(expense);
            System.out.println("[ExpenseService] Expense updated successfully.");
        } catch (SQLException ex) {
            System.err.println("[ExpenseService] Error updating expense: " + ex.getMessage());
        }
    }

    // Delete Expense
    public void deleteExpense(int id) {
        try {
            dao.deleteExpense(id);
            System.out.println("[ExpenseService] Expense deleted successfully.");
        } catch (SQLException ex) {
            System.err.println("[ExpenseService] Error deleting expense: " + ex.getMessage());
        }
    }

    // Fetch all expenses for current user
    public List<Expense> getAllExpenses() {
        try {
            int userId = UserService.getCurrentUserId();
            if (userId == -1) return List.of(); // No user logged in
            return dao.getExpensesByUser(userId);
        } catch (SQLException ex) {
            System.err.println("[ExpenseService] Error fetching expenses: " + ex.getMessage());
            return List.of();
        }
    }

    // Fetch expenses by date range for current user
    public List<Expense> getExpensesByDateRange(LocalDate start, LocalDate end) {
        try {
            int userId = UserService.getCurrentUserId();
            if (userId == -1) return List.of(); // No user logged in
            return dao.getExpensesByDateRange(userId, start, end);
        } catch (SQLException ex) {
            System.err.println("[ExpenseService] Error fetching expenses by date range: " + ex.getMessage());
            return List.of();
        }
    }

    public double totalExpenses() {
        return getAllExpenses().stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }
}