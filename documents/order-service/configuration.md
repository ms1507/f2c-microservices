# Order Service Configuration

**File**: `src/main/resources/application.yml`

## Overview
The Order Service handles order placement and management. It connects to the Config Server and Discovery Server.

## Configuration Details

### 1. Application Name
```yaml
spring:
  application:
    name: order-service
```
- **`spring.application.name`**: Identifies the service as `order-service`.

### 2. Centralized Configuration
```yaml
spring:
  config:
    import: "optional:configserver:http://localhost:8888"
  cloud:
    config:
      fail-fast: true
```
- **`import`**: Connects to Config Server at `http://localhost:8888`.
- **`fail-fast: true`**: Ensures strict dependency on the Config Server at startup.

### 3. Eureka Instance
```yaml
eureka:
  instance:
    prefer-ip-address: true
```
- **`prefer-ip-address`**: Registers IP address for service discovery.

> **Note**: `server.port` is not explicitly defined in `application.yml`. It will default to **8080** unless provided by the Config Server (e.g., in `order-service.yml` from `config-repo`). If running locally without Config Server, it might conflict with API Gateway which also uses 8080.
