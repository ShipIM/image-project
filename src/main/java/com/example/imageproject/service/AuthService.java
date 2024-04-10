package com.example.imageproject.service;

import com.example.imageproject.dto.rest.mapper.UserMapper;
import com.example.imageproject.dto.rest.user.AuthenticateUserRequest;
import com.example.imageproject.dto.rest.user.AuthenticateUserResponse;
import com.example.imageproject.model.entity.User;
import com.example.imageproject.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final DetailsService detailsService;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    public AuthenticateUserResponse register(AuthenticateUserRequest request) {
        var user = userMapper.mapToUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user = detailsService.createUser(user);

        return userMapper.mapToResponse(user, generateAccessToken(user));
    }

    public AuthenticateUserResponse authenticate(AuthenticateUserRequest request) {
        var user = userMapper.mapToUser(request);
        user = (User) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        ).getPrincipal();

        return userMapper.mapToResponse(user, generateAccessToken(user));
    }

    private String generateAccessToken(User user) {
        return generateAccessToken(new HashMap<>(), user);
    }

    private String generateAccessToken(Map<String, Object> extraClaims, User user) {
        return jwtUtils.generateAccessToken(extraClaims, user);
    }

}
