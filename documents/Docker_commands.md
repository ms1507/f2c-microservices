# Docker & Docker Compose Commands Guide

This document provides a comprehensive list of Docker and Docker Compose commands used in the Rural Marketplace microservices project, along with their usage, explanations, and examples.

---

## 🐳 Docker Compose Commands
Since this project uses a `docker-compose.yml` to manage infrastructure, these are the most frequently used commands.

### 1. Start Services
**Command:** `docker-compose up -d`
- **Explanation:** Builds, (re)creates, starts, and attaches to containers for a service. The `-d` flag runs them in detached mode (background).
- **Example:** `docker-compose up -d redis` (Starts only the redis service).

### 2. Stop and Remove Resources
**Command:** `docker-compose down`
- **Explanation:** Stops containers and removes containers, networks, volumes, and images created by `up`.
- **Note:** Use `docker-compose stop` if you only want to stop containers without removing them.

### 3. Check Status
**Command:** `docker-compose ps`
- **Explanation:** Lists the status of containers managed by the current `docker-compose.yml`.

### 4. View Logs
**Command:** `docker-compose logs -f [service_name]`
- **Explanation:** Displays log output from services. The `-f` flag follows the log output.
- **Example:** `docker-compose logs -f kafka`

### 5. Restart Services
**Command:** `docker-compose restart [service_name]`
- **Explanation:** Restarts the specified service container.
- **Example:** `docker-compose restart rural_postgres`

---

## 🛠️ Basic Docker Commands
Useful for interacting with individual containers, images, and volumes.

### 1. List Running Containers
**Command:** `docker ps`
- **Explanation:** Shows all currently running containers. Use `docker ps -a` to see all containers (including stopped ones).

### 2. Execute Command Inside Container
**Command:** `docker exec -it [container_id/name] [command]`
- **Explanation:** Runs a command in a running container. `-it` provides an interactive terminal.
- **Example (Redis CLI):** `docker exec -it rural_redis redis-cli`
- **Example (Bash):** `docker exec -it rural_postgres bash`

### 3. Manage Images
- **List Images:** `docker images`
- **Remove Image:** `docker rmi [image_id]`
- **Prune Unused:** `docker image prune` (Removes dangling images).

### 4. Manage Volumes
- **List Volumes:** `docker volume ls`
- **Inspect Volume:** `docker volume inspect [volume_name]`
- **Remove Volume:** `docker volume rm [volume_name]`

---

## 🚀 Project Specific Examples

### Inspecting Redis Cache
To see the keys currently stored in your Redis cache:
```bash
docker exec -it rural_redis redis-cli keys "*"
```

### Checking Kafka Logs
To verify that the Kafka broker is running correctly:
```bash
docker logs rural_kafka
```

### Accessing PostgreSQL
To open a psql shell in the database container:
```bash
docker exec -it rural_postgres psql -U postgres -d rural_marketplace_db
```

### Monitoring Memory Usage
To see how much RAM/CPU each container is consuming:
```bash
docker stats
```
