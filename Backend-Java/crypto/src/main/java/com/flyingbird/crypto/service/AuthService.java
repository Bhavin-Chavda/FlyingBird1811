package com.flyingbird.crypto.service;

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
     * @throws UserAlreadyExistsException if username already exists
     */
    AuthResponseDto register(RegisterRequestDto registerRequestDto);
}

