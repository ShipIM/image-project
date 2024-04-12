package com.example.imageproject.dto.rest.image;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetImagesResponse {

    @ArraySchema(schema = @Schema(implementation = ImageResponse.class, requiredMode = Schema.RequiredMode.REQUIRED),
            arraySchema = @Schema(description = "Список изображений"))
    private List<ImageResponse> images;

}
