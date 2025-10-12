package com.cleaningsystem.backend.repository;

import com.cleaningsystem.backend.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserProfileRepository
 * Data access layer for UserProfile entity
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    /**
     * Find profile by user ID
     */
    Optional<UserProfile> findByUserId(Long userId);
    
    /**
     * Find profile by employee number
     */
    Optional<UserProfile> findByEmployeeNumber(String employeeNumber);
    
    /**
     * Find profiles by status
     */
    List<UserProfile> findByStatus(String status);
    
    /**
     * Find profiles by work area
     */
    List<UserProfile> findByWorkArea(String workArea);
    
    /**
     * Find profiles by position
     */
    List<UserProfile> findByPosition(String position);
    
    /**
     * Check if profile exists for user
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Delete profile by user ID
     */
    void deleteByUserId(Long userId);
    
    /**
     * Get profiles with work information for admin/supervisor view
     */
    @Query("SELECT p FROM UserProfile p WHERE p.position IS NOT NULL ORDER BY p.createdAt DESC")
    List<UserProfile> findAllWorkProfiles();
    
    /**
     * Search profiles by multiple criteria
     */
    @Query("SELECT p FROM UserProfile p WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(p.employeeNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.position) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.workArea) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "ORDER BY p.createdAt DESC")
    List<UserProfile> searchProfiles(@Param("searchTerm") String searchTerm,
                                   @Param("status") String status);
    
    /**
     * Get performance statistics
     */
    @Query("SELECT " +
           "COUNT(p) as totalProfiles, " +
           "AVG(p.taskCompletionRate) as avgCompletionRate, " +
           "AVG(p.qualityScore) as avgQualityScore, " +
           "AVG(p.customerSatisfaction) as avgSatisfaction " +
           "FROM UserProfile p WHERE p.status = 'active'")
    Object[] getPerformanceStatistics();
    
    /**
     * Get top performers by completion rate
     */
    @Query("SELECT p FROM UserProfile p WHERE p.taskCompletionRate IS NOT NULL " +
           "ORDER BY p.taskCompletionRate DESC, p.qualityScore DESC")
    List<UserProfile> getTopPerformers();
}