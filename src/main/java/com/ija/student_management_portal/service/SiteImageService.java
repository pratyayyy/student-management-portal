package com.ija.student_management_portal.service;

import com.ija.student_management_portal.config.FileStorageConfig;
import com.ija.student_management_portal.dto.SiteImageDTO;
import com.ija.student_management_portal.entity.SiteImage;
import com.ija.student_management_portal.entity.SiteImage.ImageType;
import com.ija.student_management_portal.repository.SiteImageRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages promotional website images uploaded by admins.
 * Images are compressed (max 1920×1080, JPEG quality 0.82) before storage.
 */
@Service
@Slf4j
public class SiteImageService {

    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;
    private static final float JPEG_QUALITY = 0.82f;

    @Autowired
    private SiteImageRepository repository;

    @Autowired
    private FileStorageConfig fileStorageConfig;

    /**
     * Validate, compress and persist a site image.
     *
     * @param file      uploaded image
     * @param imageType target section (HERO, ABOUT, GALLERY, LOGO)
     * @param altText   accessibility alt text
     * @param sortOrder display order within the type group
     * @return persisted image as DTO
     * @throws IOException              on image I/O failure
     * @throws IllegalArgumentException on validation failure
     */
    @Transactional
    public SiteImageDTO upload(MultipartFile file, ImageType imageType, String altText, Integer sortOrder)
            throws IOException {

        validateFile(file);

        byte[] originalBytes = file.getBytes();
        byte[] compressedBytes = compressToJpeg(originalBytes);

        log.info("Site image compressed [{}]: {} bytes -> {} bytes",
                imageType, originalBytes.length, compressedBytes.length);

        SiteImage image = new SiteImage();
        image.setImageType(imageType);
        image.setImageData(compressedBytes);
        image.setContentType("image/jpeg");
        image.setOriginalFileName(file.getOriginalFilename());
        image.setAltText(altText);
        image.setSortOrder(sortOrder != null ? sortOrder : 0);
        image.setOriginalSize((long) originalBytes.length);
        image.setCompressedSize((long) compressedBytes.length);
        image.setUploadedAt(LocalDateTime.now());

        SiteImage saved = repository.save(image);
        return toDTO(saved);
    }

    /** Return all images for the given type as DTOs (no binary data). */
    public List<SiteImageDTO> getByType(ImageType imageType) {
        return repository.findByImageTypeOrderBySortOrderAscUploadedAtAsc(imageType)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Return all images across all types as DTOs. */
    public List<SiteImageDTO> getAll() {
        return repository.findAllByOrderByImageTypeAscSortOrderAscUploadedAtAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Retrieve the raw image bytes for serving. */
    public Optional<SiteImage> getRaw(Long id) {
        return repository.findById(id);
    }

    /**
     * Update the alt text and sort order of an existing image.
     */
    @Transactional
    public SiteImageDTO updateMeta(Long id, String altText, Integer sortOrder) {
        SiteImage image = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + id));
        if (altText != null) image.setAltText(altText);
        if (sortOrder != null) image.setSortOrder(sortOrder);
        return toDTO(repository.save(image));
    }

    /** Delete a site image by ID. */
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Image not found: " + id);
        }
        repository.deleteById(id);
        log.info("Deleted site image id={}", id);
    }

    // ── Compression ────────────────────────────────────────────────────────────

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
        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
            return image;
        }
        double scale = Math.min((double) MAX_WIDTH / width, (double) MAX_HEIGHT / height);
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
        String ext = getFileExtension(fileName);
        if (!fileStorageConfig.getAllowedExtensions().contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: "
                    + fileStorageConfig.getAllowedExtensions());
        }
        String contentType = file.getContentType();
        if (contentType == null || !fileStorageConfig.getAllowedMimeTypes().contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only image files are allowed");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    // ── Mapping ────────────────────────────────────────────────────────────────

    private SiteImageDTO toDTO(SiteImage image) {
        SiteImageDTO dto = new SiteImageDTO();
        dto.setId(image.getId());
        dto.setImageType(image.getImageType().name());
        dto.setAltText(image.getAltText());
        dto.setSortOrder(image.getSortOrder());
        dto.setOriginalFileName(image.getOriginalFileName());
        dto.setOriginalSize(image.getOriginalSize());
        dto.setCompressedSize(image.getCompressedSize());
        dto.setUploadedAt(image.getUploadedAt());
        dto.setFileUrl("/api/public/website/images/file/" + image.getId());
        return dto;
    }
}
