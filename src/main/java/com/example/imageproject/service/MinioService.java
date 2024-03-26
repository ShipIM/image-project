package com.example.imageproject.service;

import com.example.imageproject.config.minio.MinioProperties;
import com.example.imageproject.exception.UncheckedMinioException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient client;
    private final MinioProperties properties;

    public void upload(MultipartFile file, String reference) {
        try {
            var stream = new ByteArrayInputStream(file.getBytes());
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(reference)
                            .stream(stream, file.getSize(), properties.getImageSize())
                            .contentType(file.getContentType())
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

    public void delete(String imageId) {
        try {
            client.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(imageId)
                            .build()
            );
        } catch (Exception e) {
            throw new UncheckedMinioException(e.getMessage());
        }
    }

}
