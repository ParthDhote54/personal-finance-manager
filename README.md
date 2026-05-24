# Personal Finance Manager API

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)

## 🚀 Live API
This backend is live and deployed on Render! 
**Base URL:** [https://personal-finance-api-z3nk.onrender.com](https://personal-finance-api-z3nk.onrender.com)

*(Note: The service is hosted on a free tier. The first request may take up to 60 seconds as the server wakes up from sleep mode.)*

## Overview
The Personal Finance Manager API is a secure, enterprise-grade backend designed for managing personal finances. It provides a robust set of features to track income and expenses, establish and monitor savings goals, and generate comprehensive financial reports.

## Tech Stack
* **Core Framework:** Java 21, Spring Boot 3.2.5
* **Security & Persistence:** Spring Security 6, Spring Data JPA
* **Database:** PostgreSQL (Production) / H2 Database (Development & Testing)
* **Build & Tooling:** Maven, Lombok, Docker

## Architecture
The application is built using a **Modular Monolith** approach with a strict **Package-by-Feature** architecture to ensure maintainability and high cohesion. It heavily leverages the **DTO (Data Transfer Object)** pattern to prevent sensitive domain entity data leakage.
Additionally, the project employs a **Multi-Environment configuration**, utilizing Spring Profiles to seamlessly switch between an H2 in-memory database for local development and a robust PostgreSQL database for production.

## Security Implementation
Security is a foundational pillar of this API:
* **Authentication:** Stateful Session-Based Authentication utilizing `JSESSIONID` cookies, implemented via Spring Security 6.
* **Password Security:** Robust password hashing using BCrypt.
* **IDOR Prevention:** Strong authorization checks to prevent Insecure Direct Object Reference (IDOR). Users can strictly only access and mutate their own data.
* **Error Handling:** A unified Global Exception Handling strategy that ensures no stack traces or internal implementation details are leaked to clients.

## Key Business Logic
* **Financial Precision:** Uses `BigDecimal` universally for all currency values to prevent floating-point precision errors.
* **Report Aggregation:** Supports dynamic financial summary aggregation on both a monthly and yearly basis.
* **Goal Tracking:** Automatically calculates and tracks progress towards savings goals dynamically based on the user's real-time net savings (Income - Expenses).

## API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/auth/register` | Register a new user account |
| **POST** | `/api/auth/login` | Authenticate and create a session |
| **POST** | `/api/auth/logout` | Terminate the current session |
| **GET** | `/api/categories` | Retrieve default and custom categories |
| **POST** | `/api/categories` | Create a custom category |
| **POST** | `/api/transactions` | Record a new income or expense transaction |
| **GET** | `/api/transactions` | Retrieve user transactions (supports filtering) |
| **POST** | `/api/goals` | Create a new financial savings goal |
| **GET** | `/api/goals` | Retrieve all savings goals and progress |
| **GET** | `/api/reports/summary?month={month}&year={year}` | Generate a monthly financial summary |
| **GET** | `/api/reports/yearly/{year}` | Generate a yearly financial summary |

## Dockerization
The project includes an optimized, **multi-stage Docker build**. The process isolates the heavy Maven build environment (used for resolving dependencies and compiling the JAR) from the runtime environment. The final runtime container uses a highly lightweight Alpine JRE image, drastically minimizing the final footprint and enhancing deployment speeds and security.

## Default Categories
To immediately provide value without requiring configuration, the system automatically seeds 8 global default categories:
* **Income:** Salary, Freelance, Investment, Other Income
* **Expense:** Food, Transport, Shopping, Bills

Using specifically crafted JPQL queries, these global default categories seamlessly coexist with user-specific custom categories, ensuring users have access to both defaults and their own personalized budget classifications simultaneously.
