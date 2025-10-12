<template>
  <JanitorLayout>
    <div class="task-photos-container">
      <!-- Page Header -->
      <div class="page-header">
        <div class="header-nav">
          <el-button @click="goBack" class="back-btn">
            <el-icon><ArrowLeft /></el-icon>
            Back
          </el-button>
        </div>
        <div class="header-content">
          <h1 class="page-title">
            <el-icon><Camera /></el-icon>
            Task Photos
          </h1>
          <p class="page-subtitle" v-if="taskInfo">
            Photos from: {{ taskInfo.title }}
          </p>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="6" animated />
      </div>

      <!-- Task Info Card -->
      <el-card v-if="taskInfo && !loading" class="task-info-card" shadow="never">
        <div class="task-summary">
          <div class="task-basic">
            <h3 class="task-title">{{ taskInfo.title }}</h3>
            <div class="task-meta">
              <el-tag :type="getStatusType(taskInfo.status)" size="large">
                {{ getStatusText(taskInfo.status) }}
              </el-tag>
              <span class="task-location">
                <el-icon><LocationInformation /></el-icon>
                {{ taskInfo.location }}
              </span>
              <span class="task-date" v-if="taskInfo.completedAt">
                <el-icon><Clock /></el-icon>
                Completed {{ formatDateTime(taskInfo.completedAt) }}
              </span>
            </div>
          </div>
          <div class="task-stats" v-if="images.length > 0">
            <div class="stat-item">
              <span class="stat-number">{{ images.length }}</span>
              <span class="stat-label">{{ images.length === 1 ? 'Photo' : 'Photos' }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- Photo Gallery -->
      <div v-if="!loading && images.length > 0" class="photos-section">
        <TaskCompletionGallery :images="images" />
      </div>

      <!-- Empty State -->
      <div v-if="!loading && images.length === 0" class="empty-state">
        <el-empty description="No photos found for this task">
          <template #image>
            <el-icon class="empty-icon"><Camera /></el-icon>
          </template>
          <template #description>
            <p class="empty-text">
              This task doesn't have any completion photos yet.
              <br />
              Photos are uploaded when the janitor completes the task.
            </p>
          </template>
          <template #default>
            <el-button type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              Back to Task Details
            </el-button>
          </template>
        </el-empty>
      </div>

      <!-- Error State -->
      <div v-if="error" class="error-state">
        <el-result
          icon="error"
          title="Unable to Load Photos"
          :sub-title="error"
        >
          <template #extra>
            <el-button type="primary" @click="loadTaskPhotos">
              <el-icon><Refresh /></el-icon>
              Try Again
            </el-button>
            <el-button @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              Go Back
            </el-button>
          </template>
        </el-result>
      </div>
    </div>
  </JanitorLayout>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { AuthUtils } from '@/utils/auth';
import { API } from '@/utils/request';
import JanitorLayout from '@/components/JanitorLayout.vue';
import TaskCompletionGallery from '@/components/TaskCompletionGallery.vue';
import {
  ArrowLeft,
  Camera,
  LocationInformation,
  Clock,
  Refresh,
} from '@element-plus/icons-vue';

export default {
  name: 'TaskPhotos',
  components: {
    JanitorLayout,
    TaskCompletionGallery,
    ArrowLeft,
    Camera,
    LocationInformation,
    Clock,
    Refresh,
  },
  setup() {
    const router = useRouter();
    const route = useRoute();

    // Reactive data
    const loading = ref(true);
    const images = ref([]);
    const taskInfo = ref(null);
    const error = ref(null);

    // Methods
    const goBack = () => {
      // Go back to previous page or task detail
      const taskId = route.params.taskId;
      if (taskId) {
        router.push(`/janitor/tasks/${taskId}`);
      } else {
        router.back();
      }
    };

    const loadTaskPhotos = async () => {
      const taskId = route.params.taskId;
      if (!taskId) {
        error.value = 'Task ID is required';
        loading.value = false;
        return;
      }

      try {
        loading.value = true;
        error.value = null;

        // Load task info
        const taskResponse = await API.get(`/api/tasks/${taskId}`);
        if (taskResponse.data) {
          taskInfo.value = {
            id: taskResponse.data.taskId,
            title: taskResponse.data.title,
            location: taskResponse.data.location,
            status: taskResponse.data.status,
            completedAt: taskResponse.data.completedAt,
          };
        }

        // Load task images
        const imagesResponse = await API.get(`/api/tasks/${taskId}/images`);
        if (imagesResponse.data && imagesResponse.data.success) {
          images.value = imagesResponse.data.images || [];
        } else {
          images.value = [];
        }

        console.log(`Loaded ${images.value.length} photos for task ${taskId}`);
      } catch (err) {
        console.error('Failed to load task photos:', err);
        error.value = 'Failed to load task photos. Please try again.';
        ElMessage.error('Failed to load task photos');
      } finally {
        loading.value = false;
      }
    };

    // Utility functions
    const getStatusType = (status) => {
      const types = {
        pending: 'warning',
        in_progress: 'primary',
        completed: 'success',
        overdue: 'danger',
      };
      return types[status] || 'info';
    };

    const getStatusText = (status) => {
      const texts = {
        pending: 'Pending',
        in_progress: 'In Progress',
        completed: 'Completed',
        overdue: 'Overdue',
      };
      return texts[status] || status;
    };

    const formatDateTime = (dateTime) => {
      return new Date(dateTime).toLocaleString('en-GB', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    };

    // Lifecycle
    onMounted(() => {
      // Check authentication
      if (!AuthUtils.isAuthenticated()) {
        ElMessage.error('Session expired, please login again');
        router.push('/');
        return;
      }

      loadTaskPhotos();
    });

    return {
      loading,
      images,
      taskInfo,
      error,
      goBack,
      loadTaskPhotos,
      getStatusType,
      getStatusText,
      formatDateTime,
    };
  },
};
</script>

<style scoped>
/* Container */
.task-photos-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Page Header */
.page-header {
  margin-bottom: 2rem;
}

.header-nav {
  margin-bottom: 1rem;
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

.header-content {
  text-align: center;
}

.page-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  font-size: 2rem;
  font-weight: 800;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  color: #6b7280;
  font-size: 1.125rem;
  margin: 0;
}

/* Task Info Card */
.task-info-card {
  margin-bottom: 2rem;
  border-radius: 1rem;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.task-summary {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
}

.task-basic {
  flex: 1;
}

.task-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 1rem 0;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.task-location,
.task-date {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6b7280;
  font-size: 0.875rem;
  font-weight: 500;
}

.task-stats {
  flex-shrink: 0;
  text-align: center;
  padding: 1rem;
  background: linear-gradient(135deg, #10b981, #059669);
  border-radius: 1rem;
  color: white;
  min-width: 100px;
}

.stat-number {
  display: block;
  font-size: 2rem;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 0.25rem;
}

.stat-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  opacity: 0.9;
}

/* Photos Section */
.photos-section {
  margin-bottom: 2rem;
}

/* Loading State */
.loading-state {
  padding: 2rem;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-icon {
  font-size: 4rem;
  color: #d1d5db;
  margin-bottom: 1rem;
}

.empty-text {
  color: #6b7280;
  font-size: 1rem;
  line-height: 1.6;
  margin: 1rem 0;
}

/* Error State */
.error-state {
  padding: 2rem;
}

/* Responsive Design */
@media (max-width: 768px) {
  .page-title {
    font-size: 1.75rem;
  }

  .task-summary {
    flex-direction: column;
    align-items: stretch;
    gap: 1.5rem;
  }

  .task-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .task-stats {
    align-self: center;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }

  .page-subtitle {
    font-size: 1rem;
  }

  .task-title {
    font-size: 1.25rem;
  }

  .empty-state {
    padding: 3rem 1rem;
  }
}
</style>