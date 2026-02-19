# Phase 9: DevOps & CI/CD Roadmap

This phase focuses on standardizing the deployment lifecycle, automating testing, and ensuring consistent environments from development to production using Docker and GitHub Actions.

---

## 🛠️ Prerequisites & Tools

### Local Environment
- **Docker Desktop**: Required to build and run containers locally.
- **Docker Compose**: Installed with Docker Desktop; used for multi-container orchestration.
- **Java 17+ & Maven**: To build the application JARs before containerization.

### External Tools & Accounts
- **GitHub Account**: To host the repository and run GitHub Actions.
- **Docker Hub Account**: A registry to store and version your container images.
- **GitHub Secrets**: For securely storing `DOCKER_USERNAME` and `DOCKER_PASSWORD` in your repository settings.

---

## 🏁 Step-by-Step Roadmap

### Step 1: Optimized Multi-stage Dockerization
**Objective:** Create lightweight, secure, and fast-loading Docker images for each microservice.

**Why Multi-stage?**
It separates the *build* environment (Maven, JDK) from the *runtime* environment (JRE only), reducing image size from ~500MB to ~150MB.

**Example: `Dockerfile` for `catalog-service`**
```dockerfile
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
# Copy only pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .
COPY common-library/pom.xml common-library/
COPY catalog-service/pom.xml catalog-service/
# Install dependencies
RUN mvn dependency:go-offline -pl catalog-service -am
# Copy source and build
COPY common-library common-library
COPY catalog-service catalog-service
RUN mvn clean package -DskipTests -pl catalog-service -am

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/catalog-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### Step 2: Local Orchestration with Docker Compose
**Objective:** Run the entire ecosystem (all services + infrastructure) with a single command.

**Implementation:**
Update the root `docker-compose.yml` to include the microservices, ensuring they wait for infrastructure (Postgres, Kafka, Redis) to be healthy.

**Example Snippet:**
```yaml
services:
  catalog-service:
    build:
      context: .
      dockerfile: catalog-service/Dockerfile
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - CONFIG_SERVER_URL=http://config-service:8888
    depends_on:
      postgres:
        condition: service_healthy
      discovery-service:
        condition: service_started
```

---

### Step 3: CI/CD Pipeline with GitHub Actions
**Objective:** Automate the "Build -> Test -> Push" flow whenever code is pushed to GitHub.

**Workflow Location:** `.github/workflows/ci-cd.yml`

**Key Steps in Workflow:**
1. **Checkout Code**: Access the repository.
2. **Setup Java**: Configure the build environment.
3. **Maven Build**: Run `mvn clean install` to verify all modules and run unit tests.
4. **Docker login**: Authenticate with Docker Hub using Secrets.
5. **Build & Push**: Build the image and push it with a tag (e.g., `latest` or `v1.0.0`).

---

### Step 4: Environment-Specific Configuration
**Objective:** Ensure microservices can adapt to different environments (Local, Docker, Cloud) without code changes.

- **Profiles**: Use `application-docker.yml` in `config-repo` to point to container names (e.g., `jdbc:postgresql://postgres:5432/...`) instead of `localhost`.

---

## 🧪 Verification Plan

### 1. Manual Docker Build
Run `docker build -t rural-catalog:v1 -f catalog-service/Dockerfile .` and verify the image is created.

### 2. Full Stack Test
Run `docker-compose up -d` and check Eureka (`localhost:8761`) to see if all services registered via their container names.

### 3. CI Simulation
Push a change to a feature branch and observe the "Actions" tab in GitHub to ensure the build finishes successfully.
