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
- **Trigger**: `push` or `pull_request` to the `dev` branch.
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
3. Click **Generate New Token**, name it "GitHub-Actions".
4. **IMPORTANT**: In the **Access Permissions** dropdown, select **"Read, Write, Manage"** (or at least **"Read & Write"**). If set to "Public Read-only", the push will fail.
5. Click Generate and **copy it**.

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

To safely verify the CI/CD pipeline, use the Pull Request (PR) workflow. This allows you to validate the build before merging into `dev`.

### 📥 Step 1: Push to Feature Branch
Run these commands in your terminal at the project root to upload your changes:

```bash
# Add all new Docker and Workflow files
git add .

# Commit the changes
git commit -m "feat: complete phase 9 devops implementation"

# Push to your current feature/dev branch
git push origin f2c-dev
```

### 🔀 Step 2: Raise a Pull Request (GitHub Website)
1.  Open your repository on **GitHub** in your browser.
2.  Click the **"Pull requests"** tab -> **"New pull request"**. 
3.  Select `dev` as the **base** branch and `f2c-dev` as the **compare** branch.
4.  Click **"Create pull request"**.

### 📺 Step 3: Monitor & Verify
1.  **Check the PR**: Go to the bottom of your new Pull Request page to find the checks section.
2.  **Watch the Build**: You will see **"Rural Marketplace CI/CD / build-and-push"** in progress. Click **"Details"** to watch the real-time build and push logs.
3.  **Merge**: Once the build turns **Green (✅)**, click **"Merge pull request"**. This officially updates your images on **Docker Hub**.

### 🐳 Step 4: Final Docker Hub Verification
Log into [Docker Hub](https://hub.docker.com/). You should see 7 repositories (e.g., `rural-catalog-service`) updated with the `:latest` tag.

---

## ❓ Frequently Asked Questions

### Q: What will happen next if the Pull Request is merged successfully to the `dev` branch?

Once the pull request is merged successfully into the **`dev`** branch, the following automated sequence occurs:

1.  **Second CI/CD Execution (The "Push" Trigger)**: Even though the build ran when you opened the PR, it will run again immediately upon merging. This ensures that the final merged state of the code is the one that gets built and pushed to Docker Hub.
2.  **Docker Hub Update**: The GitHub Action will perform a final Maven build, re-build the Docker images for all 7 services, and overwrite the previous images on your Docker Hub with the new `:latest` versions.
3.  **Readiness for Local Deployment**: The moment the GitHub Action turns green (✅) after the merge, you can pull those fresh images to your local machine by running `docker-compose pull` followed by `docker-compose up -d`.
4.  **Transition to Phase 10**: With the CI/CD pipeline successfully verified with a real merge, we are officially ready to move to **Phase 10: Cloud Deployment & Kubernetes**.

---

## 🛠️ Local Verification & Development

To test the containerized environment on your own machine:

1.  **Build Code**: `mvn clean install -DskipTests`
2.  **Start Containers**: `docker-compose up -d --build`
3.  **Logs**: `docker-compose logs -f <service-name>` (e.g., `api-gateway`)

For a comprehensive narrative of the implementation and visual success confirmation, see the:
👉 **[Phase Completion Walkthrough](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/documents/Phase-9-DevOps-CI-CD/Phase-Completion-Walkthrough.md)**
