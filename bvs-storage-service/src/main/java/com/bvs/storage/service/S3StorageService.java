package com.bvs.storage.service;

import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for handling S3 operations.
 * 
 * This service uses Spring Cloud AWS S3Template for S3 operations.
 * Configure AWS credentials in application.properties to use this service.
 */
@Service
public class S3StorageService {

    private final S3Template s3Template;

    @Autowired
    public S3StorageService(S3Template s3Template) {
        this.s3Template = s3Template;
    }

    /**
     * Example method to upload content to S3.
     * Uncomment and implement when AWS credentials are configured.
     */
    // public void uploadFile(String bucketName, String key, InputStream inputStream) {
    //     s3Template.upload(bucketName, key, inputStream);
    // }

    /**
     * Example method to download content from S3.
     * Uncomment and implement when AWS credentials are configured.
     */
    // public InputStream downloadFile(String bucketName, String key) {
    //     return s3Template.download(bucketName, key).getInputStream();
    // }
}
