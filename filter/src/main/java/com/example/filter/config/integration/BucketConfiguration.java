package com.example.filter.config.integration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.function.Supplier;

@Profile(value = "recognition")
@Configuration
@EnableConfigurationProperties(IntegrationProperties.class)
@RequiredArgsConstructor
public class BucketConfiguration {

    private final IntegrationProperties properties;

    private final ProxyManager<String> proxyManager;

    @Bean
    public Bucket bucket() {
        return proxyManager.builder().build(properties.getTargetServiceName(), bucketConfiguration());
    }

    private Supplier<io.github.bucket4j.BucketConfiguration> bucketConfiguration() {
        return () -> io.github.bucket4j.BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(properties.getRateLimiterBucketCapacity())
                        .refillIntervally(properties.getRateLimiterBucketCapacity(),
                                Duration.ofMillis(properties.getRateLimiterRefillPeriod())).build())
                .build();
    }

}
