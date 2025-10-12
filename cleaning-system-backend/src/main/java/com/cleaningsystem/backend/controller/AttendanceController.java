package com.cleaningsystem.backend.controller;

import com.cleaningsystem.backend.entity.Attendance;
import com.cleaningsystem.backend.service.AttendanceService;
import com.cleaningsystem.backend.dto.CheckInRequest;
import com.cleaningsystem.backend.dto.CheckOutRequest;
import com.cleaningsystem.backend.dto.AttendanceStatusResponse;
import com.cleaningsystem.backend.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    // Helper method to extract user ID from JWT token
    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenProvider.extractTokenFromHeader(authHeader);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        return null;
    }
    
    // Simple check-in endpoint (timestamp-only)
    @PostMapping("/check-in")
    @PreAuthorize("hasRole('JANITOR') or hasRole('CLEANER')")
    public ResponseEntity<Map<String, Object>> checkIn(@RequestBody Map<String, Object> checkInData,
                                                       HttpServletRequest httpRequest) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Get user ID from JWT token
            Long janitorId = getUserIdFromToken(httpRequest);
            if (janitorId == null) {
                response.put("success", false);
                response.put("message", "Invalid authentication token");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            // Check if already checked in today
            if (!attendanceService.canCheckIn(janitorId)) {
                response.put("success", false);
                response.put("message", "Already checked in today");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }

            // Perform simple check-in
            Attendance attendance = attendanceService.checkIn(janitorId);
            
            if (attendance != null) {
                response.put("success", true);
                response.put("message", "Check-in successful");
                response.put("attendance", convertAttendanceToSafeFormat(attendance));
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } else {
                response.put("success", false);
                response.put("message", "Check-in failed");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error during check-in: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Simple check-out endpoint (timestamp-only)
    @PostMapping("/check-out")
    @PreAuthorize("hasRole('JANITOR') or hasRole('CLEANER')")
    public ResponseEntity<Map<String, Object>> checkOut(@RequestBody Map<String, Object> checkOutData,
                                                        HttpServletRequest httpRequest) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Get user ID from JWT token
            Long janitorId = getUserIdFromToken(httpRequest);
            if (janitorId == null) {
                response.put("success", false);
                response.put("message", "Invalid authentication token");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            String location = checkOutData.getOrDefault("location", "Office Location").toString();
            
            // Check if can check out
            if (!attendanceService.canCheckOut(janitorId)) {
                response.put("success", false);
                response.put("message", "Not checked in today or already checked out");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
            
            // Perform simple check-out
            Attendance attendance = attendanceService.checkOut(janitorId);
            
            if (attendance != null) {
                response.put("success", true);
                response.put("message", "Check-out successful");
                response.put("attendance", convertAttendanceToSafeFormat(attendance));
                response.put("workHours", attendance.getWorkHours());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Check-out failed");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error during check-out: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get current attendance status (simple)
    @GetMapping("/status")
    @PreAuthorize("hasRole('JANITOR') or hasRole('CLEANER')")
    public ResponseEntity<Map<String, Object>> getCurrentAttendanceStatus(HttpServletRequest request) {
        try {
            // Get user ID from JWT token
            Long janitorId = getUserIdFromToken(request);
            if (janitorId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Invalid authentication token");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            Map<String, Object> status = attendanceService.getAttendanceStatus(janitorId);
            return new ResponseEntity<>(status, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error getting attendance status");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Convert Attendance to safe format for JSON serialization
    private Map<String, Object> convertAttendanceToSafeFormat(Attendance attendance) {
        Map<String, Object> safeAttendance = new HashMap<>();
        safeAttendance.put("attendanceId", attendance.getAttendanceId());
        safeAttendance.put("janitorId", attendance.getJanitorId());
        safeAttendance.put("checkInTime", attendance.getCheckInTime());
        safeAttendance.put("checkOutTime", attendance.getCheckOutTime());
        safeAttendance.put("workDate", attendance.getWorkDate());
        safeAttendance.put("workHours", attendance.getWorkHours());
        return safeAttendance;
    }
    
    // Legacy check-in endpoint (maintain compatibility)
    @PostMapping("/legacy-check-in") 
    public ResponseEntity<Map<String, Object>> legacyCheckIn(@RequestBody Map<String, Object> checkInData) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            Long janitorId = Long.valueOf(checkInData.get("janitorId").toString());
            String location = checkInData.get("location").toString();
            
            if (janitorId == null || location == null) {
                response.put("success", false);
                response.put("message", "Janitor ID and location are required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            // Check if can check in
            if (!attendanceService.canCheckIn(janitorId)) {
                response.put("success", false);
                response.put("message", "Already checked in today");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
            
            Attendance attendance = attendanceService.checkIn(janitorId);
            if (attendance != null) {
                response.put("success", true);
                response.put("message", "Check-in successful");
                response.put("attendance", attendance);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } else {
                response.put("success", false);
                response.put("message", "Check-in failed");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error during check-in: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Legacy check-out endpoint (maintain compatibility)
    @PostMapping("/legacy-check-out")
    public ResponseEntity<Map<String, Object>> legacyCheckOut(@RequestBody Map<String, Object> checkOutData) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            Long janitorId = Long.valueOf(checkOutData.get("janitorId").toString());
            String location = checkOutData.getOrDefault("location", "").toString();
            
            if (janitorId == null) {
                response.put("success", false);
                response.put("message", "Janitor ID is required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            // Check if can check out
            if (!attendanceService.canCheckOut(janitorId)) {
                response.put("success", false);
                response.put("message", "Not checked in today or already checked out");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
            
            Attendance attendance = attendanceService.checkOut(janitorId);
            if (attendance != null) {
                response.put("success", true);
                response.put("message", "Check-out successful");
                response.put("attendance", attendance);
                response.put("workHours", attendance.getWorkHours());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Check-out failed");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error during check-out: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get current attendance status
    @GetMapping("/status/{janitorId}")
    public ResponseEntity<Map<String, Object>> getCurrentAttendanceStatus(@PathVariable Long janitorId) {
        try {
            Map<String, Object> status = attendanceService.getAttendanceStatus(janitorId);
            return new ResponseEntity<>(status, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error getting attendance status");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
    // Get attendance history for current user (JWT-based) 
    @GetMapping("/history")
    @PreAuthorize("hasRole('JANITOR') or hasRole('CLEANER')")
    public ResponseEntity<List<Attendance>> getCurrentUserAttendanceHistory(HttpServletRequest request,
                                                                           @RequestParam(required = false) String startDate,
                                                                           @RequestParam(required = false) String endDate,
                                                                           @RequestParam(required = false, defaultValue = "30") int limit) {
        try {
            // Get user ID from JWT token
            Long janitorId = getUserIdFromToken(request);
            if (janitorId == null) {
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }
            
            List<Attendance> history;
            
            if (startDate != null && endDate != null) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                history = attendanceService.getAttendanceHistory(janitorId, start, end);
            } else {
                history = attendanceService.getAttendanceHistory(janitorId, null, null);
            }
            
            return new ResponseEntity<>(history, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error getting attendance history: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get attendance history for janitor (legacy endpoint for admin use)
    @GetMapping("/history/{janitorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<Attendance>> getAttendanceHistory(@PathVariable Long janitorId,
                                                                 @RequestParam(required = false) String startDate,
                                                                 @RequestParam(required = false) String endDate) {
        try {
            List<Attendance> history;
            
            if (startDate != null && endDate != null) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                history = attendanceService.getAttendanceHistory(janitorId, start, end);
            } else {
                history = attendanceService.getAttendanceHistory(janitorId, null, null);
            }
            
            return new ResponseEntity<>(history, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get monthly attendance
    @GetMapping("/monthly/{janitorId}")
    public ResponseEntity<List<Attendance>> getMonthlyAttendance(@PathVariable Long janitorId,
                                                                 @RequestParam int year,
                                                                 @RequestParam int month) {
        try {
            List<Attendance> monthlyAttendance = attendanceService.getAttendanceHistory(janitorId,
                LocalDate.of(year, month, 1),
                LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth()));
            return new ResponseEntity<>(monthlyAttendance, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get monthly attendance statistics
    @GetMapping("/monthly-stats/{janitorId}")
    public ResponseEntity<Map<String, Object>> getMonthlyAttendanceStats(@PathVariable Long janitorId,
                                                                          @RequestParam int year,
                                                                          @RequestParam int month) {
        try {
            Map<String, Object> stats = attendanceService.getMonthlyAttendanceSummary(janitorId, year, month);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error getting monthly statistics");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get attendance for specific date (admin view)
    @GetMapping("/date/{date}")
    public ResponseEntity<List<Attendance>> getAttendanceForDate(@PathVariable String date) {
        try {
            LocalDate attendanceDate = LocalDate.parse(date);
            // Use getAllAttendanceRecords and filter by date on frontend
            List<Attendance> allRecords = attendanceService.getAllAttendanceRecords();
            return new ResponseEntity<>(allRecords, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get currently checked in janitors
    @GetMapping("/currently-checked-in")
    public ResponseEntity<List<Attendance>> getCurrentlyCheckedInJanitors() {
        try {
            // Use getAllAttendanceRecords and filter for current date with no checkout time
            List<Attendance> allRecords = attendanceService.getAllAttendanceRecords();
            return new ResponseEntity<>(allRecords, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get attendance statistics for admin dashboard
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAttendanceStatistics(@RequestParam(required = false) String date) {
        try {
            LocalDate statisticsDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            // Basic statistics using existing methods
            Map<String, Object> stats = new HashMap<>();
            List<Attendance> allRecords = attendanceService.getAllAttendanceRecords();
            stats.put("totalRecords", allRecords.size());
            stats.put("date", statisticsDate);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error getting attendance statistics");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Update attendance record (admin function) - DISABLED
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAttendance(@PathVariable Long id,
                                                                @RequestBody Attendance updatedAttendance) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Attendance records cannot be modified");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating attendance");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Create manual attendance record (admin function) - DISABLED
    @PostMapping("/manual")
    public ResponseEntity<Map<String, Object>> createManualAttendance(@RequestBody Attendance attendance) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Manual attendance creation not allowed");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating manual attendance record");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Delete attendance record (admin function)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAttendance(@PathVariable Long id) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            boolean deleted = attendanceService.deleteAttendance(id);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Attendance record deleted successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Attendance record not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error deleting attendance record");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Check if janitor can check in
    @GetMapping("/can-check-in/{janitorId}")
    public ResponseEntity<Map<String, Object>> canCheckIn(@PathVariable Long janitorId) {
        try {
            Map<String, Object> response = new HashMap<>();
            boolean canCheckIn = attendanceService.canCheckIn(janitorId);
            response.put("canCheckIn", canCheckIn);
            response.put("janitorId", janitorId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("canCheckIn", false);
            response.put("error", "Error checking check-in status");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Check if janitor can check out
    @GetMapping("/can-check-out/{janitorId}")
    public ResponseEntity<Map<String, Object>> canCheckOut(@PathVariable Long janitorId) {
        try {
            Map<String, Object> response = new HashMap<>();
            boolean canCheckOut = attendanceService.canCheckOut(janitorId);
            response.put("canCheckOut", canCheckOut);
            response.put("janitorId", janitorId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("canCheckOut", false);
            response.put("error", "Error checking check-out status");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get today's attendance record for current user (JWT-based)
    @GetMapping("/today")
    @PreAuthorize("hasRole('JANITOR') or hasRole('CLEANER')")
    public ResponseEntity<Map<String, Object>> getTodayAttendanceRecord(HttpServletRequest request) {
        try {
            // Get user ID from JWT token
            Long janitorId = getUserIdFromToken(request);
            if (janitorId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Invalid authentication token");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            Map<String, Object> todayRecord = attendanceService.getTodayAttendanceDetailed(janitorId);
            return new ResponseEntity<>(todayRecord, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error getting today's attendance record");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // ===================== ANALYTICS ENDPOINTS FOR ADMIN/SUPERVISOR =====================
    
    // Get comprehensive attendance analytics
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getAttendanceAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String userIds) {
        try {
            // Set default date range (last 30 days if not specified)
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            
            // Parse user IDs if provided
            List<Long> userIdList = null;
            if (userIds != null && !userIds.trim().isEmpty()) {
                userIdList = new ArrayList<>();
                String[] ids = userIds.split(",");
                for (String id : ids) {
                    try {
                        userIdList.add(Long.valueOf(id.trim()));
                    } catch (NumberFormatException e) {
                        // Skip invalid IDs
                        System.err.println("Invalid user ID: " + id);
                    }
                }
            }
            
            // Use simplified analytics with existing methods
            Map<String, Object> analytics = new HashMap<>();
            List<Attendance> allRecords = attendanceService.getAllAttendanceRecords();
            analytics.put("totalRecords", allRecords.size());
            analytics.put("dateRange", Map.of("startDate", start, "endDate", end));
            
            return new ResponseEntity<>(analytics, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error getting attendance analytics: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error getting attendance analytics: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get attendance analytics by user
    @GetMapping("/analytics/by-user")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getAttendanceAnalyticsByUser(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String userIds) {
        try {
            // Set default date range (last 30 days if not specified)
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            
            // Parse user IDs if provided
            List<Long> userIdList = null;
            if (userIds != null && !userIds.trim().isEmpty()) {
                userIdList = new ArrayList<>();
                String[] ids = userIds.split(",");
                for (String id : ids) {
                    try {
                        userIdList.add(Long.valueOf(id.trim()));
                    } catch (NumberFormatException e) {
                        // Skip invalid IDs
                        System.err.println("Invalid user ID: " + id);
                    }
                }
            }
            
            // Use simplified analytics by user with existing methods
            Map<String, Object> analytics = new HashMap<>();
            if (userIdList != null && !userIdList.isEmpty()) {
                Long userId = userIdList.get(0);
                analytics = attendanceService.getPerformanceAnalytics(userId, start, end);
            } else {
                analytics.put("totalRecords", 0);
            }
            analytics.put("dateRange", Map.of("startDate", start, "endDate", end));
            
            return new ResponseEntity<>(analytics, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error getting user attendance analytics: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error getting user attendance analytics: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get daily attendance summary for dashboard
    @GetMapping("/analytics/daily-summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getDailyAttendanceSummary(
            @RequestParam(required = false) String date) {
        try {
            LocalDate summaryDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            // Use simplified daily summary with existing methods
            Map<String, Object> summary = new HashMap<>();
            List<Attendance> allRecords = attendanceService.getAllAttendanceRecords();
            summary.put("totalRecords", allRecords.size());
            summary.put("date", summaryDate);
            return new ResponseEntity<>(summary, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error getting daily attendance summary: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error getting daily attendance summary: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get monthly attendance trends
    @GetMapping("/analytics/monthly-trends")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrends(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            // Default to current month if not specified
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            
            // Use simplified monthly trends with existing methods
            List<Map<String, Object>> trends = new ArrayList<>();
            Map<String, Object> trendData = new HashMap<>();
            List<Attendance> allRecords = attendanceService.getAllAttendanceRecords();
            trendData.put("period", start.toString() + " to " + end.toString());
            trendData.put("totalRecords", allRecords.size());
            trends.add(trendData);
            return new ResponseEntity<>(trends, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error getting monthly trends: " + e.getMessage());
            List<Map<String, Object>> errorResponse = new ArrayList<>();
            errorResponse.add(Map.of("error", "Error getting monthly trends: " + e.getMessage()));
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get attendance records in date range for analysis
    @GetMapping("/analytics/records")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> getAttendanceRecordsForAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String userIds,
            @RequestParam(required = false, defaultValue = "100") int limit) {
        try {
            // Set default date range (last 30 days if not specified)
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            
            // Parse user IDs if provided
            List<Long> userIdList = null;
            if (userIds != null && !userIds.trim().isEmpty()) {
                userIdList = new ArrayList<>();
                String[] ids = userIds.split(",");
                for (String id : ids) {
                    try {
                        userIdList.add(Long.valueOf(id.trim()));
                    } catch (NumberFormatException e) {
                        // Skip invalid IDs
                        System.err.println("Invalid user ID: " + id);
                    }
                }
            }
            
            // Get attendance records
            List<Attendance> records;
            if (userIdList != null && !userIdList.isEmpty()) {
                records = attendanceService.getAttendanceHistory(userIdList.get(0), null, null); // Single user for now
                if (startDate != null && endDate != null) {
                    records = attendanceService.getAttendanceHistory(userIdList.get(0), start, end);
                }
            } else {
                // This would need a service method to get all records in date range
                // For now, return empty list or implement getAllAttendanceInRange method
                records = new ArrayList<>();
            }
            
            // Convert to safe format and limit results
            List<Map<String, Object>> safeRecords = new ArrayList<>();
            int count = 0;
            for (Attendance record : records) {
                if (count >= limit) break;
                safeRecords.add(convertAttendanceToSafeFormat(record));
                count++;
            }
            
            return new ResponseEntity<>(safeRecords, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error getting attendance records for analysis: " + e.getMessage());
            e.printStackTrace();
            List<Map<String, Object>> errorResponse = new ArrayList<>();
            errorResponse.add(Map.of("error", "Error getting attendance records: " + e.getMessage()));
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}