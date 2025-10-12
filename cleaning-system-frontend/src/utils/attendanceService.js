/**
 * Enhanced Attendance Service with User-Aware State Management
 * Provides centralized attendance state management and API integration
 * with multi-user data isolation and real-time synchronization
 */

import { reactive } from "vue";
import { API } from "@/utils/request";
import { ElMessage, ElNotification } from "element-plus";
import { AuthUtils } from "@/utils/auth";

// User-specific state management
const userStates = new Map();
const eventBus = new Map();

// Auto-refresh intervals (per user)
const userIntervals = new Map();

/**
 * Create default attendance state structure
 */
const createDefaultState = () =>
  reactive({
    // Current attendance status
    isCheckedIn: false,
    checkInTime: null,
    checkOutTime: null,
    currentWorkHours: 0,
    workProgressPercentage: 0,
    status: null,

    // Today's comprehensive data
    todayRecord: {
      hasRecord: false,
      workHours: 0,
      breakTime: 0,
      overtimeHours: 0,
      projectedOvertime: 0,
      isLate: false,
      isEarlyLeave: false,
      checkInLocation: null,
      checkOutLocation: null,
      notes: null,
    },

    // Monthly summary
    monthlyStats: {
      monthlyHours: 0,
      monthlyOvertime: 0,
      monthlyAttendanceRate: 0,
      daysWorkedThisMonth: 0,
      workingDaysThisMonth: 22,
    },

    // Work targets
    targets: {
      standardWorkHours: 8.0,
      targetCheckInTime: "08:00",
      targetCheckOutTime: "17:00",
    },

    // UI state
    loading: false,
    lastUpdated: null,
    error: null,
  });

/**
 * Get user-specific state with proper isolation
 */
const getUserState = (userId) => {
  if (!userId) {
    console.warn("getUserState called without userId");
    return null;
  }

  if (!userStates.has(userId)) {
    userStates.set(userId, createDefaultState());
    console.log(`Created new attendance state for user ${userId}`);
  }

  return userStates.get(userId);
};

/**
 * Clear user state (called on logout or route change)
 */
const clearUserState = (userId) => {
  if (userId && userStates.has(userId)) {
    userStates.delete(userId);

    // Clear user event listeners
    if (eventBus.has(userId)) {
      eventBus.delete(userId);
    }

    // Clear user intervals
    if (userIntervals.has(userId)) {
      const intervals = userIntervals.get(userId);
      if (intervals.statusRefresh) clearInterval(intervals.statusRefresh);
      if (intervals.quickRefresh) clearInterval(intervals.quickRefresh);
      userIntervals.delete(userId);
    }

    console.log(`Cleared attendance state for user ${userId}`);
  }
};

/**
 * Subscribe to user state changes
 */
const onStateChange = (userId, callback) => {
  if (!userId || typeof callback !== "function") return;

  if (!eventBus.has(userId)) {
    eventBus.set(userId, []);
  }
  eventBus.get(userId).push(callback);
};

/**
 * Emit state changes to all subscribers
 */
const emitStateChange = (userId, newState) => {
  if (!userId) return;

  const callbacks = eventBus.get(userId) || [];
  callbacks.forEach((callback) => {
    try {
      callback(newState);
    } catch (error) {
      console.error("Error in state change callback:", error);
    }
  });
};

/**
 * Secure operation wrapper with user authentication
 */
const performSecureOperation = async (operation) => {
  const userId = AuthUtils.getUserId();

  // Validate user authentication before API calls
  if (!userId || !AuthUtils.isAuthenticated()) {
    ElMessage.error("Please login again");
    // Don't force redirect here, let the component handle it
    return { success: false, error: "Not authenticated" };
  }

  return await operation(userId);
};

/**
 * Load comprehensive attendance status from backend with user isolation
 */
