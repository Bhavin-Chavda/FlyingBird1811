package com.flyingbird.crypto.controller;

import com.flyingbird.crypto.entity.User;
import com.flyingbird.crypto.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Admin Utility Controller
 *
 * This controller provides utility endpoints for testing and administration.
 * These endpoints should be removed or secured in production.
 *
 * Note: This is for development/testing only!
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminUtilityController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * Update User Role
     *
     * Temporary endpoint to update user role for testing.
     *
     * Usage: POST /admin/update-role?username=admin_user&role=ADMIN
     */
    @PostMapping("/admin/update-role")
    public Map<String, String> updateUserRole(@RequestParam String username, @RequestParam String role) {
        log.info("Updating user role | username={} | newRole={}", username, role);

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return Map.of("error", "User not found");
        }

        User user = userOpt.get();
        user.setRole(role);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User role updated");
        response.put("username", username);
        response.put("role", role);
        return response;
    }

    /**
     * Protected Test Endpoint
     *
     * GET /protected-test
     *
     * Protected Endpoint (Requires valid JWT token)
     *
     * Purpose:
     * - Test JWT authentication
     * - Verify that user has valid token
     * - Allow any authenticated user (all roles)
     *
     * Security Flow:
     * 1. Client sends GET request WITHOUT token
     *    → Request is rejected (403 Forbidden)
     * 2. Client sends GET request with valid JWT in header
     *    Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     *    → JwtAuthenticationFilter validates token
     *    → User is authenticated in SecurityContext
     *    → Request proceeds to controller
     *    → Return protected data
     * 3. Client sends GET request with invalid/expired token
     *    → JwtAuthenticationFilter rejects token
     *    → Request is rejected (403 Forbidden)
     *
     * Success Response (200 OK):
     * {
     *   "message": "You have access to protected resource",
     *   "username": "john_doe"
     * }
     *
     * Error Response (403 Forbidden):
     * {
     *   "statusCode": 403,
     *   "error": "FORBIDDEN",
     *   "message": "Access Denied",
     *   "timestamp": "2026-04-18 10:30:45",
     *   "errorCode": "AUTH_002"
     * }
     *
     * @param authentication - Spring provides current user from SecurityContext
     * @return ResponseEntity with protected data
     */
    @GetMapping("/protected-test")
    @Operation(
            summary = "Protected Resource Test",
            description = "Access protected resource (requires valid JWT token)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Access successful"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - invalid or missing JWT token"
            )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Map<String, String>> protectedTest(Authentication authentication) {
        log.info("Protected resource accessed | username={}", authentication.getName());

        // Get username from authentication object
        String username = authentication.getName();

        // Build response
        Map<String, String> response = new HashMap<>();
        response.put("message", "You have access to protected resource");
        response.put("username", username);

        return ResponseEntity.ok(response);
    }

    /**
     * Admin Data Endpoint
     *
     * GET /admin-data
     *
     * Admin-Only Endpoint (Requires valid JWT + ADMIN role)
     *
     * @PreAuthorize("hasRole('ADMIN')")
     * - Declarative security at API level
     * - Spring Security checks BEFORE method execution
     * - If user doesn't have ROLE_ADMIN → 403 Forbidden
     * - No manual role checking needed in controller
     * - Easy to add new endpoints with role requirements
     *
     * Advantages of @PreAuthorize:
     * - More readable than manual checking
     * - Centralized authorization logic
     * - Can be combined with other conditions
     * - Easy to maintain and extend
     *
     * Purpose:
     * - Return admin-only data
     * - Only users with ROLE_ADMIN can access
     * - Regular users are rejected even with valid JWT
     *
     * Success Response (200 OK):
     * {
     *   "message": "Admin data - Sensitive information here",
     *   "username": "admin_user",
     *   "role": "ADMIN"
     * }
     *
     * Error Response (403 Forbidden):
     * {
     *   "statusCode": 403,
     *   "error": "FORBIDDEN",
     *   "message": "Access Denied",
     *   "timestamp": "2026-04-18 10:30:45"
     * }
     *
     * @param authentication - Spring provides current user from SecurityContext
     * @return ResponseEntity with admin data
     */
    @GetMapping("/admin-data")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Admin Data Access",
            description = "Access admin-only data (requires valid JWT + ADMIN role)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Access successful - admin data returned"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - insufficient permissions or invalid JWT token"
            )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Map<String, String>> adminData(Authentication authentication) {

        log.info("Admin data accessed | username={}", authentication.getName());

        // Build response
        Map<String, String> response = new HashMap<>();
        response.put("message", "Admin data - Sensitive information here");
        response.put("username", authentication.getName());
        response.put("role", "ADMIN");

        return ResponseEntity.ok(response);
    }
}

