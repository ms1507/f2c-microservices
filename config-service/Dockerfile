# --- Stage 1: Build ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy root pom and config-service pom
COPY pom.xml .
COPY config-service/pom.xml config-service/

# Download dependencies (caching layer)
RUN mvn dependency:go-offline -pl config-service -am

# Copy source code and config-repo
COPY config-service/src config-service/src
COPY config-repo config-repo

# Build the application
RUN mvn clean package -DskipTests -pl config-service -am

# --- Stage 2: Runtime ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/config-service/target/*.jar app.jar

# Expose the default config server port
EXPOSE 8888

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
