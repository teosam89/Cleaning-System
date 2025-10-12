<template>
  <AdminLayout>
    <div class="job-monitor-container">
      <!-- Page Header -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">Job Monitor Dashboard</h1>
          <p class="page-subtitle">
            Real-time monitoring of janitor attendance and task completion - {{ selectedMonthText }}
          </p>
        </div>
        <div class="header-actions">
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            placeholder="Select Month"
            format="YYYY-MM"
            value-format="YYYY-MM"
            @change="onMonthChange"
            style="margin-right: 12px;"
          />
          <el-button @click="refreshData" :loading="loading" type="primary">
            <el-icon><Refresh /></el-icon>
            Refresh Data
          </el-button>
        </div>
      </div>

      <!-- KPI Cards -->
      <div class="kpi-section">
        <el-card class="kpi-card attendance-rate" shadow="hover">
          <div class="kpi-content">
            <div class="kpi-icon">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-value">{{ kpiData.attendanceRate }}%</div>
              <div class="kpi-label">Attendance Rate</div>
              <div class="kpi-details">{{ kpiData.attendanceDetails }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="kpi-card task-completion" shadow="hover">
          <div class="kpi-content">
            <div class="kpi-icon">
              <el-icon :size="32"><Check /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-value">{{ kpiData.taskCompletionRate }}%</div>
              <div class="kpi-label">Task Completion Rate</div>
              <div class="kpi-details">{{ kpiData.taskDetails }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="kpi-card total-janitors" shadow="hover">
          <div class="kpi-content">
            <div class="kpi-icon">
              <el-icon :size="32"><UserFilled /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-value">{{ kpiData.totalJanitors }}</div>
              <div class="kpi-label">Total Janitors</div>
              <div class="kpi-details">Active workers</div>
            </div>
          </div>
        </el-card>

        <el-card class="kpi-card active-tasks" shadow="hover">
          <div class="kpi-content">
            <div class="kpi-icon">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-value">{{ kpiData.activeTasks }}</div>
              <div class="kpi-label">Active Tasks</div>
              <div class="kpi-details">In progress + pending</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- Charts Section -->
      <div class="charts-section">
        <!-- Attendance Chart -->
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <h3>Monthly Attendance Overview</h3>
              <p>{{ selectedMonthText }} attendance distribution</p>
            </div>
          </template>
          <div class="chart-container">
            <Pie
              v-if="showCharts && attendanceChartData.labels"
              :key="'attendance-' + selectedMonth"
              :data="attendanceChartData"
              :options="pieChartOptions"
            />
            <div v-else class="chart-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <p>Loading chart data...</p>
            </div>
          </div>
        </el-card>

        <!-- Task Completion Chart -->
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <h3>Task Completion Status</h3>
              <p>{{ selectedMonthText }} task distribution by status</p>
            </div>
          </template>
          <div class="chart-container">
            <Doughnut
              v-if="showCharts && taskCompletionChartData.labels"
              :key="'task-completion-' + selectedMonth"
              :data="taskCompletionChartData"
              :options="doughnutChartOptions"
            />
            <div v-else class="chart-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <p>Loading chart data...</p>
            </div>
          </div>
        </el-card>

        <!-- Attendance Trend Chart -->
        <el-card class="chart-card wide" shadow="hover">
          <template #header>
            <div class="chart-header">
              <h3>6-Month Attendance Trend</h3>
              <p>Attendance rate trend over the last 6 months</p>
            </div>
          </template>
          <div class="chart-container">
            <Line
              v-if="showCharts && attendanceTrendData.labels"
              :key="'attendance-trend-' + selectedMonth"
              :data="attendanceTrendData"
              :options="lineChartOptions"
            />
            <div v-else class="chart-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <p>Loading trend data...</p>
            </div>
          </div>
        </el-card>

        <!-- Janitor Performance Chart -->
        <el-card class="chart-card wide" shadow="hover">
          <template #header>
            <div class="chart-header">
              <h3>Janitor Performance Comparison</h3>
              <p>Individual janitor attendance vs task completion rates</p>
            </div>
          </template>
          <div class="chart-container">
            <Bar
              v-if="showCharts && janitorPerformanceData.labels"
              :key="'janitor-performance-' + selectedMonth"
              :data="janitorPerformanceData"
              :options="barChartOptions"
            />
            <div v-else class="chart-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <p>Loading performance data...</p>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </AdminLayout>
</template>

<script>
import { ref, onMounted, reactive, computed, nextTick } from "vue";
import AdminLayout from "@/components/AdminLayout.vue";
import { ElMessage } from "element-plus";
import {
  User,
  Check,
  UserFilled,
  Document,
  Refresh,
  Loading,
} from "@element-plus/icons-vue";
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
} from "chart.js";
import { Pie, Doughnut, Line, Bar } from "vue-chartjs";
import request from "@/utils/request";

// Register Chart.js components
ChartJS.register(
  Title,
  Tooltip,
  Legend,
  ArcElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement
);

export default {
  name: "JobMonitor",
  components: {
    AdminLayout,
    User,
    Check,
    UserFilled,
    Document,
    Refresh,
    Loading,
    Pie,
    Doughnut,
    Line,
    Bar,
  },
  setup() {
    const loading = ref(false);

    // Get current month in YYYY-MM format
    const getCurrentMonth = () => {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      return `${year}-${month}`;
    };

    const selectedMonth = ref(getCurrentMonth());

    // Computed property for displaying selected month
    const selectedMonthText = computed(() => {
      if (!selectedMonth.value) return 'Current month';
      const [year, month] = selectedMonth.value.split('-');
      const date = new Date(year, parseInt(month) - 1);
      return date.toLocaleDateString('en-US', { year: 'numeric', month: 'long' });
    });

    // KPI Data
    const kpiData = reactive({
      attendanceRate: 0,
      taskCompletionRate: 0,
      totalJanitors: 0,
      activeTasks: 0,
      attendanceDetails: "",
      taskDetails: "",
    });

    // Chart Data
    const attendanceChartData = ref({});
    const taskCompletionChartData = ref({});
    const attendanceTrendData = ref({});
    const janitorPerformanceData = ref({});

    // Chart visibility control for forcing re-render
    const showCharts = ref(true);

    // Chart Options
    const pieChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: "bottom",
          labels: {
            padding: 20,
            usePointStyle: true,
          },
        },
        tooltip: {
          callbacks: {
            label: function (context) {
              const total = context.dataset.data.reduce((a, b) => a + b, 0);
              const percentage = ((context.parsed / total) * 100).toFixed(1);
              return `${context.label}: ${context.parsed} (${percentage}%)`;
            },
          },
        },
      },
    };

    const doughnutChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: "bottom",
          labels: {
            padding: 20,
            usePointStyle: true,
          },
        },
      },
    };

    const lineChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          suggestedMax: 100,
          ticks: {
            callback: function (value) {
              return value + "%";
            },
          },
        },
      },
      plugins: {
        legend: {
          display: false,
        },
      },
    };

    const barChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          max: 100,
          ticks: {
            callback: function (value) {
              return value + "%";
            },
          },
        },
      },
      plugins: {
        legend: {
          position: "bottom",
        },
      },
    };

    // Load main dashboard data
    const loadJobMonitorData = async () => {
      try {
        const params = selectedMonth.value ? { month: selectedMonth.value } : {};
        const response = await request.get("/api/admin/job-monitor", { params });
        const data = response.data;

        // Update KPI data
        kpiData.attendanceRate = data.overallAttendanceRate || 0;
        kpiData.taskCompletionRate = data.overallTaskCompletionRate || 0;
        kpiData.totalJanitors = data.totalJanitors || 0;

        const taskDetails = data.taskDetails || {};
        kpiData.activeTasks =
          (taskDetails.pending || 0) + (taskDetails.inProgress || 0);

        const attendanceDetails = data.attendanceDetails || {};
        kpiData.attendanceDetails = `${
          attendanceDetails.present || 0
        } present, ${attendanceDetails.absent || 0} absent`;
        kpiData.taskDetails = `${taskDetails.completed || 0} completed, ${
          kpiData.activeTasks
        } active`;
      } catch (error) {
        console.error("Error loading job monitor data:", error);
        const errorMessage =
          error.response?.data?.message ||
          error.message ||
          "Failed to load dashboard data";
        ElMessage.error(`Dashboard Error: ${errorMessage}`);
      }
    };

    // Load chart data
    const loadChartData = async () => {
      try {
        const params = selectedMonth.value ? { month: selectedMonth.value } : {};
        // Load all chart data in parallel
        const [
          attendanceChart,
          taskCompletionChart,
          attendanceTrend,
          janitorPerformance,
        ] = await Promise.all([
          request.get("/api/admin/job-monitor/attendance-chart", { params }),
          request.get("/api/admin/job-monitor/task-completion-chart", { params }),
          request.get("/api/admin/job-monitor/attendance-trend", { params }),
          request.get("/api/admin/job-monitor/janitor-performance", { params }),
        ]);

        attendanceChartData.value = attendanceChart.data;
        taskCompletionChartData.value = taskCompletionChart.data;
        attendanceTrendData.value = attendanceTrend.data;
        janitorPerformanceData.value = janitorPerformance.data;
      } catch (error) {
        console.error("Error loading chart data:", error);
        const errorMessage =
          error.response?.data?.message ||
          error.message ||
          "Failed to load chart data";
        ElMessage.error(`Chart Error: ${errorMessage}`);
      }
    };

    // Handle month change
    const onMonthChange = async () => {
      // 隐藏所有图表
      showCharts.value = false;

      // 等待DOM更新
      await nextTick();

      // 清空数据
      attendanceChartData.value = {};
      taskCompletionChartData.value = {};
      attendanceTrendData.value = {};
      janitorPerformanceData.value = {};

      // 刷新数据
      await refreshData();

      // 显示图表（强制重新挂载）
      showCharts.value = true;
    };

    // Refresh all data
    const refreshData = async () => {
      loading.value = true;
      try {
        await Promise.all([loadJobMonitorData(), loadChartData()]);
        ElMessage.success("Dashboard data refreshed successfully");
      } catch (error) {
        console.error("Error refreshing data:", error);
        ElMessage.error("Failed to refresh dashboard data");
      } finally {
        loading.value = false;
      }
    };

    // Load data on component mount
    onMounted(() => {
      refreshData();
    });

    return {
      loading,
      selectedMonth,
      selectedMonthText,
      kpiData,
      attendanceChartData,
      taskCompletionChartData,
      attendanceTrendData,
      janitorPerformanceData,
      showCharts,
      pieChartOptions,
      doughnutChartOptions,
      lineChartOptions,
      barChartOptions,
      onMonthChange,
      refreshData,
    };
  },
};
</script>

