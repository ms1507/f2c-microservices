# Rollback: Phase 7 Observability

This plan outlines the steps to remove the Phase 7 Observability (LGTM stack) from the microservices codebase while preserving all documentation for future reference.

## Proposed Changes

### [Infrastructure]
#### [MODIFY] [docker-compose.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/docker-compose.yml)
- Remove `otel-collector`, `prometheus`, `loki`, `tempo`, and `grafana` services.
- Clean up the `--- OBSERVABILITY STACK ---` section.

### [Build & Dependencies]
#### [MODIFY] [pom.xml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/pom.xml)
- Remove `micrometer-tracing-bridge-otel` and `opentelemetry-exporter-otlp` from `dependencyManagement`.

#### [MODIFY] [common-library/pom.xml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/common-library/pom.xml)
- Remove observability dependencies: `spring-boot-starter-actuator`, `micrometer-registry-prometheus`, `micrometer-tracing-bridge-otel`, `opentelemetry-exporter-otlp`, and `loki-logback-appender`.

### [Configuration]
#### [MODIFY] [config-repo/application.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/config-repo/application.yml)
- Remove all `management`, `loki`, and `logging.pattern` configurations added for observability.

#### [MODIFY] [logback-spring.xml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/common-library/src/main/resources/logback-spring.xml)
- Revert to a basic configuration that only includes console logging without Loki or Trace ID correlation.

## Verification Plan

### Manual Verification
1. **Clean Rebuild**: Run `mvn clean install` on the root project to ensure no dependency errors.
2. **Service Startup**: Start `discovery-service`, `config-service`, and then one business service (e.g., `user-service`).
3. **Verify Logs**: Ensure logs appear on the console in the standard format without empty trace/span brackets.
4. **Endpoint Check**: Verify that `/actuator/prometheus` and other observability endpoints are no longer active OR only basic health is available.
5. **Docker Check**: Ensure `docker-compose up -d` only starts the core infrastructure (PostgreSQL, Kafka).
