package com.flyingbird.crypto.service;

import com.flyingbird.crypto.dto.AdminRegisterUserRequestDto;
import com.flyingbird.crypto.dto.AuthResponseDto;
import com.flyingbird.crypto.dto.LoginRequestDto;
import com.flyingbird.crypto.dto.RegisterRequestDto;

/**
 * Authentication Service Interface
 * 
 * This interface defines the contract for authentication operations.
 * Implementations must provide:
 * 1. User login - verify credentials and generate JWT
 * 2. User registration - create new user account
 * 
 * Following Interface Segregation Principle (ISP):
 * - Clients depend on abstractions, not implementations
 * - Easy to swap implementations
 * - Easy to mock for testing
 * 
 * We use interface-driven design per architectural guidelines.
 */
public interface AuthService {
    
    /**
     * Login User
     * 
     * Authenticates user with provided credentials and generates JWT token.
     * 
     * Steps:
     * 1. Verify username and password against database
     * 2. If valid, generate JWT token
     * 3. Return token in response
     * 4. If invalid, throw InvalidCredentialsException
     * 
     * @param loginRequestDto - username and password
     * @return AuthResponseDto with JWT token
     * @throws InvalidCredentialsException if credentials are invalid
     */
    AuthResponseDto login(LoginRequestDto loginRequestDto);
    
    /**
     * Register User
     * 
     * Creates new user account with provided credentials.
     * 
     * Steps:
     * 1. Check if username already exists
     * 2. If yes, throw UserAlreadyExistsException
     * 3. Hash password using BCrypt
     * 4. Save user to database with ROLE_USER role
     * 5. Return success response
     * 
     * @param registerRequestDto - username and password
     * @return AuthResponseDto with success message
     * @throws RegisterRequestDto if username already exists
     */
    AuthResponseDto register(RegisterRequestDto registerRequestDto);

    /**
     * Register a user on behalf of an ADMIN (protected flow).
     *
     * <p>Same persistence rules as {@link #register} (unique username, BCrypt password,
     * enabled=true) but the {@code role} is REQUIRED and explicitly chosen by the admin
     * (USER or ADMIN, normalized to upper-case). Authorization (ADMIN-only) is enforced at
     * the controller via {@code @PreAuthorize}; this method assumes the caller is authorized.</p>
     *
     * @param request - username, password and role chosen by the admin
     * @return AuthResponseDto with success message (no token issued)
     * @throws com.flyingbird.crypto.exception.UserAlreadyExistsException if username already exists
     */
    AuthResponseDto registerByAdmin(AdminRegisterUserRequestDto request);

    /**
     * Disable a user (ADMIN only, protected flow) — sets {@code enabled=false} so the account
     * can no longer authenticate. Authorization (ADMIN-only) is enforced at the controller.
     *
     * <p>Business rules: the target must exist (else 404) and must NOT have the ADMIN role
     * (ADMIN accounts cannot be disabled → 403). Idempotent if already disabled.</p>
     *
     * @param username - the user to disable
     * @return AuthResponseDto with success message
     * @throws com.flyingbird.crypto.exception.UserNotFoundException if the username doesn't exist
     * @throws com.flyingbird.crypto.exception.ForbiddenAccessException if the target is an ADMIN
     */
    AuthResponseDto disableUser(String username);
}

