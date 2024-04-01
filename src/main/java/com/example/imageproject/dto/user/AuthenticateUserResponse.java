package com.example.imageproject.dto.user;

import com.example.imageproject.model.enumeration.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticateUserResponse {

    @Schema(description = "username", type = "string")
    private String username;

    @Schema(description = "User role", type = "string")
    private RoleEnum role;

    @Schema(description = "jwt access token", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    private String access;

}
