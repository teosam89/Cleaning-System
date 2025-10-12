<template>
  <JanitorLayout>
    <div class="task-list-container">
      <!-- Page Header -->
      <div class="page-header">
        <h1 class="page-title">My Tasks</h1>
        <p class="page-subtitle">Manage and track your cleaning tasks</p>
      </div>

      <!-- Filter Section -->
      <div class="filter-section">
        <el-card class="filter-card" shadow="never">
          <div class="filter-content">
            <div class="filter-group">
              <label class="filter-label">Task Status</label>
              <el-select
                v-model="selectedStatus"
                placeholder="Select Status"
                class="filter-input"
              >
                <el-option label="All Tasks" value="all" />
                <el-option label="Pending" value="pending" />
                <el-option label="In Progress" value="in_progress" />
                <el-option label="Completed" value="completed" />
                <el-option label="Overdue" value="overdue" />
              </el-select>
            </div>
          </div>
        </el-card>
      </div>

      <!-- Task Statistics -->
      <div class="task-stats">
        <div class="stat-item pending">
          <div class="stat-icon">
            <el-icon size="24"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ taskStats.pending }}</div>
            <div class="stat-label">Pending</div>
          </div>
        </div>
        <div class="stat-item progress">
          <div class="stat-icon">
            <el-icon size="24"><Loading /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ taskStats.inProgress }}</div>
            <div class="stat-label">In Progress</div>
          </div>
        </div>
        <div class="stat-item completed">
          <div class="stat-icon">
            <el-icon size="24"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ taskStats.completed }}</div>
            <div class="stat-label">Completed</div>
          </div>
        </div>
        <div class="stat-item overdue">
          <div class="stat-icon">
            <el-icon size="24"><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ taskStats.overdue }}</div>
            <div class="stat-label">Overdue</div>
          </div>
        </div>
      </div>

      <!-- Task List -->
      <div class="task-list">
        <div
          v-for="task in filteredTasks"
          :key="task.id"
          class="task-card"
          :class="getTaskCardClass(task)"
        >
          <div class="task-header">
            <div class="task-priority">
              <el-tag :type="getPriorityType(task.priority)" size="small">
                {{ getPriorityText(task.priority) }}
              </el-tag>
            </div>
            <div class="task-status">
              <el-tag :type="getStatusType(task.status)" size="small">
                {{ getStatusText(task.status) }}
              </el-tag>
            </div>
          </div>

          <div class="task-main">
            <h3 class="task-title">{{ task.title }}</h3>
            <div class="task-location">
              <el-icon><LocationInformation /></el-icon>
              <span>{{ task.location }}</span>
            </div>
            <div class="task-time">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(task.scheduledTime) }}</span>
            </div>
            <div class="task-duration">
              <el-icon><Timer /></el-icon>
              <span>Estimated {{ task.estimatedDuration }} minutes</span>
            </div>
          </div>

          <div class="task-footer">
            <div class="task-actions">
              <el-button
                size="small"
                @click="viewTaskDetail(task.id)"
                class="detail-btn"
              >
                <el-icon><View /></el-icon>
                View Details
              </el-button>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="filteredTasks.length === 0" class="empty-state">
          <el-empty description="No tasks found matching the selected status">
            <el-button type="primary" @click="selectedStatus = 'all'">
              <el-icon><Refresh /></el-icon>
              Show All Tasks
            </el-button>
          </el-empty>
        </div>
      </div>

      <!-- Loading State -->
      <div
        v-if="loading"
        class="loading-state"
        v-loading="loading"
        element-loading-text="Loading task list..."
      >
        <div style="height: 200px"></div>
      </div>
    </div>
  </JanitorLayout>
</template>

