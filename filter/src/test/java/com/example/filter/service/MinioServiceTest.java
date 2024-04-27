package com.example.filter.service;

import com.example.filter.config.BaseTest;
import com.example.filter.config.minio.MinioProperties;
import com.example.filter.exception.UncheckedMinioException;
import io.minio.GetObjectArgs;
import io.minio.GetObjectTagsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MinioServiceTest extends BaseTest {

    @Autowired
    private MinioService minioService;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioProperties minioProperties;

    private final String objectName = "object";
    private final byte[] file = objectName.getBytes();

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
        minioService.uploadFile(file, objectName);

        Assertions.assertArrayEquals(file,
                minioClient.getObject(
                        GetObjectArgs.builder()
                                .object(objectName)
                                .bucket(minioProperties.getBucket())
                                .build()
                ).readAllBytes());
    }

    @Test
    public void uploadTmpFile_FileDoesNotExists() {
        minioService.uploadTmpFile(file, objectName);

        Assertions.assertAll(
                () -> Assertions.assertArrayEquals(file,
                        minioClient.getObject(
                                GetObjectArgs.builder()
                                        .object(objectName)
                                        .bucket(minioProperties.getBucket())
                                        .build()
                        ).readAllBytes()),
                () -> Assertions.assertTrue(
                        minioClient.getObjectTags(
                                GetObjectTagsArgs.builder()
                                        .bucket(minioProperties.getBucket())
                                        .object(objectName)
                                        .build()
                        ).get().containsKey(minioProperties.getTmpTag()))
        );

    }

    @Test
    public void downloadFile_FileExists() {
        minioService.uploadFile(file, objectName);

        var downloadedContent = minioService.download(objectName);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(downloadedContent),
                () -> Assertions.assertArrayEquals(file, downloadedContent)
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
