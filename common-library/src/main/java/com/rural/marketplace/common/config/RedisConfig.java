package com.rural.marketplace.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Configuration class for Redis-based caching.
 * Provides the {@link RedisCacheManager} with custom JSON serialization and
 * Time-To-Live (TTL) settings.
 * Also implements {@link CachingConfigurer} to provide a custom
 * {@link CacheErrorHandler} for system resilience.
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisConfig implements CachingConfigurer {

    /**
     * Configures a {@link RedisCacheManager} with default settings.
     * <ul>
     * <li>Default TTL: 10 minutes</li>
     * <li>Null values: Caching disabled</li>
     * <li>Serialization: Values are serialized as JSON using
     * {@link GenericJackson2JsonRedisSerializer}</li>
     * </ul>
     *
     * @param connectionFactory the factory to create Redis connections
     * @return a configured {@link RedisCacheManager}
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * Provides a custom {@link CacheErrorHandler} to handle Redis-related runtime
     * exceptions.
     * This ensures that cache failures (e.g., connection timeout, Redis down) do
     * not crash
     * the application flows. Errors are logged, and the flow proceeds by falling
     * back to the database.
     *
     * @return a custom {@link CacheErrorHandler}
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.error("Redis cache GET error for key {}: {}", key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.error("Redis cache PUT error for key {}: {}", key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error("Redis cache EVICT error for key {}: {}", key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error("Redis cache CLEAR error: {}", exception.getMessage());
            }
        };
    }
}
