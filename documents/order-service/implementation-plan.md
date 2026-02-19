# Order Service & Feign Client Implementation Plan

## 1. Goal
Implement the **Order Service** to handle order placement and management. This includes setting up Synchronous Communication (**Feign Client**) to fetch product details from the **Catalog Service**.

**Reference:** Sections 2.1 and 3 of `Application-Development-Plan.md`.

## 2. Architecture
*   **Service Name:** `order-service`
*   **Port:** `8083`
*   **Database:** PostgreSQL (`orders_db` schema)
*   **Communication:** Spring Cloud OpenFeign (to call `catalog-service`)

## 3. Prerequisite Checks
*   [ ] `order-service` module exists in `pom.xml`.
*   [ ] PostgreSQL is running and `orders_db` (or schema) is accessible.
*   [ ] `common-library` is available for DTOs (if shared) or we create local DTOs.

## 4. Implementation Steps

### Step 1: Dependencies & Config
*   **POM**: Add `spring-cloud-starter-openfeign`, `spring-boot-starter-data-jpa`, `spring-boot-starter-web`, `common-library`.
*   **App Class**: Add `@EnableFeignClients` and `@ComponentScan`.
*   **Config**: `application.yml` -> Port 8083, Eureka, Datasource.

### Step 2: Feign Client (The "Bridge")
Create `com.rural.marketplace.order.client.ProductClient`:
```java
@FeignClient(name = "catalog-service")
public interface ProductClient {
    @GetMapping("/api/v1/products/{id}")
    ResponseEntity<ProductDTO> getProductById(@PathVariable("id") Long id);
}
```

### Step 3: Domain Model
*   **Order**: `id`, `userId`, `totalAmount`, `orderDate`, `status` (CREATED, COMPLETED), `shippingAddress`.
*   **OrderItem**: `id`, `productId`, `productName`, `quantity`, `price`.

### Step 4: Business Logic (`OrderService`)
`placeOrder(OrderRequest request)`
1.  **Validate User**: Extract User ID from Header (`X-Logged-In-User` passed by Gateway).
2.  **Validate Products**: Loop through items:
    *   Call `productClient.getProductById(itemId)`.
    *   Verify existence and price (Optional: Check stock).
3.  **Calculate Total**: Sum up prices.
4.  **Save**: Persist to DB.
5.  **Return**: Order Response.

### Step 5: Controller
`OrderController`
*   `POST /api/v1/orders`: Place Order.
*   `GET /api/v1/orders/{id}`: Get Order Details.

## 5. Verification Plan
1.  **Start Infrastructure**: Postgres, Eureka, Config Server.
2.  **Start Services**: Catalog (Port 8082), Order (Port 8083), Gateway (Port 8080).
3.  **Test Flow (Postman/APIDog)**:
    *   **Setup**: Create a Product in Catalog (ID: 1, Price: 100).
    *   **Action**: Login -> Get Token.
    *   **Action**: `POST /api/v1/orders` (via Gateway).
        *   Body: `{ "items": [ { "productId": 1, "quantity": 2 } ] }`
    *   **Expect**: Order Created with Total = 200.
