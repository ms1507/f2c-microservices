# Config Service Configuration

**File**: `src/main/resources/application.yml`

## Overview
The Config Service centralizes configuration management for all microservices. It can serve configurations from a Git repository or local file system.

## Configuration Details

### 1. Server Configuration
```yaml
server:
  port: 8888
```
- **`server.port`**: Standard port for Spring Cloud Config Server is **8888**.

### 2. Profile Selection
```yaml
spring:
  profiles:
    active: native
```
- **`active: native`**: Tells the Config Server to load configuration files from the **local filesystem** (classpath or file path) instead of Git.

### 3. Search Locations
```yaml
spring:
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config-repo, file:./config-repo, file:../config-repo
```
- **`search-locations`**: Defines where the server looks for configuration files (like `user-service.yml`).
    - `classpath:/config-repo`: Inside the JAR (for packaging).
    - `file:./config-repo`: In the current execution directory.
    - `file:../config-repo`: In the parent directory (useful during development).

### 4. Git Configuration (Commented Out)
The file contains commented-out configuration for Git-backed storage, which is the production standard.
```yaml
# git:
#   uri: https://github.com/your-org/config-repo.git
```
