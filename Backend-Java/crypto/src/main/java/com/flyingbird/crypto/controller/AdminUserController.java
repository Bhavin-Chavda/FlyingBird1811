package com.flyingbird.crypto.controller;

import com.flyingbird.crypto.dto.AdminDisableUserRequestDto;
import com.flyingbird.crypto.dto.AdminRegisterUserRequestDto;
import com.flyingbird.crypto.dto.AuthResponseDto;
import com.flyingbird.crypto.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin user-management endpoints. ADMIN-only and JWT-protected.
 *
 * <p>{@code POST /api/admin/users/register} lets an authenticated ADMIN create a new user
 * with an explicit role. This is intentionally separate from the public
 * {@code POST /api/auth/register} — that public endpoint is unchanged; this one is locked to
 * ADMINs both by the security filter chain ({@code /api/admin/**} → {@code authenticated()}
 * → 401 without a token) and by {@code @PreAuthorize("hasRole('ADMIN')")} (→ 403 for non-admins).</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AuthService authService;

    /**
     * Create a new user (ADMIN only).
     *
     * <p>Success (201): {@code { username, role, message }} (no token issued).
     * Errors: 400 validation (blank fields / role not USER|ADMIN), 401 missing/invalid JWT,
     * 403 authenticated non-admin, 409 username already exists.</p>
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Admin: Register User",
            description = "Create a new user account with an explicit role (USER/ADMIN). "
                    + "Requires a valid JWT belonging to an ADMIN user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body (blank field or invalid role)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not an ADMIN"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    public ResponseEntity<AuthResponseDto> registerUser(
            @Valid @RequestBody AdminRegisterUserRequestDto request,
            Authentication authentication) {

        log.info("Admin '{}' creating user | username={} | role={}",
                authentication.getName(), request.getUsername(), request.getRole());

        AuthResponseDto response = authService.registerByAdmin(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Disable a user (ADMIN only).
     *
     * <p>Sets the target user's {@code enabled} flag to false so they can no longer log in.
     * Success (200): {@code { username, role, message }}. Errors: 400 blank username,
     * 401 missing/invalid JWT, 403 authenticated non-admin OR target has ADMIN role
     * (ADMIN accounts cannot be disabled), 404 username not found.</p>
     */
    @PostMapping("/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Admin: Disable User",
            description = "Disable a non-ADMIN user account (sets enabled=false). "
                    + "Requires a valid JWT belonging to an ADMIN user. ADMIN users cannot be disabled."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User disabled successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body (blank username)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN, or target user has the ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Username not found")
    })
    public ResponseEntity<AuthResponseDto> disableUser(
            @Valid @RequestBody AdminDisableUserRequestDto request,
            Authentication authentication) {

        log.info("Admin '{}' disabling user | username={}",
                authentication.getName(), request.getUsername());

        AuthResponseDto response = authService.disableUser(request.getUsername());

        return ResponseEntity.ok(response);
    }
}
