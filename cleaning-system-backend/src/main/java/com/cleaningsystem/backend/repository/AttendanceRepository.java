package com.cleaningsystem.backend.repository;

import com.cleaningsystem.backend.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    // Find attendance record for a janitor on a specific date
    Optional<Attendance> findByJanitorIdAndWorkDate(Long janitorId, LocalDate workDate);

    // Find first attendance record for a janitor on a specific date (handles duplicates)
    @Query("SELECT a FROM Attendance a WHERE a.janitorId = :janitorId AND a.workDate = :workDate ORDER BY a.checkInTime ASC LIMIT 1")
    Optional<Attendance> findFirstByJanitorIdAndWorkDate(@Param("janitorId") Long janitorId, @Param("workDate") LocalDate workDate);
    
    // Find all attendance records for a janitor ordered by date
    List<Attendance> findByJanitorIdOrderByWorkDateDesc(Long janitorId);
    
    // Find attendance records for a janitor between dates
    @Query("SELECT a FROM Attendance a WHERE a.janitorId = :janitorId AND a.workDate BETWEEN :startDate AND :endDate ORDER BY a.workDate DESC")
    List<Attendance> findByJanitorIdAndWorkDateBetween(@Param("janitorId") Long janitorId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
    
    // Find all attendance records for a janitor (simplified method)
    List<Attendance> findByJanitorId(Long janitorId);
    
    // Find attendance records for a specific date (all janitors)
    List<Attendance> findByWorkDateOrderByCheckInTimeAsc(LocalDate workDate);
    
    // Check if janitor is currently checked in (has check-in but no check-out for today)
    @Query("SELECT a FROM Attendance a WHERE a.janitorId = :janitorId AND a.workDate = :date AND a.checkInTime IS NOT NULL AND a.checkOutTime IS NULL")
    Optional<Attendance> findActiveAttendanceForJanitor(@Param("janitorId") Long janitorId, @Param("date") LocalDate date);
    
    // Find attendance records for current month
    @Query("SELECT a FROM Attendance a WHERE a.janitorId = :janitorId AND YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month ORDER BY a.workDate DESC")
    List<Attendance> findByJanitorIdAndMonth(@Param("janitorId") Long janitorId, 
                                             @Param("year") int year, 
                                             @Param("month") int month);
    
    // Count attendance days for a janitor in a month
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.janitorId = :janitorId AND YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month AND a.checkInTime IS NOT NULL")
    Long countAttendanceDaysInMonth(@Param("janitorId") Long janitorId, 
                                    @Param("year") int year, 
                                    @Param("month") int month);
    
    // Count work days for a janitor in a month (simplified - no status check)
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.janitorId = :janitorId AND YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month")
    Long countWorkDaysInMonth(@Param("janitorId") Long janitorId,
                              @Param("year") int year,
                              @Param("month") int month);
    
    
    // Calculate total work hours for a janitor in a month
    @Query("SELECT SUM(a.workHours) FROM Attendance a WHERE a.janitorId = :janitorId AND YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month")
    Double getTotalWorkHoursInMonth(@Param("janitorId") Long janitorId, 
                                    @Param("year") int year, 
                                    @Param("month") int month);
    
    
    // Get attendance count for admin dashboard (simplified)
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.workDate = :date")
    Long getAttendanceCountForDate(@Param("date") LocalDate date);
    
    // Find all janitors who are currently checked in
    @Query("SELECT a FROM Attendance a WHERE a.workDate = :date AND a.checkInTime IS NOT NULL AND a.checkOutTime IS NULL")
    List<Attendance> findCurrentlyCheckedInJanitors(@Param("date") LocalDate date);
    
    // Analytics methods for admin/supervisor dashboard
    
    // Find attendance records by multiple janitor IDs and date range
    @Query("SELECT a FROM Attendance a WHERE a.janitorId IN :janitorIds AND a.workDate BETWEEN :startDate AND :endDate ORDER BY a.workDate DESC")
    List<Attendance> findByJanitorIdInAndWorkDateBetween(@Param("janitorIds") List<Long> janitorIds,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);
    
    // Find attendance records by date range (all janitors)
    @Query("SELECT a FROM Attendance a WHERE a.workDate BETWEEN :startDate AND :endDate ORDER BY a.workDate DESC")
    List<Attendance> findByWorkDateBetween(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    // Find attendance records for specific date (all janitors)
    List<Attendance> findByWorkDate(LocalDate workDate);
    
    // Count distinct janitor IDs for dashboard summary
    @Query("SELECT COUNT(DISTINCT a.janitorId) FROM Attendance a")
    Long countDistinctJanitorIds();
}