package com.example.service;

import com.example.dao.SavingsGoalDao;
import com.example.model.SavingsGoal;
import java.sql.SQLException;
import java.util.List;

public class SavingsGoalService {
    private final SavingsGoalDao dao = new SavingsGoalDao();

    public void addGoal(SavingsGoal g) {
        try {
            dao.addGoal(g);
            dao.syncToOracle(g);
        } catch (SQLException e) {
            System.err.println("Error adding goal: " + e.getMessage());
        }
    }

    public List<SavingsGoal> getAllGoals() {
        try {
            return dao.getAllGoals();
        } catch (SQLException e) {
            System.err.println("Error reading goals: " + e.getMessage());
            return List.of();
        }
    }

    public void updateGoal(SavingsGoal g) {
        try {
            dao.updateGoal(g);
            dao.syncToOracle(g);
        } catch (SQLException e) {
            System.err.println("Error updating goal: " + e.getMessage());
        }
    }

    public void deleteGoal(int id) {
        try {
            dao.deleteGoal(id);
        } catch (SQLException e) {
            System.err.println("Error deleting goal: " + e.getMessage());
        }
    }
}
