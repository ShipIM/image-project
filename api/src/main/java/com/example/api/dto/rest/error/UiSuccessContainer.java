package com.example.api.dto.rest.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UiSuccessContainer {

    @Schema(description = "Признак успеха", type = "boolean", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean success;

    @Schema(description = "Сообщение об ошибке", type = "string")
    private String message;

}
