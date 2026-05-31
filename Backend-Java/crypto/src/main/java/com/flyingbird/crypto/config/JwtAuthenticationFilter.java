package com.flyingbird.crypto.config;

import com.flyingbird.crypto.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter
 *
 * This filter runs ONCE per HTTP request (before reaching controller).
 * It intercepts every request and:
 * 1. Extracts JWT token from Authorization header
 * 2. Validates the token
 * 3. Extracts username and roles from token
 * 4. Sets user details in Spring Security Context
 * 5. Allows the request to proceed
 *
 * Spring Security Context stores the authenticated user.
 * Later, when we use @PreAuthorize or hasRole(), Spring looks at this context.
 *
 * This filter runs for ALL requests. Swagger and public endpoints
 * are protected separately in SecurityConfig using permitAll().
 *
 * Request Flow:
 * Client → JwtAuthenticationFilter (extract & validate token) →
 * SecurityContext (store user) → Controller → Response
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;


    /**
     * Filter all requests
     *
     * This method is called for EVERY HTTP request.
     * OncePerRequestFilter ensures it runs exactly once per request
     * (even if there are other dispatchers).
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Extract token from Authorization header
            String token = extractTokenFromRequest(request);

            // If token exists and is valid, authenticate the user
            if (token != null && jwtUtil.validateToken(token)) {
                authenticateUser(token);
                log.debug("User authenticated successfully from JWT token");
            }

        } catch (Exception e) {
            log.error("Authentication filter error: {}", e.getMessage());
            // Continue with the request even if token extraction fails
            // Protected endpoints will be caught by Spring Security
        }

        // Continue with the filter chain
        // Next filter or controller will handle the request
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT Token from Authorization Header
     *
     * Token format: "Bearer eyJhbGciOiJIUzI1NiJ9..."
     *
     * Steps:
     * 1. Get Authorization header
     * 2. Check if it starts with "Bearer "
     * 3. Extract the token part after "Bearer "
     *
     * Returns: Token string or null if not found
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }

        return null;
    }

    /**
     * Authenticate User in Spring Security Context
     *
     * This method does the following:
     * 1. Extract username from JWT token
     * 2. Extract roles from JWT token
     * 3. Convert roles to Spring authorities (ROLE_ prefix added by Spring)
     * 4. Create authentication token with username and authorities
     * 5. Store in SecurityContextHolder
     *
     * Once stored, Spring Security knows the user is authenticated.
     * It will use this info for:
     * - Authorization checks (@PreAuthorize)
     * - Role-based access (@RolesAllowed)
     * - hasRole() expressions
     *
     * Note: We don't need a password because token is already validated above
     */
    private void authenticateUser(String token) {
        // Get username from token
        String username = jwtUtil.extractUsername(token);

        // Get roles from token
        List<String> roles = jwtUtil.extractRoles(token);

        // Convert string roles to Spring authorities
        // Spring expects authorities in format: ROLE_ADMIN, ROLE_USER, etc.
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        // Create authentication token
        // First parameter: username (principal)
        // Second parameter: password (null - we don't use it in token auth)
        // Third parameter: authorities (permissions/roles)
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );

        // Store in SecurityContextHolder
        // This makes user available throughout the request
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        log.info("User authenticated | username={} | roles={}", username, roles);
    }
}

