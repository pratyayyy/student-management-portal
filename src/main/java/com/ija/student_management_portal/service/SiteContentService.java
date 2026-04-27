package com.ija.student_management_portal.service;

import com.ija.student_management_portal.dto.SiteContentDTO;
import com.ija.student_management_portal.entity.SiteContent;
import com.ija.student_management_portal.repository.SiteContentRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages editable text content and feature toggles for the promotional website.
 * Provides default seed values so the site is functional out of the box.
 */
@Service
@Slf4j
public class SiteContentService {

    /** Ordered map of key → {label, default value} used for seeding and UI labelling. */
    private static final Map<String, String[]> DEFAULTS = new LinkedHashMap<>();

    /** Feature toggle keys with labels and default enabled state ("true"/"false"). */
    private static final Map<String, String[]> FEATURE_DEFAULTS = new LinkedHashMap<>();

    static {
        // hero section
        DEFAULTS.put("hero.title",       new String[]{"Hero Title",          "Institute of Junior Accountants"});
        DEFAULTS.put("hero.subtitle",    new String[]{"Hero Subtitle",       "Shaping tomorrow's accounting professionals with world-class education and practical training."});
        DEFAULTS.put("hero.cta",         new String[]{"Hero CTA Button",     "Enquire Now"});

        // about section
        DEFAULTS.put("about.heading",    new String[]{"About Heading",       "About IJA"});
        DEFAULTS.put("about.para1",      new String[]{"About Paragraph 1",  "Founded with a vision to deliver excellence in accounting education, the Institute of Junior Accountants has been guiding students toward successful careers for over two decades."});
        DEFAULTS.put("about.para2",      new String[]{"About Paragraph 2",  "Our experienced faculty, modern curriculum, and hands-on training programs ensure that every graduate is industry-ready."});
        DEFAULTS.put("about.mission",    new String[]{"Mission Statement",   "To provide accessible, high-quality accounting education that empowers students to excel in the financial world."});

        // contact section
        DEFAULTS.put("contact.phone",    new String[]{"Contact Phone",       "+91 00000 00000"});
        DEFAULTS.put("contact.email",    new String[]{"Contact Email",       "info@ija.edu"});
        DEFAULTS.put("contact.address",  new String[]{"Contact Address",     "123 Main Street, Your City, State – 000000"});
        DEFAULTS.put("contact.hours",    new String[]{"Office Hours",        "Mon – Sat: 9 AM – 6 PM"});

        // stats / highlights
        DEFAULTS.put("stats.students",   new String[]{"Stat: Students",     "2000+"});
        DEFAULTS.put("stats.experience", new String[]{"Stat: Years Experience", "20+"});
        DEFAULTS.put("stats.placement",  new String[]{"Stat: Placement Rate", "95%"});

        // JSON content blocks for complex sections
        DEFAULTS.put("json.courses",       new String[]{"Courses JSON",       "[]"});
        DEFAULTS.put("json.faculty",       new String[]{"Faculty JSON",       "[]"});
        DEFAULTS.put("json.testimonials",  new String[]{"Testimonials JSON",  "[]"});
        DEFAULTS.put("json.blog",          new String[]{"Blog Posts JSON",    "[]"});
        DEFAULTS.put("json.results",       new String[]{"Results JSON",       "[]"});
        DEFAULTS.put("json.faq",           new String[]{"FAQ JSON",           "[]"});

        // Feature toggles (stored as feature.<section> = "true"/"false")
        FEATURE_DEFAULTS.put("feature.hero",        new String[]{"Hero Section",       "true"});
        FEATURE_DEFAULTS.put("feature.carousel",    new String[]{"Carousel",           "true"});
        FEATURE_DEFAULTS.put("feature.about",       new String[]{"About Section",      "true"});
        FEATURE_DEFAULTS.put("feature.whyChooseUs", new String[]{"Why Choose Us",      "true"});
        FEATURE_DEFAULTS.put("feature.courses",     new String[]{"Courses Section",    "true"});
        FEATURE_DEFAULTS.put("feature.results",     new String[]{"Results Section",    "true"});
        FEATURE_DEFAULTS.put("feature.faculty",     new String[]{"Faculty Section",    "true"});
        FEATURE_DEFAULTS.put("feature.testimonials",new String[]{"Testimonials",       "true"});
        FEATURE_DEFAULTS.put("feature.blog",        new String[]{"Blog Section",       "true"});
        FEATURE_DEFAULTS.put("feature.resources",   new String[]{"Resources Section",  "true"});
        FEATURE_DEFAULTS.put("feature.demoBooking", new String[]{"Demo Booking",       "true"});
        FEATURE_DEFAULTS.put("feature.scholarship", new String[]{"Scholarship Section","true"});
        FEATURE_DEFAULTS.put("feature.faq",         new String[]{"FAQ Section",        "true"});
        FEATURE_DEFAULTS.put("feature.contact",     new String[]{"Contact Section",    "true"});
        FEATURE_DEFAULTS.put("feature.whatsapp",    new String[]{"WhatsApp Button",    "true"});
        FEATURE_DEFAULTS.put("feature.leadForms",   new String[]{"Lead Forms",         "true"});
        FEATURE_DEFAULTS.put("feature.login",       new String[]{"Login Link",         "true"});
    }

