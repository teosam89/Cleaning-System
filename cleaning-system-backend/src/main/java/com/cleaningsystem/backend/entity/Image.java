package com.cleaningsystem.backend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "images", indexes = {
    @Index(name = "idx_image_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_image_created_by", columnList = "created_by"),
    @Index(name = "idx_image_created_at", columnList = "created_at"),
    @Index(name = "idx_image_stored_name", columnList = "stored_name"),
    @Index(name = "idx_image_not_deleted", columnList = "is_deleted")
})
public class Image {
    
    public enum EntityType {
        TASK_COMPLETION("task_completion"),
        ANNOUNCEMENT("announcement"), 
        PROFILE("profile");
        
        private final String value;
        
        EntityType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static EntityType fromString(String text) {
            for (EntityType type : EntityType.values()) {
                if (type.value.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No constant with text " + text + " found");
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;
    
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;
    
    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;
    
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Column(name = "public_url", nullable = false, length = 500)
    private String publicUrl;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    @JsonIgnore
    private User creator;
    
    // Default constructor
    public Image() {
        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
    }
    
    // Constructor with required fields
    public Image(EntityType entityType, Long entityId, String originalName, String storedName, 
                 String filePath, String publicUrl, Long fileSize, String mimeType, Long createdBy) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.originalName = originalName;
        this.storedName = storedName;
        this.filePath = filePath;
        this.publicUrl = publicUrl;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
    }
    
    // Getters and Setters
    public Long getImageId() {
        return imageId;
    }
    
    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }
    
    public EntityType getEntityType() {
        return entityType;
    }
    
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public String getOriginalName() {
        return originalName;
    }
    
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
    
    public String getStoredName() {
        return storedName;
    }
    
    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getPublicUrl() {
        return publicUrl;
    }
    
    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public User getCreator() {
        return creator;
    }
    
    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    @Override
    public String toString() {
        return "Image{" +
                "imageId=" + imageId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", originalName='" + originalName + '\'' +
                ", storedName='" + storedName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", mimeType='" + mimeType + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy=" + createdBy +
                ", isDeleted=" + isDeleted +
                '}';
    }
}