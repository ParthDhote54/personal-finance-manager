package com.personal.finance.manager.transaction.repository;

import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.category.entity.CategoryType;
import com.personal.finance.manager.category.repository.CategoryRepository;
import com.personal.finance.manager.transaction.entity.Transaction;
import com.personal.finance.manager.transaction.entity.TransactionType;
import com.personal.finance.manager.user.entity.User;
import com.personal.finance.manager.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findFilteredTransactions_shouldFilterProperly() {
        // Setup
        User user = User.builder()
                .username("txnuser")
                .password("password")
                .fullName("Txn User")
                .build();
        entityManager.persist(user);

        Category incomeCat = Category.builder()
                .name("Salary")
                .type(CategoryType.INCOME)
                .user(user)
                .build();
        entityManager.persist(incomeCat);

        Category expenseCat = Category.builder()
                .name("Groceries")
                .type(CategoryType.EXPENSE)
                .user(user)
                .build();
        entityManager.persist(expenseCat);

        Transaction incomeTxn = Transaction.builder()
                .amount(new BigDecimal("1000"))
                .type(TransactionType.INCOME)
                .category(incomeCat)
                .description("Paycheck")
                .date(LocalDate.now())
                .user(user)
                .build();
        entityManager.persist(incomeTxn);

        Transaction expenseTxn = Transaction.builder()
                .amount(new BigDecimal("50"))
                .type(TransactionType.EXPENSE)
                .category(expenseCat)
                .description("Food")
                .date(LocalDate.now())
                .user(user)
                .build();
        entityManager.persist(expenseTxn);

        entityManager.flush();

        // Assert: Querying with only User ID returns both
        List<Transaction> allTxns = transactionRepository.findFilteredTransactions(user, null, null, null);
        assertThat(allTxns).hasSize(2);

        // Assert: Querying with INCOME categoryId returns only INCOME
        List<Transaction> incomeTxns = transactionRepository.findFilteredTransactions(user, null, null, incomeCat.getId());
        assertThat(incomeTxns).hasSize(1);
        assertThat(incomeTxns.get(0).getCategory().getId()).isEqualTo(incomeCat.getId());
    }
}
