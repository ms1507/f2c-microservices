# User Service Configuration

**File**: `src/main/resources/application.yml`

## Overview
The User Service manages user accounts, authentication, and profiles. It acts as a Eureka Client and fetches centralized configuration from the Config Server.

## Configuration Details

### 1. Server Configuration
```yaml
server:
  port: 8081
```
- **`server.port`**: Runs on port **8081**.

### 2. Centralized Configuration
```yaml
spring:
  config:
    import: "optional:configserver:http://localhost:8888"
  cloud:
    config:
      fail-fast: true
```
- **`import`**: Connects to the Config Server at `http://localhost:8888` to load external properties. `optional:` allows startup even if Config Server is down (though `fail-fast: true` overrides this behavior to ensure config is loaded).
- **`fail-fast: true`**: The application will fail to start if it cannot connect to the Config Server immediately. This helps catch configuration issues early.

### 3. Eureka Instance
```yaml
eureka:
  instance:
    prefer-ip-address: true
```
- **`prefer-ip-address`**: Registers the IP address instead of the hostname with Eureka. This is often more reliable in containerized environments (Docker).
