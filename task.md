# Task: Phase 5 - Resilience & Fault Tolerance (Resilience4j)

## Infrastructure Setup
- [x] **Dependencies**: Add `resilience4j-spring-boot3` and `spring-boot-starter-aop` to `order-service`.
- [x] **Config**: Add Resilience4j configurations to `order-service.yml` in `config-repo`.

## Implementation
- [x] **Circuit Breaker**: Apply `@CircuitBreaker` to `ProductClient` calls in `OrderService`.
- [x] **Fallback**: Implement fallback logic for when Catalog Service is unavailable.
- [x] **Retry**: Add `@Retry` for transient failures on Feign clients.

## Verification
- [x] **Build**: Build SUCCESS.
- [ ] **Simulator**: Shut down `catalog-service` (Manual).
- [ ] **Test**: Place order ➔ Verify fallback response (Manual).
- [ ] **Observation**: Monitor circuit breaker state via Actuator (Manual).
