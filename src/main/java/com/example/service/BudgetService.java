package com.example.service;

import com.example.dao.BudgetDao;
import com.example.model.Budget;
import java.sql.SQLException;
import java.util.List;

public class BudgetService {
    private final BudgetDao dao = new BudgetDao();

    public void addBudget(Budget b) {
        try {
            dao.addBudget(b);
            dao.syncToOracle(b);
        } catch (SQLException e) {
            System.err.println("Error adding budget: " + e.getMessage());
        }
    }

    public List<Budget> getAllBudgets() {
        try {
            return dao.getAllBudgets();
        } catch (SQLException e) {
            System.err.println("Error fetching budgets: " + e.getMessage());
            return List.of();
        }
    }

    public void updateBudget(Budget b) {
        try {
            dao.updateBudget(b);
            dao.syncToOracle(b);
        } catch (SQLException e) {
            System.err.println("Error updating budget: " + e.getMessage());
        }
    }

    public void deleteBudget(int id) {
        try {
            dao.deleteBudget(id);
        } catch (SQLException e) {
            System.err.println("Error deleting budget: " + e.getMessage());
        }
    }
}
