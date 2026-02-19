# Phase 9: DevOps & CI/CD - Comprehensive Implementation Plan

This document provides a complete roadmap for standardizing the deployment lifecycle, automating testing, and ensuring consistent environments across the Rural Marketplace microservices ecosystem.

---

## 1. Requirements

### Functional Requirements
- **Automated Builds**: Every push to the main branch must trigger an automated Maven build.
- **Artifact Generation**: Successful builds should generate Docker images.
- **Image Registry**: Images must be pushed to a secure remote registry (Docker Hub).
- **Service Orchestration**: Ability to bring up the entire stack (services + infrastructure) with a single command.
- **Status Reporting**: GitHub should display the pass/fail status of each build pipeline.

### Non-Functional Requirements
- **Efficiency**: Use multi-stage Docker builds to keep image sizes small (< 200MB).
- **Security**: Manage sensitive credentials (Docker Hub tokens, etc.) using Secret Management.
- **Reliability**: Use health checks to ensure services start only after their dependencies (DB, Kafka) are ready.
- **Consistency**: Parity between local development and CI environments.

---

## 2. Prerequisites

### Local Infrastructure
- **Docker Desktop**: Version 4.x or higher (with Docker Compose V2).
- **Java JDK 17**: Must be installed and configured in the system PATH.
- **Maven 3.8+**: Required for local build verification.

### External Services
- **GitHub Repository**: The codebase must be hosted on GitHub to use GitHub Actions.
- **Docker Hub Account**: A registered account to host private or public images.
- **Internet Connectivity**: Required for downloading base images and pushing layers.

---

## 3. Proposed Solutions & Features

### Solution A: Multi-Stage Dockerization
**Feature**: Separation of Build and Runtime environments.
- **How it works**: The first stage uses a full Maven/JDK image to compile the code. The second stage uses a lightweight JRE image and only copies the resulting `.jar` file.
- **Benefit**: Faster deployment, smaller footprint, and reduced attack surface.

### Solution B: GitHub Actions CI Pipeline
**Feature**: Event-driven automation within the GitHub ecosystem.
- **How it works**: YAML-based configuration defined in `.github/workflows/`. It listens for `push` and `pull_request` events.
- **Benefit**: Zero-cost (for public repos) and deep integration with the source control provider.

### Solution C: Docker Compose V2 Orchestration
**Feature**: Declarative multi-container management.
- **How it works**: A single `docker-compose.yml` file defines all 10+ containers (services + middleware).
- **Benefit**: One-command setup (`docker-compose up`) for new developers and QA testers.

---

## 4. Step-by-Step Guidelines

### Step 1: Create a `.dockerignore` file
**Goal**: Prevent unnecessary files (logs, IDE settings) from being sent to the Docker daemon.
- **Example**:
  ```text
  target/
  .git/
  .idea/
  *.log
  ```

### Step 2: Implement Multi-Stage Dockerfiles
**Goal**: Create a `Dockerfile` in each microservice directory.
- **Reference Template** (for `catalog-service`):
  ```dockerfile
  # --- Stage 1: Build ---
  FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
  WORKDIR /app
  COPY pom.xml .
  COPY common-library/ common-library/
  COPY catalog-service/ catalog-service/
  RUN mvn clean package -DskipTests -pl catalog-service -am

  # --- Stage 2: Runtime ---
  FROM eclipse-temurin:17-jre-alpine
  WORKDIR /app
  COPY --from=build /app/catalog-service/target/*.jar app.jar
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```

### Step 3: Update Centralized Config for Docker
**Goal**: Create an `application-docker.yml` in the `config-repo` to handle internal service communication.
- **Key Change**: Change `localhost:5432` to `postgres:5432` and `localhost:9092` to `kafka:29092`.

### Step 4: Finalize Docker Compose
**Goal**: Define the relationship between containers.
- **Example**:
  ```yaml
  catalog-service:
    build: 
      context: .
      dockerfile: catalog-service/Dockerfile
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - postgres
      - config-service
  ```

### Step 5: Configure GitHub Secrets & Actions
**Goal**: Automate the push to Docker Hub.
1. Go to GitHub Repo -> Settings -> Secrets and Variables -> Actions.
2. Add `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN`.
3. Create `.github/workflows/ci-cd.yml`.

---

## 5. Verification Checklist
- [ ] Direct `docker build` succeeds for each service.
- [ ] `docker-compose up` brings up all services healthy.
- [ ] GitHub Actions log shows green for the `maven-build` and `docker-push` jobs.
