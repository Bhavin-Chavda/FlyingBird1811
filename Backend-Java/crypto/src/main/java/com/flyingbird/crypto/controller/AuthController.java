package com.flyingbird.crypto.controller;

import com.flyingbird.crypto.dto.AuthResponseDto;
import com.flyingbird.crypto.dto.LoginRequestDto;
import com.flyingbird.crypto.dto.RegisterRequestDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 *
 * This is the CONTROLLER LAYER - Entry point for HTTP requests.
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
 * - Role-based access via @PreAuthorize
 *
 * Endpoints:
 * 1. POST /login - Public, authenticate user, return JWT
 * 2. POST /register - Public, create new user
 * 3. GET /protected-test - Protected, requires valid JWT
 * 4. GET /admin-data - Admin only, uses @PreAuthorize("hasRole('ADMIN')")
 *
 * All endpoints have Swagger documentation (@Operation, @ApiResponses)
 * @RequiredArgsConstructor generates constructor for final fields
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth") // Root path
public class AuthController {

    private final AuthService authService;


    /**
     * Login Endpoint
     *
     * POST /login
     *
     * Public Endpoint (No authentication required)
     *
     * Purpose:
     * - Verify user credentials (username + password)
     * - If valid, generate JWT token
     * - Client stores token and uses for future requests
     *
     * Request Flow:
     * 1. Client sends POST request with username and password
     * 2. @Valid annotation validates request body
     *    - @NotBlank ensures fields are not empty
     * 3. Controller calls authService.login()
     * 4. Service authenticates using Spring Security
     * 5. Service generates JWT token
     * 6. Return token in response
     *
     * Success Response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "username": "john_doe",
     *   "role": "USER",
     *   "message": "Login successful"
     * }
     *
     * Error Response (401 Unauthorized):
     * {
     *   "statusCode": 401,
     *   "error": "UNAUTHORIZED",
     *   "message": "Invalid username or password",
     *   "timestamp": "2026-04-18 10:30:45",
     *   "errorCode": "AUTH_001"
     * }
     *
     * @param loginRequestDto - JSON body with username and password
     * @return ResponseEntity with JWT token or error
     */
    @PostMapping("/login")
    @Operation(
            summary = "User Login",
            description = "Authenticate user with credentials and receive JWT token"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful, JWT token returned",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body (validation failed)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid username or password"
            )
    })
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("Login request received | username={}", loginRequestDto.getUsername());

        AuthResponseDto response = authService.login(loginRequestDto);

        log.info("Login successful | username={}", loginRequestDto.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Register Endpoint
     *
     * POST /register
     *
     * Public Endpoint (No authentication required)
     *
     * Purpose:
     * - Create new user account
     * - Hash password and store securely
     * - User is immediately enabled
     *
     * Request Flow:
     * 1. Client sends POST request with username and password
     * 2. @Valid annotation validates request body
     * 3. Controller calls authService.register()
     * 4. Service checks if username already exists
     * 5. If yes, throw UserAlreadyExistsException (409 Conflict)
     * 6. If no, hash password and save user
     * 7. Return success response
     *
     * Success Response (201 Created):
     * {
     *   "username": "john_doe",
     *   "role": "USER",
     *   "message": "Registration successful"
     * }
     *
     * Error Response (409 Conflict):
     * {
     *   "statusCode": 409,
     *   "error": "CONFLICT",
     *   "message": "Username 'john_doe' already exists",
     *   "timestamp": "2026-04-18 10:30:45",
     *   "errorCode": "USER_001"
     * }
     *
     * @param registerRequestDto - JSON body with username and password
     * @return ResponseEntity with success message
     */
    @PostMapping("/register")
    @Operation(
            summary = "User Registration",
            description = "Create new user account with username and password"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Registration successful",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body (validation failed)"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username already exists"
            )
    })
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        log.info("Registration request received | username={}", registerRequestDto.getUsername());

        AuthResponseDto response = authService.register(registerRequestDto);

        log.info("Registration successful | username={}", registerRequestDto.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}

