# Personal Finance Manager - Backend API

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)

An enterprise-grade, production-ready backend API for a Personal Finance Management application. This system provides strict data isolation, dynamic financial aggregations, dynamic goal tracking, and robust session-based authentication.

## 🏛️ Architecture Overview

This project implements a **Modular Monolith Architecture** utilizing **Package-by-Feature** organization. 

### Why Modular Monolith?
Instead of a traditional `controllers/`, `services/`, `repositories/` layer spread, the codebase is grouped by domain features (`auth`, `user`, `transaction`, `category`, `goal`, `report`). This enforces strict domain boundaries, prevents "spaghetti code", makes the codebase highly cohesive, and allows individual modules to be easily extracted into microservices in the future if required.

## 🛠️ Tech Stack

- **Core:** Java 17, Spring Boot 3.x
- **Data Persistence:** Spring Data JPA, Hibernate, PostgreSQL (Production), H2 (Development)
- **Security:** Spring Security (Session-Cookie based)
- **Validation:** Spring Boot Starter Validation (`jakarta.validation`)
- **DevOps:** Docker (Multi-stage build), Render (Cloud hosting)
- **Testing:** JUnit 5, Mockito, Spring MockMvc

## 🔒 Security & Authentication

The API uses **Stateful Session Authentication (`JSESSIONID`)** rather than JWTs. 

### Why Session Auth?
- **Immediate Revocation:** Logging out instantly invalidates the session on the server-side, preventing token theft replays.
- **Strict CORS & Cookies:** The system relies on `httpOnly` and `SameSite=Lax` cookies, meaning the browser automatically handles the transmission of the auth token, drastically reducing XSS vulnerability surfaces.
- **Ownership Isolation:** Every single data access query (e.g., `findByIdAndUser`) inherently relies on the injected `USER_ID` from the active server session, making Insecure Direct Object Reference (IDOR) attacks mathematically impossible.

## 🗄️ Database Design

- **Users:** Stores credentials and profiles.
- **Categories:** Supports both Global Default categories (shared across all users) and Custom Categories (owned by specific users).
- **Transactions:** Associated with a Category and a User. The `TransactionType` (INCOME/EXPENSE) is inferred dynamically from the Category.
- **Goals:** Tracks target amounts and timeframes. *Progress is never stored statically*; it is dynamically computed using JPQL aggregations on the `Transaction` table to ensure 100% data consistency.

---

## 🚀 Running Locally

### Prerequisites
- Java 17
- Maven
- Docker (optional)

### Method 1: Using Maven (Development Profile)
This method utilizes the in-memory H2 database.
```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Method 2: Using Docker (Production Profile)
This builds the multi-stage lightweight Alpine container.
```bash
docker build -t finance-manager-api .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://<host>:5432/<db> \
  -e DB_USERNAME=<username> \
  -e DB_PASSWORD=<password> \
  finance-manager-api
```

---

## 📚 API Endpoints

### 🔐 Auth Module
- `POST /api/auth/register` - Register a new user.
- `POST /api/auth/login` - Authenticate and receive `JSESSIONID` cookie.
- `POST /api/auth/logout` - Invalidate active session.

### 🏷️ Category Module
- `GET /api/categories` - Fetch global defaults + user's custom categories.
- `POST /api/categories` - Create a custom category.
- `DELETE /api/categories/{name}` - Delete a custom category (Defaults cannot be deleted).

### 💸 Transaction Module
- `POST /api/transactions` - Log an income or expense.
- `GET /api/transactions` - Fetch transactions (Supports `startDate`, `endDate`, `categoryId` filters).
- `PUT /api/transactions/{id}` - Update a transaction (Note: `date` cannot be modified).
- `DELETE /api/transactions/{id}` - Delete a transaction.

### 🎯 Goal Module
- `POST /api/goals` - Set a new savings goal.
- `GET /api/goals` - Retrieve all goals with **dynamically calculated progress**.
- `GET /api/goals/{id}` - Retrieve specific goal.
- `PUT /api/goals/{id}` - Update a goal's target amount or date.
- `DELETE /api/goals/{id}` - Remove a goal.

### 📊 Report Module
- `GET /api/reports/monthly/{year}/{month}` - Get dynamic aggregations of income/expenses for a specific month.
- `GET /api/reports/yearly/{year}` - Get dynamic aggregations for an entire year.

*(See the included Postman Collection for exact payload schemas).*

---

## 🧪 Testing Strategy

The project features a comprehensive suite of automated tests designed for headless CI/CD environments.
- **Controller Tests (`MockMvc`):** Validates precise JSON contract mappings, status codes (200, 201, 400, 401, 403, 404, 409), and endpoint security.
- **Service Tests (`Mockito`):** Isolates mathematical logic, such as proving the system handles negative net savings securely without crashing, and validating that cross-user data leakage is impossible.

Run tests using:
```bash
mvn clean test
```

## ☁️ Deployment

This backend is optimized for PaaS deployments like **Render** or **Heroku**.
1. Set the Build Command: `mvn clean package -DskipTests`
2. Set the Start Command: `java -jar target/*.jar`
3. Inject the `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` variables linking to your managed PostgreSQL instance.
4. The system will automatically detect the dynamic `$PORT` variable and bind successfully.
