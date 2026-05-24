package com.personal.finance.manager.report.controller;

import com.personal.finance.manager.report.dto.MonthlyReportResponse;
import com.personal.finance.manager.report.dto.YearlyReportResponse;
import com.personal.finance.manager.report.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @RequestParam int year, 
            @RequestParam int month, 
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        MonthlyReportResponse response = reportService.getMonthlyReport(userId, year, month);
        return ResponseEntity.ok(response);
    }

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
