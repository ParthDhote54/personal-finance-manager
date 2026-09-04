package com.personal.finance.manager.report.controller;

import com.personal.finance.manager.report.dto.MonthlyReportResponse;
import com.personal.finance.manager.report.dto.YearlyReportResponse;
import com.personal.finance.manager.report.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    public void testGetMonthlyReport() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        MonthlyReportResponse mockResponse = MonthlyReportResponse.builder()
                .month(1)
                .year(2024)
                .totalIncome(Map.of("Salary", new BigDecimal("3000.00")))
                .totalExpenses(Map.of("Rent", new BigDecimal("1200.00")))
                .netSavings(new BigDecimal("1800.00"))
                .build();

        when(reportService.getMonthlyReport(1L, 2024, 1)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/reports/monthly/2024/1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.totalIncome.Salary").value(3000.00))
                .andExpect(jsonPath("$.totalExpenses.Rent").value(1200.00))
                .andExpect(jsonPath("$.netSavings").value(1800.00));
    }

    @Test
    public void testGetYearlyReport() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        YearlyReportResponse mockResponse = YearlyReportResponse.builder()
                .year(2024)
                .totalIncome(Map.of("Salary", new BigDecimal("36000.00")))
                .totalExpenses(Map.of("Rent", new BigDecimal("14400.00")))
                .netSavings(new BigDecimal("21600.00"))
                .build();

        when(reportService.getYearlyReport(1L, 2024)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/reports/yearly/2024").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.totalIncome.Salary").value(36000.00))
                .andExpect(jsonPath("$.totalExpenses.Rent").value(14400.00))
                .andExpect(jsonPath("$.netSavings").value(21600.00));
    }
}
