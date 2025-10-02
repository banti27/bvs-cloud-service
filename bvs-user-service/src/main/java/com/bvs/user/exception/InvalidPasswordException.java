package com.bvs.user.exception;

/**
 * Exception thrown when password validation fails
 */
public class InvalidPasswordException extends UserServiceException {

    public InvalidPasswordException() {
        super("INVALID_PASSWORD", "Invalid password provided");
    }

    public InvalidPasswordException(String message) {
        super("INVALID_PASSWORD", message);
    }
}
