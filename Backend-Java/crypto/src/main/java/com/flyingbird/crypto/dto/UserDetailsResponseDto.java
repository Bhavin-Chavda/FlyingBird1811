package com.flyingbird.crypto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Details Response DTO
 *
 * This DTO is used to return user details to the client.
 * It contains essential user information without sensitive data like password.
 *
 * Fields:
 * - id: Unique identifier for the user
 * - username: User's login username
 * - role: User's role (e.g., USER, ADMIN)
 * - enabled: Whether the user account is active
 *
 * Security Note:
 * - Password is never included in response
 * - Only non-sensitive user information is exposed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponseDto {

    private Long id;
    private String username;
    private String role;
    private boolean enabled;
}

