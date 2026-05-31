package com.flyingbird.crypto.config;

import com.flyingbird.crypto.entity.User;
import com.flyingbird.crypto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService
 *
 * This service is called by Spring Security when:
 * 1. User tries to login (during authentication)
 * 2. Spring needs to verify user details
 *
 * It implements UserDetailsService interface which requires
 * implementing loadUserByUsername() method.
 *
 * Spring Security Flow:
 * 1. User sends username/password to /login endpoint
 * 2. AuthService calls this service to load user from DB
 * 3. AuthService compares password from DB with provided password
 * 4. If match, JWT token is generated
 *
 * This service is also used by Spring Security to:
 * - Load user details from DB
 * - Get user authorities/roles
 * - Check if account is enabled/active
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    /**
     * Load User by Username
     *
     * This method is called by Spring Security to fetch user from database.
     *
     * Steps:
     * 1. Search for user in database by username
     * 2. If not found, throw UsernameNotFoundException
     * 3. If found, convert User entity to Spring UserDetails object
     * 4. Return UserDetails with username, password, and authorities
     *
     * @param username - username to search for
     * @return UserDetails object containing user info and authorities
     * @throws UsernameNotFoundException - if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user details for username: {}", username);

        // Find user in database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        log.info("User found | username={} | role={}", user.getUsername(), user.getRole());

        // Build Spring UserDetails from our User entity
        // UserDetails is Spring's interface for user information
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority(user.getRole()))
                .disabled(!user.isEnabled()) // Note: disabled is opposite of enabled
                .build();
    }
}

