package com.cleaningsystem.backend.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for consistent UTC time handling across the application
 * This class addresses timezone inconsistencies that cause "Scheduled time should not be in the past" errors
 */
public class DateTimeUtils {
    
    private static final int DEFAULT_TOLERANCE_MINUTES = 10; // 10 minutes tolerance for timezone edge cases and network latency
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Get current time in UTC timezone
     * @return Current UTC time as LocalDateTime
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
    
    /**
     * Validate if a scheduled time is valid (not significantly in the past)
     * Uses enhanced tolerance to account for network latency and timezone edge cases
     * 
     * @param scheduledTime The scheduled time to validate
     * @param toleranceMinutes Number of minutes tolerance for "past" validation
     * @return true if the scheduled time is valid, false otherwise
     */
    public static boolean isValidScheduledTime(LocalDateTime scheduledTime, int toleranceMinutes) {
        if (scheduledTime == null) {
            return false;
        }
        
        LocalDateTime nowUtc = nowUtc();
        LocalDateTime minAllowedTime = nowUtc.minusMinutes(toleranceMinutes);
        
        return scheduledTime.isAfter(minAllowedTime) || scheduledTime.isEqual(minAllowedTime);
    }
    
    /**
     * Validate if a scheduled time is valid using default tolerance
     * @param scheduledTime The scheduled time to validate
     * @return true if the scheduled time is valid, false otherwise
     */
    public static boolean isValidScheduledTime(LocalDateTime scheduledTime) {
        return isValidScheduledTime(scheduledTime, DEFAULT_TOLERANCE_MINUTES);
    }
    
    /**
     * Generate detailed error message for scheduled time validation failures
     * Provides debugging information about the time difference
     * 
     * @param scheduledTime The invalid scheduled time
     * @param toleranceMinutes The tolerance used for validation
     * @return Detailed error message with timestamps
     */
    public static String getScheduledTimeErrorMessage(LocalDateTime scheduledTime, int toleranceMinutes) {
        if (scheduledTime == null) {
            return "Scheduled time cannot be null";
        }
        
        LocalDateTime nowUtc = nowUtc();
        LocalDateTime minAllowedTime = nowUtc.minusMinutes(toleranceMinutes);
        
        long minutesDifference = ChronoUnit.MINUTES.between(scheduledTime, nowUtc);
        
        return String.format(
            "Start date cannot be more than %d minutes in the past. " +
            "Scheduled: %s UTC, Current: %s UTC, " +
            "Difference: %d minutes ago",
            toleranceMinutes,
            scheduledTime.format(DISPLAY_FORMAT),
            nowUtc.format(DISPLAY_FORMAT),
            minutesDifference
        );
    }
    
    /**
     * Validate task duration constraints
     * @param startTime Task start time
     * @param endTime Task end time
     * @return true if duration is valid (between 15 minutes and 24 hours)
     */
    public static boolean isValidTaskDuration(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            return false;
        }
        
        long minutesDuration = ChronoUnit.MINUTES.between(startTime, endTime);
        return minutesDuration >= 15 && minutesDuration <= 1440; // 15 minutes to 24 hours
    }
    
    /**
     * Generate error message for duration validation failures
     * @param startTime Task start time
     * @param endTime Task end time
     * @return Detailed duration error message
     */
    public static String getDurationErrorMessage(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return "Start time and end time cannot be null";
        }
        
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            return "End time must be after start time";
        }
        
        long minutesDuration = ChronoUnit.MINUTES.between(startTime, endTime);
        
        if (minutesDuration < 15) {
            return String.format("Task duration is too short: %d minutes. Minimum duration is 15 minutes.", minutesDuration);
        }
        
        if (minutesDuration > 1440) {
            return String.format("Task duration is too long: %d minutes (%.1f hours). Maximum duration is 24 hours.", 
                minutesDuration, minutesDuration / 60.0);
        }
        
        return "Task duration is valid";
    }
    
    /**
     * Validate future scheduling constraint (not more than 1 year in advance)
     * @param scheduledTime The scheduled time to validate
     * @return true if within 1 year limit, false otherwise
     */
    public static boolean isWithinFutureLimit(LocalDateTime scheduledTime) {
        if (scheduledTime == null) {
            return false;
        }
        
        LocalDateTime nowUtc = nowUtc();
        LocalDateTime maxFutureTime = nowUtc.plusYears(1);
        
        return scheduledTime.isBefore(maxFutureTime) || scheduledTime.isEqual(maxFutureTime);
    }
    
    /**
     * Generate error message for future limit validation
     * @param scheduledTime The scheduled time
     * @return Error message for future limit violation
     */
    public static String getFutureLimitErrorMessage(LocalDateTime scheduledTime) {
        if (scheduledTime == null) {
            return "Scheduled time cannot be null";
        }
        
        LocalDateTime nowUtc = nowUtc();
        LocalDateTime maxFutureTime = nowUtc.plusYears(1);
        
        return String.format(
            "Scheduled time cannot be more than 1 year in the future. " +
            "Scheduled: %s UTC, Max allowed: %s UTC",
            scheduledTime.format(DISPLAY_FORMAT),
            maxFutureTime.format(DISPLAY_FORMAT)
        );
    }
}