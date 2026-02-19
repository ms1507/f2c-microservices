# Discovery Service Configuration

**File**: `src/main/resources/application.yml`

## Overview
The Discovery Service (Eureka Server) acts as the service registry where all other microservices register themselves. This allows services to find each other dynamically without hardcoding hostnames and ports.

## Configuration Details

### 1. Server Configuration
```yaml
server:
  port: 8761
```
- **`server.port`**: The standard port for Eureka Server is **8761**.

### 2. Application Name
```yaml
spring:
  application:
    name: discovery-service
```
- **`spring.application.name`**: Identifies the application in logs and the Spring context.

### 3. Eureka Client Configuration
```yaml
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```
- **`register-with-eureka: false`**: The server should not register with itself.
- **`fetch-registry: false`**: The server maintains the registry, so it doesn't need to fetch it.
