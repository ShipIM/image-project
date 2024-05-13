package com.example.imageapi.service;

import com.example.imageapi.config.BaseTest;
import com.example.imageapi.dto.rest.user.AuthenticateUserRequest;
import com.example.imageapi.model.entity.User;
import com.example.imageapi.util.JwtUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthServiceTest extends BaseTest {

    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private DetailsService detailsService;
    @MockBean
    private JwtUtils jwtUtils;
    @Autowired
    private AuthService authService;

    @Test
    public void registerUser() {
        var username = "username";
        var request = new AuthenticateUserRequest();
        request.setUsername(username);
        request.setPassword("password");
        var user = new User();
        user.setUsername(username);

        Mockito.when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        Mockito.when(detailsService.createUser(Mockito.any())).thenReturn(user);
        Mockito.when(jwtUtils.generateAccessToken(Mockito.anyMap(), Mockito.any())).thenReturn("accessToken");

        var response = authService.register(request);

        Assertions.assertAll(
                () -> Assertions.assertEquals(username, response.getUsername()),
                () -> Assertions.assertEquals("accessToken", response.getAccess())
        );
    }

    @Test
    public void authenticateUser() {
        var username = "username";
        var request = new AuthenticateUserRequest();
        request.setUsername(username);
        request.setPassword("password");
        var user = new User();
        user.setUsername(username);
        var authentication = new UsernamePasswordAuthenticationToken(user, null);

        Mockito.when(authenticationManager.authenticate(Mockito.any())).thenReturn(authentication);
        Mockito.when(jwtUtils.generateAccessToken(Mockito.anyMap(), Mockito.any())).thenReturn("accessToken");

        var response = authService.authenticate(request);

        Assertions.assertAll(
                () -> Assertions.assertEquals("username", response.getUsername()),
                () -> Assertions.assertEquals("accessToken", response.getAccess())
        );
    }

}
