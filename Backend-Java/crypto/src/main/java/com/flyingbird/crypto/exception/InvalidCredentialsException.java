package com.flyingbird.crypto.exception;

/**
 * Invalid Credentials Exception
 * 
 * Thrown when user provides incorrect username or password during login.
 * 
 * Status Code: 401 Unauthorized
 */
public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
    
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}

