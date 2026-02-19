# Rural Marketplace - Comprehensive Application Development Plan

This document provides a technical roadmap, implementation details, and phase-by-phase checklists for the build-out of the Rural Marketplace microservices ecosystem.

---

## 🏛️ Phase 1: Infrastructure Layer
**Goal:** Establish centralized configuration, service discovery, and a secure entry point.

### 🛠️ Tech Stack & Dependencies
- **Spring Cloud Config Server**: `spring-cloud-config-server`
- **Netflix Eureka Server**: `spring-cloud-starter-netflix-eureka-server`
- **Spring Cloud Gateway**: `spring-cloud-starter-gateway`
- **Security**: `spring-boot-starter-security`, `jjwt-api`

### ✅ Task Checklist
- [x] **Setup Config Server**: Implement `config-service` with `@EnableConfigServer`.
- [x] **Externalize Properties**: Create `config-repo` with service-specific YAMLs.
- [x] **Setup Discovery**: Implement `discovery-service` with `@EnableEurekaServer`.
- [x] **API Gateway Routing**: Define routes for User, Catalog, and Order services.
- [x] **Centralized Auth**: Implement `AuthenticationFilter` in Gateway for JWT validation.
- [x] **Identity Propagation**: Pass `X-Logged-In-User` and `X-User-Id` headers downstream.

### 🧪 Manual Verification & Testing
- **Manual Verification**:
  - Access Eureka Dashboard at `http://localhost:8761` to verify all services are registered.
  - Test Config Server by accessing `http://localhost:8888/order-service/default`.
- **Testing Scenario**:
  - **Negative Test**: Stop the Config Service and attempt to start a microservice. Verify that the microservice fails to boot due to missing configurations.

---

## 📦 Phase 2: Core Domain Services
**Goal:** Implement the primary business entities and their management logic.

### 🛠️ Tech Stack & Dependencies
- **Persistence**: `spring-boot-starter-data-jpa`, `postgresql`
- **Validation**: `spring-boot-starter-validation`
- **Migration**: `flyway-core`
- **Utilities**: `lombok`, `common-library` (Custom)

### ✅ Task Checklist
- [x] **User Service**: Implement registration, authentication (JWT), and profile endpoints.
- [x] **Catalog Service**: Implement Category and Product CRUD operations.
- [x] **Media Handling**: Implement image upload/storage logic in Catalog Service.
- [x] **Database Versioning**: Create Flyway migrations for `users`, `categories`, and `products`.
- [x] **Common Library**: Extract shared DTOs and Utility classes to `common-library`.

### 🧪 Manual Verification & Testing
- **Manual Verification**: 
  - Register a user via Postman: `POST /api/v1/users/register`.
  - Check the `rural_marketplace_db` using a DB client to verify user existence.
- **Testing Scenario**:
  - **Happy Path**: Register user -> Login -> Use the returned JWT to access a protected catalog endpoint.
  - **Validation Test**: Attempt to create a product with a negative price. Verify `400 Bad Request`.

---

## 🛒 Phase 3: Order Management & Feign
**Goal:** Implement transactional order processing with synchronous inter-service calls.

### 🛠️ Tech Stack & Dependencies
- **Feign Client**: `spring-cloud-starter-openfeign`
- **Transactional**: `@Transactional` support for multi-item orders.

### ✅ Task Checklist
- [x] **Scaffold Order Service**: Create `order-service` module.
- [x] **Feign Integration**: Create `ProductClient` to fetch product data from Catalog Service.
- [x] **Order Persistence**: Implement logic to save `Order` and `OrderItem` entities.
- [x] **Schema Separation**: Use dedicated `orders` schema in PostgreSQL via Flyway.
- [x] **Build Verification**: Ensure all modules compile and register with Eureka.

### 🧪 Manual Verification & Testing
- **Manual Verification**:
  - Place an order using `POST /api/v1/orders`.
  - Verify that the total amount in the database matches the product price fetched from the Catalog Service.
