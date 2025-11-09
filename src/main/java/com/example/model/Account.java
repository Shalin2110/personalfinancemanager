package com.example.model;

public class Account {
    private int accountId;
    private int userId;
    private String name;
    private String currency;
    private double openingBalance;
    private boolean deleteFlag;

    public Account() {}

    public Account(int accountId, int userId, String name, String currency, double openingBalance, boolean deleteFlag) {
        this.accountId = accountId;
        this.userId = userId;
        this.name = name;
        this.currency = currency;
        this.openingBalance = openingBalance;
        this.deleteFlag = deleteFlag;
    }

    // Getters and Setters
    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(double openingBalance) {
        this.openingBalance = openingBalance;
    }

    public boolean isDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }
}