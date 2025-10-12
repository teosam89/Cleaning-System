/**
 * Authentication Utilities for JWT Token Management
 * Handles JWT token storage, retrieval, and validation
 */

const TOKEN_KEY = "jwt_token";
const USER_KEY = "user_info";

/**
 * Token Management Functions
 */
export const AuthUtils = {
  /**
   * Store JWT token in localStorage
   * @param {string} token - JWT token string
   */
  setToken(token) {
    if (token) {
      localStorage.setItem(TOKEN_KEY, token);
    }
  },

  /**
   * Get JWT token from localStorage
   * @returns {string|null} JWT token or null if not found
   */
  getToken() {
    return localStorage.getItem(TOKEN_KEY);
  },

  /**
   * Remove JWT token from localStorage
   */
  removeToken() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },

  /**
   * Store user information
   * @param {Object} userInfo - User information object
   */
  setUserInfo(userInfo) {
    if (userInfo) {
      localStorage.setItem(USER_KEY, JSON.stringify(userInfo));
    }
  },

  /**
   * Get user information
   * @returns {Object|null} User info object or null
   */
  getUserInfo() {
    const userStr = localStorage.getItem(USER_KEY);
    try {
      return userStr ? JSON.parse(userStr) : null;
    } catch (e) {
      return null;
    }
  },

  /**
   * Check if user is authenticated (has valid token)
   * @returns {boolean} True if authenticated, false otherwise
   */
  isAuthenticated() {
    const token = this.getToken();
    if (!token) {
      console.log("🔍 [AUTH] No token found");
      return false;
    }

    // Check if token is expired
    try {
      const payload = this.decodeToken(token);
      const currentTime = Math.floor(Date.now() / 1000);
      const expirationDate = payload.exp ? new Date(payload.exp * 1000).toLocaleString() : "N/A";
      const currentDate = new Date(currentTime * 1000).toLocaleString();

      console.log(`🔍 [AUTH] Token validation:`, {
        currentTime: currentDate,
        expirationTime: expirationDate,
        exp: payload.exp,
        isExpired: payload.exp && payload.exp < currentTime,
        timeRemaining: payload.exp ? `${Math.floor((payload.exp - currentTime) / 60)} minutes` : "N/A"
      });

      if (payload.exp && payload.exp < currentTime) {
        console.warn("⚠️ [AUTH] Token expired, removing...");
        this.removeToken();
        return false;
      }

      console.log("✅ [AUTH] Token is valid");
      return true;
    } catch (error) {
      console.error("❌ [AUTH] Token decode error:", error);
      this.removeToken();
      return false;
    }
  },

  /**
   * Decode JWT token payload (without verification)
   * @param {string} token - JWT token
   * @returns {Object} Decoded payload
   */
  decodeToken(token) {
    try {
      const base64Url = token.split(".")[1];
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split("")
          .map(function (c) {
            return "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2);
          })
          .join("")
      );

      return JSON.parse(jsonPayload);
    } catch (error) {
      throw new Error("Invalid token format");
    }
  },

  /**
   * Get user role from token
   * @returns {string|null} User role or null
   */
  getUserRole() {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = this.decodeToken(token);
      return payload.role || null;
    } catch (error) {
      return null;
    }
  },

  /**
   * Get user ID from token
   * @returns {number|null} User ID or null
   */
  getUserId() {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = this.decodeToken(token);
      return payload.userId || null;
    } catch (error) {
      return null;
    }
  },

  /**
   * Get username from token
   * @returns {string|null} Username or null
   */
  getUsername() {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = this.decodeToken(token);
      return payload.sub || null;
    } catch (error) {
      return null;
    }
  },

  /**
   * Get token expiration time
   * @returns {Date|null} Expiration date or null
   */
  getTokenExpiration() {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = this.decodeToken(token);
      return payload.exp ? new Date(payload.exp * 1000) : null;
    } catch (error) {
      return null;
    }
  },

  /**
   * Check if user has specific role
   * @param {string} role - Role to check
   * @returns {boolean} True if user has role, false otherwise
   */
  hasRole(role) {
    const userRole = this.getUserRole();
    return userRole === role;
  },

  /**
   * Check if user is admin
   * @returns {boolean} True if user is admin, false otherwise
   */
  isAdmin() {
    return this.hasRole("admin");
  },

  /**
   * Check if user is supervisor
   * @returns {boolean} True if user is supervisor, false otherwise
   */
  isSupervisor() {
    return this.hasRole("supervisor");
  },

  /**
   * Check if user is janitor (excluding supervisors)
   * @returns {boolean} True if user is janitor or cleaner, false otherwise
   */
  isJanitor() {
    return this.hasRole("janitor") || this.hasRole("cleaner");
  },

  /**
   * Check if user is staff (janitor, cleaner, or supervisor)
   * @returns {boolean} True if user is any staff role, false otherwise
   */
  isStaff() {
    return (
      this.hasRole("janitor") ||
      this.hasRole("cleaner") ||
      this.hasRole("supervisor")
    );
  },
};
