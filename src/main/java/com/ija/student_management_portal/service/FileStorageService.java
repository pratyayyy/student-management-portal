package com.ija.student_management_portal.service;

import com.ija.student_management_portal.config.FileStorageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

/**
 * Service for handling file storage operations using AWS S3
 * Handles file uploads, validation, retrieval and deletion with security considerations
 */
@Service
@Slf4j
public class FileStorageService {

    @Autowired
    private FileStorageConfig fileStorageConfig;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    /**
     * Upload a student profile picture to S3
     * @param file The multipart file to upload
     * @param studentId The ID of the student
     * @return The S3 object key where the file was stored
     * @throws IOException if file operations fail
     * @throws IllegalArgumentException if file validation fails
     */
    public String uploadProfilePicture(MultipartFile file, String studentId) throws IOException {

        // Validate file
        validateFile(file);

        try {
            // Generate unique S3 object key
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String s3Key = "profiles/" + studentId + "/" + UUID.randomUUID() + "." + fileExtension;

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(fileStorageConfig.getS3BucketName())
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

            log.info("Successfully uploaded profile picture for student: {} with key: {}", studentId, s3Key);
            return s3Key;

        } catch (IOException e) {
            log.error("Failed to upload file for student: {}", studentId, e);
            throw new IOException("Failed to store file. Please try again.", e);
        }
    }

    /**
     * Delete a profile picture from S3
     * @param s3Key The S3 object key of the file to delete
     * @throws IOException if file deletion fails
     */
    public void deleteProfilePicture(String s3Key) throws IOException {
        if (s3Key == null || s3Key.isEmpty()) {
            return;
        }

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(fileStorageConfig.getS3BucketName())
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Successfully deleted file from S3: {}", s3Key);
        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: {}", s3Key, e);
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
     * Get a pre-signed URL for a stored profile picture in S3
     * @param s3Key The S3 object key
     * @return A pre-signed URL to access the picture, or the default avatar path if key is null/empty
     */
    public String getProfilePictureUrl(String s3Key) {
        if (s3Key == null || s3Key.isEmpty()) {
            return "/images/default-avatar.png";
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(fileStorageConfig.getS3BucketName())
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(fileStorageConfig.getPresignedUrlExpirationMinutes()))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * Check if a file exists in S3
     * @param s3Key The S3 object key
     * @return true if the object exists, false otherwise
     */
    public boolean fileExists(String s3Key) {
        if (s3Key == null || s3Key.isEmpty()) {
            return false;
        }
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(fileStorageConfig.getS3BucketName())
                    .key(s3Key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}
