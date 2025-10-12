package com.cleaningsystem.backend.controller;

import com.cleaningsystem.backend.entity.Image;
import com.cleaningsystem.backend.service.ImageService;
import com.cleaningsystem.backend.service.ProfileService;
import com.cleaningsystem.backend.utils.JwtTokenProvider;
import com.cleaningsystem.backend.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ImageController {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    
    private final ImageService imageService;
    private final ProfileService profileService;
    private final JwtTokenProvider jwtTokenProvider;
    
    public ImageController(ImageService imageService, ProfileService profileService, JwtTokenProvider jwtTokenProvider) {
        this.imageService = imageService;
        this.profileService = profileService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    /**
     * Upload images for task completion
     */
    @PostMapping("/upload/task-completion/{taskId}")
    public ResponseEntity<Map<String, Object>> uploadTaskCompletionImages(
            @PathVariable Long taskId,
            @RequestParam("files") List<MultipartFile> files,
            HttpServletRequest request) {
        
        try {
            logger.info("Uploading {} files for task completion: {}", files.size(), taskId);
            
            // Get user from JWT token
            Long userId = getUserFromToken(request);
            
            // Upload files
            List<Image> uploadedImages = imageService.uploadFiles(
                files, Image.EntityType.TASK_COMPLETION, taskId, userId
            );
            
            // Convert to response format
            List<Map<String, Object>> imageData = uploadedImages.stream()
                .map(this::convertImageToMap)
                .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Files uploaded successfully");
            response.put("images", imageData);
            response.put("count", uploadedImages.size());
            
            logger.info("Successfully uploaded {} files for task: {}", uploadedImages.size(), taskId);
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            logger.error("Business error uploading files for task {}: {}", taskId, e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error uploading files for task {}", taskId, e);
            return createErrorResponse("Failed to upload files", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Serve image files
     */
    @GetMapping("/files/task_completion/{fileName}")
    public ResponseEntity<Resource> serveTaskCompletionFile(@PathVariable String fileName,
                                                           HttpServletRequest request) {
        try {
            logger.debug("Serving file: {}", fileName);
            
            Resource resource = imageService.loadFileAsResource(fileName, Image.EntityType.TASK_COMPLETION);
            
            // Determine content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (Exception e) {
                logger.debug("Could not determine file type for: {}", fileName);
            }
            
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
                
        } catch (BusinessException e) {
            logger.error("Business error serving file {}: {}", fileName, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Unexpected error serving file: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all images for a task
     */
    @GetMapping("/tasks/{taskId}/images")
    public ResponseEntity<Map<String, Object>> getTaskImages(@PathVariable Long taskId,
                                                            HttpServletRequest request) {
        try {
            logger.debug("Getting images for task: {}", taskId);
            
            // Authentication check
            getUserFromToken(request);
            
            List<Image> images = imageService.getImagesForEntity(Image.EntityType.TASK_COMPLETION, taskId);
            
            List<Map<String, Object>> imageData = images.stream()
                .map(this::convertImageToMap)
                .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("images", imageData);
            response.put("count", images.size());
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            logger.error("Business error getting images for task {}: {}", taskId, e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error getting images for task {}", taskId, e);
            return createErrorResponse("Failed to get images", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Delete an image
     */
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable Long imageId,
                                                          HttpServletRequest request) {
        try {
            logger.info("Deleting image: {}", imageId);
            
            Long userId = getUserFromToken(request);
            
            boolean deleted = imageService.deleteImage(imageId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "Image deleted successfully" : "Failed to delete image");
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            logger.error("Business error deleting image {}: {}", imageId, e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error deleting image {}", imageId, e);
            return createErrorResponse("Failed to delete image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get image metadata by ID
     */
    @GetMapping("/images/{imageId}")
    public ResponseEntity<Map<String, Object>> getImageById(@PathVariable Long imageId,
                                                           HttpServletRequest request) {
        try {
            logger.debug("Getting image metadata: {}", imageId);
            
            // Authentication check
            getUserFromToken(request);
            
            Image image = imageService.getImageById(imageId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("image", convertImageToMap(image));
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            logger.error("Business error getting image {}: {}", imageId, e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error getting image {}", imageId, e);
            return createErrorResponse("Failed to get image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Upload images for announcements (future use)
     */
    @PostMapping("/upload/announcement/{announcementId}")
    public ResponseEntity<Map<String, Object>> uploadAnnouncementImages(
            @PathVariable Long announcementId,
            @RequestParam("files") List<MultipartFile> files,
            HttpServletRequest request) {
        
        try {
            logger.info("Uploading {} files for announcement: {}", files.size(), announcementId);
            
            Long userId = getUserFromToken(request);
            
            List<Image> uploadedImages = imageService.uploadFiles(
                files, Image.EntityType.ANNOUNCEMENT, announcementId, userId
            );
            
            List<Map<String, Object>> imageData = uploadedImages.stream()
                .map(this::convertImageToMap)
                .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Files uploaded successfully");
            response.put("images", imageData);
            response.put("count", uploadedImages.size());
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            logger.error("Business error uploading files for announcement {}: {}", announcementId, e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error uploading files for announcement {}", announcementId, e);
            return createErrorResponse("Failed to upload files", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Serve announcement image files
     */
    @GetMapping("/files/announcement/{fileName}")
    public ResponseEntity<Resource> serveAnnouncementFile(@PathVariable String fileName,
                                                         HttpServletRequest request) {
        try {
            Resource resource = imageService.loadFileAsResource(fileName, Image.EntityType.ANNOUNCEMENT);
            
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (Exception e) {
                logger.debug("Could not determine file type for: {}", fileName);
            }
            
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
                
        } catch (BusinessException e) {
            logger.error("Business error serving announcement file {}: {}", fileName, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Unexpected error serving announcement file: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Upload avatar image for user profile
     */
    @PostMapping(value = "/upload/profile/{userId}", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadProfileAvatar(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        try {
            logger.info("Uploading avatar for user: {}", userId);
            
            // Get requesting user from JWT token
            Long requestingUserId = getUserFromToken(request);
            String requestingUserRole = getRoleFromToken(request);
            
            // Authorization check: users can upload their own avatar, admin/supervisor can upload for anyone
            if (!requestingUserId.equals(userId) && 
                !"admin".equals(requestingUserRole) && 
                !"supervisor".equals(requestingUserRole)) {
                logger.warn("User {} attempted to upload avatar for user {} without permission", requestingUserId, userId);
                return createErrorResponse("Insufficient permissions to upload avatar for this user", HttpStatus.FORBIDDEN);
            }
            
            // Validate single file upload
            if (file == null || file.isEmpty()) {
                return createErrorResponse("Avatar file is required", HttpStatus.BAD_REQUEST);
            }
            
            // Validate file type for profile images
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return createErrorResponse("Only image files are allowed for avatars", HttpStatus.BAD_REQUEST);
            }
            
            // Validate file size (max 5MB for avatars)
            if (file.getSize() > 5 * 1024 * 1024) {
                return createErrorResponse("Avatar file size must not exceed 5MB", HttpStatus.BAD_REQUEST);
            }
            
            // Upload avatar using specialized method
            Image avatarImage = imageService.uploadProfileAvatar(file, userId, requestingUserId);
            
            // Update user profile with avatar URL
            profileService.updateAvatar(userId, avatarImage.getPublicUrl());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Avatar uploaded successfully");
            response.put("avatarUrl", avatarImage.getPublicUrl());
            response.put("image", convertImageToMap(avatarImage));
            
            logger.info("Successfully uploaded avatar for user: {}, URL: {}", userId, avatarImage.getPublicUrl());
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            logger.error("Business error uploading avatar for user {}: {}", userId, e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error uploading avatar for user {}", userId, e);
            return createErrorResponse("Failed to upload avatar", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Serve profile avatar image files
     */
    @GetMapping("/files/profile/{fileName}")
    public ResponseEntity<Resource> serveProfileAvatarFile(@PathVariable String fileName,
                                                           HttpServletRequest request) {
        try {
            logger.debug("Serving profile avatar file: {}", fileName);
            
            Resource resource = imageService.loadFileAsResource(fileName, Image.EntityType.PROFILE);
            
            // Determine content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (Exception e) {
                logger.debug("Could not determine file type for: {}", fileName);
            }
            
            if (contentType == null) {
                contentType = "image/jpeg"; // Default to JPEG for profile images
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000") // Cache for 1 year
                .body(resource);
                
        } catch (BusinessException e) {
            logger.error("Business error serving profile avatar file {}: {}", fileName, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Unexpected error serving profile avatar file: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get avatar image for a user
     */
    @GetMapping("/profile/{userId}/avatar")
    public ResponseEntity<Map<String, Object>> getUserAvatar(@PathVariable Long userId,
                                                            HttpServletRequest request) {
        try {
            logger.debug("Getting avatar for user: {}", userId);
            
            // Authentication check
            getUserFromToken(request);
            
            List<Image> avatarImages = imageService.getImagesForEntity(Image.EntityType.PROFILE, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            
            if (!avatarImages.isEmpty()) {
                // Return the most recent avatar
                Image latestAvatar = avatarImages.get(avatarImages.size() - 1);
                response.put("hasAvatar", true);
                response.put("avatar", convertImageToMap(latestAvatar));
                response.put("avatarUrl", latestAvatar.getPublicUrl());
            } else {
                response.put("hasAvatar", false);
                response.put("avatarUrl", null);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            logger.error("Business error getting avatar for user {}: {}", userId, e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error getting avatar for user {}", userId, e);
            return createErrorResponse("Failed to get avatar", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Helper methods
    
    private Long getUserFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenProvider.extractTokenFromHeader(authHeader);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new BusinessException("Invalid or missing authentication token");
        }
        return jwtTokenProvider.getUserIdFromToken(token);
    }
    
    private String getRoleFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenProvider.extractTokenFromHeader(authHeader);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new BusinessException("Invalid or missing authentication token");
        }
        return jwtTokenProvider.getRoleFromToken(token);
    }
    
    private Map<String, Object> convertImageToMap(Image image) {
        Map<String, Object> map = new HashMap<>();
        map.put("imageId", image.getImageId());
        map.put("entityType", image.getEntityType().getValue());
        map.put("entityId", image.getEntityId());
        map.put("originalName", image.getOriginalName());
        map.put("storedName", image.getStoredName());
        map.put("publicUrl", image.getPublicUrl());
        map.put("fileSize", image.getFileSize());
        map.put("mimeType", image.getMimeType());
        map.put("createdAt", image.getCreatedAt());
        map.put("createdBy", image.getCreatedBy());
        return map;
    }
    
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}