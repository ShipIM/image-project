package com.example.imageapi.config.integration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.function.Supplier;

@Profile(value = "!test")
@Configuration
@EnableConfigurationProperties(IntegrationProperties.class)
@RequiredArgsConstructor
public class IntegrationConfiguration {

    private final IntegrationProperties properties;

    @Bean
    public Supplier<BucketConfiguration> bucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(properties.getRateLimiterBucketCapacity())
                        .refillIntervally(properties.getRateLimiterBucketCapacity(),
                                Duration.ofMillis(properties.getRateLimiterRefillPeriod())).build())
                .build();
    }

}
