# Personal Finance Manager - Complete Engineering Analysis

> [!IMPORTANT]
> **Document Purpose**  
> This is a comprehensive technical blueprint and engineering analysis for the Personal Finance Manager backend application. It serves as the definitive source of truth for system architecture, database schema, security mechanisms, API contracts, business logic, and project state.

---

## 1. PROJECT UNDERSTANDING

The Personal Finance Manager is a production-grade RESTful backend system built with Java 17 and Spring Boot 3.x. It enables users to track their financial life by recording transactions (income and expenses), managing budget categories, setting savings goals, and viewing financial reports over different time horizons. 

**Target Audience:** Individuals seeking to take control of their personal finances through a secure, structured, and easy-to-use application.
**Core Workflows:**
1. **User Onboarding & Session Management:** Users register and log in. The system establishes a stateful, secure session (no JWT).
2. **Category Management:** Users can utilize system-default categories (e.g., Groceries, Rent) or define custom categories isolated to their account.
3. **Transaction Logging:** Users log income and expenses, strictly tied to a date, amount, and category.
4. **Goal Setting & Tracking:** Users set financial targets over a specified date range. The system computes real-time progress based on existing transactions within that window.
5. **Reporting:** The system aggregates transactions into monthly and yearly summaries.

**Non-Functional Requirements:**
- **Security:** Strict user data isolation. User A cannot access or modify User B's entities.
- **Data Integrity:** Accurate financial arithmetic using `BigDecimal` to prevent floating-point anomalies.
- **Performance:** Prevention of N+1 query problems via optimal JPA fetching strategies (`FetchType.LAZY`).
- **Reliability:** Comprehensive exception handling and transaction boundaries.

---

## 2. COMPLETE FEATURE BREAKDOWN

### Auth Module
- **Purpose:** Handles user registration, authentication, and session lifecycle.
- **Responsibilities:** Validates incoming credentials, creates the user entity, hashes passwords, authenticates against `AuthenticationManager`, and creates an HTTP session.
- **Business Rules:** Usernames must be unique. Passwords must be strongly hashed using BCrypt.
- **Database Impact:** Creates `User` records.

### Category Module
- **Purpose:** Defines the taxonomy for transactions.
- **Business Rules:** System categories (where `user_id` is null) are globally readable but immutable by users. Users can create custom categories. A unique constraint ensures a user cannot have two categories with the exact same name.
- **Edge Cases:** Attempting to delete a category that has linked transactions must be blocked (handled by `CategoryInUseException`).

### Transaction Module
- **Purpose:** The core ledger. Records money flowing in and out.
- **Business Rules:** Transactions cannot have future dates (validation logic required). Must be tied to a valid category owned by the user or a system category.
- **Database Impact:** Highly writes-heavy. Indexed heavily on `user_id` and `date`.

### Savings Goals Module
- **Purpose:** Defines financial targets (e.g., Save $5000 for a vacation by December).
- **Business Rules:** Requires a start date, end date, and target amount. Progress is NOT stored in the database but calculated dynamically by summing income/savings transactions during the active date range.
- **Validations:** Start date must be before end date. Target amount > 0.

### Reports Module
- **Purpose:** Aggregates transaction data.
- **Business Rules:** Generates Monthly and Yearly summaries detailing total income, total expenses, and net savings.

---

## 3. ENTERPRISE ARCHITECTURE DESIGN

The system follows a strict **Modular Monolith** pattern organized by layered architecture:

- **Presentation Layer (Controllers):** `[Feature]Controller` classes handle HTTP request routing, path variable extraction, and response formatting via standardized `ApiResponse<T>` wrappers.
- **Data Transfer Layer (DTOs):** Absolute separation between API contracts and DB entities. Using `[Feature]Request` and `[Feature]Response` DTOs mapped via MapStruct to prevent over-posting vulnerabilities.
- **Business Layer (Services):** `[Feature]Service` interfaces and implementations contain business rules, authorization checks (verifying the logged-in user owns the resource), and exception throwing.
- **Persistence Layer (Repositories):** Spring Data JPA `[Feature]Repository` interfaces containing custom derived queries and `@Query` annotations.

