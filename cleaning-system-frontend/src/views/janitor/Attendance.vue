<template>
  <JanitorLayout>
    <div class="attendance-container">
      <!-- Page Header -->
      <div class="page-header">
        <h1 class="page-title">Attendance Management</h1>
        <p class="page-subtitle">
          Simple time tracking with timestamp recording
        </p>
      </div>

      <!-- Current Status Card -->
      <div class="status-section">
        <el-card class="status-card" shadow="never">
          <div class="status-content">
            <div class="status-info">
              <div class="current-time">
                <div class="time-display">{{ currentTime }}</div>
                <div class="date-display">{{ currentDate }}</div>
              </div>
              <div class="attendance-status">
                <div class="status-label">Current Status</div>
                <el-tag
                  :type="attendanceStatus.checkedIn ? 'success' : 'info'"
                  size="large"
                  class="status-tag"
                >
                  {{
                    attendanceStatus.checkedIn ? "Checked In" : "Not Checked In"
                  }}
                </el-tag>
                <div
                  v-if="
                    attendanceStatus.checkedIn && attendanceStatus.checkInTime
                  "
                  class="checkin-details"
                >
                  <div class="checkin-time">
                    Check-in: {{ formatDateTime(attendanceStatus.checkInTime) }}
                  </div>
                  <div class="work-hours">
                    Current Hours:
                    {{
                      attendanceStatus.currentWorkHours?.toFixed(2) || "0.00"
                    }}h
                  </div>
                </div>
              </div>
            </div>

            <!-- Simple Check-in Actions -->
            <div class="actions-section">
              <div class="action-buttons">
                <el-button
                  :type="attendanceStatus.checkedIn ? 'danger' : 'success'"
                  size="large"
                  @click="toggleCheckIn"
                  :loading="processingAttendance"
                  class="checkin-btn"
                >
                  <el-icon>
                    <component
                      :is="
                        attendanceStatus.checkedIn
                          ? 'SwitchButton'
                          : 'VideoPlay'
                      "
                    />
                  </el-icon>
                  {{ attendanceStatus.checkedIn ? "Check Out" : "Check In" }}
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- Today's Summary -->
      <div class="summary-section">
        <div class="summary-grid">
          <div class="summary-item work-hours">
            <div class="summary-icon">
              <el-icon size="24"><Timer /></el-icon>
            </div>
            <div class="summary-content">
              <div class="summary-value">{{ todaySummary.workHours }}</div>
              <div class="summary-label">Today's Hours</div>
            </div>
          </div>
          <div class="summary-item overtime">
            <div class="summary-icon">
              <el-icon size="24"><Clock /></el-icon>
            </div>
            <div class="summary-content">
              <div class="summary-value">{{ todaySummary.overtimeHours }}</div>
              <div class="summary-label">Overtime</div>
            </div>
          </div>
          <div class="summary-item monthly-hours">
            <div class="summary-icon">
              <el-icon size="24"><Calendar /></el-icon>
            </div>
            <div class="summary-content">
              <div class="summary-value">{{ todaySummary.monthlyHours }}</div>
              <div class="summary-label">Monthly Hours</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Attendance History -->
      <el-card class="history-card" shadow="never">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <el-icon><Document /></el-icon>
              <span>Recent Attendance (Last 7 Days)</span>
            </div>
            <div class="header-right">
              <el-button @click="refreshHistory" :loading="loadingHistory">
                <el-icon><Refresh /></el-icon>
                Refresh
              </el-button>
              <el-button @click="viewFullHistory" class="full-history-btn">
                <el-icon><FolderOpened /></el-icon>
                View Full History
              </el-button>
            </div>
          </div>
        </template>

        <el-table
          :data="recentHistory"
          stripe
          class="attendance-table"
          v-loading="loadingHistory"
        >
          <el-table-column prop="workDate" label="Date" width="180">
            <template #default="{ row }">
              {{ formatDate(row.workDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="checkInTime" label="Check-in" width="150">
            <template #default="{ row }">
              <span v-if="row.checkInTime">{{
                formatTime(row.checkInTime)
              }}</span>
              <span v-else>--</span>
            </template>
          </el-table-column>
          <el-table-column prop="checkOutTime" label="Check-out" width="150">
            <template #default="{ row }">
              <span v-if="row.checkOutTime">{{
                formatTime(row.checkOutTime)
              }}</span>
              <span v-else>--</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- Check-out Dialog -->
    <el-dialog
      v-model="checkoutDialogVisible"
      title="Check Out Confirmation"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="checkout-dialog">
        <div class="checkout-summary">
          <h4>Today's Work Summary</h4>
          <div class="work-summary">
            <div class="summary-row">
              <span>Check-in Time:</span>
              <span>{{ formatDateTime(attendanceStatus.checkInTime) }}</span>
            </div>
            <div class="summary-row">
              <span>Check-out Time:</span>
              <span>{{ currentTime }}</span>
            </div>
            <div class="summary-row">
              <span>Total Work Hours:</span>
              <span class="highlight"
                >{{
                  attendanceStatus.currentWorkHours?.toFixed(2) || "0.00"
                }}h</span
              >
            </div>
          </div>
        </div>

        <div class="checkout-options">
          <div class="form-item">
            <label>Notes (optional)</label>
            <el-input
              v-model="checkoutData.notes"
              type="textarea"
              :rows="3"
              placeholder="Add any notes about your work today..."
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="checkoutDialogVisible = false">Cancel</el-button>
          <el-button
            type="primary"
            @click="confirmCheckOut"
            :loading="processingAttendance"
          >
            <el-icon><Check /></el-icon>
            Confirm Check Out
          </el-button>
        </div>
      </template>
    </el-dialog>
  </JanitorLayout>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { AuthUtils } from "@/utils/auth";
import { API } from "@/utils/request";
import { AttendanceService } from "@/utils/attendanceService";
import JanitorLayout from "@/components/JanitorLayout.vue";
import {
  Timer,
  Clock,
  Calendar,
  Document,
  SwitchButton,
  VideoPlay,
  Check,
  Refresh,
  FolderOpened,
  OfficeBuilding,
} from "@element-plus/icons-vue";

export default {
  name: "AttendanceView",
  components: {
    JanitorLayout,
    Timer,
    Clock,
    Calendar,
    Document,
    SwitchButton,
    VideoPlay,
    Check,
    Refresh,
    FolderOpened,
    OfficeBuilding,
  },
  setup() {
    const router = useRouter();

    // Reactive data
    const currentTime = ref("");
    const currentDate = ref("");
    const processingAttendance = ref(false);
    const loadingHistory = ref(false);
    const checkoutDialogVisible = ref(false);

    // Get user ID for state management
    const userId = AuthUtils.getUserId();

    // Centralized attendance state using AttendanceService
    const attendanceState = computed(() => {
      return AttendanceService.getUserState(userId);
    });

    // Computed properties for attendance status using centralized state
    const attendanceStatus = computed(() => {
      const state = attendanceState.value;
      return state
        ? {
            checkedIn: state.isCheckedIn,
            checkInTime: state.checkInTime,
            checkInLocation: state.todayRecord.checkInLocation,
            currentWorkHours: state.currentWorkHours,
            status: state.status,
            message: null,
          }
        : {
            checkedIn: false,
            checkInTime: null,
            checkInLocation: null,
            currentWorkHours: 0,
            status: null,
            message: null,
          };
    });

    // Checkout data
    const checkoutData = reactive({
      notes: "",
    });

    // Attendance history
    const recentHistory = ref([]);

    // Today's summary computed using centralized state
    const todaySummary = computed(() => {
      const state = attendanceState.value;
      if (!state) {
        return {
          workHours: "0.0h",
          overtimeHours: "0.0h",
          monthlyHours: "0.0h",
        };
      }

      const currentHours = state.currentWorkHours || 0;
      const standardHours = state.targets.standardWorkHours || 8.0;
      const overtimeHours = Math.max(0, currentHours - standardHours);
      const monthlyHours = state.monthlyStats.monthlyHours || 0;

      return {
        workHours: currentHours.toFixed(1) + "h",
        overtimeHours: overtimeHours.toFixed(1) + "h",
        monthlyHours: monthlyHours.toFixed(1) + "h",
      };
    });

    // Subscribe to attendance state changes for real-time updates
    const subscribeToAttendanceChanges = () => {
      if (userId) {
        AttendanceService.onStateChange(userId, (newState) => {
          console.log("Attendance state updated in Attendance.vue:", newState);
          // State is automatically reactive through computed properties
        });
      }
    };

    // Time update interval
    let timeInterval = null;
    let statusInterval = null;

    // Methods
    const updateTime = () => {
      const now = new Date();
      currentTime.value = now.toLocaleTimeString("en-GB", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        hour12: false,
      });
      currentDate.value = now.toLocaleDateString("en-GB", {
        year: "numeric",
        month: "long",
        day: "numeric",
        weekday: "long",
      });
    };

    const toggleCheckIn = async () => {
      if (attendanceStatus.value.checkedIn) {
        checkoutDialogVisible.value = true;
      } else {
        await handleCheckIn();
      }
    };

    const handleCheckIn = async () => {
      if (!userId) {
        ElMessage.error("User authentication error");
        return;
      }

      processingAttendance.value = true;

      try {
        const result = await AttendanceService.checkIn(
          "Office Location",
          "Check-in from full attendance interface",
          userId
        );

        if (result.success) {
          // Refresh history - state updates automatically through computed properties
          await loadRecentHistory();
        } else {
          ElMessage.error(`Check-in failed: ${result.error}`);
        }
      } catch (error) {
        console.error("Check-in error:", error);
        ElMessage.error("Check-in failed. Please try again.");
      } finally {
        processingAttendance.value = false;
      }
    };

    const confirmCheckOut = async () => {
      if (!userId) {
        ElMessage.error("User authentication error");
        return;
      }

      processingAttendance.value = true;

      try {
        const notes =
          checkoutData.notes + " [Check-out from full attendance interface]";

        const result = await AttendanceService.checkOut(
          "Office Location",
          1.0, // Default break time
          notes,
          userId
        );

        if (result.success) {
          checkoutDialogVisible.value = false;

          // Reset checkout data
          checkoutData.notes = "";

          // Refresh history - state updates automatically through computed properties
          await loadRecentHistory();
        } else {
          ElMessage.error(`Check-out failed: ${result.error}`);
        }
      } catch (error) {
        console.error("Check-out error:", error);
        ElMessage.error("Check-out failed. Please try again.");
      } finally {
        processingAttendance.value = false;
      }
    };

    const loadRecentHistory = async () => {
      loadingHistory.value = true;

      try {
        const response = await API.get("/api/attendance/history", {
          params: {
            limit: 7, // Last 7 days
          },
        });

        if (response.data && Array.isArray(response.data)) {
          recentHistory.value = response.data;
        }
      } catch (error) {
        console.error("Failed to load attendance history:", error);
        ElMessage.error("Failed to load attendance history");
      } finally {
        loadingHistory.value = false;
      }
    };

    const refreshHistory = async () => {
      await loadRecentHistory();
      ElMessage.success("History refreshed");
    };

    const viewFullHistory = () => {
      router.push("/janitor/attendance/history");
    };

    // Utility functions

    const formatDateTime = (dateTime) => {
      if (!dateTime) return "--";
      return new Date(dateTime).toLocaleString("en-GB", {
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        hour12: false,
      });
    };

    const formatTime = (time) => {
      if (!time) return "--";
      const date = typeof time === "string" ? new Date(time) : time;
      return date.toLocaleTimeString("en-GB", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false,
      });
    };

    const formatDate = (date) => {
      if (!date) return "--";
      return new Date(date).toLocaleDateString("en-GB", {
        month: "2-digit",
        day: "2-digit",
        year: "2-digit",
      });
    };

    // Lifecycle hooks
    onMounted(async () => {
      // Check authentication
      if (!AuthUtils.isAuthenticated() || !userId) {
        ElMessage.error("Please login first");
        router.push("/");
        return;
      }

      // Start time updates
      updateTime();
      timeInterval = setInterval(updateTime, 1000);

      // Initialize AttendanceService with user-aware state
      await AttendanceService.loadStatus(userId);

      // Subscribe to attendance state changes for real-time sync
      subscribeToAttendanceChanges();

      // Load recent history
      await loadRecentHistory();

      // Start the attendance service auto-refresh with user isolation
      await AttendanceService.startAutoRefresh(userId, 30000); // 30 seconds

      // Additional status refresh if checked in (every minute)
      statusInterval = setInterval(async () => {
        if (attendanceStatus.value.checkedIn) {
          await AttendanceService.loadStatus(userId);
        }
      }, 60000); // Every minute
    });

    onUnmounted(() => {
      if (timeInterval) clearInterval(timeInterval);
      if (statusInterval) clearInterval(statusInterval);

      // Clean up user-specific AttendanceService state
      if (userId) {
        AttendanceService.stopAutoRefresh(userId);
        AttendanceService.clearUserState(userId);
      }
    });

    return {
      currentTime,
      currentDate,
      processingAttendance,
      loadingHistory,
      checkoutDialogVisible,

      // Centralized state
      attendanceState,
      attendanceStatus,

      checkoutData,
      recentHistory,
      todaySummary,
      toggleCheckIn,
      confirmCheckOut,
      refreshHistory,
      viewFullHistory,
      formatDateTime,
      formatTime,
      formatDate,
    };
  },
};
</script>

<style scoped>
/* Container */
.attendance-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* Page Header */
.page-header {
  margin-bottom: 2rem;
  text-align: center;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  color: #6b7280;
  font-size: 1.1rem;
}

/* Status Card */
.status-section {
  margin-bottom: 2rem;
}

.status-card {
  border-radius: 1.5rem;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.status-card :deep(.el-card__body) {
  padding: 2rem;
  color: white;
}

.status-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
  flex-wrap: wrap;
}

.status-info {
  display: flex;
  gap: 2rem;
  align-items: center;
  flex-wrap: wrap;
}

.current-time {
  text-align: center;
}

.time-display {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.date-display {
  font-size: 1rem;
  opacity: 0.9;
}

.attendance-status {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.status-label {
  font-size: 0.875rem;
  opacity: 0.8;
}

.status-tag {
  align-self: flex-start;
}

.checkin-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  font-size: 0.875rem;
  opacity: 0.9;
}

/* Actions Section */
.actions-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  min-width: 300px;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  justify-content: center;
}

.checkin-btn {
  font-size: 1rem;
  font-weight: 600;
  padding: 0.75rem 1.5rem;
  border-radius: 0.75rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  flex: 1;
  min-width: 160px;
}

.checkin-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

/* Summary Section */
.summary-section {
  margin-bottom: 2rem;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.summary-item {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-left: 4px solid transparent;
  transition: all 0.3s ease;
}

.summary-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.summary-item.work-hours {
  border-left-color: #10b981;
}
.summary-item.overtime {
  border-left-color: #ef4444;
}
.summary-item.monthly-hours {
  border-left-color: #3b82f6;
}

.summary-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.summary-item.work-hours .summary-icon {
  background: linear-gradient(135deg, #10b981, #059669);
}
.summary-item.overtime .summary-icon {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}
.summary-item.monthly-hours .summary-icon {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.summary-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.summary-label {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
}

/* History Card */
.history-card {
  border-radius: 1rem;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #374151;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.full-history-btn {
  color: #3b82f6;
  border-color: #3b82f6;
}

.full-history-btn:hover {
  background: #3b82f6;
  color: white;
}

.attendance-table {
  border-radius: 0.5rem;
}

/* Checkout Dialog */
.checkout-dialog {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.checkout-summary h4 {
  margin: 0 0 1rem 0;
  color: #374151;
}

.work-summary {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f3f4f6;
}

.summary-row:last-child {
  border-bottom: none;
}

.highlight {
  font-weight: 600;
  color: #10b981;
}

.checkout-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-item label {
  font-weight: 500;
  color: #374151;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

/* Responsive Design */
@media (max-width: 768px) {
  .status-content {
    flex-direction: column;
    text-align: center;
  }

  .status-info {
    flex-direction: column;
    text-align: center;
    width: 100%;
  }

  .actions-section {
    width: 100%;
    min-width: unset;
  }

  .action-buttons {
    justify-content: center;
  }

  .time-display {
    font-size: 2rem;
  }

  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .card-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-right {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .checkin-btn {
    width: 100%;
  }

  .attendance-container {
    padding: 0 0.5rem;
  }
}
</style>
