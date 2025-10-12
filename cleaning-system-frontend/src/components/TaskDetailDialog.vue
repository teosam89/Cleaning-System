<template>
  <el-dialog
    v-model="dialogVisible"
    title="Task Details"
    :width="'600px'"
    @closed="handleClose"
    destroy-on-close
    :z-index="1000"
  >
    <div v-if="task" class="task-detail-container">
      <!-- Task Header -->
      <div class="task-header">
        <div class="task-title-section">
          <h2 class="task-title">{{ task.title }}</h2>
          <div class="task-badges">
            <el-tag
              :type="getStatusTagType(task.status)"
              effect="plain"
              size="large"
            >
              {{ task.statusDisplay || task.status }}
            </el-tag>
            <el-tag
              :type="getPriorityTagType(task.priority)"
              effect="dark"
              size="large"
            >
              {{ task.priorityDisplay || task.priority }}
            </el-tag>
          </div>
        </div>
        <div class="task-progress-section">
          <div class="progress-label">Progress</div>
          <el-progress
            :percentage="task.progressPercentage || 0"
            :stroke-width="12"
            :color="getProgressColor(task.progressPercentage)"
          />
        </div>
      </div>

      <!-- Task Details -->
      <el-descriptions :column="1" border class="task-descriptions">
        <el-descriptions-item label="Description">
          <span v-if="task.description">{{ task.description }}</span>
          <span v-else class="no-data">No description provided</span>
        </el-descriptions-item>

        <el-descriptions-item label="Location">
          <span v-if="task.location">{{ task.location }}</span>
          <span v-else class="no-data">No location specified</span>
        </el-descriptions-item>

        <el-descriptions-item label="Assigned To">
          <div class="assigned-info">
            <el-avatar
              :size="24"
              :src="getUserAvatar(task.assignedToId)"
              class="assigned-avatar"
            >
              {{ getInitials(task.assignedToName) }}
            </el-avatar>
            <span>{{ task.assignedToName || "Unassigned" }}</span>
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="Assigned By">
          <span>{{ task.assignedByName || "Unknown" }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="Scheduled Time">
          <div v-if="task.scheduledTime" class="time-info">
            <div class="datetime">{{ formatDateTime(task.scheduledTime) }}</div>
          </div>
          <span v-else class="no-data">Not scheduled</span>
        </el-descriptions-item>

        <el-descriptions-item label="Due Date">
          <div v-if="task.dueDate" class="time-info">
            <div class="datetime" :class="{ overdue: task.isOverdue }">
              {{ formatDateTime(task.dueDate) }}
            </div>
          </div>
          <span v-else class="no-data">No deadline</span>
        </el-descriptions-item>

        <el-descriptions-item label="Created">
          <div v-if="task.createdAt" class="time-info">
            <div class="datetime">{{ formatDateTime(task.createdAt) }}</div>
          </div>
          <span v-else class="no-data">Unknown</span>
        </el-descriptions-item>

        <el-descriptions-item label="Estimated Duration">
          <span v-if="task.estimatedDuration"
            >{{ task.estimatedDuration }} minutes</span
          >
          <span v-else class="no-data">Not specified</span>
        </el-descriptions-item>

        <el-descriptions-item label="Instructions" v-if="task.instructions">
          <div class="instructions">{{ task.instructions }}</div>
        </el-descriptions-item>

        <el-descriptions-item label="Notes" v-if="task.notes">
          <div class="notes">{{ task.notes }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <!-- Task Completion Details (for completed tasks) -->
      <div v-if="task.status === 'completed'" class="completion-section">
        <h3 class="section-title">
          <el-icon><CircleCheck /></el-icon>
          Completion Details
        </h3>

        <el-descriptions :column="1" border class="completion-descriptions">
          <el-descriptions-item label="Completed At">
            <span v-if="task.completedAt">{{ formatDateTime(task.completedAt) }}</span>
            <span v-else class="no-data">Unknown</span>
          </el-descriptions-item>

          <el-descriptions-item label="Actual Duration">
            <span v-if="task.actualDuration !== null && task.actualDuration !== undefined">{{ formatDuration(task.actualDuration) }}</span>
            <span v-else class="no-data">Not recorded</span>
          </el-descriptions-item>

          <el-descriptions-item label="Completion Notes">
            <div v-if="task.completionNotes || task.completion_notes" class="completion-notes">
              {{ task.completionNotes || task.completion_notes }}
            </div>
            <span v-else class="no-data">No completion notes provided</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- Completion Photos -->
        <div v-if="completionImages && completionImages.length > 0" class="completion-photos-section">
          <h4 class="subsection-title">
            <el-icon><Picture /></el-icon>
            Completion Photos ({{ completionImages.length }})
          </h4>
          <div class="photos-grid">
            <div
              v-for="image in completionImages"
              :key="image.imageId"
              class="photo-item"
              @click="previewImage(image)"
            >
              <img
                :src="getImageUrl(getImagePath(image))"
                :alt="`Completion photo ${image.imageId}`"
                class="completion-photo"
              />
              <div class="photo-overlay">
                <el-icon><ZoomIn /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Task Actions -->
      <div class="task-actions">
        <el-button
          v-if="task.status !== 'completed'"
          type="success"
          @click="markAsCompleted"
          :loading="actionLoading"
        >
          <el-icon><CircleCheck /></el-icon>
          Mark as Completed
        </el-button>

        <el-button
          v-if="task.status === 'pending'"
          type="primary"
          @click="startTask"
          :loading="actionLoading"
        >
          <el-icon><VideoPlay /></el-icon>
          Start Task
        </el-button>

        <el-button type="warning" @click="editTask">
          <el-icon><Edit /></el-icon>
          Edit Task
        </el-button>

        <el-button type="danger" @click="deleteTask" :loading="actionLoading">
          <el-icon><Delete /></el-icon>
          Delete Task
        </el-button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-else class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- Image Preview Dialog -->
    <el-dialog
      v-model="imagePreviewVisible"
      title="Image Preview"
      width="60%"
      :append-to-body="true"
      :z-index="3000"
      destroy-on-close
    >
      <img
        v-if="previewImageUrl"
        :src="previewImageUrl"
        alt="Preview"
        class="preview-image"
      />
    </el-dialog>
  </el-dialog>
</template>

<script>
import { ref, computed, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { API } from "@/utils/request";
import { getComprehensiveAvatar } from "@/utils/avatar";
import { CircleCheck, Edit, Delete, VideoPlay, Picture, ZoomIn } from "@element-plus/icons-vue";

export default {
  name: "TaskDetailDialog",
  components: {
    CircleCheck,
    Edit,
    Delete,
    VideoPlay,
    Picture,
    ZoomIn,
  },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    taskId: {
      type: [Number, String],
      default: null,
    },
    task: {
      type: Object,
      default: null,
    },
  },
  emits: ["update:visible", "task-updated", "task-deleted", "edit-task"],
  setup(props, { emit }) {
    // Reactive data
    const actionLoading = ref(false);
    const taskDetail = ref(null);
    const completionImages = ref([]);
    const imagePreviewVisible = ref(false);
    const previewImageUrl = ref("");

    // Load completion images for completed tasks (defined before watch)
    const loadCompletionImages = async (taskId) => {
      try {
        const response = await API.get(`/api/tasks/${taskId}/images`);
        if (response.data && response.data.success && Array.isArray(response.data.images)) {
          completionImages.value = response.data.images;
        } else if (response.data && Array.isArray(response.data)) {
          // Fallback for different response format
          completionImages.value = response.data;
        }
      } catch (error) {
        console.error("Failed to load completion images:", error);
        completionImages.value = [];
      }
    };

    // Watch for task changes
    watch(
      () => props.task,
      async (newTask) => {
        if (newTask) {
          taskDetail.value = newTask;
          // Load completion images if task is completed
          if (newTask.status === 'completed' && newTask.taskId) {
            await loadCompletionImages(newTask.taskId);
          }
        }
      },
      { immediate: true }
    );

    // Computed properties
    const dialogVisible = computed({
      get: () => props.visible,
      set: (value) => emit("update:visible", value),
    });

    // Methods
    const getStatusTagType = (status) => {
      const types = {
        pending: "info",
        in_progress: "warning",
        completed: "success",
        overdue: "danger",
      };
      return types[status] || "info";
    };

    const getPriorityTagType = (priority) => {
      const types = {
        low: "info",
        medium: "primary",
        high: "warning",
        urgent: "danger",
      };
      return types[priority] || "primary";
    };

    const getProgressColor = (percentage) => {
      if (percentage < 30) return "#f56c6c";
      if (percentage < 70) return "#e6a23c";
      return "#67c23a";
    };

    const formatDateTime = (dateTime) => {
      if (!dateTime) return "";
      const date = new Date(dateTime);
      const day = date.getDate();
      const month = date.getMonth() + 1; // Month is 0-indexed
      const year = date.getFullYear();
      const hours = date.getHours();
      const minutes = date.getMinutes();

      // Convert to 12-hour format
      const hour12 = hours === 0 ? 12 : hours > 12 ? hours - 12 : hours;
      const ampm = hours >= 12 ? 'pm' : 'am';
      const minutesStr = minutes.toString().padStart(2, '0');

      return `${day}/${month}/${year} ${hour12}:${minutesStr} ${ampm}`;
    };

    const formatDuration = (duration) => {
      if (!duration || duration <= 0) return "0 minutes";

      const hours = Math.floor(duration / 60);
      const mins = duration % 60;

      if (hours > 0 && mins > 0) {
        return `${hours} hour${hours > 1 ? 's' : ''} ${mins} minute${mins > 1 ? 's' : ''}`;
      } else if (hours > 0) {
        return `${hours} hour${hours > 1 ? 's' : ''}`;
      } else {
        return `${mins} minute${mins > 1 ? 's' : ''}`;
      }
    };

    const getInitials = (name) => {
      if (!name) return "?";
      return name
        .split(" ")
        .map((n) => n[0])
        .join("")
        .toUpperCase()
        .substring(0, 2);
    };

    const getUserAvatar = (userId) => {
      if (!userId) return null;

      // Check if task has assignedToAvatarUrl field
      if (taskDetail.value && taskDetail.value.assignedToAvatarUrl) {
        return taskDetail.value.assignedToAvatarUrl;
      }

      // Fallback to comprehensive avatar system
      return getComprehensiveAvatar(userId, {
        size: 24,
        userName: taskDetail.value?.assignedToName
      });
    };

    const markAsCompleted = async () => {
      if (!taskDetail.value) return;

      actionLoading.value = true;
      try {
        await API.put(`/api/tasks/${taskDetail.value.taskId}/complete`);
        ElMessage.success("Task marked as completed");
        emit("task-updated");
        handleClose();
      } catch (error) {
        console.error("Failed to complete task:", error);
        ElMessage.error("Failed to complete task");
      } finally {
        actionLoading.value = false;
      }
    };

    const startTask = async () => {
      if (!taskDetail.value) return;

      actionLoading.value = true;
      try {
        await API.put(`/api/tasks/${taskDetail.value.taskId}/start`);
        ElMessage.success("Task started");
        emit("task-updated");
        handleClose();
      } catch (error) {
        console.error("Failed to start task:", error);
        ElMessage.error("Failed to start task");
      } finally {
        actionLoading.value = false;
      }
    };

    const editTask = () => {
      emit("edit-task", taskDetail.value);
      handleClose();
    };

    const deleteTask = async () => {
      if (!taskDetail.value) return;

      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete "${taskDetail.value.title}"?`,
          "Delete Task",
          {
            confirmButtonText: "Delete",
            cancelButtonText: "Cancel",
            type: "error",
          }
        );

        actionLoading.value = true;
        await API.delete(`/api/tasks/${taskDetail.value.taskId}`);
        ElMessage.success("Task deleted successfully");
        emit("task-deleted", taskDetail.value);
        handleClose();
      } catch (error) {
        if (error !== "cancel") {
          console.error("Failed to delete task:", error);
          ElMessage.error("Failed to delete task");
        }
      } finally {
        actionLoading.value = false;
      }
    };

    // Get image path from image object
    const getImagePath = (image) => {
      return image.publicUrl || image.imagePath || image.image_path || image.url || image.path || '';
    };

    // Get image URL for display
    const getImageUrl = (imagePath) => {
      if (!imagePath) return '';
      // Handle both relative and absolute paths
      if (imagePath.startsWith('http')) {
        return imagePath;
      }
      // If it's already a complete API path, use it directly
      if (imagePath.startsWith('/api/files/')) {
        return imagePath;
      }
      // Otherwise, construct the URL from filename
      const filename = imagePath.split('/').pop();
      return `/api/files/task_completion/${filename}`;
    };

    // Preview image in a modal dialog
    const previewImage = (image) => {
      const imagePath = getImagePath(image);

      if (!imagePath) {
        ElMessage.error('Unable to load image preview');
        return;
      }

      const imageUrl = getImageUrl(imagePath);
      previewImageUrl.value = imageUrl;
      imagePreviewVisible.value = true;
    };

    const handleClose = () => {
      emit("update:visible", false);
      // Clear completion images when dialog closes
      completionImages.value = [];
      // Clear preview state
      imagePreviewVisible.value = false;
      previewImageUrl.value = "";
    };

    return {
      // Computed properties
      dialogVisible,

      // Reactive data
      actionLoading,
      completionImages,
      imagePreviewVisible,
      previewImageUrl,

      // Methods
      getStatusTagType,
      getPriorityTagType,
      getProgressColor,
      formatDateTime,
      formatDuration,
      getInitials,
      getUserAvatar,
      markAsCompleted,
      startTask,
      editTask,
      deleteTask,
      loadCompletionImages,
      getImagePath,
      getImageUrl,
      previewImage,
      handleClose,
    };
  },
};
</script>

<style scoped>
.task-detail-container {
  padding: 16px 0;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.task-title-section {
  flex: 1;
}

.task-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.task-badges {
  display: flex;
  gap: 8px;
}

.task-progress-section {
  width: 200px;
  text-align: right;
}

.progress-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
}

.task-descriptions {
  margin-bottom: 24px;
}

.no-data {
  color: #9ca3af;
  font-style: italic;
}

.assigned-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.assigned-avatar {
  flex-shrink: 0;
}

.time-info .datetime {
  font-weight: 500;
}

.time-info .datetime.overdue {
  color: #ef4444;
}

.instructions,
.notes {
  line-height: 1.6;
  color: #374151;
}

.task-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.loading-container {
  padding: 20px 0;
}

/* Responsive design */
@media (max-width: 768px) {
  .task-header {
    flex-direction: column;
    gap: 16px;
  }

  .task-progress-section {
    width: 100%;
    text-align: left;
  }

  .task-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
}

/* Completion Details Styles */
.completion-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 2px solid #10b981;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #059669;
  margin-bottom: 16px;
}

.completion-descriptions {
  margin-bottom: 20px;
}

.completion-notes {
  line-height: 1.6;
  color: #374151;
  background: #f9fafb;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid #10b981;
}

.completion-photos-section {
  margin-top: 20px;
}

.subsection-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 12px;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.photo-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease;
  border: 2px solid #e5e7eb;
}

.photo-item:hover {
  transform: scale(1.05);
  border-color: #10b981;
}

.completion-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
  color: white;
  font-size: 24px;
}

.photo-item:hover .photo-overlay {
  opacity: 1;
}

.no-data {
  color: #9ca3af;
  font-style: italic;
}

@media (max-width: 480px) {
  .photos-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    gap: 8px;
  }

  .section-title {
    font-size: 16px;
  }

  .subsection-title {
    font-size: 14px;
  }
}

/* Image Preview Dialog Styles */
.preview-image {
  width: 100%;
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* Ensure preview dialog has higher z-index than task detail dialog */
.el-dialog__wrapper {
  z-index: 2000 !important;
}

.el-dialog {
  z-index: 2000 !important;
}

/* Specifically target image preview dialog */
.el-dialog[aria-label*="Image Preview"] {
  z-index: 2500 !important;
}
</style>
