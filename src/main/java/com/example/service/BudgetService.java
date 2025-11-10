package com.example.service;

import com.example.dao.BudgetDao;
import com.example.model.Budget;

import java.sql.SQLException;
import java.util.List;

public class BudgetService {
    private final BudgetDao dao = new BudgetDao();

    // Add new budget (SQLite + Oracle handled inside DAO)
    public void addBudget(Budget budget) {
        try {
            // Set the current user ID before adding
            budget.setUserId(UserService.getCurrentUserId());
            dao.addBudget(budget);
            System.out.println("[BudgetService] Budget added successfully.");
        } catch (SQLException e) {
            System.err.println("[BudgetService] Error adding budget: " + e.getMessage());
        }
    }

    // Retrieve all budgets for current user
    public List<Budget> getAllBudgets() {
        try {
            int userId = UserService.getCurrentUserId();
            if (userId == -1) return List.of(); // No user logged in
            return dao.getBudgetsByUser(userId);
        } catch (SQLException e) {
            System.err.println("[BudgetService] Error fetching budgets: " + e.getMessage());
            return List.of();
        }
    }

    // Update budget
    public void updateBudget(Budget budget) {
        try {
            dao.updateBudget(budget);
            System.out.println("[BudgetService] Budget updated successfully.");
        } catch (SQLException e) {
            System.err.println("[BudgetService] Error updating budget: " + e.getMessage());
        }
    }

    // Soft delete budget
    public void deleteBudget(int id) {
        try {
            dao.deleteBudget(id);
            System.out.println("[BudgetService] Budget deleted successfully.");
        } catch (SQLException e) {
            System.err.println("[BudgetService] Error deleting budget: " + e.getMessage());
        }
    }

    public int countBudgets() {
        return getAllBudgets().size();
    }
}