const loadAttendanceStatus = async (userId = null, suppressErrors = false) => {
  return performSecureOperation(async (validatedUserId) => {
    const targetUserId = userId || validatedUserId;
    const userState = getUserState(targetUserId);

    if (!userState) {
      return { success: false, error: "Invalid user state" };
    }

    try {
      userState.loading = true;
      userState.error = null;

      const [statusResponse, todayResponse] = await Promise.all([
        API.get("/api/attendance/status"),
        API.get("/api/attendance/today"),
      ]);

      // Update current status
      if (statusResponse.data) {
        userState.isCheckedIn = statusResponse.data.isCheckedIn || false;
        userState.checkInTime = statusResponse.data.checkInTime;
        userState.checkOutTime = statusResponse.data.checkOutTime;
        userState.status = statusResponse.data.status;
      }

      // Update comprehensive today's data
      if (todayResponse.data) {
        Object.assign(userState.todayRecord, todayResponse.data);
        userState.currentWorkHours = todayResponse.data.currentWorkHours || 0;
        userState.workProgressPercentage =
          todayResponse.data.workProgressPercentage || 0;

        // Update monthly stats
        userState.monthlyStats = {
          monthlyHours: todayResponse.data.monthlyHours || 0,
          monthlyOvertime: todayResponse.data.monthlyOvertime || 0,
          monthlyAttendanceRate: todayResponse.data.monthlyAttendanceRate || 0,
          daysWorkedThisMonth: todayResponse.data.daysWorkedThisMonth || 0,
          workingDaysThisMonth: todayResponse.data.workingDaysThisMonth || 22,
        };

        // Update work targets
        userState.targets = {
          standardWorkHours: todayResponse.data.standardWorkHours || 8.0,
          targetCheckInTime: todayResponse.data.targetCheckInTime || "08:00",
          targetCheckOutTime: todayResponse.data.targetCheckOutTime || "17:00",
        };
      }

      userState.lastUpdated = new Date().toLocaleTimeString();

      // Emit state change to subscribers
      emitStateChange(targetUserId, userState);

      return { success: true, data: userState };
    } catch (error) {
      console.error("Failed to load attendance status:", error);
      userState.error = "Failed to load attendance data";

      // Don't show error messages if this is a background refresh or initialization
      const intervals = userIntervals.get(targetUserId);
      if (!intervals?.statusRefresh && !suppressErrors) {
        ElMessage.warning(
          "Unable to load attendance data. Please check your connection."
        );
      }

      return { success: false, error: error.message };
    } finally {
      userState.loading = false;
    }
  });
};

/**
 * Perform check-in operation with user isolation
 */
const performCheckIn = async (
  location = "Office Location",
  notes = "",
  userId = null
) => {
  return performSecureOperation(async (validatedUserId) => {
    const targetUserId = userId || validatedUserId;
    const userState = getUserState(targetUserId);

    if (!userState) {
      return { success: false, error: "Invalid user state" };
    }

    try {
      const checkInRequest = {
        location: location,
        notes: notes,
      };

      const response = await API.post(
        "/api/attendance/check-in",
        checkInRequest
      );

      if (response.data && response.data.success) {
        // Refresh attendance data for this specific user
        const loadResult = await loadAttendanceStatus(targetUserId);

        if (loadResult.success) {
          ElNotification({
            title: "Check-in Successful",
            message: `Welcome! You've checked in at ${new Date().toLocaleTimeString(
              "en-GB",
              {
                hour: "2-digit",
                minute: "2-digit",
                hour12: false,
              }
            )}`,
            type: "success",
            position: "top-right",
            duration: 4000,
          });

          // Force additional state change notification for immediate UI updates
          setTimeout(() => {
            emitStateChange(targetUserId, userState);
            console.log("Check-in: Additional state change notification sent");
          }, 100);

          return { success: true, data: response.data };
        } else {
          return loadResult;
        }
      } else {
        throw new Error(response.data?.message || "Check-in failed");
      }
    } catch (error) {
      const errorMessage =
        error.response?.data?.message || error.message || "Check-in failed";
      ElMessage.error(errorMessage);
      return { success: false, error: errorMessage };
    }
  });
};

/**
 * Perform check-out operation with user isolation
 */
const performCheckOut = async (
  location = "Office Location",
  breakTime = 1.0,
  notes = "",
  userId = null
) => {
  return performSecureOperation(async (validatedUserId) => {
    const targetUserId = userId || validatedUserId;
    const userState = getUserState(targetUserId);

    if (!userState) {
      return { success: false, error: "Invalid user state" };
    }

    try {
      const checkOutRequest = {
        location: location,
        breakTime: breakTime,
        notes: notes,
      };

      const response = await API.post(
        "/api/attendance/check-out",
        checkOutRequest
      );

      if (response.data && response.data.success) {
        const workHours = response.data.workHours || 0;
        const overtimeHours = response.data.overtimeHours || 0;

        // Refresh attendance data for this specific user
        const loadResult = await loadAttendanceStatus(targetUserId);

        if (loadResult.success) {
          ElNotification({
            title: "Check-out Successful",
            message: `Great job! Work hours: ${workHours.toFixed(2)}h${
              overtimeHours > 0
                ? ` (Overtime: ${overtimeHours.toFixed(2)}h)`
                : ""
            }`,
            type: "success",
            position: "top-right",
            duration: 5000,
          });

          // Force additional state change notification for immediate UI updates
          setTimeout(() => {
            emitStateChange(targetUserId, userState);
            console.log("Check-out: Additional state change notification sent");
          }, 100);

          return { success: true, data: response.data };
        } else {
          return loadResult;
        }
      } else {
        throw new Error(response.data?.message || "Check-out failed");
      }
    } catch (error) {
      const errorMessage =
        error.response?.data?.message || error.message || "Check-out failed";
      ElMessage.error(errorMessage);
      return { success: false, error: errorMessage };
    }
  });
};

