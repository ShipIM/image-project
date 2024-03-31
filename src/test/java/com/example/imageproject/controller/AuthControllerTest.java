package com.example.imageproject.controller;

import com.example.imageproject.config.BaseTest;
import com.example.imageproject.dto.user.AuthenticateUserRequest;
import com.example.imageproject.dto.user.AuthenticateUserResponse;
import com.example.imageproject.exception.EntityNotFoundException;
import com.example.imageproject.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class AuthControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;

    @Test
    public void registerUser_Success() throws Exception {
        var request = new AuthenticateUserRequest();
        request.setUsername("username");
        request.setPassword("password");

        var response = new AuthenticateUserResponse();
        response.setAccess("access");

        Mockito.when(authService.register(Mockito.any())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.access", Matchers.is("access")));
    }

    @Test
    public void authenticateUser_Success() throws Exception {
        var request = new AuthenticateUserRequest();
        request.setUsername("username");
        request.setPassword("password");

        var response = new AuthenticateUserResponse();
        response.setAccess("access");

        Mockito.when(authService.authenticate(Mockito.any())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.access", Matchers.is("access")));
    }

    @Test
    public void registerUser_UsernameTaken() throws Exception {
        var request = new AuthenticateUserRequest();
        request.setUsername("username");
        request.setPassword("password");

        Mockito.when(authService.register(Mockito.any())).thenThrow(new DataIntegrityViolationException(""));

        mockMvc.perform(MockMvcRequestBuilders.post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void authenticateUser_UserNotFound() throws Exception {
        var request = new AuthenticateUserRequest();
        request.setUsername("username");
        request.setPassword("password");

        Mockito.when(authService.authenticate(Mockito.any())).thenThrow(new EntityNotFoundException(""));

        mockMvc.perform(MockMvcRequestBuilders.post("/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void registerUser_ValidationFailure() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void authenticateUser_ValidationFailure() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}
