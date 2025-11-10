package com.example.model;

import java.sql.Timestamp;
import java.time.LocalDate;

public class Expense {
    private int expenseId;
    private String deviceTxnId;
    private int userId;
    private int accountId;
    private int categoryId;
    private double amount;
    private String currency;
    private LocalDate expenseDate;
    private String description;
    private int recurringFlag;
    private String syncStatus;
    private Timestamp createdAt;
    private Timestamp modifiedAt;
    private boolean deleteFlag;

    private String categoryName;
    private String accountName;

    public Expense() {
        this.syncStatus = "PENDING"; // Default value
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.modifiedAt = new Timestamp(System.currentTimeMillis());
    }

    public Expense(int expenseId, String deviceTxnId, int userId, int accountId, int categoryId,
                   double amount, String currency, LocalDate expenseDate, String description,
                   int recurringFlag, String syncStatus, Timestamp createdAt, Timestamp modifiedAt,
                   boolean deleteFlag) {
        this.expenseId = expenseId;
        this.deviceTxnId = deviceTxnId;
        this.userId = userId;
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.currency = currency;
        this.expenseDate = expenseDate;
        this.description = description;
        this.recurringFlag = recurringFlag;
        this.syncStatus = syncStatus != null ? syncStatus : "PENDING";
        this.createdAt = createdAt != null ? createdAt : new Timestamp(System.currentTimeMillis());
        this.modifiedAt = modifiedAt != null ? modifiedAt : new Timestamp(System.currentTimeMillis());
        this.deleteFlag = deleteFlag;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    public String getDeviceTxnId() {
        return deviceTxnId;
    }

    public void setDeviceTxnId(String deviceTxnId) {
        this.deviceTxnId = deviceTxnId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRecurringFlag() {
        return recurringFlag;
    }

    public void setRecurringFlag(int recurringFlag) {
        this.recurringFlag = recurringFlag;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Timestamp modifiedAt) {
        this.modifiedAt = modifiedAt;
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

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
