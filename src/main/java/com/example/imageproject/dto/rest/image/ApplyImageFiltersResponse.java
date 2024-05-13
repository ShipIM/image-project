package com.example.imageproject.dto.rest.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApplyImageFiltersResponse {

    @Schema(description = "ИД запроса в системе", type = "string", format = "uuid",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String requestId;

}
