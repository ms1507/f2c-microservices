# Common Library Overview

The `common-library` is a shared module in the Rural Marketplace microservices architecture designed to follow the **DRY (Don't Repeat Yourself)** principle. It provides a centralized location for shared code, utilities, and configurations used across multiple services.

## 🏠 What’s Inside?

The library handles the "shared plumbing" of the application:

1.  **Security Logic (`JwtService.java`):**
    *   Handles token generation, validation, and claim extraction.
    *   Shared by the **API Gateway** (for validation) and **User Service** (for generation).
2.  **Context Management (`UserContextHolder.java`):**
    *   A utility class to extract user context (userId, username) from HTTP request headers inject by the Gateway.
    *   Enables downstream services (Catalog, Order, etc.) to access logged-in user info without JWT parsing.
3.  **Shared Objects (DTOs):**
    *   Stores common Data Transfer Objects used across service boundaries to ensure consistency.

## 🚀 Why Use a Shared Library?

*   **Consistency:** Global changes (like security key updates or header additions) are made in one place and inherited by all services.
*   **Centralized Security:** Ensures all services use the same logic and versions for critical security operations.
*   **Reduced Complexity:** Microservices stay focused on business logic while common "plumbing" is handled externally.

## ⚙️ How to Use

### 1. Rebuild the Library
Whenever changes are made to `common-library`, it must be rebuilt and installed to the local Maven repository:
```bash
cd common-library
mvn clean install
```

### 2. Include Dependency
Add the library to the `pom.xml` of any microservice:
```xml
<dependency>
    <groupId>com.rural.marketplace</groupId>
    <artifactId>common-library</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 3. Usage in Code
Import classes as needed:
```java
import com.rural.marketplace.common.context.UserContextHolder;

// Usage in controller:
Long userId = UserContextHolder.getCurrentUserId(request);
```

## 4. Caching & Performance (Phase 8)

### 4.1 Redis Configuration
- **RedisConfig**: Centralized Redis configuration for all microservices.
- **Resilience**: Integrated `CacheErrorHandler` to handle Redis downtime gracefully (logs error and falls back to DB).

### 4.2 Standardized DTOs
- All DTOs intended for caching (e.g., `ProductDTO`, `CategoryDTO`) implement `Serializable` to support Redis data types.
- These DTOs are located in `com.rural.marketplace.common.dto`.