**Request Lifecycle:**
1. Request hits `SecurityFilterChain`. Validates `JSESSIONID` cookie.
2. Spring maps request to Controller.
3. Request payload is validated using `@Valid` (JSR-380).
4. Controller invokes Service layer, passing the authenticated user context.
5. Service executes business logic and interacts with the Repository.
6. Service maps Entity to Response DTO using MapStruct.
7. Controller wraps Response DTO in `ApiResponse` and returns HTTP 200/201.

**Exception Handling:**
A `GlobalExceptionHandler` (`@RestControllerAdvice`) catches custom exceptions (`ResourceNotFoundException`, `AccessDeniedException`) and standard Spring exceptions, returning structured JSON error payloads with appropriate HTTP status codes (404, 403, 400).

---

## 4. DATABASE DESIGN

Database chosen: **PostgreSQL** for production, **H2** for testing/local dev.

### Entity Relationship & Tables

**1. `users` Table**
- `id` (PK, BIGINT, auto-increment)
- `username` (VARCHAR 100, UNIQUE, NOT NULL)
- `password` (VARCHAR, NOT NULL)
- `full_name` (VARCHAR 100, NOT NULL)
- `is_active` (BOOLEAN, DEFAULT TRUE)
- `created_at`, `updated_at` (TIMESTAMP)

**2. `categories` Table**
- `id` (PK, BIGINT)
- `name` (VARCHAR 50, NOT NULL)
- `type` (ENUM: 'INCOME', 'EXPENSE')
- `user_id` (FK to users, NULLABLE) -> *If null, represents a system-wide default category.*
- *Constraints:* UNIQUE(`name`, `user_id`)

**3. `transactions` Table**
- `id` (PK, BIGINT)
- `amount` (NUMERIC(12,2), NOT NULL)
- `date` (DATE, NOT NULL)
- `type` (ENUM)
- `description` (VARCHAR 255)
- `category_id` (FK to categories)
- `user_id` (FK to users)
- *Indexes:* `idx_user_date` on (`user_id`, `date`) for fast report aggregation.

**4. `goals` Table**
- `id` (PK, BIGINT)
- `goal_name` (VARCHAR 100)
- `description` (TEXT)
- `target_amount` (NUMERIC(12,2))
- `start_date`, `end_date` (DATE)
- `user_id` (FK to users)
- *Indexes:* `idx_goal_user` on (`user_id`).

**Ownership Enforcement:**
Every entity (except system categories) holds a mandatory foreign key to `users`. JPA queries must ALWAYS append `AND user = :user` to prevent insecure direct object reference (IDOR).

---

## 5. SECURITY DESIGN

**Authentication Style:** Stateful Session-based Authentication via Spring Security.
**Why no JWT?** For a monolith with an expected 1:1 coupling between browser client and server, session cookies (`JSESSIONID`) provide simpler revocation, implicit token refresh, and better defense against XSS without the complexity of JWT rotation.

**Security Lifecycle:**
1. **Login:** User submits credentials to `/api/auth/login`. `AuthenticationManager` verifies the BCrypt hash. Upon success, Spring Security establishes a session context in `SecurityContextHolder`. A `JSESSIONID` cookie is returned (`HttpOnly`).
2. **Authorization:** Endpoints (except `/api/auth/**`) require authentication. 
3. **Data Ownership:** Fetching the authenticated user occurs inside the Controller: `(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()`. This user object is passed to services to ensure queries are strictly scoped.
4. **CSRF:** Disabled temporarily as this is a pure REST API assignment. In a real-world session-based production app, CSRF tokens MUST be enabled and managed via header exchanges.
5. **Concurrency:** `maximumSessions(1)` is configured to prevent the same user from logging in concurrently from multiple locations.

---

## 6. API DESIGN REPORT

**Auth Endpoints:**
- `POST /api/auth/register` -> `UserRegistrationRequest` | Returns `201 Created`
- `POST /api/auth/login` -> `UserLoginRequest` | Returns `200 OK` + JSESSIONID cookie.

