package com.cleaningsystem.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Announcement Entity
 * Represents system announcements created by administrators
 * Follows the same pattern as Task and WeekRoutine entities
 */
@Entity
@Table(name = "announcements")
public class Announcement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_id")
    private Long announcementId;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "priority", length = 20, nullable = false)
    private String priority = "normal"; // low, normal, high, urgent
    
    @Column(name = "announcement_type", length = 50)
    private String announcementType = "general"; // general, maintenance, policy, emergency
    
    @Column(name = "target_audience", length = 50)
    private String targetAudience = "all"; // all, admin, janitor, specific_role
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy; // Admin user ID who created the announcement
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Announcement() {}
    
    public Announcement(String title, String content, String priority, Long createdBy) {
        this.title = title;
        this.content = content;
        this.priority = priority;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    // Getters and Setters
    public Long getAnnouncementId() {
        return announcementId;
    }
    
    public void setAnnouncementId(Long announcementId) {
        this.announcementId = announcementId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getAnnouncementType() {
        return announcementType;
    }
    
    public void setAnnouncementType(String announcementType) {
        this.announcementType = announcementType;
    }
    
    public String getTargetAudience() {
        return targetAudience;
    }
    
    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isVisible() {
        return isActive && !isExpired();
    }
    
    @Override
    public String toString() {
        return "Announcement{" +
                "announcementId=" + announcementId +
                ", title='" + title + '\'' +
                ", priority='" + priority + '\'' +
                ", announcementType='" + announcementType + '\'' +
                ", targetAudience='" + targetAudience + '\'' +
                ", isActive=" + isActive +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                '}';
    }
}