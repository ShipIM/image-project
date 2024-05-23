package com.example.filter.config.integration;

import com.example.filter.exception.RetryableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Profile(value = {"recognition", "test"})
@Configuration
@EnableConfigurationProperties(IntegrationProperties.class)
@RequiredArgsConstructor
public class IntegrationConfiguration {

    private final IntegrationProperties properties;

    @Bean
    public Retry retry() {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(properties.getRetryMaxAttempts())
                .waitDuration(Duration.ofMillis(properties.getRetryWaitDuration()))
                .retryExceptions(RetryableException.class)
                .build();

        return Retry.of(properties.getTargetServiceName(), retryConfig);
    }

    @Bean
    public CircuitBreaker circuitBreaker() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(properties.getBreakerFailureRateThreshold())
                .slidingWindowSize(properties.getBreakerSlidingWindowSize())
                .waitDurationInOpenState(Duration.ofMillis(properties.getBreakerWaitDurationInOpenState()))
                .build();

        return CircuitBreaker.of(properties.getTargetServiceName(), circuitBreakerConfig);
    }

}
