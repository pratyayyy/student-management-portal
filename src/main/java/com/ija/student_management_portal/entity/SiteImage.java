package com.ija.student_management_portal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Stores images uploaded by admins for the promotional website.
 * Image type determines where the image appears (HERO, ABOUT, GALLERY, LOGO).
 */
@Entity
@Table(name = "site_images")
public class SiteImage {

    public enum ImageType {
        HERO, ABOUT, GALLERY, LOGO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "site_image_seq")
    @SequenceGenerator(name = "site_image_seq", sequenceName = "site_image_seq", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImageType imageType;

    @Column(columnDefinition = "bytea", nullable = false)
    private byte[] imageData;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(length = 255)
    private String originalFileName;

    @Column(length = 255)
    private String altText;

    private Integer sortOrder;

    private Long originalSize;

    private Long compressedSize;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ImageType getImageType() { return imageType; }
    public void setImageType(ImageType imageType) { this.imageType = imageType; }

    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Long getOriginalSize() { return originalSize; }
    public void setOriginalSize(Long originalSize) { this.originalSize = originalSize; }

    public Long getCompressedSize() { return compressedSize; }
    public void setCompressedSize(Long compressedSize) { this.compressedSize = compressedSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
