package com.flyingbird.crypto.exception;

/**
 * User Already Exists Exception
 * 
 * Thrown when user tries to register with a username that already exists.
 * 
 * Status Code: 409 Conflict
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

