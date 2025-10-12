package com.cleaningsystem.backend.service;

import com.cleaningsystem.backend.repository.AttendanceRepository;
import com.cleaningsystem.backend.repository.TaskRepository;
import com.cleaningsystem.backend.repository.UserRepository;
import com.cleaningsystem.backend.entity.User;
import com.cleaningsystem.backend.entity.Attendance;
import com.cleaningsystem.backend.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Performance calculation service for supervisor staff profile system
 * Calculates real performance metrics based on attendance and task data
 */
@Service
public class PerformanceService {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceService.class);

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Calculate comprehensive performance metrics for a janitor/cleaner
     * @param userId The user ID to calculate performance for
     * @param targetMonth The month to calculate performance for (optional, defaults to current month)
     * @return Map containing performance metrics
     */
    public Map<String, Object> calculatePerformanceMetrics(Long userId, YearMonth targetMonth) {
        logger.info("Calculating performance metrics for user: {} for month: {}", userId, targetMonth);

        Map<String, Object> metrics = new HashMap<>();

        try {
            // Verify user exists and is janitor/cleaner
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("User not found: " + userId);
            }

            User user = userOpt.get();
            String role = user.getRole();
            if (!Arrays.asList("janitor", "cleaner").contains(role)) {
                throw new IllegalArgumentException("Performance calculation only available for janitors and cleaners");
            }

            // Use current month if not specified
            if (targetMonth == null) {
                targetMonth = YearMonth.now();
            }

            // Calculate Monthly Attendance Percentage
            double monthlyAttendance = calculateMonthlyAttendancePercentage(userId, targetMonth);

            // Calculate Task Completion Rate
            double taskCompletionRate = calculateTaskCompletionRate(userId, targetMonth);

            // Calculate Performance Rate (based on weighted algorithm)
            double performanceRate = calculatePerformanceRate(monthlyAttendance, taskCompletionRate);

            // Additional metrics for insight
            Map<String, Object> attendanceDetails = getAttendanceDetails(userId, targetMonth);
            Map<String, Object> taskDetails = getTaskDetails(userId, targetMonth);

            // Build response
            metrics.put("monthlyAttendance", Math.round(monthlyAttendance * 10.0) / 10.0); // Round to 1 decimal
            metrics.put("taskCompletionRate", Math.round(taskCompletionRate * 10.0) / 10.0);
            metrics.put("performanceRate", Math.round(performanceRate * 10.0) / 10.0);

            // Detailed breakdown
            metrics.put("attendanceDetails", attendanceDetails);
            metrics.put("taskDetails", taskDetails);
            metrics.put("calculationMonth", targetMonth.toString());
            metrics.put("userId", userId);
            metrics.put("userRole", role);

            logger.info("Performance calculation completed for user: {}", userId);
            return metrics;

        } catch (Exception e) {
            logger.error("Error calculating performance metrics for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to calculate performance metrics: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate Monthly Attendance Percentage
     * Based on five-day work week (Monday to Friday)
     */
    private double calculateMonthlyAttendancePercentage(Long userId, YearMonth targetMonth) {
        try {
            // Get total working days in the month (Monday to Friday)
            int totalWorkingDays = calculateWorkingDaysInMonth(targetMonth);

            // Get actual attendance days for the user
            Long attendedDays = attendanceRepository.countAttendanceDaysInMonth(
                userId,
                targetMonth.getYear(),
                targetMonth.getMonthValue()
            );

            if (attendedDays == null) {
                attendedDays = 0L;
            }

            // Calculate percentage
            if (totalWorkingDays == 0) {
                return 0.0;
            }

            double percentage = (attendedDays.doubleValue() / totalWorkingDays) * 100.0;

            // Cap at 100% to handle edge cases
            return Math.min(percentage, 100.0);

        } catch (Exception e) {
            logger.error("Error calculating monthly attendance for user {}: {}", userId, e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calculate Task Completion Rate
     * Based on tasks assigned to the janitor in the target month
     */
    private double calculateTaskCompletionRate(Long userId, YearMonth targetMonth) {
        try {
            LocalDate startDate = targetMonth.atDay(1);
            LocalDate endDate = targetMonth.atEndOfMonth();

            // Get all tasks assigned to the user in the target month
            List<Task> monthlyTasks = taskRepository.findTasksByJanitorBetweenDates(
                userId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
            );

            if (monthlyTasks.isEmpty()) {
                return 0.0; // No tasks assigned
            }

            // Count completed tasks
            long completedTasks = monthlyTasks.stream()
                .filter(task -> "completed".equals(task.getStatus()))
                .count();

            // Calculate completion rate
            double completionRate = (completedTasks * 100.0) / monthlyTasks.size();

            return Math.min(completionRate, 100.0);

        } catch (Exception e) {
            logger.error("Error calculating task completion rate for user {}: {}", userId, e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calculate Performance Rate
     * Weighted algorithm based on attendance and task completion
     * Formula: (Monthly Attendance * 0.4) + (Task Completion Rate * 0.6)
     */
    private double calculatePerformanceRate(double monthlyAttendance, double taskCompletionRate) {
        // Weighted average: 40% attendance, 60% task completion
        double performanceRate = (monthlyAttendance * 0.4) + (taskCompletionRate * 0.6);

        return Math.min(performanceRate, 100.0);
    }

    /**
     * Get detailed attendance information for the month
     */
    private Map<String, Object> getAttendanceDetails(Long userId, YearMonth targetMonth) {
        Map<String, Object> details = new HashMap<>();

        try {
            int year = targetMonth.getYear();
            int month = targetMonth.getMonthValue();

            // Basic counts (simplified - no status tracking)
            Long attendedDays = attendanceRepository.countAttendanceDaysInMonth(userId, year, month);

            // Work hours (no overtime tracking)
            Double totalWorkHours = attendanceRepository.getTotalWorkHoursInMonth(userId, year, month);

            // Working days calculation
            int totalWorkingDays = calculateWorkingDaysInMonth(targetMonth);

            details.put("attendedDays", attendedDays != null ? attendedDays : 0);
            details.put("totalWorkingDays", totalWorkingDays);
            details.put("totalWorkHours", totalWorkHours != null ? totalWorkHours : 0.0);

            // Derived metrics
            double averageWorkHoursPerDay = 0.0;
            if (attendedDays != null && attendedDays > 0 && totalWorkHours != null) {
                averageWorkHoursPerDay = totalWorkHours / attendedDays;
            }
            details.put("averageWorkHoursPerDay", Math.round(averageWorkHoursPerDay * 10.0) / 10.0);

        } catch (Exception e) {
            logger.error("Error getting attendance details for user {}: {}", userId, e.getMessage());
            // Return default values
            details.put("attendedDays", 0);
            details.put("totalWorkingDays", calculateWorkingDaysInMonth(targetMonth));
            details.put("lateDays", 0);
            details.put("earlyLeaveDays", 0);
            details.put("totalWorkHours", 0.0);
            details.put("totalOvertimeHours", 0.0);
            details.put("averageWorkHoursPerDay", 0.0);
        }

        return details;
    }

    /**
     * Get detailed task information for the month
     */
    private Map<String, Object> getTaskDetails(Long userId, YearMonth targetMonth) {
        Map<String, Object> details = new HashMap<>();

        try {
            LocalDate startDate = targetMonth.atDay(1);
            LocalDate endDate = targetMonth.atEndOfMonth();

            // Get all tasks for the month
            List<Task> monthlyTasks = taskRepository.findTasksByJanitorBetweenDates(
                userId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
            );

            // Count by status
            long totalTasks = monthlyTasks.size();
            long completedTasks = monthlyTasks.stream().filter(t -> "completed".equals(t.getStatus())).count();
            long pendingTasks = monthlyTasks.stream().filter(t -> "pending".equals(t.getStatus())).count();
            long inProgressTasks = monthlyTasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count();

            // Count by priority
            long urgentTasks = monthlyTasks.stream().filter(t -> "urgent".equals(t.getPriority())).count();
            long highPriorityTasks = monthlyTasks.stream().filter(t -> "high".equals(t.getPriority())).count();

            details.put("totalTasks", totalTasks);
            details.put("completedTasks", completedTasks);
            details.put("pendingTasks", pendingTasks);
            details.put("inProgressTasks", inProgressTasks);
            details.put("urgentTasks", urgentTasks);
            details.put("highPriorityTasks", highPriorityTasks);

            // Completion rate
            double completionRate = totalTasks > 0 ? (completedTasks * 100.0) / totalTasks : 0.0;
            details.put("completionRate", Math.round(completionRate * 10.0) / 10.0);

        } catch (Exception e) {
            logger.error("Error getting task details for user {}: {}", userId, e.getMessage());
            // Return default values
            details.put("totalTasks", 0);
            details.put("completedTasks", 0);
            details.put("pendingTasks", 0);
            details.put("inProgressTasks", 0);
            details.put("urgentTasks", 0);
            details.put("highPriorityTasks", 0);
            details.put("completionRate", 0.0);
        }

        return details;
    }

    /**
     * Calculate working days in a month (Monday to Friday only)
     */
    private int calculateWorkingDaysInMonth(YearMonth targetMonth) {
        int workingDays = 0;
        LocalDate current = targetMonth.atDay(1);
        LocalDate endOfMonth = targetMonth.atEndOfMonth();

        while (!current.isAfter(endOfMonth)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            current = current.plusDays(1);
        }

        return workingDays;
    }

    /**
     * Calculate performance metrics for multiple users (for supervisor dashboard)
     */
    public Map<Long, Map<String, Object>> calculateBulkPerformanceMetrics(List<Long> userIds, YearMonth targetMonth) {
        Map<Long, Map<String, Object>> bulkMetrics = new HashMap<>();

        for (Long userId : userIds) {
            try {
                Map<String, Object> userMetrics = calculatePerformanceMetrics(userId, targetMonth);
                bulkMetrics.put(userId, userMetrics);
            } catch (Exception e) {
                logger.error("Error calculating performance for user {} in bulk operation: {}", userId, e.getMessage());
                // Add default metrics for failed calculations
                Map<String, Object> defaultMetrics = new HashMap<>();
                defaultMetrics.put("monthlyAttendance", 0.0);
                defaultMetrics.put("taskCompletionRate", 0.0);
                defaultMetrics.put("performanceRate", 0.0);
                defaultMetrics.put("error", "Calculation failed");
                bulkMetrics.put(userId, defaultMetrics);
            }
        }

        return bulkMetrics;
    }

    /**
     * Get performance summary for current month (lightweight version)
     */
    public Map<String, Object> getPerformanceSummary(Long userId) {
        Map<String, Object> fullMetrics = calculatePerformanceMetrics(userId, null);

        // Extract summary data
        Map<String, Object> summary = new HashMap<>();
        summary.put("monthlyAttendance", fullMetrics.get("monthlyAttendance"));
        summary.put("taskCompletionRate", fullMetrics.get("taskCompletionRate"));
        summary.put("performanceRate", fullMetrics.get("performanceRate"));
        summary.put("calculationMonth", fullMetrics.get("calculationMonth"));

        return summary;
    }
}