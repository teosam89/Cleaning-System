package com.cleaningsystem.backend.service;

import com.cleaningsystem.backend.entity.Attendance;
import com.cleaningsystem.backend.repository.AttendanceRepository;
import com.cleaningsystem.backend.dto.CheckInRequest;
import com.cleaningsystem.backend.dto.CheckOutRequest;
import com.cleaningsystem.backend.dto.AttendanceStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Value("${work.hours.standard:8.0}")
    private Double standardWorkHours;

    // Simple check-in functionality
    public Attendance checkIn(Long janitorId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // Check if already checked in today
        Optional<Attendance> existingAttendance = attendanceRepository.findActiveAttendanceForJanitor(janitorId, today);
        if (existingAttendance.isPresent()) {
            return null; // Already checked in
        }

        // Create new attendance record
        Attendance attendance = new Attendance(janitorId, now);

        return attendanceRepository.save(attendance);
    }

    // Simple check-out functionality
    public Attendance checkOut(Long janitorId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // Find today's attendance record
        Optional<Attendance> attendanceOpt = attendanceRepository.findActiveAttendanceForJanitor(janitorId, today);
        if (attendanceOpt.isEmpty() || attendanceOpt.get().getCheckOutTime() != null) {
            return null; // Not checked in or already checked out
        }

        Attendance attendance = attendanceOpt.get();
        attendance.setCheckOutTime(now);

        // Calculate work hours
        attendance.calculateWorkHours();

        return attendanceRepository.save(attendance);
    }

    // Check if user can check in (not already checked in today)
    public boolean canCheckIn(Long janitorId) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> existingAttendance = attendanceRepository.findActiveAttendanceForJanitor(janitorId, today);
        return existingAttendance.isEmpty();
    }

    // Check if user can check out (checked in but not checked out)
    public boolean canCheckOut(Long janitorId) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> existingAttendance = attendanceRepository.findActiveAttendanceForJanitor(janitorId, today);
        return existingAttendance.isPresent() && existingAttendance.get().getCheckOutTime() == null;
    }

    // Get current attendance status
    public Map<String, Object> getAttendanceStatus(Long janitorId) {
        LocalDate today = LocalDate.now();
        Map<String, Object> status = new HashMap<>();

        Optional<Attendance> attendanceOpt = attendanceRepository.findActiveAttendanceForJanitor(janitorId, today);

        if (attendanceOpt.isPresent()) {
            Attendance attendance = attendanceOpt.get();
            status.put("isCheckedIn", true);
            status.put("checkInTime", attendance.getCheckInTime());
            status.put("checkOutTime", attendance.getCheckOutTime());
            status.put("workHours", attendance.getWorkHours());
        } else {
            status.put("isCheckedIn", false);
            status.put("checkInTime", null);
            status.put("checkOutTime", null);
            status.put("workHours", 0.0);
        }

        return status;
    }

    // Get attendance history for a janitor
    public List<Attendance> getAttendanceHistory(Long janitorId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return attendanceRepository.findByJanitorIdAndWorkDateBetween(janitorId, startDate, endDate);
        } else {
            return attendanceRepository.findByJanitorId(janitorId);
        }
    }

    // Get today's attendance for user
    public Optional<Attendance> getTodayAttendance(Long janitorId) {
        LocalDate today = LocalDate.now();
        return attendanceRepository.findActiveAttendanceForJanitor(janitorId, today);
    }

    // Enhanced method to get today's attendance with analytics
    public Map<String, Object> getTodayAttendanceDetailed(Long janitorId) {
        Map<String, Object> result = new HashMap<>();
        Optional<Attendance> attendanceOpt = getTodayAttendance(janitorId);

        if (attendanceOpt.isPresent()) {
            Attendance attendance = attendanceOpt.get();
            Map<String, Object> todayRecord = new HashMap<>();

            todayRecord.put("attendanceId", attendance.getAttendanceId());
            todayRecord.put("workDate", attendance.getWorkDate());
            todayRecord.put("checkInTime", attendance.getCheckInTime());
            todayRecord.put("checkOutTime", attendance.getCheckOutTime());

            // Work hours calculations
            todayRecord.put("workHours", attendance.getWorkHours() != null ? attendance.getWorkHours() : 0.0);

            // Real-time current work hours if checked in but not checked out
            if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() == null) {
                LocalDateTime now = LocalDateTime.now();
                Duration duration = Duration.between(attendance.getCheckInTime(), now);
                double currentHours = duration.toMinutes() / 60.0;
                todayRecord.put("currentWorkHours", currentHours);
                todayRecord.put("isCheckedIn", true);
            } else {
                todayRecord.put("currentWorkHours", attendance.getWorkHours() != null ? attendance.getWorkHours() : 0.0);
                todayRecord.put("isCheckedIn", false);
            }

            // Work progress indicators
            double currentHours = (Double) todayRecord.get("currentWorkHours");
            double progressPercentage = Math.min(100.0, (currentHours / standardWorkHours) * 100);
            todayRecord.put("workProgressPercentage", progressPercentage);

            result.put("attendance", todayRecord);
            result.put("hasRecord", true);
        } else {
            result.put("attendance", null);
            result.put("hasRecord", false);
        }

        return result;
    }

    // Get monthly attendance summary
    public Map<String, Object> getMonthlyAttendanceSummary(Long janitorId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Attendance> monthlyRecords = attendanceRepository.findByJanitorIdAndWorkDateBetween(janitorId, startDate, endDate);

        Map<String, Object> summary = new HashMap<>();

        if (!monthlyRecords.isEmpty()) {
            double totalWorkHours = monthlyRecords.stream()
                .filter(a -> a.getWorkHours() != null)
                .mapToDouble(Attendance::getWorkHours)
                .sum();

            int daysWorked = monthlyRecords.size();
            int workingDaysInMonth = calculateWorkingDaysInMonth(year, month);

            summary.put("totalWorkHours", totalWorkHours);
            summary.put("daysWorked", daysWorked);
            summary.put("workingDaysInMonth", workingDaysInMonth);
            summary.put("attendanceRate", (double) daysWorked / workingDaysInMonth * 100);
            summary.put("averageWorkHours", daysWorked > 0 ? totalWorkHours / daysWorked : 0);

        } else {
            summary.put("totalWorkHours", 0.0);
            summary.put("daysWorked", 0);
            summary.put("attendanceRate", 0.0);
            summary.put("averageWorkHours", 0.0);
        }

        return summary;
    }

    // Get performance analytics for a janitor
    public Map<String, Object> getPerformanceAnalytics(Long janitorId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> records = getAttendanceHistory(janitorId, startDate, endDate);

        Map<String, Object> analytics = new HashMap<>();

        if (records.isEmpty()) {
            analytics.put("totalRecords", 0);
            analytics.put("totalWorkHours", 0.0);
            analytics.put("averageWorkHours", 0.0);
            return analytics;
        }

        int totalRecords = records.size();
        double totalWorkHours = records.stream()
            .filter(r -> r.getWorkHours() != null)
            .mapToDouble(Attendance::getWorkHours)
            .sum();

        double avgWorkHours = totalRecords > 0 ? totalWorkHours / totalRecords : 0;

        analytics.put("totalRecords", totalRecords);
        analytics.put("totalWorkHours", totalWorkHours);
        analytics.put("averageWorkHours", avgWorkHours);

        return analytics;
    }

    // Calculate working days in a month (excluding weekends)
    private int calculateWorkingDaysInMonth(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        int workingDays = 0;
        LocalDate current = start;

        while (!current.isAfter(end)) {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY && current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            current = current.plusDays(1);
        }

        return workingDays;
    }

    // Get all attendance records (admin function)
    public List<Attendance> getAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }

    // Delete attendance record
    public boolean deleteAttendance(Long attendanceId) {
        try {
            attendanceRepository.deleteById(attendanceId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}