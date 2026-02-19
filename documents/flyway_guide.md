# Flyway Configuration Guide

Follow these steps to integrate Flyway into any service (e.g., `user-service`).

## Step 1: Add Dependencies
Add these to your `pom.xml` (inside the `<dependencies>` section):

```xml
<!-- Flyway Core -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<!-- Flyway Postgres Extension (Required for Spring Boot 3+) -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

## Step 2: Configure `application.yml`
Update your `src/main/resources/application.yml` to disable Hibernate's auto-DDL and enable Flyway:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate # CHANGE THIS from 'update' to 'validate' (or 'none')
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

## Step 3: Create Migration Directory
Create this folder structure in your project:
`src/main/resources/db/migration`

## Step 4: Create Initial Script
Create a SQL file named `V1__init_schema.sql` inside that folder.
(Note: It is `V1` with **two underscores** `__`).

Example Content:
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Step 5: Run Application
Start the service. Flyway will automatically detect the SQL file inside `db/migration`, create a `flyway_schema_history` table, and execute your script.
