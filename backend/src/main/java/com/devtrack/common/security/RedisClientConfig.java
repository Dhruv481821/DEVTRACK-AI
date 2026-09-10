package com.devtrack.common.security;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a native Lettuce RedisClient bean for components that require it
 * directly, such as Bucket4j's LettuceBasedProxyManager.
 *
 * Reuses Spring Boot's existing Redis configuration:
 * - Production: spring.data.redis.url
 * - Development: spring.data.redis.host / port
 */
@Configuration
public class RedisClientConfig {

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient(RedisProperties properties) {

        RedisURI uri;

        if (properties.getUrl() != null && !properties.getUrl().isBlank()) {

            // Production: use the configured Redis URL
            uri = RedisURI.create(properties.getUrl());

        } else {

            // Development: use configured host and port
            RedisURI.Builder builder = RedisURI.builder()
                    .withHost(properties.getHost())
                    .withPort(properties.getPort());

            if (properties.getPassword() != null) {
                builder.withPassword(
                        properties.getPassword().toCharArray()
                );
            }

            uri = builder.build();
        }

        return RedisClient.create(uri);
    }
}