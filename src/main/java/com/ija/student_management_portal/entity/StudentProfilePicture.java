package com.ija.student_management_portal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity that stores compressed profile picture data for a student.
 * The image bytes are stored directly in the database (PostgreSQL bytea column),
 * replacing the previous local-filesystem approach.
 */
@Entity
@Table(name = "student_profile_pictures")
public class StudentProfilePicture {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profile_picture_seq")
    @SequenceGenerator(name = "profile_picture_seq", sequenceName = "profile_picture_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true, nullable = false)
    private String studentId;

    @Column(columnDefinition = "bytea", nullable = false)
    private byte[] imageData;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(length = 255)
    private String originalFileName;

    private Long originalSize;

    private Long compressedSize;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public Long getOriginalSize() {
        return originalSize;
    }

    public void setOriginalSize(Long originalSize) {
        this.originalSize = originalSize;
    }

    public Long getCompressedSize() {
        return compressedSize;
    }

    public void setCompressedSize(Long compressedSize) {
        this.compressedSize = compressedSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
