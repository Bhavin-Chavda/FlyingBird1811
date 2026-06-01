package com.flyingbird.crypto.exception;

import com.flyingbird.crypto.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 *
 * This class centralizes exception handling for the entire application.
 * Instead of handling exceptions in each controller, we handle them here.
 *
 * @RestControllerAdvice:
 * - Applies to all @RestController classes
 * - Returns JSON responses (not HTML)
 * - Centralized error handling
 *
 * Benefits:
 * 1. Consistent error responses across all endpoints
 * 2. No repeated try-catch blocks in controllers
 * 3. Easy to modify error format globally
 * 4. Professional, structured error responses
 *
 * Error Response Structure (defined in ErrorResponse.java):
 * {
 *   "statusCode": 400,
 *   "error": "BAD_REQUEST",
 *   "message": "Username or password is invalid",
 *   "timestamp": "2026-04-18 10:30:45",
 *   "errorCode": "AUTH_001"
 * }
 *
 * How it works:
 * 1. Exception occurs in controller or service
 * 2. Spring catches it and checks for matching @ExceptionHandler
 * 3. Matching handler is executed
 * 4. Handler returns ErrorResponse with appropriate status code
 * 5. Client receives structured error response
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle User Already Exists Exception
     *
     * Status: 409 Conflict
     * Reason: Username already exists in database
     *
     * When to throw:
     * - User tries to register with existing username
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex,
            WebRequest request) {

        log.warn("User already exists exception: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpStatus.CONFLICT.value())
                .error("CONFLICT")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errorCode("USER_001")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle Invalid Credentials Exception
     *
     * Status: 401 Unauthorized
     * Reason: Username or password is incorrect
     *
     * When to throw:
     * - User provides wrong password
     * - User tries to login with non-existent username
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            WebRequest request) {

        log.warn("Invalid credentials attempted");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .error("UNAUTHORIZED")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errorCode("AUTH_001")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle Forbidden Access Exception
     *
     * Status: 403 Forbidden
     * Reason: User doesn't have required role/permission
     *
     * When to throw:
     * - Non-admin user tries to access /admin-data
     * - User tries to access resource they don't have permission for
     */
    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenAccess(
            ForbiddenAccessException ex,
            WebRequest request) {

        log.warn("Forbidden access attempt: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .error("FORBIDDEN")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errorCode("AUTH_002")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle Validation Errors
     *
     * Status: 400 Bad Request
     * Reason: Request body fails validation (e.g., @NotBlank, @Email)
     *
     * When to throw:
     * - Username field is blank in login/register request
     * - Email format is invalid
     * - Required field is missing
     *
     * Returns: Field-level validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.warn("Validation failed for request");

        // Extract field errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(fieldName, message);
        });

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.BAD_REQUEST.value());
        response.put("error", "VALIDATION_ERROR");
        response.put("message", "Validation failed for one or more fields");
        response.put("timestamp", LocalDateTime.now());
        response.put("fieldErrors", fieldErrors);

        log.error("Validation errors: {}", fieldErrors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Username Not Found Exception
     *
     * Status: 401 Unauthorized
     * Reason: User doesn't exist in database
     *
     * When thrown by Spring Security:
     * - CustomUserDetailsService throws when user not found
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(
            UsernameNotFoundException ex,
            WebRequest request) {

        log.warn("Username not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .error("UNAUTHORIZED")
                .message("Invalid username or password")
                .timestamp(LocalDateTime.now())
                .errorCode("AUTH_003")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle Bad Credentials Exception
     *
     * Status: 401 Unauthorized
     * Reason: Password is incorrect
     *
     * When thrown by Spring Security:
     * - During authentication when password doesn't match
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            WebRequest request) {

        log.warn("Bad credentials provided");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .error("UNAUTHORIZED")
                .message("Invalid username or password")
                .timestamp(LocalDateTime.now())
                .errorCode("AUTH_001")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle Authorization Denied Exception
     *
     * Status: 403 Forbidden
     * Reason: User doesn't have required authority/role for @PreAuthorize
     *
     * When thrown by Spring Security:
     * - @PreAuthorize condition not met
     * - Method marked with role requirement but user lacks role
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
            AuthorizationDeniedException ex,
            WebRequest request) {

        log.warn("Authorization denied: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .error("FORBIDDEN")
                .message("Access Denied - Insufficient permissions")
                .timestamp(LocalDateTime.now())
                .errorCode("AUTH_002")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle User Not Found Exception
     *
     * Status: 404 Not Found
     * Reason: User with given username doesn't exist
     *
     * When to throw:
     * - User queries for a username that doesn't exist in database
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            WebRequest request) {

        log.warn("User not found exception: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .error("NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errorCode("USER_002")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle Job Not Found Exception
     *
     * Status: 404 Not Found
     * Reason: Scheduler job with given jobId is not registered
     *
     * When to throw:
     * - Client queries /api/scheduler/jobs/{jobId} with an unknown jobId
     */
    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleJobNotFound(
            JobNotFoundException ex,
            WebRequest request) {

        log.warn("Scheduler job not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .error("NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errorCode("JOB_001")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle All Other Exceptions (Catch-all)
     *
     * Status: 500 Internal Server Error
     * Reason: Unexpected/unhandled exception
     *
     * This is a fallback handler for exceptions not specifically handled above.
     * It prevents stack traces from leaking to clients.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected exception occurred", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .timestamp(LocalDateTime.now())
                .errorCode("SYS_001")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

