# --- Stage 1: Build ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy root pom and discovery-service pom
COPY pom.xml .
COPY discovery-service/pom.xml discovery-service/

# Download dependencies (caching layer)
RUN mvn dependency:go-offline -pl discovery-service -am

# Copy source code
COPY discovery-service/src discovery-service/src

# Build the application
RUN mvn clean package -DskipTests -pl discovery-service -am

# --- Stage 2: Runtime ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/discovery-service/target/*.jar app.jar

# Expose the Eureka dashboard port
EXPOSE 8761

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
