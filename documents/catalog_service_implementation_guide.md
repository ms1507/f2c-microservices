# Catalog Service Implementation Guide

This document outlines the step-by-step implementation process for the **Catalog Service** in the Rural Marketplace application.

## 1. Setup & Configuration

### 1.1 Project Structure
The service is located in `catalog-service` directory.
Package structure: `com.rural.marketplace.catalog`

### 1.2 Dependencies (`pom.xml`)
Ensure the following dependencies are present:
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `flyway-core`
- `postgresql`
- `lombok`
- `spring-cloud-starter-netflix-eureka-client`
- `common-library`

### 1.3 Application Configuration (`application.yml`)
Configure:
- Server port: `8082` (or similar)
- Datasource (PostgreSQL) connection details
- JPA settings (DDL auto: validate/none recommended with Flyway, but 'update' for dev is ok initially if Flyway isn't strict)
- File upload limits (Multipart)

### 1.4 Database Migrations (Flyway)
Create `src/main/resources/db/migration` directory.
Create `V1__init_catalog_schema.sql` to define `categories` and `products` tables.

## 2. Category Management

### 2.1 Entity
Create `Category` entity with:
- `id` (Long, PK)
- `name` (String, unique)
- `description` (String)
- `parent_id` (Long, nullable, for sub-categories)
- Audit fields (created_at, updated_at)

### 2.2 Repository
Create `CategoryRepository` extending `JpaRepository`.

### 2.3 Service
Create `CategoryService` with methods:
- `createCategory(CategoryDTO)`
- `updateCategory(Long id, CategoryDTO)`
- `deleteCategory(Long id)`
- `getCategory(Long id)`
- `getAllCategories()`

### 2.4 Controller
Create `CategoryController` with endpoints:
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`
- `GET /api/categories/{id}`
- `GET /api/categories`

## 3. Product Management

### 3.1 Entity
Create `Product` entity with:
- `id` (Long, PK)
- `name` (String)
- `description` (String)
- `price` (BigDecimal)
- `stock_quantity` (Integer)
- `category_id` (Long, FK)
- `farmer_id` (Long)
- `image_url` (String)
- `location` (String)
- Audit fields

### 3.2 Repository
Create `ProductRepository` extending `JpaRepository`. Add `JpaSpecificationExecutor` for filtering.

### 3.3 Service
Create `ProductService` with methods:
- `createProduct(ProductDTO)`
- `updateProduct`
- `deleteProduct`
- `getProduct`
- `searchProducts(SearchCriteria)`

### 3.4 Controller
Create `ProductController` with endpoints:
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`
- `GET /api/products/{id}`
- `GET /api/products` (with search params)

## 4. Image Handling

### 4.1 Storage Service
Implement `FileStorageService` interface.
- Method `String storeFile(MultipartFile file)`
- Initially implement `LocalFileStorageService` to save to a local directory.

### 4.2 Integration
Update `ProductController` to handle `multipart/form-data` for creating/updating products with images.

## 5. Search & Filtering

### 5.1 Specifications
Create `ProductSpecification` to handle dynamic filtering:
- Category
- Price range
- Location
- Name/Description search

### 5.2 Pagination
Ensure `getAllProducts` supports `Pageable`.

## 7. Performance & Caching (Phase 8)

### 7.1 Redis Integration
- Enable Spring Caching with Redis support.
- Implement `RedisConfig` in `common-library` for shared configuration.

### 7.2 Caching Strategy
- **@Cacheable**: Used for `getProduct(id)` and `getAllCategories()`.
- **@CacheEvict**: Used for Create/Update/Delete operations to ensure cache consistency.

### 7.3 Resilience
- Implement a `CacheErrorHandler` to prevent service failure if Redis is unavailable.

## 8. Verification

- Run Unit Tests
- Test endpoints using Postman/Curl
