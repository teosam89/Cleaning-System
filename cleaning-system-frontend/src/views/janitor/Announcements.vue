<template>
  <JanitorLayout>
    <div class="announcements-content">
      <!-- Loading State -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
        <el-skeleton :rows="5" animated style="margin-top: 1rem" />
      </div>

      <!-- Announcements Content -->
      <div v-else>
        <!-- Header Section -->
        <div class="announcements-header">
          <div class="header-left">
            <h2 class="page-title">System Announcements</h2>
            <p class="page-subtitle">
              Stay updated with the latest news and announcements
            </p>
          </div>
          <div class="header-right">
            <el-button type="primary" :loading="loading" @click="refreshData">
              Refresh Announcements
            </el-button>
          </div>
        </div>

        <!-- Announcements List -->
        <div class="announcements-grid">
          <el-card
            v-for="announcement in announcements"
            :key="announcement.id"
            class="announcement-card"
            shadow="hover"
          >
            <template #header>
              <div class="announcement-header">
                <div class="announcement-meta">
                  <el-tag
                    :type="getPriorityType(announcement.priority)"
                    size="large"
                  >
                    {{ announcement.priority.toUpperCase() }}
                  </el-tag>
                  <span class="announcement-time">{{
                    formatTime(announcement.createdAt)
                  }}</span>
                </div>
              </div>
            </template>

            <div class="announcement-body">
              <h3 class="announcement-title">{{ announcement.title }}</h3>
              <p class="announcement-content">{{ announcement.content }}</p>
              <div class="announcement-footer">
                <div class="announcement-author">
                  <el-icon><User /></el-icon>
                  <span>{{ announcement.createdBy }}</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- Empty State -->
          <div v-if="announcements.length === 0" class="empty-announcements">
            <el-empty
              description="No announcements available"
              :image-size="120"
            >
              <el-button type="primary" @click="refreshData">
                Check for Updates
              </el-button>
            </el-empty>
          </div>
        </div>
      </div>
    </div>
  </JanitorLayout>
</template>

<script>
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import JanitorLayout from "@/components/JanitorLayout.vue";
import { User } from "@element-plus/icons-vue";
import { API } from "@/utils/request";

export default {
  name: "JanitorAnnouncements",
  components: {
    JanitorLayout,
    User,
  },
  setup() {
    const loading = ref(false);
    const announcements = ref([]);

    // Load announcements from API
    const loadAnnouncements = async () => {
      try {
        const response = await API.get("/api/announcements");
        announcements.value = response.data.map((announcement) => ({
          id: announcement.announcementId,
          title: announcement.title,
          content: announcement.content,
          priority: announcement.priority || "normal",
          createdAt: new Date(announcement.createdAt),
          createdBy: announcement.createdByName || "System Administrator",
          isActive: announcement.isActive,
        }));
      } catch (error) {
        console.error("Failed to load announcements:", error);
        ElMessage.error("Failed to load announcements");
        // Fallback to mock data if API fails
        announcements.value = [
          {
            id: 1,
            title: "New Safety Protocols",
            content:
              "Please review the updated safety guidelines in the staff portal. All janitors are required to follow the new procedures starting Monday.",
            priority: "high",
            createdAt: new Date("2025-08-16T10:00:00"),
            createdBy: "System Administrator",
          },
          {
            id: 2,
            title: "Equipment Maintenance Schedule",
            content:
              "Floor cleaning equipment will be serviced tomorrow morning from 8:00 AM to 10:00 AM. Alternative equipment will be available in the storage room.",
            priority: "normal",
            createdAt: new Date("2025-08-15T14:30:00"),
            createdBy: "Facilities Manager",
          },
          {
            id: 3,
            title: "Holiday Schedule Update",
            content:
              "Please note the updated holiday schedule. Building access will be restricted on the following dates...",
            priority: "normal",
            createdAt: new Date("2025-08-14T09:15:00"),
            createdBy: "Human Resources",
          },
        ];
      }
    };

    const refreshData = async () => {
      loading.value = true;
      try {
        await loadAnnouncements();
        ElMessage.success("Announcements refreshed successfully");
      } catch (error) {
        ElMessage.error("Failed to refresh announcements");
        console.error("Refresh error:", error);
      } finally {
        loading.value = false;
      }
    };

    const formatTime = (date) => {
      return new Date(date).toLocaleString("en-GB", {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    };

    const getPriorityType = (priority) => {
      const typeMap = {
        urgent: "danger",
        high: "warning",
        normal: "success",
        low: "info",
      };
      return typeMap[priority] || "info";
    };

    onMounted(() => {
      loadAnnouncements();
    });

    return {
      loading,
      announcements,
      refreshData,
      formatTime,
      getPriorityType,
    };
  },
};
</script>

<style scoped>
/* Main container */
.announcements-content {
  padding: 0;
}

.loading-container {
  padding: 2rem;
}

/* Header styles */
.announcements-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2rem;
  border-radius: 12px;
  margin-bottom: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.header-left .page-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  color: #ffffff;
}

.header-left .page-subtitle {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

.header-right .el-button {
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-weight: 600;
  padding: 0.75rem 1.5rem;
  border-radius: 0.75rem;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.header-right .el-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

/* Announcements grid */
.announcements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.announcement-card {
  border-radius: 1rem;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.announcement-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
  width: 100%;
  justify-content: space-between;
}

.announcement-time {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
}

.announcement-body {
  padding: 0;
}

.announcement-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 1rem 0;
  line-height: 1.4;
}

.announcement-content {
  color: #4b5563;
  font-size: 1rem;
  line-height: 1.6;
  margin: 0 0 1.5rem 0;
}

.announcement-footer {
  border-top: 1px solid #e5e7eb;
  padding-top: 1rem;
  margin-top: 1.5rem;
}

.announcement-author {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6b7280;
  font-size: 0.875rem;
  font-weight: 500;
}

.announcement-author .el-icon {
  font-size: 1rem;
}

/* Empty state */
.empty-announcements {
  grid-column: 1 / -1;
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.empty-announcements .el-button {
  margin-top: 1rem;
}

/* Responsive design */
@media (max-width: 1024px) {
  .announcements-grid {
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .announcements-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .announcements-grid {
    grid-template-columns: 1fr;
  }

  .announcement-meta {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .announcements-header {
    padding: 1.5rem;
  }

  .header-left .page-title {
    font-size: 1.5rem;
  }

  .announcements-grid {
    gap: 1rem;
  }
}
</style>
