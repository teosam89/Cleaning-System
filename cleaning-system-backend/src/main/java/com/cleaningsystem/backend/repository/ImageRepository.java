package com.cleaningsystem.backend.repository;

import com.cleaningsystem.backend.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    
    /**
     * Find all images for a specific entity that are not deleted
     */
    @Query("SELECT i FROM Image i WHERE i.entityType = :entityType AND i.entityId = :entityId AND i.isDeleted = false ORDER BY i.createdAt ASC")
    List<Image> findByEntityTypeAndEntityIdAndNotDeleted(@Param("entityType") Image.EntityType entityType, @Param("entityId") Long entityId);
    
    /**
     * Find image by stored name that is not deleted
     */
    @Query("SELECT i FROM Image i WHERE i.storedName = :storedName AND i.isDeleted = false")
    Optional<Image> findByStoredNameAndNotDeleted(@Param("storedName") String storedName);
    
    /**
     * Find all images by entity type that are not deleted
     */
    @Query("SELECT i FROM Image i WHERE i.entityType = :entityType AND i.isDeleted = false ORDER BY i.createdAt DESC")
    List<Image> findByEntityTypeAndNotDeleted(@Param("entityType") Image.EntityType entityType);
    
    /**
     * Find all images created by a specific user that are not deleted
     */
    @Query("SELECT i FROM Image i WHERE i.createdBy = :createdBy AND i.isDeleted = false ORDER BY i.createdAt DESC")
    List<Image> findByCreatedByAndNotDeleted(@Param("createdBy") Long createdBy);
    
    /**
     * Count images for a specific entity that are not deleted
     */
    @Query("SELECT COUNT(i) FROM Image i WHERE i.entityType = :entityType AND i.entityId = :entityId AND i.isDeleted = false")
    Long countByEntityTypeAndEntityIdAndNotDeleted(@Param("entityType") Image.EntityType entityType, @Param("entityId") Long entityId);
    
    /**
     * Find images older than specified date that are marked as deleted (for cleanup)
     */
    @Query("SELECT i FROM Image i WHERE i.isDeleted = true AND i.createdAt < :cutoffDate")
    List<Image> findDeletedImagesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find images by file path (for validation/cleanup)
     */
    @Query("SELECT i FROM Image i WHERE i.filePath = :filePath")
    Optional<Image> findByFilePath(@Param("filePath") String filePath);
    
    /**
     * Get total file size for a specific entity
     */
    @Query("SELECT SUM(i.fileSize) FROM Image i WHERE i.entityType = :entityType AND i.entityId = :entityId AND i.isDeleted = false")
    Long getTotalFileSizeByEntity(@Param("entityType") Image.EntityType entityType, @Param("entityId") Long entityId);
    
    /**
     * Get total file size for a user's uploads
     */
    @Query("SELECT SUM(i.fileSize) FROM Image i WHERE i.createdBy = :userId AND i.isDeleted = false")
    Long getTotalFileSizeByUser(@Param("userId") Long userId);
    
    /**
     * Find images by mime type that are not deleted
     */
    @Query("SELECT i FROM Image i WHERE i.mimeType = :mimeType AND i.isDeleted = false ORDER BY i.createdAt DESC")
    List<Image> findByMimeTypeAndNotDeleted(@Param("mimeType") String mimeType);
    
    /**
     * Soft delete images by entity (used when entity is deleted)
     */
    @Query("UPDATE Image i SET i.isDeleted = true WHERE i.entityType = :entityType AND i.entityId = :entityId")
    int softDeleteByEntity(@Param("entityType") Image.EntityType entityType, @Param("entityId") Long entityId);
}