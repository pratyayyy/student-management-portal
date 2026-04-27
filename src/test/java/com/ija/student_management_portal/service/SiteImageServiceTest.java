package com.ija.student_management_portal.service;

import com.ija.student_management_portal.config.FileStorageConfig;
import com.ija.student_management_portal.dto.SiteImageDTO;
import com.ija.student_management_portal.entity.SiteImage;
import com.ija.student_management_portal.entity.SiteImage.ImageType;
import com.ija.student_management_portal.repository.SiteImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SiteImageService Tests")
public class SiteImageServiceTest {

    @Mock
    private SiteImageRepository repository;

    @Mock
    private FileStorageConfig fileStorageConfig;

    @InjectMocks
    private SiteImageService siteImageService;

    private byte[] sampleJpegBytes;

    @BeforeEach
    public void setUp() throws IOException {
        BufferedImage img = new BufferedImage(200, 150, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 200, 150);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpeg", baos);
        sampleJpegBytes = baos.toByteArray();
    }

    // ── upload ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("upload – stores compressed image and returns DTO")
    public void testUpload_Success() throws IOException {
        when(fileStorageConfig.getMaxFileSize()).thenReturn(5_242_880L);
        when(fileStorageConfig.getAllowedExtensions()).thenReturn(Arrays.asList("jpg", "jpeg", "png", "gif"));
        when(fileStorageConfig.getAllowedMimeTypes()).thenReturn(Arrays.asList("image/jpeg", "image/png", "image/gif"));

        SiteImage saved = new SiteImage();
        saved.setId(1L);
        saved.setImageType(ImageType.HERO);
        saved.setContentType("image/jpeg");
        saved.setOriginalFileName("hero.jpg");
        saved.setAltText("Hero banner");
        saved.setSortOrder(0);
        saved.setOriginalSize((long) sampleJpegBytes.length);
        saved.setCompressedSize(1000L);
        saved.setUploadedAt(LocalDateTime.now());
        saved.setImageData(new byte[]{1, 2, 3});

        when(repository.save(any(SiteImage.class))).thenReturn(saved);

        MockMultipartFile file = new MockMultipartFile(
                "file", "hero.jpg", "image/jpeg", sampleJpegBytes);

        SiteImageDTO dto = siteImageService.upload(file, ImageType.HERO, "Hero banner", 0);

        assertNotNull(dto);
        assertEquals("HERO", dto.getImageType());
        assertTrue(dto.getFileUrl().contains("/api/public/website/images/file/"));
        verify(repository, times(1)).save(any(SiteImage.class));
    }

    @Test
    @DisplayName("upload – empty file throws IllegalArgumentException")
    public void testUpload_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "hero.jpg", "image/jpeg", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> siteImageService.upload(emptyFile, ImageType.HERO, "", 0));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("upload – oversized file throws IllegalArgumentException")
    public void testUpload_OversizedFile() {
        when(fileStorageConfig.getMaxFileSize()).thenReturn(10L);

        MockMultipartFile bigFile = new MockMultipartFile(
                "file", "hero.jpg", "image/jpeg", new byte[100]);

        assertThrows(IllegalArgumentException.class,
                () -> siteImageService.upload(bigFile, ImageType.HERO, "", 0));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("upload – disallowed extension throws IllegalArgumentException")
    public void testUpload_DisallowedExtension() {
        when(fileStorageConfig.getMaxFileSize()).thenReturn(5_242_880L);
        when(fileStorageConfig.getAllowedExtensions()).thenReturn(Arrays.asList("jpg", "jpeg", "png", "gif"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "script.exe", "application/octet-stream", new byte[100]);

        assertThrows(IllegalArgumentException.class,
                () -> siteImageService.upload(file, ImageType.GALLERY, "", 0));
        verify(repository, never()).save(any());
    }

    // ── getByType ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByType – returns DTOs for the given image type")
    public void testGetByType() {
        SiteImage img = new SiteImage();
        img.setId(1L);
        img.setImageType(ImageType.GALLERY);
        img.setContentType("image/jpeg");
        img.setOriginalFileName("gallery1.jpg");
        img.setSortOrder(0);
        img.setUploadedAt(LocalDateTime.now());

        when(repository.findByImageTypeOrderBySortOrderAscUploadedAtAsc(ImageType.GALLERY))
                .thenReturn(List.of(img));

        List<SiteImageDTO> result = siteImageService.getByType(ImageType.GALLERY);

        assertEquals(1, result.size());
        assertEquals("GALLERY", result.get(0).getImageType());
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete – removes existing image")
    public void testDelete_Exists() {
        when(repository.existsById(5L)).thenReturn(true);
        doNothing().when(repository).deleteById(5L);

        siteImageService.delete(5L);

        verify(repository, times(1)).deleteById(5L);
    }

    @Test
    @DisplayName("delete – throws when image not found")
    public void testDelete_NotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> siteImageService.delete(99L));
        verify(repository, never()).deleteById(any());
    }

    // ── updateMeta ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateMeta – updates alt text and sort order")
    public void testUpdateMeta() {
        SiteImage img = new SiteImage();
        img.setId(2L);
        img.setImageType(ImageType.ABOUT);
        img.setContentType("image/jpeg");
        img.setAltText("Old alt");
        img.setSortOrder(0);
        img.setUploadedAt(LocalDateTime.now());

        when(repository.findById(2L)).thenReturn(Optional.of(img));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SiteImageDTO dto = siteImageService.updateMeta(2L, "New alt", 3);

        assertEquals("New alt", img.getAltText());
        assertEquals(3, img.getSortOrder());
        assertNotNull(dto);
    }

    @Test
    @DisplayName("updateMeta – throws when image not found")
    public void testUpdateMeta_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> siteImageService.updateMeta(99L, "alt", 0));
    }

    // ── getRaw ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getRaw – returns entity when it exists")
    public void testGetRaw_Exists() {
        SiteImage img = new SiteImage();
        img.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(img));

        Optional<SiteImage> result = siteImageService.getRaw(1L);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("getRaw – returns empty when not found")
    public void testGetRaw_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Optional<SiteImage> result = siteImageService.getRaw(1L);

        assertFalse(result.isPresent());
    }
}
