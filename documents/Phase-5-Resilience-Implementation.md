# Phase 5 Implementation: Resilience & Fault Tolerance (Resilience4j)

This document outlines the implementation steps for Phase 5, focusing on making the **Order Service** resilient to failures in the **Catalog Service**.

## 🎯 Objective
Prevent cascading failures and handle downstream service downtime gracefully using Circuit Breakers, Retries, and Fallbacks.

---

## 🚀 Implementation Steps

### Step 1: Add Dependencies
Add Resilience4j and AOP support to the Order Service.

**File**: `order-service/pom.xml`
- `resilience4j-spring-boot3`
- `spring-boot-starter-aop`

### Step 2: Configuration in Config Server
Define the circuit breaker and retry policies.

**File**: [order-service.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/config-repo/order-service.yml)
```yaml
resilience4j:
  circuitbreaker:
    instances:
      catalogService:
        registerHealthMap: true
        slidingWindowSize: 10
        permittedNumberOfCallsInHalfOpenState: 3
        slidingWindowType: COUNT_BASED
        minimumNumberOfCalls: 5
        waitDurationInOpenState: 5s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
  retry:
    instances:
      catalogService:
        maxAttempts: 3
        waitDuration: 1s
```

### Step 3: Implement Circuit Breaker & Fallback
Wrap the Feign client calls in the `OrderService`.

**File**: `OrderService.java`
1. Annotate method with `@CircuitBreaker(name = "catalogService", fallbackMethod = "catalogServiceFallback")`.
2. Annotate method with `@Retry(name = "catalogService")`.
3. Implement the `catalogServiceFallback` method to handle cases where the product cannot be fetched.

---

## ✅ Verification Checklist
- [ ] **Build**: Order Service compiles with new dependencies.
- [ ] **Shutdown Test**: Stop `catalog-service`.
- [ ] **Fallback Test**: Attempt to place an order. Verify the system returns a graceful "Service Unavailable" or "Cached Data" instead of a 500 internal server error.
- [ ] **Circuit State**: Check Actuator health endpoint to see the circuit breaker transitioning from CLOSED ➔ OPEN.

---
**Reference**: See the [Main Development Plan](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/documents/Application-Development-Plan.md) for context.
