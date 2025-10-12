package com.cleaningsystem.backend.repository;

import com.cleaningsystem.backend.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Find tasks by assigned janitor
    List<Task> findByAssignedToOrderByScheduledTimeAsc(Long janitorId);
    
    // Find tasks by status
    List<Task> findByStatusOrderByScheduledTimeAsc(String status);
    
    // Find tasks by assigned janitor and status
    List<Task> findByAssignedToAndStatusOrderByScheduledTimeAsc(Long janitorId, String status);
    
    // Find tasks by priority
    List<Task> findByPriorityOrderByScheduledTimeAsc(String priority);
    
    // Find tasks assigned by admin
    List<Task> findByAssignedByOrderByCreatedAtDesc(Long adminId);
    
    // Find overdue tasks (scheduled time has passed but status is not completed)
    @Query("SELECT t FROM Task t WHERE t.scheduledTime < :currentTime AND t.status NOT IN ('completed') ORDER BY t.scheduledTime ASC")
    List<Task> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime);
    
    // Find tasks scheduled for today for a janitor
    @Query("SELECT t FROM Task t WHERE t.assignedTo = :janitorId AND DATE(t.scheduledTime) = DATE(:date) ORDER BY t.scheduledTime ASC")
    List<Task> findTasksForJanitorOnDate(@Param("janitorId") Long janitorId, @Param("date") LocalDateTime date);
    
    // Find tasks by location
    List<Task> findByLocationContainingIgnoreCaseOrderByScheduledTimeAsc(String location);
    
    // Count tasks by status for a janitor
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo = :janitorId AND t.status = :status")
    Long countTasksByJanitorAndStatus(@Param("janitorId") Long janitorId, @Param("status") String status);
    
    // Find tasks scheduled between dates
    @Query("SELECT t FROM Task t WHERE t.scheduledTime BETWEEN :startDate AND :endDate ORDER BY t.scheduledTime ASC")
    List<Task> findTasksBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find tasks by janitor and between dates
    @Query("SELECT t FROM Task t WHERE t.assignedTo = :janitorId AND t.scheduledTime BETWEEN :startDate AND :endDate ORDER BY t.scheduledTime ASC")
    List<Task> findTasksByJanitorBetweenDates(@Param("janitorId") Long janitorId, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    // Get task statistics for admin dashboard
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> getTaskStatistics();
    
    // Get task statistics for a specific janitor
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.assignedTo = :janitorId GROUP BY t.status")
    List<Object[]> getTaskStatisticsByJanitor(@Param("janitorId") Long janitorId);
    
    // Count tasks by status
    long countByStatus(String status);
    
    // Find recent tasks for dashboard
    List<Task> findTop5ByOrderByCreatedAtDesc();
    
    // Find tasks by status ordered by created time (for recent activities)
    List<Task> findByStatusOrderByCreatedAtDesc(String status);
    
    // Count overdue tasks (for dashboard alerts)
    @Query("SELECT COUNT(t) FROM Task t WHERE t.scheduledTime < :currentTime AND t.status NOT IN ('completed') AND t.scheduledTime IS NOT NULL")
    long countOverdueTasks(@Param("currentTime") LocalDateTime currentTime);
    
    // Advanced search with pagination - complex query for all filters
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.janitor LEFT JOIN FETCH t.admin WHERE " +
           "(:searchTerm IS NULL OR " +
           " LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(t.location) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:statuses IS NULL OR t.status IN :statuses) AND " +
           "(:priorities IS NULL OR t.priority IN :priorities) AND " +
           "(:assignedTo IS NULL OR t.assignedTo IN :assignedTo) AND " +
           "(:assignedBy IS NULL OR t.assignedBy IN :assignedBy) AND " +
           "(:location IS NULL OR LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:scheduledStartDate IS NULL OR t.scheduledTime >= :scheduledStartDate) AND " +
           "(:scheduledEndDate IS NULL OR t.scheduledTime <= :scheduledEndDate) AND " +
           "(:createdStartDate IS NULL OR t.createdAt >= :createdStartDate) AND " +
           "(:createdEndDate IS NULL OR t.createdAt <= :createdEndDate) AND " +
           "(:minProgress IS NULL OR t.progressPercentage >= :minProgress) AND " +
           "(:maxProgress IS NULL OR t.progressPercentage <= :maxProgress) AND " +
           "(:isOverdue IS NULL OR " +
           " (:isOverdue = true AND t.scheduledTime < :currentTime AND t.status != 'completed') OR " +
           " (:isOverdue = false AND (t.scheduledTime >= :currentTime OR t.status = 'completed')))")
    Page<Task> findTasksWithAdvancedFilter(
            @Param("searchTerm") String searchTerm,
            @Param("statuses") List<String> statuses,
            @Param("priorities") List<String> priorities,
            @Param("assignedTo") List<Long> assignedTo,
            @Param("assignedBy") List<Long> assignedBy,
            @Param("location") String location,
            @Param("scheduledStartDate") LocalDateTime scheduledStartDate,
            @Param("scheduledEndDate") LocalDateTime scheduledEndDate,
            @Param("createdStartDate") LocalDateTime createdStartDate,
            @Param("createdEndDate") LocalDateTime createdEndDate,
            @Param("minProgress") Integer minProgress,
            @Param("maxProgress") Integer maxProgress,
            @Param("isOverdue") Boolean isOverdue,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable);
    
    // Count query for advanced search (for pagination metadata)
    @Query("SELECT COUNT(t) FROM Task t WHERE " +
           "(:searchTerm IS NULL OR " +
           " LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(t.location) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:statuses IS NULL OR t.status IN :statuses) AND " +
           "(:priorities IS NULL OR t.priority IN :priorities) AND " +
           "(:assignedTo IS NULL OR t.assignedTo IN :assignedTo) AND " +
           "(:assignedBy IS NULL OR t.assignedBy IN :assignedBy) AND " +
           "(:location IS NULL OR LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:scheduledStartDate IS NULL OR t.scheduledTime >= :scheduledStartDate) AND " +
           "(:scheduledEndDate IS NULL OR t.scheduledTime <= :scheduledEndDate) AND " +
           "(:createdStartDate IS NULL OR t.createdAt >= :createdStartDate) AND " +
           "(:createdEndDate IS NULL OR t.createdAt <= :createdEndDate) AND " +
           "(:minProgress IS NULL OR t.progressPercentage >= :minProgress) AND " +
           "(:maxProgress IS NULL OR t.progressPercentage <= :maxProgress) AND " +
           "(:isOverdue IS NULL OR " +
           " (:isOverdue = true AND t.scheduledTime < :currentTime AND t.status != 'completed') OR " +
           " (:isOverdue = false AND (t.scheduledTime >= :currentTime OR t.status = 'completed')))")
    long countTasksWithAdvancedFilter(
            @Param("searchTerm") String searchTerm,
            @Param("statuses") List<String> statuses,
            @Param("priorities") List<String> priorities,
            @Param("assignedTo") List<Long> assignedTo,
            @Param("assignedBy") List<Long> assignedBy,
            @Param("location") String location,
            @Param("scheduledStartDate") LocalDateTime scheduledStartDate,
            @Param("scheduledEndDate") LocalDateTime scheduledEndDate,
            @Param("createdStartDate") LocalDateTime createdStartDate,
            @Param("createdEndDate") LocalDateTime createdEndDate,
            @Param("minProgress") Integer minProgress,
            @Param("maxProgress") Integer maxProgress,
            @Param("isOverdue") Boolean isOverdue,
            @Param("currentTime") LocalDateTime currentTime);
    
    // Statistics queries for filtered results
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE " +
           "(:searchTerm IS NULL OR " +
           " LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(t.location) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:statuses IS NULL OR t.status IN :statuses) AND " +
           "(:priorities IS NULL OR t.priority IN :priorities) AND " +
           "(:assignedTo IS NULL OR t.assignedTo IN :assignedTo) AND " +
           "(:assignedBy IS NULL OR t.assignedBy IN :assignedBy) AND " +
           "(:location IS NULL OR LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:scheduledStartDate IS NULL OR t.scheduledTime >= :scheduledStartDate) AND " +
           "(:scheduledEndDate IS NULL OR t.scheduledTime <= :scheduledEndDate) AND " +
           "(:createdStartDate IS NULL OR t.createdAt >= :createdStartDate) AND " +
           "(:createdEndDate IS NULL OR t.createdAt <= :createdEndDate) AND " +
           "(:minProgress IS NULL OR t.progressPercentage >= :minProgress) AND " +
           "(:maxProgress IS NULL OR t.progressPercentage <= :maxProgress) AND " +
           "(:isOverdue IS NULL OR " +
           " (:isOverdue = true AND t.scheduledTime < :currentTime AND t.status != 'completed') OR " +
           " (:isOverdue = false AND (t.scheduledTime >= :currentTime OR t.status = 'completed'))) " +
           "GROUP BY t.status")
    List<Object[]> getFilteredTaskStatsByStatus(
            @Param("searchTerm") String searchTerm,
            @Param("statuses") List<String> statuses,
            @Param("priorities") List<String> priorities,
            @Param("assignedTo") List<Long> assignedTo,
            @Param("assignedBy") List<Long> assignedBy,
            @Param("location") String location,
            @Param("scheduledStartDate") LocalDateTime scheduledStartDate,
            @Param("scheduledEndDate") LocalDateTime scheduledEndDate,
            @Param("createdStartDate") LocalDateTime createdStartDate,
            @Param("createdEndDate") LocalDateTime createdEndDate,
            @Param("minProgress") Integer minProgress,
            @Param("maxProgress") Integer maxProgress,
            @Param("isOverdue") Boolean isOverdue,
            @Param("currentTime") LocalDateTime currentTime);
    
    // Priority statistics for filtered results
    @Query("SELECT t.priority, COUNT(t) FROM Task t WHERE " +
           "(:searchTerm IS NULL OR " +
           " LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(t.location) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:statuses IS NULL OR t.status IN :statuses) AND " +
           "(:priorities IS NULL OR t.priority IN :priorities) AND " +
           "(:assignedTo IS NULL OR t.assignedTo IN :assignedTo) AND " +
           "(:assignedBy IS NULL OR t.assignedBy IN :assignedBy) AND " +
           "(:location IS NULL OR LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:scheduledStartDate IS NULL OR t.scheduledTime >= :scheduledStartDate) AND " +
           "(:scheduledEndDate IS NULL OR t.scheduledTime <= :scheduledEndDate) AND " +
           "(:createdStartDate IS NULL OR t.createdAt >= :createdStartDate) AND " +
           "(:createdEndDate IS NULL OR t.createdAt <= :createdEndDate) AND " +
           "(:minProgress IS NULL OR t.progressPercentage >= :minProgress) AND " +
           "(:maxProgress IS NULL OR t.progressPercentage <= :maxProgress) AND " +
           "(:isOverdue IS NULL OR " +
           " (:isOverdue = true AND t.scheduledTime < :currentTime AND t.status != 'completed') OR " +
           " (:isOverdue = false AND (t.scheduledTime >= :currentTime OR t.status = 'completed'))) " +
           "GROUP BY t.priority")
    List<Object[]> getFilteredTaskStatsByPriority(
            @Param("searchTerm") String searchTerm,
            @Param("statuses") List<String> statuses,
            @Param("priorities") List<String> priorities,
            @Param("assignedTo") List<Long> assignedTo,
            @Param("assignedBy") List<Long> assignedBy,
            @Param("location") String location,
            @Param("scheduledStartDate") LocalDateTime scheduledStartDate,
            @Param("scheduledEndDate") LocalDateTime scheduledEndDate,
            @Param("createdStartDate") LocalDateTime createdStartDate,
            @Param("createdEndDate") LocalDateTime createdEndDate,
            @Param("minProgress") Integer minProgress,
            @Param("maxProgress") Integer maxProgress,
            @Param("isOverdue") Boolean isOverdue,
            @Param("currentTime") LocalDateTime currentTime);

    // Find tasks assigned to any of the given user IDs (for job monitor)
    List<Task> findByAssignedToIn(List<Long> assignedToIds);

    // Find tasks created by supervisor (for supervisor task scheduler)
    List<Task> findByAssignedByOrderByScheduledTimeAsc(Long supervisorId);

    // Find tasks created by supervisor with status filter
    List<Task> findByAssignedByAndStatusOrderByScheduledTimeAsc(Long supervisorId, String status);

    // Find tasks created by supervisor with priority filter
    List<Task> findByAssignedByAndPriorityOrderByScheduledTimeAsc(Long supervisorId, String priority);

    // Find tasks created by supervisor with both status and priority filters
    List<Task> findByAssignedByAndStatusAndPriorityOrderByScheduledTimeAsc(Long supervisorId, String status, String priority);

    // Find tasks created between dates (for report generation)
    List<Task> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find tasks assigned by supervisor within date range (for supervisor team report)
    List<Task> findByAssignedByAndCreatedAtBetween(Long supervisorId, LocalDateTime startDate, LocalDateTime endDate);
}