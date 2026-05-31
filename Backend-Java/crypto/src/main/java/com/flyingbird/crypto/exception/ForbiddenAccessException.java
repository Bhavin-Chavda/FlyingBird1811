package com.flyingbird.crypto.exception;

/**
 * Forbidden Access Exception
 *
 * Thrown when user tries to access a resource they don't have permission for.
 * For example: Non-admin user trying to access /admin-data endpoint.
 *
 * Status Code: 403 Forbidden
 */
public class ForbiddenAccessException extends RuntimeException {

    public ForbiddenAccessException(String message) {
        super(message);
    }

    public ForbiddenAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

