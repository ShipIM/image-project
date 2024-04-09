package com.example.imageproject.controller;

import com.example.imageproject.dto.error.UiSuccessContainer;
import com.example.imageproject.dto.image.GetImagesResponse;
import com.example.imageproject.dto.image.ImageResponse;
import com.example.imageproject.dto.image.UploadImageResponse;
import com.example.imageproject.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Tag(name = "Image Controller", description = "Базовый CRUD API для работы с картинками")
@RestController
@Validated
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @Value("${file.max-size}")
    private Long fileSize;
    @Value("${file.allowed-format}")
    private String[] formats;

    @Operation(summary = "Загрузка нового изображения в систему", operationId = "uploadImage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UploadImageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Файл не прошел валидацию",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @PostMapping(value = "/image", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('IMAGE_UPLOAD_PRIVILEGE')")
    public UploadImageResponse uploadImage(
            @Schema(type = "string", format = "binary")
            @RequestPart("file") MultipartFile file) {
        var fileName = file.getOriginalFilename();

        if (file.getSize() > fileSize) {
            throw new ValidationException(String.format("The allowed file size (%d bytes) has been exceeded",
                    fileSize));
        } else if (Objects.isNull(fileName) || Arrays.stream(formats)
                .noneMatch(format -> Objects.equals(format, fileName.substring(fileName.indexOf(".") + 1)))) {
            throw new ValidationException(String.format("The file extension is not included in the allowed list: %s",
                    String.join(", ", formats)));
        }

        return imageService.saveImage(file);
    }

    @Operation(summary = "Скачивание файла по ИД", operationId = "downloadImage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = @Content(mediaType = "*/*", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @GetMapping(value = "/image/{image-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('IMAGE_DOWNLOAD_PRIVILEGE')")
    public ResponseEntity<Resource> downloadImage(
            @Schema(type = "string", format = "uuid")
            @PathVariable(value = "image-id")
            String imageId) {
        return imageService.download(imageId);
    }

    @Operation(summary = "Удаление файла по ИД", operationId = "deleteImage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @DeleteMapping(value = "/image/{image-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('IMAGE_DELETE_PRIVILEGE')")
    public UiSuccessContainer deleteImage(
            @Schema(type = "string", format = "uuid")
            @PathVariable(value = "image-id")
            String imageId) {
        return imageService.delete(imageId);
    }

    @Operation(summary = "Получение списка изображений, которые доступны пользователю", operationId = "getImages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetImagesResponse.class))),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @GetMapping(value = "/images", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('IMAGE_READ_PRIVILEGE')")
    public List<ImageResponse> getImages() {
        return imageService.getAllMeta();
    }

}
