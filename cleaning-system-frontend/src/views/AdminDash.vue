<template>
  <AdminLayout>
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
            <h2 class="welcome-title">Welcome back, Admin!</h2>
            <p class="welcome-subtitle">
              Here's what's happening with your cleaning operations today.
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
            <small class="last-updated" v-if="lastUpdated">
              Last updated: {{ lastUpdated }}
            </small>
          </div>
        </div>

        <!-- Statistics Cards -->
        <div class="stats-grid">
          <el-card class="stat-card stat-card-blue" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon size="32"><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">Total Staff</div>
                <div class="stat-value">{{ stats.totalStaff }}</div>
              </div>
            </div>
          </el-card>

          <el-card class="stat-card stat-card-green" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon size="32"><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">Active Tasks</div>
                <div class="stat-value">{{ stats.activeTasks }}</div>
              </div>
            </div>
          </el-card>

          <el-card class="stat-card stat-card-orange" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon size="32"><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">Pending Jobs</div>
                <div class="stat-value">{{ stats.pendingJobs }}</div>
              </div>
            </div>
          </el-card>

          <el-card class="stat-card stat-card-success" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon size="32"><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">Completed Today</div>
                <div class="stat-value">{{ stats.completedToday }}</div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- Content Sections -->
        <div class="content-grid">
          <!-- Recent Activities -->
          <el-card class="activities-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">Recent Activities</span>
                <el-button type="text" @click="viewAllActivities"
                  >View All</el-button
                >
              </div>
            </template>
            <div class="activities-list">
              <div
                v-for="activity in recentActivities"
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-icon" :class="activity.type">
                  <el-icon>
                    <component :is="activity.icon" />
                  </el-icon>
                </div>
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-description">
                    {{ activity.description }}
                  </div>
                  <div class="activity-time">{{ activity.time }}</div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- System Alerts -->
          <el-card class="alerts-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">System Alerts</span>
              </div>
            </template>
            <div class="alerts-list">
              <el-alert
                v-for="alert in systemAlerts"
                :key="alert.id"
                :title="alert.title"
                :description="alert.description"
                :type="alert.type"
                :closable="false"
                show-icon
                class="alert-item"
              />
            </div>
          </el-card>

          <!-- Quick Actions -->
          <el-card class="actions-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">Quick Actions</span>
              </div>
            </template>
            <div class="actions-list">
              <el-button
                type="primary"
                size="large"
                class="action-button"
                @click="addNewStaff"
              >
                <el-icon><Plus /></el-icon>
                Add New Staff
              </el-button>
              <el-button
                type="default"
                size="large"
                class="action-button"
                @click="scheduleTask"
              >
                <el-icon><Calendar /></el-icon>
                Schedule Task
              </el-button>
              <el-button
                type="default"
                size="large"
                class="action-button"
                @click="showReportDialog = true"
              >
                <el-icon><Document /></el-icon>
                Generate Report
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
      <!-- End Dashboard Content -->
    </div>

    <!-- Report Generation Dialog -->
    <el-dialog
      v-model="showReportDialog"
      title="Generate Job Monitor Report"
      width="500px"
    >
      <el-form label-width="120px">
        <el-form-item label="Date Range">
          <el-date-picker
            v-model="reportDateRange"
            type="daterange"
            range-separator="to"
            start-placeholder="Start date"
            end-placeholder="End date"
            format="DD/MM/YYYY"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
            :disabled-date="disabledFutureDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item>
          <el-alert
            title="Maximum 6 months date range allowed"
            type="info"
            :closable="false"
            show-icon
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReportDialog = false">Cancel</el-button>
        <el-button
          type="primary"
          @click="generateReport"
          :loading="reportGenerating"
        >
          {{ reportGenerating ? "Generating..." : "Generate CSV" }}
        </el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import AdminLayout from "../components/AdminLayout.vue";
