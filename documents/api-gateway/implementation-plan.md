# API Gateway Implementation Plan

## 1. Goal
Implement the **API Gateway** as the single entry point for the Rural Marketplace Microservices ecosystem. It will handle efficient routing to downstream services (`user-service`, `catalog-service`) and enforce centralized security policies (JWT Authentication).

**Reference:** Section 1.3 of `Application-Development-Plan.md`.

## 2. Architecture Overview
*   **Technology:** Spring Cloud Gateway
*   **Port:** `8080` (Standard HTTP port, exposing the app)
*   **Discovery:** Netflix Eureka Client (Simpler routing by Service ID)
*   **Security:** Global/Custom Filter for JWT Validation

## 3. Prerequisite Checks
*   [x] `pom.xml` dependencies (`spring-cloud-starter-gateway`, `spring-cloud-starter-netflix-eureka-client`) are present (Verified).
*   [ ] `common-library` setup (Required for sharing JWT logic).

## 4. Implementation Steps

### Step 1: Core Configuration
Configure `application.yml` to:
1.  Name the application `api-gateway`.
2.  Register with Eureka (`discovery-service`).
3.  Enable Discovery Locator (optional, but specific routes are preferred for control).

### Step 2: Refactor Security Logic (Common Library)
To ensure consistent Token Generation (User Service) and Token Validation (Gateway), we must share the JWT logic.
1.  **Initialize `common-library`**: Ensure it has a valid source structure.
2.  **Move JWT Code**: Move `JwtService` (and related `JwtAuthenticationFilter` helpers if generic) from `user-service` to `common-library`.
3.  **Update Dependencies**: 
    *   Add `jjwt` (Java JWT) dependencies to `common-library`.
    *   Make `user-service` and `api-gateway` depend on `common-library`.

### Step 3: Implement Authentication Filter (The "Bouncer")
Create a custom Gateway Filter `AuthenticationFilter.java` in `api-gateway`.
*   **Logic**:
    1.  Intercept every request.
    2.  Check for `Authorization` header.
    3.  Validate Token using `JwtService` (from common-lib).
    4.  If valid: Extract claims (userID, role) and mutate request header (e.g., `X-User-Id`) to pass context downstream.
    5.  If invalid/missing: Return `401 Unauthorized`.
*   **Whitelisting**: Define "Open" endpoints that don't need auth (e.g., `/api/v1/auth/register`, `/api/v1/auth/login`, `/api/v1/products/search`).

### Step 4: Define Routes
Configure routes in `application.yml` (or Java Config).

| ID | Path Pattern | Target Service | Filters |
| :--- | :--- | :--- | :--- |
| `user-service` | `/api/v1/auth/**` | `lb://user-service` | (None - Public) |
| `user-service-secured` | `/api/v1/users/**` | `lb://user-service` | `AuthenticationFilter` |
| `catalog-service` | `/api/v1/products/**` | `lb://catalog-service` | (Optional: Auth for Writes, Public for Reads) |
| `catalog-categories` | `/api/v1/categories/**`| `lb://catalog-service` | (Optional Auth) |

### Step 5: Cross-Cutting Concerns
1.  **Correlation ID**: Implement a `GlobalFilter` to generate or propagate a `X-Correlation-Id` header for tracing requests across services.

## 5. Verification Plan
1.  **Build**: `mvn clean install` on common, user, and gateway services.
2.  **Start Infrastructure**: Run `config-service`, `discovery-service`.
3.  **Start Services**: Run `user-service`, `catalog-service`, `api-gateway`.
4.  **Test Scenarios (Postman)**:
    *   `POST /api/v1/auth/login` (via Gateway) -> Should return Token (200 OK).
    *   `GET /api/v1/users/profile` (No Token) -> Should return 401 Unauthorized.
    *   `GET /api/v1/users/profile` (With Token) -> Should return Profile (200 OK).
    *   `GET /api/v1/products` -> Should route to Catalog Service.

## 6. Development Checklist
- [ ] Refactor `JwtService` to `common-library`.
- [ ] Update `user-service` to use `common-library`.
- [ ] Configure `api-gateway` (`application.yml`).
- [ ] Implement `AuthenticationFilter` in Gateway.
- [ ] Configure Routes.
- [ ] Verify End-to-End.
