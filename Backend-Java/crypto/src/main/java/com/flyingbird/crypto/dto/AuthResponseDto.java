package com.flyingbird.crypto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication Response DTO
 *
 * This DTO is returned after successful login.
 * It contains the JWT token that the client should send
 * in the Authorization header for subsequent requests.
 *
 * Fields:
 * - token: JWT token with user claims encoded
 * - username: Username of the authenticated user
 * - role: Role/permission level of the user
 * - message: Success message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    private String token;
    private String username;
    private String role;
    private String message;
}

