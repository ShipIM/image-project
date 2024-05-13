package com.example.imageapi.service;

import com.example.imageapi.config.minio.MinioProperties;
import com.example.imageapi.exception.UncheckedMinioException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
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
