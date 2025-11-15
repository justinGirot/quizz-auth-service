package com.quizz.authservice.security;

import com.quizz.authservice.config.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for generating and validating JWT tokens.
 * Uses JwtProperties for configuration management.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * Get the signing key for JWT token operations.
     * @return SecretKey for signing tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token from Authentication object.
     * @param authentication Spring Security authentication
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        String roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userPrincipal.getEmail())
                .claim("userId", userPrincipal.getId())
                .claim("roles", roles)
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate JWT token from user details.
     * @param email User email
     * @param userId User ID
     * @param roles User roles (comma-separated)
     * @return JWT token string
     */
    public String generateTokenFromEmail(String email, Long userId, String roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("roles", roles)
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract email (subject) from JWT token.
     * @param token JWT token
     * @return User email
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    /**
     * Extract user ID from JWT token.
     * @param token JWT token
     * @return User ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Get all claims from JWT token.
     * @param token JWT token
     * @return Claims object
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validate JWT token.
     * @param authToken JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
}