import { API } from "@/utils/request";
import { AuthUtils } from "@/utils/auth";
import {
  Document,
  Clock,
  CircleCheck,
  Plus,
  Warning,
  Tools,
} from "@element-plus/icons-vue";

export default {
  name: "AdminDashboard",
  components: {
    AdminLayout,
    Document,
    Clock,
    CircleCheck,
    Plus,
    Warning,
    Tools,
  },
  setup() {
    // Router for navigation
    const router = useRouter();

    // Loading state
    const loading = ref(false);
    const lastUpdated = ref("");
    const autoRefreshInterval = ref(null);

    // Statistics data with fallback values
    const stats = reactive({
      totalStaff: 6, // Fallback value
      activeTasks: 0,
      pendingJobs: 0,
      completedToday: 0,
    });

    // Recent activities data
    const recentActivities = ref([
      {
        id: 1,
        type: "success",
        icon: "CircleCheck",
        title: "Task Completed",
        description: "Office cleaning completed by Maria Santos",
        time: "2 minutes ago",
      },
      {
        id: 2,
        type: "info",
        icon: "User",
        title: "New Staff Added",
        description: "John Doe joined as Senior Cleaner",
        time: "1 hour ago",
      },
      {
        id: 3,
        type: "warning",
        icon: "Tools",
        title: "Equipment Maintenance",
        description: "Vacuum cleaner needs maintenance in Building A",
        time: "3 hours ago",
      },
    ]);

    // System alerts data
    const systemAlerts = ref([
      {
        id: 1,
        type: "warning",
        title: "Staff Shortage",
        description: "3 cleaners have not confirmed their shifts for tomorrow",
      },
      {
        id: 2,
        type: "info",
        title: "Scheduled Maintenance",
        description: "Building B elevator maintenance at 9 AM tomorrow",
      },
    ]);

    // Load real-time task statistics
    const loadTaskStatistics = async () => {
      try {
        const response = await API.get("/api/tasks/quick-stats");
        if (response.data) {
          stats.activeTasks = response.data.totalTasks || 0;
          stats.pendingJobs =
            (response.data.pendingTasks || 0) +
            (response.data.inProgressTasks || 0);
          stats.completedToday = response.data.completedTasks || 0;
        }
      } catch (error) {
        console.warn("Failed to load task statistics:", error);
      }
    };

    // API Functions
    const loadDashboardData = async () => {
      const userId = AuthUtils.getUserId();
      if (!userId) {
        console.warn("No user ID found, using static data");
        await loadTaskStatistics(); // Still try to get task stats
        return;
      }

      try {
        loading.value = true;

        // Load task statistics first (most reliable)
        await loadTaskStatistics();

        // Try to load admin dashboard data
        const response = await API.get(`/api/admin/dashboard/${userId}`);

        if (response.data && response.data.stats) {
          // Update stats with API data, but prioritize task stats we just loaded
          const apiStats = response.data.stats;
          stats.totalStaff = apiStats.totalStaff || stats.totalStaff;
          // Keep the task stats we loaded from /tasks/quick-stats
        }

        // Update activities if provided
        if (response.data && response.data.recentActivities) {
          recentActivities.value = response.data.recentActivities;
        }

        // Update alerts if provided
        if (response.data && response.data.systemAlerts) {
          systemAlerts.value = response.data.systemAlerts;
        }

        console.log("Dashboard data loaded successfully");

        // Update last refresh timestamp
        lastUpdated.value = new Date().toLocaleTimeString();
      } catch (error) {
        console.warn(
          "Failed to load dashboard data, using available data:",
          error
        );
        ElMessage.info(
          "Using available data - some information may not be current"
        );
      } finally {
        loading.value = false;
      }
    };

    // Manual refresh dashboard data
    const refreshDashboard = async () => {
      await loadDashboardData();
      ElMessage.success("Dashboard data refreshed successfully");
    };

    // Auto refresh every 30 seconds
    const startAutoRefresh = () => {
      autoRefreshInterval.value = setInterval(() => {
        if (!loading.value) {
          // Only refresh if not already loading
          // Prioritize task statistics for more frequent updates
          loadTaskStatistics();
          loadDashboardData();
        }
      }, 30000); // 30 seconds
    };

    // Stop auto refresh
    const stopAutoRefresh = () => {
      if (autoRefreshInterval.value) {
        clearInterval(autoRefreshInterval.value);
        autoRefreshInterval.value = null;
      }
    };

    // Methods
    const viewAllActivities = () => {
      // Navigate to a dedicated activities page (if exists)
      // For now, show message about future implementation
      ElMessage.info("Activities page will be available in the next update");
    };

    const addNewStaff = () => {
      // Navigate to staff management page using Vue Router
      router.push('/admin/staff');
      ElMessage.success('Navigating to Staff Management...');
    };

    const scheduleTask = () => {
      // Navigate to task calendar page using Vue Router
      router.push('/admin/calendar');
      ElMessage.success('Navigating to Task Calendar...');
    };

    // Report generation state
    const showReportDialog = ref(false);
    const reportDateRange = ref([]);
    const reportGenerating = ref(false);

    // Date picker shortcuts
    const dateShortcuts = [
      {
        text: "Last 7 Days",
        value: () => {
          const end = new Date();
          const start = new Date();
          start.setDate(start.getDate() - 7);
          return [start, end];
        },
      },
      {
        text: "Last 30 Days",
        value: () => {
          const end = new Date();
          const start = new Date();
          start.setDate(start.getDate() - 30);
          return [start, end];
        },
      },
      {
        text: "Last 3 Months",
        value: () => {
          const end = new Date();
          const start = new Date();
          start.setMonth(start.getMonth() - 3);
          return [start, end];
        },
      },
      {
        text: "Last 6 Months",
        value: () => {
          const end = new Date();
          const start = new Date();
          start.setMonth(start.getMonth() - 6);
          return [start, end];
        },
      },
    ];

    // Disable future dates
    const disabledFutureDate = (date) => {
      return date > new Date();
    };

    const generateReport = async () => {
      try {
        // Validate date range selection
        if (!reportDateRange.value || reportDateRange.value.length !== 2) {
          ElMessage.warning("Please select a date range");
          return;
        }

        const [startDate, endDate] = reportDateRange.value;

        // Validate 6 months limit
        const start = new Date(startDate);
        const end = new Date(endDate);
        const daysDiff = Math.ceil((end - start) / (1000 * 60 * 60 * 24));

        if (daysDiff > 180) {
          ElMessage.warning("Date range cannot exceed 6 months");
          return;
        }

        reportGenerating.value = true;
        ElMessage.info("Generating report... Please wait.");

        const response = await API.post(
          "/api/admin/generate-report",
          {
            startDate,
            endDate,
          },
          {
            responseType: "blob",
            timeout: 120000, // 2 minutes timeout
          }
        );

        // Download CSV file
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute(
          "download",
          `job_monitor_report_${startDate}_to_${endDate}.csv`
        );
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        ElMessage.success("Report downloaded successfully!");
        showReportDialog.value = false;
      } catch (error) {
        console.error("Failed to generate report:", error);
        if (error.code === "ECONNABORTED") {
          ElMessage.error(
            "Report generation timeout. Please try a shorter date range."
          );
        } else {
          ElMessage.error("Failed to generate report: " + error.message);
        }
      } finally {
        reportGenerating.value = false;
      }
    };

    // Load data on component mount and setup auto-refresh
    onMounted(() => {
      loadDashboardData();
      startAutoRefresh();
    });

    // Cleanup on component unmount
    onUnmounted(() => {
      stopAutoRefresh();
    });

    return {
      // Data
      loading,
      lastUpdated,
      stats,
      recentActivities,
      systemAlerts,
      showReportDialog,
      reportDateRange,
      reportGenerating,
      dateShortcuts,
      disabledFutureDate,
      // Methods
      loadDashboardData,
      refreshDashboard,
      viewAllActivities,
      addNewStaff,
      scheduleTask,
      generateReport,
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

/* Dashboard content */
.dashboard-content {
  width: 100%;
}

/* Welcome section */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
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
  color: #2c3e50;
  margin: 0 0 0.5rem 0;
}

.welcome-subtitle {
  color: #7f8c8d;
  font-size: 1.125rem;
  margin: 0;
}

.last-updated {
  color: #95a5a6;
  font-size: 0.75rem;
  white-space: nowrap;
}

/* Statistics grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  border-radius: 1rem;
  border: none;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  padding: 1rem;
  border-radius: 0.75rem;
  color: white;
}

.stat-card-blue .stat-icon {
  background: linear-gradient(135deg, #2c3e50, #34495e);
}

.stat-card-green .stat-icon {
  background: linear-gradient(135deg, #27ae60, #2d7a39);
}

.stat-card-orange .stat-icon {
  background: linear-gradient(135deg, #d68910, #b7750a);
}

.stat-card-success .stat-icon {
  background: linear-gradient(135deg, #059669, #047857);
}

.stat-label {
  color: #7f8c8d;
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
}

/* Content grid */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 300px;
  gap: 1.5rem;
}

/* Card styles */
.el-card {
  border-radius: 1rem;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: 600;
  color: #2c3e50;
}

/* Activities card */
.activities-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  border-radius: 0.5rem;
  background-color: #f8f9fa;
  transition: background-color 0.3s;
}

