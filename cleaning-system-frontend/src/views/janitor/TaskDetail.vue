<template>
  <JanitorLayout>
    <div class="task-detail-container">
      <!-- Back Navigation -->
      <div class="nav-header">
        <el-button @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          Back to Task List
        </el-button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- Task Detail Content -->
      <div v-else-if="taskDetail" class="task-detail-content">
        <!-- Task Header Card -->
        <el-card class="task-header-card" shadow="never">
          <div class="task-header">
            <div class="task-title-section">
              <h1 class="task-title">{{ taskDetail.title }}</h1>
              <div class="task-meta">
                <el-tag :type="getStatusType(taskDetail.status)" size="large">
                  {{ getStatusText(taskDetail.status) }}
                </el-tag>
                <el-tag
                  :type="getPriorityType(taskDetail.priority)"
                  size="large"
                >
                  {{ getPriorityText(taskDetail.priority) }}
                </el-tag>
              </div>
            </div>
            <div class="task-actions">
              <el-button
                v-if="taskDetail.status === 'pending'"
                type="primary"
                size="large"
                @click="startTask"
                :loading="actionLoading"
                class="action-btn"
              >
                <el-icon><VideoPlay /></el-icon>
                Start Task
              </el-button>
              <el-button
                v-else-if="taskDetail.status === 'in_progress'"
                type="success"
                size="large"
                @click="openCompletionDialog"
                :loading="actionLoading"
                class="action-btn"
              >
                <el-icon><Check /></el-icon>
                Complete Task
              </el-button>
              <div
                v-else-if="taskDetail.status === 'completed'"
                class="completed-status"
              >
                <el-tag type="success" size="large" class="completed-tag">
                  <el-icon><CircleCheck /></el-icon>
                  Task Completed
                </el-tag>
                <el-button
                  v-if="showUndoOption"
                  size="small"
                  @click="undoCompletion"
                  :loading="undoLoading"
                  class="undo-btn"
                >
                  <el-icon><RefreshLeft /></el-icon>
                  Undo Completion
                </el-button>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Task Information Grid -->
        <div class="info-grid">
          <!-- Basic Information -->
          <el-card class="info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><Document /></el-icon>
                <span>Basic Information</span>
              </div>
            </template>
            <div class="info-content">
              <div class="info-item">
                <div class="info-label">Task Description</div>
                <div class="info-value">{{ taskDetail.description }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">Cleaning Location</div>
                <div class="info-value">
                  <el-icon><LocationInformation /></el-icon>
                  {{ taskDetail.location }}
                </div>
              </div>
              <div class="info-item">
                <div class="info-label">Scheduled Time</div>
                <div class="info-value">
                  <el-icon><Clock /></el-icon>
                  {{ formatDateTime(taskDetail.scheduledTime) }}
                </div>
              </div>
              <div class="info-item">
                <div class="info-label">Estimated Duration</div>
                <div class="info-value">
                  <el-icon><Timer /></el-icon>
                  {{ taskDetail.estimatedDuration }} minutes
                </div>
              </div>
              <div class="info-item">
                <div class="info-label">Assigned By</div>
                <div class="info-value">
                  {{
                    taskDetail.assignedByName ||
                    taskDetail.assignedBy ||
                    "Unknown"
                  }}
                </div>
              </div>
            </div>
          </el-card>

          <!-- Tools and Requirements -->
          <el-card class="info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><Tools /></el-icon>
                <span>Tool Requirements</span>
              </div>
            </template>
            <div class="tools-content">
              <div class="tools-list">
                <div
                  v-for="tool in taskDetail.tools"
                  :key="tool"
                  class="tool-item"
                >
                  <el-icon><Check /></el-icon>
                  <span>{{ tool }}</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- Instructions -->
          <el-card class="info-card full-width" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><Reading /></el-icon>
                <span>Instructions</span>
              </div>
            </template>
            <div class="instructions-content">
              <p>
                {{
                  taskDetail.instructions ||
                  "Follow standard cleaning procedures to ensure quality standards are met."
                }}
              </p>
            </div>
          </el-card>
        </div>

        <!-- Task Timeline (if available) -->
        <el-card
          v-if="taskDetail.timeline"
          class="timeline-card"
          shadow="never"
        >
          <template #header>
            <div class="card-header">
              <el-icon><Clock /></el-icon>
              <span>Task Timeline</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="event in taskDetail.timeline"
              :key="event.id"
              :timestamp="formatDateTime(event.timestamp)"
              :type="getTimelineType(event.type)"
            >
              {{ event.description }}
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <!-- Task Completion Gallery (only show when task is completed) -->
        <div v-if="taskDetail.status === 'completed' && existingImages.length > 0" class="completion-gallery-section">
          <div class="gallery-header">
            <h4 class="gallery-title">
              <el-icon><Camera /></el-icon>
              Task Completion Photos
            </h4>
            <el-button
              @click="viewAllPhotos"
              type="primary"
              size="small"
              class="view-all-btn"
            >
              <el-icon><View /></el-icon>
              View All Photos
            </el-button>
          </div>
          <TaskCompletionGallery :images="existingImages" />
        </div>
      </div>

      <!-- Error State -->
      <div v-else class="error-state">
        <el-result
          icon="error"
          title="Task Not Found"
          sub-title="Please check if the task ID is correct"
        >
          <template #extra>
            <el-button type="primary" @click="goBack"
              >Back to Task List</el-button
            >
          </template>
        </el-result>
      </div>
    </div>

    <!-- Task Completion Dialog -->
    <el-dialog
      v-model="completionDialogVisible"
      title="Complete Task"
      width="60%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="completion-dialog">
        <!-- Completion Header -->
        <div class="completion-header">
          <h3 class="completion-title">
            <el-icon><Check /></el-icon>
            Complete "{{ taskDetail?.title }}"
          </h3>
          <p class="completion-subtitle">
            Please review your work and optionally add photos and notes before
            marking this task as completed.
          </p>
        </div>

        <!-- Task Summary -->
        <el-card class="task-summary-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>Task Summary</span>
            </div>
          </template>
          <div class="task-summary">
            <div class="summary-item">
              <span class="summary-label">Location:</span>
              <span class="summary-value">{{ taskDetail?.location }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Duration:</span>
              <span class="summary-value"
                >{{ taskDetail?.estimatedDuration }} minutes</span
              >
            </div>
            <div class="summary-item">
              <span class="summary-label">Started:</span>
              <span class="summary-value">{{
                formatDateTime(taskDetail?.startedAt)
              }}</span>
            </div>
          </div>
        </el-card>

        <!-- Photo Upload Section -->
        <PhotoUpload
          ref="photoUploadRef"
          :task-id="taskDetail?.id"
          :max-files="5"
          :existing-images="existingImages"
          @photos-changed="handlePhotosChanged"
          @upload-success="handleUploadSuccess"
          @image-deleted="handleImageDeleted"
        />

        <!-- Completion Notes -->
        <el-card class="completion-notes-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><EditPen /></el-icon>
              <span>Completion Notes</span>
            </div>
          </template>
          <div class="completion-notes">
            <el-input
              v-model="completionNotes"
              type="textarea"
              :rows="4"
              placeholder="Add any notes about the completed work, issues encountered, or additional observations..."
              class="notes-textarea"
            />
          </div>
        </el-card>
      </div>

      <template #footer>
        <div class="completion-dialog-footer">
          <el-button
            @click="completionDialogVisible = false"
            class="cancel-btn"
          >
            Cancel
          </el-button>
          <el-button
            type="success"
            @click="completeTask"
            :loading="actionLoading"
            class="complete-btn"
          >
            <el-icon><Check /></el-icon>
            {{ actionLoading ? "Completing..." : "Mark as Completed" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </JanitorLayout>
</template>

<script>
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { AuthUtils } from "@/utils/auth";
import { API } from "@/utils/request";
import JanitorLayout from "@/components/JanitorLayout.vue";
import {
  ArrowLeft,
  VideoPlay,
  Check,
  CircleCheck,
  Document,
  LocationInformation,
  Clock,
  Timer,
  Tools,
  Reading,
  RefreshLeft,
  Camera,
  View,
} from "@element-plus/icons-vue";
import PhotoUpload from "@/components/PhotoUpload.vue";
import TaskCompletionGallery from "@/components/TaskCompletionGallery.vue";

export default {
  name: "TaskDetail",
  components: {
    JanitorLayout,
    PhotoUpload,
    TaskCompletionGallery,
    ArrowLeft,
    VideoPlay,
    Check,
    CircleCheck,
    Document,
    LocationInformation,
    Clock,
    Timer,
    Tools,
    Reading,
    RefreshLeft,
    Camera,
    View,
  },
  setup() {
    const router = useRouter();
    const route = useRoute();

    // Reactive data
    const loading = ref(true);
    const actionLoading = ref(false);
    const taskDetail = ref(null);
    const existingImages = ref([]);
    const userInfo = ref({ userId: null, fullName: "Janitor" });
    const completionDialogVisible = ref(false);
    const undoLoading = ref(false);
    const showUndoOption = ref(false);
    const completionPhotos = ref([]);
    const completionNotes = ref("");
    const photoUploadRef = ref(null);

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

    // Methods
    const loadTaskDetail = async () => {
      loading.value = true;
      try {
        const taskId = parseInt(route.params.id);
        if (!taskId) {
          console.error("Invalid task ID:", route.params.id);
          taskDetail.value = null;
          return;
        }

        console.log("Loading task detail for ID:", taskId);

        const response = await API.get(`/api/tasks/${taskId}`);
        console.log("Task detail API response:", response.data);

        if (response.data) {
          // Transform API data to match frontend format
          const task = response.data;
          taskDetail.value = {
            id: task.taskId,
            title: task.title,
            location: task.location,
            status: task.status,
            priority: task.priority || "normal",
            scheduledTime: task.scheduledTime,
            estimatedDuration: task.estimatedDuration || 60,
            progress: task.progressPercentage || 0,
            description: task.description,
            instructions:
              task.instructions ||
              "Follow standard cleaning procedures to ensure quality standards are met.",
            tools: task.toolsRequired ? task.toolsRequired.split(",") : [],
            assignedBy: task.assignedBy || "Manager",
            assignedByName: task.assignedByName || "Manager",
            createdAt: task.createdAt,
            startedAt: task.startedAt,
            completedAt: task.completedAt,
            notes: task.notes || "",
            // Create a simple timeline from available data
            timeline: [
              {
                id: 1,
                type: "created",
                description: "Task created",
                timestamp: task.createdAt || new Date().toISOString(),
              },
              task.assignedTo
                ? {
                    id: 2,
                    type: "assigned",
                    description: "Task assigned to you",
                    timestamp: task.createdAt || new Date().toISOString(),
                  }
                : null,
              task.startedAt
                ? {
                    id: 3,
                    type: "started",
                    description: "Task started",
                    timestamp: task.startedAt,
                  }
                : null,
              task.completedAt
                ? {
                    id: 4,
                    type: "completed",
                    description: "Task completed",
                    timestamp: task.completedAt,
                  }
                : null,
            ].filter(Boolean),
          };


          // Extract images from the response
          if (task.images && Array.isArray(task.images)) {
            existingImages.value = task.images;
          } else {
            existingImages.value = [];
          }

          // If task is completed and no images in response, try to fetch task images separately
          if (taskDetail.value.status === 'completed' && existingImages.value.length === 0) {
            try {
              const imagesResponse = await API.get(`/api/tasks/${taskId}/images`);
              if (imagesResponse.data && imagesResponse.data.success && imagesResponse.data.images) {
                existingImages.value = imagesResponse.data.images;
                console.log("Loaded task completion images:", existingImages.value);
              }
            } catch (imageError) {
              console.warn("Failed to load task images:", imageError);
              // Don't show error to user, just continue without images
            }
          }

          console.log("Task detail loaded successfully:", taskDetail.value);
        } else {
          taskDetail.value = null;
        }
      } catch (error) {
        console.error("Failed to load task details:", error);
        ElMessage.error("Failed to load task details. Please try again.");
        taskDetail.value = null;
      } finally {
        loading.value = false;
      }
    };

    const goBack = () => {
      router.push("/janitor/tasks");
    };

    const viewAllPhotos = () => {
      router.push(`/janitor/tasks/${taskDetail.value.id}/photos`);
    };

    const startTask = async () => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to start task "${taskDetail.value.title}"?`,
          "Start Task",
          {
            confirmButtonText: "Start",
            cancelButtonText: "Cancel",
            type: "info",
          }
        );

        actionLoading.value = true;

        // Call API to start task
        const response = await API.put(
          `/api/tasks/${taskDetail.value.id}/start`
        );

        if (response.data) {
          // Update local task data
          taskDetail.value.status = "in_progress";
          taskDetail.value.progress = 0;
          taskDetail.value.startedAt = new Date().toISOString();

          // Add timeline event
          taskDetail.value.timeline.push({
            id: taskDetail.value.timeline.length + 1,
            type: "started",
            description: "Task started",
            timestamp: new Date().toISOString(),
          });

          ElMessage.success("Task started successfully");
        }
      } catch (error) {
        if (error.name !== "ElMessageBoxClosedByCancel") {
          console.error("Start task error:", error);
          ElMessage.error("Failed to start task. Please try again.");
        }
      } finally {
        actionLoading.value = false;
      }
    };

    const openCompletionDialog = () => {
      completionDialogVisible.value = true;
      completionNotes.value = taskDetail.value.notes || "";
    };

    const completeTask = async () => {
      try {
        actionLoading.value = true;

        // First, upload any ready-to-upload images
        let uploadedImages = [];
        if (photoUploadRef.value && photoUploadRef.value.getReadyImages) {
          const readyImages = photoUploadRef.value.getReadyImages();

          if (readyImages.length > 0) {
            console.log("Uploading", readyImages.length, "prepared images...");

            // Create FormData for image upload
            const formData = new FormData();
            readyImages.forEach((image) => {
              if (image.file) {
                formData.append("files", image.file);
              }
            });

            // Upload images to server
            try {
              const token = AuthUtils.getToken();
              const uploadResponse = await fetch(
                `/api/upload/task-completion/${taskDetail.value.id}`,
                {
                  method: "POST",
                  headers: {
                    Authorization: `Bearer ${token}`,
                  },
                  body: formData,
                }
              );

              const uploadResult = await uploadResponse.json();

              if (uploadResponse.ok && uploadResult.success) {
                uploadedImages = uploadResult.images || [];
                console.log("Successfully uploaded", uploadedImages.length, "images");
                ElMessage.success(`Uploaded ${uploadedImages.length} photo(s)`);

                // Clear the ready images after successful upload
                if (photoUploadRef.value && photoUploadRef.value.resetUpload) {
                  photoUploadRef.value.resetUpload();
                }
              } else {
                throw new Error(uploadResult.message || "Image upload failed");
              }
            } catch (uploadError) {
              console.error("Image upload error:", uploadError);
              ElMessage.error("Failed to upload images. Task not completed.");
              return; // Don't complete task if image upload fails
            }
          }
        }

        // Prepare completion data
        const completionData = {
          notes: completionNotes.value,
          uploadedImageCount: uploadedImages.length,
        };

        // Call API to complete task
        const response = await API.put(
          `/api/tasks/${taskDetail.value.id}/complete`,
          completionData
        );

        if (response.data) {
          // Update local task data
          taskDetail.value.status = "completed";
          taskDetail.value.progress = 100;
          taskDetail.value.completedAt = new Date().toISOString();
          taskDetail.value.completionNotes = completionNotes.value;

          // Add timeline event
          taskDetail.value.timeline.push({
            id: taskDetail.value.timeline.length + 1,
            type: "completed",
            description: `Task completed${uploadedImages.length > 0 ? ` with ${uploadedImages.length} photo(s)` : ''}`,
            timestamp: new Date().toISOString(),
          });

          // Show undo option for 30 seconds
          showUndoOption.value = true;
          setTimeout(() => {
            showUndoOption.value = false;
          }, 30000);

          completionDialogVisible.value = false;
          ElMessage.success(`Task completed successfully${uploadedImages.length > 0 ? ` with ${uploadedImages.length} photo(s) uploaded` : ''}`);
        }
      } catch (error) {
        console.error("Complete task error:", error);
        ElMessage.error("Failed to complete task. Please try again.");
      } finally {
        actionLoading.value = false;
      }
    };

    const undoCompletion = async () => {
      try {
        await ElMessageBox.confirm(
          'Are you sure you want to undo the task completion? This will revert the task back to "In Progress" status.',
          "Undo Task Completion",
          {
            confirmButtonText: "Undo",
            cancelButtonText: "Cancel",
            type: "warning",
          }
        );

        undoLoading.value = true;

        // Call API to undo task completion
        const response = await API.put(
          `/api/tasks/${taskDetail.value.id}/undo-completion`
        );

        if (response.data) {
          // Update local task data
          taskDetail.value.status = "in_progress";
          taskDetail.value.progress =
            taskDetail.value.progress > 0 ? taskDetail.value.progress : 50; // Set to previous progress or 50%
          taskDetail.value.completedAt = null;

          // Add timeline event
          taskDetail.value.timeline.push({
            id: taskDetail.value.timeline.length + 1,
            type: "reverted",
            description: "Task completion undone - reverted to in progress",
            timestamp: new Date().toISOString(),
          });

          showUndoOption.value = false;
          ElMessage.success("Task completion has been undone");
        }
      } catch (error) {
        if (error.name !== "ElMessageBoxClosedByCancel") {
          console.error("Undo completion error:", error);
          ElMessage.error("Failed to undo task completion. Please try again.");
        }
      } finally {
        undoLoading.value = false;
      }
    };

    const handlePhotosChanged = (photos) => {
      completionPhotos.value = photos;
    };

    const handleUploadSuccess = (result) => {
      // PhotoUpload component handles uploaded images internally
      // No need to add to existingImages here to avoid duplication
      ElMessage.success("Photos uploaded successfully!");
    };

    const handleImageDeleted = (imageId) => {
      // Remove from existing images when deleted
      existingImages.value = existingImages.value.filter(
        (img) => img.imageId !== imageId
      );
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

    const getPriorityText = (priority) => {
      const texts = {
        low: "Low Priority",
        normal: "Normal",
        high: "High Priority",
        urgent: "Urgent",
      };
      return texts[priority] || priority;
    };

    const getTimelineType = (type) => {
      const types = {
        created: "primary",
        assigned: "info",
        started: "warning",
        progress: "primary",
        completed: "success",
      };
      return types[type] || "primary";
    };

    const formatDateTime = (dateTime) => {
      return new Date(dateTime).toLocaleString("en-GB", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
      });
    };

    // Lifecycle
    onMounted(async () => {
      try {
        // Initialize user info
        const initSuccess = initUserInfo();
        if (!initSuccess) {
          return;
        }

        // Load task detail from API
        await loadTaskDetail();

        console.log(
          "Task detail initialized successfully with API integration"
        );
      } catch (error) {
        console.error("Error in onMounted:", error);
        ElMessage.error(
          "Failed to initialize task detail. Please try refreshing the page."
        );
      }
    });

    return {
      loading,
      actionLoading,
      taskDetail,
      userInfo,
      completionDialogVisible,
      undoLoading,
      showUndoOption,
      completionPhotos,
      completionNotes,
      photoUploadRef,
      initUserInfo,
      loadTaskDetail,
      goBack,
      viewAllPhotos,
      startTask,
      openCompletionDialog,
      completeTask,
      undoCompletion,
      handlePhotosChanged,
      handleUploadSuccess,
      handleImageDeleted,
      existingImages,
      getStatusType,
      getStatusText,
      getPriorityType,
      getPriorityText,
      getTimelineType,
      formatDateTime,
    };
  },
};
</script>

<style scoped>
/* Container */
.task-detail-container {
  max-width: 1000px;
  margin: 0 auto;
}

/* Navigation Header */
.nav-header {
  margin-bottom: 1.5rem;
}

.back-btn {
  border-radius: 0.5rem;
  color: #6b7280;
  border-color: #d1d5db;
}

.back-btn:hover {
  color: #10b981;
  border-color: #10b981;
}

/* Loading State */
.loading-state {
  padding: 2rem;
}

/* Task Header Card */
.task-header-card {
  margin-bottom: 1.5rem;
  border-radius: 1rem;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1.5rem;
}

.task-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 1rem 0;
}

.task-meta {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.task-actions {
  flex-shrink: 0;
}

.action-btn {
  border-radius: 0.75rem;
  font-weight: 600;
  padding: 0.75rem 1.5rem;
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

/* Information Grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.info-card {
  border-radius: 1rem;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.info-card.full-width {
  grid-column: 1 / -1;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #374151;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #6b7280;
}

.info-value {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #1f2937;
  font-weight: 500;
}

/* Tools Content */
.tools-content {
  padding: 0.5rem 0;
}

.tools-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.tool-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: #f3f4f6;
  border-radius: 0.5rem;
  color: #374151;
}

.tool-item .el-icon {
  color: #10b981;
}

/* Instructions Content */
.instructions-content {
  padding: 0.5rem 0;
}

.instructions-content p {
  color: #374151;
  line-height: 1.6;
  margin: 0;
}

/* Timeline Card */
.timeline-card {
  margin-bottom: 1.5rem;
  border-radius: 1rem;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* Task Completion Gallery Integration */
.task-detail-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.completion-gallery-section {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: none;
}

.gallery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f3f4f6;
}

.gallery-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.view-all-btn {
  background: linear-gradient(135deg, #10b981, #059669);
  border-color: #10b981;
  border-radius: 0.5rem;
  font-weight: 500;
}

.view-all-btn:hover {
  background: linear-gradient(135deg, #059669, #047857);
  border-color: #059669;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

/* Error State */
.error-state {
  padding: 2rem;
}

/* Completed Status */
.completed-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.75rem;
}

.completed-tag {
  font-weight: 600;
}

.undo-btn {
  font-size: 0.75rem;
  padding: 0.375rem 0.75rem;
  border-radius: 0.375rem;
  color: #f59e0b;
  border-color: #f59e0b;
  background: transparent;
}

.undo-btn:hover {
  background: #f59e0b;
  color: white;
}

/* Completion Dialog */
.completion-dialog {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.completion-header {
  text-align: center;
  padding: 0 1rem;
}

.completion-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.75rem 0;
}

.completion-subtitle {
  color: #6b7280;
  font-size: 0.875rem;
  line-height: 1.5;
  margin: 0;
}

.task-summary-card,
.completion-notes-card {
  border-radius: 0.75rem;
  border: 1px solid #e5e7eb;
}

.task-summary {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f3f4f6;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-label {
  font-weight: 500;
  color: #6b7280;
  font-size: 0.875rem;
}

.summary-value {
  font-weight: 600;
  color: #1f2937;
  font-size: 0.875rem;
}

.completion-notes {
  padding: 0.5rem 0;
}

.notes-textarea {
  border-radius: 0.5rem;
}

.completion-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 1rem 0 0 0;
}

.cancel-btn {
  border-radius: 0.5rem;
  color: #6b7280;
  border-color: #d1d5db;
}

.cancel-btn:hover {
  color: #374151;
  border-color: #9ca3af;
}

.complete-btn {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  border-color: #22c55e;
  border-radius: 0.5rem;
  font-weight: 600;
  padding: 0.75rem 1.5rem;
}

.complete-btn:hover {
  background: linear-gradient(135deg, #16a34a, #15803d);
  border-color: #16a34a;
}

/* Responsive Design */
@media (max-width: 768px) {
  .task-header {
    flex-direction: column;
    gap: 1rem;
  }

  .task-actions {
    width: 100%;
  }

  .action-btn {
    width: 100%;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .completed-status {
    align-items: center;
    width: 100%;
  }

  .undo-btn {
    width: auto;
  }

  .completion-dialog-footer {
    flex-direction: column;
    gap: 0.5rem;
  }

  .completion-dialog-footer .el-button {
    width: 100%;
  }

  .summary-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}

@media (max-width: 480px) {
  .task-title {
    font-size: 1.5rem;
  }

  .task-meta {
    justify-content: center;
  }

  .completion-title {
    font-size: 1.125rem;
  }
}
</style>
