package com.flyingbird.crypto.config;

import com.flyingbird.crypto.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Custom Authentication Entry Point
 *
 * This component handles authentication failures when:
 * 1. No JWT token is provided
 * 2. Invalid/expired JWT token is provided
 * 3. Access to protected endpoint without authentication
 *
 * Purpose:
 * Instead of Spring Security's default error response,
 * we return a structured JSON error response matching our ErrorResponse format.
 *
 * When triggered:
 * - User tries to access protected endpoint without token → 401 Unauthorized
 * - User provides invalid token → 401 Unauthorized
 * - User provides expired token → 401 Unauthorized
 *
 * Response Format:
 * {
 *   "statusCode": 401,
 *   "error": "UNAUTHORIZED",
 *   "message": "Authentication failed. JWT token is missing or invalid.",
 *   "timestamp": "2026-04-18 14:23:22",
 *   "errorCode": "AUTHENTICATION_FAILED"
 * }
 */
@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        log.warn("Authentication failed: {} | Request URI: {}",
                authException.getMessage(), request.getRequestURI());

        // Build error response using ErrorResponse DTO
        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Authentication failed. JWT token is missing or invalid.")
                .timestamp(LocalDateTime.now())
                .errorCode("AUTHENTICATION_FAILED")
                .build();

        // Set response type to JSON
        response.setContentType("application/json;charset=UTF-8");

        // Set HTTP status code to 401 Unauthorized
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // Convert ErrorResponse to JSON and write to response
        // ObjectMapper created locally with JSR310 module for LocalDateTime support
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}
