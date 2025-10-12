package com.cleaningsystem.backend.service;

import com.cleaningsystem.backend.entity.Announcement;
import com.cleaningsystem.backend.entity.User;
import com.cleaningsystem.backend.repository.AnnouncementRepository;
import com.cleaningsystem.backend.repository.UserRepository;
import com.cleaningsystem.backend.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AnnouncementService
 * Business logic for announcement management
 * Follows the same patterns as TaskService and WeekRoutineService
 */
@Service
public class AnnouncementService {
    
    @Autowired
    private AnnouncementRepository announcementRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create a new announcement
     * @param announcement The announcement to create
     * @return Created announcement
     * @throws IllegalArgumentException if validation fails
     */
    public Announcement createAnnouncement(Announcement announcement) {
        // Validate announcement data
        validateAnnouncement(announcement);
        
        // Set creation timestamp
        announcement.setCreatedAt(DateTimeUtils.nowUtc());
        
        // Set default values if not provided
        if (announcement.getIsActive() == null) {
            announcement.setIsActive(true);
        }
        if (announcement.getPriority() == null || announcement.getPriority().trim().isEmpty()) {
            announcement.setPriority("normal");
        }
        if (announcement.getAnnouncementType() == null || announcement.getAnnouncementType().trim().isEmpty()) {
            announcement.setAnnouncementType("general");
        }
        if (announcement.getTargetAudience() == null || announcement.getTargetAudience().trim().isEmpty()) {
            announcement.setTargetAudience("all");
        }
        
        return announcementRepository.save(announcement);
    }
    
    /**
     * Get all active announcements
     * @return List of active announcements
     */
    public List<Announcement> getAllActiveAnnouncements() {
        return announcementRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }
    
    /**
     * Get all visible announcements (active and not expired)
     * @return List of visible announcements
     */
    public List<Announcement> getVisibleAnnouncements() {
        return announcementRepository.findVisibleAnnouncements(DateTimeUtils.nowUtc());
    }
    
    /**
     * Get visible announcements for janitor users (includes 'janitor', 'cleaner', and 'all' audiences)
     * @return List of announcements visible to janitors
     */
    public List<Announcement> getVisibleAnnouncementsForJanitor() {
        return announcementRepository.findVisibleAnnouncementsForJanitor(DateTimeUtils.nowUtc());
    }
    
    /**
     * Get visible announcements for admin users (includes 'admin' and 'all' audiences)
     * @return List of announcements visible to admins
     */
    public List<Announcement> getVisibleAnnouncementsForAdmin() {
        return announcementRepository.findVisibleAnnouncementsForAdmin(DateTimeUtils.nowUtc());
    }
    
    /**
     * Get visible announcements for supervisor users (includes 'supervisor' and 'all' audiences)
     * @return List of announcements visible to supervisors
     */
    public List<Announcement> getVisibleAnnouncementsForSupervisor() {
        return announcementRepository.findVisibleAnnouncementsForSupervisor(DateTimeUtils.nowUtc());
    }
    
    /**
     * Get announcement by ID
     * @param announcementId Announcement ID
     * @return Optional containing the announcement if found
     */
    public Optional<Announcement> getAnnouncementById(Long announcementId) {
        return announcementRepository.findById(announcementId);
    }
    
    /**
     * Get announcements created by a specific admin
     * @param adminId Admin user ID
     * @return List of announcements created by the admin
     */
    public List<Announcement> getAnnouncementsByCreator(Long adminId) {
        return announcementRepository.findByCreatedByOrderByCreatedAtDesc(adminId);
    }
    
    /**
     * Get announcements by target audience
     * @param targetAudience Target audience (all, admin, janitor, etc.)
     * @return List of announcements for the target audience
     */
    public List<Announcement> getAnnouncementsByAudience(String targetAudience) {
        return announcementRepository.findByTargetAudienceOrderByCreatedAtDesc(targetAudience);
    }
    
    /**
     * Get announcements by priority
     * @param priority Priority level (low, normal, high, urgent)
     * @return List of announcements with the specified priority
     */
    public List<Announcement> getAnnouncementsByPriority(String priority) {
        return announcementRepository.findByPriorityOrderByCreatedAtDesc(priority);
    }
    
