package com.flyingbird.crypto.exception;

/**
 * User Not Found Exception
 *
 * Thrown when a user with the given username cannot be found in the database.
 *
 * Status Code: 404 Not Found
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

