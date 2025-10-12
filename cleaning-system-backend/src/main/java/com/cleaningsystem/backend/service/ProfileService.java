package com.cleaningsystem.backend.service;

import com.cleaningsystem.backend.entity.User;
import com.cleaningsystem.backend.entity.UserProfile;
import com.cleaningsystem.backend.entity.Task;
import com.cleaningsystem.backend.entity.Attendance;
import com.cleaningsystem.backend.entity.Image;
import com.cleaningsystem.backend.repository.UserRepository;
import com.cleaningsystem.backend.repository.UserProfileRepository;
import com.cleaningsystem.backend.repository.TaskRepository;
import com.cleaningsystem.backend.repository.AttendanceRepository;
import com.cleaningsystem.backend.repository.ImageRepository;
import com.cleaningsystem.backend.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.Arrays;

/**
 * ProfileService
 * Business logic for user profile management
 * Handles dynamic profile data and performance metrics calculation
 */
@Service
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private PerformanceService performanceService;
    
    /**
     * Get complete profile data for a user
     * @param userId User ID
     * @return Complete profile data with performance metrics
     */
    public Map<String, Object> getCompleteProfile(Long userId) {
        Map<String, Object> profileData = new HashMap<>();
        
        try {
            // Get basic user information
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }
            
            User user = userOpt.get();
            
            // Get or create user profile
            UserProfile profile = getOrCreateProfile(userId);
            
            // Calculate real-time performance metrics
            updatePerformanceMetrics(profile);
            
            // Build complete profile data
            profileData.put("userId", user.getUserId());
            profileData.put("username", user.getUsername());
            profileData.put("email", user.getEmail());
            profileData.put("fullName", user.getFullName());
            profileData.put("role", user.getRole());
            profileData.put("createdAt", user.getCreatedAt());
            
            // Profile-specific data
            profileData.put("employeeNumber", profile.getEmployeeNumber());
            profileData.put("gender", profile.getGender());
            profileData.put("phone", profile.getPhone());
            profileData.put("birthDate", profile.getBirthDate());
            profileData.put("emergencyContact", profile.getEmergencyContact());
            profileData.put("address", profile.getAddress());
            profileData.put("avatarUrl", validateAndCleanAvatarUrl(profile));
            profileData.put("status", profile.getStatus());
            profileData.put("joinDate", profile.getJoinDate());
            
            // Work information
            Map<String, Object> workInfo = new HashMap<>();
            workInfo.put("position", profile.getPosition());
            workInfo.put("workArea", profile.getWorkArea());
            workInfo.put("workHours", profile.getWorkHours());
            workInfo.put("restDays", profile.getRestDays());
            workInfo.put("monthlyAttendance", profile.getMonthlyAttendance());
            workInfo.put("taskCompletionRate", profile.getTaskCompletionRate());
            workInfo.put("qualityScore", profile.getQualityScore());
            workInfo.put("customerSatisfaction", profile.getCustomerSatisfaction());
            
            profileData.put("workInfo", workInfo);
            
            // Preferences
            Map<String, Object> preferences = new HashMap<>();
            preferences.put("taskNotification", profile.getTaskNotification());
            preferences.put("language", profile.getLanguage());

            profileData.put("preferences", preferences);
            
            // Login history (simplified - could be enhanced with actual tracking)
            profileData.put("loginHistory", generateLoginHistory());
            
            return profileData;
            
        } catch (Exception e) {
            System.err.println("Error getting complete profile for user " + userId + ": " + e.getMessage());
            throw new RuntimeException("Failed to retrieve profile data: " + e.getMessage());
        }
    }
    
    /**
     * Update basic profile information
     */
    @Transactional
    public UserProfile updateBasicProfile(Long userId, Map<String, Object> profileData) {
        UserProfile profile = getOrCreateProfile(userId);
        
        try {
            // Update basic fields
            if (profileData.containsKey("phone")) {
                profile.setPhone((String) profileData.get("phone"));
            }
            if (profileData.containsKey("gender")) {
                profile.setGender((String) profileData.get("gender"));
            }
            if (profileData.containsKey("birthDate")) {
                String birthDateStr = (String) profileData.get("birthDate");
                if (birthDateStr != null && !birthDateStr.isEmpty()) {
                    try {
                        // Handle ISO format dates (e.g., "2025-09-24T16:00:00.000Z")
                        if (birthDateStr.contains("T")) {
                            // Extract date part from ISO format
                            String datePart = birthDateStr.substring(0, birthDateStr.indexOf("T"));
                            profile.setBirthDate(LocalDate.parse(datePart));
                        } else {
                            // Handle simple date format (e.g., "2025-09-24")
                            profile.setBirthDate(LocalDate.parse(birthDateStr));
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing birthDate '" + birthDateStr + "': " + e.getMessage());
                        // Skip setting birthDate if parsing fails
                    }
                }
            }
            if (profileData.containsKey("emergencyContact")) {
                profile.setEmergencyContact((String) profileData.get("emergencyContact"));
            }
            if (profileData.containsKey("address")) {
                profile.setAddress((String) profileData.get("address"));
            }
            
            // Update corresponding User entity for email and fullName
            if (profileData.containsKey("email") || profileData.containsKey("fullName")) {
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (profileData.containsKey("email")) {
                        user.setEmail((String) profileData.get("email"));
                    }
                    if (profileData.containsKey("fullName")) {
                        user.setFullName((String) profileData.get("fullName"));
                    }
                    userRepository.save(user);
                }
            }
            
            return userProfileRepository.save(profile);
            
        } catch (Exception e) {
            System.err.println("Error updating basic profile: " + e.getMessage());
            throw new RuntimeException("Failed to update profile: " + e.getMessage());
        }
    }
    
    /**
     * Update user preferences
     */
    @Transactional
    public UserProfile updatePreferences(Long userId, Map<String, Object> preferences) {
        UserProfile profile = getOrCreateProfile(userId);

        try {
            if (preferences.containsKey("taskNotification")) {
                profile.setTaskNotification((Boolean) preferences.get("taskNotification"));
            }
            if (preferences.containsKey("language")) {
                profile.setLanguage((String) preferences.get("language"));
            }

            return userProfileRepository.save(profile);

        } catch (Exception e) {
            System.err.println("Error updating preferences: " + e.getMessage());
            throw new RuntimeException("Failed to update preferences: " + e.getMessage());
        }
    }
    
    /**
     * Update avatar URL
     */
    @Transactional
    public UserProfile updateAvatar(Long userId, String avatarUrl) {
        UserProfile profile = getOrCreateProfile(userId);
        profile.setAvatarUrl(avatarUrl);
        return userProfileRepository.save(profile);
    }
    
    /**
     * Get profile for admin/supervisor viewing
     */
    public Map<String, Object> getProfileForViewing(Long targetUserId, Long requestingUserId) {
        try {
            // Verify requesting user has permission (admin or supervisor)
            Optional<User> requestingUserOpt = userRepository.findById(requestingUserId);
            if (requestingUserOpt.isEmpty()) {
                throw new IllegalArgumentException("Requesting user not found");
            }
            
            String requestingRole = requestingUserOpt.get().getRole();
            if (!"admin".equals(requestingRole) && !"supervisor".equals(requestingRole)) {
                throw new SecurityException("Insufficient permissions to view profile");
            }
            
            // Get complete profile data
            return getCompleteProfile(targetUserId);
            
        } catch (Exception e) {
            System.err.println("Error getting profile for viewing: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve profile for viewing: " + e.getMessage());
        }
    }
    
    /**
     * Get or create user profile
     */
    private UserProfile getOrCreateProfile(Long userId) {
        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userId);
        
        if (profileOpt.isPresent()) {
            return profileOpt.get();
        } else {
            // Create default profile for new user
            UserProfile newProfile = createDefaultProfile(userId);
            return userProfileRepository.save(newProfile);
        }
    }
    
    /**
     * Create default profile for new user
     */
    private UserProfile createDefaultProfile(Long userId) {
        UserProfile profile = new UserProfile(userId);
        
        // Get user info to set defaults
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Generate employee number
            profile.setEmployeeNumber(generateEmployeeNumber(user.getRole()));
            
            // Set default work info based on role
            if ("janitor".equals(user.getRole()) || "cleaner".equals(user.getRole())) {
                profile.setPosition("Janitor");
                profile.setWorkArea("Building A");
                profile.setWorkHours(8);
                profile.setRestDays("Saturday, Sunday");
            }
        }
        
        return profile;
    }
    
    /**
     * Generate unique employee number
     */
    private String generateEmployeeNumber(String role) {
        String prefix = "EMP";
        if ("janitor".equals(role) || "cleaner".equals(role)) {
            prefix = "JAN";
        } else if ("supervisor".equals(role)) {
            prefix = "SUP";
        } else if ("admin".equals(role)) {
            prefix = "ADM";
        }
        
        // Generate sequential number
        long count = userProfileRepository.count() + 1;
        return prefix + String.format("%03d", count);
    }
    
    /**
     * Update performance metrics from real data using PerformanceService
     */
    private void updatePerformanceMetrics(UserProfile profile) {
        try {
            Long userId = profile.getUserId();

            // Check if user is janitor or cleaner (performance calculation is only for these roles)
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return;
            }

            User user = userOpt.get();
            if (!Arrays.asList("janitor", "cleaner").contains(user.getRole())) {
                return; // Skip performance calculation for non-janitor/cleaner roles
            }

            // Use PerformanceService for real calculations
            Map<String, Object> performanceMetrics = performanceService.calculatePerformanceMetrics(userId, null);

            // Update profile with real calculated values
            if (performanceMetrics.containsKey("monthlyAttendance")) {
                Double monthlyAttendance = (Double) performanceMetrics.get("monthlyAttendance");
                profile.setMonthlyAttendance(monthlyAttendance.intValue());
            }

            if (performanceMetrics.containsKey("taskCompletionRate")) {
                Double taskCompletionRate = (Double) performanceMetrics.get("taskCompletionRate");
                profile.setTaskCompletionRate(taskCompletionRate.intValue());
            }

            if (performanceMetrics.containsKey("customerSatisfaction")) {
                Double customerSatisfaction = (Double) performanceMetrics.get("customerSatisfaction");
                profile.setCustomerSatisfaction(customerSatisfaction.intValue());
            }

            // Remove qualityScore from automatic calculation (as requested)
            // Keep existing value if set, or set to null
            if (profile.getQualityScore() == null) {
                profile.setQualityScore(null); // Remove quality score
            }

            userProfileRepository.save(profile);

        } catch (Exception e) {
            System.err.println("Error updating performance metrics: " + e.getMessage());
            // Fallback to basic calculations if PerformanceService fails
            try {
                Long userId = profile.getUserId();

                // Basic monthly attendance fallback
                List<Attendance> monthlyAttendances = attendanceRepository.findByJanitorIdAndWorkDateBetween(
                    userId, LocalDate.now().withDayOfMonth(1), LocalDate.now()
                );
                profile.setMonthlyAttendance(monthlyAttendances.size());

                // Basic task completion fallback
                List<Task> userTasks = taskRepository.findByAssignedToOrderByScheduledTimeAsc(userId);
                if (!userTasks.isEmpty()) {
                    long completedTasks = userTasks.stream()
                        .filter(task -> "completed".equals(task.getStatus()))
                        .count();
                    int completionRate = (int) ((completedTasks * 100) / userTasks.size());
                    profile.setTaskCompletionRate(completionRate);
                } else {
                    profile.setTaskCompletionRate(0);
                }

                userProfileRepository.save(profile);
            } catch (Exception fallbackError) {
                System.err.println("Fallback performance calculation also failed: " + fallbackError.getMessage());
            }
        }
    }
    
    /**
     * Parse skills JSON string
     */
    private List<Map<String, Object>> parseSkills(String skillsJson) {
        List<Map<String, Object>> skills = new ArrayList<>();
        
        if (skillsJson == null || skillsJson.isEmpty()) {
            // Return default skills
            Map<String, Object> skill1 = new HashMap<>();
            skill1.put("name", "Basic Cleaning");
            skill1.put("certified", true);
            skills.add(skill1);
            
            return skills;
        }
        
        try {
            // Simple JSON parsing (could use Jackson for more complex scenarios)
            if (skillsJson.startsWith("[") && skillsJson.endsWith("]")) {
                String content = skillsJson.substring(1, skillsJson.length() - 1);
                String[] skillItems = content.split("\\},\\{");
                
                for (String item : skillItems) {
                    item = item.replace("{", "").replace("}", "");
                    String[] parts = item.split(",");
                    
                    Map<String, Object> skill = new HashMap<>();
                    for (String part : parts) {
                        String[] keyValue = part.split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].replace("\"", "").trim();
                            String value = keyValue[1].replace("\"", "").trim();
                            
                            if ("certified".equals(key)) {
                                skill.put(key, Boolean.parseBoolean(value));
                            } else {
                                skill.put(key, value);
                            }
                        }
                    }
                    
                    if (!skill.isEmpty()) {
                        skills.add(skill);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing skills JSON: " + e.getMessage());
            // Return default skills on error
            Map<String, Object> skill = new HashMap<>();
            skill.put("name", "Basic Cleaning");
            skill.put("certified", true);
            skills.add(skill);
        }
        
        return skills;
    }
    
    /**
     * Generate login history (placeholder - could be enhanced with actual tracking)
     */
    private List<Map<String, Object>> generateLoginHistory() {
        List<Map<String, Object>> history = new ArrayList<>();
        
        Map<String, Object> login1 = new HashMap<>();
        login1.put("device", "Chrome Browser");
        login1.put("location", "Office");
        login1.put("loginTime", DateTimeUtils.nowUtc().minusHours(2).toString());
        login1.put("status", "success");
        history.add(login1);
        
        Map<String, Object> login2 = new HashMap<>();
        login2.put("device", "Mobile App");
        login2.put("location", "Office");
        login2.put("loginTime", DateTimeUtils.nowUtc().minusDays(1).toString());
        login2.put("status", "success");
        history.add(login2);
        
        return history;
    }

    /**
     * Validate and clean avatar URL
     * Checks if the avatar URL corresponds to an existing image record
     * If not, clears the avatar URL from the profile
     */
    private String validateAndCleanAvatarUrl(UserProfile profile) {
        String avatarUrl = profile.getAvatarUrl();

        // If no avatar URL, return null
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return null;
        }

        try {
            // Extract filename from URL (e.g., /api/files/profile/profile_194_xxx.png -> profile_194_xxx.png)
            String fileName = extractFileNameFromUrl(avatarUrl);
            if (fileName == null) {
                logger.warn("Invalid avatar URL format for user {}: {}", profile.getUserId(), avatarUrl);
                clearUserAvatarUrl(profile);
                return null;
            }

            // Check if image record exists and is not deleted
            Optional<Image> imageOpt = imageRepository.findByStoredNameAndNotDeleted(fileName);

            if (imageOpt.isEmpty()) {
                logger.warn("Avatar image record not found in database for user {} - clearing URL: {}", profile.getUserId(), avatarUrl);
                clearUserAvatarUrl(profile);
                return null;
            }

            // Check if physical file actually exists on filesystem
            Image image = imageOpt.get();
            try {
                Path filePath = Paths.get(image.getFilePath());
                if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                    logger.warn("Avatar physical file missing for user {} - file: {} - cleaning up orphaned record",
                                profile.getUserId(), image.getFilePath());

                    // Mark database record as deleted (cleanup orphaned record)
                    image.setIsDeleted(true);
                    imageRepository.save(image);

                    // Clear user's avatar URL
                    clearUserAvatarUrl(profile);
                    return null;
                }
            } catch (Exception e) {
                logger.error("Error checking avatar file existence for user {}: {}", profile.getUserId(), e.getMessage());
                clearUserAvatarUrl(profile);
                return null;
            }

            // Avatar URL is valid
            return avatarUrl;

        } catch (Exception e) {
            logger.error("Error validating avatar URL for user {}: {}", profile.getUserId(), e.getMessage());
            clearUserAvatarUrl(profile);
            return null;
        }
    }

    /**
     * Extract filename from avatar URL
     */
    private String extractFileNameFromUrl(String url) {
        if (url == null || !url.contains("/")) {
            return null;
        }

        // Extract filename from URL like "/api/files/profile/profile_194_xxx.png"
        String[] parts = url.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }

        return null;
    }

    /**
     * Clear avatar URL from user profile
     */
    @Transactional
    private void clearUserAvatarUrl(UserProfile profile) {
        try {
            profile.setAvatarUrl(null);
            userProfileRepository.save(profile);
            logger.info("Cleared invalid avatar URL for user: {}", profile.getUserId());
        } catch (Exception e) {
            logger.error("Error clearing avatar URL for user {}: {}", profile.getUserId(), e.getMessage());
        }
    }
}