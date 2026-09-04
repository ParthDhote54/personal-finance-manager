package com.personal.finance.manager.transaction.service;

import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.category.entity.CategoryType;
import com.personal.finance.manager.category.repository.CategoryRepository;
import com.personal.finance.manager.exception.ResourceNotFoundException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    public void testCreateTransactionWithCategoryName() {
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(10L);
        category.setName("Food");
        category.setType(CategoryType.EXPENSE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndUserIdOrUserIdNull("Food", 1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(100L);
            return t;
        });

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setDate(LocalDate.now());
        request.setCategory("Food");
        request.setDescription("Lunch");

        TransactionResponse response = transactionService.createTransaction(1L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("50.00"), response.getAmount());
        assertEquals("Food", response.getCategory());
        assertEquals(TransactionType.EXPENSE, response.getType());
    }

    @Test
    public void testCreateTransactionWithNumericCategoryId() {
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(10L);
        category.setName("Salary");
        category.setType(CategoryType.INCOME);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserIdOrUserIdNull(10L, 1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(101L);
            return t;
        });

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setDate(LocalDate.now());
        request.setCategory("10");
        request.setDescription("Monthly Salary");

        TransactionResponse response = transactionService.createTransaction(1L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getAmount());
        assertEquals("Salary", response.getCategory());
        assertEquals(TransactionType.INCOME, response.getType());
    }

    @Test
    public void testGetTransactionsWithTypeFilter() {
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setName("Salary");
        category.setType(CategoryType.INCOME);

        Transaction txn = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .category(category)
                .user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(transactionRepository.findFilteredTransactions(user, null, null, null, TransactionType.INCOME))
                .thenReturn(List.of(txn));

        List<TransactionResponse> results = transactionService.getTransactions(1L, null, null, null, TransactionType.INCOME);

        assertEquals(1, results.size());
        assertEquals(TransactionType.INCOME, results.get(0).getType());
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
        when(categoryRepository.findByIdAndUserIdOrUserIdNull(100L, 1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("150.00"));
        request.setDate(LocalDate.of(2024, 2, 2)); // New date submitted
        request.setCategory("100");
        request.setDescription("Updated description");

        TransactionResponse response = transactionService.updateTransaction(1L, 99L, request);

        // Date should remain unchanged
        assertEquals(originalDate, response.getDate());
        assertEquals(new BigDecimal("150.00"), response.getAmount());
        assertEquals("Updated description", response.getDescription());
    }

    @Test
    public void testDeleteTransaction() {
        User user = new User();
        user.setId(1L);

        Transaction existingTransaction = Transaction.builder()
                .id(99L)
                .user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.of(existingTransaction));

        transactionService.deleteTransaction(1L, 99L);

        verify(transactionRepository, times(1)).delete(existingTransaction);
    }

    @Test
    public void testCreateTransactionInvalidCategoryThrowsIllegalArgumentException() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndUserIdOrUserIdNull("Unknown", 1L)).thenReturn(Optional.empty());

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setDate(LocalDate.now());
        request.setCategory("Unknown");

        assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(1L, request));
    }
}
