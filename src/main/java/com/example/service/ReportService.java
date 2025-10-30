package com.example.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import java.sql.Connection;
import java.util.HashMap;

public class ReportService {

    public void generateBudgetAdherenceReport(Connection oracleConn) {
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(
                    getClass().getResourceAsStream("/reports/budget_adherence.jrxml"));
            JasperPrint print = JasperFillManager.fillReport(jasperReport, new HashMap<>(), oracleConn);
            JasperViewer.viewReport(print, false);
        } catch (JRException e) {
            System.err.println("Error generating budget report: " + e.getMessage());
        }
    }

    public void generateSavingsProgressReport(Connection oracleConn) {
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(
                    getClass().getResourceAsStream("/reports/savings_progress.jrxml"));
            JasperPrint print = JasperFillManager.fillReport(jasperReport, new HashMap<>(), oracleConn);
            JasperViewer.viewReport(print, false);
        } catch (JRException e) {
            System.err.println("Error generating savings report: " + e.getMessage());
        }
    }
}