    /**
     * Update an existing announcement
     * @param announcementId Announcement ID
     * @param updatedAnnouncement Updated announcement data
     * @return Updated announcement
     * @throws IllegalArgumentException if announcement not found or validation fails
     */
    @Transactional
    public Announcement updateAnnouncement(Long announcementId, Announcement updatedAnnouncement) {
        Optional<Announcement> existingAnnouncementOpt = announcementRepository.findById(announcementId);
        
        if (existingAnnouncementOpt.isEmpty()) {
            throw new IllegalArgumentException("Announcement not found with ID: " + announcementId);
        }
        
        Announcement existingAnnouncement = existingAnnouncementOpt.get();
        
        // Update fields if provided
        if (updatedAnnouncement.getTitle() != null && !updatedAnnouncement.getTitle().trim().isEmpty()) {
            existingAnnouncement.setTitle(updatedAnnouncement.getTitle().trim());
        }
        if (updatedAnnouncement.getContent() != null) {
            existingAnnouncement.setContent(updatedAnnouncement.getContent().trim());
        }
        if (updatedAnnouncement.getPriority() != null) {
            existingAnnouncement.setPriority(updatedAnnouncement.getPriority());
        }
        if (updatedAnnouncement.getAnnouncementType() != null) {
            existingAnnouncement.setAnnouncementType(updatedAnnouncement.getAnnouncementType());
        }
        if (updatedAnnouncement.getTargetAudience() != null) {
            existingAnnouncement.setTargetAudience(updatedAnnouncement.getTargetAudience());
        }
        if (updatedAnnouncement.getIsActive() != null) {
            existingAnnouncement.setIsActive(updatedAnnouncement.getIsActive());
        }
        if (updatedAnnouncement.getExpiresAt() != null) {
            existingAnnouncement.setExpiresAt(updatedAnnouncement.getExpiresAt());
        }
        
        // Set update timestamp
        existingAnnouncement.setUpdatedAt(DateTimeUtils.nowUtc());
        
        // Validate updated announcement
        validateAnnouncement(existingAnnouncement);
        
        return announcementRepository.save(existingAnnouncement);
    }
    
    /**
     * Delete an announcement
     * @param announcementId Announcement ID
     * @throws IllegalArgumentException if announcement not found
     */
    @Transactional
    public void deleteAnnouncement(Long announcementId) {
        if (!announcementRepository.existsById(announcementId)) {
            throw new IllegalArgumentException("Announcement not found with ID: " + announcementId);
        }
        
        announcementRepository.deleteById(announcementId);
    }
    
    /**
     * Toggle announcement active status
     * @param announcementId Announcement ID
     * @return Updated announcement
     * @throws IllegalArgumentException if announcement not found
     */
    @Transactional
    public Announcement toggleAnnouncementStatus(Long announcementId) {
        Optional<Announcement> announcementOpt = announcementRepository.findById(announcementId);
        
        if (announcementOpt.isEmpty()) {
            throw new IllegalArgumentException("Announcement not found with ID: " + announcementId);
        }
        
        Announcement announcement = announcementOpt.get();
        announcement.setIsActive(!announcement.getIsActive());
        announcement.setUpdatedAt(DateTimeUtils.nowUtc());
        
        return announcementRepository.save(announcement);
    }
    
    /**
     * Search announcements with multiple filters
     * @param searchTerm Search term for title/content
     * @param priority Priority filter
     * @param announcementType Type filter
     * @param targetAudience Audience filter
     * @param createdBy Creator filter
     * @param isActive Active status filter
     * @return List of filtered announcements
     */
    public List<Announcement> searchAnnouncements(String searchTerm, String priority, 
                                                String announcementType, String targetAudience, 
                                                Long createdBy, Boolean isActive) {
        return announcementRepository.findAnnouncementsWithFilters(
                searchTerm, priority, announcementType, targetAudience, 
                createdBy, isActive, DateTimeUtils.nowUtc()
        );
    }
    
    /**
     * Get recent announcements
     * @param limit Maximum number of announcements to return
     * @return List of recent announcements
     */
    public List<Announcement> getRecentAnnouncements(int limit) {
        return announcementRepository.findRecentAnnouncements(Math.min(limit, 50)); // Cap at 50
    }
    
    /**
     * Get announcement statistics
     * @return Map containing various statistics
     */
    public Map<String, Object> getAnnouncementStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Basic counts
            stats.put("totalActiveAnnouncements", announcementRepository.countByIsActiveTrue());
            