- **Testing Scenario**:
  - **Integration Test**: Update a product price in the Catalog Service. Place a new order. Verify the order uses the *new* price via Feign communication.

---

## 🔀 Phase 4: Asynchronous Communication
**Goal:** Decouple services using event streaming for non-blocking processes like notifications.

### 🛠️ Tech Stack & Dependencies
- **Message Broker**: Apache Kafka (7.4.0)
- **Spring Kafka**: `spring-kafka`
- **Docker**: `confluentinc/cp-kafka`, `confluentinc/cp-zookeeper`

### 📝 Implementation Details
- **Status**: COMPLETED
- **Shared Event**: Created [OrderPlacedEvent](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/common-library/src/main/java/com/rural/marketplace/common/event/OrderPlacedEvent.java) in `common-library`.
- **Infrastructure**: Configured Kafka & Zookeeper in [docker-compose.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/docker-compose.yml).
- **Producer (Order Service)**: [OrderService](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/order-service/src/main/java/com/rural/marketplace/order/service/OrderService.java) uses `KafkaTemplate` to publish events.
- **Consumer (Notification Service)**: [NotificationListener](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/notification-service/src/main/java/com/rural/marketplace/notification/kafka/NotificationListener.java) handles events with `@KafkaListener`.
- **Custom Config**: Implemented [KafkaConfig](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/notification-service/src/main/java/com/rural/marketplace/notification/config/KafkaConfig.java) with `JsonMessageConverter` for automated JSON handling.

### ✅ Task Checklist
- [x] **Docker Infrastructure**: Enable Kafka & Zookeeper in `docker-compose.yml`.
- [x] **Event Producer**: Implement `OrderPlacedEvent` publisher in `order-service`.
- [x] **Event Consumer**: Implement `NotificationListener` in `notification-service`.
- [x] **Notification Logic**: Implement email/SMS simulation based on received events.
- [x] **Retry Mechanism**: Configure Kafka retries and JsonMessageConverter for robustness.

### 🧪 Manual Verification & Testing
- **Manual Verification**:
  - Check Kafka container logs: `docker logs rural_kafka`.
  - Monitor `notification-service` console for the receipt log: `RECEIVED ORDER PLACED EVENT VIA KAFKA`.
- **Testing Scenario**:
  - **Flow Test**: Place an order -> Immediately receive `201 Created` -> Confirm that the "Simulating Email" log appears in the Notification Service within seconds.

---

## 🛡️ Phase 5: Resilience & Fault Tolerance
**Goal:** Protect the system from cascading failures and handle downstream service downtime gracefully.

### 🛠️ Tech Stack & Dependencies
- **Library**: `resilience4j-spring-boot3`
- **AOP Support**: `spring-boot-starter-aop`
- **Monitoring**: `io.github.resilience4j:resilience4j-micrometer` (optional for metrics)

### 📝 Implementation Details
- **Status**: COMPLETED
- **Circuit Breaker**: Implement state-based protection for Feign clients. If the failure rate exceeds the threshold (e.g., 50%), the circuit opens to prevent further calls to the failing service.
- **Retry Mechanism**: Automatically retry transient failures (e.g., network blips) before triggering the circuit breaker.
- **Fallback Logic**: Define alternative behaviors (e.g., returning default values or cached data) to maintain service availability.

### ✅ Task Checklist
- [x] **Add Dependencies**: Update [order-service/pom.xml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/order-service/pom.xml) with Resilience4j and AOP.
- [x] **Defined Policies**: Configure instances for `catalogService` in [order-service.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/config-repo/order-service.yml).
- [x] **Circuit Breaker Integration**: Annotate `OrderService.placeOrder` with `@CircuitBreaker`.
- [x] **Retry Integration**: Annotate Feign calls with `@Retry`.
- [x] **Fallback Implementation**: Create `catalogServiceFallback` method to handle service unavailability.
- [x] **Health Monitoring**: Enable Actuator endpoints to monitor circuit states (CLOSED, OPEN, HALF_OPEN).

### 🧪 Manual Verification & Testing
- **Manual Verification**:
  - Access `http://localhost:8083/actuator/health` to verify the `circuitBreaker` health status is visible.
