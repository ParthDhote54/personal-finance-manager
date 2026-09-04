package com.personal.finance.manager.report.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Data Transfer Object for yearly financial summary report responses.
 */
@Data
@Builder
public class YearlyReportResponse {
    private Integer year;
    private Map<String, BigDecimal> totalIncome;
    private Map<String, BigDecimal> totalExpenses;
    private BigDecimal netSavings;
}
