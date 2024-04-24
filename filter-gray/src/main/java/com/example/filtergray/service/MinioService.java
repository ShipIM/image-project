package com.example.filtergray.service;

import com.example.filtergray.config.minio.MinioProperties;
import com.example.filtergray.exception.UncheckedMinioException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient client;
    private final MinioProperties properties;

    public void upload(byte[] bytes, String reference) {
        try {
            var stream = new ByteArrayInputStream(bytes);
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(reference)
                            .stream(stream, bytes.length, properties.getImageSize())
                            .build()
            );
        } catch (Exception e) {
            throw new UncheckedMinioException(e.getMessage());
        }
    }

    public byte[] download(String imageId) {
        try (var stream = client.getObject(
                GetObjectArgs.builder()
                        .bucket(properties.getBucket())
                        .object(imageId)
                        .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new UncheckedMinioException(e.getMessage());
        }
    }

}
