package com.personal.finance.manager.transaction.service;

import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.category.entity.CategoryType;
import com.personal.finance.manager.category.repository.CategoryRepository;
import com.personal.finance.manager.exception.AccessDeniedException;
import com.personal.finance.manager.exception.ResourceNotFoundException;
import com.personal.finance.manager.transaction.dto.TransactionRequest;
import com.personal.finance.manager.transaction.dto.TransactionResponse;
import com.personal.finance.manager.transaction.entity.Transaction;
import com.personal.finance.manager.transaction.entity.TransactionType;
import com.personal.finance.manager.transaction.repository.TransactionRepository;
import com.personal.finance.manager.user.entity.User;
import com.personal.finance.manager.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findByIdAndUserIdOrUserIdNull(Long.valueOf(request.getCategory()), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or doesn't belong to user"));

        TransactionType mappedType = category.getType() == CategoryType.INCOME ? TransactionType.INCOME : TransactionType.EXPENSE;

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .type(mappedType)
                .description(request.getDescription())
                .category(category)
                .user(user)
                .build();

        transaction = transactionRepository.save(transaction);
        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(Long userId, LocalDate startDate, LocalDate endDate, Long categoryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(user, startDate, endDate, categoryId);
        return transactions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public TransactionResponse updateTransaction(Long userId, Long transactionId, TransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        Category category = categoryRepository.findByIdAndUserIdOrUserIdNull(Long.valueOf(request.getCategory()), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or doesn't belong to user"));

        TransactionType mappedType = category.getType() == CategoryType.INCOME ? TransactionType.INCOME : TransactionType.EXPENSE;

        transaction.setAmount(request.getAmount());
        // Do NOT allow updating date per requirements
        transaction.setCategory(category);
        transaction.setType(mappedType);
        transaction.setDescription(request.getDescription());

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        transactionRepository.delete(transaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .category(transaction.getCategory().getName())
                .description(transaction.getDescription())
                .type(transaction.getType())
                .build();
    }
}
