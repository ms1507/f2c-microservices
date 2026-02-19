package com.rural.marketplace.gateway.filter;

import com.rural.marketplace.common.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthenticationFilterGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthenticationFilterGatewayFilterFactory.Config> {

    private final JwtService jwtService;

    public AuthenticationFilterGatewayFilterFactory(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 1. Check if route is secured (handled by definition, but double check
            // headers)
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Missing Authorization header");
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            // 2. Extract Token
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            } else {
                log.warn("Invalid Authorization header format");
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            // 3. Validate Token
            try {
                if (jwtService.validateToken(authHeader)) {
                    // 4. Extract Claims (username and userId)
                    String username = jwtService.extractUsername(authHeader);
                    Long userId = jwtService.extractClaim(authHeader,
                            claims -> claims.get("userId", Long.class));

                    log.debug("ACCESS GRANTED: User [{}] (ID: {}) authorized for request to [{}]",
                            username, userId, exchange.getRequest().getURI());

                    // 5. Mutate the request to add custom headers for downstream services
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("X-Logged-In-User", username)
                                    .header("X-User-Id", userId != null ? userId.toString() : "")
                                    .build())
                            .build();

                    // 6. Continue filter chain with mutated exchange
                    return chain.filter(mutatedExchange);

                } else {
                    log.error("ACCESS DENIED: Invalid Token for request to [{}]", exchange.getRequest().getURI());
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }
            } catch (Exception e) {
                log.error("ACCESS DENIED: Token validation error for request to [{}]: {}",
                        exchange.getRequest().getURI(), e.getMessage());
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Put configuration properties here
    }
}
