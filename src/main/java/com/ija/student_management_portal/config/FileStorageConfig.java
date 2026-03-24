package com.ija.student_management_portal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for file storage settings
 * Manages S3 bucket settings, file size limits, and allowed file types
 */
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageConfig {

    private Long maxFileSize = 5242880L; // 5MB default
    private List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");
    private List<String> allowedMimeTypes = Arrays.asList("image/jpeg", "image/png", "image/gif");
    private String s3BucketName;
    private String s3Region;
    private long presignedUrlExpirationMinutes = 60;

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public List<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(List<String> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public void setS3BucketName(String s3BucketName) {
        this.s3BucketName = s3BucketName;
    }

    public String getS3Region() {
        return s3Region;
    }

    public void setS3Region(String s3Region) {
        this.s3Region = s3Region;
    }

    public long getPresignedUrlExpirationMinutes() {
        return presignedUrlExpirationMinutes;
    }

    public void setPresignedUrlExpirationMinutes(long presignedUrlExpirationMinutes) {
        this.presignedUrlExpirationMinutes = presignedUrlExpirationMinutes;
    }
}
