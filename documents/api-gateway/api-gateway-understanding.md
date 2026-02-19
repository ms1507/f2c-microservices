# API Gateway Implementation Guide

This document explains the technical implementation of the API Gateway (`api-gateway`) in the Rural Marketplace application.

## 1. Role in Architecture
The API Gateway serves as the **Single Entry Point** for all client requests (Mobile App, Web Frontend). Instead of clients calling `user-service` or `catalog-service` directly, they call the Gateway, which routes the request to the appropriate microservice.

**Key Responsibilities:**
1.  **Routing**: Forwarding requests based on URL patterns.
2.  **Security**: Validating JWT tokens before they reach internal services.
3.  **Cross-Cutting Concerns**: Logging, Monitoring, and Response modification.

## 2. Key Components & Implementation

### 2.1 Dependencies (`pom.xml`)
We use **Spring Cloud Gateway** as the core framework and **Netflix Eureka** for service discovery.

```xml
<dependencies>
    <!-- Core Gateway Logic -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <!-- Service Discovery (to route by Service Name) -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <!-- Shared Security Logic -->
    <dependency>
        <groupId>com.rural.marketplace</groupId>
        <artifactId>common-library</artifactId>
    </dependency>
</dependencies>
```

### 2.2 Configuration (`application.yml`)
The routing logic is defined here. We use the **Load Balancer (`lb://`)** protocol so the Gateway can dynamically find service instances from Eureka.

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true # Enables automatic routing based on serviceID (e.g., /user-service/**)
      routes:
        # Route 1: Authentication Endpoints (Public)
        - id: user-service-auth
          uri: lb://user-service          # Target: Load Balanced User Service
          predicates:
            - Path=/api/v1/auth/**        # Match: /api/v1/auth/login, /register
          # No filters applied (Open Access)

        # Route 2: User Management (Secured)
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/v1/users/**       # Match: /api/v1/users/profile
          filters:
            - AuthenticationFilter        # Action: Apply our custom security filter

        # Route 3: Catalog Service (Public/Secured Mixed)
        - id: catalog-service
          uri: lb://catalog-service
          predicates:
            - Path=/api/v1/products/**, /api/v1/categories/**
```

### 2.3 Custom Security Filter (`AuthenticationFilter.java`)
This is the "Bouncer". It intercepts requests marked with the `AuthenticationFilter` in the routes above.

**Location:** `src/main/java/com/rural/marketplace/gateway/filter/AuthenticationFilter.java`

**Logic Flow:**
1.  **Intercept**: Catches the request before routing.
2.  **Check Header**: Looks for `Authorization: Bearer <token>`.
3.  **Validate**: Calls `jwtService.validateToken(token)` (from `common-library`).
    *   *Note: This is a stateless check (Signature + Expiration only).*
4.  **Extract Identity**: extracts the Username/Mobile from the token.
5.  **Mutate Request**: Adds a new header `X-Logged-In-User: <mobile>` to the request before sending it to the downstream service.
    *   *Benefit: Downstream services know EXACTLY who the user is without parsing the token again if they choose to trust this header.*

### 2.4 Shared JWT Logic (`common-library`)
To avoid code duplication, the JWT generation (User Service) and Validation (Gateway) logic lives in `common-library`.

**Location:** `common-library/src/main/java/com/rural/marketplace/common/security/JwtService.java`

## 3. Request Flow Example

**Scenario: User requests their Profile (`GET /api/v1/users/profile`)**

1.  **Client** sends Request: `GET http://localhost:8080/api/v1/users/profile` (Header: `Authorization: Bearer xyz...`)
2.  **Gateway** matches route `user-service`.
3.  **Gateway** sees `AuthenticationFilter` configured.
4.  **Filter** runs:
    *   Validates `xyz...` token.
    *   Extracts user `9876543210`.
    *   Adds header `X-Logged-In-User: 9876543210`.
5.  **Gateway** looks up `user-service` in Eureka (e.g., finds `localhost:8081`).
6.  **Gateway** forwards request: `GET http://localhost:8081/api/v1/users/profile`.
7.  **User Service** receives request, performs its own security checks (optional but recommended), and returns the profile.

## 4. How to Run & Verify
1.  Start **Config Server**.
2.  Start **Discovery Service** (Eureka).
3.  Start **User Service**.
4.  Start **API Gateway**.
5.  **Verification**:
    *   Try accessing `http://localhost:8080/api/v1/users/profile` EXCLUDING the token.
    *   **Result**: `401 Unauthorized` (Blocked by Gateway).