.activity-item:hover {
  background-color: #e9ecef;
}

.activity-icon {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.125rem;
}

.activity-icon.success {
  background: linear-gradient(135deg, #059669, #047857);
}

.activity-icon.info {
  background: linear-gradient(135deg, #2c3e50, #34495e);
}

.activity-icon.warning {
  background: linear-gradient(135deg, #d68910, #b7750a);
}

.activity-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.activity-description {
  color: #7f8c8d;
  font-size: 0.875rem;
  margin-bottom: 0.25rem;
}

.activity-time {
  color: #95a5a6;
  font-size: 0.75rem;
}

/* Alerts card */
.alerts-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.alert-item {
  border-radius: 0.5rem;
}

/* Actions card */
.actions-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  width: 100%;
  padding: 0.5rem;
}

.action-button {
  width: 100%;
  height: 3rem;
  border-radius: 0.5rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

/* Responsive design */
@media (max-width: 1200px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}

/* 方案A: 统一状态标签颜色 - 专业暗沉系 */
:deep(.el-tag--success) {
  background-color: #27ae60 !important;
  border-color: #27ae60 !important;
  color: white !important;
}

:deep(.el-tag--warning) {
  background-color: #d68910 !important;
  border-color: #d68910 !important;
  color: white !important;
}

:deep(.el-tag--danger) {
  background-color: #c0392b !important;
  border-color: #c0392b !important;
  color: white !important;
}

:deep(.el-tag--info) {
  background-color: #2980b9 !important;
  border-color: #2980b9 !important;
  color: white !important;
}

:deep(.el-tag--primary) {
  background-color: #2c3e50 !important;
  border-color: #2c3e50 !important;
  color: white !important;
}

/* 按钮统一颜色 - 方案A */
:deep(.el-button--primary) {
  background-color: #2c3e50 !important;
  border-color: #2c3e50 !important;
}

:deep(.el-button--primary:hover) {
  background-color: #34495e !important;
  border-color: #34495e !important;
}

:deep(.el-button--success) {
  background-color: #27ae60 !important;
  border-color: #27ae60 !important;
}

:deep(.el-button--warning) {
  background-color: #d68910 !important;
  border-color: #d68910 !important;
}

:deep(.el-button--danger) {
  background-color: #c0392b !important;
  border-color: #c0392b !important;
}
</style>
