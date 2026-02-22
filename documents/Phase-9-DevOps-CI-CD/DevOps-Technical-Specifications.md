# Phase 9: DevOps Technical Specifications

This document provides a consolidated technical reference for the containerization and automation components implemented in Phase 9.

---

## 🐋 1. Microservice Dockerfiles

All services follow a **"Standardized Recipe"** pattern: the steps/structure are identical across the ecosystem to ensure maintainability, while the "ingredients" (files and references) are tailored for each specific service.

### 💡 Design Philosophy: The Recipe Template
| Step-Wise Structure (Same for all) | Ingredients/References (Service-Specific) |
| :--- | :--- |
| **Stage 1 (Build Environment)** | Specific Maven/JDK version for the project. |
| **Working Directory** | Standardized to `/app` for all containers. |
| **Dependency Metadata** | Individual `pom.xml` paths based on module names. |
| **Build Execution** | Service name flag (e.g., `-pl catalog-service`). |
| **Stage 2 (Runtime Environment)** | Slim JRE Alpine image for performance. |
| **Artifact Injection** | Specific JAR path from the build stage. |
| **Exposure & Launch** | Unique ports (8080-8084) and JAR execution. |

### 🏗️ Build Stage Details
- **Base Image**: `maven:3.9.6-eclipse-temurin-17-alpine`
- **Logic**: 
    1. Copies root `pom.xml` and service-specific `pom.xml` to leverage Docker's layer caching for dependencies.
    2. Downloads dependencies early to avoid re-downloading on code-only changes.
    3. Builds the specific module using the `-am` (also-make) flag to include the `common-library`.

### 🏃 Runtime Stage Details
- **Base Image**: `eclipse-temurin:17-jre-alpine`
- **Security**: Contains only the Java Runtime, significantly reducing the attack surface.
- **Port Management**: Uses `EXPOSE` to document the container's primary listener.

### 📊 Service Matrix
| Service Name | Dockerfile Path | Internal Port | External Port |
| :--- | :--- | :--- | :--- |
| `config-service` | `config-service/Dockerfile` | 8888 | 8888 |
| `discovery-service` | `discovery-service/Dockerfile` | 8761 | 8761 |
| `api-gateway` | `api-gateway/Dockerfile` | 8080 | 8080 |
| `user-service` | `user-service/Dockerfile` | 8081 | N/A (Internal) |
| `catalog-service` | `catalog-service/Dockerfile` | 8082 | N/A (Internal) |
| `order-service` | `order-service/Dockerfile` | 8083 | N/A (Internal) |
| `notification-service` | `notification-service/Dockerfile` | 8084 | N/A (Internal) |

---

## 🎼 2. Docker Compose Orchestration

The root `docker-compose.yml` orchestrates 11 containers (4 Infrastructure + 7 Microservices).

### 🛠️ Infrastructure Components
- **Postgres**: Healthchecked with `pg_isready`.
- **Kafka + Zookeeper**: Healthchecked by checking connectivity on port `29092`.
- **Redis**: Alpine-based, healthchecked with `redis-cli ping`.

### 🔄 Startup Dependencies (Boot Order)
To ensure system stability, services use the `service_healthy` condition:
1. **Config Service** (Starts first).
2. **Discovery Service** (Waits for Config).
3. **Internal Services** (Wait for Discovery + DBs + Kafka).

---

## 🤖 3. GitHub & Docker Hub Integration

### Automated CI/CD Workflow
- **File**: `.github/workflows/ci-cd.yml`
- **Trigger**: `push` or `pull_request` to the `main` branch.
- **Steps**:
    1. Checkout code and setup JDK 17.
    2. Build all modules via Maven.
    3. Authenticate with Docker Hub registry.
    4. Build and Push 7 tagged images.

### 🔐 Sensitive Information (GitHub Secrets)
The following secrets must be configured in your GitHub Repository settings (**Settings -> Secrets -> Actions**).

#### 🛠️ Manual Activity: How to Add Secrets

**Stage 1: Generate a Docker Hub Token** (Better than using your password):
1.  Login to [Docker Hub](https://hub.docker.com/).
2.  Go to **Account Settings** -> **Security** -> **Personal Access Tokens**.
3.  Click **Generate New Token**, name it "GitHub-Actions", and **copy it**.

**Stage 2: Add to GitHub**:
1.  Open your repository on GitHub in your browser.
2.  Go to the **Settings** tab.
3.  On the left sidebar, click **Secrets and variables** -> **Actions**.
4.  Click the **New repository secret** button.
5.  Add the following two secrets:

| Secret Name | Value |
| :--- | :--- |
| **`DOCKERHUB_USERNAME`** | Your Docker Hub username. |
| **`DOCKERHUB_TOKEN`** | The Token you copied from Stage 1. |

### 🏷️ Image Tagging Convention
Images are pushed to Docker Hub using the pattern:
`${DOCKERHUB_USERNAME}/rural-<service-name>:latest`

---

## 🌐 4. Networking & Configuration
- **Network Mode**: Bridge (named `rural-network`).
- **Profile**: All microservices are started with `-Dspring.profiles.active=docker`.
- **Hostname Overrides**: Controlled via `config-repo/application-docker.yml`.

---

## 🚀 5. Verification & Activation (Pull Request Workflow)

To safely verify the CI/CD pipeline, use the Pull Request (PR) workflow. This allows you to validate the build before merging into `main`.

### 📥 Step 1: Push to Development Branch
Run these commands in your terminal at the project root to upload your changes:

```bash
# Add all new Docker and Workflow files
git add .

# Commit the changes
git commit -m "feat: complete phase 9 devops implementation"

# Push to your development branch
git push origin f2c-dev
```

### 🔀 Step 2: Raise a Pull Request (GitHub Website)
1.  Open your repository on **GitHub** in your browser.
2.  Click the **"Pull requests"** tab -> **"New pull request"**. 
3.  Select `main` as the **base** branch and `f2c-dev` as the **compare** branch.
4.  Click **"Create pull request"**.

### 📺 Step 3: Monitor & Verify
1.  **Check the PR**: Go to the bottom of your new Pull Request page to find the checks section.
2.  **Watch the Build**: You will see **"Rural Marketplace CI/CD / build-and-push"** in progress. Click **"Details"** to watch the real-time build and push logs.
3.  **Merge**: Once the build turns **Green (✅)**, click **"Merge pull request"**. This officially updates your images on **Docker Hub**.

### 🐳 Step 4: Final Docker Hub Verification
Log into [Docker Hub](https://hub.docker.com/). You should see 7 repositories (e.g., `rural-catalog-service`) updated with the `:latest` tag.
