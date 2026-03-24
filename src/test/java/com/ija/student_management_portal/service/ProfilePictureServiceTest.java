package com.ija.student_management_portal.service;

import com.ija.student_management_portal.config.FileStorageConfig;
import com.ija.student_management_portal.entity.StudentProfilePicture;
import com.ija.student_management_portal.repository.StudentProfilePictureRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfilePictureService Tests")
public class ProfilePictureServiceTest {

    @Mock
    private StudentProfilePictureRepository repository;

    @Mock
    private FileStorageConfig fileStorageConfig;

    @InjectMocks
    private ProfilePictureService profilePictureService;

    private byte[] sampleJpegBytes;

    @BeforeEach
    public void setUp() throws IOException {
        // Build a tiny valid JPEG in memory so ImageIO can decode it
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpeg", baos);
        sampleJpegBytes = baos.toByteArray();
    }

    // ── storeProfilePicture ────────────────────────────────────────────────────

    @Test
    @DisplayName("Store new profile picture – should compress and persist")
    public void testStoreProfilePicture_NewPicture() throws IOException {
        // Arrange
        when(fileStorageConfig.getMaxFileSize()).thenReturn(5_242_880L);
        when(fileStorageConfig.getAllowedExtensions()).thenReturn(Arrays.asList("jpg", "jpeg", "png", "gif"));
        when(fileStorageConfig.getAllowedMimeTypes()).thenReturn(Arrays.asList("image/jpeg", "image/png", "image/gif"));
        when(repository.findByStudentId("2025-0001")).thenReturn(Optional.empty());

        StudentProfilePicture saved = new StudentProfilePicture();
        saved.setStudentId("2025-0001");
        saved.setContentType("image/jpeg");
        when(repository.save(any(StudentProfilePicture.class))).thenReturn(saved);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", sampleJpegBytes);

        // Act
        StudentProfilePicture result = profilePictureService.storeProfilePicture(file, "2025-0001");

        // Assert
        assertNotNull(result);
        assertEquals("2025-0001", result.getStudentId());
        verify(repository, times(1)).save(any(StudentProfilePicture.class));
    }

    @Test
    @DisplayName("Store profile picture – replaces existing row (upsert)")
    public void testStoreProfilePicture_UpdateExisting() throws IOException {
        // Arrange
        when(fileStorageConfig.getMaxFileSize()).thenReturn(5_242_880L);
        when(fileStorageConfig.getAllowedExtensions()).thenReturn(Arrays.asList("jpg", "jpeg", "png", "gif"));
        when(fileStorageConfig.getAllowedMimeTypes()).thenReturn(Arrays.asList("image/jpeg", "image/png", "image/gif"));

        StudentProfilePicture existing = new StudentProfilePicture();
        existing.setStudentId("2025-0001");
        when(repository.findByStudentId("2025-0001")).thenReturn(Optional.of(existing));
        when(repository.save(any(StudentProfilePicture.class))).thenReturn(existing);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", sampleJpegBytes);

        // Act
        profilePictureService.storeProfilePicture(file, "2025-0001");

        // Assert: save called once on the existing entity
        verify(repository, times(1)).save(existing);
    }

    @Test
    @DisplayName("Store profile picture – empty file should throw IllegalArgumentException")
    public void testStoreProfilePicture_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> profilePictureService.storeProfilePicture(emptyFile, "2025-0001"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Store profile picture – oversized file should throw IllegalArgumentException")
    public void testStoreProfilePicture_OversizedFile() {
        when(fileStorageConfig.getMaxFileSize()).thenReturn(10L); // 10 bytes limit

        MockMultipartFile bigFile = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[100]);

        assertThrows(IllegalArgumentException.class,
                () -> profilePictureService.storeProfilePicture(bigFile, "2025-0001"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Store profile picture – disallowed extension should throw IllegalArgumentException")
    public void testStoreProfilePicture_DisallowedExtension() {
        when(fileStorageConfig.getMaxFileSize()).thenReturn(5_242_880L);
        when(fileStorageConfig.getAllowedExtensions()).thenReturn(Arrays.asList("jpg", "jpeg", "png", "gif"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "virus.exe", "application/octet-stream", new byte[100]);

        assertThrows(IllegalArgumentException.class,
                () -> profilePictureService.storeProfilePicture(file, "2025-0001"));
        verify(repository, never()).save(any());
    }

    // ── deleteProfilePicture ───────────────────────────────────────────────────

    @Test
    @DisplayName("Delete existing profile picture – should remove row")
    public void testDeleteProfilePicture_Exists() {
        when(repository.existsByStudentId("2025-0001")).thenReturn(true);
        doNothing().when(repository).deleteByStudentId("2025-0001");

        profilePictureService.deleteProfilePicture("2025-0001");

        verify(repository, times(1)).deleteByStudentId("2025-0001");
    }

    @Test
    @DisplayName("Delete profile picture when none exists – no-op")
    public void testDeleteProfilePicture_NotExists() {
        when(repository.existsByStudentId("2025-0001")).thenReturn(false);

        profilePictureService.deleteProfilePicture("2025-0001");

        verify(repository, never()).deleteByStudentId(anyString());
    }

    // ── getProfilePicture ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Get profile picture – returns entity when it exists")
    public void testGetProfilePicture_Exists() {
        StudentProfilePicture pic = new StudentProfilePicture();
        pic.setStudentId("2025-0001");
        pic.setImageData(sampleJpegBytes);
        pic.setContentType("image/jpeg");
        when(repository.findByStudentId("2025-0001")).thenReturn(Optional.of(pic));

        Optional<StudentProfilePicture> result = profilePictureService.getProfilePicture("2025-0001");

        assertTrue(result.isPresent());
        assertEquals("2025-0001", result.get().getStudentId());
    }

    @Test
    @DisplayName("Get profile picture – returns empty when none stored")
    public void testGetProfilePicture_NotExists() {
        when(repository.findByStudentId("2025-0001")).thenReturn(Optional.empty());

        Optional<StudentProfilePicture> result = profilePictureService.getProfilePicture("2025-0001");

        assertFalse(result.isPresent());
    }

    // ── hasProfilePicture ──────────────────────────────────────────────────────

    @Test
    @DisplayName("hasProfilePicture – returns true when row exists")
    public void testHasProfilePicture_True() {
        when(repository.existsByStudentId("2025-0001")).thenReturn(true);
        assertTrue(profilePictureService.hasProfilePicture("2025-0001"));
    }

    @Test
    @DisplayName("hasProfilePicture – returns false when no row")
    public void testHasProfilePicture_False() {
        when(repository.existsByStudentId("2025-0001")).thenReturn(false);
        assertFalse(profilePictureService.hasProfilePicture("2025-0001"));
    }
}
