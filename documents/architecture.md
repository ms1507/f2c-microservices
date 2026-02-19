# System Architecture & Design
## Rural Marketplace Application (Gramin Bazar)

### 1. High-Level Architecture Diagram
The system follows a Microservices Architecture pattern to ensure scalability, independent deployment, and fault tolerance.

```mermaid
graph TD
    User[Web/Mobile Client] -->|HTTPS| API_GW[API Gateway (Spring Cloud Gateway)]
    
    subgraph Infrastructure
        Disc[Service Discovery (Eureka)]
        Config[Config Server]
        MsgBroker[Kafka/RabbitMQ]
    end
    
    API_GW --> Auth_Svc[Auth Service]
    API_GW --> User_Svc[User Profile Service]
    API_GW --> Catalog_Svc[Catalog Service]
    API_GW --> Order_Svc[Order Service]
    API_GW --> Search_Svc[Search Service]

    %% Database Connections
    Auth_Svc --> Auth_DB[(PostgreSQL: Auth)]
    User_Svc --> User_DB[(PostgreSQL: Users)]
    Catalog_Svc --> Catalog_DB[(PostgreSQL: Catalog)]
    Catalog_Svc --> Redis[(Redis Cache)]
    Order_Svc --> Order_DB[(PostgreSQL: Orders)]

    %% Event Driven Communication
    Order_Svc -.->|OrderPlaced| MsgBroker
    MsgBroker -.->|Consume| Notif_Svc[Notification Service]
    Catalog_Svc -.->|ProductUpdated| MsgBroker
    MsgBroker -.->|Consume| Search_Svc
```

### 2. Core Microservices Description

| Service Name | Port | Description | Tech Stack |
| :--- | :--- | :--- | :--- |
| **Edge Server** | 8080 | API Gateway, Routing, Rate Limiting, CORS | Spring Cloud Gateway |
| **Auth Service** | 8081 | Authentication, Token Management (JWT) | Spring Security, OAuth2 |
| **User Service** | 8082 | Profile Management (Farmers, Buyers, Sellers) | Spring Boot, PostgreSQL |
| **Catalog Service** | 8083 | Product Management (CRUD), Inventory | Spring Boot, PostgreSQL, Redis |
| **Order Service** | 8084 | Order Processing, Cart Management | Spring Boot, PostgreSQL |
| **Search Service** | 8085 | Advanced Search (Fuzzy matching) | Spring Boot, Elasticsearch |
| **Notification Service** | 8086 | SMS/Email Notifications | Spring Boot, Kafka/RabbitMQ |
| **Discovery Service** | 8761 | Service Registry | Netflix Eureka |

### 3. Data Model Design (Schema)

#### 3.1 User Service Schema (`users_db`)
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    mobile_number VARCHAR(15) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL, -- FARMER, BUYER, SHOP_OWNER
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 3.2 Catalog Service Schema (`catalog_db`)
```sql
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL, -- Ref to User
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    category_id INT REFERENCES categories(id),
    image_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 3.3 Order Service Schema (`orders_db`)
```sql
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    buyer_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, CONFIRMED, DELIVERED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_at_purchase DECIMAL(10, 2) NOT NULL
);
```

### 4. Security Implementation
-   **Authentication:** JWT (JSON Web Tokens).
    -   Gateway validates the `Authorization: Bearer <token>` header.
    -   Requests without valid tokens are rejected at the Gateway (401 Unauthorized).
-   **Communication:**
    -   External: HTTPS (SSL/TLS).
    -   Internal: mTLS (optional for high security) or private network.

### 5. Resilience & Performance Strategy
-   **Circuit Breakers (Resilience4j):** Prevent cascading failures. If Catalog Service is down, Gateway returns a cached fallback response.
-   **Caching (Redis):** Frequently accessed product data (e.g., product lists, category trees) is cached using the **Cache-Aside** pattern.
-   **Standardized DTOs:** All cached objects implement `Serializable` and are centralized in the `common-library` to ensure consistency across the ecosystem.
-   **Async Processing:** Order confirmation emails/SMS are sent asynchronously via Kafka/RabbitMQ to ensure the user doesn't wait.
