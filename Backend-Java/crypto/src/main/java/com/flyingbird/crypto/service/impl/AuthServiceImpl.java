package com.flyingbird.crypto.service.impl;

import com.flyingbird.crypto.dto.AuthResponseDto;
import com.flyingbird.crypto.dto.LoginRequestDto;
import com.flyingbird.crypto.dto.RegisterRequestDto;
import com.flyingbird.crypto.entity.User;
import com.flyingbird.crypto.exception.InvalidCredentialsException;
import com.flyingbird.crypto.exception.UserAlreadyExistsException;
import com.flyingbird.crypto.repository.UserRepository;
import com.flyingbird.crypto.service.AuthService;
import com.flyingbird.crypto.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication Service Implementation
 *
 * This service implements the business logic for:
 * 1. User login - authenticate and generate JWT
 * 2. User registration - create new user account
 *
 * This is the SERVICE LAYER in our layered architecture.
 * - Controllers call this service
 * - This service calls repository for DB operations
 * - All business logic is here
 *
 * Dependencies (Constructor Injection via @RequiredArgsConstructor):
 * - UserRepository: Access database
 * - PasswordEncoder: Hash passwords securely
 * - AuthenticationManager: Verify username/password
 * - JwtUtil: Generate JWT tokens
 *
 * Per architectural guidelines:
 * - Use @RequiredArgsConstructor (Lombok) for constructor injection
 * - Single Responsibility (auth only)
 * - Extensive logging for observability
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


    /**
     * Login Implementation
     *
     * Detailed Flow:
     * 1. Create authentication token with provided credentials
     * 2. Use AuthenticationManager to verify credentials
     *    - AuthenticationManager finds DaoAuthenticationProvider
     *    - Provider loads user from DB using CustomUserDetailsService
     *    - Provider compares provided password with stored (using PasswordEncoder)
     * 3. If authentication fails, throw InvalidCredentialsException
     * 4. Extract user details from authentication
     * 5. Generate JWT token with user info and roles
     * 6. Return token in response
     *
     * Security Notes:
     * - Password is never stored in memory as plain text
     * - Password is never logged
     * - We use AuthenticationManager (Spring Security standard)
     * - Credentials are verified using BCrypt (one-way hashing)
     *
     * @param loginRequestDto - username and password from client
     * @return AuthResponseDto with JWT token
     * @throws InvalidCredentialsException if login fails
     */
    @Override
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        String username = loginRequestDto.getUsername();
        String password = loginRequestDto.getPassword();

        log.info("Login attempt | username={}", username);

        try {
            // Create authentication token with provided credentials
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            // Authenticate using AuthenticationManager
            // This verifies username and password against database
            Authentication authentication = authenticationManager.authenticate(authToken);

            log.info("Authentication successful | username={}", username);

            // Get authenticated user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Get user from repository to access additional fields (role)
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new InvalidCredentialsException("User not found"));

            // Generate JWT token
            String jwtToken = jwtUtil.generateToken(userDetails);

            log.info("JWT token generated | username={}", username);

            // Build response
            return AuthResponseDto.builder()
                    .token(jwtToken)
                    .username(user.getUsername())
                    .role(user.getRole())
                    .message("Login successful")
                    .build();

        } catch (Exception e) {
            log.error("Authentication failed | username={} | error={}", username, e.getMessage());
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    /**
     * Register Implementation
     *
     * Detailed Flow:
     * 1. Check if username already exists in database
     * 2. If yes, throw UserAlreadyExistsException
     * 3. Hash password using BCryptPasswordEncoder
     *    - Each hash is unique due to salt
     *    - Slow by design to prevent brute force
     * 4. Create new User entity with:
     *    - Username from request
     *    - Hashed password
     *    - Default role (USER)
     *    - Enabled = true
     * 5. Save to database
     * 6. Return success response
     *
     * Security Notes:
     * - Username uniqueness is enforced in DB and code
     * - Password is hashed BEFORE storing
     * - Plain password is never stored
     * - User is enabled by default
     *
     * @param registerRequestDto - username and password from client
     * @return AuthResponseDto with success message
     * @throws UserAlreadyExistsException if username exists
     */
    @Override
    public AuthResponseDto register(RegisterRequestDto registerRequestDto) {
        String username = registerRequestDto.getUsername();
        String password = registerRequestDto.getPassword();

        log.info("Registration attempt | username={}", username);

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            log.warn("Username already exists | username={}", username);
            throw new UserAlreadyExistsException("Username '" + username + "' already exists");
        }

        // Hash password before storing
        String hashedPassword = passwordEncoder.encode(password);

        // Set default role for new users
        String role = registerRequestDto.getRole();
        if (role == null || role.isBlank()) {
            role = "USER";
        }

        // Create new user entity
        User newUser = User.builder()
                .username(username)
                .password(hashedPassword) // Store hashed password
                .role(role)
                .enabled(true) // Enable user immediately
                .build();

        // Save to database
        userRepository.save(newUser);

        log.info("User registered successfully | username={} | role={}", username, role);

        // Return response
        return AuthResponseDto.builder()
                .username(username)
                .role(role)
                .message("Registration successful")
                .build();
    }
}

