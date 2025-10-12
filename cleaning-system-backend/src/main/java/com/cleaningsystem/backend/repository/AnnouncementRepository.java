package com.cleaningsystem.backend.repository;

import com.cleaningsystem.backend.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AnnouncementRepository Interface
 * Provides database operations for Announcement entity
 * Follows the same patterns as TaskRepository and WeekRoutineRepository
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    // Find active announcements
    List<Announcement> findByIsActiveTrueOrderByCreatedAtDesc();
    
    // Find announcements by creator (admin)
    List<Announcement> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
    
    // Find announcements by target audience
    List<Announcement> findByTargetAudienceOrderByCreatedAtDesc(String targetAudience);
    
    // Find announcements by priority
    List<Announcement> findByPriorityOrderByCreatedAtDesc(String priority);
    
    // Find announcements by type
    List<Announcement> findByAnnouncementTypeOrderByCreatedAtDesc(String announcementType);
    
    // Find visible announcements (active and not expired)
    @Query("SELECT a FROM Announcement a WHERE a.isActive = true AND (a.expiresAt IS NULL OR a.expiresAt > :currentTime) ORDER BY a.createdAt DESC")
    List<Announcement> findVisibleAnnouncements(@Param("currentTime") LocalDateTime currentTime);
    
    // Find expired announcements
    @Query("SELECT a FROM Announcement a WHERE a.expiresAt IS NOT NULL AND a.expiresAt <= :currentTime ORDER BY a.expiresAt DESC")
    List<Announcement> findExpiredAnnouncements(@Param("currentTime") LocalDateTime currentTime);
    
    // Search announcements by title or content
    @Query("SELECT a FROM Announcement a WHERE a.isActive = true AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(a.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY a.createdAt DESC")
    List<Announcement> searchActiveAnnouncements(@Param("searchTerm") String searchTerm);
    
    // Find recent announcements (limited)
    @Query("SELECT a FROM Announcement a WHERE a.isActive = true ORDER BY a.createdAt DESC LIMIT :limit")
    List<Announcement> findRecentAnnouncements(@Param("limit") int limit);
    
    // Count announcements by priority
    long countByPriorityAndIsActiveTrue(String priority);
    
    // Count announcements by creator
    long countByCreatedByAndIsActiveTrue(Long createdBy);
    
    // Count total active announcements
    long countByIsActiveTrue();
    
    // Advanced search with multiple filters
    @Query("SELECT a FROM Announcement a WHERE " +
           "(:searchTerm IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(a.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:priority IS NULL OR a.priority = :priority) AND " +
           "(:announcementType IS NULL OR a.announcementType = :announcementType) AND " +
           "(:targetAudience IS NULL OR a.targetAudience = :targetAudience) AND " +
           "(:createdBy IS NULL OR a.createdBy = :createdBy) AND " +
           "(:isActive IS NULL OR a.isActive = :isActive) AND " +
           "(a.expiresAt IS NULL OR a.expiresAt > :currentTime) " +
           "ORDER BY a.createdAt DESC")
    List<Announcement> findAnnouncementsWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("priority") String priority,
            @Param("announcementType") String announcementType,
            @Param("targetAudience") String targetAudience,
            @Param("createdBy") Long createdBy,
            @Param("isActive") Boolean isActive,
            @Param("currentTime") LocalDateTime currentTime
    );
    
    // Get announcement statistics
    @Query("SELECT a.priority, COUNT(a) FROM Announcement a WHERE a.isActive = true GROUP BY a.priority")
    List<Object[]> getAnnouncementStatsByPriority();
    
    @Query("SELECT a.announcementType, COUNT(a) FROM Announcement a WHERE a.isActive = true GROUP BY a.announcementType")
    List<Object[]> getAnnouncementStatsByType();
    
    @Query("SELECT a.targetAudience, COUNT(a) FROM Announcement a WHERE a.isActive = true GROUP BY a.targetAudience")
    List<Object[]> getAnnouncementStatsByAudience();
    
    // Find visible announcements for janitor audience (includes 'janitor', 'cleaner', and 'all' target audiences)
    @Query("SELECT a FROM Announcement a WHERE a.isActive = true AND (a.targetAudience IN ('janitor', 'cleaner', 'all')) AND (a.expiresAt IS NULL OR a.expiresAt > :currentTime) ORDER BY a.createdAt DESC")
    List<Announcement> findVisibleAnnouncementsForJanitor(@Param("currentTime") LocalDateTime currentTime);
    
    // Find visible announcements for admin audience (includes 'admin' and 'all' target audiences)
    @Query("SELECT a FROM Announcement a WHERE a.isActive = true AND (a.targetAudience IN ('admin', 'all')) AND (a.expiresAt IS NULL OR a.expiresAt > :currentTime) ORDER BY a.createdAt DESC")
    List<Announcement> findVisibleAnnouncementsForAdmin(@Param("currentTime") LocalDateTime currentTime);
    
    // Find visible announcements for supervisor audience (includes 'supervisor' and 'all' target audiences)
    @Query("SELECT a FROM Announcement a WHERE a.isActive = true AND (a.targetAudience IN ('supervisor', 'all')) AND (a.expiresAt IS NULL OR a.expiresAt > :currentTime) ORDER BY a.createdAt DESC")
    List<Announcement> findVisibleAnnouncementsForSupervisor(@Param("currentTime") LocalDateTime currentTime);
}