            // Priority statistics
            List<Object[]> priorityStats = announcementRepository.getAnnouncementStatsByPriority();
            Map<String, Long> priorityMap = new HashMap<>();
            for (Object[] stat : priorityStats) {
                priorityMap.put((String) stat[0], (Long) stat[1]);
            }
            stats.put("announcementsByPriority", priorityMap);
            
            // Type statistics
            List<Object[]> typeStats = announcementRepository.getAnnouncementStatsByType();
            Map<String, Long> typeMap = new HashMap<>();
            for (Object[] stat : typeStats) {
                typeMap.put((String) stat[0], (Long) stat[1]);
            }
            stats.put("announcementsByType", typeMap);
            
            // Audience statistics
            List<Object[]> audienceStats = announcementRepository.getAnnouncementStatsByAudience();
            Map<String, Long> audienceMap = new HashMap<>();
            for (Object[] stat : audienceStats) {
                audienceMap.put((String) stat[0], (Long) stat[1]);
            }
            stats.put("announcementsByAudience", audienceMap);
            
        } catch (Exception e) {
            System.err.println("Error calculating announcement statistics: " + e.getMessage());
            // Return default values on error
            stats.put("totalActiveAnnouncements", 0L);
            stats.put("announcementsByPriority", new HashMap<>());
            stats.put("announcementsByType", new HashMap<>());
            stats.put("announcementsByAudience", new HashMap<>());
        }
        
        return stats;
    }
    
    /**
     * Clean up expired announcements (utility method)
     * Sets expired announcements to inactive
     * @return Number of announcements cleaned up
     */
    @Transactional
    public int cleanupExpiredAnnouncements() {
        List<Announcement> expiredAnnouncements = announcementRepository.findExpiredAnnouncements(DateTimeUtils.nowUtc());
        
        int cleanedUp = 0;
        for (Announcement announcement : expiredAnnouncements) {
            if (announcement.getIsActive()) {
                announcement.setIsActive(false);
                announcement.setUpdatedAt(DateTimeUtils.nowUtc());
                announcementRepository.save(announcement);
                cleanedUp++;
            }
        }
        
        return cleanedUp;
    }
    
    /**
     * Validate announcement data
     * @param announcement Announcement to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateAnnouncement(Announcement announcement) {
        if (announcement.getTitle() == null || announcement.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Announcement title is required");
        }
        
        if (announcement.getTitle().length() > 255) {
            throw new IllegalArgumentException("Announcement title cannot exceed 255 characters");
        }
        
        if (announcement.getContent() != null && announcement.getContent().length() > 10000) {
            throw new IllegalArgumentException("Announcement content cannot exceed 10,000 characters");
        }
        
        if (announcement.getCreatedBy() == null) {
            throw new IllegalArgumentException("Creator (createdBy) is required");
        }
        
        // Validate creator exists and has proper role (admin or supervisor)
        Optional<User> creatorOpt = userRepository.findById(announcement.getCreatedBy());
        if (creatorOpt.isEmpty()) {
            throw new IllegalArgumentException("Creator user not found");
        }
        
        User creator = creatorOpt.get();
        if (!"admin".equals(creator.getRole()) && !"supervisor".equals(creator.getRole())) {
            throw new IllegalArgumentException("Only administrators and supervisors can create announcements");
        }
        
        // Validate priority
        if (announcement.getPriority() != null) {
            String priority = announcement.getPriority().toLowerCase();
            if (!List.of("low", "normal", "high", "urgent").contains(priority)) {
                throw new IllegalArgumentException("Invalid priority. Must be: low, normal, high, or urgent");
            }
        }
        
        // Validate announcement type
        if (announcement.getAnnouncementType() != null) {
            String type = announcement.getAnnouncementType().toLowerCase();
            if (!List.of("general", "maintenance", "policy", "emergency").contains(type)) {
                throw new IllegalArgumentException("Invalid announcement type. Must be: general, maintenance, policy, or emergency");
            }
        }
        
        // Validate target audience
        if (announcement.getTargetAudience() != null) {
            String audience = announcement.getTargetAudience().toLowerCase();
            if (!List.of("all", "admin", "janitor", "cleaner", "supervisor").contains(audience)) {
                throw new IllegalArgumentException("Invalid target audience. Must be: all, admin, janitor, cleaner, or supervisor");
            }
        }
        
        // Validate expiration date
        if (announcement.getExpiresAt() != null && announcement.getExpiresAt().isBefore(DateTimeUtils.nowUtc())) {
            throw new IllegalArgumentException("Expiration date cannot be in the past");
        }
    }
}