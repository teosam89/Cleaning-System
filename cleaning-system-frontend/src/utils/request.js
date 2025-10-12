/**
 * Axios Configuration with JWT Interceptors
 * Automatically adds Authorization headers and handles token expiration
 */

import axios from "axios";
import { AuthUtils } from "./auth";
import { ElMessage } from "element-plus";
import router from "../router";

// Create axios instance
const request = axios.create({
  baseURL: "", // Empty to work with Vue proxy properly
  timeout: 10000, // 10 second timeout
  headers: {
    "Content-Type": "application/json",
  },
});

/**
 * Request Interceptor
 * Adds JWT token to Authorization header for authenticated requests
 */
request.interceptors.request.use(
  (config) => {
    // Get JWT token from storage
    const token = AuthUtils.getToken();

    if (token && AuthUtils.isAuthenticated()) {
      // Add Authorization header
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response Interceptor
 * Handles successful responses and error cases including token expiration
 */
request.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Handle different error scenarios
    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 401:
          // Unauthorized - token invalid or expired

          // Clear authentication data
          AuthUtils.removeToken();

          // Show error message
          ElMessage.error("Session expired, please login again");

          // Redirect to login page (avoid infinite loops)
          if (router.currentRoute.value.path !== "/") {
            router.push("/");
          }
          break;

        case 403:
          ElMessage.error("You do not have permission to perform this action");
          break;

        case 404:
          ElMessage.error("Requested resource not found");
          break;

        case 500: {
          // Let specific API calls handle their own error display logic
          // Don't show errors for task-related endpoints that have fallback handling
          const silentEndpoints = [
            "/tasks",
            "/admin/assignable-users",
            "/admin/dashboard",
            "/supervisor/dashboard",
            "/supervisor/staff",
            "/supervisor/assignable-users",
            "/supervisor/reports",
            "/attendance/status",
            "/attendance/today",
            "/attendance/history",
          ];
          // 排除job-monitor相关的端点，让错误显示出来
          const excludeFromSilent = ["/admin/job-monitor"];
          const requestUrl = error.config?.url || "";
          // 检查是否应该排除在静默处理之外
          const shouldExcludeFromSilent = excludeFromSilent.some((endpoint) =>
            requestUrl.includes(endpoint)
          );
          // 如果在排除列表中，强制显示错误；否则按原逻辑处理
          const shouldShowError =
            shouldExcludeFromSilent ||
            !silentEndpoints.some((endpoint) => requestUrl.includes(endpoint));

          if (shouldShowError) {
            const errorMsg =
              data?.message || "Server error, please try again later";
            ElMessage.error(errorMsg);
          }
          break;
        }

        default: {
          // Other HTTP errors
          const errorMessage =
            data?.message || `Request failed with status ${status}`;
          ElMessage.error(errorMessage);
          break;
        }
      }
    } else if (error.request) {
      ElMessage.error("Network error, please check your connection");
    } else {
      ElMessage.error("Request error: " + error.message);
    }

    return Promise.reject(error);
  }
);

/**
 * API Helper Functions
 */
export const API = {
  /**
   * Login request
   * @param {Object} credentials - { username, password }
   * @returns {Promise} Login response with token
   */
  async login(credentials) {
    const response = await request.post("/api/login", credentials);

    if (response.data.success && response.data.token) {
      // Store JWT token and user info
      AuthUtils.setToken(response.data.token);
      AuthUtils.setUserInfo(response.data.user);

      return response.data;
    } else {
      throw new Error(response.data.message || "Login failed");
    }
  },

  /**
   * Logout - clear local storage and redirect
   */
  logout() {
    AuthUtils.removeToken();
    ElMessage.success("Logged out successfully");
    router.push("/");
  },

  /**
   * Generic GET request
   * @param {string} url - API endpoint
   * @param {Object} config - Additional axios config
   * @returns {Promise} API response
   */
  get(url, config = {}) {
    return request.get(url, config);
  },

  /**
   * Generic POST request
   * @param {string} url - API endpoint
   * @param {Object} data - Request data
   * @param {Object} config - Additional axios config
   * @returns {Promise} API response
   */
  post(url, data, config = {}) {
    return request.post(url, data, config);
  },

  /**
   * Generic PUT request
   * @param {string} url - API endpoint
   * @param {Object} data - Request data
   * @param {Object} config - Additional axios config
   * @returns {Promise} API response
   */
  put(url, data, config = {}) {
    return request.put(url, data, config);
  },

  /**
   * Generic DELETE request
   * @param {string} url - API endpoint
   * @param {Object} config - Additional axios config
   * @returns {Promise} API response
   */
  delete(url, config = {}) {
    return request.delete(url, config);
  },
};

// Export the configured axios instance
export default request;
