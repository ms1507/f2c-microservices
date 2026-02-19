# Standard Security Patterns for Spring Boot REST APIs

When building modern REST APIs (especially Microservices), **Stateless Security** is the industry standard.

## 1. Stateless Authentication (JWT Pattern)
**Best for:** Microservices, Mobile Apps, SPAs (React/Angular).
**Concept:** The server never "remembers" a logged-in user in RAM (Session). Instead, it issues a "Passport" (token).
**Flow:**
1.  **Login:** Client sends `username/password`.
2.  **Issue:** Server validates and signs a **JWT (JSON Web Token)** containing UserID and Roles.
3.  **Use:** Client sends JWT in the `Authorization: Bearer <token>` header for every request.
4.  **Verify:** Server checks the signature. if valid, request proceeds.
**Pros:** Highly scalable (no server memory used).

## 2. API Gateway Security (The "Bouncer" Pattern)
**Best for:** Systems with many microservices.
**Concept:** Authentication happens **once** at the entry point (Gateway).
**Flow:**
1.  **Gateway:** Intercepts request -> Validates Token.
2.  **Forward:** If valid, forwards request to `User Service` or `Order Service` with simple headers (e.g., `X-User-Id: 123`, `X-Role: ADMIN`).
3.  **Service:** Microservices trust the Gateway and don't re-validate the token logic (or valid it cheaply).
**Pros:** Centralized security logic; Microservices focus on business logic.

## 3. Role-Based Access Control (RBAC)
**Best for:** Controlling *who* can do *what*.
**Concept:** Assign broad roles (`ADMIN`, `USER`) or granular authorities (`PRODUCT_CREATE`, `ORDER_READ`).
**Implementation:**
-   **URL Level:** `.requestMatchers("/admin/**").hasRole("ADMIN")` (Broad, rigid).
-   **Method Level (Recommended):** `@PreAuthorize("hasRole('ADMIN')")` on specific Service/Controller methods.

## 4. OAuth2 / OpenID Connect (OIDC)
**Best for:** "Log in with Google/Facebook" or Enterprise (SSO).
**Concept:** Delegate login to a 3rd party (Identity Provider like Keycloak, Auth0, Google).
**Flow:** App redirects user to Google -> User logs in -> Google sends code -> App swaps code for Token.

## 5. Defense Patterns (Must Haves)
-   **CORS (Cross-Origin Resource Sharing):** Restricts which websites (domains) can call your API (e.g., allow `localhost:3000` but block `hacker.com`).
-   **Rate Limiting:** Prevent users from hitting APIs 1000 times/second (DoS protection).
-   **Input Validation:** Sanitize all inputs to prevent SQL Injection (handled by JPA mostly) and XSS.

---

## My Recommendation for Rural Marketplace
We should use **Pattern 1 (JWT)** + **Pattern 3 (Method Level RBAC)**.
-   **Simple:** We build it ourselves in `user-service`.
-   **Effective:** Fits our Monorepo/Microservices structure perfectly.
