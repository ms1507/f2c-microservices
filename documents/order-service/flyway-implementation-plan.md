# Standardize Order Service Database Migration

## 1. Goal
Align `order-service` with `user-service` and `catalog-service` by introducing **Flyway** for database migrations. This ensures schema changes are version controlled and explicit scripts are available, as requested by the user.

## 2. Changes
*   **Dependency:** Add `flyway-core` to `order-service/pom.xml`.
*   **Config:** Disable `hibernate.ddl-auto` in `application.yml` (change to `validate`).
*   **Migration Script:** Create `V1__init_orders_schema.sql` with manual DDL for `orders` and `order_items`.

## 3. SQL Script Details (`V1__init_orders_schema.sql`)
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    shipping_address VARCHAR(255),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    quantity INTEGER NOT NULL,
    price NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

## 4. Verification
1.  **Verify Files:** User can see `src/main/resources/db/migration/V1__init_orders_schema.sql`.
2.  **Runtime:** Restart `order-service`. Flyway logs will show `Successfully validated 1 migration`.
3.  **Database:** Check Postgres `orders_db` to ensure tables exist.
