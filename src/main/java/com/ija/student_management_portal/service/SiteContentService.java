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
 * Manages editable text content for the promotional website.
 * Provides default seed values so the site is functional out of the box.
 */
@Service
@Slf4j
public class SiteContentService {

    /** Ordered map of key → {label, default value} used for seeding and UI labelling. */
    private static final Map<String, String[]> DEFAULTS = new LinkedHashMap<>();

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
    }

    /** Return all content entries as DTOs. */
    public List<SiteContentDTO> getAllContent() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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
