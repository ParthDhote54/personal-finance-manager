# Finalization, DevOps & Deployment Plan

This document details the final steps required to transform the Personal Finance Manager backend into a production-grade, containerized, and professionally documented system.

## User Review Required

> [!IMPORTANT]
> **CORS & Authentication Configurations**
> Since this application uses session-based authentication (Cookies), I will configure CORS to explicitly support `allowCredentials(true)` and permit a standard list of origins (e.g. `localhost`, `vercel.app`, `onrender.com`). I will also update `SecurityConfig` to explicitly manage this CORS filter. 

> [!WARNING]
> **Environment Variables**
> I will map PostgreSQL connections to standard environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. For Render compatibility, I will use `PORT` for the server port in the `application-prod.yml`.

## Open Questions
- None. The requirements for Docker, Render, PostgreSQL, and README are crystal clear.

## Proposed Changes

---

### Part 1: Dockerization

#### [NEW] `Dockerfile`
- Multi-stage build.
- **Stage 1 (Builder):** Uses `maven:3.9.6-eclipse-temurin-17` to run `mvn clean package -DskipTests`. (We skip tests during Docker build to speed up CI/CD since tests run separately).
- **Stage 2 (Runtime):** Uses `eclipse-temurin:17-jre-alpine` for a lightweight runtime. Exposes port 8080 (or $PORT).
- Command: `java -jar app.jar`.

#### [NEW] `.dockerignore`
- Ignore `target/`, `.git/`, `.idea/`, `.vscode/`, `logs/`, etc.

---

### Part 2 & 3: Spring Profiles & PostgreSQL Config

#### [MODIFY] `src/main/resources/application.yml`
- Explicitly map `server.port: ${PORT:8080}` to support dynamic port injection on platforms like Render.

#### [MODIFY] `src/main/resources/application-prod.yml`
- Configure `spring.datasource` to use PostgreSQL.
- Link variables: `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`.
- Configure safe `ddl-auto: update` (or `validate`).
- Tweak `spring.jpa.properties.hibernate.jdbc.batch_size` for production pooling if needed.

---

### Part 4: Render Deployment

#### [NEW] `render.yaml`
- Define a Web Service running Docker or Java natively.
- Inject placeholder environment variables (`DB_URL`, etc.).

---

### Part 5: CORS & Security Configurations

#### [MODIFY] `src/main/java/com/personal/finance/manager/security/SecurityConfig.java`
- Inject a `CorsConfigurationSource` Bean.
- Attach `.cors(c -> c.configurationSource(corsConfigurationSource()))` to the `HttpSecurity` chain to allow frontend web apps to successfully handle `JSESSIONID` cookies.

---

### Part 6 & 7: README & API Documentation

#### [MODIFY] `README.md`
- Completely overhaul the file to present as an enterprise-grade architectural blueprint.
- Sections: Overview, Architecture (Modular Monolith), Tech Stack, Security (Why session auth), Database, Docker, Render Deployment, and detailed endpoint documentation.

---

### Part 8: Postman Collection

#### [NEW] `Personal_Finance_Manager.postman_collection.json`
- Create a fully structured JSON file containing all endpoints grouped by Feature (Auth, Category, Transaction, Goal, Report).
- Configure the collection to automatically capture and pass cookies.

---

### Part 9: Final Cleanup

- Run a regex-based script or manual sweep to verify zero unused imports and zero `@TODO` markers remain.

## Verification Plan

- Inspect `Dockerfile` for correct multi-stage syntax.
- Verify `application-prod.yml` connects to PostgreSQL correctly.
- Ensure the `README.md` looks incredibly professional and matches standard open-source enterprise projects.
