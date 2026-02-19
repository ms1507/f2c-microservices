# API Gateway & Security Refactoring Walkthrough

## 1. Overview
This task involved implementing the **API Gateway** (Section 1.3) and refactoring the security logic to be reusable across microservices.

## 2. Changes Implemented

### 2.1 Common Library Refactoring
*   **Moved `JwtService`**: Extracted the JWT generation and validation logic from `user-service` to `common-library` (`com.rural.marketplace.common.security.JwtService`).
*   **Updated Dependencies**: Added `jjwt` (Java JWT) dependencies to `common-library` so it can handle token operations.

### 2.2 User Service Updates
*   **Removed Local JwtService**: Deleted the duplicate `JwtService` class.
*   **Updated Imports**: Refactored `JwtAuthenticationFilter` and `AuthController` to use the shared `JwtService` from `common-library`.

### 2.3 API Gateway Implementation
*   **App Configuration**: Defined routes in `application.yml` for `user-service` and `catalog-service`.
*   **Security Filter**: Implemented `AuthenticationFilter` to intercept requests, validate JWT tokens using the shared library, and pass user context (headers) downstream.
*   **Dependencies**: Added `common-library` and `lombok` to `api-gateway`.

## 3. Verification
Run the following command to verify the build:
```powershell
mvn clean install -DskipTests
```
**Status**: ✅ APIs and Services compiled successfully.

## 4. Next Steps
*   Start the `discovery-service`, `api-gateway`, and `user-service`.
*   Test End-to-End flows via Postman (Login -> Get Token -> Call Secured API via Gateway).
