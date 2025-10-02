package com.bvs.common.exception;

import lombok.Getter;

/**
 * Base exception for all BVS services
 */
@Getter
public class BvsException extends RuntimeException {
    
    private final String errorCode;
    private final Object[] args;
    
    public BvsException(String message) {
        super(message);
        this.errorCode = "BVS_ERROR";
        this.args = null;
    }
    
    public BvsException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }
    
    public BvsException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }
    
    public BvsException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }
}