<script>
import { ref, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { AuthUtils } from "@/utils/auth";
import { API } from "@/utils/request";
import JanitorLayout from "@/components/JanitorLayout.vue";
import {
  Clock,
  Loading,
  CircleCheck,
  Warning,
  LocationInformation,
  Timer,
  View,
  Refresh,
} from "@element-plus/icons-vue";

export default {
  name: "TaskList",
  components: {
    JanitorLayout,
    Clock,
    Loading,
    CircleCheck,
    Warning,
    LocationInformation,
    Timer,
    View,
    Refresh,
  },
  setup() {
    const router = useRouter();
    const route = useRoute();

    // Reactive data
    const loading = ref(false);
    const selectedStatus = ref("all");
    const tasksData = ref([]);
    const userInfo = ref({ userId: null, fullName: "Janitor" });
    const lastUpdated = ref("");

    // Initialize user info from JWT token
    const initUserInfo = () => {
      if (!AuthUtils.isAuthenticated()) {
        console.error("User not authenticated, redirecting to login");
        ElMessage.error("Session expired, please login again");
        router.push("/");
        return false;
      }

      const tokenUserInfo = AuthUtils.getUserInfo();
      const userId = AuthUtils.getUserId();
      const userRole = AuthUtils.getUserRole();

      if (userId && tokenUserInfo) {
        userInfo.value = {
          fullName: tokenUserInfo.fullName || "Janitor",
          userId: userId,
          role: userRole || "janitor",
        };
        return true;
      } else if (userId) {
        userInfo.value = {
          fullName: "Janitor",
          userId: userId,
          role: userRole || "janitor",
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

    // Task statistics
    const taskStats = computed(() => {
      const stats = {
        pending: 0,
        inProgress: 0,
        completed: 0,
        overdue: 0,
      };

      tasksData.value.forEach((task) => {
        if (task.status === "pending") stats.pending++;
        else if (task.status === "in_progress") stats.inProgress++;
        else if (task.status === "completed") stats.completed++;
        else if (task.status === "overdue") stats.overdue++;
      });

      return stats;
    });

    // Filtered tasks
    const filteredTasks = computed(() => {
      let filtered = tasksData.value;

      // Filter by status
      if (selectedStatus.value !== "all") {
        filtered = filtered.filter(
          (task) => task.status === selectedStatus.value
        );
      }

      // Sort by priority and time
      return filtered.sort((a, b) => {
        const priorityOrder = { urgent: 0, high: 1, normal: 2, low: 3 };
        const priorityDiff =
          priorityOrder[a.priority] - priorityOrder[b.priority];
        if (priorityDiff !== 0) return priorityDiff;

        return new Date(a.scheduledTime) - new Date(b.scheduledTime);
      });
    });

    // API Functions
    const loadJanitorTasks = async () => {
      // Support for admin/supervisor viewing other janitors' tasks via URL parameter
      const targetJanitorId = route.query?.janitorId || userInfo.value.userId;

      if (!targetJanitorId) {
        console.warn("No user ID found for loading tasks");
        return;
      }

      // Check if current user has permission to view other janitors' tasks
      const isAdmin = userInfo.value.role === "admin";
      const isSupervisor = userInfo.value.role === "supervisor";
      const viewingOwnTasks = targetJanitorId == userInfo.value.userId;

      if (!viewingOwnTasks && !isAdmin && !isSupervisor) {
        console.error("Unauthorized to view other janitor's tasks");
        ElMessage.error("You don't have permission to view these tasks");
        return;
      }

      try {
        loading.value = true;
        console.log(
          "Loading tasks for user:",
          targetJanitorId,
          "| Role:",
          userInfo.value.role,
          viewingOwnTasks ? "(own tasks)" : "(viewing as admin/supervisor)"
        );

        let apiUrl = `/api/tasks?janitorId=${targetJanitorId}`;
        console.log("API URL being called:", apiUrl);

        // Add status filter if not "all"
        if (selectedStatus.value !== "all") {
          apiUrl += `&status=${selectedStatus.value}`;
        }

        const response = await API.get(apiUrl);
        console.log("Tasks API response:", response.data);

        if (response.data && Array.isArray(response.data)) {
          // Transform API data to match frontend format
          tasksData.value = response.data.map((task) => ({
            id: task.taskId,
            title: task.title,
            location: task.location,
            status: task.status,
            priority: task.priority || "normal",
            scheduledTime: task.scheduledTime,
            estimatedDuration: task.estimatedDuration || 60,
            progress: task.progressPercentage || 0,
            description: task.description,
            tools: task.toolsRequired ? task.toolsRequired.split(",") : [],
            assignedBy: task.assignedByName || task.assignedBy || "Manager",
            createdAt: task.createdAt,
            startedAt: task.startedAt,
            completedAt: task.completedAt,
            notes: task.notes,
          }));
          console.log("Transformed tasks data:", tasksData.value);
        } else {
          tasksData.value = [];
        }

        lastUpdated.value = new Date().toLocaleTimeString();
        console.log("Tasks loaded successfully");
      } catch (error) {
        console.warn("Failed to load tasks:", error);
        ElMessage.warning(
          "Unable to load task data. Please check your connection."
        );
        // Keep empty array as fallback
        tasksData.value = [];
      } finally {
        loading.value = false;
      }
    };

    // Filter tasks - now triggers API reload

    const viewTaskDetail = (taskId) => {
      router.push(`/janitor/tasks/${taskId}`);
    };

    // Utility functions
    const getTaskCardClass = (task) => {
      return `task-${task.status} priority-${task.priority}`;
    };

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

    const getPriorityText = (priority) => {
      const texts = {
        low: "Low Priority",
        normal: "Normal",
        high: "High Priority",
        urgent: "Urgent",
      };
      return texts[priority] || priority;
    };

    const formatDateTime = (dateTime) => {
      return new Date(dateTime).toLocaleString("en-GB", {
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
      });
    };

    // Lifecycle
    onMounted(async () => {
      try {
        loading.value = true;

        // Initialize user info
        const initSuccess = initUserInfo();
        if (!initSuccess) {
          loading.value = false;
          return;
        }

        // Load tasks for the authenticated user
        await loadJanitorTasks();

        console.log("Task list initialized successfully with API integration");
      } catch (error) {
        console.error("Error in onMounted:", error);
        ElMessage.error(
          "Failed to initialize task list. Please try refreshing the page."
        );
      } finally {
        loading.value = false;
      }
    });

    return {
      loading,
      selectedStatus,
      tasksData,
      taskStats,
      filteredTasks,
      userInfo,
      lastUpdated,
      loadJanitorTasks,
      viewTaskDetail,
      getTaskCardClass,
      getStatusType,
      getStatusText,
      getPriorityType,
      getPriorityText,
      formatDateTime,
    };
  },
};
</script>

<style scoped>
/* Container */
.task-list-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Page Header */
.page-header {
  margin-bottom: 1.5rem;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 1rem;
}

/* Filter Section */
.filter-section {
  margin-bottom: 1.5rem;
}

.filter-card {
  border-radius: 1rem;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.filter-content {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  min-width: 200px;
}

.filter-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.filter-input {
  width: 100%;
}

/* Task Statistics */
.task-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-item {
  background: white;
  border-radius: 1rem;
  padding: 1.25rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-left: 4px solid transparent;
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.stat-item.pending {
  border-left-color: #f59e0b;
}

.stat-item.progress {
  border-left-color: #3b82f6;
}

.stat-item.completed {
  border-left-color: #10b981;
}

.stat-item.overdue {
  border-left-color: #ef4444;
}

.stat-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-item.pending .stat-icon {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.stat-item.progress .stat-icon {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.stat-item.completed .stat-icon {
  background: linear-gradient(135deg, #10b981, #059669);
}

.stat-item.overdue .stat-icon {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.stat-number {
  font-size: 1.75rem;
  font-weight: 800;
  color: #1f2937;
  line-height: 1;
}

.stat-label {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
}

/* Task List */
.task-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.task-card {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(229, 231, 235, 0.5);
  border-left: 4px solid transparent;
  transition: all 0.3s ease;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border-color: rgba(16, 185, 129, 0.2);
}

.task-card.task-pending {
  border-left-color: #f59e0b;
}

.task-card.task-in_progress {
  border-left-color: #3b82f6;
}

.task-card.task-completed {
  border-left-color: #10b981;
}

.task-card.task-overdue {
  border-left-color: #ef4444;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.task-title {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.75rem 0;
}

.task-main > div {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  color: #6b7280;
  font-size: 0.875rem;
}

.task-footer {
  margin-top: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.task-actions {
  display: flex;
  gap: 0.75rem;
  flex-shrink: 0;
}

.detail-btn {
  border-radius: 0.5rem;
  color: #6b7280;
  border-color: #d1d5db;
}

.detail-btn:hover {
  color: #10b981;
  border-color: #10b981;
}

.action-btn {
  border-radius: 0.5rem;
  font-weight: 500;
}

.action-btn.el-button--primary {
  background: linear-gradient(135deg, #10b981, #059669);
  border-color: #10b981;
}

.action-btn.el-button--primary:hover {
  background: linear-gradient(135deg, #059669, #047857);
  border-color: #059669;
}

.action-btn.el-button--success {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  border-color: #22c55e;
}

.action-btn.el-button--success:hover {
  background: linear-gradient(135deg, #16a34a, #15803d);
  border-color: #16a34a;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 3rem 1.5rem;
  background: white;
  border-radius: 1rem;
  border: 2px dashed #d1d5db;
}

.empty-state .el-button {
  background: linear-gradient(135deg, #10b981, #059669);
  border-color: #10b981;
  border-radius: 0.75rem;
  font-weight: 600;
  padding: 0.75rem 2rem;
}

.empty-state .el-button:hover {
  background: linear-gradient(135deg, #059669, #047857);
  border-color: #059669;
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 3rem;
}

/* Responsive Design */
@media (max-width: 768px) {
  .filter-content {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-group {
    min-width: unset;
  }

  .task-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .task-footer {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }

  .task-actions {
    justify-content: stretch;
  }

  .task-actions .el-button {
    flex: 1;
  }
}

@media (max-width: 480px) {
  .task-stats {
    grid-template-columns: 1fr;
  }

  .page-title {
    font-size: 1.5rem;
  }
}
</style>
