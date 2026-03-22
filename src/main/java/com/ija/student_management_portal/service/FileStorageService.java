package com.ija.student_management_portal.service;

import com.ija.student_management_portal.config.FileStorageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service for handling file storage operations
 * Handles file uploads, validation, retrieval and deletion with security considerations
 */
@Service
@Slf4j
public class FileStorageService {

    @Autowired
    private FileStorageConfig fileStorageConfig;

    /**
     * Upload a student profile picture
     * @param file The multipart file to upload
     * @param studentId The ID of the student
     * @return The relative path where the file was stored
     * @throws IOException if file operations fail
     * @throws IllegalArgumentException if file validation fails
     */
    public String uploadProfilePicture(MultipartFile file, String studentId) throws IOException {

        // Validate file
        validateFile(file);

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(fileStorageConfig.getUploadDir(), studentId).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Generate unique filename with UUID
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String storedFileName = UUID.randomUUID() + "." + fileExtension;

            // Save file
            Path filePath = uploadPath.resolve(storedFileName).normalize();

            // Security check: ensure the file path is within the upload directory
            if (!filePath.getParent().equals(uploadPath.normalize())) {
                throw new IllegalArgumentException("Invalid file path - potential path traversal attack detected");
            }

            Files.write(filePath, file.getBytes());

            // Return relative path for storage in database
            String relativePath = "uploads/profiles/" + studentId + "/" + storedFileName;
            log.info("Successfully uploaded profile picture for student: {} at path: {}", studentId, relativePath);

            return relativePath;

        } catch (IOException e) {
            log.error("Failed to upload file for student: {}", studentId, e);
            throw new IOException("Failed to store file. Please try again.", e);
        }
    }

    /**
     * Delete a profile picture
     * @param storagePath The relative storage path of the file to delete
     * @throws IOException if file deletion fails
     */
    public void deleteProfilePicture(String storagePath) throws IOException {
        if (storagePath == null || storagePath.isEmpty()) {
            return;
        }

        try {
            Path filePath = Paths.get(storagePath).toAbsolutePath().normalize();

            // Security check
            if (!filePath.toString().contains("uploads/profiles")) {
                throw new IllegalArgumentException("Invalid file path for deletion");
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Successfully deleted file: {}", storagePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", storagePath, e);
            throw new IOException("Failed to delete file", e);
        }
    }

    /**
     * Validate uploaded file
     * @param file The file to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateFile(MultipartFile file) {

        // Check if file is empty
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or missing");
        }

        // Check file size
        if (file.getSize() > fileStorageConfig.getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " +
                (fileStorageConfig.getMaxFileSize() / (1024 * 1024)) + "MB");
        }

        // Check file extension
        String fileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(fileName);
        if (!fileStorageConfig.getAllowedExtensions().contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " +
                fileStorageConfig.getAllowedExtensions());
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !fileStorageConfig.getAllowedMimeTypes().contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only image files are allowed");
        }
    }

    /**
     * Extract file extension from filename
     * @param fileName The filename to process
     * @return The file extension without the dot
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Get the full URL path for a stored profile picture
     * @param storagePath The relative storage path
     * @return The URL path to access the picture
     */
    public String getProfilePictureUrl(String storagePath) {
        if (storagePath == null || storagePath.isEmpty()) {
            return "/images/default-avatar.png"; // Default avatar
        }
        return "/" + storagePath;
    }

    /**
     * Check if a file exists
     * @param storagePath The relative storage path
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String storagePath) {
        if (storagePath == null || storagePath.isEmpty()) {
            return false;
        }
        return Files.exists(Paths.get(storagePath).toAbsolutePath().normalize());
    }
}
