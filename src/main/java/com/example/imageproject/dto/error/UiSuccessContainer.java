package com.example.imageproject.dto.error;

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

    @Schema(description = "A sign of success", type = "boolean")
    private Boolean success;

    @Schema(description = "Error message", type = "string")
    private String message;

}
