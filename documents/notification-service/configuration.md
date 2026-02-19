# Notification Service Configuration

**File**: `src/main/resources/application.yml`

## Overview
The Notification Service is responsible for sending alerts (emails, SMS, etc.) to users. It registers with the Discovery Server to be accessible by other services.

## Configuration Details

### 1. Server Configuration
```yaml
server:
  port: 8086
```
- **`server.port`**: Runs the service on port **8086** to avoid conflicts.

### 2. Application Name
```yaml
spring:
  application:
    name: notification-service
```
- **`spring.application.name`**: Unique identifier used by Eureka for service registration.

### 3. Service Discovery
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```
- **`defaultZone`**: Points to the Eureka Server URL (`http://localhost:8761/eureka/`) for registration.
