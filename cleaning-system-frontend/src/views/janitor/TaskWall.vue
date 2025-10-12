<template>
  <JanitorLayout>
    <div class="task-wall-container">
      <!-- Page Header -->
      <div class="page-header">
        <h1 class="page-title">Task Wall</h1>
        <p class="page-subtitle">
          Available public tasks for all janitors to claim
        </p>
      </div>

      <!-- Filter Section -->
      <div class="filter-section">
        <el-card class="filter-card" shadow="never">
          <div class="filter-content">
            <div class="filter-group">
              <label class="filter-label">Search Tasks</label>
              <el-input
                v-model="searchQuery"
                placeholder="Search by title or description"
                @change="loadPublicTasks"
                class="filter-input"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
            <div class="filter-group">
              <label class="filter-label">Priority</label>
              <el-select
                v-model="selectedPriority"
                placeholder="All Priorities"
                @change="loadPublicTasks"
                class="filter-input"
                clearable
              >
                <el-option label="🔴 Urgent" value="urgent" />
                <el-option label="🟠 High" value="high" />
                <el-option label="🟡 Normal" value="normal" />
                <el-option label="🟢 Low" value="low" />
              </el-select>
            </div>
            <div class="filter-group">
              <label class="filter-label">Location</label>
              <el-input
                v-model="selectedLocation"
                placeholder="Filter by location"
                @change="loadPublicTasks"
                class="filter-input"
                clearable
              >
                <template #prefix>
                  <el-icon><LocationInformation /></el-icon>
                </template>
              </el-input>
            </div>
          </div>
        </el-card>
      </div>

      <!-- Public Tasks List -->
      <div class="public-tasks-section">
        <el-card class="tasks-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><Postcard /></el-icon>
                <span>Public Tasks ({{ publicTasks.length }})</span>
              </div>
              <div class="header-actions">
                <el-tag type="info" size="small"
                  >Last updated: {{ lastUpdated }}</el-tag
                >
              </div>
            </div>
          </template>

          <!-- Loading State -->
          <div v-if="loading" class="loading-state">
            <el-skeleton :rows="5" animated />
          </div>

          <!-- Empty State -->
          <div v-else-if="publicTasks.length === 0" class="empty-state">
            <el-empty
              description="No public tasks available. Tasks will auto-refresh every 30 seconds."
            />
          </div>

          <!-- Tasks List -->
          <div v-else class="tasks-list">
            <div
              v-for="task in publicTasks"
              :key="task.id"
              class="task-item"
              :class="getTaskItemClass(task)"
            >
              <div class="task-header">
                <div class="task-priority">
                  <el-tag :type="getPriorityType(task.priority)" size="small">
                    {{ getPriorityText(task.priority) }}
                  </el-tag>
                </div>
                <div class="task-status">
                  <el-tag v-if="task.assignedTo" type="success" size="small">
                    <el-icon><User /></el-icon>
                    Claimed
                  </el-tag>
                  <el-tag v-else type="warning" size="small">
                    <el-icon><Clock /></el-icon>
                    Available
                  </el-tag>
                </div>
              </div>

              <div class="task-main">
                <h3 class="task-title">{{ task.title }}</h3>
                <p class="task-description">{{ task.description }}</p>
                <div class="task-details">
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
                    <span>{{ task.estimatedDuration || 60 }} minutes</span>
                  </div>
                </div>
              </div>

              <div class="task-footer">
                <div class="task-actions">
                  <!-- Claim Button for Available Tasks -->
                  <el-button
                    v-if="!task.assignedTo"
                    type="primary"
                    size="small"
                    @click="claimTask(task)"
                    :loading="claimingTaskId === task.id"
                    class="claim-btn"
                  >
                    <el-icon><Plus /></el-icon>
                    Claim Task
                  </el-button>

                  <!-- Already Claimed by Current User -->
                  <el-button
                    v-else-if="task.assignedTo === userInfo.userId"
                    type="success"
                    size="small"
                    @click="viewTaskDetail(task.id)"
                    class="my-task-btn"
                  >
                    <el-icon><View /></el-icon>
                    My Task
                  </el-button>

                  <!-- Claimed by Another Janitor -->
                  <el-tag v-else type="info" size="small" class="claimed-tag">
                    <el-icon><Lock /></el-icon>
                    Claimed by other janitor
                  </el-tag>

                  <!-- View Details Button -->
                  <el-button
                    size="small"
                    @click="viewTaskDetail(task.id)"
                    class="detail-btn"
                  >
                    <el-icon><View /></el-icon>
                    Details
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- Auto-refresh Notice -->
      <div class="auto-refresh-notice">
        <el-alert
          title="Auto-refresh enabled"
          description="Task wall updates automatically every 30 seconds"
          type="info"
          :closable="false"
          show-icon
        />
      </div>
    </div>
  </JanitorLayout>
