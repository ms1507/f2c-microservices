package com.rural.marketplace.order.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ResilienceLoggingConfig {

    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    public void postConstruct() {
        // Logging for Retry attempts
        retryRegistry.retry("catalogService").getEventPublisher()
                .onRetry(event -> log.info("Retry attempt #{} for catalogService. Reason: {}",
                        event.getNumberOfRetryAttempts(), event.getLastThrowable().getMessage()))
                .onSuccess(event -> log.info("Retry succeeded for catalogService after #{} attempts",
                        event.getNumberOfRetryAttempts()))
                .onError(event -> log.error("Retry failed for catalogService after #{} attempts. Exhausted.",
                        event.getNumberOfRetryAttempts()));

        // Logging for Circuit Breaker state changes
        circuitBreakerRegistry.circuitBreaker("catalogService").getEventPublisher()
                .onStateTransition(event -> log.warn("Circuit Breaker 'catalogService' transitioned from {} to {}",
                        event.getStateTransition().getFromState(), event.getStateTransition().getToState()))
                .onCallNotPermitted(
                        event -> log.error("Circuit Breaker 'catalogService' is OPEN. Call not permitted."));
    }
}
