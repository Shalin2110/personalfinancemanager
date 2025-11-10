package com.example.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

public class ReportService {

    private JasperPrint showReport(String jrxmlPath, Connection conn, int userId) throws JRException {
        try {
            // Check if resource exists
            String resourcePath = "/reports/" + jrxmlPath;
            InputStream reportStream = getClass().getResourceAsStream(resourcePath);

            if (reportStream == null) {
                throw new JRException("Report file not found: " + resourcePath);
            }

            JasperReport report = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> params = new HashMap<>();
            params.put("USER_ID", userId);

            JasperPrint print = JasperFillManager.fillReport(report, params, conn);

            if (print.getPages() != null && !print.getPages().isEmpty()) {
                JasperViewer.viewReport(print, false);
            }

            return print;

        } catch (Exception e) {
            System.err.println("[ReportService] Error loading report: " + jrxmlPath);
            e.printStackTrace();
            throw new JRException("Failed to load report: " + e.getMessage(), e);
        }
    }

    private boolean saveReportAsPDF(String jrxml, Connection conn, int userId) throws JRException {
        JasperReport report = JasperCompileManager.compileReport(
                getClass().getResourceAsStream("/reports/" + jrxml)
        );

        Map<String, Object> params = new HashMap<>();
        params.put("USER_ID", userId);

        JasperPrint print = JasperFillManager.fillReport(report, params, conn);

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

    // Report calls now receive userId
    public JasperPrint showMonthlyExpenditureReport(Connection conn, int userId) throws JRException {
        return showReport("MonthlyExpenditureAnalysis.jrxml", conn, userId);
    }
    public boolean saveMonthlyExpenditureReport(Connection conn, int userId) throws JRException {
        return saveReportAsPDF("MonthlyExpenditureAnalysis.jrxml", conn, userId);
    }

    public JasperPrint showBudgetAdherenceReport(Connection conn, int userId) throws JRException {
        return showReport("BudgetAdherenceTracking.jrxml", conn, userId);
    }
    public boolean saveBudgetAdherenceReport(Connection conn, int userId) throws JRException {
        return saveReportAsPDF("BudgetAdherenceTracking.jrxml", conn, userId);
    }

    public JasperPrint showSavingsGoalProgressReport(Connection conn, int userId) throws JRException {
        return showReport("SavingsGoalProgress.jrxml", conn, userId);
    }
    public boolean saveSavingsGoalProgressReport(Connection conn, int userId) throws JRException {
        return saveReportAsPDF("SavingsGoalProgress.jrxml", conn, userId);
    }

    public JasperPrint showCategoryExpenseDistributionReport(Connection conn, int userId) throws JRException {
        return showReport("Category-WiseExpenseDistribution.jrxml", conn, userId);
    }
    public boolean saveCategoryExpenseDistributionReport(Connection conn, int userId) throws JRException {
        return saveReportAsPDF("Category-WiseExpenseDistribution.jrxml", conn, userId);
    }

    public JasperPrint showForecastSavingsReport(Connection conn, int userId) throws JRException {
        return showReport("ForecastedSavingTrends.jrxml", conn, userId);
    }
    public boolean saveForecastSavingsReport(Connection conn, int userId) throws JRException {
        return saveReportAsPDF("ForecastedSavingTrends.jrxml", conn, userId);
    }
}
