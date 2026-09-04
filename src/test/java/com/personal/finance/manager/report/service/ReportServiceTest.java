package com.personal.finance.manager.report.service;

import com.personal.finance.manager.category.entity.CategoryType;
import com.personal.finance.manager.report.dto.MonthlyReportResponse;
import com.personal.finance.manager.report.dto.YearlyReportResponse;
import com.personal.finance.manager.transaction.entity.TransactionType;
import com.personal.finance.manager.transaction.repository.TransactionRepository;
import com.personal.finance.manager.user.entity.User;
import com.personal.finance.manager.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    public void testGetMonthlyReportWithData() {
        User user = new User();
        user.setId(1L);

        List<Object[]> queryResults = new ArrayList<>();
        queryResults.add(new Object[]{"Salary", TransactionType.INCOME, new BigDecimal("5000.00")});
        queryResults.add(new Object[]{"Rent", TransactionType.EXPENSE, new BigDecimal("1200.00")});
        queryResults.add(new Object[]{"Food", TransactionType.EXPENSE, new BigDecimal("300.00")});

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(transactionRepository.aggregateMonthlyTransactions(user, 2024, 1)).thenReturn(queryResults);

        MonthlyReportResponse response = reportService.getMonthlyReport(1L, 2024, 1);

        assertEquals(1, response.getTotalIncome().size());
        assertEquals(new BigDecimal("5000.00"), response.getTotalIncome().get("Salary"));
        assertEquals(2, response.getTotalExpenses().size());
        assertEquals(new BigDecimal("1200.00"), response.getTotalExpenses().get("Rent"));
        assertEquals(new BigDecimal("3500.00"), response.getNetSavings());
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
    public void testGetYearlyReportWithData() {
        User user = new User();
        user.setId(1L);

        List<Object[]> queryResults = new ArrayList<>();
        queryResults.add(new Object[]{"Salary", TransactionType.INCOME, new BigDecimal("60000.00")});
        queryResults.add(new Object[]{"Rent", TransactionType.EXPENSE, new BigDecimal("14400.00")});

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(transactionRepository.aggregateYearlyTransactions(user, 2024)).thenReturn(queryResults);

        YearlyReportResponse response = reportService.getYearlyReport(1L, 2024);

        assertEquals(1, response.getTotalIncome().size());
        assertEquals(new BigDecimal("60000.00"), response.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("45600.00"), response.getNetSavings());
    }
}
