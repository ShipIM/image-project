package com.example.imageproject.controller;

import com.example.imageproject.dto.error.UiSuccessContainer;
import com.example.imageproject.dto.user.AuthenticateUserRequest;
import com.example.imageproject.dto.user.AuthenticateUserResponse;
import com.example.imageproject.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth controller", description = "Basic API for working registration and authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registration of a new user in the system", operationId = "registerUser")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success of the operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticateUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "The parameters were not validated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    public AuthenticateUserResponse registerUser(
            @RequestBody @Valid
            AuthenticateUserRequest request) {
        return authService.register(request);
    }

    @PostMapping("/authentication")
    @Operation(summary = "Authentication of an existing user in the system", operationId = "authenticateUser")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success of the operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticateUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "The parameters were not validated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "404", description = "A user with that username does not exist",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UiSuccessContainer.class)))
    })
    public AuthenticateUserResponse authenticateUser(
            @RequestBody @Valid
            AuthenticateUserRequest request) {
        return authService.authenticate(request);
    }

}
