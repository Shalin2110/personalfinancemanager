package com.example.service;

import com.example.dao.SavingsGoalDao;
import com.example.model.SavingsGoal;

import java.sql.SQLException;
import java.util.List;

public class SavingsGoalService {
    private final SavingsGoalDao dao = new SavingsGoalDao();

    // Add new goal (SQLite + Oracle handled inside DAO)
    public void addGoal(SavingsGoal goal) {
        try {
            // Set the current user ID before adding
            goal.setUserId(UserService.getCurrentUserId());
            dao.addGoal(goal);
            System.out.println("[SavingsGoalService] Savings goal added successfully.");
        } catch (SQLException e) {
            System.err.println("[SavingsGoalService] Error adding goal: " + e.getMessage());
        }
    }

    // Retrieve all goals for current user
    public List<SavingsGoal> getAllGoals() {
        try {
            int userId = UserService.getCurrentUserId();
            if (userId == -1) return List.of(); // No user logged in
            return dao.getGoalsByUser(userId);
        } catch (SQLException e) {
            System.err.println("[SavingsGoalService] Error reading goals: " + e.getMessage());
            return List.of();
        }
    }

    // Update goal
    public void updateGoal(SavingsGoal goal) {
        try {
            dao.updateGoal(goal);
            System.out.println("[SavingsGoalService] Savings goal updated successfully.");
        } catch (SQLException e) {
            System.err.println("[SavingsGoalService] Error updating goal: " + e.getMessage());
        }
    }

    // Soft delete goal
    public void deleteGoal(int id) {
        try {
            dao.deleteGoal(id);
            System.out.println("[SavingsGoalService] Savings goal deleted successfully.");
        } catch (SQLException e) {
            System.err.println("[SavingsGoalService] Error deleting goal: " + e.getMessage());
        }
    }

    public int countGoals() {
        return getAllGoals().size();
    }
}