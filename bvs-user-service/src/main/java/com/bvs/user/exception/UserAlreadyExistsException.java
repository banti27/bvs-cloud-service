package com.bvs.user.exception;

/**
 * Exception thrown when trying to create a user that already exists
 */
public class UserAlreadyExistsException extends UserServiceException {

    public UserAlreadyExistsException(String field, String value) {
        super("USER_ALREADY_EXISTS", 
              String.format("User with %s '%s' already exists", field, value));
    }

    public UserAlreadyExistsException(String message) {
        super("USER_ALREADY_EXISTS", message);
    }
}
