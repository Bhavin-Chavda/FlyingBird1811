package com.flyingbird.crypto.service;

import com.flyingbird.crypto.dto.UserDetailsResponseDto;
import com.flyingbird.crypto.exception.UserNotFoundException;

/**
 * User Service Interface
 *
 * This interface defines the contract for user-related operations.
 * Implementations must provide:
 * 1. Fetch user details by username
 *
 * Following Interface Segregation Principle (ISP):
 * - Clients depend on abstractions, not implementations
 * - Easy to swap implementations
 * - Easy to mock for testing
 *
 * We use interface-driven design per architectural guidelines.
 */
public interface UserService {

    /**
     * Get User Details by Username
     *
     * Retrieves detailed information about a user based on their username.
     *
     * Steps:
     * 1. Query database using UserRepository
     * 2. If user found, return user details in DTO format
     * 3. If not found, throw UserNotFoundException
     *
     * @param username - The username to search for
     * @return UserDetailsResponseDto with user information
     * @throws UserNotFoundException if user with given username doesn't exist
     */
    UserDetailsResponseDto getUserDetailsByUsername(String username);
}

