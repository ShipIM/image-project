package com.example.imageproject.dto.rest.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "Image")
public class ImageResponse {

    @Schema(description = "ИД файла", format = "uuid", type = "string")
    private String imageId;

    @Schema(description = "Название изображения", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    private String filename;

    @Schema(description = "Размер файла в байтах", format = "int32", type = "integer",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer size;

}
