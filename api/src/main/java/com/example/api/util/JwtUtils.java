package com.example.api.util;

import com.example.api.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret.access.key}")
    private String access;
    @Value("${jwt.secret.access.expiration}")
    private Long accessExpiration;

    public String generateAccessToken(Map<String, Object> extraClaims, User user) {
        var expirationDate = calculateDate(accessExpiration);

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setExpiration(expirationDate)
                .signWith(getSignInKey(access), SignatureAlgorithm.HS256)
                .compact();
    }

    private Date calculateDate(Long expiration) {
        var now = LocalDateTime.now();
        var expirationInstant = now.plusSeconds(expiration)
                .atZone(ZoneId.systemDefault()).toInstant();

        return Date.from(expirationInstant);
    }

    public Key getSignInKey(String key) {
        var keyBytes = Decoders.BASE64.decode(key);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractAccessUsername(String token) {
        return extractUsername(token, access);
    }

    private String extractUsername(String token, String key) {
        return extractClaim(token, key, Claims::getSubject);
    }

    public <T> T extractClaim(String token, String key, Function<Claims, T> claimsResolver) {
        var claims = extractAllClaims(token, key);

        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token, String key) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey(key))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token, access);
    }

    private boolean isTokenValid(String token, String key) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey(key))
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (ExpiredJwtException expEx) {
            log.error("token expired", expEx);
        } catch (UnsupportedJwtException unsEx) {
            log.error("unsupported jwt", unsEx);
        } catch (MalformedJwtException mjEx) {
            log.error("malformed jwt", mjEx);
        } catch (Exception e) {
            log.error("invalid token", e);
        }

        return false;
    }

}
