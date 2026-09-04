package com.personal.finance.manager.report.controller;

import com.personal.finance.manager.report.dto.MonthlyReportResponse;
import com.personal.finance.manager.report.dto.YearlyReportResponse;
import com.personal.finance.manager.report.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling financial report generation.
 * Provides endpoints for monthly and yearly financial summary reports.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Generates a monthly financial summary report for the authenticated user.
     *
     * @param year    report year
     * @param month   report month (1-12)
     * @param session authenticated HTTP session
     * @return monthly report containing income/expense breakdowns and net savings
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable int year, 
            @PathVariable int month, 
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        MonthlyReportResponse response = reportService.getMonthlyReport(userId, year, month);
        return ResponseEntity.ok(response);
    }

    /**
     * Generates a yearly financial summary report for the authenticated user.
     *
     * @param year    report year
     * @param session authenticated HTTP session
     * @return yearly report containing income/expense breakdowns and net savings
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(
            @PathVariable int year, 
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        YearlyReportResponse response = reportService.getYearlyReport(userId, year);
        return ResponseEntity.ok(response);
    }
}
