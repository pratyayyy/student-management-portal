package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.SiteContentDTO;
import com.ija.student_management_portal.dto.SiteImageDTO;
import com.ija.student_management_portal.entity.SiteImage;
import com.ija.student_management_portal.entity.SiteImage.ImageType;
import com.ija.student_management_portal.service.SiteContentService;
import com.ija.student_management_portal.service.SiteImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Public REST endpoints consumed by the promotional website.
 * No authentication required (permitted in SecurityConfig).
 */
@RestController
@RequestMapping("/api/public/website")
@Slf4j
public class ApiPublicWebsiteController {

    @Autowired
    private SiteContentService siteContentService;

    @Autowired
    private SiteImageService siteImageService;

    /** Fetch all text content as a list of key/value DTOs. */
    @GetMapping("/content")
    public ResponseEntity<List<SiteContentDTO>> getContent() {
        return ResponseEntity.ok(siteContentService.getAllContent());
    }

    /** Fetch feature toggles as featureName → enabled map. */
    @GetMapping("/features")
    public ResponseEntity<Map<String, Boolean>> getFeatures() {
        return ResponseEntity.ok(siteContentService.getAllFeatures());
    }

    /** Fetch combined config (featureConfig + contentConfig) for the promo site. */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(siteContentService.getPublicConfig());
    }

    /** Fetch images for a specific section type (HERO, ABOUT, GALLERY, LOGO). */
    @GetMapping("/images/{imageType}")
    public ResponseEntity<List<SiteImageDTO>> getImages(@PathVariable String imageType) {
        try {
            ImageType type = ImageType.valueOf(imageType.toUpperCase());
            return ResponseEntity.ok(siteImageService.getByType(type));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** Serve the raw image file by ID. */
    @GetMapping("/images/file/{id}")
    public ResponseEntity<byte[]> getImageFile(@PathVariable Long id) {
        return siteImageService.getRaw(id)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(img.getContentType()))
                        .body(img.getImageData()))
                .orElse(ResponseEntity.notFound().build());
    }
}
