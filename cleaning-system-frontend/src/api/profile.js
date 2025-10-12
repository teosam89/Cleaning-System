import { API } from "@/utils/request";

/**
 * Profile API Service
 * Handles all profile-related API calls for dynamic profile management
 */
export const ProfileAPI = {
  /**
   * Get complete profile data for a user
   * @param {number} userId - User ID
   * @returns {Promise} Profile data
   */
  async getProfile(userId) {
    try {
      const response = await API.get(`/api/profile/${userId}`);
      return response.data;
    } catch (error) {
      console.error("Error getting profile:", error);
      throw error;
    }
  },

  /**
   * Get profile summary for dashboard display
   * @param {number} userId - User ID
   * @returns {Promise} Profile summary data
   */
  async getProfileSummary(userId) {
    try {
      const response = await API.get(`/api/profile/${userId}/summary`);
      return response.data;
    } catch (error) {
      console.error("Error getting profile summary:", error);
      throw error;
    }
  },

  /**
   * Update basic profile information
   * @param {number} userId - User ID
   * @param {Object} profileData - Profile data to update
   * @returns {Promise} Update response
   */
  async updateBasicProfile(userId, profileData) {
    try {
      const response = await API.put(
        `/api/profile/${userId}/basic`,
        profileData
      );
      return response.data;
    } catch (error) {
      console.error("Error updating basic profile:", error);
      throw error;
    }
  },

  /**
   * Update user preferences
   * @param {number} userId - User ID
   * @param {Object} preferences - Preferences to update
   * @returns {Promise} Update response
   */
  async updatePreferences(userId, preferences) {
    try {
      const response = await API.put(
        `/api/profile/${userId}/preferences`,
        preferences
      );
      return response.data;
    } catch (error) {
      console.error("Error updating preferences:", error);
      throw error;
    }
  },

  /**
   * Update security settings
   * @param {number} userId - User ID
   * @param {Object} securitySettings - Security settings to update
   * @returns {Promise} Update response
   */
  async updateSecuritySettings(userId, securitySettings) {
    try {
      const response = await API.put(
        `/api/profile/${userId}/security`,
        securitySettings
      );
      return response.data;
    } catch (error) {
      console.error("Error updating security settings:", error);
      throw error;
    }
  },

  /**
   * Update avatar
   * @param {number} userId - User ID
   * @param {string} avatarUrl - New avatar URL
   * @returns {Promise} Update response
   */
  async updateAvatar(userId, avatarUrl) {
    try {
      const response = await API.put(`/api/profile/${userId}/avatar`, {
        avatarUrl,
      });
      return response.data;
    } catch (error) {
      console.error("Error updating avatar:", error);
      throw error;
    }
  },

  /**
   * Change password
   * @param {number} userId - User ID
   * @param {string} currentPassword - Current password
   * @param {string} newPassword - New password
   * @returns {Promise} Change password response
   */
  async changePassword(userId, currentPassword, newPassword) {
    try {
      const response = await API.put(`/api/profile/${userId}/password`, {
        currentPassword,
        newPassword,
      });
      return response.data;
    } catch (error) {
      console.error("Error changing password:", error);
      throw error;
    }
  },

  /**
   * Get profile for admin/supervisor viewing
   * @param {number} targetUserId - Target user ID to view
   * @param {number} requestingUserId - Requesting user ID (admin/supervisor)
   * @returns {Promise} Profile data for viewing
   */
  async getProfileForViewing(targetUserId, requestingUserId) {
    try {
      const response = await API.get(
        `/api/profile/view/${targetUserId}?requestingUserId=${requestingUserId}`
      );
      return response.data;
    } catch (error) {
      console.error("Error getting profile for viewing:", error);
      throw error;
    }
  },

  /**
   * Upload avatar file (placeholder - would integrate with file upload service)
   * @param {number} userId - User ID
   * @param {File} file - Avatar file
   * @returns {Promise} Upload response with avatar URL
   */
  async uploadAvatar(userId, file) {
    try {
      // Placeholder for actual file upload implementation
      // In real implementation, this would upload to a file service
      // and return the URL

      // For now, create a data URL for preview
      return new Promise((resolve) => {
        const reader = new FileReader();
        reader.onload = (e) => {
          resolve({
            success: true,
            data: {
              avatarUrl: e.target.result,
            },
            message: "Avatar uploaded successfully",
          });
        };
        reader.readAsDataURL(file);
      });
    } catch (error) {
      console.error("Error uploading avatar:", error);
      throw error;
    }
  },

  /**
   * Get login history (placeholder)
   * @returns {Promise} Login history data
   */
  async getLoginHistory() {
    try {
      // Placeholder - would be implemented with actual login tracking
      return {
        success: true,
        data: [
          {
            device: "Chrome Browser",
            location: "Office",
            loginTime: new Date().toISOString(),
            status: "success",
          },
          {
            device: "Mobile App",
            location: "Office",
            loginTime: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
            status: "success",
          },
        ],
        message: "Login history retrieved successfully",
      };
    } catch (error) {
      console.error("Error getting login history:", error);
      throw error;
    }
  },
};

export default ProfileAPI;
