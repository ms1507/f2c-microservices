# Microservices Observability Guide: LGTM Stack

This document outlines the strategy, architecture, and implementation patterns for Observability in the Rural Marketplace microservices ecosystem using the **LGTM** (**L**oki, **G**rafana, **T**empo, **M**icrometer/Prometheus) stack.

---

## 🔍 Core Pillars of Observability

Observability is the ability to understand the internal state of a system from its external outputs.

1.  **Metrics (Prometheus/Micrometer)**: "Is something wrong?" (e.g., High CPU, Error Rate spikes).
2.  **Logging (Loki)**: "Why is it happening?" (Detailed stack traces and error messages).
3.  **Tracing (Tempo)**: "Where is it happening?" (End-to-end request flow across multiple services).
4.  **Dashboards (Grafana)**: The "single pane of glass" that correlates all three.

---

## 🏗️ The LGTM Components: Deep Dive

### 1. Prometheus (Metrics)
*   **What it is**: A Time Series Database (TSDB) and monitoring system.
*   **Internal Workings**:
    *   **Pull Model**: Typically scrapes metrics from HTTP endpoints (like `/actuator/prometheus`).
    *   **Multi-dimensional Data**: Uses key-value pairs (labels) for efficient data slices.
    *   **PromQL**: A powerful functional query language for aggregations.
*   **Why we use it**: It is the industry standard for monitoring cloud-native applications. It is lightweight, scales well, and handles numeric time-series data with extreme efficiency.

### 2. Loki (Logs)
*   **What it is**: A horizontally-scalable, highly-available, multi-tenant log aggregation system inspired by Prometheus.
*   **Internal Workings**:
    *   **Label-based Indexing**: Unlike Elasticsearch, Loki does *not* index the log text. It only indexes the labels attached to the log stream (e.g., `app=user-service`, `env=prod`).
    *   **Chunks & Streams**: Logs are compressed and stored as "chunks" in object storage.
    *   **LogQL**: A query language very similar to PromQL.
*   **Why we use it**: It is significantly more cost-effective than full-text search engines. Since it shares labels with Prometheus, switching between metrics and logs in Grafana is seamless.

### 3. Tempo (Traces)
*   **What it is**: A high-scale, distributed tracing backend.
*   **Internal Workings**:
    *   **Trace-ID Index Only**: Tempo only indexes Trace IDs. It does not index span metadata, making it incredibly cheap to run at high volume.
    *   **Discovery**: It relies on logs (Loki) or metrics (Prometheus) to provide a Trace ID, then it retrieves the complete trace.
    *   **Storage**: Designed to store massive amounts of trace data in simple object storage.
*   **Why we use it**: It completes the observability loop. When you see an error in Loki, you can immediately jump to the specific Span in Tempo to see exactly where the bottleneck or failure occurred.

### 4. Grafana (Visualization)
*   **What it is**: The open-source platform for monitoring and observability.
*   **Internal Workings**:
    *   **Data Source Architecture**: Grafana doesn't store data. It is a portal that queries your backends (Loki, Prometheus, Tempo) and presents them in a unified UI.
    *   **Data Correlation**: It uses "Derived Fields" to link logs to traces and "Exemplars" to link metrics to traces.
*   **Why we use it**: It provides a unified "Single Pane of Glass." Instead of jumping between three different tools, an engineer can correlate a spike in Prometheus with the error logs in Loki and the trace span in Tempo, all from one dashboard.

---

## 🛠️ Step-by-Step Implementation (OTel Collector Approach)

We implemented **Approach 2: OpenTelemetry Collector**, which is the industry best practice for decoupling applications from their observability backends.

### Step 1: Monitoring Infrastructure Configuration
Created specialized configuration files for the LGTM stack backends:
- `otel-collector-config.yml`: Routes OTLP signals from services to backends.
- `prometheus.yml`: Configured scraping intervals and targets.
- `loki-config.yml` & `tempo-config.yml`: Defined storage and retention policies.

### Step 2: Docker Infrastructure Update
Integrated the following services into the [docker-compose.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/docker-compose.yml):
- **OTel Collector**: The gateway for all telemetry.
- **Prometheus**: TSDB for metrics.
- **Loki**: Log aggregator.
- **Tempo**: Trace storage.
- **Grafana**: Unified UI.

