# Project Structure: Rural Marketplace Application

The application will be structured as a **Maven Multi-Module Project**. This allows us to manage dependencies centrally (in the parent `pom.xml`) and build all services with a single command.

## 1. Directory Layout

```text
RuralMarketPlace-backend/
├── pom.xml                     # Parent POM (Dependency Managment)
├── docker-compose.yml          # Infrastructure setup (Postgres, RabbitMQ, Redis, etc.)
├── .gitignore                  # Git ignore rules
│
├── discovery-service/          # Eureka Server
│   ├── src/
│   └── pom.xml
│
├── config-service/             # Centralized Configuration Server
│   ├── src/
│   └── pom.xml
│
├── api-gateway/                # Spring Cloud Gateway
│   ├── src/
│   └── pom.xml
│
├── user-service/               # User Management & Auth
│   ├── src/
│   └── pom.xml
│
├── catalog-service/            # Product & Inventory
│   ├── src/
│   └── pom.xml
│
├── order-service/              # Order Processing
│   ├── src/
│   └── pom.xml
│
├── notification-service/       # Email/SMS Notifications
│   ├── src/
│   └── pom.xml
│
├── common-library/             # Shared DTOs, Utils, Exceptions
│   ├── src/
│   └── pom.xml
│
└── documents/                  # Documentation
    ├── BRD.md
    ├── architecture.md
    ├── project_structure.md
    └── ...
```

## 2. Module Descriptions

| Module | Type | Description |
| :--- | :--- | :--- |
| **`discovery-service`** | Infrastructure | Central registry where all services register themselves. |
| **`config-service`** | Infrastructure | Centralized configuration for all microservices. |
| **`api-gateway`** | Infrastructure | Single entry point for all client requests. Handles routing and security whitelisting. |
| **`user-service`** | Business | Manages user profiles roles (Farmer, Buyer) and Authentication. |
| **`catalog-service`** | Business | Manages products, categories, and inventory stock. |
| **`order-service`** | Business | Manages order lifecycle, cart, and triggers payment/notifications. |
| **`notification-service`** | Support | Consumes events (e.g., `OrderPlaced`) to send alerts. |
| **`common-library`** | Utility | Contains shared classes like `ApiResponse`, `GlobalExceptionHandler`, and DTOs to avoid code duplication. |

## 3. Recommended .gitignore

```gitignore
.idea/
target/
*.iml
.DS_Store
*.log
.mvn/
mvnw
mvnw.cmd
```

## 4. Architectural Decision: Monorepo (Recommended) vs. Polyrepo

The user asked: *"Are these services independent to each other? Or should we maintain separate application for each microservice?"*

**We represent the "Monorepo" (Single Repository) approach.**

### Why Monorepo is Better for this Project:
1.  **Logical Independence, Physical Unity:**
    *   **Independent:** Each service (`user-service`, `order-service`) has its own `pom.xml`, its own Database, and runs as a separate process on a separate port (`8081`, `8084`, etc.). They communicate *only* via APIs or Events.
    *   **Unified:** They sit in one folder so you can debug, refactor, and manage versions easily.
2.  **Shared Code Management:**
    *   With `common-library` in the same repo, you can update a shared DTO and immediately use it in `order-service` without needing to publish a library to a remote server (Nexus/Artifactory) first.
3.  **Simplified Development:**
    *   You only need **one IDE window** open.
    *   You can run `docker-compose up` to start everything at once.

### The Alternative (Polyrepo) - Not Recommended for Start:
*   Creating a separate Git repository for every service (e.g., 6 different repos).
*   **Pros:** strict isolation.
*   **Cons:** "Dependency Hell" (updating a shared lib requires updating 6 repos), difficult to navigate code, high operational overhead for a small team.
