package com.flyingbird.crypto.service;

import com.flyingbird.crypto.dto.UserDetailsResponseDto;
import com.flyingbird.crypto.entity.User;
import com.flyingbird.crypto.exception.UserNotFoundException;
import com.flyingbird.crypto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * User Service Implementation
 *
 * This service implements the business logic for user-related operations.
 *
 * This is the SERVICE LAYER in our layered architecture.
 * - Controllers call this service
 * - This service calls repository for DB operations
 * - All business logic is here
 *
 * Dependencies (Constructor Injection via @RequiredArgsConstructor):
 * - UserRepository: Access database for user queries
 *
 * Per architectural guidelines:
 * - Use @RequiredArgsConstructor (Lombok) for constructor injection
 * - Single Responsibility (user operations only)
 * - Extensive logging for observability
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Get User Details by Username Implementation
     *
     * Detailed Flow:
     * 1. Log the incoming request with username
     * 2. Query repository to find user by username
     * 3. If user exists, convert User entity to UserDetailsResponseDto
     * 4. Log successful retrieval
     * 5. Return DTO with user information (id, username, role, enabled)
     * 6. If user not found, throw UserNotFoundException
     *
     * Security Notes:
     * - Password is never included in the response
     * - Only non-sensitive user data is exposed
     * - User exists check prevents information disclosure
     *
     * @param username - The username to search for
     * @return UserDetailsResponseDto with user details
     * @throws UserNotFoundException if user doesn't exist
     */
    @Override
    public UserDetailsResponseDto getUserDetailsByUsername(String username) {
        log.info("Fetching user details | username={}", username);

        // Query repository to find user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found | username={}", username);
                    return new UserNotFoundException("User with username '" + username + "' not found");
                });

        log.info("User details retrieved successfully | username={} | userId={}", username, user.getId());

        // Convert User entity to DTO
        return UserDetailsResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }
}

