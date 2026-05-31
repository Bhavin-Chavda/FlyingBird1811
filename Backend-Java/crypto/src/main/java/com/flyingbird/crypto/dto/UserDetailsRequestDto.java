package com.flyingbird.crypto.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Details Request DTO
 *
 * This DTO is used to receive username from the client to fetch user details.
 * Username is required and cannot be blank.
 *
 * Validation:
 * - @NotBlank ensures the field is not null, empty, or whitespace
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsRequestDto {

    @NotBlank(message = "Username cannot be blank")
    private String username;
}

