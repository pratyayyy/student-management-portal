package com.ija.student_management_portal.service;

import com.ija.student_management_portal.config.FileStorageConfig;
import com.ija.student_management_portal.entity.StudentProfilePicture;
import com.ija.student_management_portal.repository.StudentProfilePictureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;

/**
 * Service that stores, retrieves and deletes student profile pictures in the database.
 * Images are compressed (max 800×800, JPEG quality 0.75) before storage to minimise
 * the data footprint.  Replaces the previous local-filesystem approach.
 */
@Service
@Slf4j
public class ProfilePictureService {

    private static final int MAX_DIMENSION = 800;
    private static final float JPEG_QUALITY = 0.75f;

    @Autowired
    private StudentProfilePictureRepository repository;

    @Autowired
    private FileStorageConfig fileStorageConfig;

    /**
     * Validate, compress and persist a profile picture for the given student.
     * If the student already has a picture row it is updated (upsert semantics).
     *
     * @param file      uploaded image
     * @param studentId owning student
     * @return persisted {@link StudentProfilePicture}
     * @throws IOException              on image I/O failure
     * @throws IllegalArgumentException on validation failure
     */
    @Transactional
    public StudentProfilePicture storeProfilePicture(MultipartFile file, String studentId) throws IOException {
        validateFile(file);

        byte[] originalBytes = file.getBytes();
        byte[] compressedBytes = compressToJpeg(originalBytes);

        log.info("Profile picture compressed for student {}: {} bytes -> {} bytes",
                studentId, originalBytes.length, compressedBytes.length);

        StudentProfilePicture picture = repository.findByStudentId(studentId)
                .orElse(new StudentProfilePicture());

        picture.setStudentId(studentId);
        picture.setImageData(compressedBytes);
        picture.setContentType("image/jpeg");
        picture.setOriginalFileName(file.getOriginalFilename());
        picture.setOriginalSize((long) originalBytes.length);
        picture.setCompressedSize((long) compressedBytes.length);
        picture.setUploadedAt(LocalDateTime.now());

        return repository.save(picture);
    }

    /**
     * Delete the profile picture row for the given student.
     * No-op when no row exists.
     *
     * @param studentId owning student
     */
    @Transactional
    public void deleteProfilePicture(String studentId) {
        if (repository.existsByStudentId(studentId)) {
            repository.deleteByStudentId(studentId);
            log.info("Deleted profile picture for student: {}", studentId);
        }
    }

    /**
     * Retrieve the stored profile picture for a student.
     *
     * @param studentId owning student
     * @return {@link Optional} containing the picture entity, or empty if not stored
     */
    public Optional<StudentProfilePicture> getProfilePicture(String studentId) {
        return repository.findByStudentId(studentId);
    }

    /**
     * Check whether a profile picture exists for the given student.
     *
     * @param studentId owning student
     * @return {@code true} if a picture row exists
     */
    public boolean hasProfilePicture(String studentId) {
        return repository.existsByStudentId(studentId);
    }

    // ── Compression ────────────────────────────────────────────────────────────

    /**
     * Compress and optionally resize an image to JPEG with {@value #JPEG_QUALITY} quality.
     * Either dimension is capped at {@value #MAX_DIMENSION} pixels while preserving aspect ratio.
     */
    private byte[] compressToJpeg(byte[] originalBytes) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (image == null) {
            throw new IOException("Unable to decode image – unsupported or corrupt format");
        }

        image = resizeIfNeeded(image);
        image = ensureRgb(image);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG ImageWriter available on this JVM");
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(JPEG_QUALITY);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }

        return baos.toByteArray();
    }

    private BufferedImage resizeIfNeeded(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= MAX_DIMENSION && height <= MAX_DIMENSION) {
            return image;
        }
        double scale = Math.min((double) MAX_DIMENSION / width, (double) MAX_DIMENSION / height);
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return resized;
    }

    /** JPEG does not support an alpha channel; convert images that have one. */
    private BufferedImage ensureRgb(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }
        BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rgb;
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or missing");
        }

        if (file.getSize() > fileStorageConfig.getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of "
                    + (fileStorageConfig.getMaxFileSize() / (1024 * 1024)) + "MB");
        }

        String fileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(fileName);
        if (!fileStorageConfig.getAllowedExtensions().contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: "
                    + fileStorageConfig.getAllowedExtensions());
        }

        String contentType = file.getContentType();
        if (contentType == null || !fileStorageConfig.getAllowedMimeTypes().contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only image files are allowed");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
