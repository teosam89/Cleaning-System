package com.cleaningsystem.backend.controller;

import com.cleaningsystem.backend.entity.Announcement;
import com.cleaningsystem.backend.entity.User;
import com.cleaningsystem.backend.repository.UserRepository;
import com.cleaningsystem.backend.service.AnnouncementService;
import com.cleaningsystem.backend.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AnnouncementController
 * REST API endpoints for announcement management
 * Follows the same patterns as TaskController and WeekRoutineController
 */
@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*")
public class AnnouncementController {
    
    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // Helper method to extract user info from JWT token
    private Map<String, Object> getUserInfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenProvider.extractTokenFromHeader(authHeader);

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new SecurityException("Invalid token");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", jwtTokenProvider.getUserIdFromToken(token));
        userInfo.put("role", jwtTokenProvider.getRoleFromToken(token));
        return userInfo;
    }
    
    /**
     * Create a new announcement
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> createAnnouncement(@RequestBody Announcement announcement,
                                                                HttpServletRequest request) {
        try {
            // Get current user info from JWT token
            Map<String, Object> userInfo = getUserInfo(request);
            Long currentUserId = (Long) userInfo.get("userId");

            // Force set correct creator ID from JWT token (ignore frontend value)
            announcement.setCreatedBy(currentUserId);

            Announcement createdAnnouncement = announcementService.createAnnouncement(announcement);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("announcementId", createdAnnouncement.getAnnouncementId());
            response.put("message", "Announcement created successfully");
            response.put("data", convertAnnouncementToSafeFormat(createdAnnouncement));

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (SecurityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "认证失败，请重新登录");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", "VALIDATION_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create announcement: " + e.getMessage());
            errorResponse.put("errorType", "INTERNAL_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get all visible announcements (active and not expired)
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllAnnouncements(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String audience,
            @RequestParam(required = false) Long createdBy,
            @RequestParam(required = false) Boolean active) {
        try {
            List<Announcement> announcements;
            
            if (search != null || priority != null || type != null || audience != null || createdBy != null || active != null) {
                announcements = announcementService.searchAnnouncements(search, priority, type, audience, createdBy, active);
            } else {
                announcements = announcementService.getVisibleAnnouncements();
            }
            
            // Convert to safe format to prevent serialization issues
            List<Map<String, Object>> safeAnnouncements = announcements.stream()
                .map(this::convertAnnouncementToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeAnnouncements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get visible announcements for janitor users (includes 'janitor', 'cleaner', and 'all' audiences)
     */
    @GetMapping("/janitor")
    public ResponseEntity<List<Map<String, Object>>> getAnnouncementsForJanitor() {
        try {
            List<Announcement> announcements = announcementService.getVisibleAnnouncementsForJanitor();
            
            List<Map<String, Object>> safeAnnouncements = announcements.stream()
                .map(this::convertAnnouncementToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeAnnouncements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get visible announcements for admin users (includes 'admin' and 'all' audiences)
     */
    @GetMapping("/admin")
    public ResponseEntity<List<Map<String, Object>>> getAnnouncementsForAdmin() {
        try {
            List<Announcement> announcements = announcementService.getVisibleAnnouncementsForAdmin();
            
            List<Map<String, Object>> safeAnnouncements = announcements.stream()
                .map(this::convertAnnouncementToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeAnnouncements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get visible announcements for supervisor users (includes 'supervisor' and 'all' audiences)
     */
    @GetMapping("/supervisor")
    public ResponseEntity<List<Map<String, Object>>> getAnnouncementsForSupervisor() {
        try {
            List<Announcement> announcements = announcementService.getVisibleAnnouncementsForSupervisor();
            
            List<Map<String, Object>> safeAnnouncements = announcements.stream()
                .map(this::convertAnnouncementToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeAnnouncements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get announcements by creator (admin)
     */
    @GetMapping("/creator/{adminId}")
    public ResponseEntity<List<Map<String, Object>>> getAnnouncementsByCreator(@PathVariable Long adminId) {
        try {
            List<Announcement> announcements = announcementService.getAnnouncementsByCreator(adminId);
            
            List<Map<String, Object>> safeAnnouncements = announcements.stream()
                .map(this::convertAnnouncementToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeAnnouncements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get announcements by target audience
     */
    @GetMapping("/audience/{audience}")
    public ResponseEntity<List<Map<String, Object>>> getAnnouncementsByAudience(@PathVariable String audience) {
        try {
            List<Announcement> announcements = announcementService.getAnnouncementsByAudience(audience);
            
            List<Map<String, Object>> safeAnnouncements = announcements.stream()
                .map(this::convertAnnouncementToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeAnnouncements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get recent announcements
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentAnnouncements(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Announcement> announcements = announcementService.getRecentAnnouncements(limit);
            
            List<Map<String, Object>> safeAnnouncements = announcements.stream()
                .map(this::convertAnnouncementToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeAnnouncements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get announcement statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAnnouncementStatistics() {
        try {
            Map<String, Object> stats = announcementService.getAnnouncementStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get announcement statistics: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Clean up expired announcements (utility endpoint)
     */
    @PostMapping("/cleanup-expired")
    public ResponseEntity<Map<String, Object>> cleanupExpiredAnnouncements() {
        try {
            int cleanedUp = announcementService.cleanupExpiredAnnouncements();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expired announcements cleanup completed");
            response.put("announcementsDeactivated", cleanedUp);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to cleanup expired announcements: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get filter options for announcements
     */
    @GetMapping("/filter-options")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        try {
            Map<String, Object> options = new HashMap<>();
            
            options.put("priorities", List.of("low", "normal", "high", "urgent"));
            options.put("types", List.of("general", "maintenance", "policy", "emergency"));
            options.put("audiences", List.of("all", "admin", "janitor", "cleaner", "supervisor"));
            options.put("message", "Filter options retrieved successfully");
            
            return new ResponseEntity<>(options, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to get filter options");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get announcement by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAnnouncementById(@PathVariable Long id) {
        try {
            Optional<Announcement> announcementOpt = announcementService.getAnnouncementById(id);
            
            if (announcementOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", convertAnnouncementToSafeFormat(announcementOpt.get()));
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Announcement not found");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving announcement: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Update announcement
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAnnouncement(@PathVariable Long id, 
                                                                @RequestBody Announcement announcement) {
        try {
            Announcement updatedAnnouncement = announcementService.updateAnnouncement(id, announcement);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Announcement updated successfully");
            response.put("data", convertAnnouncementToSafeFormat(updatedAnnouncement));
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", "VALIDATION_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update announcement: " + e.getMessage());
            errorResponse.put("errorType", "INTERNAL_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Delete announcement
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAnnouncement(@PathVariable Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Announcement deleted successfully");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete announcement: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Toggle announcement active status
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleAnnouncementStatus(@PathVariable Long id) {
        try {
            Announcement updatedAnnouncement = announcementService.toggleAnnouncementStatus(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Announcement status updated successfully");
            response.put("data", convertAnnouncementToSafeFormat(updatedAnnouncement));
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to toggle announcement status: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Convert Announcement entity to safe format to prevent Jackson serialization issues
     * @param announcement Announcement entity
     * @return Safe map representation
     */
    private Map<String, Object> convertAnnouncementToSafeFormat(Announcement announcement) {
        Map<String, Object> safeAnnouncement = new HashMap<>();
        
        safeAnnouncement.put("announcementId", announcement.getAnnouncementId());
        safeAnnouncement.put("title", announcement.getTitle());
        safeAnnouncement.put("content", announcement.getContent());
        safeAnnouncement.put("priority", announcement.getPriority());
        safeAnnouncement.put("announcementType", announcement.getAnnouncementType());
        safeAnnouncement.put("targetAudience", announcement.getTargetAudience());
        safeAnnouncement.put("isActive", announcement.getIsActive());
        safeAnnouncement.put("expiresAt", announcement.getExpiresAt());
        safeAnnouncement.put("createdBy", announcement.getCreatedBy());
        safeAnnouncement.put("createdAt", announcement.getCreatedAt());
        safeAnnouncement.put("updatedAt", announcement.getUpdatedAt());
        
        // Add helper information
        safeAnnouncement.put("isExpired", announcement.isExpired());
        safeAnnouncement.put("isVisible", announcement.isVisible());
        
        // Add creator name if available
        if (announcement.getCreatedBy() != null) {
            Optional<User> creatorOpt = userRepository.findById(announcement.getCreatedBy());
            if (creatorOpt.isPresent()) {
                User creator = creatorOpt.get();
                safeAnnouncement.put("createdByName", creator.getFullName() != null ? creator.getFullName() : creator.getUsername());
            } else {
                safeAnnouncement.put("createdByName", "Unknown Admin");
            }
        }
        
        return safeAnnouncement;
    }
}