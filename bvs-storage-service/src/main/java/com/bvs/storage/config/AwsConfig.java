package com.bvs.storage.config;

import org.springframework.context.annotation.Configuration;

/**
 * AWS Configuration for S3 integration.
 * 
 * Spring Cloud AWS will auto-configure S3Client based on application.properties.
 * Additional custom configuration can be added here as needed.
 */
@Configuration
public class AwsConfig {
    
    // Spring Cloud AWS automatically provides S3Client bean
    // Custom beans can be added here if needed
    
}
