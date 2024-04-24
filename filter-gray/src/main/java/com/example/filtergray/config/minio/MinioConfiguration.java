package com.example.filtergray.config.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketLifecycleArgs;
import io.minio.messages.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfiguration {

    @Bean
    public MinioClient minioClient(MinioProperties properties) throws Exception {
        var client = MinioClient.builder()
                .credentials(
                        properties.getAccessKey(),
                        properties.getSecretKey())
                .endpoint(properties.getUrl())
                .build();

        if (!client.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(properties.getBucket())
                        .build())) {

            client.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(properties.getBucket())
                            .build()
            );
        }

        var rule = new LifecycleRule(
                Status.ENABLED,
                null,
                new Expiration((ResponseDate) null, properties.getTtl(), null),
                new RuleFilter(null, null, new Tag(properties.getTmpTag(), "")),
                "TempFiles",
                null,
                null,
                null
        );
        var config = new LifecycleConfiguration(List.of(rule));
        client.setBucketLifecycle(
                SetBucketLifecycleArgs.builder()
                        .bucket(properties.getBucket())
                        .config(config)
                        .build()
        );

        return client;
    }

}
