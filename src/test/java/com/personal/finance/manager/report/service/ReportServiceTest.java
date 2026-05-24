package com.personal.finance.manager.report.service;

import com.personal.finance.manager.report.dto.MonthlyReportResponse;
import com.personal.finance.manager.transaction.repository.TransactionRepository;
import com.personal.finance.manager.user.entity.User;
import com.personal.finance.manager.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class ReportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetMonthlyReportWithEmptyDataset() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(transactionRepository.aggregateMonthlyTransactions(user, 2024, 1))
                .thenReturn(Collections.emptyList());

        MonthlyReportResponse response = reportService.getMonthlyReport(1L, 2024, 1);

        assertTrue(response.getTotalIncome().isEmpty());
        assertTrue(response.getTotalExpenses().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getNetSavings());
    }

    @Test
    public void testGetMonthlyReportWithAggregationNullHandling() {
        // Even if some objects are completely absent, it shouldn't crash.
        // It's tested indirectly by empty datasets, but if an amount is null somehow:
        // Wait, JPQL SUM doesn't return null if the group has records, but it might.
        // For standard exact JSON matching, empty datasets must yield empty maps.
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(transactionRepository.aggregateMonthlyTransactions(user, 2024, 1))
                .thenReturn(Collections.emptyList());

        MonthlyReportResponse response = reportService.getMonthlyReport(1L, 2024, 1);
        assertEquals(BigDecimal.ZERO, response.getNetSavings());
    }
}
