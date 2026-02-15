package com.bvs.aadhar.exception;

public class XmlProcessingTimeoutException extends RuntimeException {
    public XmlProcessingTimeoutException(String message) {
        super(message);
    }
    
    public XmlProcessingTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
