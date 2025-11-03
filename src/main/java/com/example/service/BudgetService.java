package com.example.service;

import com.example.dao.BudgetDao;
import com.example.model.Budget;

import java.sql.SQLException;
import java.util.List;

public class BudgetService {
    private final BudgetDao dao = new BudgetDao();

    // Add new budget (SQLite + Oracle handled inside DAO)
    public void addBudget(Budget b) {
        try {
            dao.addBudget(b);
            System.out.println("[BudgetService] Budget added successfully.");
        } catch (SQLException e) {
            System.err.println("[BudgetService] Error adding budget: " + e.getMessage());
        }
    }

    // Retrieve all budgets
    public List<Budget> getAllBudgets() {
        try {
            return dao.getAllBudgets();
        } catch (SQLException e) {
            System.err.println("[BudgetService] Error fetching budgets: " + e.getMessage());
            return List.of();
        }
    }

    // Update budget
    public void updateBudget(Budget b) {
        try {
            dao.updateBudget(b);
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
}
