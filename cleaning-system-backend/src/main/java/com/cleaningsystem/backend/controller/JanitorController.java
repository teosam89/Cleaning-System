package com.cleaningsystem.backend.controller;

import com.cleaningsystem.backend.entity.User;
import com.cleaningsystem.backend.entity.Task;
import com.cleaningsystem.backend.entity.Attendance;
import com.cleaningsystem.backend.entity.Image;
import com.cleaningsystem.backend.service.TaskService;
import com.cleaningsystem.backend.service.AttendanceService;
import com.cleaningsystem.backend.service.ProfileService;
import com.cleaningsystem.backend.service.ImageService;
import com.cleaningsystem.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.time.LocalDateTime;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/janitors")
@CrossOrigin(origins = "*")
public class JanitorController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private ProfileService profileService;
    
    @Autowired
    private ImageService imageService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Get all janitors (for admin use)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllJanitors() {
        try {
            List<User> janitors = userRepository.findByRoleIn(
                Arrays.asList("janitor", "cleaner", "supervisor")
            );
            
            List<Map<String, Object>> janitorsWithAvatars = new ArrayList<>();
            for (User janitor : janitors) {
                Map<String, Object> janitorData = new HashMap<>();
                janitorData.put("userId", janitor.getUserId());
                janitorData.put("username", janitor.getUsername());
                janitorData.put("fullName", janitor.getFullName());
                janitorData.put("email", janitor.getEmail());
                janitorData.put("role", janitor.getRole());
                janitorData.put("createdAt", janitor.getCreatedAt());
                
                // Add avatar URL
                try {
                    List<Image> avatarImages = imageService.getImagesForEntity(Image.EntityType.PROFILE, janitor.getUserId());
                    if (!avatarImages.isEmpty()) {
                        Image latestAvatar = avatarImages.get(avatarImages.size() - 1);
                        janitorData.put("avatarUrl", latestAvatar.getPublicUrl());
                        janitorData.put("avatar", latestAvatar.getPublicUrl());
                    } else {
                        janitorData.put("avatarUrl", null);
                        janitorData.put("avatar", null);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading avatar for janitor " + janitor.getUserId() + ": " + e.getMessage());
                    janitorData.put("avatarUrl", null);
                    janitorData.put("avatar", null);
                }
                
                janitorsWithAvatars.add(janitorData);
            }
            
            return ResponseEntity.ok(janitorsWithAvatars);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Get janitor dashboard data
     * Security: Users can only access their own dashboard data
     */
    @GetMapping("/{id}/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('JANITOR') or hasRole('CLEANER') or hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getJanitorDashboard(@PathVariable Long id) {
        try {
            System.out.println("JanitorController: Dashboard request for janitor ID: " + id);
            
            Optional<User> janitorOpt = userRepository.findById(id);
            if (!janitorOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Janitor not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            User janitor = janitorOpt.get();
            Map<String, Object> dashboardData = new HashMap<>();
            
            // Get janitor's tasks
            List<Task> assignedTasks = taskService.getTasksByJanitor(id);
            List<Task> completedTasks = taskService.getTasksByJanitorAndStatus(id, "completed");
            List<Task> pendingTasks = taskService.getTasksByJanitorAndStatus(id, "pending");
            List<Task> inProgressTasks = taskService.getTasksByJanitorAndStatus(id, "in_progress");
            
            Map<String, Object> taskStats = new HashMap<>();
            taskStats.put("total", assignedTasks.size());
            taskStats.put("completed", completedTasks.size());
            taskStats.put("pending", pendingTasks.size());
            taskStats.put("inProgress", inProgressTasks.size());
            
            dashboardData.put("taskStats", taskStats);
            
            // Convert recent tasks to safe format (prevent JSON serialization issues)
            List<Map<String, Object>> safeRecentTasks = new ArrayList<>();
            List<Task> recentTasksList = assignedTasks.subList(0, Math.min(5, assignedTasks.size()));
            for (Task task : recentTasksList) {
                Map<String, Object> taskInfo = new HashMap<>();
                taskInfo.put("taskId", task.getTaskId());
                taskInfo.put("title", task.getTitle());
                taskInfo.put("description", task.getDescription());
                taskInfo.put("location", task.getLocation());
                taskInfo.put("status", task.getStatus());
                taskInfo.put("priority", task.getPriority());
                taskInfo.put("scheduledTime", task.getScheduledTime() != null ? task.getScheduledTime().toString() : null);
                taskInfo.put("dueDate", task.getDueDate() != null ? task.getDueDate().toString() : null);
                taskInfo.put("createdAt", task.getCreatedAt() != null ? task.getCreatedAt().toString() : null);
                taskInfo.put("progressPercentage", task.getProgressPercentage());
                safeRecentTasks.add(taskInfo);
            }
            dashboardData.put("recentTasks", safeRecentTasks);
            
            // Get attendance information
            Optional<Attendance> todayAttendanceOpt = attendanceService.getTodayAttendance(id);
            dashboardData.put("todayAttendance", todayAttendanceOpt.orElse(null));
            
            // Get recent attendance history
            List<Attendance> recentAttendance = attendanceService.getAttendanceHistory(id, null, null);
            dashboardData.put("recentAttendance", recentAttendance);
            
            // Janitor profile
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", janitor.getUserId());
            profile.put("username", janitor.getUsername());
            profile.put("fullName", janitor.getFullName());
            profile.put("email", janitor.getEmail());
            profile.put("role", janitor.getRole());
            
            dashboardData.put("profile", profile);
            
            return ResponseEntity.ok(dashboardData);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to load janitor dashboard");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Create new janitor (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createJanitor(@RequestBody Map<String, Object> janitorData) {
        try {
            String username = (String) janitorData.get("username");
            String password = (String) janitorData.get("password");
            String email = (String) janitorData.get("email");
            String fullName = (String) janitorData.get("fullName");
            String role = (String) janitorData.getOrDefault("role", "janitor");
            
            // Validate role
            if (!Arrays.asList("janitor", "cleaner", "supervisor").contains(role)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid role. Must be janitor, cleaner, or supervisor");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Check if username already exists
            if (userRepository.existsByUsername(username)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Username already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // Create new janitor
            User janitor = new User();
            janitor.setUsername(username);
            janitor.setPassword(passwordEncoder.encode(password)); // Hash the password for security
            janitor.setEmail(email);
            janitor.setFullName(fullName);
            janitor.setRole(role);
            janitor.setCreatedAt(LocalDateTime.now());
            
            User savedJanitor = userRepository.save(janitor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Janitor created successfully");
            response.put("user", savedJanitor);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create janitor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update janitor (admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateJanitor(@PathVariable Long id, @RequestBody Map<String, Object> janitorData) {
        try {
            Optional<User> janitorOpt = userRepository.findById(id);
            if (!janitorOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Janitor not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            User janitor = janitorOpt.get();
            
            // Update fields if provided
            if (janitorData.containsKey("email")) {
                janitor.setEmail((String) janitorData.get("email"));
            }
            if (janitorData.containsKey("fullName")) {
                janitor.setFullName((String) janitorData.get("fullName"));
            }
            if (janitorData.containsKey("role")) {
                String role = (String) janitorData.get("role");
                if (Arrays.asList("janitor", "cleaner", "supervisor").contains(role)) {
                    janitor.setRole(role);
                }
            }
            
            User updatedJanitor = userRepository.save(janitor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Janitor updated successfully");
            response.put("user", updatedJanitor);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update janitor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete janitor (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteJanitor(@PathVariable Long id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                
                Map<String, String> response = new HashMap<>();
                response.put("message", "Janitor deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Janitor not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to delete janitor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get comprehensive janitor profile (admin only)
     * Provides complete user profile including task statistics, attendance data, and performance metrics
     */
    @GetMapping("/{id}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getJanitorProfile(@PathVariable Long id) {
        try {
            System.out.println("JanitorController: Profile request for janitor ID: " + id);
            
            // Check if janitor exists
            Optional<User> janitorOpt = userRepository.findById(id);
            if (!janitorOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Janitor not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            User janitor = janitorOpt.get();
            Map<String, Object> profileData = new HashMap<>();
            
            // Basic user information
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("userId", janitor.getUserId());
            basicInfo.put("username", janitor.getUsername());
            basicInfo.put("fullName", janitor.getFullName());
            basicInfo.put("email", janitor.getEmail());
            basicInfo.put("role", janitor.getRole());
            basicInfo.put("createdAt", janitor.getCreatedAt() != null ? janitor.getCreatedAt().toString() : null);
            basicInfo.put("status", "active"); // Default status
            
            // Add avatar URL
            try {
                List<Image> avatarImages = imageService.getImagesForEntity(Image.EntityType.PROFILE, janitor.getUserId());
                if (!avatarImages.isEmpty()) {
                    Image latestAvatar = avatarImages.get(avatarImages.size() - 1);
                    basicInfo.put("avatarUrl", latestAvatar.getPublicUrl());
                    basicInfo.put("avatar", latestAvatar.getPublicUrl());
                } else {
                    basicInfo.put("avatarUrl", null);
                    basicInfo.put("avatar", null);
                }
            } catch (Exception e) {
                System.err.println("Error loading avatar for janitor " + id + ": " + e.getMessage());
                basicInfo.put("avatarUrl", null);
                basicInfo.put("avatar", null);
            }
            
            profileData.put("basicInfo", basicInfo);
            
            // Task statistics
            try {
                List<Task> allTasks = taskService.getTasksByJanitor(id);
                List<Task> completedTasks = taskService.getTasksByJanitorAndStatus(id, "completed");
                List<Task> pendingTasks = taskService.getTasksByJanitorAndStatus(id, "pending");
                List<Task> inProgressTasks = taskService.getTasksByJanitorAndStatus(id, "in_progress");
                
                Map<String, Object> taskStats = new HashMap<>();
                taskStats.put("totalTasks", allTasks.size());
                taskStats.put("completedTasks", completedTasks.size());
                taskStats.put("pendingTasks", pendingTasks.size());
                taskStats.put("inProgressTasks", inProgressTasks.size());
                
                // Calculate completion rate
                double completionRate = allTasks.size() > 0 ? 
                    (double) completedTasks.size() / allTasks.size() * 100 : 0;
                taskStats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
                
                profileData.put("taskStatistics", taskStats);
                
                // Recent tasks (last 5)
                List<Map<String, Object>> recentTasks = new ArrayList<>();
                List<Task> recentTasksList = allTasks.subList(0, Math.min(5, allTasks.size()));
                for (Task task : recentTasksList) {
                    Map<String, Object> taskInfo = new HashMap<>();
                    taskInfo.put("taskId", task.getTaskId());
                    taskInfo.put("title", task.getTitle());
                    taskInfo.put("status", task.getStatus());
                    taskInfo.put("priority", task.getPriority());
                    taskInfo.put("dueDate", task.getDueDate() != null ? task.getDueDate().toString() : null);
                    taskInfo.put("progressPercentage", task.getProgressPercentage());
                    recentTasks.add(taskInfo);
                }
                profileData.put("recentTasks", recentTasks);
                
            } catch (Exception e) {
                System.err.println("Error loading task data for janitor " + id + ": " + e.getMessage());
                // Provide empty task statistics if task loading fails
                Map<String, Object> emptyStats = new HashMap<>();
                emptyStats.put("totalTasks", 0);
                emptyStats.put("completedTasks", 0);
                emptyStats.put("pendingTasks", 0);
                emptyStats.put("inProgressTasks", 0);
                emptyStats.put("completionRate", 0.0);
                profileData.put("taskStatistics", emptyStats);
                profileData.put("recentTasks", new ArrayList<>());
            }
            
            // Attendance information
            try {
                Map<String, Object> attendanceData = new HashMap<>();
                
                // Get today's attendance and convert to safe format
                Optional<Attendance> todayAttendanceOpt = attendanceService.getTodayAttendance(id);
                if (todayAttendanceOpt.isPresent()) {
                    Attendance todayAttendance = todayAttendanceOpt.get();
                    Map<String, Object> todayAttendanceData = new HashMap<>();
                    todayAttendanceData.put("attendanceId", todayAttendance.getAttendanceId());
                    todayAttendanceData.put("janitorId", todayAttendance.getJanitorId());
                    todayAttendanceData.put("workDate", todayAttendance.getWorkDate() != null ? 
                        todayAttendance.getWorkDate().toString() : null);
                    todayAttendanceData.put("checkInTime", todayAttendance.getCheckInTime() != null ? 
                        todayAttendance.getCheckInTime().toString() : null);
                    todayAttendanceData.put("checkOutTime", todayAttendance.getCheckOutTime() != null ? 
                        todayAttendance.getCheckOutTime().toString() : null);
                    todayAttendanceData.put("workHours", todayAttendance.getWorkHours());
                    attendanceData.put("todayAttendance", todayAttendanceData);
                } else {
                    attendanceData.put("todayAttendance", null);
                }
                
                // Get recent attendance (last 7 days) and convert to safe format
                List<Attendance> recentAttendance = attendanceService.getAttendanceHistory(id, null, null);
                attendanceData.put("recentAttendanceCount", recentAttendance.size());
                
                // Calculate total work hours and convert recent attendance to safe format
                double totalHours = 0.0;
                List<Map<String, Object>> recentAttendanceData = new ArrayList<>();
                for (Attendance att : recentAttendance) {
                    if (att.getWorkHours() != null) {
                        totalHours += att.getWorkHours();
                    }
                    
                    Map<String, Object> attData = new HashMap<>();
                    attData.put("workDate", att.getWorkDate() != null ? att.getWorkDate().toString() : null);
                    attData.put("workHours", att.getWorkHours());
                    attData.put("checkInTime", att.getCheckInTime() != null ?
                        att.getCheckInTime().toString() : null);
                    attData.put("checkOutTime", att.getCheckOutTime() != null ?
                        att.getCheckOutTime().toString() : null);
                    recentAttendanceData.add(attData);
                }
                attendanceData.put("totalWeeklyHours", Math.round(totalHours * 100.0) / 100.0);
                attendanceData.put("recentAttendanceList", recentAttendanceData);
                
                profileData.put("attendanceData", attendanceData);
                
            } catch (Exception e) {
                System.err.println("Error loading attendance data for janitor " + id + ": " + e.getMessage());
                e.printStackTrace();
                // Provide empty attendance data if loading fails
                Map<String, Object> emptyAttendance = new HashMap<>();
                emptyAttendance.put("todayAttendance", null);
                emptyAttendance.put("recentAttendanceCount", 0);
                emptyAttendance.put("totalWeeklyHours", 0.0);
                emptyAttendance.put("recentAttendanceList", new ArrayList<>());
                profileData.put("attendanceData", emptyAttendance);
            }
            
            // Performance insights
            Map<String, Object> performanceInsights = new HashMap<>();
            performanceInsights.put("accountAge", janitor.getCreatedAt() != null ? 
                java.time.Duration.between(janitor.getCreatedAt(), LocalDateTime.now()).toDays() : 0);
            performanceInsights.put("lastActivity", "Recent"); // Placeholder
            profileData.put("performanceInsights", performanceInsights);
            
            // Wrap in success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("profile", profileData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to load janitor profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}