package com.example.model;

import java.time.LocalDate;

public class Budget {
    private int budgetId;
    private int categoryId;
    private double amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean deleteFlag;

    // Optional helper fields for UI / joins
    private String categoryName;

    public Budget() {}

    public Budget(int budgetId, int categoryId, double amount, LocalDate startDate, LocalDate endDate, boolean deleteFlag) {
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deleteFlag = deleteFlag;
    }

    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
