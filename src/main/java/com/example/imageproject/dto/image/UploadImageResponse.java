package com.example.imageproject.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadImageResponse {

    @Schema(description = "File ID", format = "uuid", type = "string")
    private String imageId;

}
