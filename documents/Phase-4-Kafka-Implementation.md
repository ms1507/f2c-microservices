# Phase 4 Implementation: Asynchronous Communication (Kafka)

This document outlines the step-by-step implementation for Phase 4, focusing on decoupling the **Order Service** and **Notification Service** using Apache Kafka.

## 🎯 Objective
Automatically trigger an asynchronous notification flow whenever a new order is successfully placed, without blocking the user response in the Order Service.

---

## 🛠️ Infrastructure Setup

### 1. Update Docker Compose
We need to add Kafka and Zookeeper to our local development environment.

**File**: [docker-compose.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/docker-compose.yml)
- Uncomment/Add `zookeeper` service.
- Uncomment/Add `kafka` service.
- Ensure they are on the `rural-network`.

### 2. Update Configuration (Config Server)
Add Kafka connection details to the centralized configuration.

**File**: [order-service.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/config-repo/order-service.yml) & `notification-service.yml`
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: notification-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.rural.marketplace.common.event"
```

---

## 🚀 Implementation Steps

### Step 1: Define Shared Events in `common-library`
Create a DTO that represents the "Order Placed" event.
- **Class**: `OrderPlacedEvent`
- **Fields**: `orderId`, `userId`, `totalAmount`, `customerEmail`, `customerName`.

### Step 2: Order Service (Producer)
1. **Dependency**: Add `spring-kafka` to `order-service/pom.xml`.
2. **Kafka Config**: Create a `KafkaTopicConfig` to auto-create the `order-placed-topic`.
3. **Producer Logic**: In `OrderService`, after saving the order to the database, use `KafkaTemplate` to publish the `OrderPlacedEvent`.

### Step 3: Notification Service (Consumer)
1. **Dependency**: Add `spring-kafka` to `notification-service/pom.xml`.
2. **Listener Logic**: Implement a listener method using `@KafkaListener`.
   - **Topic**: `order-placed-topic`
   - **Group ID**: `notification-group`
3. **Processing**: Log the event and simulate sending an email/SMS.

---

## ✅ Verification Checklist
- [ ] **Infrastructure**: Kafka and Zookeeper are running (`docker ps`).
- [ ] **Topic Creation**: Verify `order-placed-topic` exists.
- [ ] **Connectivity**: Order Service connects to Kafka on startup.
- [ ] **E2E Test**:
  1. Place an order via API Gateway.
  2. Order Service returns `201 Created`.
  3. Notification Service logs show: "Received OrderPlacedEvent for Order ID: X".

---
**Reference**: See the [Main Development Plan](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/documents/Application-Development-Plan.md) for context.
