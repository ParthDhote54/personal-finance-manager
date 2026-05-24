package com.personal.finance.manager.report.service;

import com.personal.finance.manager.exception.ResourceNotFoundException;
import com.personal.finance.manager.report.dto.MonthlyReportResponse;
import com.personal.finance.manager.report.dto.YearlyReportResponse;
import com.personal.finance.manager.transaction.entity.TransactionType;
import com.personal.finance.manager.transaction.repository.TransactionRepository;
import com.personal.finance.manager.user.entity.User;
import com.personal.finance.manager.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(Long userId, int year, int month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Object[]> aggregates = transactionRepository.aggregateMonthlyTransactions(user, year, month);

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();
        BigDecimal netSavings = BigDecimal.ZERO;

        for (Object[] row : aggregates) {
            String category = (String) row[0];
            TransactionType type = (TransactionType) row[1];
            BigDecimal amount = (BigDecimal) row[2];

            if (type == TransactionType.INCOME) {
                totalIncome.put(category, amount);
                netSavings = netSavings.add(amount);
            } else {
                totalExpenses.put(category, amount);
                netSavings = netSavings.subtract(amount);
            }
        }

        return MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings)
                .build();
    }

    @Transactional(readOnly = true)
    public YearlyReportResponse getYearlyReport(Long userId, int year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Object[]> aggregates = transactionRepository.aggregateYearlyTransactions(user, year);

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();
        BigDecimal netSavings = BigDecimal.ZERO;

        for (Object[] row : aggregates) {
            String category = (String) row[0];
            TransactionType type = (TransactionType) row[1];
            BigDecimal amount = (BigDecimal) row[2];

            if (type == TransactionType.INCOME) {
                totalIncome.put(category, amount);
                netSavings = netSavings.add(amount);
            } else {
                totalExpenses.put(category, amount);
                netSavings = netSavings.subtract(amount);
            }
        }

        return YearlyReportResponse.builder()
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings)
                .build();
    }
}
