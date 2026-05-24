package com.personal.finance.manager.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.finance.manager.exception.ResourceNotFoundException;
import com.personal.finance.manager.transaction.dto.TransactionRequest;
import com.personal.finance.manager.transaction.dto.TransactionResponse;
import com.personal.finance.manager.transaction.entity.TransactionType;
import com.personal.finance.manager.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import com.personal.finance.manager.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    public void testCreateTransactionSuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("50000.00"));
        request.setDate(LocalDate.now());
        request.setCategory("Salary");
        request.setDescription("January Salary");

        TransactionResponse response = new TransactionResponse(1L, new BigDecimal("50000.00"), LocalDate.now(), "Salary", "January Salary", TransactionType.INCOME);

        when(transactionService.createTransaction(eq(1L), any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(50000.00))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    public void testCreateTransactionInvalidAmount() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("-50.00")); // Invalid
        request.setDate(LocalDate.now());
        request.setCategory("Salary");

        mockMvc.perform(post("/api/transactions")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTransactionFutureDate() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setDate(LocalDate.now().plusDays(10)); // Future date
        request.setCategory("Food");

        mockMvc.perform(post("/api/transactions")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetTransactionsFiltering() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        when(transactionService.getTransactions(eq(1L), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/transactions")
                .session(session)
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31")
                .param("categoryId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void testUpdateTransactionOwnershipIsolation() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setDate(LocalDate.now());
        request.setCategory("Food");

        when(transactionService.updateTransaction(eq(1L), eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Transaction not found")); // Because ownership constraint fails

        mockMvc.perform(put("/api/transactions/99")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteTransactionSuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        mockMvc.perform(delete("/api/transactions/1").session(session))
                .andExpect(status().isNoContent());
    }
}
