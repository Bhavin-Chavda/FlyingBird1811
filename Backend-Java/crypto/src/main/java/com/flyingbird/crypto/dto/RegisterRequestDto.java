package com.flyingbird.crypto.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Register Request DTO
 * 
 * This DTO is used to receive registration details from the client.
 * 
 * Validation:
 * - @NotBlank ensures the field is not null, empty, or whitespace
 * - Username must be unique (checked in service layer)
 * - Password will be encrypted before storing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    
    @NotBlank(message = "Username cannot be blank")
    private String username;
    
    @NotBlank(message = "Password cannot be blank")
    private String password;
    
    // Default role for new users
    private String role;
}