- **Testing Scenario**:
  - **Resilience Test**: Manually stop the `catalog-service`. Attempt to place an order. Verify that the response is the custom fallback message: *"Catalog Service is currently unavailable."*
  - **Recovery Test**: Restart the `catalog-service`. Verify that orders can be placed again after the circuit transitions back to CLOSED.

---

## �️ Phase 6: Generic Exception Handling & Advanced Resilience
**Goal:** Standardize error responses across all microservices and provide intelligent fallbacks with recommendations.

### 🛠️ Tech Stack & Dependencies
- **Spring Boot Starter Validation**: `spring-boot-starter-validation`
- **Controller Advice**: `@RestControllerAdvice`
- **Resilience4j**: Custom `Fallback` with business logic.

### ✅ Task Checklist
- [x] **Common Error DTO**: Implement `ErrorResponse` in `common-library` for structured JSON errors.
- [x] **Global Exception Handler**: Implement `@RestControllerAdvice` in `common-library` to catch all exceptions globally.
- [x] **Meaningful Fallbacks**: Update `OrderService` fallback to return recommended products when `catalog-service` is down.
- [x] **API Consistency**: Ensure every service returns a consistent error format for 4xx and 5xx errors.

### 🧪 Manual Verification & Testing
- **Manual Verification**:
  - Stop any service and call its API. Verify the error response follows the `ErrorResponse` structure.
- **Testing Scenario**:
  - **Fallback Recommendation Test**: Stop `catalog-service`. Place an order. Verify the response contains a "Service Unavailable" message along with list of "Recommended Products".

---

### 📊 Phase 7: Observability & Monitoring (LGTM Stack)
**Goal:** Gain deep insights into request traces, performance metrics, and system health using the industry-standard LGTM stack with OpenTelemetry.

### 🛠️ Tech Stack & Dependencies
- **Collector**: OpenTelemetry Collector (Contrib)
- **Tracing**: `micrometer-tracing-bridge-otel`, `opentelemetry-exporter-otlp` (Tempo backend)
- **Metrics**: `micrometer-registry-prometheus` (Prometheus backend)
- **Logs**: `loki-logback-appender` (Loki backend)
- **Visualization**: Grafana (Unified Dashboard)

### 📝 Implementation Details
- **Status**: DOCUMENTED (Currently Disabled in Code)
- **Architecture**: **Approach 2 (OpenTelemetry Collector)**. Microservices send all telemetry (Traces, Metrics, Logs) via OTLP to a central collector, which then routes them to specialized backends.
- **Log Correlation**: Unified logging pattern ensures every log line contains a Trace ID and Span ID.
- **Infrastructure**: Configured Loki (Logs), Tempo (Traces), Prometheus (Metrics), and Grafana in [docker-compose.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/docker-compose.yml).

### ⚙️ Core Configurations

#### 1. OpenTelemetry Collector ([otel-collector-config.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/infrastructure/monitoring/otel-collector-config.yml))
The collector receives OTLP data and exports it to Loki, Tempo, and Prometheus.
```yaml
receivers:
  otlp:
    protocols:
      grpc:
      http:
exporters:
  prometheus:
    endpoint: "0.0.0.0:8889"
  otlphttp/tempo:
    endpoint: "http://tempo:4318"
  loki:
    endpoint: "http://loki:3100/loki/api/v1/push"
```

#### 2. Centralized Log Pattern ([application.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/config-repo/application.yml))
Standardized across all services for seamless correlation in Grafana.
```yaml
logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

#### 3. Logback Integration ([logback-spring.xml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/common-library/src/main/resources/logback-spring.xml))
Pushes logs directly to Loki with contextual labels.
```xml
<appender name="LOKI" class="com.github.loki4j.logback.Loki4jAppender">
    <http><url>http://loki:3100/loki/api/v1/push</url></http>
    <format>
        <label><pattern>app=${appName},level=%level</pattern></label>
        <message><pattern>[%X{traceId:-},%X{spanId:-}] %level %logger - %msg</pattern></message>
    </format>
