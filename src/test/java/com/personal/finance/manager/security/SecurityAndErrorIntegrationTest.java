package com.personal.finance.manager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.finance.manager.auth.dto.LoginRequest;
import com.personal.finance.manager.auth.dto.UserRegistrationRequest;
import com.personal.finance.manager.category.dto.CategoryRequest;
import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.category.entity.CategoryType;
import com.personal.finance.manager.category.repository.CategoryRepository;
import com.personal.finance.manager.goal.dto.GoalRequest;
import com.personal.finance.manager.goal.entity.Goal;
import com.personal.finance.manager.goal.repository.GoalRepository;
import com.personal.finance.manager.transaction.dto.TransactionRequest;
import com.personal.finance.manager.transaction.entity.Transaction;
import com.personal.finance.manager.transaction.entity.TransactionType;
import com.personal.finance.manager.transaction.repository.TransactionRepository;
import com.personal.finance.manager.user.entity.User;
import com.personal.finance.manager.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // Real security filters ENABLED!
@Transactional
public class SecurityAndErrorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;

    @BeforeEach
    public void setup() {
        transactionRepository.deleteAll();
        goalRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        userA = User.builder()
                .username("usera@example.com")
                .password(passwordEncoder.encode("Password123"))
                .fullName("User A")
                .phoneNumber("+1234567890")
                .isActive(true)
                .build();
        userA = userRepository.save(userA);

        userB = User.builder()
                .username("userb@example.com")
                .password(passwordEncoder.encode("Password123"))
                .fullName("User B")
                .phoneNumber("+0987654321")
                .isActive(true)
                .build();
        userB = userRepository.save(userB);
    }

    private MockHttpSession loginUser(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession();
    }

    // --- 1. AUTHENTICATION & ACCESS CONTROL TESTS ---

    @Test
    public void testProtectedEndpointWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRegisterWithoutAuthAllowed() throws Exception {
        UserRegistrationRequest reg = new UserRegistrationRequest();
        reg.setUsername("newuser@example.com");
        reg.setPassword("Password123");
        reg.setFullName("New User");
        reg.setPhoneNumber("+1122334455");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testLoginWithoutAuthAllowed() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("usera@example.com");
        loginRequest.setPassword("Password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void testLogoutWithoutAuthReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testProtectedEndpointAfterLogoutReturns401() throws Exception {
        MockHttpSession session = loginUser("usera@example.com", "Password123");

        // Request with active session works
        mockMvc.perform(get("/api/categories").session(session))
                .andExpect(status().isOk());

        // Logout
        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk());

        // Request with invalidated session fails
        mockMvc.perform(get("/api/categories").session(session))
                .andExpect(status().isUnauthorized());
    }

    // --- 2. CROSS-USER DATA ISOLATION TESTS ---

    @Test
    public void testUserACannotGetAndUpdateAndDeleteUserBTransaction() throws Exception {
        MockHttpSession sessionA = loginUser("usera@example.com", "Password123");

        Category categoryB = categoryRepository.save(Category.builder().name("CatB").type(CategoryType.EXPENSE).user(userB).build());
        Transaction txnB = transactionRepository.save(Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .type(TransactionType.EXPENSE)
                .category(categoryB)
                .user(userB)
                .build());

        // Update User B transaction as User A -> 404
        TransactionRequest updateReq = new TransactionRequest();
        updateReq.setAmount(new BigDecimal("200.00"));
        updateReq.setCategory("CatB");
        updateReq.setDate(LocalDate.now());

        mockMvc.perform(put("/api/transactions/" + txnB.getId())
                        .session(sessionA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // Delete User B transaction as User A -> 404
        mockMvc.perform(delete("/api/transactions/" + txnB.getId()).session(sessionA))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUserACannotGetAndUpdateAndDeleteUserBGoal() throws Exception {
        MockHttpSession sessionA = loginUser("usera@example.com", "Password123");

        Goal goalB = goalRepository.save(Goal.builder()
                .goalName("User B Goal")
                .targetAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .user(userB)
                .build());

        // Update Goal B as User A -> 404
        GoalRequest updateReq = new GoalRequest();
        updateReq.setGoalName("Hacked Goal");
        updateReq.setTargetAmount(new BigDecimal("9999.00"));
        updateReq.setTargetDate(LocalDate.now().plusYears(1));

        mockMvc.perform(put("/api/goals/" + goalB.getId())
                        .session(sessionA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // Delete Goal B as User A -> 404
        mockMvc.perform(delete("/api/goals/" + goalB.getId()).session(sessionA))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUserACannotDeleteUserBCategory() throws Exception {
        MockHttpSession sessionA = loginUser("usera@example.com", "Password123");

        categoryRepository.save(Category.builder().name("PrivateCatB").type(CategoryType.EXPENSE).user(userB).build());

        // Attempting to delete User B's category as User A -> 404
        mockMvc.perform(delete("/api/categories/PrivateCatB").session(sessionA))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUserAReportExcludesUserBTransactions() throws Exception {
        MockHttpSession sessionA = loginUser("usera@example.com", "Password123");

        Category categoryB = categoryRepository.save(Category.builder().name("Salary").type(CategoryType.INCOME).user(null).build());
        transactionRepository.save(Transaction.builder()
                .amount(new BigDecimal("10000.00"))
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .category(categoryB)
                .user(userB) // Belongs to User B
                .build());

        // User A's monthly report should show 0 net savings
        mockMvc.perform(get("/api/reports/monthly/" + LocalDate.now().getYear() + "/" + LocalDate.now().getMonthValue())
                        .session(sessionA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netSavings").value(0.00));
    }

    // --- 3. AUTHORIZATION TESTS ---

    @Test
    public void testDeletingDefaultCategoryReturns403() throws Exception {
        MockHttpSession sessionA = loginUser("usera@example.com", "Password123");

        categoryRepository.save(Category.builder().name("Rent").type(CategoryType.EXPENSE).user(null).build());

        mockMvc.perform(delete("/api/categories/Rent").session(sessionA))
                .andExpect(status().isForbidden());
    }

    // --- 4. MALFORMED REQUEST & ERROR HANDLING TESTS ---

    @Test
    public void testMalformedJsonReturns400() throws Exception {
        MockHttpSession sessionA = loginUser("usera@example.com", "Password123");

        mockMvc.perform(post("/api/categories")
                        .session(sessionA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": ")) // Malformed JSON
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    public void testInvalidPathVariableTypeReturns400() throws Exception {
        MockHttpSession sessionA = loginUser("usera@example.com", "Password123");

        mockMvc.perform(get("/api/reports/monthly/abc/1").session(sessionA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    public void testInvalidQueryParameterTypeReturns400() throws Exception {
        MockHttpSession sessionA = loginUser("usera@example.com", "Password123");

        mockMvc.perform(get("/api/transactions?type=INVALID_ENUM_VALUE").session(sessionA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    public void testBeanValidationFailureReturns400() throws Exception {
        MockHttpSession sessionA = loginUser("usera@example.com", "Password123");

        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("-100.00")); // Invalid negative amount
        req.setCategory("Food");
        req.setDate(LocalDate.now());

        mockMvc.perform(post("/api/transactions")
                        .session(sessionA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }
}
