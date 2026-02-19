# Advanced API Gateway Features & Implementation

This document outlines additional functionalities you can add to your Spring Cloud Gateway to make it production-ready.

## 1. Rate Limiting (Redis)
**Goal:** Prevent abuse by limiting how many requests a user can make in a given time (e.g., 10 req/minute).

### Implementation
1.  **Dependencies:** Add `spring-boot-starter-data-redis-reactive` to `pom.xml`.
2.  **Configuration:**
    Define a `KeyResolver` bean to identify users (e.g., by IP or User ID header).
    ```java
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }
    ```
3.  **Application.yml:**
    ```yaml
    routes:
      - id: sensitive-route
        uri: lb://user-service
        filters:
          - name: RequestRateLimiter
            args:
              redis-rate-limiter.replenishRate: 10
              redis-rate-limiter.burstCapacity: 20
              key-resolver: "#{@userKeyResolver}"
    ```

## 2. Circuit Breaker (Resilience4j)
**Goal:** Prevent cascading failures. If `catalog-service` is down, fail fast or return a default response instead of hanging.

### Implementation
1.  **Dependencies:** Add `spring-cloud-starter-circuitbreaker-reactor-resilience4j`.
2.  **Application.yml:**
    ```yaml
    filters:
      - name: CircuitBreaker
        args:
          name: myCircuitBreaker
          fallbackUri: forward:/fallback
    ```
3.  **Fallback Controller:** Create a controller in valid Gateway code to handle `/fallback` and return a friendly message.

## 3. CORS Configuration (Cross-Origin Resource Sharing)
**Goal:** Allow your React/Angular frontend (running on a different domain/port) to call your API.

### Implementation
**Application.yml:**
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "http://localhost:3000"
            allowedMethods: "*"
            allowedHeaders: "*"
```

## 4. Request Tracing (Correlation ID)
**Goal:** Track a single request as it hops from Gateway -> User -> Database.

### Implementation
1.  **Global Filter:** Create a `GlobalFilter` that runs for *every* request.
2.  **Logic:**
    *   Check for header `X-Correlation-Id`.
    *   If missing, generate a `UUID`.
    *   Add it to the request headers.
3.  **Logging:** Include this ID in every log statement (MDC Logging).

## 5. Response Caching
**Goal:** Cache common responses (like "Get All Categories") at the Gateway level to reduce load on backend.

### Implementation
*   Requires custom implementation or usage of specific caching filters combined with Redis.
*   Generally easier to implement at the *Service* level (Phase 7 of your plan), but possible at Gateway for high-traffic public read APIs.

## 6. Request Aggregation (Advanced)
**Goal:** Client calls `/api/dashboard`, Gateway calls `User` + `Catalog` + `Order` services and combines the JSON.
*   **Note:** Usually complex in Gateway. Better suited for a "BFF" (Backend for Frontend) pattern or GraphQL.

---

### Recommendation for Next Steps
1.  **Rate Limiting** is crucial if you plan to expose this to public internet.
2.  **CORS** is mandatory once you start building the Frontend.
3.  **Circuit Breakers** are vital for High Availability (Phase 5).
