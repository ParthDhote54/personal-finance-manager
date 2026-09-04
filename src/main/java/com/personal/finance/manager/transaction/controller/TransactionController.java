package com.personal.finance.manager.transaction.controller;

import com.personal.finance.manager.transaction.dto.TransactionListResponse;
import com.personal.finance.manager.transaction.dto.TransactionRequest;
import com.personal.finance.manager.transaction.dto.TransactionResponse;
import com.personal.finance.manager.transaction.dto.TransactionUpdateRequest;
import com.personal.finance.manager.transaction.entity.TransactionType;
import com.personal.finance.manager.transaction.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller handling user financial transaction management.
 * Provides endpoints for creating, retrieving, updating, and deleting transactions.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Creates a new financial transaction for the authenticated user.
     *
     * @param request transaction creation payload
     * @param session authenticated HTTP session
     * @return created transaction details
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        TransactionResponse response = transactionService.createTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves filtered financial transactions for the authenticated user.
     *
     * @param startDate  optional start date filter
     * @param endDate    optional end date filter
     * @param categoryId optional category ID filter
     * @param type       optional transaction type filter
     * @param session    authenticated HTTP session
     * @return wrapped list of transactions matching criteria
     */
    @GetMapping
    public ResponseEntity<TransactionListResponse> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TransactionType type,
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<TransactionResponse> response = transactionService.getTransactions(userId, startDate, endDate, categoryId, type);
        return ResponseEntity.ok(new TransactionListResponse(response));
    }

    /**
     * Updates an existing financial transaction for the authenticated user.
     *
     * @param id      transaction ID
     * @param request transaction update payload
     * @param session authenticated HTTP session
     * @return updated transaction details
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id, 
            @Valid @RequestBody TransactionUpdateRequest request, 
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        TransactionResponse response = transactionService.updateTransaction(userId, id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a financial transaction owned by the authenticated user.
     *
     * @param id      transaction ID
     * @param session authenticated HTTP session
     * @return success message response map
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        transactionService.deleteTransaction(userId, id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Transaction deleted successfully");
        return ResponseEntity.ok(response);
    }
}
