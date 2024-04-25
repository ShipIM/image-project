package com.example.imageapi.util;

import com.example.imageapi.config.BaseTest;
import com.example.imageapi.model.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;

public class JwtUtilsTest extends BaseTest {

    @Autowired
    private JwtUtils jwtUtils;
    @Value("${jwt.secret.access.key}")
    private String accessKey;

    @Test
    public void generateAccessToken() {
        var username = "username";
        User user = new User();
        user.setUsername(username);

        var token = jwtUtils.generateAccessToken(new HashMap<>(), user);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(token),
                () -> Assertions.assertTrue(jwtUtils.isAccessTokenValid(token)),
                () -> Assertions.assertEquals(username, jwtUtils.extractAccessUsername(token))
        );
    }

    @Test
    public void extractUsername_Success() {
        var username = "username";
        var user = new User();
        user.setUsername(username);
        var token = jwtUtils.generateAccessToken(Collections.emptyMap(), user);

        var extractedUsername = jwtUtils.extractAccessUsername(token);

        Assertions.assertEquals(username, extractedUsername);
    }

    @Test
    public void extractClaim() {
        var claimKey = "customClaim";
        var claimValue = "customValue";
        var claims = Collections.singletonMap(claimKey, (Object) claimValue);
        var token = jwtUtils.generateAccessToken(claims, new User());

        var extractedClaim = jwtUtils.extractClaim(token, accessKey, c -> (String) c.get(claimKey));

        Assertions.assertEquals(claimValue, extractedClaim);
    }

    @Test
    public void extractAllClaims() {
        var claimKey = "customClaim";
        var claimValue = "customValue";
        var claims = Collections.singletonMap(claimKey, (Object) claimValue);
        String token = jwtUtils.generateAccessToken(claims, new User());

        var extractedClaims = jwtUtils.extractAllClaims(token, accessKey);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(extractedClaims),
                () -> Assertions.assertTrue(extractedClaims.containsKey(claimKey)),
                () -> Assertions.assertEquals(claimValue, extractedClaims.get(claimKey, String.class))
        );
    }

    @Test
    public void isAccessTokenValid_ValidToken() {
        var token = jwtUtils.generateAccessToken(new HashMap<>(), new User());

        Assertions.assertTrue(jwtUtils.isAccessTokenValid(token));
    }

    @Test
    public void testIsAccessTokenValid_ExpiredToken() {
        var expiration = ReflectionTestUtils.getField(jwtUtils, "accessExpiration");
        ReflectionTestUtils.setField(jwtUtils, "accessExpiration", -1L);
        var token = jwtUtils.generateAccessToken(new HashMap<>(), new User());
        ReflectionTestUtils.setField(jwtUtils, "accessExpiration", expiration);

        Assertions.assertFalse(jwtUtils.isAccessTokenValid(token));
    }

    @Test
    public void isAccessTokenValid_MalformedToken() {
        String malformedToken = "malformed";

        Assertions.assertFalse(jwtUtils.isAccessTokenValid(malformedToken));
    }

}
