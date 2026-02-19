# Technical Queries & Architecture Decisions

This document captures the rationale behind recent architectural changes and technical decisions made during the implementation of the Rural Marketplace backend.

---

## 1. Why Selective Component Scanning in `ApiGatewayApplication.java`?

### The Problem
By default, we used a broad scan: `@ComponentScan(basePackages = "com.rural.marketplace")`. This caused the **API Gateway** (a Reactive/WebFlux app) to attempt to load the `GlobalExceptionHandler` from the `common-library`.

### The Conflict
The `GlobalExceptionHandler` is designed for **Spring MVC** and depends on `jakarta.servlet.http.HttpServletRequest`. Since the Gateway uses **Spring WebFlux (Netty)**, it doesn't have the Servlet runtime. This led to a "Spring MVC found on classpath" conflict, preventing the Gateway from starting.

### The Solution
We restricted the scan to only include necessary, runtime-compatible components:
```java
@ComponentScan(basePackages = {
        "com.rural.marketplace.gateway",
        "com.rural.marketplace.common.security"
})
```
This ensures the Gateway loads its own filters and the shared `JwtService` but ignores the MVC-specific error handlers.

---

## 2. Why WebFlux for the API Gateway?

The API Gateway is the entry point for all traffic. Using **Spring WebFlux (Reactive)** provides several benefits over traditional blocking models:

- **Non-blocking I/O**: Threads don't sit idle waiting for downstream responses. A small number of threads can handle thousands of concurrent connections.
- **Resource Efficiency**: Significantly lower memory and CPU footprint under high load.
- **Modern Standard**: **Spring Cloud Gateway** is built natively on WebFlux and is the successor to older, blocking technologies.

### The "Old" Alternative: Netflix Zuul 1
Before WebFlux, the industry standard was **Netflix Zuul 1**.
- **Zuul 1**: Used a "Thread-per-Request" (Servlet) model. If 1,000 requests were slow, 1,000 threads were blocked.
- **Spring Cloud Gateway**: Uses an "Event-Loop" model. It can handle those same 1,000 requests with just a handful of threads.

---

## 3. Where is WebFlux used in our Ecosystem?

Currently, the application uses a **Hybrid Model**:

| Service | Architecture | Engine | Role |
| :--- | :--- | :--- | :--- |
| **`api-gateway`** | **Spring WebFlux** | Netty | **Reactive Routing**: High-performance entry point. |
| **`user-service`** | Spring MVC | Tomcat | **Blocking Logic**: Traditional CRUD and Auth. |
| **`catalog-service`**| Spring MVC | Tomcat | **Blocking Logic**: Product/Category management. |
| **`order-service`** | Spring MVC | Tomcat | **Blocking Logic**: Transactional order processing. |
| **`notification...`**| Spring MVC | Tomcat | **Blocking Logic**: Event consumption and alerts. |

This hybrid approach allows us to have a highly scalable entry point (Gateway) while keeping the business logic simple and easy to debug in the core services (MVC).

---

## 4. Resilience Order: When is the Fallback called?

There is often confusion about whether the Circuit Breaker or Retry happens first. In our Spring Boot setup, we follow the **"Onion" Model**.

### The Execution Flow
1. **`@Retry` (Outer Layer)**: Starts the overall operation and manages retry attempts.
2. **`@CircuitBreaker` (Middle Layer)**: Checks if the service is already known to be down (OPEN). If not, it lets the call through and records the result.
3. **Service Method**: The actual logic (`placeOrder`) executes.

### What happens during failure?
- If the service fails, the **Circuit Breaker** records the failure and propagates it up.
- The **Retry** aspect catches that failure and initiates a second attempt.
- This loop continues until **all retries are exhausted**.

### When does the Fallback run?
The **`fallbackMethod`** is the absolute last resort. It is called by the `@CircuitBreaker` aspect only in two cases:
1. **Exhausted Attempts**: After the final retry attempt still fails.
2. **Circuit OPEN**: If the circuit is already in an `OPEN` state, it skips the service call entirely and jumps straight to the fallback.

**Finding Evidence**: In your logs, you will see `Retry attempt #1`, `Retry attempt #2` etc., *before* you see the log for the `catalogServiceFallback`.

---

## 5. What other Resilience Patterns can we use?

Beyond **Circuit Breaker** and **Retry**, there are several other patterns provided by **Resilience4j** that can further protect your microservices:

### 1. Bulkhead (Isolation)
- **Concept**: If one service is slow, it shouldn't consume all the threads of your application. Bulkhead limits the number of concurrent calls to a specific service.
- **Analogy**: Like a ship's compartments—if one compartment floods, the rest keep the ship afloat.
- **Use Case**: Preventing a slow `Catalog-service` from "starving" the `User-service` of resources.

### 2. Rate Limiter (Traffic Control)
- **Concept**: Limits the number of requests a service can handle in a specific time window.
- **Analogy**: A nightclub with a "one-in, one-out" policy once it reaches capacity.
- **Use Case**: Protecting your database or an expensive external third-party API from being overwhelmed by a sudden spike in traffic.

### 3. Time Limiter (Timeout Management)
- **Concept**: Defines how long you are willing to wait for a response before giving up and throwing an error.
- **Analogy**: Setting a timer for a phone call; if they don't answer in 30 seconds, you hang up.
- **Use Case**: Ensuring that a hung downstream service doesn't keep your users waiting indefinitely with a spinning wheel.

### 4. Cache (Fallback to Old Data)
- **Concept**: If a service is down, return the last known good value from a local or distributed cache (like Redis).
- **Use Case**: If `Catalog-service` is down, show the user the product prices as of 10 minutes ago rather than an error message.

### Summary Comparison

| Pattern | Focus | Primary Goal |
| :--- | :--- | :--- |
| **Circuit Breaker**| Failure Frequency | Prevent cascading failure by stopping calls. |
| **Retry** | Transient Errors | Fix temporary glitches (network blips). |
| **Bulkhead** | Resource Limits | Isolate failures to a specific thread pool. |
| **Rate Limiter** | Traffic Volume | Protect against overload and DDoS. |
| **Time Limiter** | Latency | Prevent infinite waiting. |
