package com.flyingbird.crypto.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Request DTO
 * 
 * This DTO is used to receive login credentials from the client.
 * Both username and password are required and cannot be blank.
 * 
 * Validation:
 * - @NotBlank ensures the field is not null, empty, or whitespace
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    
    @NotBlank(message = "Username cannot be blank")
    private String username;
    
    @NotBlank(message = "Password cannot be blank")
    private String password;
}

