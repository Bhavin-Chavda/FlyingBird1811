package com.flyingbird.crypto.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT Utility Class
 *
 * This class handles all JWT token operations:
 * 1. Token Generation - Create JWT after successful login
 * 2. Token Validation - Verify JWT signature and expiration
 * 3. Claims Extraction - Extract user info from token
 *
 * JWT Structure: HEADER.PAYLOAD.SIGNATURE
 *
 * How it works:
 * 1. User logs in with credentials
 * 2. We verify credentials from DB
 * 3. We create a JWT token with username and roles
 * 4. Client stores this token
 * 5. Client sends token in Authorization header for protected requests
 * 6. We validate the token and extract username/roles
 * 7. Spring Security uses this info to authorize the request
 *
 * @Value annotations inject values from application.yml
 * This avoids hardcoding sensitive values
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${spring.jwt.secret}")
    private String secret;

    @Value("${spring.jwt.expiration}")
    private long expirationTime; // in milliseconds

    /**
     * Generate JWT Token
     *
     * Creates a new JWT token for the authenticated user.
     * The token contains:
     * - username (subject)
     * - roles (authority)
     * - issued at time
     * - expiration time
     *
     * This token is signed with the secret key using HMAC-SHA256
     * making it tamper-proof (server can verify it wasn't modified)
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Extract user roles/authorities and add to token
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        claims.put("roles", roles);

        long now = System.currentTimeMillis();
        long expiryDate = now + expirationTime;

        log.info("Generating JWT token for user: {}", userDetails.getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername()) // Subject = username
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiryDate))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract Username from JWT Token
     *
     * The username is stored as the "subject" claim in JWT.
     * We extract it and use it to identify which user made the request.
     */
    public String extractUsername(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Extract Roles from JWT Token
     *
     * Roles are custom claims we added during token generation.
     * We extract them to determine what user can access.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = getClaimsFromToken(token);
        return (List<String>) claims.get("roles");
    }

    /**
     * Validate JWT Token
     *
     * Verification steps:
     * 1. Check if signature is valid (not tampered with)
     * 2. Check if token is not expired
     * 3. If both pass, token is valid
     *
     * If signature verification fails or token is expired,
     * an exception is thrown and caught by exception handler
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            log.debug("JWT token validated successfully");
            return true;
        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get Claims from JWT Token
     *
     * Internal method to extract all claims (payload data) from token.
     * This is used by other methods to extract specific claims.
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get Signing Key
     *
     * Converts the secret string into a cryptographic key.
     * Used to sign tokens (during generation) and verify them (during validation).
     *
     * We use HMAC-SHA256 which is symmetric:
     * - Same key is used to sign and verify
     * - More secure than basic string comparison
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}

