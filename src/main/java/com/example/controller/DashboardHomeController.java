package com.example.controller;

import com.example.dao.SyncLogDao;
import com.example.service.BudgetService;
import com.example.service.SavingsGoalService;
import com.example.service.ExpenseService;
import com.example.service.AccountService;
import com.example.service.CategoryService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardHomeController {

    @FXML private Label lblTotalBudgets;
    @FXML private Label lblTotalGoals;
    @FXML private Label lblTotalExpenses;
    @FXML private Label lblTotalAccounts;
    @FXML private Label lblTotalCategories;
    @FXML private Label lblPendingSync;

    private final BudgetService budgetService = new BudgetService();
    private final SavingsGoalService goalService = new SavingsGoalService();
    private final ExpenseService expenseService = new ExpenseService();
    private final AccountService accountService = new AccountService();
    private final CategoryService categoryService = new CategoryService();
    private final SyncLogDao syncDao = new SyncLogDao();

    private DashboardController dashboardController;

    @FXML
    public void initialize() {
        updateDashboardStats();
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    private void updateDashboardStats() {
        // Update budgets count
        int budgetCount = budgetService.getAllBudgets().size();
        lblTotalBudgets.setText(String.valueOf(budgetCount));

        // Update goals count
        int goalCount = goalService.getAllGoals().size();
        lblTotalGoals.setText(String.valueOf(goalCount));

        // Update total expenses
        double totalExpenses = expenseService.getAllExpenses().stream()
                .mapToDouble(expense -> expense.getAmount())
                .sum();
        lblTotalExpenses.setText(String.format("%.2f", totalExpenses));

        // Update accounts count
        int accountCount = accountService.getAllAccounts().size();
        lblTotalAccounts.setText(String.valueOf(accountCount));

        // Update categories count
        int categoryCount = categoryService.getAllCategories().size();
        lblTotalCategories.setText(String.valueOf(categoryCount));

        // Update pending sync count
        lblPendingSync.setText("Pending Syncs: " + syncDao.countPending());
    }
    @FXML
    private void openBudgets() {
        if (dashboardController != null) {
            dashboardController.openBudgets();
        }
    }

    @FXML
    private void openGoals() {
        if (dashboardController != null) {
            dashboardController.openGoals();
        }
    }

    @FXML
    private void openExpenses() {
        if (dashboardController != null) {
            dashboardController.openExpenses();
        }
    }

    @FXML
    private void openAccounts() {
        if (dashboardController != null) {
            dashboardController.openAccounts();
        }
    }

    @FXML
    private void openCategories() {
        if (dashboardController != null) {
            dashboardController.openCategories();
        }
    }

}