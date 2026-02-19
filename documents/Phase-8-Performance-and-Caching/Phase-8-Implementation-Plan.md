# Phase 8: Performance & Caching - Implementation Plan

## Goal
Improve application performance and reduce database load by implementing a multi-level caching strategy using Redis and Spring Cache.

## User Review Required
> [!IMPORTANT]
> **Redis Infrastructure:** This phase requires a Redis instance. We will add it to the `docker-compose.yml` for local development.
> **Consistency:** Cache eviction will be handled for basic updates, but complex data dependencies might require further tuning.

## Proposed Changes

---

### 🏛️ Infrastructure Component
Update the local development environment to support Redis.

#### [MODIFY] [docker-compose.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/docker-compose.yml)
- Add `redis` service using `redis:alpine` image.
- Expose port `6379`.

---

### 📦 Common Library Component
Centralize caching configuration and dependencies.

#### [MODIFY] [common-library/pom.xml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/common-library/pom.xml)
- Add `spring-boot-starter-data-redis` dependency.
- Add `spring-boot-starter-cache` dependency.

#### [NEW] `RedisConfig.java`
- Create a configuration class in `com.rural.marketplace.common.config`.
- Configure `RedisCacheManager` with default TTL (e.g., 10 minutes).
- Implement custom `CacheErrorHandler` to prevent app failure if Redis is down.

---

### 🛒 Catalog Service Component
Implement caching for performance-critical product operations.

#### [MODIFY] `CatalogService.java`
- Annotate `getAllProducts()` with `@Cacheable(value = "products")`.
- Annotate `getProductById(id)` with `@Cacheable(value = "product", key = "#id")`.
- Annotate `saveProduct()` and `updateProduct()` with `@CacheEvict(value = {"products", "product"}, allEntries = true)`.

#### [MODIFY] [catalog-service/src/main/resources/application.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/catalog-service/src/main/resources/application.yml)
- Enable spring cache type as `redis`.
- Configure Redis host and port (fetching from Config Server).

---

### ⚙️ Config Repository
Externalize Redis settings.

#### [MODIFY] [config-repo/application.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/config-repo/application.yml)
- Add default Redis connection properties.

---

## Verification Plan

### Automated Tests
- No new automated tests planned yet, but existing integration tests will be run to ensure no regressions.

### Manual Verification
1.  **Redis Connectivity:**
    - Run `docker-compose up redis`.
    - Use `redis-cli ping` to verify it's alive.
2.  **Cache Hit/Miss Verification:**
    - Call `GET /api/v1/products` via Gateway. Note the time (e.g., 200ms).
    - Call it again. Verify time reduces significantly (e.g., 10ms).
    - Check Redis keys using `redis-cli keys *` to see `products::...` entries.
3.  **Cache Eviction Verification:**
    - Update a product via `PUT /api/v1/products/{id}`.
    - Fetch the product list again. Verify it reflects the change and the previous cache key was evicted.
4.  **Resilience Test:**
    - Stop the Redis container.
    - Verify the application still works (falling back to DB) without throwing 500 errors (if `CacheErrorHandler` is implemented).
