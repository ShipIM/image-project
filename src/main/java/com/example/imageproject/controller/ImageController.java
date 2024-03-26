package com.example.imageproject.controller;

import com.example.imageproject.dto.error.UiSuccessContainer;
import com.example.imageproject.dto.image.GetImageResponse;
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
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Tag(name = "Image Controller", description = "Basic CRUD API for working with images")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Validated
public class ImageController {

    private final ImageService imageService;

    @Value("${file.max-size}")
    private Long fileSize;
    @Value("${file.allowed-format}")
    private String[] formats;

    @Operation(summary = "Uploading a new image to the system", operationId = "uploadImage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success of the operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ImageResponse.class))),
            @ApiResponse(responseCode = "400", description = "The file was not validated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @PostMapping("/image")
    @PreAuthorize("hasAuthority('IMAGE_UPLOAD_PRIVILEGE')")
    public UploadImageResponse uploadImage(
            @Schema(type = "string", format = "binary")
            @RequestBody
            @NotNull(message = "File must not be empty")
            MultipartFile file) {
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

    @Operation(summary = "Downloading a file by ID", operationId = "downloadImage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success of the operation",
                    content = @Content(mediaType = "*/*", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "The file was not found in the system or is unavailable",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @GetMapping(value = "/image/{image-id}")
    @PreAuthorize("hasAuthority('IMAGE_DOWNLOAD_PRIVILEGE')")
    public ResponseEntity<Resource> downloadImage(
            @Schema(type = "string", format = "uuid")
            @PathVariable(value = "image-id")
            String imageId) {
        return imageService.download(imageId);
    }

    @Operation(summary = "Deleting a file by ID", operationId = "deleteImage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success of the operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "404", description = "The file was not found in the system or is unavailable",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @DeleteMapping("/image/{image-id}")
    @PreAuthorize("hasAuthority('IMAGE_DELETE_PRIVILEGE')")
    public UiSuccessContainer deleteImage(
            @Schema(type = "string", format = "uuid")
            @PathVariable(value = "image-id")
            String imageId) {
        return imageService.delete(imageId);
    }

    @Operation(summary = "Getting a list of images that are available to the user", operationId = "getImages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success of the operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetImageResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @GetMapping("/images")
    @PreAuthorize("hasAuthority('IMAGE_READ_PRIVILEGE')")
    public List<ImageResponse> getImages() {
        return imageService.getAllMeta();
    }

}
