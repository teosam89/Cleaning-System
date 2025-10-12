<template>
  <JanitorLayout>
    <div class="attendance-history-container">
      <!-- Page Header -->
      <div class="page-header">
        <div class="header-content">
          <div class="title-section">
            <h1 class="page-title">
              <el-icon><Clock /></el-icon>
              Attendance History
            </h1>
            <p class="page-subtitle">View your attendance records</p>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="refreshHistory">
              <el-icon><Refresh /></el-icon>
              Refresh
            </el-button>
          </div>
        </div>
      </div>

      <!-- Filter Section -->
      <el-card class="filter-card" shadow="never">
        <div class="filter-content">
          <div class="filter-group">
            <label class="filter-label">Date Range:</label>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="To"
              start-placeholder="Start date"
              end-placeholder="End date"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="onDateRangeChange"
              :shortcuts="datePickerShortcuts"
            />
          </div>
          <div class="filter-actions">
            <el-button type="primary" @click="applyFilters">
              <el-icon><Search /></el-icon>
              Apply Filters
            </el-button>
            <el-button @click="clearFilters">
              <el-icon><RefreshLeft /></el-icon>
              Clear
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- History Table -->
      <el-card class="history-card" shadow="never">
        <template #header>
          <div class="table-header">
            <h3>Attendance Records</h3>
            <div class="table-info">
              Total: {{ filteredHistory.length }} records
            </div>
          </div>
        </template>

        <el-table
          :data="paginatedHistory"
          v-loading="loadingHistory"
          class="history-table"
          stripe
          border
        >
          <el-table-column prop="workDate" label="Date" width="180" sortable>
            <template #default="{ row }">
              <div class="date-cell">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(row.workDate) }}
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="checkInTime" label="Check In" width="150">
            <template #default="{ row }">
              <div v-if="row.checkInTime" class="time-cell">
                <el-icon><Timer /></el-icon>
                {{ formatTime(row.checkInTime) }}
              </div>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>

          <el-table-column prop="checkOutTime" label="Check Out" width="150">
            <template #default="{ row }">
              <div v-if="row.checkOutTime" class="time-cell">
                <el-icon><Timer /></el-icon>
                {{ formatTime(row.checkOutTime) }}
              </div>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- Pagination -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="filteredHistory.length"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </JanitorLayout>
</template>

<script>
import { ref, computed, onMounted } from "vue";
import { ElMessage, ElNotification } from "element-plus";
import { API } from "@/utils/request";
import JanitorLayout from "@/components/JanitorLayout.vue";
import {
  Clock,
  Calendar,
  Timer,
  Refresh,
  Search,
  RefreshLeft,
} from "@element-plus/icons-vue";

