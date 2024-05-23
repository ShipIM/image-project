package com.example.imageapi.config.integration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration")
public class IntegrationProperties {

    private Integer rateLimiterBucketCapacity;

    private Long rateLimiterRefillPeriod;

}
