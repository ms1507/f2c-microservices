# --- Stage 1: Build ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy root pom, common-library pom, and user-service pom
COPY pom.xml .
COPY common-library/pom.xml common-library/
COPY user-service/pom.xml user-service/

# Download dependencies (caching layer)
RUN mvn dependency:go-offline -pl user-service -am

# Copy source code for common-library and user-service
COPY common-library common-library
COPY user-service user-service

# Build the application
RUN mvn clean package -DskipTests -pl user-service -am

# --- Stage 2: Runtime ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/user-service/target/*.jar app.jar

# Expose the User Service port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