</template>

<script>
import { ref, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { AuthUtils } from "@/utils/auth";
import { API } from "@/utils/request";
import JanitorLayout from "@/components/JanitorLayout.vue";
import {
  Search,
  LocationInformation,
  Postcard,
  User,
  Clock,
  Timer,
  Plus,
  View,
  Lock,
} from "@element-plus/icons-vue";

export default {
  name: "TaskWall",
  components: {
    JanitorLayout,
    Search,
    LocationInformation,
    Postcard,
    User,
    Clock,
    Timer,
    Plus,
    View,
    Lock,
  },
  setup() {
    const router = useRouter();

    // Reactive data
    const loading = ref(false);
    const publicTasks = ref([]);
    const searchQuery = ref("");
    const selectedPriority = ref("");
    const selectedLocation = ref("");
    const claimingTaskId = ref(null);
    const userInfo = ref({
      userId: null,
      fullName: "Janitor",
      role: "janitor",
    });
    const lastUpdated = ref("");
    const refreshInterval = ref(null);

    // Initialize user info
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

    // Load public tasks
    const loadPublicTasks = async () => {
      try {
        loading.value = true;
        console.log("Loading public tasks...");

        let apiUrl = "/api/tasks/public";
        const params = new URLSearchParams();

        if (searchQuery.value.trim()) {
          params.append("search", searchQuery.value.trim());
        }
        if (selectedPriority.value) {
          params.append("priority", selectedPriority.value);
        }
        if (selectedLocation.value.trim()) {
          params.append("location", selectedLocation.value.trim());
        }

        if (params.toString()) {
          apiUrl += `?${params.toString()}`;
        }

        console.log("Public tasks API URL:", apiUrl);

        const response = await API.get(apiUrl);
        console.log("Public tasks API response:", response.data);

        if (response.data && Array.isArray(response.data)) {
          publicTasks.value = response.data.map((task) => ({
            id: task.taskId,
            title: task.title,
            description: task.description,
            location: task.location,
            priority: task.priority || "normal",
            scheduledTime: task.scheduledTime,
            dueDate: task.dueDate,
            estimatedDuration: task.estimatedDuration || 60,
            assignedTo: task.assignedTo,
            assignedBy: task.assignedByName || task.assignedBy || "Manager",
            status: task.status,
            createdAt: task.createdAt,
            instructions: task.instructions,
            toolsRequired: task.toolsRequired,
          }));

          console.log("Transformed public tasks:", publicTasks.value);
        } else {
          publicTasks.value = [];
        }

        lastUpdated.value = new Date().toLocaleTimeString();
        console.log("Public tasks loaded successfully");
      } catch (error) {
        console.error("Failed to load public tasks:", error);
        ElMessage.error("Failed to load public tasks. Please try again.");
        publicTasks.value = [];
      } finally {
        loading.value = false;
      }
    };

    // Claim a task
    const claimTask = async (task) => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to claim task "${task.title}"?`,
          "Claim Task",
          {
            confirmButtonText: "Claim",
            cancelButtonText: "Cancel",
            type: "info",
          }
        );

        claimingTaskId.value = task.id;

        const response = await API.post(`/api/tasks/${task.id}/claim`, null, {
          params: { janitorId: userInfo.value.userId },
        });

        if (response.data && response.data.success) {
          ElMessage.success("Task claimed successfully!");
          await loadPublicTasks(); // Refresh the task list
        } else {
          ElMessage.error(response.data?.message || "Failed to claim task");
        }
      } catch (error) {
        if (error.name !== "ElMessageBoxClosedByCancel") {
          console.error("Claim task error:", error);
          const errorMessage =
            error.response?.data?.message ||
            error.message ||
            "Failed to claim task. Please try again.";
          ElMessage.error(errorMessage);
        }
      } finally {
        claimingTaskId.value = null;
      }
    };

    // View task detail
    const viewTaskDetail = (taskId) => {
      router.push(`/janitor/tasks/${taskId}`);
    };

    // Get task item class
    const getTaskItemClass = (task) => {
      const classes = [`priority-${task.priority}`];
      if (task.assignedTo) {
        classes.push("task-claimed");
        if (task.assignedTo === userInfo.value.userId) {
          classes.push("my-task");
        }
      } else {
        classes.push("task-available");
      }
      return classes.join(" ");
    };

    // Utility functions
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
        low: "🟢 Low",
        normal: "🟡 Normal",
        high: "🟠 High",
        urgent: "🔴 Urgent",
      };
      return texts[priority] || priority;
    };

    const formatDateTime = (dateTime) => {
      if (!dateTime) return "Not scheduled";
      return new Date(dateTime).toLocaleString("en-GB", {
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
      });
    };

    // Setup auto-refresh
    const setupAutoRefresh = () => {
      refreshInterval.value = setInterval(() => {
        loadPublicTasks();
      }, 30000); // Refresh every 30 seconds
    };

    // Lifecycle
    onMounted(async () => {
      try {
        const initSuccess = initUserInfo();
        if (!initSuccess) {
          return;
        }

        await loadPublicTasks();
        setupAutoRefresh();

        console.log("Task wall initialized successfully");
      } catch (error) {
        console.error("Error in onMounted:", error);
        ElMessage.error(
          "Failed to initialize task wall. Please try refreshing the page."
        );
      }
    });

    onUnmounted(() => {
      if (refreshInterval.value) {
        clearInterval(refreshInterval.value);
      }
    });

    return {
      loading,
      publicTasks,
      searchQuery,
      selectedPriority,
      selectedLocation,
      claimingTaskId,
      userInfo,
      lastUpdated,
      loadPublicTasks,
      claimTask,
      viewTaskDetail,
      getTaskItemClass,
      getPriorityType,
      getPriorityText,
      formatDateTime,
    };
  },
};
</script>

<style scoped>
/* Container */
.task-wall-container {
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

/* Tasks Section */
.public-tasks-section {
  margin-bottom: 2rem;
}

.tasks-card {
  border-radius: 1rem;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #374151;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
}

/* Loading and Empty States */
.loading-state {
  padding: 2rem;
}

.empty-state {
  text-align: center;
  padding: 3rem 1.5rem;
}

/* Tasks List */
.tasks-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.task-item {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 1rem;
  padding: 1.5rem;
  border-left: 4px solid transparent;
  transition: all 0.3s ease;
}

.task-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border-color: rgba(16, 185, 129, 0.2);
}

.task-item.task-available {
  border-left-color: #3b82f6;
  background: linear-gradient(
    135deg,
    rgba(59, 130, 246, 0.05),
    rgba(147, 197, 253, 0.05)
  );
}

.task-item.task-claimed {
  border-left-color: #ef4444;
  background: rgba(249, 250, 251, 0.8);
}

.task-item.my-task {
  border-left-color: #10b981;
  background: linear-gradient(
    135deg,
    rgba(16, 185, 129, 0.05),
    rgba(110, 231, 183, 0.05)
  );
}

.task-item.priority-urgent {
  border-left-color: #ef4444;
}

.task-item.priority-high {
  border-left-color: #f59e0b;
}

.task-item.priority-normal {
  border-left-color: #3b82f6;
}

.task-item.priority-low {
  border-left-color: #10b981;
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
  margin: 0 0 0.5rem 0;
}

.task-description {
  color: #6b7280;
  margin: 0 0 1rem 0;
  line-height: 1.5;
}

.task-details {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
}

.task-details > div {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6b7280;
  font-size: 0.875rem;
}

.task-footer {
  display: flex;
  justify-content: flex-end;
}

.task-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.claim-btn {
  background: linear-gradient(135deg, #10b981, #059669);
  border-color: #10b981;
  border-radius: 0.5rem;
  font-weight: 500;
}

.claim-btn:hover {
  background: linear-gradient(135deg, #059669, #047857);
  border-color: #059669;
}

.my-task-btn {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  border-color: #22c55e;
  border-radius: 0.5rem;
  font-weight: 500;
}

.my-task-btn:hover {
  background: linear-gradient(135deg, #16a34a, #15803d);
  border-color: #16a34a;
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

.claimed-tag {
  opacity: 0.8;
}

/* Auto-refresh Notice */
.auto-refresh-notice {
  margin-top: 1rem;
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

  .task-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .task-details {
    flex-direction: column;
    gap: 0.5rem;
  }

  .task-actions {
    flex-direction: column;
    width: 100%;
  }

  .task-actions .el-button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }
}
</style>
