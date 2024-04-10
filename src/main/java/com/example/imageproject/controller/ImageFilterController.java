package com.example.imageproject.controller;

import com.example.imageproject.dto.kafka.image.ImageFilter;
import com.example.imageproject.dto.rest.error.UiSuccessContainer;
import com.example.imageproject.dto.rest.image.ApplyImageFiltersResponse;
import com.example.imageproject.dto.rest.image.GetModifiedImageByRequestIdResponse;
import com.example.imageproject.model.enumeration.FilterType;
import com.example.imageproject.service.ImageFilterProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Tag(name = "Image Filters Controller",
        description = "Базовый CRUD API для работы с пользовательскими запросами на редактирование картинок")
@RestController
@Validated
@RequiredArgsConstructor
public class ImageFilterController {

    private final ImageFilterProducer imageFilterProducer;

    @Operation(summary = "Применение указанных фильтров к изображению", operationId = "applyImageFilters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApplyImageFiltersResponse.class))),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @PostMapping(value = "/image/{image-id}/filters/apply", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('FILTER_APPLY_PRIVILEGE')")
    public ApplyImageFiltersResponse applyFilter(
            @Schema(type = "string", format = "uuid")
            @PathVariable("image-id") String image,
            String[] filters) {
        try {
            var resolvedFilters = Arrays.stream(filters).map(FilterType::valueOf).toList();
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Some specified filters do not exist");
        }

        imageFilterProducer.send(new ImageFilter());

        return null;
    }

    @Operation(summary = "Получение ИД измененного файла по ИД запроса", operationId = "getModifiedImageByRequestId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetModifiedImageByRequestIdResponse.class))),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    @GetMapping(value = "/image/{image-id}/filters/{request-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('FILTER_READ_PRIVILEGE')")
    public GetModifiedImageByRequestIdResponse getModifiedImage(
            @Schema(type = "string", format = "uuid")
            @PathVariable("image-id") String image,
            @Schema(type = "string", format = "uuid")
            @PathVariable("request-id") String request) {
        return null;
    }

}
