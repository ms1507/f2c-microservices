# Running and Testing the Rural Marketplace Application

This guide outlines the step-by-step process to start the application components and test the API endpoints.

## 1. Prerequisites
- **Java 17+** installed.
- **Docker** installed and running (for the database).
- **Postman** or `curl` for API testing.

## 2. Infrastructure Setup
Before starting any Java application, ensure the database is running.

```bash
# Navigate to the project root
cd RuralMarketPlace-backend

# Start PostgreSQL & Redis
docker-compose up -d postgres redis
```

> **Verify**: Check that containers `rural_postgres` (5432) and `rural_redis` (6379) are running.

## 3. Service Startup Order
Start the microservices in the following strict order to avoid connection errors:

| Order | Service | Port | Description |
| :--- | :--- | :--- | :--- |
| **1** | **Config Service** | `8888` | Provides configuration to all other services. |
| **2** | **Discovery Service** | `8761` | Service Registry (Eureka). Wait for it to fully start. |
| **3** | **User Service** | `8081` | Manages users and auth. (Connects to DB). |
| **4** | **Catalog Service** | `8082` | Manages products. (Connects to DB). |
| **5** | **Order Service** | `8083` | Manages orders. (Connects to DB). |
| **6** | **Notification Service** | `8086` | Handles notifications. |
| **7** | **API Gateway** | `8080` | Entry point for all requests. |

### Startup Verification
- Open **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761)
- You should see `USER-SERVICE`, `CATALOG-SERVICE`, `ORDER-SERVICE`, `NOTIFICATION-SERVICE`, and `API-GATEWAY` registered.

## 4. API Testing Flow

All API requests should be sent through the **API Gateway** on port **8080**.

### A. Authentication (User Service)
**1. Register a new user**
*   **Endpoint:** `POST http://localhost:8080/api/v1/auth/register`
*   **Body:**
    ```json
    {
      "username": "testuser",
      "email": "test@example.com",
      "password": "password123",
      "role": "BUYER"
    }
    ```

**2. Login to get Token**
*   **Endpoint:** `POST http://localhost:8080/api/v1/auth/login`
*   **Body:**
    ```json
    {
      "email": "test@example.com",
      "password": "password123"
    }
    ```
*   **Response:** Copy the `accessToken` from the response. You will need this for secured endpoints.

**3. Validate Token**
*   **Endpoint:** `GET http://localhost:8080/api/v1/auth/validate?token=<your_token>`
*   **Response:** Returns `true` if valid, `false` otherwise.

### B. Product Catalog (Catalog Service)
**1. Create a Category (Admin/Seller usually, but open for now)**
*   **Endpoint:** `POST http://localhost:8080/api/v1/categories`
*   **Headers:** `Authorization: Bearer <your_token>` (if secured)
*   **Body:**
    ```json
    {
      "name": "Electronics",
      "description": "Gadgets and devices"
    }
    ```

**2. Create a Product**
*   **Endpoint:** `POST http://localhost:8080/api/v1/products`
*   **Body:**
    ```json
    {
      "name": "Smart Phone",
      "description": "Latest model",
      "price": 500.00,
      "categoryId": 1
    }
    ```

**3. Get All Products (Public)**
*   **Endpoint:** `GET http://localhost:8080/api/v1/products`

**4. Get Product By ID**
*   **Endpoint:** `GET http://localhost:8080/api/v1/products/{id}`
*   **Example:** `http://localhost:8080/api/v1/products/1`

### C. Orders (Order Service)
**1. Place an Order**
*   **Endpoint:** `POST http://localhost:8080/api/v1/orders`
*   **Headers:** `Authorization: Bearer <your_token>`
*   **Body:**
    ```json
    {
      "items": [
        {
          "productId": 1,
          "quantity": 2
        }
      ]
    }
    ```

**2. Get My Orders**
*   **Endpoint:** `GET http://localhost:8080/api/v1/orders`
*   **Headers:** `Authorization: Bearer <your_token>`

## 5. Troubleshooting
- **Connection Refused**: Ensure Config Service and Discovery Service started FIRST.
- **Database Error**: Check if Docker container is running (`docker ps`).
- **404 Not Found**: Check if the service is registered in Eureka. If not, restart the service.
- **401 Unauthorized**: Ensure you are passing the `Bearer` token in the header for secured endpoints.

## 6. Performance & Caching (Phase 8)
Caching is implemented in the **Catalog Service** using Redis.

### Verification
1.  **Initial Load**: Fetch products/categories. First hit is recorded in logs.
2.  **Cached Load**: Subsequent hits should be significantly faster (< 20ms).
3.  **Inspect Redis**:
    ```bash
    docker exec -it rural_redis redis-cli keys "*"
    ```
4.  **Resilience**: Stop the `rural_redis` container. The Catalog Service will log cache errors but continue to serve data from the database.