### Step 3: Root Dependency Management
Updated the root [pom.xml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/pom.xml) with the following versions:
- `micrometer-tracing-bridge-otel`: (Trace bridging).
- `opentelemetry-exporter-otlp`: (Standard OTLP export).

### Step 4: Common Library Integration
Added core libraries to [common-library/pom.xml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/common-library/pom.xml) to ensure all microservices inherit monitoring capabilities:
- `spring-boot-starter-actuator`: Health and metrics.
- `micrometer-registry-prometheus`: Prometheus metrics export.
- `loki4j-logback-appender`: High-performance log shipping for Loki.

### Step 5: Centralized Config (Actuator & Tracing)
Updated [config-repo/application.yml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/config-repo/application.yml) to enable features globally:
- **Actuator Exposure**: Exposed `health`, `metrics`, and `prometheus`.
- **Sampling**: Set `management.tracing.sampling.probability=1.0` for full visibility in dev.
- **OTLP Endpoint**: Pointed all tracing to the `otel-collector` endpoint.

### Step 6: Logging Pattern Standardization
Configured a unified logging format in the `config-repo`:
- Format: `[${spring.application.name},%X{traceId:-},%X{spanId:-}]`
- This ensures that every log line globally can be correlated with a Trace and Span.

### Step 7: Shared Logback Configuration
Implemented a standardized [logback-spring.xml](file:///d:/Rural_marketplace_App/RuralMarketPlace-backend/common-library/src/main/resources/logback-spring.xml) in the `common-library`. This file handles:
- Console logging for local debugging.
- Conditional shipping of logs to Loki in the cloud/containerized environments.

### Step 8: Build & Verification
Cleaned and rebuilt the ecosystem starting from the `common-library` to verify that all dependencies and transitive configurations are stable.

---

---

## 🚀 Verification Guidelines (How to Check Logs, Traces & Metrics)

Follow these steps to explore the observability data in your running system.

### 1. Unified Dashboard Access (Grafana)
Grafana is your main interface for all observability data.
*   **URL**: [http://localhost:3000](http://localhost:3000)
*   **Credentials**: Admin access is automatically granted (no password required in Dev).
*   **Data Sources**: Navigate to **Configuration > Data Sources** to ensure **Prometheus**, **Loki**, and **Tempo** are active.

### 2. Checking Metrics (Prometheus)
Metrics help you monitor system health and performance.
*   **In Grafana**:
    1. Click the **Explore** icon (compass) on the sidebar.
    2. Select **Prometheus** from the data source dropdown.
    3. Use the query builder or enter a PromQL query like:
       - `http_server_requests_seconds_count`: Count of all HTTP requests.
       - `process_cpu_usage`: Current CPU usage of the microservice.
       - `jvm_memory_used_bytes`: Memory consumption.
*   **Direct Access**: You can also see raw metrics at [http://localhost:<service-port>/actuator/prometheus].

### 3. Checking Logs (Loki)
Loki aggregates logs from all services into a single searchable stream.
*   **In Grafana**:
    1. Go to **Explore** and select **Loki**.
    2. Use **LogQL** to filter logs:
       - `{app="user-service"}`: Sees all logs for the User service.
       - `{level="ERROR"}`: Sees all error logs across the ecosystem.
    3. Notice the **Trace ID correlation**: Every log line contains `[service-name, traceId, spanId]`.

### 4. Checking Traces (Tempo)
Tempo allows you to visualize the journey of a single request across service boundaries.
*   **How finding Traces works**:
    1. **From Logs**: When viewing logs in Loki, click on any **TraceID**. Grafana will automatically split the screen and show you the full trace in Tempo.
    2. **Manual Search**: 
       - Go to **Explore** and select **Tempo**.
       - Use the **Search** tab to find traces by service name, duration, or HTTP status (e.g., `status=500`).
*   **What to look for**: The "Service Graph" or "Node Graph" shows you the path the request took (e.g., `Gateway -> Order Service -> Catalog Service`).

### 5. Summary Table: Where to find what?
| Signal | Tool | Query Language | Purpose |
| :--- | :--- | :--- | :--- |
| **Metrics** | Prometheus | PromQL | "Are we healthy?" |
| **Logs** | Loki | LogQL | "What exactly happened?" |
| **Traces** | Tempo | TraceQL | "Where is the bottleneck/fail?" |
| **Visuals** | Grafana | - | The "Single Pane of Glass" |
