package com.flyingbird.crypto.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Global Error Response DTO
 * 
 * This class is used by the Global Exception Handler to provide
 * consistent error responses to the client.
 * 
 * Fields:
 * - statusCode: HTTP status code (e.g., 400, 401, 403, 500)
 * - error: Error type/name (e.g., "BAD_REQUEST", "UNAUTHORIZED")
 * - message: Human-readable error message
 * - timestamp: When the error occurred (automatically formatted)
 * - errorCode: Application-specific error code for debugging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int statusCode;
    private String error;
    private String message;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String errorCode;
}

