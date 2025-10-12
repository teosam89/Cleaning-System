<!-- eslint-disable -->
<template>
  <JanitorLayout>
    <div class="dashboard-content">
      <!-- Loading State -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
        <el-skeleton :rows="5" animated style="margin-top: 1rem" />
      </div>

      <!-- Dashboard Content -->
      <div v-else>
        <!-- Welcome Section -->
        <div class="welcome-section">
          <div class="welcome-left">
            <h2 class="welcome-title">
              Welcome back, {{ userInfo.fullName }}!
            </h2>
            <p class="welcome-subtitle">
              Today is {{ currentDate }} - Ready for another productive day!
            </p>
          </div>
          <div class="welcome-right">
            <el-button
              type="primary"
              :loading="loading"
              @click="refreshDashboard"
              :icon="loading ? null : 'Refresh'"
            >
              {{ loading ? "Refreshing..." : "Refresh Data" }}
            </el-button>
            <el-button
              :type="isCheckedIn ? 'danger' : 'success'"
              size="large"
              @click="toggleCheckIn"
              :loading="checkingIn"
              class="checkin-btn"
            >
              <el-icon>
                <component :is="isCheckedIn ? 'SwitchButton' : 'VideoPlay'" />
              </el-icon>
              {{ isCheckedIn ? "Check Out" : "Check In" }}
            </el-button>
            <small class="last-updated" v-if="lastUpdated">
              Last updated: {{ lastUpdated }}
            </small>
          </div>
        </div>

        <!-- Main Content Grid -->
        <div class="main-content-grid">
          <!-- Announcements Section -->
          <el-card class="announcements-section" shadow="hover">
            <template #header>
              <div class="section-header">
                <div class="section-title">
                  <el-icon><Bell /></el-icon>
                  <span>System Announcements</span>
                </div>
                <el-button
                  type="text"
                  size="small"
                  @click="refreshAnnouncements"
                  :loading="loadingAnnouncements"
                >
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </div>
            </template>
            <div class="announcements-content">
              <div v-if="loadingAnnouncements" class="loading-announcements">
                <el-skeleton :rows="2" animated />
              </div>
              <div
                v-else-if="announcements.length === 0"
                class="empty-announcements"
              >
                <el-empty
                  description="No announcements at this time"
                  :image-size="80"
                />
              </div>
              <div v-else class="announcements-list">
                <!-- Show only the first announcement as preview -->
                <div v-if="announcements.length > 0" class="announcement-card">
                  <div class="announcement-header">
                    <div class="announcement-meta">
                      <el-tag
                        :type="getPriorityTagType(announcements[0].priority)"
                        size="small"
                        class="priority-tag"
                      >
                        {{ announcements[0].priority.toUpperCase() }}
                      </el-tag>
                      <el-tag type="info" size="small" class="type-tag">
                        {{
                          getAnnouncementTypeDisplay(
                            announcements[0].announcementType
                          )
                        }}
                      </el-tag>
                    </div>
                    <div class="announcement-time">
                      {{ formatAnnouncementTime(announcements[0].createdAt) }}
                    </div>
                  </div>
                  <div class="announcement-body">
                    <h4 class="announcement-title">
                      {{ announcements[0].title }}
                    </h4>
                    <p class="announcement-content">
                      {{ announcements[0].content }}
                    </p>
                    <div class="announcement-footer">
                      <span class="announcement-author">
                        <el-icon><User /></el-icon>
                        {{ announcements[0].createdByName || "Admin" }}
                      </span>
                    </div>
                  </div>
                </div>

                <!-- View More button if there are multiple announcements -->
                <div
                  v-if="announcements.length > 1"
                  class="view-more-container"
                >
                  <el-button
                    type="text"
                    class="view-more-btn"
                    @click="goToAnnouncements"
                  >
                    <el-icon><ArrowRight /></el-icon>
                    View {{ announcements.length - 1 }} more announcement{{
                      announcements.length > 2 ? "s" : ""
                    }}
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>

          <!-- Quick Actions Section -->
          <el-card class="quick-actions-section" shadow="hover">
            <template #header>
              <div class="section-header">
                <div class="section-title">
                  <el-icon><Operation /></el-icon>
                  <span>Quick Actions</span>
                </div>
              </div>
            </template>
            <div class="actions-grid">
              <el-button
                type="primary"
                size="large"
                class="action-button"
                @click="goToTaskList"
              >
                <el-icon><Document /></el-icon>
                <span>My Tasks</span>
              </el-button>
              <el-button
                type="success"
                size="large"
                class="action-button"
                @click="goToTaskWall"
              >
                <el-icon><Postcard /></el-icon>
                <span>Task Wall</span>
              </el-button>
              <el-button
                type="info"
                size="large"
                class="action-button"
                @click="goToProfile"
              >
                <el-icon><User /></el-icon>
                <span>My Profile</span>
              </el-button>
            </div>
          </el-card>
        </div>

        <!-- Recent Tasks Preview -->
        <el-card class="tasks-preview-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">Recent Tasks &emsp;</span>
              <el-button type="text" @click="goToTaskList">
                View All Tasks
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="tasks-preview">
            <el-table
              :data="recentTasks"
              style="width: 100%"
              class="tasks-table"
            >
              <el-table-column prop="title" label="Task Name" min-width="200">
                <template #default="{ row }">
                  <div class="task-name">
                    <span>{{ row.title }}</span>
                    <el-tag
                      :type="getPriorityType(row.priority)"
                      size="small"
                      v-if="
                        row.priority === 'urgent' || row.priority === 'high'
                      "
                    >
                      {{ row.priority }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="scheduledTime" label="Time" width="120">
                <template #default="{ row }">
                  {{ formatTime(row.scheduledTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="Status" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" size="small">
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="Actions" width="120">
                <template #default="{ row }">
                  <el-button
                    size="small"
                    @click="viewTaskDetail(row.id)"
                    class="detail-btn"
                  >
                    View
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- Empty State -->
            <div v-if="recentTasks.length === 0" class="empty-tasks">
              <el-empty description="No recent tasks" :image-size="100">
                <el-button type="primary" @click="goToTaskWall">
                  Browse Task Wall
                </el-button>
              </el-empty>
            </div>
          </div>
        </el-card>
      </div>
      <!-- End Dashboard Content -->
    </div>

    <!-- Simple Attendance Dialog -->
    <el-dialog
      v-model="locationDialogVisible"
      title="⏰ Attendance Check-in"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="simple-attendance">
        <div class="attendance-info">
          <el-icon size="48" color="#409EFF"><Clock /></el-icon>
          <h3>Ready to check in?</h3>
          <p>Current time: {{ new Date().toLocaleString() }}</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="locationDialogVisible = false" size="large">
            Cancel
          </el-button>
          <el-button
            type="primary"
            @click="proceedWithCheckIn"
            :loading="checkingIn"
            size="large"
          >
            <el-icon><CircleCheck /></el-icon>
            Confirm Check-in
          </el-button>
        </div>
      </template>
    </el-dialog>
  </JanitorLayout>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { AuthUtils } from "@/utils/auth";
import { API } from "@/utils/request";
import { AttendanceService } from "@/utils/attendanceService";
import JanitorLayout from "@/components/JanitorLayout.vue";
import {
  Document,
  CircleCheck,
  Loading,
  Clock,
  Bell,
  ArrowRight,
  SwitchButton,
  VideoPlay,
  User,
  Postcard,
  Operation,
  Refresh,
  Calendar,
  TrendCharts,
} from "@element-plus/icons-vue";

export default {
  name: "JanitorDashboard",
  components: {
    JanitorLayout,
    Document,
    CircleCheck,
    Loading,
    Clock,
    Bell,
    ArrowRight,
    SwitchButton,
    VideoPlay,
    User,
    Postcard,
    Operation,
    Refresh,
    Calendar,
    TrendCharts,
  },
  setup() {
    const router = useRouter();

    // Reactive data - simplified with centralized state
    const checkingIn = ref(false);
    const locationDialogVisible = ref(false);
    const loading = ref(true);
    const loadingAnnouncements = ref(false);

    // User info from JWT token
    const userInfo = ref({
      fullName: "Janitor",
      userId: null,
    });

    // Initialize user info from JWT token
    const initUserInfo = () => {
      if (!AuthUtils.isAuthenticated()) {
        console.error("User not authenticated, redirecting to login");
        ElMessage.error("Session expired, please login again");
        router.push("/");
        return false;
      }

      const token = AuthUtils.getToken();
      const tokenUserInfo = AuthUtils.getUserInfo();
      const userId = AuthUtils.getUserId();

      console.log("JWT Token:", token ? "Present" : "Missing");
      console.log("User ID from token:", userId);
      console.log("User info from storage:", tokenUserInfo);

      if (userId && tokenUserInfo) {
        userInfo.value = {
          fullName: tokenUserInfo.fullName || "Janitor",
          userId: userId,
        };
        return true;
      } else if (userId) {
        userInfo.value = {
          fullName: "Janitor",
          userId: userId,
        };
        return true;
      } else {
        console.error("Failed to get user ID from JWT token");
        ElMessage.error("Invalid session data, please login again");
        AuthUtils.removeToken();
        router.push("/");
        return false;
      }
    };

    // Add initialization state tracking
    const attendanceInitialized = ref(false);
    // Force reactivity trigger for attendance updates
    const attendanceUpdateTrigger = ref(0);

    // Centralized attendance state using AttendanceService with forced reactivity
    const attendanceState = computed(() => {
      if (
        !userInfo.value.userId ||
        loading.value ||
        !attendanceInitialized.value
      )
        return null;

      // Trigger dependency to force recomputation
      attendanceUpdateTrigger.value;
      return AttendanceService.getUserState(userInfo.value.userId);
    });

    // Computed properties for attendance status using centralized state
    const isCheckedIn = computed(() => {
      return attendanceState.value?.isCheckedIn || false;
    });

    const checkInTime = computed(() => {
      return attendanceState.value?.checkInTime || null;
    });

    // Force attendance state refresh
    const forceAttendanceRefresh = () => {
      attendanceUpdateTrigger.value++;
      console.log("Forced attendance refresh triggered");
    };

    // Subscribe to attendance state changes for real-time updates
    const subscribeToAttendanceChanges = () => {
      if (userInfo.value.userId) {
        AttendanceService.onStateChange(userInfo.value.userId, (newState) => {
          console.log("Attendance state updated in JanitorDash:", newState);
          // Force reactivity update
          forceAttendanceRefresh();
        });
      }
    };

    // Current date
    const currentDate = computed(() => {
      return new Date().toLocaleDateString("en-GB", {
        year: "numeric",
        month: "long",
        day: "numeric",
        weekday: "long",
      });
    });

    // Work hours calculation using AttendanceService
    const workHours = computed(() => {
      if (userInfo.value.userId) {
        return AttendanceService.getTimeSinceCheckIn(userInfo.value.userId);
      }
      return "0h 0m";
    });

    // Task summary with fallback values
    const taskSummary = reactive({
      total: 0,
      completed: 0,
      inProgress: 0,
      overdue: 0,
    });

    // Recent tasks data with fallback
    const recentTasks = ref([]);

    // Real announcements from database
    const announcements = ref([]);

    // Auto refresh
    const lastUpdated = ref("");
    const autoRefreshInterval = ref(null);

    // Load announcements from database
    const loadAnnouncements = async () => {
      try {
        loadingAnnouncements.value = true;
        console.log("Loading announcements from database...");

        const response = await API.get("/api/announcements/janitor");

        console.log("Announcements API response:", response.data);

        if (response.data && Array.isArray(response.data)) {
          announcements.value = response.data
            .filter(
              (announcement) => announcement.isVisible && announcement.isActive
            )
            .sort((a, b) => {
              const priorityOrder = { urgent: 4, high: 3, normal: 2, low: 1 };
              const priorityDiff =
                (priorityOrder[b.priority] || 2) -
                (priorityOrder[a.priority] || 2);
              if (priorityDiff !== 0) return priorityDiff;
              return new Date(b.createdAt) - new Date(a.createdAt);
            })
            .slice(0, 10);

          console.log("Loaded announcements:", announcements.value.length);
        } else {
          announcements.value = [];
        }
      } catch (error) {
        console.warn("Failed to load announcements:", error);
        announcements.value = [];
      } finally {
        loadingAnnouncements.value = false;
      }
    };

    // API Functions for Janitor Dashboard
    const loadJanitorDashboard = async () => {
      const userId = userInfo.value.userId;
      if (!userId) {
        console.warn("No user ID found for janitor dashboard");
        return;
      }

      try {
        loading.value = true;
        console.log("Loading janitor dashboard for user:", userId);

        const response = await API.get(`/api/janitors/${userId}/dashboard`);
        console.log("Janitor dashboard API response:", response.data);

        if (response.data && response.data.taskStats) {
          const apiStats = response.data.taskStats;
          taskSummary.total = apiStats.total || 0;
          taskSummary.completed = apiStats.completed || 0;
          taskSummary.inProgress = apiStats.inProgress || 0;
          taskSummary.overdue = 0;

          console.log("Updated task summary:", taskSummary);
        }

        if (response.data && response.data.recentTasks) {
          const apiTasks = response.data.recentTasks;
          recentTasks.value = apiTasks.slice(0, 5).map((task) => ({
            id: task.taskId,
            title: task.title,
            location: task.location,
            status: task.status,
            priority: task.priority || "normal",
            scheduledTime: formatTime(task.scheduledTime),
          }));
          console.log("Updated recent tasks:", recentTasks.value);
        }

        lastUpdated.value = new Date().toLocaleTimeString();
        console.log("Janitor dashboard loaded successfully");
      } catch (error) {
        console.warn(
          "Failed to load janitor dashboard data, using fallback:",
          error
        );
        ElMessage.info(
          "Unable to load latest task data. Please check your connection."
        );
      } finally {
        loading.value = false;
      }
    };

    // Manual refresh dashboard data
    const refreshDashboard = async () => {
      await Promise.all([loadJanitorDashboard(), loadAnnouncements()]);
      ElMessage.success("Dashboard data refreshed successfully");
    };

    // Refresh announcements only
    const refreshAnnouncements = async () => {
      await loadAnnouncements();
      if (!loadingAnnouncements.value) {
        ElMessage.success("Announcements refreshed successfully");
      }
    };

    // Auto refresh every 60 seconds
    const startAutoRefresh = () => {
      autoRefreshInterval.value = setInterval(() => {
        if (!loading.value && userInfo.value.userId) {
          loadJanitorDashboard();
          loadAnnouncements();
        }
      }, 60000);
    };

    // Stop auto refresh
    const stopAutoRefresh = () => {
      if (autoRefreshInterval.value) {
        clearInterval(autoRefreshInterval.value);
        autoRefreshInterval.value = null;
      }
    };

    // Simplified check-in/check-out methods using AttendanceService
    const toggleCheckIn = async () => {
      if (isCheckedIn.value) {
        await handleCheckOut();
      } else {
        locationDialogVisible.value = true;
      }
    };

    const proceedWithCheckIn = async () => {
      if (!userInfo.value.userId) {
        ElMessage.error("User information not available");
        return;
      }

      try {
        checkingIn.value = true;
        console.log("Proceeding with check-in using AttendanceService...");

        // Use AttendanceService with user isolation
        const result = await AttendanceService.checkIn(
          "Office Location",
          "",
          userInfo.value.userId
        );

        if (result.success) {
          locationDialogVisible.value = false;
          await loadJanitorDashboard();
          // Force immediate attendance state refresh for real-time UI update
          forceAttendanceRefresh();
          console.log("Check-in successful - attendance state refreshed");
        } else {
          ElMessage.error(`Check-in failed: ${result.error}`);
        }
      } catch (error) {
        console.error("Check-in failed:", error);
        ElMessage.error(`Check-in failed: ${error.message}`);
      } finally {
        checkingIn.value = false;
      }
    };

    const handleCheckOut = async () => {
      if (!userInfo.value.userId) {
        ElMessage.error("User information not available");
        return;
      }

      try {
        await ElMessageBox.confirm(
          "Are you sure you want to check out?",
          "Check Out Confirmation",
          {
            confirmButtonText: "Confirm Check Out",
            cancelButtonText: "Cancel",
            type: "warning",
          }
        );

        checkingIn.value = true;
        console.log("Proceeding with check-out using AttendanceService...");

        // Use AttendanceService with user isolation and simplified parameters
        const result = await AttendanceService.checkOut(
          "Office Location",
          1.0,
          "",
          userInfo.value.userId
        );

        if (result.success) {
          await loadJanitorDashboard();
          // Force immediate attendance state refresh for real-time UI update
          forceAttendanceRefresh();
          console.log("Check-out successful - attendance state refreshed");
        } else {
          ElMessage.error(`Check-out failed: ${result.error}`);
        }
      } catch (error) {
        if (error.name !== "ElMessageBoxClosedByCancel") {
          console.error("Check-out error:", error);
          ElMessage.error(
            `Check-out failed: ${error.message || "Please try again"}`
          );
        }
      } finally {
        checkingIn.value = false;
      }
    };

    // Navigation methods
    const goToTaskList = () => {
      router.push("/janitor/tasks");
    };

    const goToTaskWall = () => {
      router.push("/janitor/task-wall");
    };

    const goToAttendance = () => {
      router.push("/janitor/attendance");
    };

    const goToProfile = () => {
      router.push("/janitor/profile");
    };

    const goToAnnouncements = () => {
      router.push("/janitor/announcements");
    };

    const viewTaskDetail = (taskId) => {
      router.push(`/janitor/tasks/${taskId}`);
    };

    // Utility functions
    const getStatusType = (status) => {
      const types = {
        pending: "warning",
        in_progress: "primary",
        completed: "success",
        overdue: "danger",
      };
      return types[status] || "info";
    };

    const getStatusText = (status) => {
      const texts = {
        pending: "Pending",
        in_progress: "In Progress",
        completed: "Completed",
        overdue: "Overdue",
      };
      return texts[status] || status;
    };

    const getPriorityType = (priority) => {
      const types = {
        low: "info",
        normal: "",
        high: "warning",
        urgent: "danger",
      };
      return types[priority] || "";
    };

    const formatTime = (time) => {
      if (typeof time === "string" && time.length === 5) {
        return time;
      }
      return new Date(time).toLocaleTimeString("en-GB", {
        hour: "2-digit",
        minute: "2-digit",
      });
    };

    const formatDateTime = (dateTime) => {
      return new Date(dateTime).toLocaleString("en-GB", {
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
      });
    };

    // Announcement utility functions
    const getPriorityTagType = (priority) => {
      const types = {
        urgent: "danger",
        high: "warning",
        normal: "success",
        low: "info",
      };
      return types[priority] || "info";
    };

    const getAnnouncementTypeDisplay = (type) => {
      const displays = {
        general: "General",
        maintenance: "Maintenance",
        policy: "Policy",
        emergency: "Emergency",
      };
      return displays[type] || "General";
    };

    const formatAnnouncementTime = (dateTime) => {
      const now = new Date();
      const announcementDate = new Date(dateTime);
      const diffInHours = Math.floor(
        (now - announcementDate) / (1000 * 60 * 60)
      );

      if (diffInHours < 1) {
        const diffInMinutes = Math.floor(
          (now - announcementDate) / (1000 * 60)
        );
        return diffInMinutes <= 1 ? "Just now" : `${diffInMinutes} minutes ago`;
      } else if (diffInHours < 24) {
        return `${diffInHours} hour${diffInHours > 1 ? "s" : ""} ago`;
      } else {
        const diffInDays = Math.floor(diffInHours / 24);
        if (diffInDays < 7) {
          return `${diffInDays} day${diffInDays > 1 ? "s" : ""} ago`;
        } else {
          return announcementDate.toLocaleDateString("en-GB", {
            month: "short",
            day: "numeric",
            year: "numeric",
          });
        }
      }
    };

    // Lifecycle with API integration
    onMounted(async () => {
      try {
        loading.value = true;

        // Initialize user info
        const initSuccess = initUserInfo();

        if (!initSuccess) {
          loading.value = false;
          return;
        }

        // Initialize AttendanceService FIRST before other components try to access it (suppress errors during init)
        console.log(
          "Initializing AttendanceService for user:",
          userInfo.value.userId
        );
        console.log("Authentication status:", AuthUtils.isAuthenticated());
        await AttendanceService.loadStatus(userInfo.value.userId, true);
        attendanceInitialized.value = true;
        console.log("AttendanceService initialized successfully");

        // Load janitor dashboard data and announcements
        await Promise.all([loadJanitorDashboard(), loadAnnouncements()]);

        // Subscribe to attendance state changes for real-time sync
        subscribeToAttendanceChanges();

        // Start auto refresh for task data and announcements
        startAutoRefresh();

        // Start attendance service auto-refresh with user isolation
        await AttendanceService.startAutoRefresh(userInfo.value.userId, 30000);

        console.log(
          "Janitor dashboard initialized successfully with API integration"
        );
      } catch (error) {
        console.error("Error in onMounted:", error);
        ElMessage.error(
          "Failed to initialize dashboard. Please try refreshing the page."
        );
      } finally {
        loading.value = false;
      }
    });

    // Cleanup on component unmount with user-specific cleanup
    onUnmounted(() => {
      stopAutoRefresh();

      // Clean up user-specific AttendanceService state
      if (userInfo.value.userId) {
        AttendanceService.stopAutoRefresh(userInfo.value.userId);
        AttendanceService.clearUserState(userInfo.value.userId);
      }
    });

    return {
      // Attendance state - now using computed properties
      checkingIn,
      isCheckedIn,
      checkInTime,
      attendanceState,
      locationDialogVisible,

      // User and UI state
      userInfo,
      currentDate,
      workHours,
      taskSummary,
      recentTasks,
      announcements,
      loading,
      loadingAnnouncements,
      lastUpdated,

      // Methods
      toggleCheckIn,
      proceedWithCheckIn,
      forceAttendanceRefresh,
      refreshDashboard,
      refreshAnnouncements,
      loadJanitorDashboard,
      loadAnnouncements,
      goToTaskList,
      goToTaskWall,
      goToAttendance,
      goToProfile,
      goToAnnouncements,
      viewTaskDetail,
      getStatusType,
      getStatusText,
      getPriorityType,
      getPriorityTagType,
      getAnnouncementTypeDisplay,
      formatTime,
      formatDateTime,
      formatAnnouncementTime,
    };
  },
};
</script>

<style scoped>
/* Loading state */
.loading-container {
  padding: 2rem;
  width: 100%;
}

/* Dashboard content - wider layout to match admin proportions */
.dashboard-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* Welcome section */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2rem 2.5rem;
  border-radius: 1.5rem;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.welcome-left {
  flex: 1;
}

.welcome-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.5rem;
}

.welcome-title {
  font-size: 2rem;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 0.5rem 0;
}

.welcome-subtitle {
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.125rem;
  margin: 0;
}

.checkin-btn {
  font-size: 1.125rem;
  font-weight: 600;
  padding: 1rem 2rem;
  border-radius: 1rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.checkin-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

.last-updated {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.75rem;
  white-space: nowrap;
}

/* Main content grid - wider layout */
.main-content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;
}

/* Section styles */
.el-card {
  border-radius: 1rem;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.el-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  font-size: 1.1rem;
  color: #2c3e50;
}

/* Announcements section */
.announcements-section {
  height: fit-content;
}

.announcements-content {
  min-height: 200px;
}

.loading-announcements {
  padding: 1rem;
}

.empty-announcements {
  padding: 2rem;
  text-align: center;
  color: #8892a6;
}

.announcements-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.view-more-container {
  text-align: center;
  padding: 1rem 0;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  margin-top: 0.5rem;
}

.view-more-btn {
  color: #667eea;
  font-weight: 600;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.view-more-btn:hover {
  color: #5a67d8;
  transform: translateX(2px);
}

.announcement-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  padding: 1.25rem;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  position: relative;
}

.announcement-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: rgba(102, 126, 234, 0.2);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.announcement-meta {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.priority-tag {
  font-weight: 600;
  font-size: 0.75rem;
}

.type-tag {
  font-size: 0.75rem;
}

.announcement-time {
  font-size: 0.75rem;
  color: #8892a6;
  white-space: nowrap;
}

.announcement-body {
  flex: 1;
}

.announcement-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 0.5rem 0;
  line-height: 1.3;
}

.announcement-content {
  color: #5a6c7d;
  font-size: 0.9rem;
  line-height: 1.5;
  margin: 0 0 0.75rem 0;
  word-wrap: break-word;
}

.announcement-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 0.5rem;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.announcement-author {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: #8892a6;
  font-weight: 500;
}

/* Quick actions section - Redesigned for professional appearance */
.quick-actions-section {
  height: fit-content;
}

.actions-grid {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.5rem;
}

.action-button {
  width: 100%;
  height: 3.5rem;
  border-radius: 0.75rem;
  font-weight: 600;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 1rem;
  padding: 0 1.5rem;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
}

/* Professional color schemes and hover effects */
.action-button[type="primary"] {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: 2px solid transparent;
}

.action-button[type="primary"]:hover {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46a3 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.4);
}

.action-button[type="success"] {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: 2px solid transparent;
}

.action-button[type="success"]:hover {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(16, 185, 129, 0.4);
}

.action-button[type="warning"] {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  border: 2px solid transparent;
}

.action-button[type="warning"]:hover {
  background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(245, 158, 11, 0.4);
}

.action-button[type="info"] {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
  border: 2px solid transparent;
}

.action-button[type="info"]:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(59, 130, 246, 0.4);
}

/* Icon styling */
.action-button .el-icon {
  font-size: 1.375rem;
  flex-shrink: 0;
}

/* Text styling - no truncation issues */
.action-button span {
  font-size: 1rem;
  font-weight: 600;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  flex: 1;
  text-align: left;
}

/* Subtle animated background effect */
.action-button::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.1),
    transparent
  );
  transition: left 0.5s ease;
}

