package com.example.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import java.sql.Connection;
import java.util.HashMap;
import java.io.File;

public class ReportService {

    private void showReport(String jrxmlPath, Connection conn) throws JRException {
        JasperReport report = JasperCompileManager.compileReport(
                getClass().getResourceAsStream("/reports/" + jrxmlPath)
        );
        JasperPrint print = JasperFillManager.fillReport(report, new HashMap<>(), conn);
        JasperViewer.viewReport(print, false);
    }

    private void saveReportAsPDF(String jrxml, Connection conn) throws JRException {
        JasperReport report = JasperCompileManager.compileReport(
                getClass().getResourceAsStream("/reports/" + jrxml));
        JasperPrint print = JasperFillManager.fillReport(report, new HashMap<>(), conn);

        File outDir = new File("reports_output");
        if (!outDir.exists()) outDir.mkdirs();

        String outputPath = "reports_output/" + jrxml.replace(".jrxml", ".pdf");
        JasperExportManager.exportReportToPdfFile(print, outputPath);
        System.out.println("PDF Saved At: " + outputPath);
    }

    public void showMonthlyExpenditureReport(Connection conn) throws JRException {
        showReport("MonthlyExpenditureAnalysis.jrxml", conn);
    }

    public void saveMonthlyExpenditureReport(Connection conn) throws JRException {
        saveReportAsPDF("MonthlyExpenditureAnalysis.jrxml", conn);
    }

    public void showBudgetAdherenceReport(Connection conn) throws JRException {
        showReport("BudgetAdherenceTracking.jrxml", conn);
    }

    public void saveBudgetAdherenceReport(Connection conn) throws JRException {
        saveReportAsPDF("BudgetAdherenceTracking.jrxml", conn);
    }

    public void showSavingsGoalProgressReport(Connection conn) throws JRException {
        showReport("SavingsGoalProgress.jrxml", conn);
    }

    public void saveSavingsGoalProgressReport(Connection conn) throws JRException {
        saveReportAsPDF("SavingsGoalProgress.jrxml", conn);
    }

    public void showCategoryExpenseDistributionReport(Connection conn) throws JRException {
        showReport("Category-WiseExpenseDistribution.jrxml", conn);
    }

    public void saveCategoryExpenseDistributionReport(Connection conn) throws JRException {
        saveReportAsPDF("Category-WiseExpenseDistribution.jrxml", conn);
    }

    public void showForecastSavingsReport(Connection conn) throws JRException {
        showReport("ForecastedSavingTrends.jrxml", conn);
    }

    public void saveForecastSavingsReport(Connection conn) throws JRException {
        saveReportAsPDF("ForecastedSavingTrends.jrxml", conn);
    }
}
