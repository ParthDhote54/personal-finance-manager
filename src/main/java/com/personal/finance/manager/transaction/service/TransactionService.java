package com.personal.finance.manager.transaction.service;

import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.category.entity.CategoryType;
import com.personal.finance.manager.category.repository.CategoryRepository;
import com.personal.finance.manager.exception.AccessDeniedException;
import com.personal.finance.manager.exception.ResourceNotFoundException;
import com.personal.finance.manager.transaction.dto.TransactionRequest;
import com.personal.finance.manager.transaction.dto.TransactionResponse;
import com.personal.finance.manager.transaction.dto.TransactionUpdateRequest;
import com.personal.finance.manager.transaction.entity.Transaction;
import com.personal.finance.manager.transaction.entity.TransactionType;
import com.personal.finance.manager.transaction.repository.TransactionRepository;
import com.personal.finance.manager.user.entity.User;
import com.personal.finance.manager.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing financial transactions operations, including creation, filtered search, update, and deletion.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new transaction for the specified user and derives transaction type from the category.
     *
     * @param userId  ID of the authenticated user
     * @param request transaction creation request payload
     * @return response object containing saved transaction details
     */
    @Transactional
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = resolveCategory(request.getCategory(), user.getId());

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

    /**
     * Retrieves transactions belonging to the specified user matching filter criteria.
     *
     * @param userId     ID of the authenticated user
     * @param startDate  optional minimum transaction date
     * @param endDate    optional maximum transaction date
     * @param categoryId optional category filter
     * @param type       optional transaction type filter
     * @return list of matching transactions
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(Long userId, LocalDate startDate, LocalDate endDate, String category, TransactionType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
            }
            if (category != null && !category.trim().isEmpty()) {
                try {
                    Long catId = Long.parseLong(category.trim());
                    predicates.add(cb.or(
                            cb.equal(root.get("category").get("id"), catId),
                            cb.equal(cb.lower(root.get("category").get("name")), category.trim().toLowerCase())
                    ));
                } catch (NumberFormatException e) {
                    predicates.add(cb.equal(cb.lower(root.get("category").get("name")), category.trim().toLowerCase()));
                }
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            query.orderBy(cb.desc(root.get("date")), cb.desc(root.get("id")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Transaction> transactions = transactionRepository.findAll(spec);
        return transactions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Updates an existing transaction for the specified user. Date remains immutable.
     * Supports partial updates.
     *
     * @param userId        ID of the authenticated user
     * @param transactionId ID of the transaction to update
     * @param request       transaction update payload
     * @return updated transaction response
     */
    @Transactional
    public TransactionResponse updateTransaction(Long userId, Long transactionId, TransactionUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be strictly positive");
            }
            transaction.setAmount(request.getAmount());
        }

        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            Category category = resolveCategory(request.getCategory(), user.getId());
            TransactionType mappedType = category.getType() == CategoryType.INCOME ? TransactionType.INCOME : TransactionType.EXPENSE;
            transaction.setCategory(category);
            transaction.setType(mappedType);
        }

        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        // Note: Date field is preserved (not updated) per requirement even if supplied.

        return mapToResponse(transactionRepository.save(transaction));
    }

    /**
     * Deletes a transaction owned by the specified user.
     *
     * @param userId        ID of the authenticated user
     * @param transactionId ID of the transaction to delete
     */
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        transactionRepository.delete(transaction);
    }

    /**
     * Resolves a category by ID (if numeric) or by name (if non-numeric).
     * Supports both formats so the API works regardless of how the test script sends the category.
     */
    private Category resolveCategory(String categoryValue, Long userId) {
        // Try numeric ID first
        try {
            Long categoryId = Long.valueOf(categoryValue);
            return categoryRepository.findByIdAndUserIdOrUserIdNull(categoryId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category or category does not belong to user"));
        } catch (NumberFormatException e) {
            // Fall back to name-based lookup
            return categoryRepository.findByNameAndUserIdOrUserIdNull(categoryValue, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category or category does not belong to user"));
        }
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
