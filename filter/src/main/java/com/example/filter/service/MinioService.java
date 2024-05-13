package com.example.filter.service;

import com.example.filter.config.minio.MinioProperties;
import com.example.filter.exception.UncheckedMinioException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient client;
    private final MinioProperties properties;

    public void uploadFile(byte[] bytes, String reference) {
        upload(bytes, reference, PutObjectArgs.builder());
    }

    public void uploadTmpFile(byte[] bytes, String reference) {
        var tags = Map.of(properties.getTmpTag(), "");

        upload(bytes, reference, PutObjectArgs.builder().tags(tags));
    }

    private void upload(byte[] bytes, String reference, PutObjectArgs.Builder builder) {
        try {
            var stream = new ByteArrayInputStream(bytes);
            client.putObject(
                    builder
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

    public Boolean objectExists(String id) {
        try {
            client.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(id)
                            .build()
            );
        } catch (Exception e) {
            if (e instanceof ErrorResponseException && ((ErrorResponseException) e)
                    .errorResponse().code().equals("NoSuchKey")) {
                return false;
            }

            throw new UncheckedMinioException(e.getMessage());
        }

        return true;
    }

}
