package com.example.imageproject.dto.image;

import com.example.imageproject.model.enumeration.ImageStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetModifiedImageByRequestIdResponse {

    @Schema(description = "ИД модифицированного или оригинального файла в случае отсутствия первого",
            type = "string", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imageId;

    @Schema(description = "Статус обработки файла", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    private ImageStatus status;

}
