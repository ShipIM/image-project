package com.example.imageproject.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadImageResponse {

    @Schema(description = "ИД файла", format = "uuid", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imageId;

}
