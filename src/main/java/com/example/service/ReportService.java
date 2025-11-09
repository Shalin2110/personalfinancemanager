package com.example.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import java.sql.Connection;
import java.util.HashMap;
import java.io.File;

public class ReportService {

    private JasperPrint showReport(String jrxmlPath, Connection conn) throws JRException {
        JasperReport report = JasperCompileManager.compileReport(
                getClass().getResourceAsStream("/reports/" + jrxmlPath)
        );
        JasperPrint print = JasperFillManager.fillReport(report, new HashMap<>(), conn);

        // Only show the viewer if there's data
        if (print.getPages() != null && !print.getPages().isEmpty()) {
            JasperViewer.viewReport(print, false);
        }

        return print;
    }

    private boolean saveReportAsPDF(String jrxml, Connection conn) throws JRException {
        JasperReport report = JasperCompileManager.compileReport(
                getClass().getResourceAsStream("/reports/" + jrxml));
        JasperPrint print = JasperFillManager.fillReport(report, new HashMap<>(), conn);

        // Check if there's data to save
        if (print.getPages() == null || print.getPages().isEmpty()) {
            return false;
        }

        File outDir = new File("reports_output");
        if (!outDir.exists()) outDir.mkdirs();

        String outputPath = "reports_output/" + jrxml.replace(".jrxml", ".pdf");
        JasperExportManager.exportReportToPdfFile(print, outputPath);
        System.out.println("PDF Saved At: " + outputPath);
        return true;
    }

    public JasperPrint showMonthlyExpenditureReport(Connection conn) throws JRException {
        return showReport("MonthlyExpenditureAnalysis.jrxml", conn);
    }

    public boolean saveMonthlyExpenditureReport(Connection conn) throws JRException {
        return saveReportAsPDF("MonthlyExpenditureAnalysis.jrxml", conn);
    }

    public JasperPrint showBudgetAdherenceReport(Connection conn) throws JRException {
        return showReport("BudgetAdherenceTracking.jrxml", conn);
    }

    public boolean saveBudgetAdherenceReport(Connection conn) throws JRException {
        return saveReportAsPDF("BudgetAdherenceTracking.jrxml", conn);
    }

    public JasperPrint showSavingsGoalProgressReport(Connection conn) throws JRException {
        return showReport("SavingsGoalProgress.jrxml", conn);
    }

    public boolean saveSavingsGoalProgressReport(Connection conn) throws JRException {
        return saveReportAsPDF("SavingsGoalProgress.jrxml", conn);
    }

    public JasperPrint showCategoryExpenseDistributionReport(Connection conn) throws JRException {
        return showReport("Category-WiseExpenseDistribution.jrxml", conn);
    }

    public boolean saveCategoryExpenseDistributionReport(Connection conn) throws JRException {
        return saveReportAsPDF("Category-WiseExpenseDistribution.jrxml", conn);
    }

    public JasperPrint showForecastSavingsReport(Connection conn) throws JRException {
        return showReport("ForecastedSavingTrends.jrxml", conn);
    }

    public boolean saveForecastSavingsReport(Connection conn) throws JRException {
        return saveReportAsPDF("ForecastedSavingTrends.jrxml", conn);
    }
}