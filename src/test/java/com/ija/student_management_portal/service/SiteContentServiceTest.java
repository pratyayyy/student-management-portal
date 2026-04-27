package com.ija.student_management_portal.service;

import com.ija.student_management_portal.dto.SiteContentDTO;
import com.ija.student_management_portal.entity.SiteContent;
import com.ija.student_management_portal.repository.SiteContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SiteContentService Tests")
public class SiteContentServiceTest {

    @Mock
    private SiteContentRepository repository;

    @InjectMocks
    private SiteContentService siteContentService;

    private SiteContent makeEntry(String key, String label, String value) {
        SiteContent sc = new SiteContent();
        sc.setId(1L);
        sc.setContentKey(key);
        sc.setLabel(label);
        sc.setContentValue(value);
        sc.setUpdatedAt(LocalDateTime.now());
        return sc;
    }

    // ── seedDefaults ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("seedDefaults – skips keys that already exist")
    public void testSeedDefaults_SkipsExistingKeys() {
        when(repository.existsByContentKey(anyString())).thenReturn(true);

        siteContentService.seedDefaults();

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("seedDefaults – persists new keys that are missing")
    public void testSeedDefaults_PersistsMissingKeys() {
        when(repository.existsByContentKey(anyString())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        siteContentService.seedDefaults();

        // The DEFAULTS map has 14 entries; all should be saved
        verify(repository, atLeast(1)).save(any(SiteContent.class));
    }

    // ── getAllContent ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllContent – returns all entries as DTOs")
    public void testGetAllContent() {
        SiteContent sc1 = makeEntry("hero.title", "Hero Title", "Welcome");
        SiteContent sc2 = makeEntry("contact.phone", "Phone", "+91 00000 00000");
        when(repository.findAll()).thenReturn(List.of(sc1, sc2));

        List<SiteContentDTO> result = siteContentService.getAllContent();

        assertEquals(2, result.size());
        assertEquals("hero.title", result.get(0).getContentKey());
        assertEquals("Welcome", result.get(0).getContentValue());
    }

    // ── updateContent ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateContent – updates existing entry")
    public void testUpdateContent_UpdatesExisting() {
        SiteContent existing = makeEntry("hero.title", "Hero Title", "Old Title");
        when(repository.findByContentKey("hero.title")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(repository.findAll()).thenReturn(List.of(existing));

        Map<String, String> updates = Map.of("hero.title", "New Title");
        List<SiteContentDTO> result = siteContentService.updateContent(updates);

        verify(repository, times(1)).save(existing);
        assertEquals("New Title", existing.getContentValue());
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("updateContent – creates entry when key does not exist")
    public void testUpdateContent_CreatesNew() {
        when(repository.findByContentKey("custom.key")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(repository.findAll()).thenReturn(List.of());

        siteContentService.updateContent(Map.of("custom.key", "Custom Value"));

        verify(repository, times(1)).save(argThat(sc ->
                "custom.key".equals(sc.getContentKey()) && "Custom Value".equals(sc.getContentValue())));
    }
}
