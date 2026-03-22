package com.ija.student_management_portal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for file storage settings
 * Manages upload directory, file size limits, and allowed file types
 */
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageConfig {

    private String uploadDir = "uploads/profiles";
    private Long maxFileSize = 5242880L; // 5MB default
    private List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");
    private List<String> allowedMimeTypes = Arrays.asList("image/jpeg", "image/png", "image/gif");

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

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
}
