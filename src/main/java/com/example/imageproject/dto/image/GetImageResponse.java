package com.example.imageproject.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetImageResponse {

    @Schema(description = "List of images", type = "array")
    private List<ImageResponse> images;

}
