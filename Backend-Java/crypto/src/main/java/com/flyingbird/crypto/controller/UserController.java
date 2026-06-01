package com.flyingbird.crypto.controller;

import com.flyingbird.crypto.dto.UserDetailsRequestDto;
import com.flyingbird.crypto.dto.UserDetailsResponseDto;
import com.flyingbird.crypto.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller
 *
 * This is the CONTROLLER LAYER - Entry point for HTTP requests related to user operations.
 *
 * Responsibilities:
 * 1. Accept HTTP requests
 * 2. Validate request format
 * 3. Call service layer for business logic
 * 4. Return HTTP responses
 *
 * Per architectural guidelines:
 * - No business logic here (logic in service)
 * - No database access (access in repository)
 * - Only request/response handling
 * - Extensive input validation (@Valid)
 *
 * Endpoints:
 * 1. POST /userDetails - Get user details by username
 *
 * All endpoints have Swagger documentation (@Operation, @ApiResponses)
 * @RequiredArgsConstructor generates constructor for final fields
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Get User Details Endpoint
     *
     * POST /api/users/userDetails
     *
     * Protected Endpoint (Requires valid JWT token)
     *
     * Purpose:
     * - Retrieve detailed information about a user by username
     * - Returns user id, username, role, and enabled status
     * - Does not expose sensitive information like password
     *
     * Request Flow:
     * 1. Client sends POST request with username in JSON body
     * 2. @Valid annotation validates request body
     *    - @NotBlank ensures username is not empty
     * 3. Controller calls userService.getUserDetailsByUsername()
     * 4. Service queries database and converts to DTO
     * 5. Return user details in response
     *
     * Success Response (200 OK):
     * {
     *   "id": 1,
     *   "username": "john_doe",
     *   "role": "USER",
     *   "enabled": true
     * }
     *
     * Error Response (404 Not Found):
     * {
     *   "statusCode": 404,
     *   "error": "NOT_FOUND",
     *   "message": "User with username 'john_doe' not found",
     *   "timestamp": "2026-05-10 10:30:45",
     *   "errorCode": "USER_002"
     * }
     *
     * @param userDetailsRequestDto - JSON body with username
     * @return ResponseEntity with user details
     */ 
    @PostMapping("/userDetails")
    @Operation(
            summary = "Get User Details",
            description = "Retrieve detailed information about a user by username"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserDetailsResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body (validation failed)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User with given username not found"
            )
    })
    public ResponseEntity<UserDetailsResponseDto> getUserDetails(
            @Valid @RequestBody UserDetailsRequestDto userDetailsRequestDto) {

        log.info("User details request received | username={}", userDetailsRequestDto.getUsername());

        UserDetailsResponseDto response = userService.getUserDetailsByUsername(
                userDetailsRequestDto.getUsername());

        log.info("User details sent | username={}", userDetailsRequestDto.getUsername());
        return ResponseEntity.ok(response);
    }
}
