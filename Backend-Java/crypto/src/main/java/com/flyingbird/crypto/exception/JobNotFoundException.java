package com.flyingbird.crypto.exception;

/**
 * Job Not Found Exception
 *
 * Thrown when a scheduler job with the given jobId is not registered.
 *
 * Status Code: 404 Not Found
 */
public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(String message) {
        super(message);
    }
}