.action-button:hover::before {
  left: 100%;
}

/* Tasks preview card */
.tasks-preview-card {
  grid-column: 1 / -1;
  margin-bottom: 2rem;
}

.tasks-table {
  border-radius: 0.5rem;
}

.task-name {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.detail-btn {
  border-radius: 0.5rem;
  color: #10b981;
  border-color: #10b981;
}

.detail-btn:hover {
  background: #10b981;
  color: white;
}

.empty-tasks {
  padding: 2rem;
  text-align: center;
}

/* Simple Attendance Dialog */
.simple-attendance {
  padding: 1.5rem;
  text-align: center;
}

.attendance-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.attendance-info h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.25rem;
}

.attendance-info p {
  margin: 0.25rem 0;
  color: #6b7280;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

/* Enhanced Responsive Design */
@media (max-width: 1200px) {
  .dashboard-content {
    max-width: 1200px;
    padding: 0 0.75rem;
  }

  .main-content-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .actions-grid {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .action-button {
    height: 3.25rem;
  }
}

@media (max-width: 768px) {
  .dashboard-content {
    max-width: 100%;
    padding: 0 0.5rem;
  }

  .welcome-section {
    flex-direction: column;
    text-align: center;
    gap: 1.5rem;
    padding: 1.5rem;
  }

  .main-content-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .checkin-btn {
    width: 100%;
  }

  .actions-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 0.75rem;
  }

  .action-button {
    height: 4rem;
    flex-direction: column;
    justify-content: center;
    gap: 0.5rem;
    padding: 0.75rem;
  }

  .action-button span {
    font-size: 0.875rem;
    text-align: center;
    line-height: 1.2;
  }

  .action-button .el-icon {
    font-size: 1.25rem;
  }

  .announcements-list {
    max-height: 400px;
  }

  .announcement-card {
    padding: 1rem;
  }

  .announcement-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}

@media (max-width: 480px) {
  .welcome-title {
    font-size: 1.5rem;
  }

  .actions-grid {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .action-button {
    height: 3.5rem;
    flex-direction: row;
    justify-content: flex-start;
    gap: 1rem;
    padding: 0 1.25rem;
  }

  .action-button span {
    font-size: 0.9rem;
    text-align: left;
  }

  .action-button .el-icon {
    font-size: 1.25rem;
  }

  .announcement-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }

  .announcement-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}
</style>