/**
 * Start auto-refresh for attendance status with user isolation
 * @param {string} userId - User ID for state isolation
 * @param {number} intervalMs - Refresh interval in milliseconds (default: 30 seconds)
 */
const startAutoRefresh = (userId = null, intervalMs = 30000) => {
  return performSecureOperation(async (validatedUserId) => {
    const targetUserId = userId || validatedUserId;
    const userState = getUserState(targetUserId);

    if (!userState) {
      return { success: false, error: "Invalid user state" };
    }

    // Clear existing intervals for this user
    stopAutoRefresh(targetUserId);

    // Initialize user intervals object
    if (!userIntervals.has(targetUserId)) {
      userIntervals.set(targetUserId, {});
    }

    const intervals = userIntervals.get(targetUserId);

    // Start main refresh interval
    intervals.statusRefresh = setInterval(async () => {
      if (userState.isCheckedIn) {
        await loadAttendanceStatus(targetUserId);
      }
    }, intervalMs);

    // Start quick refresh for work hours when checked in (every 10 seconds)
    intervals.quickRefresh = setInterval(async () => {
      if (userState.isCheckedIn && userState.checkInTime) {
        try {
          const statusResponse = await API.get("/api/attendance/status");
          if (statusResponse.data) {
            userState.currentWorkHours =
              statusResponse.data.currentWorkHours || 0;

            // Update work progress
            const progressPercentage = Math.min(
              100.0,
              (userState.currentWorkHours /
                userState.targets.standardWorkHours) *
                100
            );
            userState.workProgressPercentage =
              Math.round(progressPercentage * 100.0) / 100.0;

            // Update projected overtime
            const projectedOvertime = Math.max(
              0,
              userState.currentWorkHours - userState.targets.standardWorkHours
            );
            userState.todayRecord.projectedOvertime =
              Math.round(projectedOvertime * 100.0) / 100.0;

            // Emit state change
            emitStateChange(targetUserId, userState);
          }
        } catch (error) {
          console.warn("Quick refresh failed:", error);
        }
      }
    }, 10000);

    console.log(`Attendance auto-refresh started for user ${targetUserId}`);
    return { success: true };
  });
};

/**
 * Stop auto-refresh for specific user
 */
const stopAutoRefresh = (userId = null) => {
  const targetUserId = userId || AuthUtils.getUserId();

  if (targetUserId && userIntervals.has(targetUserId)) {
    const intervals = userIntervals.get(targetUserId);

    if (intervals.statusRefresh) {
      clearInterval(intervals.statusRefresh);
      intervals.statusRefresh = null;
    }

    if (intervals.quickRefresh) {
      clearInterval(intervals.quickRefresh);
      intervals.quickRefresh = null;
    }

    userIntervals.delete(targetUserId);
    console.log(`Attendance auto-refresh stopped for user ${targetUserId}`);
  }
};

/**
 * Format time for display
 */
const formatTime = (time, includeSeconds = false) => {
  if (!time) return "--";

  const date = typeof time === "string" ? new Date(time) : time;
  const options = {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  };

  if (includeSeconds) {
    options.second = "2-digit";
  }

  return date.toLocaleTimeString("en-GB", options);
};

/**
 * Format work hours for display
 */
const formatWorkHours = (hours) => {
  if (typeof hours !== "number") return "0.0h";
  return `${hours.toFixed(1)}h`;
};

/**
 * Get attendance status badge type
 */
const getStatusBadgeType = (status) => {
  const statusTypes = {
    normal: "success",
    late: "warning",
    early_leave: "warning",
    absent: "danger",
    leave: "info",
  };
  return statusTypes[status] || "info";
};

/**
 * Calculate time since check-in with user isolation
 */
const getTimeSinceCheckIn = (userId = null) => {
  const targetUserId = userId || AuthUtils.getUserId();
  const userState = getUserState(targetUserId);

  if (!userState || !userState.checkInTime) return "0h 0m";

  const checkInTime = new Date(userState.checkInTime);
  const now = new Date();
  const diff = now - checkInTime;

  const hours = Math.floor(diff / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

  return `${hours}h ${minutes}m`;
};

// Export enhanced attendance service with user-aware state management
export const AttendanceService = {
  // User state management
  getUserState,
  clearUserState,
  onStateChange,

  // Core operations with user isolation
  loadStatus: loadAttendanceStatus,
  checkIn: performCheckIn,
  checkOut: performCheckOut,

  // Auto-refresh with user isolation
  startAutoRefresh,
  stopAutoRefresh,

  // Utilities
  formatTime,
  formatWorkHours,
  getStatusBadgeType,
  getTimeSinceCheckIn,

  // Legacy compatibility (for gradual migration)
  state: null, // Will be deprecated - use getUserState instead
};

export default AttendanceService;
