package com.cleaningsystem.backend.controller;

import com.cleaningsystem.backend.entity.User;
import com.cleaningsystem.backend.entity.Task;
import com.cleaningsystem.backend.entity.Image;
import com.cleaningsystem.backend.entity.UserProfile;
import com.cleaningsystem.backend.service.AuthService;
import com.cleaningsystem.backend.service.TaskService;
import com.cleaningsystem.backend.service.ProfileService;
import com.cleaningsystem.backend.service.ImageService;
import com.cleaningsystem.backend.service.PerformanceService;
import com.cleaningsystem.backend.service.ReportService;
import com.cleaningsystem.backend.repository.UserProfileRepository;
import com.cleaningsystem.backend.repository.UserRepository;
import com.cleaningsystem.backend.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Supervisor Controller - Limited admin functionality for team supervisors
 * Supervisors can view and manage staff but cannot create/delete users
 */
@RestController
@RequestMapping("/api/supervisor")
@CrossOrigin(origins = "*")
public class SupervisorController {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SupervisorController.class);
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

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
    private ReportService reportService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    /**
     * Get supervisor dashboard statistics
     * Similar to admin dashboard but with limited scope
     */
    @GetMapping("/dashboard/{supervisorId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getSupervisorDashboard(@PathVariable Long supervisorId) {
        try {
            System.out.println("Supervisor dashboard endpoint hit for supervisor ID: " + supervisorId);
            
            Map<String, Object> dashboardData = new HashMap<>();
            
            // Add basic database queries for supervisor scope
            Map<String, Object> stats = new HashMap<>();
            
            try {
                // Staff statistics (supervisor can view staff counts)
                long totalStaff = userRepository.countByRoleIn(Arrays.asList("janitor", "cleaner"));
                stats.put("totalStaff", totalStaff);
                System.out.println("Staff count successful: " + totalStaff);
                
                // Task statistics
                long activeTasks = taskService.countByStatus("in_progress");
                stats.put("activeTasks", activeTasks);
                System.out.println("Active tasks count successful: " + activeTasks);
                
                long pendingTasks = taskService.countByStatus("pending");
                stats.put("pendingTasks", pendingTasks);
                System.out.println("Pending tasks count successful: " + pendingTasks);
                
                long completedTasks = taskService.countByStatus("completed");
                stats.put("completedTasks", completedTasks);
                System.out.println("Completed tasks count successful: " + completedTasks);
                
                // Total supervisors (for reference)
                long totalSupervisors = userRepository.countByRole("supervisor");
                stats.put("totalSupervisors", totalSupervisors);
                
            } catch (Exception e) {
                System.err.println("Database query error: " + e.getMessage());
                e.printStackTrace();
                // Fallback to default values if database calls fail
                stats.put("totalStaff", 5L);
                stats.put("totalSupervisors", 1L);
                stats.put("activeTasks", 3L);
                stats.put("pendingTasks", 7L);
                stats.put("completedTasks", 12L);
            }
            
            dashboardData.put("stats", stats);
            
            // Recent tasks with safe serialization
            try {
                System.out.println("Testing getRecentTasks for supervisor dashboard...");
                List<Task> recentTasksRaw = taskService.getRecentTasks(5);
                System.out.println("Raw tasks retrieved: " + recentTasksRaw.size());
                
                // Convert to safe format
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
                            System.err.println("Error loading assigned user in SupervisorController: " + e.getMessage());
                        }
                    }
                    
                    if (task.getAssignedBy() != null) {
                        try {
                            Optional<User> assignedByUser = userRepository.findById(task.getAssignedBy());
                            assignedByName = assignedByUser.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                                .orElse("Unknown Admin");
                        } catch (Exception e) {
                            System.err.println("Error loading assigning user in SupervisorController: " + e.getMessage());
                        }
                    }
                    
                    taskInfo.put("assignedToName", assignedToName);
                    taskInfo.put("assignedByName", assignedByName);
                    
                    recentTasks.add(taskInfo);
                }
                
                dashboardData.put("recentTasks", recentTasks);
                System.out.println("Recent tasks successful: " + recentTasks.size() + " tasks");
                
                // Recent staff query (janitors and cleaners only)
                System.out.println("Testing recent staff query for supervisor...");
                List<User> recentStaffRaw = userRepository.findTop5ByRoleInOrderByCreatedAtDesc(
                    Arrays.asList("janitor", "cleaner")
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

                    // Get avatar URL from user_profiles
                    try {
                        Optional<UserProfile> userProfile = userProfileRepository.findByUserId(user.getUserId());
                        if (userProfile.isPresent() && userProfile.get().getAvatarUrl() != null) {
                            userInfo.put("avatarUrl", userProfile.get().getAvatarUrl());
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching avatar for user " + user.getUserId() + ": " + e.getMessage());
                    }

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
            
            // Generate supervisor-specific recent activities
            dashboardData.put("recentActivities", generateSupervisorActivities());
            
            // Generate supervisor-specific system alerts
            long totalStaff = (Long) stats.get("totalStaff");
            long pendingTasks = (Long) stats.get("pendingTasks");
            dashboardData.put("systemAlerts", generateSupervisorAlerts(totalStaff, pendingTasks));
            
            System.out.println("Supervisor dashboard data prepared successfully");
            
            return ResponseEntity.ok(dashboardData);
            
        } catch (Exception e) {
            System.err.println("Supervisor dashboard endpoint error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load supervisor dashboard data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get staff list (VIEW ONLY - no creation/deletion capabilities)
     * Supervisors can view janitors and cleaners but not admins or other supervisors
     */
    @GetMapping("/staff")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> getStaffList() {
        try {
            List<User> staffUsers = userRepository.findByRoleIn(
                Arrays.asList("janitor", "cleaner")
            );
            
            // Convert to safe format for JSON serialization
            List<Map<String, Object>> userList = new ArrayList<>();
            for (User user : staffUsers) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("username", user.getUsername());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("role", user.getRole());
                userInfo.put("email", user.getEmail());
                userInfo.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
                
                // Add avatar URL
                try {
                    List<Image> avatarImages = imageService.getImagesForEntity(Image.EntityType.PROFILE, user.getUserId());
                    if (!avatarImages.isEmpty()) {
                        Image latestAvatar = avatarImages.get(avatarImages.size() - 1);
                        userInfo.put("avatarUrl", latestAvatar.getPublicUrl());
                        userInfo.put("avatar", latestAvatar.getPublicUrl());
                    } else {
                        userInfo.put("avatarUrl", null);
                        userInfo.put("avatar", null);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading avatar for user " + user.getUserId() + ": " + e.getMessage());
                    userInfo.put("avatarUrl", null);
                    userInfo.put("avatar", null);
                }
                
                userList.add(userInfo);
            }
            
            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            logger.error("Error retrieving staff list: {}", e.getMessage(), e);
            
            // Return structured error response instead of empty list
            List<Map<String, Object>> errorResponse = new ArrayList<>();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to load staff data");
            error.put("code", "STAFF_FETCH_ERROR");
            error.put("timestamp", LocalDateTime.now());
            errorResponse.add(error);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get assignable users for task management
     * Supervisors can assign tasks to janitors and cleaners
     */
    @GetMapping("/assignable-users")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> getAssignableUsers() {
        try {
            List<User> assignableUsers = userRepository.findByRoleIn(
                Arrays.asList("janitor", "cleaner")
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
            logger.error("Error retrieving assignable users: {}", e.getMessage(), e);
            
            // Return structured error response
            List<Map<String, Object>> errorResponse = new ArrayList<>();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to load assignable users");
            error.put("code", "ASSIGNABLE_USERS_ERROR");
            error.put("timestamp", LocalDateTime.now());
            errorResponse.add(error);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get detailed staff profile for supervisor viewing
     * Supervisors can view comprehensive profile information for janitors and cleaners
     */
    @GetMapping("/staff/{userId}/profile")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getStaffProfile(
            @PathVariable Long userId,
            @RequestParam(required = false) Long requestingUserId) {
        try {
            logger.info("Supervisor profile request for user: {} by requesting user: {}", userId, requestingUserId);
            
            // Verify the target user is a janitor or cleaner (supervisors can only view these roles)
            Optional<User> targetUser = userRepository.findById(userId);
            if (targetUser.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                errorResponse.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            String targetRole = targetUser.get().getRole();
            if (!Arrays.asList("janitor", "cleaner").contains(targetRole)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Access denied. Supervisors can only view janitor and cleaner profiles");
                errorResponse.put("code", "ACCESS_DENIED");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            // Use ProfileService to get comprehensive profile data
            // For supervisor access, we don't need to validate requesting user ID as strictly
            // as the endpoint is already protected by @PreAuthorize
            Map<String, Object> profileData;
            
            if (requestingUserId != null) {
                profileData = profileService.getProfileForViewing(userId, requestingUserId);
            } else {
                // Fallback: get profile data directly (supervisor access)
                profileData = profileService.getCompleteProfile(userId);
            }
            
            // Wrap in success response format
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("profile", profileData);
            response.put("accessLevel", "supervisor");
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("Successfully retrieved profile for user: {}", userId);
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            logger.warn("Security exception for profile request: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Insufficient permissions to view this profile");
            errorResponse.put("code", "INSUFFICIENT_PERMISSIONS");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid argument for profile request: {}", e.getMessage());
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
     * Get basic staff profile information (lightweight endpoint)
     * Alternative endpoint for quick profile previews
     */
    @GetMapping("/staff/{userId}/summary")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getStaffSummary(@PathVariable Long userId) {
        try {
            logger.info("Supervisor profile summary request for user: {}", userId);
            
            // Verify the target user exists and is accessible
            Optional<User> targetUser = userRepository.findById(userId);
            if (targetUser.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            User user = targetUser.get();
            String targetRole = user.getRole();
            
            // Verify role access
            if (!Arrays.asList("janitor", "cleaner").contains(targetRole)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Access denied");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            // Create basic profile summary
            Map<String, Object> summary = new HashMap<>();
            summary.put("userId", user.getUserId());
            summary.put("username", user.getUsername());
            summary.put("fullName", user.getFullName());
            summary.put("email", user.getEmail());
            summary.put("role", user.getRole());
            summary.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
            
            // Try to get basic profile info if available
            try {
                Map<String, Object> profileData = profileService.getCompleteProfile(userId);
                if (profileData != null) {
                    summary.put("employeeNumber", profileData.get("employeeNumber"));
                    summary.put("position", profileData.get("position"));
                    summary.put("department", profileData.get("department"));
                    summary.put("avatarUrl", profileData.get("avatarUrl"));
                    summary.put("status", profileData.get("status"));
                }
            } catch (Exception e) {
                logger.warn("Could not retrieve extended profile data for summary: {}", e.getMessage());
                // Continue with basic data only
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("summary", summary);
            response.put("accessLevel", "supervisor");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving staff summary for user {}: {}", userId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve staff summary");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Generate reports (LIMITED - only staff and task reports, no system reports)
     */
    @GetMapping("/reports/{reportType}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> generateReport(@PathVariable String reportType) {
        logger.info("Supervisor report generation request - type: {}", reportType);
        
        // Enhanced input validation
        if (reportType == null || reportType.trim().isEmpty()) {
            logger.warn("Invalid report type provided: {}", reportType);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Report type is required");
            errorResponse.put("code", "INVALID_REPORT_TYPE");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            Map<String, Object> report = new HashMap<>();
            String normalizedReportType = reportType.toLowerCase().trim();
            
            switch (normalizedReportType) {
                case "tasks":
                    report.put("totalTasks", taskService.countAllTasks());
                    report.put("completedTasks", taskService.countByStatus("completed"));
                    report.put("pendingTasks", taskService.countByStatus("pending"));
                    report.put("inProgressTasks", taskService.countByStatus("in_progress"));
                    break;
                    
                case "staff":
                    // Only janitors and cleaners (no admins or supervisors)
                    report.put("totalStaff", userRepository.countByRoleIn(Arrays.asList("janitor", "cleaner")));
                    report.put("janitors", userRepository.countByRole("janitor"));
                    report.put("cleaners", userRepository.countByRole("cleaner"));
                    break;
                    
                default:
                    report.put("error", "Unknown report type or insufficient permissions");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(report);
            }
            
            report.put("generatedAt", LocalDateTime.now());
            report.put("reportType", reportType);
            report.put("generatedBy", "supervisor");
            
            return ResponseEntity.ok(report);
            
        } catch (Exception e) {
            logger.error("Error generating supervisor report '{}': {}", reportType, e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            
            // Enhanced error categorization for reports
            if (e instanceof org.springframework.dao.DataAccessException) {
                errorResponse.put("error", "Database error while generating report");
                errorResponse.put("code", "REPORT_DATABASE_ERROR");
                errorResponse.put("retry", true);
            } else if (e instanceof java.lang.SecurityException) {
                errorResponse.put("error", "Insufficient permissions for report type");
                errorResponse.put("code", "REPORT_PERMISSION_ERROR");
                errorResponse.put("retry", false);
            } else {
                errorResponse.put("error", "Failed to generate report");
                errorResponse.put("code", "REPORT_GENERATION_ERROR");
                errorResponse.put("retry", true);
            }
            
            errorResponse.put("reportType", reportType);
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Generate supervisor-specific recent activities
     */
    private List<Map<String, Object>> generateSupervisorActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        try {
            // Get recent completed tasks
            List<Task> recentCompletedTasks = taskService.getRecentTasksByStatus("completed", 3);
            for (Task task : recentCompletedTasks) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", "task_" + task.getTaskId());
                activity.put("type", "success");
                activity.put("icon", "CircleCheck");
                activity.put("title", "Task Completed");
                activity.put("description", task.getTitle() + " completed by team");
                activity.put("time", getRelativeTime(task.getCompletedAt() != null ? task.getCompletedAt() : task.getCreatedAt()));
                activities.add(activity);
            }
            
            // Add supervisor-specific activity
            Map<String, Object> systemActivity = new HashMap<>();
            systemActivity.put("id", "supervisor_check");
            systemActivity.put("type", "info");
            systemActivity.put("icon", "UserCheck");
            systemActivity.put("title", "Team Status Check");
            systemActivity.put("description", "Supervisor dashboard accessed - monitoring team performance");
            systemActivity.put("time", "Just now");
            activities.add(systemActivity);
            
        } catch (Exception e) {
            System.err.println("Error generating supervisor activities: " + e.getMessage());
            // Fallback activity
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", "fallback");
            activity.put("type", "info");
            activity.put("icon", "Tools");
            activity.put("title", "System Active");
            activity.put("description", "Supervisor monitoring system operational");
            activity.put("time", "Just now");
            activities.add(activity);
        }
        
        return activities;
    }
    
    /**
     * Generate supervisor-specific system alerts
     */
    private List<Map<String, Object>> generateSupervisorAlerts(long totalStaff, long pendingTasks) {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        // Check staff levels (supervisor perspective)
        if (totalStaff < 3) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", "staff_shortage");
            alert.put("type", "warning");
            alert.put("title", "Team Size Alert");
            alert.put("description", "Team has only " + totalStaff + " active members. Consider requesting additional staff.");
            alerts.add(alert);
        }
        
        // Check pending tasks (supervisor scope)
        if (pendingTasks > 8) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", "pending_tasks");
            alert.put("type", "warning");
            alert.put("title", "High Workload Alert");
            alert.put("description", pendingTasks + " tasks pending. Review team assignments and prioritize critical tasks.");
            alerts.add(alert);
        }
        
        // Check for overdue tasks
        try {
            long overdueTasks = taskService.countOverdueTasks();
            if (overdueTasks > 0) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "overdue_tasks");
                alert.put("type", "error");
                alert.put("title", "Overdue Tasks Alert");
                alert.put("description", overdueTasks + " tasks overdue. Team intervention required.");
                alerts.add(alert);
            }
        } catch (Exception e) {
            System.err.println("Error checking overdue tasks: " + e.getMessage());
        }
        
        // If no alerts, add positive message
        if (alerts.isEmpty()) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", "team_good");
            alert.put("type", "success");
            alert.put("title", "Team Status Normal");
            alert.put("description", "Team performance is on track. All systems operating normally.");
            alerts.add(alert);
        }
        
        return alerts;
    }
    
    /**
     * Get staff performance metrics
     * Enhanced endpoint with real performance calculations
     */
    @GetMapping("/staff/{userId}/performance")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getStaffPerformance(
            @PathVariable Long userId,
            @RequestParam(required = false) String month) {
        try {
            logger.info("Supervisor performance request for user: {} for month: {}", userId, month);

            // Verify the target user is a janitor or cleaner
            Optional<User> targetUser = userRepository.findById(userId);
            if (targetUser.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                errorResponse.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            String targetRole = targetUser.get().getRole();
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

            // Calculate performance metrics
            Map<String, Object> performanceData = performanceService.calculatePerformanceMetrics(userId, targetMonth);

            // Wrap in success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("performance", performanceData);
            response.put("accessLevel", "supervisor");
            response.put("timestamp", LocalDateTime.now().toString());

            logger.info("Successfully calculated performance for user: {}", userId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid argument for performance request: {}", e.getMessage());
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
     * Get performance summary for multiple staff members
     * Bulk endpoint for supervisor dashboard
     */
    @GetMapping("/staff/performance/bulk")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getBulkStaffPerformance(
            @RequestParam(required = false) List<Long> userIds,
            @RequestParam(required = false) String month) {
        try {
            logger.info("Bulk performance request for users: {} for month: {}", userIds, month);

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
            response.put("accessLevel", "supervisor");
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
     * Get tasks for a specific staff member
     * Supervisors can view all tasks assigned to janitors and cleaners
     */
    @GetMapping("/staff/{userId}/tasks")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getStaffTasks(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false, defaultValue = "0") int offset) {
        try {
            logger.info("Supervisor task request for user: {} with status: {}, priority: {}", userId, status, priority);

            // Verify the target user is a janitor or cleaner
            Optional<User> targetUser = userRepository.findById(userId);
            if (targetUser.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                errorResponse.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            String targetRole = targetUser.get().getRole();
            if (!Arrays.asList("janitor", "cleaner").contains(targetRole)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Task data only available for janitors and cleaners");
                errorResponse.put("code", "INVALID_ROLE");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            // Get tasks for the user
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
            response.put("accessLevel", "supervisor");
            response.put("timestamp", LocalDateTime.now().toString());

            logger.info("Successfully retrieved {} tasks for user: {}", taskList.size(), userId);
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
     * Get task statistics for a specific staff member
     * Provides summary statistics for supervisor dashboards
     */
    @GetMapping("/staff/{userId}/task-stats")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getStaffTaskStats(@PathVariable Long userId) {
        try {
            logger.info("Supervisor task statistics request for user: {}", userId);

            // Verify the target user is a janitor or cleaner
            Optional<User> targetUser = userRepository.findById(userId);
            if (targetUser.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                errorResponse.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            String targetRole = targetUser.get().getRole();
            if (!Arrays.asList("janitor", "cleaner").contains(targetRole)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Task statistics only available for janitors and cleaners");
                errorResponse.put("code", "INVALID_ROLE");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

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
            response.put("accessLevel", "supervisor");
            response.put("timestamp", LocalDateTime.now().toString());

            logger.info("Successfully calculated task statistics for user: {}", userId);
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
     * Get all tasks created by supervisor (including pending/unassigned tasks)
     * This endpoint allows supervisors to see their own created tasks in task scheduler
     */
    @GetMapping("/my-tasks")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getMyTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false, defaultValue = "100") int limit,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract supervisor ID from JWT token
            Long supervisorId = extractUserIdFromToken(authHeader);
            if (supervisorId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid authorization token");
                errorResponse.put("code", "INVALID_TOKEN");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            logger.info("Supervisor {} requesting their created tasks with status: {}, priority: {}",
                       supervisorId, status, priority);

            // Get tasks created by this supervisor using TaskService
            List<Task> supervisorTasks = taskService.getTasksCreatedBy(supervisorId, status, priority, limit, offset);

            // Convert tasks to response format
            List<Map<String, Object>> taskList = new ArrayList<>();
            for (Task task : supervisorTasks) {
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("taskId", task.getTaskId());
                taskMap.put("title", task.getTitle());
                taskMap.put("description", task.getDescription());
                taskMap.put("status", task.getStatus());
                taskMap.put("priority", task.getPriority());
                taskMap.put("assignedTo", task.getAssignedTo());
                taskMap.put("assignedBy", task.getAssignedBy());
                taskMap.put("scheduledTime", task.getScheduledTime());
                taskMap.put("estimatedDuration", task.getEstimatedDuration());
                taskMap.put("location", task.getLocation());
                taskMap.put("instructions", task.getInstructions());
                taskMap.put("notes", task.getNotes());
                taskMap.put("toolsRequired", task.getToolsRequired());
                taskMap.put("createdAt", task.getCreatedAt());
                taskMap.put("dueDate", task.getDueDate());
                taskMap.put("progressPercentage", task.getProgressPercentage());

                // Add assignedBy user name
                if (task.getAssignedBy() != null) {
                    Optional<User> assignedByUser = userRepository.findById(task.getAssignedBy());
                    if (assignedByUser.isPresent()) {
                        taskMap.put("assignedByName", assignedByUser.get().getFullName());
                        taskMap.put("assignedByUsername", assignedByUser.get().getUsername());
                    } else {
                        taskMap.put("assignedByName", "Unknown User");
                        taskMap.put("assignedByUsername", null);
                    }
                } else {
                    taskMap.put("assignedByName", null);
                    taskMap.put("assignedByUsername", null);
                }

                // Add assigned user name if task is assigned
                if (task.getAssignedTo() != null) {
                    Optional<User> assignedUser = userRepository.findById(task.getAssignedTo());
                    if (assignedUser.isPresent()) {
                        taskMap.put("assignedToName", assignedUser.get().getFullName());
                        taskMap.put("assignedToUsername", assignedUser.get().getUsername());
                    }
                } else {
                    taskMap.put("assignedToName", "Unassigned");
                    taskMap.put("assignedToUsername", null);
                }

                taskList.add(taskMap);
            }

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tasks", taskList);
            response.put("total", taskList.size());
            response.put("hasMore", taskList.size() == limit);
            response.put("filters", Map.of(
                "status", status != null ? status : "all",
                "priority", priority != null ? priority : "all",
                "limit", limit,
                "offset", offset
            ));
            response.put("supervisorId", supervisorId);
            response.put("timestamp", LocalDateTime.now().toString());

            logger.info("Successfully retrieved {} tasks for supervisor {}", taskList.size(), supervisorId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting supervisor tasks: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get supervisor tasks");
            errorResponse.put("code", "SUPERVISOR_TASKS_ERROR");
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Extract user ID from JWT token
     */
    private Long extractUserIdFromToken(String authHeader) {
        try {
            String token = jwtTokenProvider.extractTokenFromHeader(authHeader);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                return null;
            }
            return jwtTokenProvider.getUserIdFromToken(token);
        } catch (Exception e) {
            logger.error("Error extracting user ID from token: {}", e.getMessage());
            return null;
        }
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
     * Generate Supervisor Team Report CSV
     */
    @PostMapping("/generate-report")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<String> generateTeamReport(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            // Extract supervisor ID from JWT token
            String token = authHeader.replace("Bearer ", "");
            String username = jwtTokenProvider.getUsernameFromToken(token);
            User supervisor = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Supervisor not found"));

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
            String csvContent = reportService.generateSupervisorTeamReport(
                    supervisor.getUserId(), startDate, endDate);

            // Return CSV file
            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .header("Content-Disposition", "attachment; filename=\"supervisor_team_report_"
                            + startDate + "_to_" + endDate + ".csv\"")
                    .body(csvContent);

        } catch (Exception e) {
            logger.error("Failed to generate supervisor team report: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate report: " + e.getMessage());
        }
    }
}