package com.example.filter.config.integration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration")
public class IntegrationProperties {

    private String targetServiceName;

    private Integer retryMaxAttempts;

    private Long retryWaitDuration;

    private Integer breakerFailureRateThreshold;

    private Integer breakerSlidingWindowSize;

    private Long breakerWaitDurationInOpenState;

    private Integer rateLimiterBucketCapacity;

    private Long rateLimiterRefillPeriod;

}
