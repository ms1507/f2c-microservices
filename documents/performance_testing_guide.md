# Performance Testing Guide: Rural Marketplace

This guide covers how to measure and improve the performance of our microservices architecture, specifically focusing on API response times and caching efficiency.

---

## 1. Manual Latency Benchmarking
The easiest way to verify Phase 8 (Caching).

1.  **Cold Start:** Restart Catalog Service or run `docker exec -it rural_redis redis-cli flushall`.
2.  **First Request:** Send `GET /api/v1/products` via Postman. 
    - *Expected:* ~100-200ms (Data fetched from PostgreSQL).
3.  **Warm Request:** Send the same request again.
    - *Expected:* ~10-20ms (Data fetched from Redis).

---

## 3. High-Volume Testing with Apache JMeter
Apache JMeter is an industry-standard GUI tool for simulating heavy loads on servers, networks, or objects.

### 3.1 Prerequisites
- **Java:** JMeter requires Java 8 or higher (which we already have for the microservices).
- **Download:** Get the latest binaries from [jmeter.apache.org](https://jmeter.apache.org/download_jmeter.cgi).
- **Extraction:** Extract the zip/tar file and navigate to the `bin` folder.
- **Launch:** Run `jmeter.bat` (Windows) or `jmeter` (Linux/Mac).

### 3.2 Basic Workflow
1.  **Test Plan:** Right-click 'Test Plan' -> Add -> Threads -> **Thread Group**.
    - Set *Number of Threads* (Concurrent users).
    - Set *Ramp-up period* (How fast to start users).
    - Set *Loop Count* (How many times to repeat).
2.  **HTTP Request:** Right-click 'Thread Group' -> Add -> Sampler -> **HTTP Request**.
    - Server Name/IP: `localhost`
    - Port: `8080`
    - Path: `/api/v1/products`
3.  **Headers:** Right-click 'HTTP Request' -> Add -> Config Element -> **HTTP Header Manager**.
    - Add `Content-Type`: `application/json`.
4.  **Listeners:** Right-click 'Thread Group' -> Add -> Listener.
    - **View Results Tree:** To see raw request/response.
    - **Summary Report:** To see Average, Min, Max, and Throughput (RPS).
5.  **Run:** Click the green **Play** icon to start the test.

---

## 4. Metrics to Monitor (Grafana)
While running tests, monitor these in [http://localhost:3000](http://localhost:3000):

| Metric Categories | Prometheus Expression | Dashboard Utility |
| :--- | :--- | :--- |
| **Throughput** | `rate(http_server_requests_seconds_count[1m])` | Requests Per Second (RPS) |
| **Error Rate** | `rate(http_server_requests_seconds_count{status=~"5.."}[1m])` | Identify service failures |
| **Cache Hit Rate** | `cache_gets_total` vs `cache_puts_total` | Verify Redis usage |
| **DB Pool** | `hikaricp_connections_active` | Check if DB is getting overwhelmed |

---

## 4. Key Performance Indicators (KPIs)
*   **P95 Latency:** 95% of requests should be faster than this value.
*   **Standard Target:**
    - Cached Reads: **< 30ms**
    - Uncached Reads (DB): **< 200ms**
    - Writes (POST/PUT): **< 500ms**

---

## 5. Optimization Strategies
If performance is low:
1.  **Index Database Columns:** Ensure `category_id` and `farmer_id` have indexes in PostgreSQL.
2.  **Increase DB Pool Size:** Adjust `spring.datasource.hikari.maximum-pool-size` in `application.yml`.
3.  **Horizontal Scaling:** Run multiple instances of `catalog-service` and let API Gateway load balance them.
4.  **Redis TTL Tuning:** Adjust cache expiration based on how frequently your data changes.
