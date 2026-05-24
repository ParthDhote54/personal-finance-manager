package com.personal.finance.manager.transaction.service;

import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.category.entity.CategoryType;
import com.personal.finance.manager.category.repository.CategoryRepository;
import com.personal.finance.manager.transaction.dto.TransactionRequest;
import com.personal.finance.manager.transaction.dto.TransactionResponse;
import com.personal.finance.manager.transaction.entity.Transaction;
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
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUpdateTransactionIgnoresDate() {
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setName("Food");
        category.setType(CategoryType.EXPENSE);

        LocalDate originalDate = LocalDate.of(2024, 1, 1);
        Transaction existingTransaction = Transaction.builder()
                .id(99L)
                .amount(new BigDecimal("100.00"))
                .date(originalDate)
                .category(category)
                .user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.of(existingTransaction));
        when(categoryRepository.findByNameAndUserId("Food", 1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("150.00"));
        request.setDate(LocalDate.of(2024, 2, 2)); // New date submitted
        request.setCategory("Food");
        request.setDescription("Updated description");

        TransactionResponse response = transactionService.updateTransaction(1L, 99L, request);

        // Date should remain unchanged
        assertEquals(originalDate, response.getDate());
        assertEquals(new BigDecimal("150.00"), response.getAmount());
        assertEquals("Updated description", response.getDescription());
    }
}
