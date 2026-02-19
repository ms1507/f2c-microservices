# Master Implementation Plan: Rural Marketplace Application

> [!IMPORTANT]
> **Strict Workflow:** 
> 1. No code generation without prior plan approval.
> 2. This document is the single source of truth for the development sequence.
> 3. We will proceed step-by-step, verifying each major component before moving to the next.

## 1. Prerequisites & Environment Setup
Before writing code, ensure the following are ready:

- **Java JDK:** 17 (LTS)
- **Maven:** 3.8+
- **Docker Desktop:** Installed and running (for DB/Broker containers).
- **IDE:** IntelliJ IDEA or Eclipse.
- **Git:** Version control initialized.

## 2. Development Sequence

We will build the application in **6 Sequential Phases**.

### Phase 1: Project Skeleton & Infrastructure
**Goal:** Create the Monorepo structure and set up the foundation.
1.  **Initialize Monorepo:** Create root folder `RuralMarketPlace-backend` and parent `pom.xml`.
2.  **Define Modules:** Create empty modules for `discovery-service`, `api-gateway`, `user-service`, etc.
3.  **Docker Compose:** specific `docker-compose.yml` for PostgreSQL, RabbitMQ/Kafka, Redis, and Zipkin.
4.  **Verification:** Run `mvn clean install` to ensure the reactor build works.

### Phase 1.5: Configuration Service
**Goal:** Externalize configuration from code.
1.  **Module Setup:** Create `config-service`.
2.  **Dependencies:** `spring-cloud-config-server`.
3.  **Config:** Point to a local Git repo or filesystem (native) for config properties.
4.  **Verification:** Services should fetch their `application.yml` from this server.

### Phase 2: Service Discovery & Gateway
**Goal:** Enable services to find each other and route traffic.
1.  **Discovery Service (Eureka):**
    -   Add dependencies (`spring-cloud-starter-netflix-eureka-server`).
    -   Enable `@EnableEurekaServer`.
    -   Configure `application.yml` (Port 8761).
2.  **API Gateway:**
    -   Add dependencies (`spring-cloud-starter-gateway`, `eureka-client`).
    -   Configure Routes (e.g., `/api/v1/auth/**` -> `lb://USER-SERVICE`).
    -   Configure CORS.
3.  **Verification:** Start both services. Access Eureka Dashboard at `http://localhost:8761`.

### Phase 3: Common Library & Shared Utilities
**Goal:** Eliminate code duplication.
1.  **Module Setup:** `common-library` module.
2.  **DTOs:** Create shared standard objects (e.g., `ApiResponse`, `ErrorResponse`).
3.  **Exceptions:** Define `GlobalExceptionHandler` and custom exceptions (`ResourceNotFoundException`).
4.  **Utils:** Shared security utils or date formatters.
5.  **Verification:** Build locally and ensure other services can depend on it.

### Phase 4: User Service (Authentication & Profiles)
**Goal:** Manage users and security.
1.  **DB Setup:** Configure PostgreSQL connection (`users_db`).
2.  **Migrations (Flyway):**
    -   Add `flyway-core` and `flyway-database-postgresql` dependencies.
    -   Create `db/migration/V1__init_schema.sql`.
    -   Disable Hibernate `ddl-auto`.
3.  **Entities:** `User`, `Role`, `Address`.
4.  **Security:** Implement JWT generation and validation (Spring Security).
4.  **API:** Register (`/auth/register`), Login (`/auth/login`), Get Profile.
5.  **Verification:** Test Registration and Login via Postman.

### Phase 5: Catalog Service (Core Business)
**Goal:** Product management.
1.  **DB Setup:** Configure PostgreSQL connection (`catalog_db`).
2.  **Entities:** `Category`, `Product`.
3.  **API:** CRUD endpoints for Products (Create, specific for Farmers).
4.  **Verification:** Create a product via Gateway and verify it persists.

### Phase 6: Order Service & Notifications (Transactions)
**Goal:** Order lifecycle and events.
1.  **Order Service:**
    -   Feign Client to check Product validity from Catalog.
    -   Create Order -> Deduct Stock -> Save Order.
    -   Publish `OrderPlacedEvent` to Broker.
2.  **Notification Service:**
    -   Listen to `OrderPlacedEvent`.
    -   Log/Send Notification.
3.  **Verification:** End-to-End flow: Login -> Search Product -> Buy Product -> Receive Notification.

---

## 3. Next Steps
**Current Status:** Planning Complete.
**Pending Action:** User Approval to start **Phase 1 (Project Skeleton)**.