<style scoped>
.job-monitor-container {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 4px;
}

.header-left h1 {
  margin: 0;
  color: #1f2937;
  font-size: 28px;
  font-weight: 600;
}

.page-subtitle {
  margin: 4px 0 0 0;
  color: #6b7280;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* KPI Cards */
.kpi-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.kpi-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
}

.kpi-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.kpi-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.kpi-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.attendance-rate .kpi-icon {
  background: linear-gradient(135deg, #10b981, #059669);
}

.task-completion .kpi-icon {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.total-janitors .kpi-icon {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.active-tasks .kpi-icon {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.kpi-info {
  flex: 1;
}

.kpi-value {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
  margin-bottom: 4px;
}

.kpi-label {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 2px;
}

.kpi-details {
  font-size: 13px;
  color: #6b7280;
}

/* Charts Section */
.charts-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  border-radius: 12px;
  border: none;
  min-height: 400px;
}

.chart-card.wide {
  grid-column: span 2;
}

.chart-header {
  text-align: center;
}

.chart-header h3 {
  margin: 0 0 4px 0;
  color: #1f2937;
  font-size: 18px;
  font-weight: 600;
}

.chart-header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.chart-container {
  height: 300px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  gap: 12px;
}

.chart-loading .el-icon {
  font-size: 24px;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .charts-section {
    grid-template-columns: 1fr;
  }

  .chart-card.wide {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .job-monitor-container {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .kpi-section {
    grid-template-columns: 1fr;
  }

  .kpi-content {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }

  .chart-container {
    height: 250px;
  }
}
</style>
