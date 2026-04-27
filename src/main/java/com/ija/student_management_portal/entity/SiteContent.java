package com.ija.student_management_portal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Stores editable text content for the promotional website (key/value pairs).
 * Examples: hero.title, about.description, contact.phone, etc.
 */
@Entity
@Table(name = "site_content")
public class SiteContent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "site_content_seq")
    @SequenceGenerator(name = "site_content_seq", sequenceName = "site_content_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String contentKey;

    @Column(columnDefinition = "text")
    private String contentValue;

    @Column(length = 255)
    private String label;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContentKey() { return contentKey; }
    public void setContentKey(String contentKey) { this.contentKey = contentKey; }

    public String getContentValue() { return contentValue; }
    public void setContentValue(String contentValue) { this.contentValue = contentValue; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
