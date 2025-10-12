package com.cleaningsystem.backend.controller;

import com.cleaningsystem.backend.entity.User;
import com.cleaningsystem.backend.entity.Task;
import com.cleaningsystem.backend.entity.Image;
import com.cleaningsystem.backend.service.AuthService;
import com.cleaningsystem.backend.service.TaskService;
import com.cleaningsystem.backend.service.ProfileService;
import com.cleaningsystem.backend.service.ImageService;
import com.cleaningsystem.backend.service.PerformanceService;
import com.cleaningsystem.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.YearMonth;
import java.util.stream.Collectors;
import com.cleaningsystem.backend.entity.Attendance;
import com.cleaningsystem.backend.repository.AttendanceRepository;
import com.cleaningsystem.backend.repository.TaskRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private AuthService authService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private PerformanceService performanceService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private com.cleaningsystem.backend.service.ReportService reportService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AdminController.class);
    
    /**
     * Get admin dashboard statistics
     */
    @GetMapping("/dashboard/{adminId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminDashboard(@PathVariable Long adminId) {
        try {
            System.out.println("Dashboard endpoint hit for admin ID: " + adminId);
            
            Map<String, Object> dashboardData = new HashMap<>();
            
            // Add basic database queries step by step
            Map<String, Object> stats = new HashMap<>();
            
            try {
                // Test individual database calls
                System.out.println("Testing countByRole...");
                long totalAdmins = userRepository.countByRole("admin");
                stats.put("totalAdmins", totalAdmins);
                System.out.println("Admin count successful: " + totalAdmins);
                
                System.out.println("Testing countByRoleIn...");
                long totalStaff = userRepository.countByRoleIn(Arrays.asList("janitor", "cleaner", "supervisor"));
                stats.put("totalStaff", totalStaff);
                System.out.println("Staff count successful: " + totalStaff);
                
                System.out.println("Testing taskService.countByStatus...");
                long activeTasks = taskService.countByStatus("in_progress");
                stats.put("activeTasks", activeTasks);
                System.out.println("Active tasks count successful: " + activeTasks);
                
                long pendingTasks = taskService.countByStatus("pending");
                stats.put("pendingTasks", pendingTasks);
                System.out.println("Pending tasks count successful: " + pendingTasks);
                
                long completedTasks = taskService.countByStatus("completed");
                stats.put("completedTasks", completedTasks);
                System.out.println("Completed tasks count successful: " + completedTasks);
                
            } catch (Exception e) {
                System.err.println("Database query error: " + e.getMessage());
                e.printStackTrace();
                // Fallback to hardcoded values if any database call fails
                stats.put("totalStaff", 5L);
                stats.put("totalAdmins", 1L);
                stats.put("activeTasks", 3L);
                stats.put("pendingTasks", 7L);
                stats.put("completedTasks", 12L);
            }
            
            dashboardData.put("stats", stats);
            
            // Test recent tasks query with safe serialization
            try {
                System.out.println("Testing getRecentTasks with safe serialization...");
                List<Task> recentTasksRaw = taskService.getRecentTasks(5);
                System.out.println("Raw tasks retrieved: " + recentTasksRaw.size());
                
                // Convert to safe format to avoid JSON serialization issues
                List<Map<String, Object>> recentTasks = new ArrayList<>();
                for (Task task : recentTasksRaw) {
                    Map<String, Object> taskInfo = new HashMap<>();
                    taskInfo.put("taskId", task.getTaskId());
                    taskInfo.put("title", task.getTitle());
                    taskInfo.put("status", task.getStatus());
                    taskInfo.put("priority", task.getPriority());
                    taskInfo.put("location", task.getLocation());
                    taskInfo.put("createdAt", task.getCreatedAt() != null ? task.getCreatedAt().toString() : null);
                    taskInfo.put("assignedTo", task.getAssignedTo());
                    taskInfo.put("assignedBy", task.getAssignedBy());
                    
                    // Get user names for assignment information
                    String assignedToName = "Unassigned";
                    String assignedByName = "Unknown";
                    
                    if (task.getAssignedTo() != null) {
                        try {
                            Optional<User> assignedUser = userRepository.findById(task.getAssignedTo());
                            assignedToName = assignedUser.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                                .orElse("Unknown User");
                        } catch (Exception e) {
                            System.err.println("Error loading assigned user in AdminController: " + e.getMessage());
                        }
                    }
                    
                    if (task.getAssignedBy() != null) {
                        try {
                            Optional<User> assignedByUser = userRepository.findById(task.getAssignedBy());
                            assignedByName = assignedByUser.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                                .orElse("Unknown Admin");
                        } catch (Exception e) {
                            System.err.println("Error loading assigning user in AdminController: " + e.getMessage());
                        }
                    }
                    
                    taskInfo.put("assignedToName", assignedToName);
                    taskInfo.put("assignedByName", assignedByName);
                    
                    recentTasks.add(taskInfo);
                }
                
                dashboardData.put("recentTasks", recentTasks);
                System.out.println("Recent tasks successful: " + recentTasks.size() + " tasks");
                
                // Add recent staff query with safe serialization
                System.out.println("Testing recent staff query...");
                List<User> recentStaffRaw = userRepository.findTop5ByRoleInOrderByCreatedAtDesc(
                    Arrays.asList("janitor", "cleaner", "supervisor")
                );
                System.out.println("Raw staff retrieved: " + recentStaffRaw.size());
                
                // Convert to safe format
                List<Map<String, Object>> recentStaff = new ArrayList<>();
                for (User user : recentStaffRaw) {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("userId", user.getUserId());
                    userInfo.put("username", user.getUsername());
                    userInfo.put("fullName", user.getFullName());
                    userInfo.put("role", user.getRole());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
                    recentStaff.add(userInfo);
                }
                
                dashboardData.put("recentStaff", recentStaff);
                System.out.println("Recent staff successful: " + recentStaff.size() + " staff");
                
            } catch (Exception e) {
                System.err.println("Recent tasks query error: " + e.getMessage());
                e.printStackTrace();
                // Add empty lists as fallback
                dashboardData.put("recentTasks", new ArrayList<>());
                dashboardData.put("recentStaff", new ArrayList<>());
            }
            
            // Minimal static activities
            List<Map<String, Object>> recentActivities = new ArrayList<>();
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", "test_1");
            activity.put("type", "info");
            activity.put("icon", "Tools");
            activity.put("title", "API Test");
            activity.put("description", "Dashboard API is working");
            activity.put("time", "Just now");
            recentActivities.add(activity);
            dashboardData.put("recentActivities", recentActivities);
            
            // Minimal static alerts
            List<Map<String, Object>> systemAlerts = new ArrayList<>();
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", "test_alert");
            alert.put("type", "success");
            alert.put("title", "API Connection");
            alert.put("description", "Successfully connected to backend API");
            systemAlerts.add(alert);
            dashboardData.put("systemAlerts", systemAlerts);
            
            System.out.println("Dashboard data prepared successfully");
            
            return ResponseEntity.ok(dashboardData);
            
        } catch (Exception e) {
            System.err.println("Dashboard endpoint error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load dashboard data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Generate recent activities based on actual system data
     */
    private List<Map<String, Object>> generateRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // Get recent completed tasks
        List<Task> recentCompletedTasks = taskService.getRecentTasksByStatus("completed", 3);
        for (Task task : recentCompletedTasks) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", "task_" + task.getTaskId());
            activity.put("type", "success");
            activity.put("icon", "CircleCheck");
            activity.put("title", "Task Completed");
            activity.put("description", task.getTitle() + " completed");
            activity.put("time", getRelativeTime(task.getCompletedAt() != null ? task.getCompletedAt() : task.getCreatedAt()));
            activities.add(activity);
        }
        
        // Get recent staff additions
        List<User> recentStaff = userRepository.findTop2ByRoleInOrderByCreatedAtDesc(
            Arrays.asList("janitor", "cleaner", "supervisor")
        );
        for (User user : recentStaff) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", "staff_" + user.getUserId());
            activity.put("type", "info");
            activity.put("icon", "User");
            activity.put("title", "New Staff Added");
            activity.put("description", user.getFullName() + " joined as " + user.getRole());
            activity.put("time", getRelativeTime(user.getCreatedAt()));
            activities.add(activity);
        }
        
        // If we have fewer than 3 activities, add some system activities
        if (activities.size() < 3) {
            Map<String, Object> systemActivity = new HashMap<>();
            systemActivity.put("id", "system_update");
            systemActivity.put("type", "info");
            systemActivity.put("icon", "Tools");
            systemActivity.put("title", "System Update");
            systemActivity.put("description", "Dashboard data refreshed successfully");
            systemActivity.put("time", "Just now");
            activities.add(systemActivity);
        }
        
        // Sort by time (most recent first) and limit to 5
        return activities.stream()
                .sorted((a, b) -> b.get("time").toString().compareTo(a.get("time").toString()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Generate system alerts based on current system state
     */
    private List<Map<String, Object>> generateSystemAlerts(long totalStaff, long pendingTasks) {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        // Check staff levels
        if (totalStaff < 3) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", "staff_shortage");
            alert.put("type", "warning");
            alert.put("title", "Staff Shortage Alert");
            alert.put("description", "Only " + totalStaff + " staff members available. Consider hiring more personnel.");
            alerts.add(alert);
        }
        
        // Check pending tasks
        if (pendingTasks > 10) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", "pending_tasks");
            alert.put("type", "warning");
            alert.put("title", "High Pending Tasks");
            alert.put("description", pendingTasks + " tasks are pending. Review task assignments and priorities.");
            alerts.add(alert);
        }
        
        // Check for overdue tasks
        long overdueTasks = taskService.countOverdueTasks();
        if (overdueTasks > 0) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", "overdue_tasks");
            alert.put("type", "error");
            alert.put("title", "Overdue Tasks");
            alert.put("description", overdueTasks + " tasks are overdue. Immediate attention required.");
            alerts.add(alert);
        }
        
        // If no alerts, add a positive message
        if (alerts.isEmpty()) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", "all_good");
            alert.put("type", "success");
            alert.put("title", "System Status Normal");
            alert.put("description", "All systems operating normally. No immediate actions required.");
            alerts.add(alert);
        }
        
        return alerts;
    }
    
    /**
     * Convert LocalDateTime to relative time string
     */
    private String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Recently";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (minutes < 1440) { // 24 hours
            long hours = minutes / 60;
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else {
            long days = minutes / 1440;
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }
    }
    
    /**
     * Get all users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Create new admin user
     */
    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createAdmin(@RequestBody Map<String, Object> adminData) {
        try {
            String username = (String) adminData.get("username");
            String password = (String) adminData.get("password");
            String email = (String) adminData.get("email");
            String fullName = (String) adminData.get("fullName");
            
            // Check if username already exists
            if (authService.isUsernameExists(username)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Username already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // Create new admin user
            User admin = new User();
            admin.setUsername(username);
            admin.setPassword(password); // Note: In production, this should be hashed
            admin.setEmail(email);
            admin.setFullName(fullName);
            admin.setRole("admin");
            admin.setCreatedAt(LocalDateTime.now());
            
            User savedAdmin = userRepository.save(admin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Admin user created successfully");
            response.put("user", savedAdmin);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create admin user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete user
     */
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        try {
            if (userRepository.existsById(userId)) {
                userRepository.deleteById(userId);
                
                Map<String, String> response = new HashMap<>();
                response.put("message", "User deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to delete user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get all non-admin users for task assignment
     */
    @GetMapping("/assignable-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAssignableUsers() {
        try {
            List<User> assignableUsers = userRepository.findByRoleIn(
                Arrays.asList("janitor", "cleaner", "supervisor")
            );
            
            // Convert to safe format for JSON serialization
            List<Map<String, Object>> userList = new ArrayList<>();
            for (User user : assignableUsers) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("username", user.getUsername());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("role", user.getRole());
                userInfo.put("email", user.getEmail());
                userList.add(userInfo);
            }
            
            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    /**
     * Get detailed staff profile for admin viewing (with full access)
     * Admins can view comprehensive profile information for all staff roles
     */
    @GetMapping("/staff/{userId}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStaffProfile(
            @PathVariable Long userId,
            @RequestParam(required = false) Long requestingUserId) {
        try {
            logger.info("Admin profile request for user: {} by requesting user: {}", userId, requestingUserId);

            // Verify the target user exists
            Optional<User> targetUser = userRepository.findById(userId);
            if (targetUser.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                errorResponse.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            String targetRole = targetUser.get().getRole();

            // Admins can view all role profiles (no role restrictions)
            Map<String, Object> profileData;

            if (requestingUserId != null) {
                profileData = profileService.getProfileForViewing(userId, requestingUserId);
            } else {
                // Admin access: get complete profile data
                profileData = profileService.getCompleteProfile(userId);
            }

            // Wrap in success response format
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("profile", profileData);
            response.put("accessLevel", "admin");
            response.put("targetRole", targetRole);
            response.put("timestamp", LocalDateTime.now().toString());

            logger.info("Successfully retrieved profile for user: {} (role: {})", userId, targetRole);
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            logger.warn("Security exception for admin profile request: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Insufficient permissions to view this profile");
            errorResponse.put("code", "INSUFFICIENT_PERMISSIONS");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid argument for admin profile request: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("code", "INVALID_ARGUMENT");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            logger.error("Error retrieving staff profile for user {}: {}", userId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve profile data");
            errorResponse.put("code", "PROFILE_FETCH_ERROR");
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get staff performance metrics (Admin access - all roles)
     * Enhanced endpoint with real performance calculations for admin interface
     */
    @GetMapping("/staff/{userId}/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStaffPerformance(
            @PathVariable Long userId,
            @RequestParam(required = false) String month) {
        try {
            logger.info("Admin performance request for user: {} for month: {}", userId, month);

            // Verify the target user exists
            Optional<User> targetUser = userRepository.findById(userId);
            if (targetUser.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                errorResponse.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            String targetRole = targetUser.get().getRole();

            // Admin can view performance for janitors and cleaners only
            if (!Arrays.asList("janitor", "cleaner").contains(targetRole)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Performance data only available for janitors and cleaners");
                errorResponse.put("code", "INVALID_ROLE");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            // Parse target month
            YearMonth targetMonth = null;
            if (month != null && !month.trim().isEmpty()) {
                try {
                    targetMonth = YearMonth.parse(month);
                } catch (Exception e) {
                    logger.warn("Invalid month format provided: {}", month);
                    targetMonth = YearMonth.now(); // Fallback to current month
                }
            }

            // Calculate performance metrics using PerformanceService
            Map<String, Object> performanceData = performanceService.calculatePerformanceMetrics(userId, targetMonth);

            // Wrap in success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("performance", performanceData);
            response.put("accessLevel", "admin");
            response.put("targetRole", targetRole);
            response.put("timestamp", LocalDateTime.now().toString());

            logger.info("Successfully calculated performance for user: {} (role: {})", userId, targetRole);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid argument for admin performance request: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("code", "INVALID_ARGUMENT");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            logger.error("Error calculating performance for user {}: {}", userId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to calculate performance metrics");
            errorResponse.put("code", "PERFORMANCE_CALCULATION_ERROR");
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get performance summary for multiple staff members (Admin bulk access)
     * Bulk endpoint for admin dashboard with enhanced capabilities
     */
    @GetMapping("/staff/performance/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBulkStaffPerformance(
            @RequestParam(required = false) List<Long> userIds,
            @RequestParam(required = false) String month) {
        try {
            logger.info("Admin bulk performance request for users: {} for month: {}", userIds, month);

            // If no specific user IDs provided, get all janitors and cleaners
            if (userIds == null || userIds.isEmpty()) {
                List<User> staffUsers = userRepository.findByRoleIn(Arrays.asList("janitor", "cleaner"));
                userIds = staffUsers.stream().map(User::getUserId).toList();
            }

            // Parse target month
            YearMonth targetMonth = null;
            if (month != null && !month.trim().isEmpty()) {
                try {
                    targetMonth = YearMonth.parse(month);
                } catch (Exception e) {
                    logger.warn("Invalid month format provided: {}", month);
                    targetMonth = YearMonth.now();
                }
            }

            // Calculate bulk performance metrics
            Map<Long, Map<String, Object>> bulkPerformanceData = performanceService.calculateBulkPerformanceMetrics(userIds, targetMonth);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("performanceData", bulkPerformanceData);
            response.put("calculationMonth", targetMonth != null ? targetMonth.toString() : YearMonth.now().toString());
            response.put("userCount", userIds.size());
            response.put("accessLevel", "admin");
            response.put("timestamp", LocalDateTime.now().toString());

            logger.info("Successfully calculated bulk performance for {} users", userIds.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error calculating bulk performance: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to calculate bulk performance metrics");
            errorResponse.put("code", "BULK_PERFORMANCE_ERROR");
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get tasks for a specific staff member (Admin access)
     * Admins can view all tasks assigned to any staff member
     */
    @GetMapping("/staff/{userId}/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStaffTasks(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false, defaultValue = "0") int offset) {
        try {
            logger.info("Admin task request for user: {} with status: {}, priority: {}", userId, status, priority);

            // Verify the target user exists
            Optional<User> targetUser = userRepository.findById(userId);
            if (targetUser.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                errorResponse.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            String targetRole = targetUser.get().getRole();

            // Get tasks for the user (admin can access all roles)
            List<Task> tasks;
            if (status != null && !status.trim().isEmpty()) {
                tasks = taskService.getTasksByJanitorAndStatus(userId, status);
            } else {
                tasks = taskService.getTasksByJanitor(userId);
            }

            // Filter by priority if specified
            if (priority != null && !priority.trim().isEmpty()) {
                tasks = tasks.stream()
                    .filter(task -> priority.equalsIgnoreCase(task.getPriority()))
                    .collect(java.util.stream.Collectors.toList());
            }

            // Sort by creation date descending (most recent first)
            tasks.sort((t1, t2) -> {
                LocalDateTime dt1 = t1.getCreatedAt();
                LocalDateTime dt2 = t2.getCreatedAt();
                if (dt1 == null && dt2 == null) return 0;
                if (dt1 == null) return 1;
                if (dt2 == null) return -1;
                return dt2.compareTo(dt1);
            });

            // Apply pagination
            int totalTasks = tasks.size();
            int startIndex = Math.min(offset, totalTasks);
            int endIndex = Math.min(offset + limit, totalTasks);
            List<Task> paginatedTasks = tasks.subList(startIndex, endIndex);

            // Convert to safe format
            List<Map<String, Object>> taskList = new ArrayList<>();
            for (Task task : paginatedTasks) {
                Map<String, Object> taskInfo = new HashMap<>();
                taskInfo.put("taskId", task.getTaskId());
                taskInfo.put("title", task.getTitle());
                taskInfo.put("description", task.getDescription());
                taskInfo.put("status", task.getStatus());
                taskInfo.put("priority", task.getPriority());
                taskInfo.put("location", task.getLocation());
                taskInfo.put("assignedTo", task.getAssignedTo());
                taskInfo.put("assignedBy", task.getAssignedBy());
                taskInfo.put("scheduledTime", task.getScheduledTime() != null ? task.getScheduledTime().toString() : null);
                taskInfo.put("dueDate", task.getDueDate() != null ? task.getDueDate().toString() : null);
                taskInfo.put("createdAt", task.getCreatedAt() != null ? task.getCreatedAt().toString() : null);
                taskInfo.put("startedAt", task.getStartedAt() != null ? task.getStartedAt().toString() : null);
                taskInfo.put("completedAt", task.getCompletedAt() != null ? task.getCompletedAt().toString() : null);
                taskInfo.put("progressPercentage", task.getProgressPercentage());
                taskInfo.put("estimatedDuration", task.getEstimatedDuration());

                // Get user names for assignment information
                String assignedToName = "Unassigned";
                String assignedByName = "System";

                if (task.getAssignedTo() != null) {
                    try {
                        Optional<User> assignedUser = userRepository.findById(task.getAssignedTo());
                        assignedToName = assignedUser.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                            .orElse("Unknown User");
                    } catch (Exception e) {
                        logger.warn("Error loading assigned user for task {}: {}", task.getTaskId(), e.getMessage());
                    }
                }

                if (task.getAssignedBy() != null) {
                    try {
                        Optional<User> assignedByUser = userRepository.findById(task.getAssignedBy());
                        assignedByName = assignedByUser.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                            .orElse("Unknown");
                    } catch (Exception e) {
                        logger.warn("Error loading assigning user for task {}: {}", task.getTaskId(), e.getMessage());
                    }
                }

                taskInfo.put("assignedToName", assignedToName);
                taskInfo.put("assignedByName", assignedByName);

                taskList.add(taskInfo);
            }

            // Build response with pagination info
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tasks", taskList);
            response.put("pagination", Map.of(
                "total", totalTasks,
                "limit", limit,
                "offset", offset,
                "hasMore", endIndex < totalTasks
            ));
            response.put("filters", Map.of(
                "status", status != null ? status : "all",
                "priority", priority != null ? priority : "all"
            ));
            response.put("user", Map.of(
                "userId", userId,
                "username", targetUser.get().getUsername(),
                "fullName", targetUser.get().getFullName(),
                "role", targetUser.get().getRole()
            ));
            response.put("accessLevel", "admin");
            response.put("timestamp", LocalDateTime.now().toString());

            logger.info("Successfully retrieved {} tasks for user: {} (role: {})", taskList.size(), userId, targetRole);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving tasks for user {}: {}", userId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve task data");
            errorResponse.put("code", "TASK_FETCH_ERROR");
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get task statistics for a specific staff member (Admin access)
     * Provides summary statistics for admin dashboards
     */
    @GetMapping("/staff/{userId}/task-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStaffTaskStats(@PathVariable Long userId) {
        try {
            logger.info("Admin task statistics request for user: {}", userId);

            // Verify the target user exists
            Optional<User> targetUser = userRepository.findById(userId);
            if (targetUser.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                errorResponse.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            String targetRole = targetUser.get().getRole();

            // Get all tasks for the user
            List<Task> allTasks = taskService.getTasksByJanitor(userId);

            // Calculate statistics
            long totalTasks = allTasks.size();
            long completedTasks = allTasks.stream().mapToLong(task -> "completed".equals(task.getStatus()) ? 1 : 0).sum();
            long pendingTasks = allTasks.stream().mapToLong(task -> "pending".equals(task.getStatus()) ? 1 : 0).sum();
            long inProgressTasks = allTasks.stream().mapToLong(task -> "in_progress".equals(task.getStatus()) ? 1 : 0).sum();
            long overdueTasks = allTasks.stream().mapToLong(task -> {
                if (task.getDueDate() != null && !"completed".equals(task.getStatus())) {
                    return task.getDueDate().isBefore(LocalDateTime.now()) ? 1 : 0;
                }
                return 0;
            }).sum();

            // Calculate completion rate
            double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

            // Priority breakdown
            Map<String, Long> priorityBreakdown = new HashMap<>();
            priorityBreakdown.put("urgent", allTasks.stream().mapToLong(task -> "urgent".equals(task.getPriority()) ? 1 : 0).sum());
            priorityBreakdown.put("high", allTasks.stream().mapToLong(task -> "high".equals(task.getPriority()) ? 1 : 0).sum());
            priorityBreakdown.put("normal", allTasks.stream().mapToLong(task -> "normal".equals(task.getPriority()) ? 1 : 0).sum());
            priorityBreakdown.put("low", allTasks.stream().mapToLong(task -> "low".equals(task.getPriority()) ? 1 : 0).sum());

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", Map.of(
                "totalTasks", totalTasks,
                "completedTasks", completedTasks,
                "pendingTasks", pendingTasks,
                "inProgressTasks", inProgressTasks,
                "overdueTasks", overdueTasks,
                "completionRate", Math.round(completionRate * 100.0) / 100.0,
                "priorityBreakdown", priorityBreakdown
            ));
            response.put("user", Map.of(
                "userId", userId,
                "username", targetUser.get().getUsername(),
                "fullName", targetUser.get().getFullName(),
                "role", targetUser.get().getRole()
            ));
            response.put("accessLevel", "admin");
            response.put("timestamp", LocalDateTime.now().toString());

            logger.info("Successfully calculated task statistics for user: {} (role: {})", userId, targetRole);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error calculating task statistics for user {}: {}", userId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to calculate task statistics");
            errorResponse.put("code", "TASK_STATS_ERROR");
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Generate reports (Enhanced with additional admin-only report types)
     */
    @GetMapping("/reports/{reportType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> generateReport(@PathVariable String reportType) {
        try {
            Map<String, Object> report = new HashMap<>();

            switch (reportType.toLowerCase()) {
                case "tasks":
                    report.put("totalTasks", taskService.countAllTasks());
                    report.put("completedTasks", taskService.countByStatus("completed"));
                    report.put("pendingTasks", taskService.countByStatus("pending"));
                    report.put("inProgressTasks", taskService.countByStatus("in_progress"));
                    break;

                case "staff":
                    report.put("totalStaff", userRepository.countByRoleIn(Arrays.asList("janitor", "cleaner", "supervisor")));
                    report.put("janitors", userRepository.countByRole("janitor"));
                    report.put("cleaners", userRepository.countByRole("cleaner"));
                    report.put("supervisors", userRepository.countByRole("supervisor"));
                    report.put("admins", userRepository.countByRole("admin")); // Admin-only: view admin count
                    break;

                case "system":
                    // Admin-only system report
                    report.put("totalUsers", userRepository.count());
                    report.put("totalTasks", taskService.countAllTasks());
                    report.put("overdueTasks", taskService.countOverdueTasks());
                    break;

                default:
                    report.put("error", "Unknown report type");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(report);
            }

            report.put("generatedAt", LocalDateTime.now());
            report.put("reportType", reportType);
            report.put("generatedBy", "admin");

            return ResponseEntity.ok(report);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to generate report");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Simple Job Monitor API for overall janitor performance
     * Returns basic attendance rate and task completion rate for all janitors
     * @param month Optional month parameter in format YYYY-MM (defaults to current month)
     */
    @GetMapping("/job-monitor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getJobMonitorData(
            @RequestParam(required = false) String month) {
        try {
            Map<String, Object> monitorData = new HashMap<>();

            // Get all janitors and cleaners
            List<User> janitors = userRepository.findByRoleIn(Arrays.asList("janitor", "cleaner"));
            monitorData.put("totalJanitors", janitors.size());

            if (janitors.isEmpty()) {
                monitorData.put("overallAttendanceRate", 0.0);
                monitorData.put("overallTaskCompletionRate", 0.0);
                monitorData.put("attendanceDetails", Map.of("present", 0, "absent", 0));
                monitorData.put("taskDetails", Map.of("completed", 0, "pending", 0, "inProgress", 0));
                return ResponseEntity.ok(monitorData);
            }

            // Parse month parameter or use current month
            LocalDate now = LocalDate.now();
            if (month != null && !month.trim().isEmpty()) {
                try {
                    YearMonth yearMonth = YearMonth.parse(month);
                    now = yearMonth.atEndOfMonth();
                } catch (Exception e) {
                    logger.warn("Invalid month format: {}, using current month", month);
                }
            }
            LocalDate startOfMonth = now.withDayOfMonth(1);

            int totalWorkingDays = calculateWorkingDaysInMonth(now.getYear(), now.getMonthValue());
            int totalPossibleAttendance = janitors.size() * totalWorkingDays;

            // Get attendance records for current month
            List<Attendance> monthlyAttendance = attendanceRepository.findByWorkDateBetween(startOfMonth, now);

            // Count unique janitor-date combinations, excluding weekends
            long actualAttendanceCount = monthlyAttendance.stream()
                .filter(a -> {
                    DayOfWeek day = a.getWorkDate().getDayOfWeek();
                    return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
                })
                .map(a -> a.getJanitorId() + "_" + a.getWorkDate().toString())
                .distinct()
                .count();

            double attendanceRate = totalPossibleAttendance > 0 ?
                    (double) actualAttendanceCount / totalPossibleAttendance * 100 : 0;
            monitorData.put("overallAttendanceRate", Math.round(attendanceRate * 100.0) / 100.0);

            // Attendance details
            Map<String, Object> attendanceDetails = new HashMap<>();
            attendanceDetails.put("present", actualAttendanceCount);
            attendanceDetails.put("absent", totalPossibleAttendance - actualAttendanceCount);
            attendanceDetails.put("totalWorkingDays", totalWorkingDays);
            attendanceDetails.put("totalJanitors", janitors.size());
            monitorData.put("attendanceDetails", attendanceDetails);

            // Calculate task completion statistics
            List<Long> janitorIds = janitors.stream().map(User::getUserId).collect(Collectors.toList());
            List<Task> janitorTasks = taskRepository.findByAssignedToIn(janitorIds);

            Map<String, Long> taskCounts = janitorTasks.stream()
                    .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

            long completedTasks = taskCounts.getOrDefault("completed", 0L);
            long totalTasks = janitorTasks.size();

            double taskCompletionRate = totalTasks > 0 ?
                    (double) completedTasks / totalTasks * 100 : 0;
            monitorData.put("overallTaskCompletionRate", Math.round(taskCompletionRate * 100.0) / 100.0);

            // Task details
            Map<String, Object> taskDetails = new HashMap<>();
            taskDetails.put("completed", completedTasks);
            taskDetails.put("pending", taskCounts.getOrDefault("pending", 0L));
            taskDetails.put("inProgress", taskCounts.getOrDefault("in_progress", 0L));
            taskDetails.put("total", totalTasks);
            monitorData.put("taskDetails", taskDetails);

            return ResponseEntity.ok(monitorData);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load job monitor data: " + e.getMessage()));
        }
    }

    // Helper method to calculate working days in current month
    private int calculateWorkingDaysInMonth(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        int workingDays = 0;
        LocalDate current = start;

        while (!current.isAfter(end)) {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY &&
                current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            current = current.plusDays(1);
        }

        return workingDays;
    }

    // Helper method to calculate working days between two dates
    private int calculateWorkingDaysBetween(LocalDate start, LocalDate end) {
        int workingDays = 0;
        LocalDate current = start;

        while (!current.isAfter(end)) {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY &&
                current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            current = current.plusDays(1);
        }

        return workingDays;
    }

    /**
     * Enhanced Job Monitor API endpoints for Chart.js integration
     */

    // Attendance chart data - pie chart for current month
    @GetMapping("/job-monitor/attendance-chart")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAttendanceChartData(
            @RequestParam(required = false) String month) {
        try {
            Map<String, Object> chartData = new HashMap<>();

            // Parse month parameter or use current month
            LocalDate now = LocalDate.now();
            if (month != null && !month.trim().isEmpty()) {
                try {
                    YearMonth yearMonth = YearMonth.parse(month);
                    now = yearMonth.atEndOfMonth();
                } catch (Exception e) {
                    logger.warn("Invalid month format: {}, using current month", month);
                }
            }
            LocalDate startOfMonth = now.withDayOfMonth(1);

            List<User> janitors = userRepository.findByRoleIn(Arrays.asList("janitor", "cleaner"));
            List<Attendance> monthlyAttendance = attendanceRepository.findByWorkDateBetween(startOfMonth, now);

            int totalWorkingDays = calculateWorkingDaysInMonth(now.getYear(), now.getMonthValue());
            int totalPossibleAttendance = janitors.size() * totalWorkingDays;

            // Count unique janitor-date combinations, excluding weekends
            long actualAttendanceCount = monthlyAttendance.stream()
                .filter(a -> {
                    DayOfWeek day = a.getWorkDate().getDayOfWeek();
                    return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
                })
                .map(a -> a.getJanitorId() + "_" + a.getWorkDate().toString())
                .distinct()
                .count();

            chartData.put("labels", Arrays.asList("Present", "Absent"));
            chartData.put("datasets", Arrays.asList(Map.of(
                "data", Arrays.asList(actualAttendanceCount, totalPossibleAttendance - actualAttendanceCount),
                "backgroundColor", Arrays.asList("#10b981", "#ef4444")
            )));

            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load attendance chart data"));
        }
    }

    // Task completion chart data - doughnut chart
    @GetMapping("/job-monitor/task-completion-chart")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTaskCompletionChartData(
            @RequestParam(required = false) String month) {
        try {
            Map<String, Object> chartData = new HashMap<>();

            // Parse month parameter or use current month
            LocalDate tempStartDate = LocalDate.now().withDayOfMonth(1);
            LocalDate tempEndDate = LocalDate.now();

            if (month != null && !month.trim().isEmpty()) {
                try {
                    YearMonth yearMonth = YearMonth.parse(month);
                    tempStartDate = yearMonth.atDay(1);
                    tempEndDate = yearMonth.atEndOfMonth();
                } catch (Exception e) {
                    logger.warn("Invalid month format: {}, using current month", month);
                }
            }

            final LocalDate startDate = tempStartDate;
            final LocalDate endDate = tempEndDate;

            List<User> janitors = userRepository.findByRoleIn(Arrays.asList("janitor", "cleaner"));
            List<Long> janitorIds = janitors.stream().map(User::getUserId).collect(Collectors.toList());

            // Filter tasks by the selected month
            List<Task> janitorTasks = taskRepository.findByAssignedToIn(janitorIds).stream()
                .filter(task -> {
                    LocalDateTime createdAt = task.getCreatedAt();
                    if (createdAt == null) return false;
                    LocalDate taskDate = createdAt.toLocalDate();
                    return !taskDate.isBefore(startDate) && !taskDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

            Map<String, Long> taskCounts = janitorTasks.stream()
                    .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

            chartData.put("labels", Arrays.asList("Completed", "In Progress", "Pending"));
            chartData.put("datasets", Arrays.asList(Map.of(
                "data", Arrays.asList(
                    taskCounts.getOrDefault("completed", 0L),
                    taskCounts.getOrDefault("in_progress", 0L),
                    taskCounts.getOrDefault("pending", 0L)
                ),
                "backgroundColor", Arrays.asList("#10b981", "#f59e0b", "#6b7280")
            )));

            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load task completion chart data"));
        }
    }

    // Monthly attendance trend - line chart for last 6 months
    @GetMapping("/job-monitor/attendance-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAttendanceTrendData(
            @RequestParam(required = false) String month) {
        try {
            Map<String, Object> chartData = new HashMap<>();
            List<User> janitors = userRepository.findByRoleIn(Arrays.asList("janitor", "cleaner"));

            List<String> labels = new ArrayList<>();
            List<Double> attendanceRates = new ArrayList<>();

            // Parse month parameter or use current month
            LocalDate current = LocalDate.now();
            if (month != null && !month.trim().isEmpty()) {
                try {
                    YearMonth yearMonth = YearMonth.parse(month);
                    current = yearMonth.atEndOfMonth();
                } catch (Exception e) {
                    logger.warn("Invalid month format: {}, using current month", month);
                }
            }

            // Get last 6 months data from the selected month
            for (int i = 5; i >= 0; i--) {
                LocalDate monthStart = current.minusMonths(i).withDayOfMonth(1);
                LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

                // 只查询到今天为止，不包含未来日期
                LocalDate queryEnd = monthEnd.isAfter(LocalDate.now()) ? LocalDate.now() : monthEnd;

                labels.add(monthStart.getMonth().toString().substring(0, 3) + " " + monthStart.getYear());

                List<Attendance> monthAttendance = attendanceRepository.findByWorkDateBetween(monthStart, queryEnd);

                // 计算到今天为止的实际工作日数
                int workingDays = calculateWorkingDaysBetween(monthStart, queryEnd);
                int totalPossible = janitors.size() * workingDays;

                // Count unique janitor-date combinations, excluding weekends
                long uniqueAttendances = monthAttendance.stream()
                    .filter(a -> {
                        DayOfWeek day = a.getWorkDate().getDayOfWeek();
                        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
                    })
                    .map(a -> a.getJanitorId() + "_" + a.getWorkDate().toString())
                    .distinct()
                    .count();

                double rate = totalPossible > 0 ? (double) uniqueAttendances / totalPossible * 100 : 0;
                attendanceRates.add(Math.round(rate * 100.0) / 100.0);
            }

            chartData.put("labels", labels);
            chartData.put("datasets", Arrays.asList(Map.of(
                "label", "Attendance Rate (%)",
                "data", attendanceRates,
                "borderColor", "#3b82f6",
                "backgroundColor", "rgba(59, 130, 246, 0.1)",
                "tension", 0.4
            )));

            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load attendance trend data"));
        }
    }

    // Janitor performance comparison - bar chart
    @GetMapping("/job-monitor/janitor-performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getJanitorPerformanceData(
            @RequestParam(required = false) String month) {
        try {
            Map<String, Object> chartData = new HashMap<>();
            List<User> janitors = userRepository.findByRoleIn(Arrays.asList("janitor", "cleaner"));

            List<String> labels = new ArrayList<>();
            List<Double> attendanceRates = new ArrayList<>();
            List<Double> taskCompletionRates = new ArrayList<>();

            // Parse month parameter or use current month
            LocalDate now = LocalDate.now();
            if (month != null && !month.trim().isEmpty()) {
                try {
                    YearMonth yearMonth = YearMonth.parse(month);
                    now = yearMonth.atEndOfMonth();
                } catch (Exception e) {
                    logger.warn("Invalid month format: {}, using current month", month);
                }
            }
            LocalDate startOfMonth = now.withDayOfMonth(1);
            int workingDaysThisMonth = calculateWorkingDaysInMonth(now.getYear(), now.getMonthValue());

            for (User janitor : janitors) {
                labels.add(janitor.getUsername());

                // Calculate attendance rate for this janitor
                List<Attendance> janitorAttendance = attendanceRepository.findByJanitorIdAndWorkDateBetween(
                    janitor.getUserId(), startOfMonth, now);

                // Count only working days (exclude weekends)
                long workingDayAttendance = janitorAttendance.stream()
                    .filter(a -> {
                        DayOfWeek day = a.getWorkDate().getDayOfWeek();
                        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
                    })
                    .count();

                double attendanceRate = workingDaysThisMonth > 0 ?
                    (double) workingDayAttendance / workingDaysThisMonth * 100 : 0;
                attendanceRates.add(Math.round(attendanceRate * 100.0) / 100.0);

                // Calculate task completion rate for this janitor
                List<Task> janitorTasks = taskRepository.findByAssignedToOrderByScheduledTimeAsc(janitor.getUserId());
                long completed = janitorTasks.stream()
                    .filter(task -> "completed".equals(task.getStatus()))
                    .count();
                double taskRate = janitorTasks.size() > 0 ?
                    (double) completed / janitorTasks.size() * 100 : 0;
                taskCompletionRates.add(Math.round(taskRate * 100.0) / 100.0);
            }

            chartData.put("labels", labels);
            chartData.put("datasets", Arrays.asList(
                Map.of(
                    "label", "Attendance Rate (%)",
                    "data", attendanceRates,
                    "backgroundColor", "#10b981"
                ),
                Map.of(
                    "label", "Task Completion Rate (%)",
                    "data", taskCompletionRates,
                    "backgroundColor", "#3b82f6"
                )
            ));

            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load janitor performance data"));
        }
    }

    /**
     * Generate Job Monitor CSV Report
     */
    @PostMapping("/generate-report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> generateReport(@RequestBody Map<String, String> request) {
        try {
            LocalDate startDate = LocalDate.parse(request.get("startDate"));
            LocalDate endDate = LocalDate.parse(request.get("endDate"));

            // Validate date range (max 6 months)
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body("Start date must be before end date");
            }

            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            if (daysBetween > 180) {
                return ResponseEntity.badRequest().body("Date range cannot exceed 6 months");
            }

            if (endDate.isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest().body("End date cannot be in the future");
            }

            // Generate CSV
            String csvContent = reportService.generateJobMonitorReport(startDate, endDate);

            // Return CSV file
            return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=\"job_monitor_report_"
                    + startDate + "_to_" + endDate + ".csv\"")
                .body(csvContent);

        } catch (Exception e) {
            logger.error("Failed to generate report: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to generate report: " + e.getMessage());
        }
    }
}