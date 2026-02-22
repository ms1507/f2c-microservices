# --- Stage 1: Build ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy root pom and ALL module poms to satisfy parent POM requirements
COPY pom.xml .
COPY common-library/pom.xml common-library/
COPY config-service/pom.xml config-service/
COPY discovery-service/pom.xml discovery-service/
COPY api-gateway/pom.xml api-gateway/
COPY user-service/pom.xml user-service/
COPY catalog-service/pom.xml catalog-service/
COPY order-service/pom.xml order-service/
COPY notification-service/pom.xml notification-service/

# Download dependencies (caching layer)
RUN mvn dependency:go-offline -pl notification-service -am

# Copy source code for common-library and notification-service
COPY common-library common-library
COPY notification-service notification-service

# Build the application
RUN mvn clean package -DskipTests -pl notification-service -am

# --- Stage 2: Runtime ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/notification-service/target/*.jar app.jar

# Expose the Notification Service port
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
