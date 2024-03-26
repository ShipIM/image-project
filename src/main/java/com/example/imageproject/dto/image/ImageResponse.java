package com.example.imageproject.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageResponse {

    @Schema(description = "File ID", format = "uuid", type = "string")
    private String imageId;

    @Schema(description = "Image name", type = "string")
    private String filename;

    @Schema(description = "File size in bytes", format = "int32", type = "integer")
    private Integer size;

}
