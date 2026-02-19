# API Gateway Configuration

**File**: `src/main/resources/application.yml`

## Overview
The API Gateway is the entry point for all client requests. It routes traffic to the appropriate microservices (`user-service`, `catalog-service`, `order-service`) and handles cross-cutting concerns like authentication.

## Configuration Details

### 1. Server Configuration
```yaml
server:
  port: 8080
```
- **`server.port`**: Runs on the standard HTTP port **8080**.

### 2. Dynamic Routing (Discovery Locator)
```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
```
- **`locator.enabled: true`**: Automatically creates routes based on services registered in Eureka (e.g., `/user-service/**` forwards to the `user-service`).
- **`lower-case-service-id: true`**: converting service names to lowercase in URLs.

### 3. Custom Routes
Explicit routes allow cleaner URLs and custom filter application.

#### User Service Routes
- **Auth**: `/api/v1/auth/**` -> `user-service`. (Public, no filters).
- **Users**: `/api/v1/users/**` -> `user-service`. (Secured with `AuthenticationFilter`).

#### Catalog Service Routes
- **Products/Categories**: `/api/v1/products/**`, `/api/v1/categories/**` -> `catalog-service`.
- **Security**: Currently configured with `AuthenticationFilter` for testing.

#### Order Service Routes
- **Orders**: `/api/v1/orders/**` -> `order-service`. (Secured with `AuthenticationFilter`).

### 4. Eureka Client
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```
- Registers the Gateway so it can discover other services.
