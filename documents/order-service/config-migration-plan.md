# Config Migration Plan: Order Service

## 1. Goal
Move `order-service` configuration from local `src/main/resources/application.yml` to the Centralized Config Server repository (`config-repo/order-service.yml`). This aligns with **Phase 1.1** of the architecture.

## 2. Changes

### 2.1 Create `config-repo/order-service.yml`
Move the following properties to the central repo:
*   Server Port (`8083`)
*   DataSource (PostgreSQL URL, Username, Password)
*   JPA/Hibernate properties (`ddl-auto: validate`, dialect)
*   Eureka Client settings (`prefer-ip-address`, `service-url`)

### 2.2 Update `order-service/src/main/resources/application.yml`
Strip it down to only bootstrap configuration:
```yaml
spring:
  application:
    name: order-service
  config:
    import: "optional:configserver:http://localhost:8888"
  cloud:
    config:
      fail-fast: true
```

## 3. Verification
1.  **Restart Config Service**: Ensure it picks up the new file (native mode is usually hot-reload, but restart is safer).
2.  **Restart Order Service**: Check logs.
    *   It should fetch config from `http://localhost:8888`.
    *   It should start on port `8083`.
    *   It should connect to DB successfully.
