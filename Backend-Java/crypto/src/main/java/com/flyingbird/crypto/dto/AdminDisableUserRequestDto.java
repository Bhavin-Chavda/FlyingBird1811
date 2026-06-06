package com.flyingbird.crypto.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin "disable user" request DTO — used by {@code POST /api/admin/users/disable} (ADMIN only).
 * Sets the target user's {@code enabled} flag to false so they can no longer log in.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDisableUserRequestDto {

    @NotBlank(message = "Username cannot be blank")
    private String username;
}
