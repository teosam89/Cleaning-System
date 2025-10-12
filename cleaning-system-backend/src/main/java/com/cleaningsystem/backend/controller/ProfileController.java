package com.cleaningsystem.backend.controller;

import com.cleaningsystem.backend.entity.UserProfile;
import com.cleaningsystem.backend.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ProfileController
 * REST API endpoints for user profile management
 * Supports dynamic profile data for janitors with admin/supervisor viewing capabilities
 */
@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {
    
    @Autowired
    private ProfileService profileService;
    
    /**
     * Get complete profile data for current user
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable Long userId) {
        try {
            Map<String, Object> profileData = profileService.getCompleteProfile(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", profileData);
            response.put("message", "Profile data retrieved successfully");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", "USER_NOT_FOUND");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve profile: " + e.getMessage());
            errorResponse.put("errorType", "INTERNAL_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Update basic profile information
     */
    @PutMapping("/{userId}/basic")
    public ResponseEntity<Map<String, Object>> updateBasicProfile(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> profileData) {
        try {
            UserProfile updatedProfile = profileService.updateBasicProfile(userId, profileData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("profileId", updatedProfile.getProfileId());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", "VALIDATION_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update profile: " + e.getMessage());
            errorResponse.put("errorType", "INTERNAL_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Update user preferences
     */
    @PutMapping("/{userId}/preferences")
    public ResponseEntity<Map<String, Object>> updatePreferences(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> preferences) {
        try {
            UserProfile updatedProfile = profileService.updatePreferences(userId, preferences);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Preferences updated successfully");
            response.put("profileId", updatedProfile.getProfileId());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update preferences: " + e.getMessage());
            errorResponse.put("errorType", "INTERNAL_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Update avatar
     */
    @PutMapping("/{userId}/avatar")
    public ResponseEntity<Map<String, Object>> updateAvatar(
            @PathVariable Long userId,
            @RequestBody Map<String, String> avatarData) {
        try {
            String avatarUrl = avatarData.get("avatarUrl");
            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Avatar URL is required");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
            
            UserProfile updatedProfile = profileService.updateAvatar(userId, avatarUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Avatar updated successfully");
            response.put("profileId", updatedProfile.getProfileId());
            response.put("avatarUrl", updatedProfile.getAvatarUrl());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update avatar: " + e.getMessage());
            errorResponse.put("errorType", "INTERNAL_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get profile for admin/supervisor viewing
     * Admin and supervisors can view janitor profiles
     */
    @GetMapping("/view/{targetUserId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getProfileForViewing(
            @PathVariable Long targetUserId,
            @RequestParam Long requestingUserId) {
        try {
            Map<String, Object> profileData = profileService.getProfileForViewing(targetUserId, requestingUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", profileData);
            response.put("message", "Profile data retrieved for viewing");
            response.put("viewedBy", requestingUserId);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (SecurityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", "PERMISSION_DENIED");
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", "USER_NOT_FOUND");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve profile for viewing: " + e.getMessage());
            errorResponse.put("errorType", "INTERNAL_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Change password (placeholder - would integrate with AuthService)
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> passwordData) {
        try {
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (currentPassword == null || newPassword == null || 
                currentPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Current password and new password are required");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
            
            if (newPassword.length() < 6) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "New password must be at least 6 characters long");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
            
            // TODO: Integrate with AuthService for actual password change
            // For now, simulate success
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password changed successfully");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to change password: " + e.getMessage());
            errorResponse.put("errorType", "INTERNAL_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get profile summary for dashboard display
     */
    @GetMapping("/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getProfileSummary(@PathVariable Long userId) {
        try {
            Map<String, Object> profileData = profileService.getCompleteProfile(userId);
            
            // Extract summary information
            Map<String, Object> summary = new HashMap<>();
            summary.put("fullName", profileData.get("fullName"));
            summary.put("role", profileData.get("role"));
            summary.put("employeeNumber", profileData.get("employeeNumber"));
            summary.put("status", profileData.get("status"));
            summary.put("avatarUrl", profileData.get("avatarUrl"));
            summary.put("joinDate", profileData.get("joinDate"));
            
            // Work info summary
            Map<String, Object> workInfo = (Map<String, Object>) profileData.get("workInfo");
            if (workInfo != null) {
                summary.put("position", workInfo.get("position"));
                summary.put("department", workInfo.get("department"));
                summary.put("taskCompletionRate", workInfo.get("taskCompletionRate"));
                summary.put("monthlyAttendance", workInfo.get("monthlyAttendance"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", summary);
            response.put("message", "Profile summary retrieved successfully");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve profile summary: " + e.getMessage());
            errorResponse.put("errorType", "INTERNAL_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}