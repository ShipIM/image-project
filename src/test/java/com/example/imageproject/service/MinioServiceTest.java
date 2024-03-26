package com.example.imageproject.service;

import com.example.imageproject.config.BaseTest;
import com.example.imageproject.config.minio.MinioProperties;
import com.example.imageproject.exception.UncheckedMinioException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class MinioServiceTest extends BaseTest {

    @Autowired
    private MinioService minioService;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioProperties minioProperties;

    private static MultipartFile file = new MockMultipartFile("file", "file.txt",
            "text/plain", "content".getBytes());
    private final String objectName = "object";

    @AfterEach
    private void deleteObject() throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .object(objectName)
                        .bucket(minioProperties.getBucket())
                        .build()
        );
    }

    @Test
    public void uploadFile_FileDoesNotExists() throws Exception {
        minioService.upload(file, objectName);

        Assertions.assertArrayEquals(file.getBytes(),
                minioClient.getObject(
                        GetObjectArgs.builder()
                                .object(objectName)
                                .bucket(minioProperties.getBucket())
                                .build()
                ).readAllBytes());
    }

    @Test
    public void downloadFile_FileExists() {
        minioService.upload(file, objectName);

        var downloadedContent = minioService.download(objectName);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(downloadedContent),
                () -> Assertions.assertArrayEquals(file.getBytes(), downloadedContent)
        );
    }

    @Test
    public void downloadFile_FileDoesNotExists() {
        Assertions.assertThrows(UncheckedMinioException.class,
                () -> minioService.download("nonExistentObject"));
    }

    @Test
    public void deleteFile() {
        minioService.delete(objectName);

        Assertions.assertThrows(ErrorResponseException.class,
                () -> minioClient.getObject(
                        GetObjectArgs.builder()
                                .object(objectName)
                                .bucket(minioProperties.getBucket())
                                .build()
                ));
    }

}
