# Catalog Service Configuration

**File**: `src/main/resources/application.yml`

## Overview
The Catalog Service manages products and categories. It registers with Eureka and pulls config from the Config Server.

## Configuration Details

### 1. Server Configuration
```yaml
server:
  port: 8082
```
- **`server.port`**: Runs on port **8082**.

### 2. Centralized Configuration
```yaml
spring:
  config:
    import: "optional:configserver:http://localhost:8888"
  cloud:
    config:
      fail-fast: true
```
- **`import`**: Fetches configuration from `http://localhost:8888`.
- **`fail-fast: true`**: Stops startup if Config Server is unreachable.

### 3. Eureka Instance
```yaml
eureka:
  instance:
    prefer-ip-address: true
```
- **`prefer-ip-address`**: Uses IP address for registration.
