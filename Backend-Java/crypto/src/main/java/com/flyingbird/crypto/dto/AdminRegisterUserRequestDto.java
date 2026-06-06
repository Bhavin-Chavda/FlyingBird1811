package com.flyingbird.crypto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin "create user" request DTO — used ONLY by the protected
 * {@code POST /api/admin/users/register} endpoint (ADMIN only).
 *
 * <p>Unlike the public {@link RegisterRequestDto}, {@code role} is REQUIRED and must be one
 * of {@code USER} / {@code ADMIN} (case-insensitive; normalized to upper-case in the service).
 * Kept as a separate DTO so the public registration contract/validation is untouched.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRegisterUserRequestDto {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @NotBlank(message = "Role cannot be blank")
    @Pattern(regexp = "(?i)(USER|ADMIN)", message = "Role must be USER or ADMIN")
    private String role;
}