</appender>
```

### ✅ Task Checklist
- [x] **Infrastructure Setup**: Deploy Loki, Tempo, Prometheus, Grafana, and OTel Collector via Docker.
- [x] **Application Dependencies**: Add Micrometer OTel bridge and OTLP exporters to `common-library`.
- [x] **Centralized Config**: Enable Actuator and OTLP tracing in `config-repo`.
- [x] **Log Correlation**: Standardize log patterns to include `traceId` and `spanId`.
- [x] **Dashboarding**: Pre-configure Grafana with data sources for Loki, Tempo, and Prometheus.

### 🧪 Manual Verification & Testing
- **Manual Verification**:
  - Access Prometheus: `http://localhost:9090`
  - Access Grafana: `http://localhost:3000` (Search in "Explore" for Traces/Logs)
- **Testing Scenario**:
  - **Correlation Test**: Perform a cross-service call (Order -> Catalog). Find the log for "Order Placed" in Loki. Click the attached `traceId` to instantly see the full request span in Tempo.

---

## ⚡ Phase 8: Performance & Caching
**Goal:** Improve response times and reduce database load for frequent read operations.

### � Implementation Details
- **Status**: COMPLETED
- **Infrastructure**: Redis (alpine) deployed via Docker Compose.
- **Library**: Spring Boot Starter Data Redis & Spring Cache.
- **Pattern**: Cache-Aside pattern for Product read operations.
- **Resilience**: Custom `CacheErrorHandler` to ensure service availability if Redis fails.

### ✅ Task Checklist
- [x] **Redis Setup**: Deploy Redis container in `docker-compose.yml`.
- [x] **Library Integration**: Add dependencies to `common-library` and services.
- [x] **Centralized Config**: Define Redis connection properties in Config Repo.
- [x] **Application Caching**: Implement `@Cacheable` for product lookups in Catalog Service.
- [x] **Cache Eviction**: Implement `@CacheEvict` for product updates/deletions.
- [x] **Serialization**: Configure JSON serializers for cached entities.

### 🧪 Manual Verification & Testing
- **Manual Verification**: Use `redis-cli monitor` to observe cache hits/misses.
- **Testing Scenario**:
  - **Latency Test**: Fetch a product list multiple times. Verify that subsequent requests take < 20ms.
  - **Fall-through Test**: Stop Redis container. Verify that the application continues to serve data from the database without crashing.

---

## 🚢 Phase 9: DevOps & CI/CD
**Goal:** Standardize environment setup and automate the delivery pipeline.

### 🛠️ Tech Stack & Dependencies
- **Containerization**: Docker, Docker Compose
- **Platform**: GitHub Actions

### ✅ Task Checklist
- [ ] **Multi-stage Dockerfiles**: Write optimized Dockerfiles for all Java services.
- [ ] **Orchestration**: Finalize `docker-compose.yml` for local development.
- [ ] **CI Pipeline**: Create GitHub Actions to run tests and build images on push.
- [ ] **Deployment**: Setup automated deployment to cloud/staging (e.g., K8s or EC2).

### 🧪 Manual Verification & Testing
- **Manual Verification**: Run `docker-compose up` and verify all 8+ containers start healthy.
- **Testing Scenario**:
  - **CI/CD Test**: Push a minor change to GitHub. Verify the Action triggers, runs tests, and builds the container image.

---

## 🏁 Summary Checklist
- [x] Phase 1: Infrastructure (Gateway/Config/Discovery)
- [x] Phase 2: Core Domain Services (User/Catalog)
- [x] Phase 3: Synchronous Communication (Order)
- [x] Phase 4: Event-Driven Communication (Kafka)
- [x] Phase 5: Resilience (Resilience4j)
- [x] Phase 6: Generic Exception Handling & Advanced Resilience
- [x] Phase 7: Observability (Documented / Disabled)
- [x] Phase 8: Performance (Redis Cache)
- [ ] Phase 9: DevOps (CI/CD/Docker)
