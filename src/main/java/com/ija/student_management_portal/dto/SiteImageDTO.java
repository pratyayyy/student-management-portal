package com.ija.student_management_portal.dto;

import java.time.LocalDateTime;

/**
 * DTO for site images – image bytes are NOT included; use the file URL to fetch them.
 */
public class SiteImageDTO {

    private Long id;
    private String imageType;
    private String altText;
    private Integer sortOrder;
    private String originalFileName;
    private Long originalSize;
    private Long compressedSize;
    private LocalDateTime uploadedAt;
    private String fileUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }

    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public Long getOriginalSize() { return originalSize; }
    public void setOriginalSize(Long originalSize) { this.originalSize = originalSize; }

    public Long getCompressedSize() { return compressedSize; }
    public void setCompressedSize(Long compressedSize) { this.compressedSize = compressedSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}
