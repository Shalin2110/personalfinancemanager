package com.example.service;

import com.example.dao.ExpenseDao;
import com.example.model.Expense;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ExpenseService {
    private final ExpenseDao dao = new ExpenseDao();

    // Add new Expense
    public void addExpense(Expense e) {
        try {
            dao.addExpense(e);
            dao.syncToOracle(e);
        } catch (SQLException ex) {
            System.err.println("Error adding expense: " + ex.getMessage());
        }
    }

    // Update Expense
    public void updateExpense(Expense e) {
        try {
            dao.updateExpense(e);
            dao.syncToOracle(e);
        } catch (SQLException ex) {
            System.err.println("Error updating expense: " + ex.getMessage());
        }
    }

    // Delete Expense
    public void deleteExpense(int id) {
        try {
            dao.deleteExpense(id);
        } catch (SQLException ex) {
            System.err.println("Error deleting expense: " + ex.getMessage());
        }
    }

    // Fetch all expenses for user
    public List<Expense> getExpensesByUser(int userId) {
        try {
            return dao.getExpensesByUser(userId);
        } catch (SQLException ex) {
            System.err.println("Error fetching expenses: " + ex.getMessage());
            return List.of();
        }
    }

    // Fetch expenses by date range
    public List<Expense> getExpensesByDateRange(int userId, LocalDate start, LocalDate end) {
        try {
            return dao.getExpensesByDateRange(userId, start, end);
        } catch (SQLException ex) {
            System.err.println("Error fetching expenses by date range: " + ex.getMessage());
            return List.of();
        }
    }
}
