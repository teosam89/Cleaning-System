package com.cleaningsystem.backend.service;

import com.cleaningsystem.backend.entity.Image;
import com.cleaningsystem.backend.repository.ImageRepository;
import com.cleaningsystem.backend.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class ImageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    
    // Allowed file types
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif"
    );
    
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif"
    );
    
    // File size limits
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2MB for profile avatars
    private static final long MAX_TOTAL_SIZE_PER_ENTITY = 50 * 1024 * 1024; // 50MB per entity
    
    // Avatar-specific constraints
    private static final List<String> AVATAR_ALLOWED_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png"
    );
    private static final List<String> AVATAR_ALLOWED_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png"
    );
    
    // Security patterns
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    
    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;
    
    @Value("${app.upload.max-files-per-request:5}")
    private int maxFilesPerRequest;
    
    private final ImageRepository imageRepository;
    
    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }
    
    /**
     * Upload multiple files for a specific entity
     */
    public List<Image> uploadFiles(List<MultipartFile> files, Image.EntityType entityType, 
                                  Long entityId, Long uploadedBy) {
        logger.info("Starting file upload: {} files for entity {}:{}", files.size(), entityType, entityId);
        
        validateUploadRequest(files, entityType, entityId);
        
        // Check total size limit for entity
        Long currentTotalSize = imageRepository.getTotalFileSizeByEntity(entityType, entityId);
        long newFilesTotalSize = files.stream().mapToLong(MultipartFile::getSize).sum();
        
        if (currentTotalSize != null && (currentTotalSize + newFilesTotalSize) > MAX_TOTAL_SIZE_PER_ENTITY) {
            throw new BusinessException("Total file size would exceed limit of 50MB for this entity");
        }
        
        // Create directory structure
        Path entityDir = createEntityDirectory(entityType);
        
        return files.stream().map(file -> {
            try {
                return uploadSingleFile(file, entityType, entityId, uploadedBy, entityDir);
            } catch (Exception e) {
                logger.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                throw new BusinessException("Failed to upload file: " + file.getOriginalFilename());
            }
        }).toList();
    }
    
    /**
     * Upload avatar for user profile (single file, replaces existing)
     */
    public Image uploadProfileAvatar(MultipartFile file, Long userId, Long uploadedBy) {
        logger.info("Starting avatar upload for user: {}", userId);
        
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Avatar file is required");
        }
        
        validateAvatarFile(file);
        
        // Remove existing avatar for this user (soft delete)
        List<Image> existingAvatars = imageRepository.findByEntityTypeAndEntityIdAndNotDeleted(
            Image.EntityType.PROFILE, userId);
        for (Image existingAvatar : existingAvatars) {
            existingAvatar.setIsDeleted(true);
            imageRepository.save(existingAvatar);
            logger.info("Removed existing avatar: {} for user: {}", existingAvatar.getStoredName(), userId);
        }
        
        // Create directory structure for profile images
        Path entityDir = createEntityDirectory(Image.EntityType.PROFILE);
        
        try {
            return uploadSingleFile(file, Image.EntityType.PROFILE, userId, uploadedBy, entityDir);
        } catch (Exception e) {
            logger.error("Failed to upload avatar for user: {}", userId, e);
            throw new BusinessException("Failed to upload avatar: " + e.getMessage());
        }
    }
    
    /**
     * Upload a single file
     */
    private Image uploadSingleFile(MultipartFile file, Image.EntityType entityType, 
                                  Long entityId, Long uploadedBy, Path entityDir) throws IOException {
        
        // Use appropriate validation based on entity type
        if (entityType == Image.EntityType.PROFILE) {
            validateAvatarFile(file);
        } else {
            validateSingleFile(file);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        String storedName = String.format("%s_%s_%s_%s.%s", 
            entityType.getValue(), entityId, uuid, timestamp, fileExtension);
        
        // Create file path
        Path filePath = entityDir.resolve(storedName);
        
        // Ensure the directory exists
        Files.createDirectories(filePath.getParent());
        
        // Copy file to destination
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Generate public URL
        String publicUrl = String.format("/api/files/%s/%s", entityType.getValue(), storedName);
        
        // Create and save image record
        Image image = new Image(
            entityType,
            entityId,
            originalFilename,
            storedName,
            filePath.toString(),
            publicUrl,
            file.getSize(),
            file.getContentType(),
            uploadedBy
        );
        
        Image savedImage = imageRepository.save(image);
        logger.info("Successfully uploaded file: {} -> {}", originalFilename, storedName);
        
        return savedImage;
    }
    
    /**
     * Get file as Resource for serving
     */
    public Resource loadFileAsResource(String fileName, Image.EntityType entityType) {
        try {
            // Find image in database first (security check)
            Image image = imageRepository.findByStoredNameAndNotDeleted(fileName)
                .orElseThrow(() -> new BusinessException("File not found: " + fileName));

            if (!image.getEntityType().equals(entityType)) {
                throw new BusinessException("File type mismatch");
            }

            Path filePath = Paths.get(image.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                // File exists in database but not on filesystem - clean up orphaned record
                logger.warn("Found orphaned image record in database - file missing: {} (ID: {})", fileName, image.getImageId());
                image.setIsDeleted(true);
                imageRepository.save(image);
                throw new BusinessException("File not found or not readable: " + fileName);
            }
        } catch (MalformedURLException e) {
            logger.error("Error loading file as resource: {}", fileName, e);
            throw new BusinessException("Error loading file: " + fileName);
        }
    }
    
    /**
     * Get all images for an entity
     */
    public List<Image> getImagesForEntity(Image.EntityType entityType, Long entityId) {
        return imageRepository.findByEntityTypeAndEntityIdAndNotDeleted(entityType, entityId);
    }
    
    /**
     * Delete an image (soft delete)
     */
    public boolean deleteImage(Long imageId, Long userId) {
        Image image = imageRepository.findById(imageId)
            .orElseThrow(() -> new BusinessException("Image not found"));
        
        // Security check - only creator or admin can delete
        if (!image.getCreatedBy().equals(userId)) {
            // Additional check for admin role would go here if needed
            throw new BusinessException("Not authorized to delete this image");
        }
        
        image.setIsDeleted(true);
        imageRepository.save(image);
        
        logger.info("Soft deleted image: {} by user: {}", imageId, userId);
        return true;
    }
    
    /**
     * Delete all images for an entity (when entity is deleted)
     */
    public void deleteImagesForEntity(Image.EntityType entityType, Long entityId) {
        imageRepository.softDeleteByEntity(entityType, entityId);
        logger.info("Soft deleted all images for entity: {}:{}", entityType, entityId);
    }
    
    /**
     * Physically delete old soft-deleted files (cleanup job)
     */
    public void cleanupDeletedFiles() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Image> deletedImages = imageRepository.findDeletedImagesOlderThan(cutoffDate);
        
        for (Image image : deletedImages) {
            try {
                Path filePath = Paths.get(image.getFilePath());
                Files.deleteIfExists(filePath);
                imageRepository.delete(image);
                logger.info("Physically deleted file: {}", image.getFilePath());
            } catch (IOException e) {
                logger.error("Failed to delete file: {}", image.getFilePath(), e);
            }
        }
    }
    
    /**
     * Get image metadata by ID
     */
    public Image getImageById(Long imageId) {
        return imageRepository.findById(imageId)
            .filter(image -> !image.getIsDeleted())
            .orElseThrow(() -> new BusinessException("Image not found"));
    }
    
    // Private helper methods
    
    private void validateUploadRequest(List<MultipartFile> files, Image.EntityType entityType, Long entityId) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException("No files provided");
        }
        
        if (files.size() > maxFilesPerRequest) {
            throw new BusinessException("Too many files. Maximum " + maxFilesPerRequest + " files allowed per request");
        }
        
        if (entityType == null || entityId == null) {
            throw new BusinessException("Entity type and ID are required");
        }
    }
    
    private void validateSingleFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("File size exceeds maximum limit of 5MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("Invalid filename");
        }
        
        // Check file extension
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }
        
        // Check MIME type
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            throw new BusinessException("Invalid file type. Allowed types: JPEG, PNG, GIF");
        }
        
        // Security: Check for path traversal attempts
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new BusinessException("Invalid filename contains illegal characters");
        }
    }
    
    /**
     * Validate avatar file with stricter requirements
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("Avatar file is empty");
        }
        
        // Stricter size limit for avatars
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException("Avatar file size exceeds maximum limit of 2MB");
        }
        
        // Minimum size check for avatars (at least 1KB to ensure it's a valid image)
        if (file.getSize() < 1024) {
            throw new BusinessException("Avatar file is too small. Minimum size is 1KB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("Invalid avatar filename");
        }
        
        // Check file extension (stricter for avatars - no GIF)
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!AVATAR_ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Avatar file type not allowed. Allowed types: JPG, JPEG, PNG");
        }
        
        // Check MIME type (stricter for avatars)
        String mimeType = file.getContentType();
        if (mimeType == null || !AVATAR_ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            throw new BusinessException("Invalid avatar file type. Only JPEG and PNG images are allowed");
        }
        
        // Security: Check for path traversal attempts
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new BusinessException("Invalid avatar filename contains illegal characters");
        }
        
        // Additional security: Check for suspicious file names
        String baseFilename = originalFilename.toLowerCase();
        if (baseFilename.contains("script") || baseFilename.contains("exe") || 
            baseFilename.contains("php") || baseFilename.contains("jsp")) {
            throw new BusinessException("Suspicious avatar filename not allowed");
        }
        
        logger.debug("Avatar file validation passed: {} ({})", originalFilename, file.getSize());
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException("File must have an extension");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    
    private Path createEntityDirectory(Image.EntityType entityType) {
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        
        Path entityDir = Paths.get(uploadDir, entityType.getValue(), year, month);
        
        try {
            Files.createDirectories(entityDir);
        } catch (IOException e) {
            logger.error("Failed to create directory: {}", entityDir, e);
            throw new BusinessException("Failed to create upload directory");
        }
        
        return entityDir;
    }
}