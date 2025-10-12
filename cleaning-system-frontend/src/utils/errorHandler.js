import { ElMessage, ElNotification } from "element-plus";

/**
 * Error Handler Utility for the Cleaning Management System Frontend
 * Provides consistent error handling and user feedback
 */
export class ErrorHandler {
  /**
   * Handle API errors with user-friendly messages
   * @param {Error} error - The error object from axios or other sources
   * @param {string} context - Context of where the error occurred
   * @param {Object} options - Additional options for error handling
   */
  static handleError(error, context = "", options = {}) {
    const {
      showNotification = false,
      duration = 4000,
      type = "error",
      fallbackMessage = "An unexpected error occurred",
    } = options;

    let message = fallbackMessage;
    let title = "Error";

    if (error.response) {
      // Server responded with error status
      const { status, data } = error.response;

      switch (status) {
        case 400:
          title = "Invalid Request";
          message = data.message || "Invalid request data";
          if (data.fieldErrors) {
            message +=
              ". Please check: " + Object.values(data.fieldErrors).join(", ");
          }
          break;

        case 401:
          title = "Authentication Required";
          message = "Please log in to continue";
          // Auto redirect to login after showing error
          setTimeout(() => {
            this.redirectToLogin();
          }, 2000);
          break;

        case 403:
          title = "Access Denied";
          message = "You do not have permission to perform this action";
          break;

        case 404:
          title = "Not Found";
          message = data.message || "The requested resource was not found";
          break;

        case 409:
          title = "Conflict";
          message =
            data.message || "A conflict occurred with the current state";
          break;

        case 422:
          title = "Validation Error";
          message = data.message || "Validation failed";
          if (data.fieldErrors) {
            message +=
              ". Errors: " +
              Object.entries(data.fieldErrors)
                .map(([field, error]) => `${field}: ${error}`)
                .join(", ");
          }
          break;

        case 500:
          title = "Server Error";
          message = "Internal server error. Please try again later.";
          break;

        case 502:
        case 503:
        case 504:
          title = "Service Unavailable";
          message =
            "Service is temporarily unavailable. Please try again later.";
          break;

        default:
          title = `Error ${status}`;
          message = data.message || `Server error: ${status}`;
      }
    } else if (error.request) {
      // Network error
      title = "Network Error";
      message =
        "Unable to connect to server. Please check your internet connection.";
    } else {
      // Other error
      title = "Error";
      message = error.message || fallbackMessage;
    }

    // Add context if provided
    if (context) {
      message = `${context}: ${message}`;
    }

    console.error("Error handled by ErrorHandler:", {
      context,
      error,
      message,
      title,
    });

    // Show error to user
    if (showNotification) {
      ElNotification({
        title,
        message,
        type,
        duration,
        position: "top-right",
      });
    } else {
      ElMessage({
        message,
        type,
        duration,
      });
    }

    return { title, message, status: error.response?.status };
  }

  /**
   * Handle validation errors specifically
   * @param {Object} fieldErrors - Field validation errors from server
   */
  static handleValidationErrors(fieldErrors) {
    if (!fieldErrors || typeof fieldErrors !== "object") return;

    const errorMessages = Object.entries(fieldErrors)
      .map(([field, message]) => `${field}: ${message}`)
      .join("\n");

    ElNotification({
      title: "Validation Errors",
      message: errorMessages,
      type: "warning",
      duration: 6000,
      position: "top-right",
    });
  }

  /**
   * Handle success responses
   * @param {string} message - Success message
   * @param {Object} options - Additional options
   */
  static handleSuccess(message, options = {}) {
    const {
      showNotification = false,
      duration = 3000,
      title = "Success",
    } = options;

    if (showNotification) {
      ElNotification({
        title,
        message,
        type: "success",
        duration,
        position: "top-right",
      });
    } else {
      ElMessage({
        message,
        type: "success",
        duration,
      });
    }
  }

  /**
   * Handle warning messages
   * @param {string} message - Warning message
   * @param {Object} options - Additional options
   */
  static handleWarning(message, options = {}) {
    const {
      showNotification = false,
      duration = 4000,
      title = "Warning",
    } = options;

    if (showNotification) {
      ElNotification({
        title,
        message,
        type: "warning",
        duration,
        position: "top-right",
      });
    } else {
      ElMessage({
        message,
        type: "warning",
        duration,
      });
    }
  }

  /**
   * Handle info messages
   * @param {string} message - Info message
   * @param {Object} options - Additional options
   */
  static handleInfo(message, options = {}) {
    const {
      showNotification = false,
      duration = 3000,
      title = "Information",
    } = options;

    if (showNotification) {
      ElNotification({
        title,
        message,
        type: "info",
        duration,
        position: "top-right",
      });
    } else {
      ElMessage({
        message,
        type: "info",
        duration,
      });
    }
  }

  /**
   * Redirect to login page
   */
  static redirectToLogin() {
    // Clear authentication data
    localStorage.removeItem("token");
    localStorage.removeItem("user");

    // Redirect to login
    if (window.location.pathname !== "/") {
      window.location.href = "/";
    }
  }

  /**
   * Show loading message
   * @param {string} message - Loading message
   */
  static showLoading(message = "Loading...") {
    return ElMessage({
      message,
      type: "info",
      duration: 0, // Don't auto close
      showClose: false,
    });
  }

  /**
   * Close loading message
   * @param {Object} loadingInstance - Loading message instance
   */
  static closeLoading(loadingInstance) {
    if (loadingInstance && typeof loadingInstance.close === "function") {
      loadingInstance.close();
    }
  }

  /**
   * Format error for logging
   * @param {Error} error - Error object
   * @param {string} context - Context information
   */
  static formatErrorForLogging(error, context) {
    return {
      timestamp: new Date().toISOString(),
      context,
      message: error.message,
      stack: error.stack,
      response: error.response?.data,
      status: error.response?.status,
      url: error.config?.url,
      method: error.config?.method,
    };
  }

  /**
   * Log error to console with formatting
   * @param {Error} error - Error object
   * @param {string} context - Context information
   */
  static logError(error, context) {
    const formattedError = this.formatErrorForLogging(error, context);
    console.error("Application Error:", formattedError);

    // In production, you might want to send this to a logging service
    // Example: LoggingService.sendError(formattedError);
  }
}
