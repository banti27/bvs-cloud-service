package com.bvs.user.exception;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends UserServiceException {

    public UserNotFoundException(String field, String value) {
        super("USER_NOT_FOUND", String.format("User not found with %s: %s", field, value));
    }
}
