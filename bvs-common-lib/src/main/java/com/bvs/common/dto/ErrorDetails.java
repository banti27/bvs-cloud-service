package com.bvs.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Error details for API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorDetails {
    
    /**
     * Error code (e.g., "USER_NOT_FOUND", "VALIDATION_ERROR")
     */
    private String code;
    
    /**
     * Detailed error message
     */
    private String details;
    
    /**
     * Field-specific errors (for validation)
     */
    private Map<String, String> fieldErrors;
    
    /**
     * Stack trace (only in development)
     */
    private String stackTrace;
    
    /**
     * Additional error information
     */
    private List<String> additionalInfo;
}
