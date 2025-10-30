package com.example.model;

import java.time.LocalDate;

public class SavingsGoal {
    private int goalId;
    private String name;
    private double targetAmount;
    private double currentAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean deleteFlag;

    public SavingsGoal() {}

    public SavingsGoal(int goalId, String name, double targetAmount, double currentAmount,
                       LocalDate startDate, LocalDate endDate, boolean deleteFlag) {
        this.goalId = goalId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deleteFlag = deleteFlag;
    }

    public int getGoalId() { return goalId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isDeleteFlag() { return deleteFlag; }
    public void setDeleteFlag(boolean deleteFlag) { this.deleteFlag = deleteFlag; }

    // Helper: calculate progress %
    public double getProgressPercent() {
        return targetAmount == 0 ? 0 : (currentAmount / targetAmount) * 100.0;
    }

    public String getStatus() {
        return currentAmount >= targetAmount ? "ACHIEVED" : "IN PROGRESS";
    }
}