**Category Endpoints:**
- `GET /api/categories` -> Returns `List<CategoryResponse>` (Combines User's custom + System defaults)
- `POST /api/categories` -> `CategoryRequest` | Creates user-specific category.

**Transaction Endpoints:**
- `POST /api/transactions` -> `TransactionRequest` | Validates category belongs to user/system. Returns `201`.
- `GET /api/transactions` -> Supports pagination & filtering (`?startDate=&endDate=&categoryId=`).
- `GET /api/transactions/{id}` -> Returns `TransactionResponse`.
- `DELETE /api/transactions/{id}` -> Verifies ownership before deletion.

**Goal Endpoints:**
- `POST /api/goals` -> `GoalRequest` | Returns `201 Created`.
- `GET /api/goals` -> Returns `List<GoalResponse>`. Includes dynamically calculated `currentAmount` and `percentageCompleted`.

**Report Endpoints:**
- `GET /api/reports/monthly?year=2024&month=5` -> `ReportResponse`
- `GET /api/reports/yearly?year=2024` -> `ReportResponse`

---

## 7. BUSINESS LOGIC ANALYSIS

**Transaction Validation Rules:**
- Amount must be strictly greater than 0.
- Date cannot be in the future (Checked via `@PastOrPresent` validation or manual service logic).

**Goal Progress Calculation:**
When querying goals, the `GoalService` does the following:
1. Fetch the goal entity.
2. Query `TransactionRepository` for the sum of `INCOME` transactions (or a specific savings category type) for the given user, where `date >= goal.startDate` AND `date <= goal.endDate`.
3. Set `currentAmount` = SUM.
4. Calculate `percentageCompleted = (currentAmount / targetAmount) * 100`.

**Financial Aggregation (Reports):**
The `ReportService` queries the database for transactions within the specified date boundaries.
- Total Income = Sum of all `INCOME` transactions.
- Total Expense = Sum of all `EXPENSE` transactions.
- Net Savings = Total Income - Total Expense.
*(Calculations done via JPQL `SUM()` to offload work to the DB rather than doing it in-memory via Java Streams, improving memory efficiency).*

---

## 8. EDGE CASES & FAILURE ANALYSIS

> [!WARNING]
> **Critical Edge Cases to Handle:**
1. **Category Deletion:** If a user deletes a custom category, what happens to existing transactions tied to it? 
   *Strategy:* The service must query `TransactionRepository.existsByCategoryId()`. If true, throw `CategoryInUseException`.
2. **Data Isolation (IDOR):** A malicious user passes `GET /api/transactions/5` where ID 5 belongs to another user.
   *Strategy:* Repositories must query via `findByIdAndUser(id, user)`. Throw `ResourceNotFoundException` if no match.
3. **Floating Point Arithmetic:** Using double/float for money leads to precision loss.
   *Strategy:* `BigDecimal` is strictly enforced at the Entity and DTO levels.
4. **Timezone Discrepancies:** A transaction logged at 11 PM locally might register as the next day on the server.
   *Strategy:* Standardize API inputs on ISO-8601 strings and force UTC handling or rely strictly on `LocalDate` mapped directly from the client.

---

## 9. TESTING STRATEGY

**Unit Tests (Mockito + JUnit 5):**
- **Service Layer:** Mocks the Repository layer. Verifies business logic (e.g., Goal progress math, ownership exception throwing).
- **Controller Layer:** `@WebMvcTest`. Mocks the Service layer. Tests HTTP routing, `@Valid` constraint triggers, and JSON serialization.

**Critical Test Scenarios:**
- Unauthorized user attempting to access endpoints.
- Authenticated user attempting to modify another user's goal/transaction.
- Goal percentage calculation when current savings > target amount (ensure it caps or displays >100% properly).
- Creation of duplicate categories throwing `DuplicateResourceException`.

---

## 10. DEPLOYMENT & DEVOPS

- **Platform:** Render.com (Web Service) + Render PostgreSQL database.
- **Docker Strategy:** Standard `Dockerfile` using a multi-stage build (Maven base for build -> Eclipse Temurin 17 JRE for runtime).
- **Environment Variables:** 
  - `SPRING_PROFILES_ACTIVE=prod`
  - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- **Build Process:** `mvn clean package -DskipTests` (tests run in CI).
- **Database Migrations:** Currently relying on `spring.jpa.hibernate.ddl-auto=update` based on `application-prod.yml`. For real production, integrating Flyway/Liquibase is highly recommended.

---

## 11. CURRENT PROJECT STATE ANALYSIS

**COMPLETED ITEMS:**
- Foundational directory structure established (`config`, `controller`, `dto`, `entity`, `exception`, `repository`, `security`, `service`).
- All JPA Entities created (`User`, `Transaction`, `Goal`, `Category`, `TransactionType`).
- Basic DTOs, Security Configuration (`SecurityConfig`, `CustomUserDetailsService`), and Global Exception Handler are implemented.
- `CategorySeeder` is present to initialize default categories.
- Controller and Service skeletons exist.
- Foundational Unit tests for `AuthController`, `CategoryController`, `GoalController`, `TransactionController`, `CategoryService`, `GoalService`, `TransactionService` exist.

**REMAINING ITEMS (Development Priorities):**
1. **Implementation of `ReportService` and `ReportController`.** (Currently missing tests and likely missing implementation details).
2. **DTO mapping logic:** Ensure MapStruct interfaces are fully wired.
3. **Service Logic Depth:** Verification that IDOR protections (`AndUser`) are implemented inside all service methods.
4. **Goal Progress Logic:** Verify dynamic calculations are implemented in `GoalService`.
5. **Testing Coverage:** Add tests for `AuthService`, `ReportService`, and Repositories.

---

## 12. DEVELOPMENT ROADMAP

**Phase 1: Core & Security Audit (Sprint 1)**
- Verify `AuthService` and session management are robust.
- Ensure `GlobalExceptionHandler` covers all edge cases natively.

**Phase 2: Transactions & Categories (Sprint 2)**
- Solidify `CategoryService` (Default vs Custom logic).
- Implement `TransactionService` with strict validation (No future dates, valid category mapping).
- Lock down repository methods (append `User` entity to all lookups).

**Phase 3: Business Logic Heavy Modules (Sprint 3)**
- Implement `GoalService` dynamic progress calculators.
- Implement `ReportService` aggregation queries using JPQL sum/grouping.
- Add comprehensive Unit tests for Reports and Goals.

**Phase 4: Pre-Deployment & DevOps (Sprint 4)**
- Write/Refine Dockerfile.
- Configure Render environment variables.
- Run full suite of Postman/Integration tests against a staging H2 DB.

---

## 13. INTERVIEW & EVALUATION PERSPECTIVE

If this assignment is being reviewed by technical evaluators:
- **What creates a strong impression:** Proper use of `BigDecimal`, preventing N+1 queries using `FetchType.LAZY` on `@ManyToOne`, using DTOs to separate API surface from Database schema, and implementing robust Global Exception Handling.
- **Red Flags:** Exposing DB Entities directly from Controllers, returning generic 500 errors instead of clean 400/404/403 payloads, calculating DB aggregations in Java memory instead of via SQL, or failing to isolate data per user.
- **Overengineering to Avoid:** Do not add Kafka, Redis caching, or microservices splits for this scope. A clean, highly-tested Modular Monolith is the gold standard here.

---

## 14. FINAL EXECUTION BLUEPRINT

**Execution Checklist:**
- [ ] Review existing Service classes to ensure all methods strictly require a `User` object for context.
- [ ] Implement `ReportService.getMonthlyReport` and `getYearlyReport` using optimized JPA queries.
- [ ] Validate `CategorySeeder` runs only on first startup (check if DB is empty).
- [ ] Review `pom.xml` to ensure MapStruct and Lombok play nicely (annotation processor paths).
- [ ] Run `mvn clean test` and verify 100% pass rate on existing tests.
- [ ] Create Postman collection covering standard user flow (Register -> Login -> Create Custom Category -> Add Transaction -> View Reports).
- [ ] Containerize via Docker and deploy to Render.

This document represents the complete analysis and execution plan. It is detailed enough to guide immediate implementation and testing phases.
