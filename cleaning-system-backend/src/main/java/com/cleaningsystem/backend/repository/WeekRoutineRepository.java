package com.cleaningsystem.backend.repository;

import com.cleaningsystem.backend.entity.WeekRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WeekRoutineRepository extends JpaRepository<WeekRoutine, Long> {
    
    // Find active routines
    List<WeekRoutine> findByActiveTrueOrderByCreatedAtDesc();
    
    // Find routines by creator (admin)
    List<WeekRoutine> findByCreatedByOrderByCreatedAtDesc(Long adminId);
    
    // Find active routines by creator
    List<WeekRoutine> findByCreatedByAndActiveTrueOrderByCreatedAtDesc(Long adminId);
    
    // Find routines by task type
    List<WeekRoutine> findByTaskTypeOrderByCreatedAtDesc(String taskType);
    
    // Find active public routines (for task wall)
    List<WeekRoutine> findByTaskTypeAndActiveTrueOrderByScheduledTimeAsc(String taskType);
    
    // Find routines assigned to specific janitor
    List<WeekRoutine> findByAssignedToOrderByScheduledTimeAsc(Long janitorId);
    
    // Find active routines assigned to specific janitor
    List<WeekRoutine> findByAssignedToAndActiveTrueOrderByScheduledTimeAsc(Long janitorId);
    
    // Find routines by priority
    List<WeekRoutine> findByPriorityOrderByScheduledTimeAsc(String priority);
    
    // Find routines by location
    List<WeekRoutine> findByLocationContainingIgnoreCaseOrderByScheduledTimeAsc(String location);
    
    // Find routines that should generate tasks for a specific day of week
    // weekDays field contains comma-separated day numbers (1=Monday, 7=Sunday)
    @Query("SELECT wr FROM WeekRoutine wr WHERE wr.active = true AND wr.weekDays LIKE %:dayOfWeek% ORDER BY wr.scheduledTime ASC")
    List<WeekRoutine> findActiveRoutinesForDay(@Param("dayOfWeek") String dayOfWeek);
    
    // Find routines that haven't generated tasks recently (to prevent duplicate generation)
    @Query("SELECT wr FROM WeekRoutine wr WHERE wr.active = true AND " +
           "(wr.lastGenerated IS NULL OR wr.lastGenerated < :cutoffTime) AND " +
           "wr.weekDays LIKE %:dayOfWeek% ORDER BY wr.scheduledTime ASC")
    List<WeekRoutine> findRoutinesNeedingGeneration(@Param("dayOfWeek") String dayOfWeek, 
                                                   @Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Count routines by task type
    long countByTaskType(String taskType);
    
    // Count active routines
    long countByActiveTrue();
    
    // Count routines by creator
    long countByCreatedBy(Long adminId);
    
    // Get routine statistics for admin dashboard
    @Query("SELECT wr.taskType, COUNT(wr) FROM WeekRoutine wr GROUP BY wr.taskType")
    List<Object[]> getRoutineStatisticsByType();
    
    // Get routine statistics by priority
    @Query("SELECT wr.priority, COUNT(wr) FROM WeekRoutine wr WHERE wr.active = true GROUP BY wr.priority")
    List<Object[]> getActiveRoutineStatisticsByPriority();
    
    // Get routine statistics by creator
    @Query("SELECT wr.createdBy, COUNT(wr) FROM WeekRoutine wr WHERE wr.active = true GROUP BY wr.createdBy")
    List<Object[]> getActiveRoutineStatisticsByCreator();
    
    // Find recent routines for dashboard
    List<WeekRoutine> findTop5ByOrderByCreatedAtDesc();
    
    // Find routines created within date range
    @Query("SELECT wr FROM WeekRoutine wr WHERE wr.createdAt BETWEEN :startDate AND :endDate ORDER BY wr.createdAt DESC")
    List<WeekRoutine> findRoutinesCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    // Find routines updated recently
    @Query("SELECT wr FROM WeekRoutine wr WHERE wr.updatedAt >= :since ORDER BY wr.updatedAt DESC")
    List<WeekRoutine> findRoutinesUpdatedSince(@Param("since") LocalDateTime since);
    
    // Advanced search for routines
    @Query("SELECT wr FROM WeekRoutine wr WHERE " +
           "(:searchTerm IS NULL OR " +
           " LOWER(wr.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(wr.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(wr.location) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:taskType IS NULL OR wr.taskType = :taskType) AND " +
           "(:priority IS NULL OR wr.priority = :priority) AND " +
           "(:active IS NULL OR wr.active = :active) AND " +
           "(:createdBy IS NULL OR wr.createdBy = :createdBy) AND " +
           "(:assignedTo IS NULL OR wr.assignedTo = :assignedTo) " +
           "ORDER BY wr.scheduledTime ASC")
    List<WeekRoutine> findRoutinesWithFilter(@Param("searchTerm") String searchTerm,
                                           @Param("taskType") String taskType,
                                           @Param("priority") String priority,
                                           @Param("active") Boolean active,
                                           @Param("createdBy") Long createdBy,
                                           @Param("assignedTo") Long assignedTo);
    
    // Count routines with filter
    @Query("SELECT COUNT(wr) FROM WeekRoutine wr WHERE " +
           "(:searchTerm IS NULL OR " +
           " LOWER(wr.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(wr.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(wr.location) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:taskType IS NULL OR wr.taskType = :taskType) AND " +
           "(:priority IS NULL OR wr.priority = :priority) AND " +
           "(:active IS NULL OR wr.active = :active) AND " +
           "(:createdBy IS NULL OR wr.createdBy = :createdBy) AND " +
           "(:assignedTo IS NULL OR wr.assignedTo = :assignedTo)")
    long countRoutinesWithFilter(@Param("searchTerm") String searchTerm,
                                @Param("taskType") String taskType,
                                @Param("priority") String priority,
                                @Param("active") Boolean active,
                                @Param("createdBy") Long createdBy,
                                @Param("assignedTo") Long assignedTo);
    
    // Find routines that generate tasks for multiple days
    @Query("SELECT wr FROM WeekRoutine wr WHERE wr.active = true AND " +
           "(wr.weekDays LIKE '%,%' OR LENGTH(wr.weekDays) > 1) " +
           "ORDER BY wr.scheduledTime ASC")
    List<WeekRoutine> findMultiDayRoutines();
    
    // Find routines for a specific day and time range
    @Query("SELECT wr FROM WeekRoutine wr WHERE wr.active = true AND " +
           "wr.weekDays LIKE %:dayOfWeek% AND " +
           "wr.scheduledTime BETWEEN :startTime AND :endTime " +
           "ORDER BY wr.scheduledTime ASC")
    List<WeekRoutine> findRoutinesForDayAndTimeRange(@Param("dayOfWeek") String dayOfWeek,
                                                    @Param("startTime") java.time.LocalTime startTime,
                                                    @Param("endTime") java.time.LocalTime endTime);
    
    // Find routines that should be generating tasks soon (for scheduling)
    @Query("SELECT wr FROM WeekRoutine wr WHERE wr.active = true AND " +
           "wr.weekDays LIKE %:dayOfWeek% AND " +
           "TIME(wr.scheduledTime) <= TIME(:upcomingTime) " +
           "ORDER BY wr.scheduledTime ASC")
    List<WeekRoutine> findUpcomingRoutinesForDay(@Param("dayOfWeek") String dayOfWeek,
                                               @Param("upcomingTime") java.time.LocalTime upcomingTime);
}