package com.flyingbird.crypto.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Security Configuration
 *
 * This class configures how Spring Security behaves in our application.
 * It defines:
 * 1. Which endpoints are public vs protected
 * 2. How to authenticate users
 * 3. Which JWT filter to use
 * 4. Password encoding strategy
 * 5. Session management policy
 *
 * Security Flow in this application:
 * 1. Client sends request (with or without JWT token)
 * 2. JwtAuthenticationFilter intercepts request
 * 3. If JWT present and valid, user is authenticated
 * 4. If JWT missing/invalid and endpoint is protected, request is rejected
 * 5. If endpoint is public, request is allowed without authentication
 *
 * This configuration uses STATELESS session policy:
 * - No sessions are created on server
 * - Each request is independent
 * - JWT contains all needed user info
 * - Scalable for microservices and distributed systems
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enables @PreAuthorize, @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;


    /**
     * Security Filter Chain
     *
     * This bean configures the security filter chain for HTTP requests.
     * It defines:
     * 1. CSRF disabled (not needed for stateless JWT)
     * 2. Session management as STATELESS
     * 3. Public vs protected endpoints
     * 4. Custom JWT filter
     *
     * Endpoint Security:
     * - /login, /register, /swagger-ui.html, /v3/api-docs/** → permitAll()
     * - /protected-test → requires valid JWT
     * - /admin-data → requires valid JWT + ROLE_ADMIN
     * - /actuator/health → permitAll()
     * - Any other → authenticated
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Security Filter Chain");

        http
                // Enable CORS — must come before auth checks so OPTIONS preflights pass through
                // Uses the WebMvcConfigurer corsConfigurer() bean defined below
                .cors(Customizer.withDefaults())

                // Disable CSRF (Cross-Site Request Forgery)
                // CSRF not needed for stateless REST APIs with JWT
                .csrf(csrf -> csrf.disable())

                // Disable frame options for H2 console (if using H2 in-memory DB)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))

                // Configure session management to be STATELESS
                // No HttpSession will be created or used
                .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configure exception handling
                .exceptionHandling(exception ->
                    exception.authenticationEntryPoint(customAuthenticationEntryPoint)
                )

                // Configure authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints - no authentication needed
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Protected endpoints - authentication required
                        .requestMatchers("/protected-test").authenticated()

                        // Admin-only endpoint - authentication required
                        // Role check is done in controller to provide custom error response
                        .requestMatchers("/admin-data").authenticated()

                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )

                // Add JWT filter before username/password filter
                // This ensures JWT is validated before other auth methods
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        log.info("Security Filter Chain configured successfully");
        return http.build();
    }

    /**
     * CORS Configuration Bean
     *
     * Allows frontend applications (like React on localhost:5173)
     * to make cross-origin requests to this API.
     *
     * Without this, the browser blocks requests from different origins
     * (same-origin policy).
     *
     * This configuration:WebMvcConfigurer
     * - Applies to all /api/** endpoints
     * - Allows requests from http://localhost:5173 (React dev server)
     * - Allows standard HTTP methods (GET, POST, PUT, DELETE)
     * - Allows all headers (including Authorization for JWT)
     * - Allows credentials (cookies, auth headers)
     * - Caches preflight response for 1 hour
     *
     * Note: For production, replace localhost:5173 with actual domain
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                log.info("Configuring CORS for frontend applications");

                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);

                log.info("CORS configuration applied for http://localhost:5173");
            }
        };
    }

    /**
     * Password Encoder Bean
     *
     * BCryptPasswordEncoder:
     * - Uses bcrypt algorithm to hash passwords
     * - Automatically generates salt and stores it with hash
     * - One-way function (can't reverse)
     * - During login, provided password is hashed and compared with stored hash
     *
     * Why BCrypt?
     * - Slow by design (prevents brute force attacks)
     * - Each hash is unique even for same password (due to salt)
     * - Industry standard, battle-tested
     *
     * Usage:
     * - In register endpoint: passwordEncoder.encode(plainPassword)
     * - In login: passwordEncoder.matches(providedPassword, storedHash)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Creating BCryptPasswordEncoder bean");
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Provider Bean
     *
     * DaoAuthenticationProvider:
     * - Uses UserDetailsService to load user from DB
     * - Uses PasswordEncoder to verify password
     * - Is called when authentication is needed
     *
     * Flow:
     * 1. User submits username + password
     * 2. AuthenticationManager finds matching provider
     * 3. Provider loads user from DB using UserDetailsService
     * 4. Provider compares provided password with stored (using PasswordEncoder)
     * 5. If match, authentication successful
     * 6. If no match, throw AuthenticationException
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        log.info("Creating DaoAuthenticationProvider bean");

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    /**
     * Authentication Manager Bean
     *
     * AuthenticationManager:
     * - Central component that orchestrates authentication
     * - Holds list of AuthenticationProviders
     * - When authenticate() is called, tries each provider
     * - Returns Authentication if any provider succeeds
     *
     * Used in:
     * - AuthService to authenticate login credentials
     * - Any place where we need to verify username + password
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.info("Creating AuthenticationManager bean");
        return config.getAuthenticationManager();
    }
}

