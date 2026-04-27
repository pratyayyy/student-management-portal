package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.SiteContentDTO;
import com.ija.student_management_portal.dto.SiteImageDTO;
import com.ija.student_management_portal.entity.SiteImage.ImageType;
import com.ija.student_management_portal.service.SiteContentService;
import com.ija.student_management_portal.service.SiteImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Admin-only REST endpoints for managing the promotional website content.
 * All routes are under /api/admin/website and require ADMIN role (enforced in SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin/website")
@Slf4j
public class ApiAdminWebsiteController {

    @Autowired
    private SiteContentService siteContentService;

    @Autowired
    private SiteImageService siteImageService;

    // ── Text Content ───────────────────────────────────────────────────────────

    /** Get all editable text content entries. */
    @GetMapping("/content")
    public ResponseEntity<List<SiteContentDTO>> getContent() {
        return ResponseEntity.ok(siteContentService.getAllContent());
    }

    /**
     * Batch-update text content.
     * Request body: { "hero.title": "New Title", "about.para1": "Updated text", ... }
     */
    @PutMapping("/content")
    public ResponseEntity<?> updateContent(@RequestBody Map<String, String> updates) {
        try {
            List<SiteContentDTO> result = siteContentService.updateContent(updates);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error updating site content", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to update content"));
        }
    }

    // ── Images ─────────────────────────────────────────────────────────────────

    /** List all site images (metadata only, no binary data). */
    @GetMapping("/images")
    public ResponseEntity<List<SiteImageDTO>> getAllImages() {
        return ResponseEntity.ok(siteImageService.getAll());
    }

    /** List site images filtered by type. */
    @GetMapping("/images/type/{imageType}")
    public ResponseEntity<List<SiteImageDTO>> getImagesByType(@PathVariable String imageType) {
        try {
            ImageType type = ImageType.valueOf(imageType.toUpperCase());
            return ResponseEntity.ok(siteImageService.getByType(type));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Upload a new site image.
     *
     * @param file      multipart image file
     * @param imageType one of HERO, ABOUT, GALLERY, LOGO
     * @param altText   (optional) alt text for accessibility
     * @param sortOrder (optional) display order within the type group
     */
    @PostMapping("/images/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("imageType") String imageType,
            @RequestParam(value = "altText", required = false, defaultValue = "") String altText,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "0") Integer sortOrder) {
        try {
            ImageType type = ImageType.valueOf(imageType.toUpperCase());
            SiteImageDTO dto = siteImageService.upload(file, type, altText, sortOrder);
            return ResponseEntity.ok(Map.of("success", true, "image", dto));
        } catch (IllegalArgumentException e) {
            log.warn("Validation error uploading site image: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            log.error("IO error uploading site image", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Failed to process image"));
        }
    }

    /**
     * Update the alt text / sort order of an existing site image.
     * Request body: { "altText": "...", "sortOrder": 2 }
     */
    @PutMapping("/images/{id}")
    public ResponseEntity<?> updateImageMeta(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            String altText = body.containsKey("altText") ? (String) body.get("altText") : null;
            Integer sortOrder = body.containsKey("sortOrder")
                    ? ((Number) body.get("sortOrder")).intValue() : null;
            SiteImageDTO dto = siteImageService.updateMeta(id, altText, sortOrder);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Delete a site image. */
    @DeleteMapping("/images/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        try {
            siteImageService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Image deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