export default {
  name: "AttendanceHistory",
  components: {
    JanitorLayout,
    Clock,
    Calendar,
    Timer,
    Refresh,
    Search,
    RefreshLeft,
  },
  setup() {
    // Reactive data
    const loadingHistory = ref(false);
    const historyData = ref([]);
    const dateRange = ref([]);
    const currentPage = ref(1);
    const pageSize = ref(20);

    // Date picker shortcuts
    const datePickerShortcuts = [
      {
        text: "Last week",
        value: () => {
          const end = new Date();
          const start = new Date();
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
          return [start, end];
        },
      },
      {
        text: "Last month",
        value: () => {
          const end = new Date();
          const start = new Date();
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
          return [start, end];
        },
      },
      {
        text: "Last 3 months",
        value: () => {
          const end = new Date();
          const start = new Date();
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
          return [start, end];
        },
      },
    ];

    // Computed properties
    const filteredHistory = computed(() => {
      let filtered = [...historyData.value];

      // Date range filter
      if (dateRange.value && dateRange.value.length === 2) {
        const [startDate, endDate] = dateRange.value;
        filtered = filtered.filter((record) => {
          const workDate = new Date(record.workDate)
            .toISOString()
            .split("T")[0];
          return workDate >= startDate && workDate <= endDate;
        });
      }

      return filtered.sort(
        (a, b) => new Date(b.workDate) - new Date(a.workDate)
      );
    });

    const paginatedHistory = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value;
      return filteredHistory.value.slice(start, start + pageSize.value);
    });

    // Methods
    const loadAttendanceHistory = async () => {
      loadingHistory.value = true;
      try {
        const response = await API.get("/api/attendance/history", {
          params: { limit: 365 }, // Get last year of data
        });

        if (response.data && Array.isArray(response.data)) {
          historyData.value = response.data.map((record) => ({
            workDate: record.workDate || "",
            checkInTime: record.checkInTime || null,
            checkOutTime: record.checkOutTime || null,
          }));
          ElNotification.success({
            title: "Success",
            message: `Loaded ${historyData.value.length} attendance records`,
            duration: 3000,
          });
        } else {
          historyData.value = [];
          ElMessage.warning("No attendance history found");
        }
      } catch (error) {
        console.error("Failed to load attendance history:", error);
        historyData.value = [];

        // Better error handling
        let errorMessage = "Failed to load attendance history";
        if (error.response) {
          if (error.response.status === 401) {
            errorMessage = "Authentication failed. Please login again.";
          } else if (error.response.status === 403) {
            errorMessage = "Access denied. Insufficient permissions.";
          } else if (error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message;
          }
        } else if (error.message) {
          errorMessage = error.message;
        }

        ElNotification.error({
          title: "Error",
          message: errorMessage,
          duration: 5000,
        });
      } finally {
        loadingHistory.value = false;
      }
    };

    const refreshHistory = async () => {
      await loadAttendanceHistory();
      ElMessage.success("History refreshed successfully");
    };

    const onDateRangeChange = () => {
      currentPage.value = 1;
    };

    const applyFilters = () => {
      currentPage.value = 1;
      ElMessage.success("Filters applied");
    };

    const clearFilters = () => {
      dateRange.value = [];
      currentPage.value = 1;
      ElMessage.success("Filters cleared");
    };

    const handleSizeChange = (newSize) => {
      pageSize.value = newSize;
      currentPage.value = 1;
    };

    const handleCurrentChange = (newPage) => {
      currentPage.value = newPage;
    };

    const formatDate = (date) => {
      if (!date) return "--";
      return new Date(date).toLocaleDateString("en-GB", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
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

    // Lifecycle
    onMounted(() => {
      loadAttendanceHistory();
    });

    return {
      loadingHistory,
      historyData,
      dateRange,
      currentPage,
      pageSize,
      datePickerShortcuts,
      filteredHistory,
      paginatedHistory,
      refreshHistory,
      onDateRangeChange,
      applyFilters,
      clearFilters,
      handleSizeChange,
      handleCurrentChange,
      formatDate,
      formatTime,
    };
  },
};
</script>

<style scoped>
/* Container */
.attendance-history-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* Page Header */
.page-header {
  margin-bottom: 2rem;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
}

.title-section .page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.title-section .page-subtitle {
  color: #6b7280;
  font-size: 1.1rem;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

/* Filter Section */
.filter-card {
  margin-bottom: 2rem;
  border-radius: 1rem;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-content {
  display: flex;
  align-items: center;
  gap: 2rem;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.filter-label {
  font-weight: 600;
  color: #374151;
  white-space: nowrap;
}

.filter-actions {
  margin-left: auto;
  display: flex;
  gap: 1rem;
}

/* History Table */
.history-card {
  border-radius: 1rem;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header h3 {
  margin: 0;
  color: #1f2937;
}

.table-info {
  color: #6b7280;
  font-size: 0.875rem;
}

.history-table {
  width: 100%;
}

/* Table Cell Styles */
.date-cell,
.time-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.no-data {
  color: #9ca3af;
  font-style: italic;
}

/* Pagination */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
  padding: 1rem 0;
}

/* Responsive Design */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .filter-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .filter-actions {
    margin-left: 0;
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
