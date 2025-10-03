package com.bvs.user.exception;

/**
 * Base exception for User Service
 * All user-related exceptions should extend this class
 */
public class UserServiceException extends RuntimeException {

    private final String errorCode;

    public UserServiceException(String message) {
        super(message);
        this.errorCode = "USER_SERVICE_ERROR";
    }

    public UserServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "USER_SERVICE_ERROR";
    }

    public UserServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
