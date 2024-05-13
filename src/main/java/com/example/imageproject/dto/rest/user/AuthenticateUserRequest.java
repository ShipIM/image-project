package com.example.imageproject.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticateUserRequest {

    @Schema(description = "username", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 16, message = "incorrect username size")
    @NotBlank(message = "username must not be empty")
    private String username;

    @Schema(description = "password", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 8, max = 16, message = "incorrect password size")
    @NotBlank(message = "password must not be empty")
    private String password;

}
