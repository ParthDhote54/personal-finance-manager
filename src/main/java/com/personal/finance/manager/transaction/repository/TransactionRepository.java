package com.personal.finance.manager.transaction.repository;

import com.personal.finance.manager.transaction.entity.Transaction;
import com.personal.finance.manager.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    Optional<Transaction> findByIdAndUser(Long id, User user);
    
    boolean existsByCategoryId(Long categoryId);
    
    // Using JPQL to handle dynamic filtering dates manually or via Specifications
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND " +
           "(:startDate IS NULL OR t.date >= :startDate) AND " +
           "(:endDate IS NULL OR t.date <= :endDate) AND " +
           "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
           "(:type IS NULL OR t.type = :type) " +
           "ORDER BY t.date DESC")
    List<Transaction> findFilteredTransactions(
            @Param("user") User user, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate, 
            @Param("categoryId") Long categoryId,
            @Param("type") com.personal.finance.manager.transaction.entity.TransactionType type);

    @Query("SELECT COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0) " +
           "FROM Transaction t WHERE t.user = :user AND t.date >= :startDate AND t.date <= :endDate")
    java.math.BigDecimal sumNetTransactionsBetweenDates(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t.category.name, t.type, SUM(t.amount) " +
           "FROM Transaction t " +
           "WHERE t.user = :user AND YEAR(t.date) = :year AND MONTH(t.date) = :month " +
           "GROUP BY t.category.name, t.type")
    List<Object[]> aggregateMonthlyTransactions(
            @Param("user") User user, 
            @Param("year") int year, 
            @Param("month") int month);

    @Query("SELECT t.category.name, t.type, SUM(t.amount) " +
           "FROM Transaction t " +
           "WHERE t.user = :user AND YEAR(t.date) = :year " +
           "GROUP BY t.category.name, t.type")
    List<Object[]> aggregateYearlyTransactions(
            @Param("user") User user, 
            @Param("year") int year);
}