    @Autowired
    private SiteContentRepository repository;

    /**
     * Seed default values on first startup.  If a key already exists it is left unchanged.
     */
    @PostConstruct
    @Transactional
    public void seedDefaults() {
        DEFAULTS.forEach((key, meta) -> {
            if (!repository.existsByContentKey(key)) {
                SiteContent sc = new SiteContent();
                sc.setContentKey(key);
                sc.setLabel(meta[0]);
                sc.setContentValue(meta[1]);
                sc.setUpdatedAt(LocalDateTime.now());
                repository.save(sc);
                log.debug("Seeded site content: {}", key);
            }
        });
        FEATURE_DEFAULTS.forEach((key, meta) -> {
            if (!repository.existsByContentKey(key)) {
                SiteContent sc = new SiteContent();
                sc.setContentKey(key);
                sc.setLabel(meta[0]);
                sc.setContentValue(meta[1]);
                sc.setUpdatedAt(LocalDateTime.now());
                repository.save(sc);
                log.debug("Seeded feature toggle: {}", key);
            }
        });
    }

    /** Return all content entries as DTOs. */
    public List<SiteContentDTO> getAllContent() {
        return repository.findAll().stream()
                .filter(sc -> !sc.getContentKey().startsWith("feature."))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Return all feature toggles as a map of featureName → enabled.
     * The feature name is the part after "feature." in the key.
     */
    public Map<String, Boolean> getAllFeatures() {
        Map<String, Boolean> features = new LinkedHashMap<>();
        FEATURE_DEFAULTS.keySet().forEach(key -> {
            repository.findByContentKey(key).ifPresentOrElse(
                sc -> features.put(key.substring("feature.".length()), "true".equalsIgnoreCase(sc.getContentValue())),
                () -> features.put(key.substring("feature.".length()), true)
            );
        });
        return features;
    }

    /**
     * Update feature toggles in bulk.
     *
     * @param toggles map of featureName (without "feature." prefix) → boolean
     * @return updated features map
     */
    @Transactional
    public Map<String, Boolean> updateFeatures(Map<String, Boolean> toggles) {
        LocalDateTime now = LocalDateTime.now();
        toggles.forEach((name, enabled) -> {
            String key = "feature." + name;
            SiteContent sc = repository.findByContentKey(key)
                    .orElseGet(() -> {
                        SiteContent n = new SiteContent();
                        n.setContentKey(key);
                        String[] meta = FEATURE_DEFAULTS.get(key);
                        n.setLabel(meta != null ? meta[0] : name);
                        return n;
                    });
            sc.setContentValue(enabled ? "true" : "false");
            sc.setUpdatedAt(now);
            repository.save(sc);
        });
        return getAllFeatures();
    }

    /**
     * Return the combined public site config (features + content) as raw maps,
     * suitable for the promo site to consume.
     */
    public Map<String, Object> getPublicConfig() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("featureConfig", getAllFeatures());
        // Build flat key/value map for content
        Map<String, String> contentMap = new LinkedHashMap<>();
        repository.findAll().stream()
                .filter(sc -> !sc.getContentKey().startsWith("feature."))
                .forEach(sc -> contentMap.put(sc.getContentKey(), sc.getContentValue()));
        result.put("contentConfig", contentMap);
        return result;
    }

    /**
     * Upsert a batch of content entries.
     *
     * @param updates map of contentKey → contentValue
     * @return updated entries as DTOs
     */
    @Transactional
    public List<SiteContentDTO> updateContent(Map<String, String> updates) {
        LocalDateTime now = LocalDateTime.now();
        updates.forEach((key, value) -> {
            SiteContent sc = repository.findByContentKey(key)
                    .orElseGet(() -> {
                        SiteContent n = new SiteContent();
                        n.setContentKey(key);
                        // try to set a label from defaults, fall back to the key itself
                        String[] meta = DEFAULTS.get(key);
                        n.setLabel(meta != null ? meta[0] : key);
                        return n;
                    });
            sc.setContentValue(value);
            sc.setUpdatedAt(now);
            repository.save(sc);
        });
        return getAllContent();
    }

    // ── Mapping ────────────────────────────────────────────────────────────────

    private SiteContentDTO toDTO(SiteContent sc) {
        SiteContentDTO dto = new SiteContentDTO();
        dto.setId(sc.getId());
        dto.setContentKey(sc.getContentKey());
        dto.setContentValue(sc.getContentValue());
        dto.setLabel(sc.getLabel());
        dto.setUpdatedAt(sc.getUpdatedAt());
        return dto;
    }
}
