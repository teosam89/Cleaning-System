<!-- eslint-disable -->
<template>
  <SupervisorLayout>
    <div class="staff-management-container">
      <!-- Page Header with Professional Dark Theme -->
      <div class="page-header">
        <div class="header-content">
          <div class="header-text">
            <h1 class="page-title">Team Management Center</h1>
            <p class="page-subtitle">
              Comprehensive team oversight with view-only permissions and
              professional insights
            </p>
          </div>
          <div class="header-actions">
            <el-tag type="info" size="large" class="permission-badge">
              <el-icon><View /></el-icon>
              VIEW-ONLY ACCESS
            </el-tag>
            <el-button type="info" disabled class="restricted-btn">
              <el-icon><Lock /></el-icon>
              Account Management Restricted
            </el-button>
          </div>
        </div>
      </div>

      <!-- Team Statistics Dashboard -->
      <div class="stats-dashboard">
        <el-card class="stat-card stat-total" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalStaff }}</div>
              <div class="stat-label">Total Team Members</div>
              <div class="stat-detail">Active workforce under supervision</div>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card stat-janitors" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Tools /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ janitorStaff }}</div>
              <div class="stat-label">Janitors</div>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card stat-recent" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ recentStaff }}</div>
              <div class="stat-label">New Members</div>
              <div class="stat-detail">Joined in last 30 days</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- Search and Filter Panel -->
      <el-card class="filter-panel" shadow="never">
        <div class="filter-content">
          <div class="filter-left">
            <el-input
              v-model="searchQuery"
              placeholder="Search by name, username, or email..."
              prefix-icon="Search"
              clearable
              class="search-input"
              @input="handleSearch"
            />
          </div>
          <div class="filter-right">
            <el-button @click="refreshData" class="refresh-btn">
              <el-icon><Refresh /></el-icon>
              Refresh Team Data
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- Permission Notice -->
      <el-card class="permission-notice" shadow="never">
        <div class="notice-content">
          <el-icon size="20" color="#64748b"><InfoFilled /></el-icon>
          <div class="notice-text">
            <strong>Supervisor Access Level:</strong> You have view-only
            permissions for team member information. Contact an administrator
            for any staff account modifications, creation, or deletion requests.
          </div>
        </div>
      </el-card>

      <!-- Staff Table -->
      <el-card class="staff-table-card" shadow="never">
        <template #header>
          <div class="table-header">
            <div class="table-title">
              <el-icon><DataBoard /></el-icon>
              <span
                >Team Directory ({{ filteredStaffList.length }} members)</span
              >
            </div>
            <div class="table-actions">
              <el-tag type="info" size="small" class="access-tag">
                <el-icon><View /></el-icon>
                View-Only Access
              </el-tag>
            </div>
          </div>
        </template>

        <el-table
          v-loading="loading"
          :data="paginatedStaffList"
          stripe
          style="width: 100%"
          class="staff-table"
          :default-sort="{ prop: 'fullName', order: 'ascending' }"
        >
          <el-table-column
            prop="avatar"
            label="Profile"
            width="80"
            align="center"
          >
            <template #default="{ row }">
              <el-avatar
                :size="44"
                :src="getAvatarUrl(row.avatarUrl || row.avatar)"
                class="member-avatar"
              >
                {{ row.fullName.charAt(0) }}
              </el-avatar>
            </template>
          </el-table-column>

          <el-table-column
            prop="username"
            label="Username"
            width="130"
            sortable
          >
            <template #default="{ row }">
              <div class="username-cell">
                <span class="username">{{ row.username }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column
            prop="fullName"
            label="Full Name"
            min-width="180"
            sortable
          >
            <template #default="{ row }">
              <div class="name-cell">
                <span class="full-name">{{ row.fullName }}</span>
                <span class="user-id">ID: {{ row.userId }}</span>
              </div>
            </template>
          </el-table-column>


          <el-table-column prop="email" label="Contact Email" min-width="200">
            <template #default="{ row }">
              <div class="email-cell">
                <el-icon><Message /></el-icon>
                <span>{{ row.email }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column
            prop="createdAt"
            label="Joined Date"
            width="150"
            sortable
            align="center"
            header-align="center"
            class-name="date-column"
          >
            <template #default="{ row }">
              <div class="date-cell">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(row.createdAt) }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column
            label="Actions"
            width="140"
            fixed="right"
            align="center"
          >
            <template #default="{ row }">
              <el-button
                size="default"
                @click="viewStaffDetails(row)"
                class="view-btn"
              >
                <el-icon><View /></el-icon>
                View Profile
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- Enhanced Pagination -->
        <div class="pagination-container">
          <div class="pagination-info">
            Showing {{ startIndex + 1 }} to {{ endIndex }} of
            {{ filteredStaffList.length }} team members
          </div>
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 30, 50]"
            :total="filteredStaffList.length"
            layout="sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            class="staff-pagination"
          />
        </div>
      </el-card>

      <!-- Enhanced Staff Details Modal -->
      <el-dialog
        v-model="staffDetailDialogVisible"
        title="Staff Profile Details"
        width="900px"
        @closed="resetStaffDetail"
        class="staff-detail-dialog"
        :close-on-click-modal="false"
        :destroy-on-close="true"
        top="5vh"
      >
        <div v-if="selectedStaff" class="staff-details">
          <!-- Profile Header -->
          <div class="detail-header">
            <el-avatar
              :size="80"
              :src="getAvatarUrl(staffProfile?.avatarUrl || selectedStaff.avatar)"
            >
              {{ selectedStaff.fullName.charAt(0) }}
            </el-avatar>
            <div class="detail-info">
              <h3>{{ selectedStaff.fullName }}</h3>
              <p>@{{ selectedStaff.username }}</p>
              <el-tag :type="getRoleType(selectedStaff.role)" size="large">
                {{ getRoleText(selectedStaff.role) }}
              </el-tag>
              <el-tag
                :type="getStatusType('active')"
                size="large"
                style="margin-left: 8px"
              >
                {{ getStatusText("active") }}
              </el-tag>
            </div>
          </div>

          <!-- Loading State -->
          <div v-if="profileLoading" class="profile-loading">
            <el-skeleton :rows="4" animated />
            <el-skeleton :rows="3" animated style="margin-top: 1rem" />
          </div>

          <!-- Profile Content -->
          <div v-else class="profile-content">
            <!-- Optimized Two-Column Layout -->
            <div class="profile-detail-grid">
              <div class="detail-section">
                <div class="section-header">
                  <el-icon><User /></el-icon>
                  <span>Basic Information</span>
                </div>
                <div class="detail-items">
                  <div class="detail-item">
                    <label>Full Name:</label>
                    <span>{{ selectedStaff.fullName }}</span>
                  </div>
                  <div class="detail-item">
                    <label>Username:</label>
                    <span>{{ selectedStaff.username }}</span>
                  </div>
                  <div class="detail-item">
                    <label>User ID:</label>
                    <span>#{{ selectedStaff.userId }}</span>
                  </div>
                  <div class="detail-item">
                    <label>Employee Number:</label>
                    <span>{{
                      staffProfile?.employeeNumber || "Not assigned"
                    }}</span>
                  </div>
                  <div class="detail-item">
                    <label>Gender:</label>
                    <span>{{ staffProfile?.gender || "Not specified" }}</span>
                  </div>
                  <div class="detail-item">
                    <label>Birth Date:</label>
                    <span>{{
                      staffProfile?.birthDate
                        ? formatDetailDate(staffProfile.birthDate)
                        : "Not provided"
                    }}</span>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <div class="section-header">
                  <el-icon><Message /></el-icon>
                  <span>Contact Information</span>
                </div>
                <div class="detail-items">
                  <div class="detail-item">
                    <label>Email Address:</label>
                    <span>{{ selectedStaff.email || "Not provided" }}</span>
                  </div>
                  <div class="detail-item">
                    <label>Phone Number:</label>
                    <span>{{ staffProfile?.phone || "Not provided" }}</span>
                  </div>
                  <div class="detail-item">
                    <label>Address:</label>
                    <span>{{ staffProfile?.address || "Not provided" }}</span>
                  </div>
                  <div class="detail-item">
                    <label>Emergency Contact:</label>
                    <span>{{
                      staffProfile?.emergencyContact || "Not provided"
                    }}</span>
                  </div>
                  <div class="detail-item">
                    <label>Join Date:</label>
                    <span>{{
                      staffProfile?.joinDate
                        ? formatDetailDate(staffProfile.joinDate)
                        : formatDetailDate(selectedStaff.createdAt)
                    }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Extended Information Sections -->
            <div class="extended-sections">
              <!-- Performance Metrics -->
              <el-card
                class="profile-section"
                shadow="never"
                v-if="performanceData"
              >
                <template #header>
                  <div class="section-header">
                    <el-icon><TrendCharts /></el-icon>
                    <span>Performance Overview</span>
                    <el-tag type="info" size="small" style="margin-left: 10px">
                      {{ performanceData.calculationMonth || "Current Month" }}
                    </el-tag>
                  </div>
                </template>
                <div class="performance-grid">
                  <div class="performance-metrics-row">
                    <div class="performance-item">
                      <div class="performance-value">
                        {{ performanceData.monthlyAttendance || 0 }}%
                      </div>
                      <div class="performance-label">Monthly Attendance</div>
                      <div class="performance-detail">
                        {{ getAttendanceDetail() }}
                      </div>
                    </div>
                    <div class="performance-item">
                      <div class="performance-value">
                        {{ performanceData.taskCompletionRate || 0 }}%
                      </div>
                      <div class="performance-label">Task Completion Rate</div>
                      <div class="performance-detail">
                        {{ getTaskDetail() }}
                      </div>
                    </div>
                    <div class="performance-item">
                      <div class="performance-value">
                        {{ performanceData.performanceRate || 0 }}%
                      </div>
                      <div class="performance-label">Performance Rate</div>
                      <div class="performance-detail">
                        Weighted average based on attendance & tasks
                      </div>
                    </div>
                  </div>
                </div>

                <div class="performance-actions" style="margin-top: 20px">
                  <el-button
                    @click="refreshPerformanceData"
                    :loading="performanceLoading"
                    type="primary"
                    size="small"
                  >
                    <el-icon><Refresh /></el-icon>
                    Refresh Metrics
                  </el-button>
                  <el-popover
                    placement="top"
                    width="300"
                    trigger="hover"
                    content="Performance metrics are calculated based on real attendance data (Mon-Fri) and assigned task completion rates. Performance rate is a weighted average of attendance (40%) and task completion (60%)."
                  >
                    <template #reference>
                      <el-button type="info" size="small" plain>
                        <el-icon><QuestionFilled /></el-icon>
                        How It's Calculated
                      </el-button>
                    </template>
                  </el-popover>
                </div>
              </el-card>

              <!-- Task Records Section -->
              <el-card
                class="profile-section"
                shadow="never"
                v-if="staffProfile && (selectedStaff.role === 'janitor' || selectedStaff.role === 'cleaner')"
              >
                <template #header>
                  <div class="section-header">
                    <el-icon><List /></el-icon>
                    <span>Task Records</span>
                    <el-tag type="success" size="small" style="margin-left: 10px">
                      {{ userTasks.length }} Total Tasks
                    </el-tag>
                  </div>
                </template>

                <div class="task-controls">
                  <el-button
                    @click="loadUserTasks"
                    :loading="tasksLoading"
                    type="primary"
                    size="default"
                  >
                    <el-icon><Search /></el-icon>
                    Load Tasks
                  </el-button>
                  <el-select
                    v-model="taskStatusFilter"
                    placeholder="Filter by Status"
                    size="default"
                    style="width: 150px; margin-left: 10px"
                    @change="handleTaskFilterChange"
                  >
                    <el-option label="All Tasks" value="" />
                    <el-option label="Pending" value="pending" />
                    <el-option label="In Progress" value="in_progress" />
                    <el-option label="Completed" value="completed" />
                    <el-option label="Cancelled" value="cancelled" />
                  </el-select>
                </div>

                <!-- Task Records Table -->
                <div
                  v-if="filteredTasks.length > 0"
                  class="task-table-container"
                >
                  <el-table
                    :data="paginatedTasks"
                    stripe
                    size="small"
                    style="width: 100%; margin-top: 15px"
                    class="task-table"
                  >
                    <el-table-column prop="taskId" label="Task ID" width="80" />
                    <el-table-column prop="title" label="Task Title" min-width="150" />
                    <el-table-column prop="status" label="Status" width="120">
                      <template #default="{ row }">
                        <el-tag :type="getTaskStatusType(row.status)" size="small">
                          {{ formatTaskStatus(row.status) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="priority" label="Priority" width="100">
                      <template #default="{ row }">
                        <el-tag :type="getTaskPriorityType(row.priority)" size="small">
                          {{ row.priority }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="assignedDate" label="Assigned" width="120">
                      <template #default="{ row }">
                        {{ formatTaskDate(row.assignedDate) }}
                      </template>
                    </el-table-column>
                    <el-table-column prop="dueDate" label="Due Date" width="120">
                      <template #default="{ row }">
                        <span :class="{ 'text-danger': isTaskOverdue(row.dueDate, row.status) }">
                          {{ formatTaskDate(row.dueDate) }}
                        </span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="completedDate" label="Completed" width="120">
                      <template #default="{ row }">
                        {{ row.completedDate ? formatTaskDate(row.completedDate) : '-' }}
                      </template>
                    </el-table-column>
                  </el-table>

                  <!-- Task Pagination -->
                  <div class="task-pagination">
                    <el-pagination
                      v-model:current-page="taskCurrentPage"
                      v-model:page-size="taskPageSize"
                      :page-sizes="[5, 10, 20]"
                      :total="filteredTasks.length"
                      layout="total, sizes, prev, pager, next"
                      @size-change="handleTaskSizeChange"
                      @current-change="handleTaskCurrentChange"
                    />
                  </div>
                </div>

                <!-- No Tasks Message -->
                <div v-else-if="!tasksLoading && userTasks.length === 0" class="no-data">
                  <el-empty
                    description="No tasks found for this staff member"
                    :image-size="60"
                  />
                </div>

                <!-- Tasks Loading -->
                <div v-else-if="tasksLoading" class="loading-state">
                  <el-skeleton :rows="3" animated />
                  <p style="text-align: center; margin-top: 10px; color: #666;">
                    Loading task records...
                  </p>
                </div>
              </el-card>

              <!-- Attendance Records Section -->
              <el-card
                class="profile-section"
                shadow="never"
                v-if="staffProfile && (selectedStaff.role === 'janitor' || selectedStaff.role === 'cleaner')"
              >
                <template #header>
                  <div class="section-header">
                    <el-icon><Clock /></el-icon>
                    <span>Attendance Records</span>
                    <el-tag type="info" size="small" style="margin-left: 10px">
                      {{ attendanceRecords.length }} Records
                    </el-tag>
                  </div>
                </template>

                <div class="attendance-controls">
                  <el-button
                    @click="loadAttendanceRecords"
                    :loading="attendanceLoading"
                    type="primary"
                    size="default"
                  >
                    <el-icon><Search /></el-icon>
                    Load Attendance
                  </el-button>
                  <el-date-picker
                    v-model="attendanceDateRange"
                    type="daterange"
                    range-separator="To"
                    start-placeholder="Start date"
                    end-placeholder="End date"
                    size="default"
                    style="width: 240px; margin-left: 10px"
                    @change="loadAttendanceRecords"
                  />
                </div>

                <!-- Attendance Records Table -->
                <div
                  v-if="attendanceRecords.length > 0"
                  class="attendance-table-container"
                >
                  <el-table
                    :data="paginatedAttendanceRecords"
                    stripe
                    size="small"
                    style="width: 100%; margin-top: 15px"
                    class="attendance-table"
                  >
                    <el-table-column prop="date" label="Date" width="120">
                      <template #default="{ row }">
                        {{ formatAttendanceDate(row.date || row.attendanceDate || row.checkInTime) }}
                      </template>
                    </el-table-column>
                    <el-table-column prop="checkInTime" label="Check In" width="100">
                      <template #default="{ row }">
                        {{ formatAttendanceTime(row.checkInTime) }}
                      </template>
                    </el-table-column>
                    <el-table-column prop="checkOutTime" label="Check Out" width="100">
                      <template #default="{ row }">
                        {{ formatAttendanceTime(row.checkOutTime) }}
                      </template>
                    </el-table-column>
                    <el-table-column prop="workHours" label="Work Hours" width="120">
                      <template #default="{ row }">
                        {{ calculateWorkHours(row.checkInTime, row.checkOutTime) }}
                      </template>
                    </el-table-column>
                  </el-table>

                  <!-- Attendance Pagination -->
                  <div class="attendance-pagination">
                    <el-pagination
                      v-model:current-page="attendanceCurrentPage"
                      v-model:page-size="attendancePageSize"
                      :page-sizes="[5, 10, 20]"
                      :total="attendanceRecords.length"
                      layout="total, sizes, prev, pager, next"
                      @size-change="handleAttendanceSizeChange"
                      @current-change="handleAttendanceCurrentChange"
                    />
                  </div>
                </div>

                <!-- No Attendance Records Message -->
                <div v-else-if="!attendanceLoading && attendanceRecordsLoaded && attendanceRecords.length === 0" class="no-data">
                  <el-empty
                    description="No attendance records found for the selected period"
                    :image-size="60"
                  />
                </div>

                <!-- Attendance Loading -->
                <div v-else-if="attendanceLoading" class="loading-state">
                  <el-skeleton :rows="3" animated />
                  <p style="text-align: center; margin-top: 10px; color: #666;">
                    Loading attendance records...
                  </p>
                </div>

                <!-- Initial State -->
                <div v-else class="no-data">
                  <el-empty
                    description="Click 'Load Attendance' to view attendance records"
                    :image-size="60"
                  />
                </div>
              </el-card>

              <!-- Permission Notice -->
              <el-card class="profile-section" shadow="never">
                <el-alert
                  title="Supervisor View-Only Access"
                  description="You can view comprehensive team member profiles and task assignments but cannot modify their information. Contact an administrator for any profile updates or task reassignments."
                  type="info"
                  :closable="false"
                  show-icon
                />
              </el-card>

              <!-- Modal Footer Actions -->
              <div class="modal-footer">
                <el-button @click="staffDetailDialogVisible = false" size="large">
                  Close Profile
                </el-button>
                <el-button type="primary" @click="contactAdmin" size="large">
                  <el-icon><Message /></el-icon>
                  Contact Admin
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
  </SupervisorLayout>
</template>

<script>
import { ref, computed, onMounted } from "vue";
import { ElMessage, ElNotification } from "element-plus";
import { API } from "@/utils/request";
import SupervisorLayout from "@/components/SupervisorLayout.vue";
import {
  User,
  Tools,
  Brush,
  Clock,
  View,
  Lock,
  InfoFilled,
  Refresh,
  DataBoard,
  Message,
  Calendar,
  CircleCheckFilled,
  CircleCheck,
  Search,
  QuestionFilled,
  DocumentChecked,
  TrendCharts,
} from "@element-plus/icons-vue";

export default {
  name: "SupervisorStaffPro",
  components: {
    SupervisorLayout,
    User,
    Tools,
    Brush,
    Clock,
    View,
    Lock,
    InfoFilled,
    Refresh,
      DataBoard,
      Message,
    Calendar,
    CircleCheckFilled,
    CircleCheck,
    Search,
    QuestionFilled,
    DocumentChecked,
    TrendCharts,
  },
  setup() {
    // Reactive data
    const loading = ref(true);
    const staffList = ref([]);
    const selectedStaff = ref(null);
    const staffDetailDialogVisible = ref(false);
    const profileLoading = ref(false);
    const staffProfile = ref(null);

    // Performance data
    const performanceData = ref(null);
    const performanceLoading = ref(false);

    // Attendance Records
    const attendanceRecords = ref([]);
    const attendanceLoading = ref(false);
    const attendanceRecordsLoaded = ref(false);
    const attendanceDateRange = ref([]);
    const attendanceCurrentPage = ref(1);
    const attendancePageSize = ref(10);

    // Task Management
    const userTasks = ref([]);
    const tasksLoading = ref(false);
    const tasksLoaded = ref(false);
    const taskStatistics = ref(null);
    const taskStatusFilter = ref("");
    const taskPriorityFilter = ref("");
    const taskCurrentPage = ref(1);
    const taskPageSize = ref(10);

    // Search and filter
    const searchQuery = ref("");

    // Pagination
    const currentPage = ref(1);
    const pageSize = ref(20);

    // Computed properties
    const filteredStaffList = computed(() => {
      let filtered = staffList.value;

      // Search filter
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();
        filtered = filtered.filter(
          (staff) =>
            staff.fullName.toLowerCase().includes(query) ||
            staff.username.toLowerCase().includes(query) ||
            staff.email.toLowerCase().includes(query)
        );
      }

      return filtered;
    });

    const paginatedStaffList = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value;
      const end = start + pageSize.value;
      return filteredStaffList.value.slice(start, end);
    });

    const startIndex = computed(() => (currentPage.value - 1) * pageSize.value);
    const endIndex = computed(() =>
      Math.min(
        startIndex.value + pageSize.value,
        filteredStaffList.value.length
      )
    );

    // Statistics
    const totalStaff = computed(() => staffList.value.length);
    const janitorStaff = computed(
      () => staffList.value.filter((staff) => staff.role === "janitor").length
    );
    const recentStaff = computed(() => {
      const thirtyDaysAgo = new Date();
      thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
      return staffList.value.filter(
        (staff) => new Date(staff.createdAt) > thirtyDaysAgo
      ).length;
    });

    // Attendance computed properties
    const paginatedAttendanceRecords = computed(() => {
      const start =
        (attendanceCurrentPage.value - 1) * attendancePageSize.value;
      const end = start + attendancePageSize.value;
      return attendanceRecords.value.slice(start, end);
    });

    // Task computed properties
    const filteredTasks = computed(() => {
      let filtered = userTasks.value;

      // Apply status filter
      if (taskStatusFilter.value) {
        filtered = filtered.filter(
          (task) => task.status === taskStatusFilter.value
        );
      }

      return filtered;
    });

    const paginatedTasks = computed(() => {
      const start = (taskCurrentPage.value - 1) * taskPageSize.value;
      const end = start + taskPageSize.value;
      return filteredTasks.value.slice(start, end);
    });

    const paginatedUserTasks = computed(() => {
      const start = (taskCurrentPage.value - 1) * taskPageSize.value;
      const end = start + taskPageSize.value;
      return userTasks.value.slice(start, end);
    });

    // Methods
    const loadStaffData = async () => {
      try {
        loading.value = true;
        console.log("Loading staff data for supervisor...");

        const response = await API.get("/api/supervisor/staff");

        if (response.data && Array.isArray(response.data)) {
          staffList.value = response.data;
          console.log(
            "Staff data loaded:",
            response.data.length,
            "team members"
          );
        } else {
          console.warn("Invalid staff data received:", response.data);
          staffList.value = [];
        }
      } catch (error) {
        console.error("Failed to load staff data:", error);
        ElMessage.error("Failed to load team data. Please try again.");
        staffList.value = [];
      } finally {
        loading.value = false;
      }
    };

    const refreshData = async () => {
      await loadStaffData();
      ElMessage.success("Team data refreshed successfully");
    };


    const handleSearch = () => {
      currentPage.value = 1;
    };

    // Attendance Records Methods
    const loadAttendanceRecords = async () => {
      if (!selectedStaff.value?.userId) {
        ElMessage.warning("No staff member selected");
        return;
      }

      try {
        attendanceLoading.value = true;
        console.log(
          "Loading attendance records for user:",
          selectedStaff.value.userId
        );

        // Build query parameters
        const params = new URLSearchParams();
        if (
          attendanceDateRange.value &&
          attendanceDateRange.value.length === 2
        ) {
          const startDate = attendanceDateRange.value[0]
            .toISOString()
            .split("T")[0];
          const endDate = attendanceDateRange.value[1]
            .toISOString()
            .split("T")[0];
          params.append("startDate", startDate);
          params.append("endDate", endDate);
        }

        const queryString = params.toString();
        const url = `/api/attendance/history/${selectedStaff.value.userId}${
          queryString ? "?" + queryString : ""
        }`;

        const response = await API.get(url);
        console.log("Attendance API response:", response.data);

        if (response.data && Array.isArray(response.data)) {
          attendanceRecords.value = response.data;
          attendanceRecordsLoaded.value = true;
          attendanceCurrentPage.value = 1;
          console.log("Loaded", response.data.length, "attendance records");
        } else {
          attendanceRecords.value = [];
          attendanceRecordsLoaded.value = true;
          console.warn("No attendance records found");
        }
      } catch (error) {
        console.error("Failed to load attendance records:", error);
        ElMessage.error("Failed to load attendance records. Please try again.");
        attendanceRecords.value = [];
        attendanceRecordsLoaded.value = true;
      } finally {
        attendanceLoading.value = false;
      }
    };

    // Attendance formatting functions
    const formatAttendanceDate = (dateString) => {
      if (!dateString) return "--";
      const date = new Date(dateString);
      return date.toLocaleDateString("en-GB", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
      });
    };

    const formatAttendanceTime = (timeString) => {
      if (!timeString) return "--";
      const time = new Date(timeString);
      return time.toLocaleTimeString("en-GB", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false,
      });
    };

    const calculateWorkHours = (checkInTime, checkOutTime) => {
      if (!checkInTime || !checkOutTime) return "--";
      try {
        const checkIn = new Date(checkInTime);
        const checkOut = new Date(checkOutTime);

        if (isNaN(checkIn.getTime()) || isNaN(checkOut.getTime())) {
          return "--";
        }

        const diffInMs = checkOut.getTime() - checkIn.getTime();
        if (diffInMs < 0) return "--";

        const diffInMinutes = Math.floor(diffInMs / (1000 * 60));
        const hours = Math.floor(diffInMinutes / 60);
        const minutes = diffInMinutes % 60;

        if (hours > 0 && minutes > 0) {
          return `${hours}h ${minutes}m`;
        } else if (hours > 0) {
          return `${hours}h`;
        } else if (minutes > 0) {
          return `${minutes}m`;
        } else {
          return "0m";
        }
      } catch (error) {
        console.error("Error calculating work hours:", error);
        return "--";
      }
    };

    const getAttendanceStatusType = (status) => {
      const statusTypes = {
        normal: "success",
        late: "warning",
        early_leave: "warning",
        absent: "danger",
        leave: "info",
      };
      return statusTypes[status] || "success";
    };

    // Reset attendance data when dialog closes
    const resetAttendanceData = () => {
      attendanceRecords.value = [];
      attendanceRecordsLoaded.value = false;
      attendanceDateRange.value = [];
      attendanceCurrentPage.value = 1;
    };

    const handleAttendanceSizeChange = (val) => {
      attendancePageSize.value = val;
      attendanceCurrentPage.value = 1;
    };

    const handleAttendanceCurrentChange = (val) => {
      attendanceCurrentPage.value = val;
    };

    const handleSizeChange = (val) => {
      pageSize.value = val;
      currentPage.value = 1;
    };

    const handleCurrentChange = (val) => {
      currentPage.value = val;
    };


    const viewStaffDetails = async (staff) => {
      selectedStaff.value = staff;
      staffProfile.value = null;
      staffDetailDialogVisible.value = true;

      // Reset task data
      resetTaskData();

      // Load comprehensive profile data
      await loadStaffProfile(staff.userId);

      // Load performance data
      await loadPerformanceData(staff.userId);

      // Load task statistics
      await loadTaskStatistics(staff.userId);
    };

    const loadStaffProfile = async (userId) => {
      if (!userId) {
        console.warn("No user ID provided for profile loading");
        return;
      }

      try {
        profileLoading.value = true;
        console.log("Loading comprehensive profile for user:", userId);

        const response = await API.get(
          `/api/supervisor/staff/${userId}/profile`
        );
        console.log("Profile API response:", response.data);

        if (response.data && response.data.success) {
          staffProfile.value = response.data.profile;
          console.log("Profile loaded successfully:", staffProfile.value);
        } else {
          console.warn(
            "Profile API returned unsuccessful response:",
            response.data
          );
          ElMessage.warning("Could not load complete profile data");
        }
      } catch (error) {
        console.error("Failed to load staff profile:", error);
        ElMessage.error("Failed to load profile details. Please try again.");
      } finally {
        profileLoading.value = false;
      }
    };

    const resetStaffDetail = () => {
      selectedStaff.value = null;
      staffProfile.value = null;
      performanceData.value = null;
      resetAttendanceData();
      resetTaskData();
    };

    const contactAdmin = () => {
      ElNotification({
        title: "Contact Administrator",
        message:
          "Please contact your system administrator for any staff account modifications or questions.",
        type: "info",
        duration: 5000,
      });
    };

    // Performance-related methods
    const loadPerformanceData = async (userId) => {
      if (!userId) {
        console.warn("No user ID provided for performance loading");
        return;
      }

      try {
        performanceLoading.value = true;
        console.log("Loading performance data for user:", userId);

        const response = await API.get(
          `/api/supervisor/staff/${userId}/performance`
        );
        console.log("Performance API response:", response.data);

        if (response.data && response.data.success) {
          performanceData.value = response.data.performance;
          console.log(
            "Performance data loaded successfully:",
            performanceData.value
          );
        } else {
          console.warn(
            "Performance API returned unsuccessful response:",
            response.data
          );
          ElMessage.warning("Could not load performance data");
        }
      } catch (error) {
        console.error("Failed to load performance data:", error);
        ElMessage.error(
          "Failed to load performance metrics. Please try again."
        );
        performanceData.value = null;
      } finally {
        performanceLoading.value = false;
      }
    };

    const refreshPerformanceData = async () => {
      if (selectedStaff.value && selectedStaff.value.userId) {
        await loadPerformanceData(selectedStaff.value.userId);
        ElMessage.success("Performance metrics refreshed");
      }
    };

    const getAttendanceDetail = () => {
      if (!performanceData.value || !performanceData.value.attendanceDetails) {
        return "No data available";
      }
      const details = performanceData.value.attendanceDetails;
      return `${details.attendedDays || 0}/${
        details.totalWorkingDays || 0
      } working days`;
    };

    const getTaskDetail = () => {
      if (!performanceData.value || !performanceData.value.taskDetails) {
        return "No data available";
      }
      const details = performanceData.value.taskDetails;
      return `${details.completedTasks || 0}/${
        details.totalTasks || 0
      } tasks completed`;
    };

    // Task-related methods
    const loadTaskStatistics = async (userId) => {
      if (!userId) {
        console.warn("No user ID provided for task statistics loading");
        return;
      }

      try {
        console.log("Loading task statistics for user:", userId);

        const response = await API.get(
          `/api/supervisor/staff/${userId}/task-stats`
        );
        console.log("Task statistics API response:", response.data);

        if (response.data && response.data.success) {
          taskStatistics.value = response.data.statistics;
          console.log(
            "Task statistics loaded successfully:",
            taskStatistics.value
          );
        } else {
          console.warn(
            "Task statistics API returned unsuccessful response:",
            response.data
          );
          ElMessage.warning("Could not load task statistics");
        }
      } catch (error) {
        console.error("Failed to load task statistics:", error);
        ElMessage.error("Failed to load task statistics. Please try again.");
        taskStatistics.value = null;
      }
    };

    const loadUserTasks = async () => {
      if (!selectedStaff.value?.userId) {
        ElMessage.warning("No staff member selected");
        return;
      }

      try {
        tasksLoading.value = true;
        console.log(
          "Loading tasks for user:",
          selectedStaff.value.userId,
          "with filters:",
          { status: taskStatusFilter.value, priority: taskPriorityFilter.value }
        );

        // Build query parameters
        const params = new URLSearchParams();
        if (taskStatusFilter.value) {
          params.append("status", taskStatusFilter.value);
        }
        if (taskPriorityFilter.value) {
          params.append("priority", taskPriorityFilter.value);
        }
        params.append("limit", "50"); // Get more tasks for better overview
        params.append("offset", "0");

        const queryString = params.toString();
        const url = `/api/supervisor/staff/${selectedStaff.value.userId}/tasks${
          queryString ? "?" + queryString : ""
        }`;

        const response = await API.get(url);
        console.log("Tasks API response:", response.data);

        if (response.data && response.data.success) {
          userTasks.value = response.data.tasks;
          tasksLoaded.value = true;
          taskCurrentPage.value = 1;
          console.log("Loaded", response.data.tasks.length, "tasks");

          if (response.data.tasks.length === 0) {
            ElMessage.info("No tasks found for the selected filters");
          }
        } else {
          userTasks.value = [];
          tasksLoaded.value = true;
          console.warn("No tasks found");
        }
      } catch (error) {
        console.error("Failed to load tasks:", error);
        ElMessage.error("Failed to load tasks. Please try again.");
        userTasks.value = [];
        tasksLoaded.value = true;
      } finally {
        tasksLoading.value = false;
      }
    };

    const handleTaskFilterChange = () => {
      // Reset pagination and reload tasks
      taskCurrentPage.value = 1;
      userTasks.value = [];
      tasksLoaded.value = false;

      // Auto-load tasks when filter changes if dialog is open
      if (selectedStaff.value) {
        loadUserTasks();
      }
    };

    const resetTaskData = () => {
      userTasks.value = [];
      tasksLoaded.value = false;
      taskStatistics.value = null;
      taskStatusFilter.value = "";
      taskPriorityFilter.value = "";
      taskCurrentPage.value = 1;
    };

    // Task formatting and utility functions
    const getTaskStatusType = (status) => {
      const statusTypes = {
        completed: "success",
        in_progress: "primary",
        pending: "warning",
        overdue: "danger",
      };
      return statusTypes[status] || "info";
    };

    const getTaskStatusText = (status) => {
      const statusTexts = {
        completed: "Completed",
        in_progress: "In Progress",
        pending: "Pending",
        overdue: "Overdue",
      };
      return statusTexts[status] || status;
    };

    const getTaskPriorityType = (priority) => {
      const priorityTypes = {
        urgent: "danger",
        high: "warning",
        normal: "",
        low: "info",
      };
      return priorityTypes[priority] || "";
    };

    const formatTaskDate = (dateString) => {
      if (!dateString) return "N/A";
      try {
        const date = new Date(dateString);
        return date.toLocaleDateString("en-GB", {
          month: "short",
          day: "numeric",
          year: "numeric",
        });
      } catch (error) {
        return "Invalid Date";
      }
    };

    const formatTaskStatus = (status) => {
      const statusTexts = {
        completed: "Completed",
        in_progress: "In Progress",
        pending: "Pending",
        cancelled: "Cancelled",
        overdue: "Overdue",
      };
      return statusTexts[status] || status;
    };

    const isTaskOverdue = (dueDate, status) => {
      if (status === "completed" || status === "cancelled") return false;
      if (!dueDate) return false;
      return new Date(dueDate) < new Date();
    };

    const handleTaskSizeChange = (val) => {
      taskPageSize.value = val;
      taskCurrentPage.value = 1;
    };

    const handleTaskCurrentChange = (val) => {
      taskCurrentPage.value = val;
    };

    // Helper methods
    const getAvatarUrl = (url) => {
      if (!url) return null;
      if (url.startsWith("http")) return url;
      // Convert relative URL to full backend URL
      return url.startsWith("/api")
        ? `http://localhost:8765${url}`
        : `http://localhost:8765/api${url}`;
    };

    const getRoleType = (role) => {
      const roleTypes = {
        janitor: "info",
        cleaner: "info",
        supervisor: "warning",
        admin: "danger",
      };
      return roleTypes[role] || "info";
    };

    const getRoleText = (role) => {
      const roleTexts = {
        janitor: "Janitor",
        cleaner: "Cleaner",
        supervisor: "Supervisor",
        admin: "Administrator",
      };
      return roleTexts[role] || role.charAt(0).toUpperCase() + role.slice(1);
    };

    // Status handling functions (for user status if available)
    const getStatusType = (status) => {
      const types = {
        active: "success",
        inactive: "danger",
        pending: "warning",
      };
      return types[status] || "info";
    };

    const getStatusText = (status) => {
      const texts = {
        active: "Active",
        inactive: "Inactive",
        pending: "Pending",
      };
      return texts[status] || status || "Active"; // Default to Active if no status
    };

    const getRoleIcon = (role) => {
      const roleIcons = {
        janitor: Tools,
        cleaner: Brush,
        supervisor: User,
        admin: User,
      };
      return roleIcons[role] || User;
    };

    const getDepartment = (role) => {
      const departments = {
        janitor: "Maintenance & Facilities",
        cleaner: "Cleaning Services",
        supervisor: "Team Leadership",
        admin: "Administration",
      };
      return departments[role] || "General";
    };

    const formatDate = (dateString) => {
      if (!dateString) return "N/A";
      try {
        const date = new Date(dateString);
        return date.toLocaleDateString("en-GB", {
          month: "short",
          day: "numeric",
          year: "numeric",
        });
      } catch (error) {
        return "Invalid Date";
      }
    };

    const formatDetailDate = (dateString) => {
      if (!dateString) return "N/A";
      try {
        const date = new Date(dateString);
        return date.toLocaleDateString("en-GB", {
          weekday: "long",
          year: "numeric",
          month: "long",
          day: "numeric",
        });
      } catch (error) {
        return "Invalid Date";
      }
    };

    // Component lifecycle
    onMounted(() => {
      loadStaffData();
    });

    return {
      loading,
      staffList,
      selectedStaff,
      staffDetailDialogVisible,
      profileLoading,
      staffProfile,
      // Performance data
      performanceData,
      performanceLoading,
      searchQuery,
      currentPage,
      pageSize,
      filteredStaffList,
      paginatedStaffList,
      startIndex,
      endIndex,
      totalStaff,
      janitorStaff,
      recentStaff,
      // Attendance Records
      attendanceRecords,
      attendanceLoading,
      attendanceRecordsLoaded,
      attendanceDateRange,
      attendanceCurrentPage,
      attendancePageSize,
      paginatedAttendanceRecords,
      // Task Management
      userTasks,
      tasksLoading,
      tasksLoaded,
      taskStatistics,
      taskStatusFilter,
      taskPriorityFilter,
      taskCurrentPage,
      taskPageSize,
      filteredTasks,
      paginatedTasks,
      paginatedUserTasks,
      loadStaffData,
      loadStaffProfile,
      refreshData,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      viewStaffDetails,
      resetStaffDetail,
      contactAdmin,
      // Performance functions
      loadPerformanceData,
      refreshPerformanceData,
      getAttendanceDetail,
      getTaskDetail,
      // Attendance Functions
      loadAttendanceRecords,
      formatAttendanceDate,
      formatAttendanceTime,
      calculateWorkHours,
      getAttendanceStatusType,
      resetAttendanceData,
      handleAttendanceSizeChange,
      handleAttendanceCurrentChange,
      // Task Functions
      loadTaskStatistics,
      loadUserTasks,
      handleTaskFilterChange,
      resetTaskData,
      getTaskStatusType,
      getTaskStatusText,
      getTaskPriorityType,
      formatTaskDate,
      formatTaskStatus,
      isTaskOverdue,
      handleTaskSizeChange,
      handleTaskCurrentChange,
      getAvatarUrl,
      getRoleType,
      getRoleText,
      getStatusType,
      getStatusText,
      getRoleIcon,
      getDepartment,
      formatDate,
      formatDetailDate,
    };
  },
};
</script>

<style scoped>
/* Professional Dark Theme for Staff Management */
.staff-management-container {
  max-width: 1600px;
  margin: 0 auto;
}

/* Staff Profile Header - Admin Style */
.staff-details {
  padding: 16px 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.detail-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.detail-info p {
  margin: 0 0 8px 0;
  color: #6b7280;
  font-size: 14px;
}

/* Page Header - Professional Dark Theme */
.page-header {
  background: linear-gradient(135deg, #1e293b 0%, #334155 50%, #475569 100%);
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(71, 85, 105, 0.3);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 2rem;
}

.header-text {
  flex: 1;
}

.page-title {
  font-size: 2.25rem;
  font-weight: 800;
  margin: 0 0 0.75rem 0;
  color: #f1f5f9;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.page-subtitle {
  font-size: 1.125rem;
  margin: 0;
  color: #cbd5e1;
  opacity: 0.9;
  line-height: 1.6;
}

.header-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 1rem;
}

.permission-badge {
  font-weight: 700;
  padding: 0.75rem 1.25rem;
  border-radius: 0.75rem;
  background: rgba(71, 85, 105, 0.3);
  border: 1px solid rgba(71, 85, 105, 0.5);
  color: #cbd5e1;
}

.restricted-btn {
  background: rgba(71, 85, 105, 0.3);
  border: 1px solid rgba(71, 85, 105, 0.5);
  color: #94a3b8;
  font-weight: 600;
}

/* Statistics Dashboard */
.stats-dashboard {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  border: none;
  border-radius: 1rem;
  transition: all 0.3s ease;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  padding: 0.5rem;
}

.stat-icon {
  width: 72px;
  height: 72px;
  border-radius: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 2rem;
  flex-shrink: 0;
}

.stat-total .stat-icon {
  background: linear-gradient(135deg, #1e293b, #334155);
}

.stat-janitors .stat-icon {
  background: linear-gradient(135deg, #059669, #10b981);
}

.stat-recent .stat-icon {
  background: linear-gradient(135deg, #d97706, #f59e0b);
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: 800;
  color: #1e293b;
  line-height: 1;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.25rem;
}

.stat-detail {
  font-size: 0.75rem;
  color: #94a3b8;
  font-weight: 500;
}

/* Filter Panel */
.filter-panel {
  margin-bottom: 1.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
}

.filter-left {
  display: flex;
  gap: 1rem;
  flex: 1;
  align-items: center;
}

.search-input {
  width: 320px;
}

.filter-right {
  display: flex;
  gap: 1rem;
}

.refresh-btn {
  font-weight: 600;
  border-radius: 0.75rem;
  padding: 0.75rem 1.25rem;
  background: linear-gradient(135deg, #059669, #10b981);
  border: none;
  color: white;
}

/* Permission Notice */
.permission-notice {
  margin-bottom: 1.5rem;
  border: 1px solid #64748b;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  border-radius: 1rem;
}

.notice-content {
  display: flex;
  align-items: center;
  gap: 1rem;
  color: #334155;
  font-weight: 500;
}

.notice-text {
  flex: 1;
}

/* Staff Table */
.staff-table-card {
  border: none;
  border-radius: 1rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 700;
  color: #1e293b;
  font-size: 1.25rem;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.access-tag {
  font-weight: 600;
}


/* Table Styling */
.staff-table {
  font-size: 0.95rem;
}

/* Fix sorting button position for date column - Element Plus override */
.staff-table :deep(.date-column .el-table__header .cell) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  position: relative !important;
  padding-right: 30px !important;
}

.staff-table :deep(.date-column .caret-wrapper) {
  position: absolute !important;
  right: 8px !important;
  top: 50% !important;
  transform: translateY(-50%) !important;
  height: 14px !important;
  width: 14px !important;
  display: flex !important;
  flex-direction: column !important;
  justify-content: space-between !important;
}

.staff-table :deep(.date-column .sort-caret.ascending) {
  top: 2px !important;
}

.staff-table :deep(.date-column .sort-caret.descending) {
  bottom: 2px !important;
}

.member-avatar {
  background: linear-gradient(135deg, #475569, #64748b);
  color: white;
  font-weight: 700;
  border: 2px solid #e2e8f0;
}

.username-cell .username {
  font-weight: 600;
  color: #1e293b;
  font-family: "Monaco", "Menlo", monospace;
}

.name-cell {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.full-name {
  font-weight: 700;
  color: #1e293b;
  font-size: 0.95rem;
}

.user-id {
  font-size: 0.75rem;
  color: #94a3b8;
  font-weight: 500;
  font-family: "Monaco", "Menlo", monospace;
}

.role-tag {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-weight: 600;
  padding: 0.375rem 0.75rem;
}

.email-cell,
.date-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #64748b;
}

.view-btn {
  background: linear-gradient(135deg, #334155, #475569);
  border: none;
  color: white;
  font-weight: 600;
  border-radius: 0.5rem;
  padding: 0.5rem 1rem;
}

.view-btn:hover {
  background: linear-gradient(135deg, #475569, #64748b);
  transform: translateY(-1px);
}

/* Pagination */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2rem;
  padding: 1rem 0;
}

.pagination-info {
  color: #64748b;
  font-weight: 500;
  font-size: 0.9rem;
}

.staff-pagination {
  justify-content: flex-end;
}

/* Staff Detail Modal - Enhanced Dialog Container */
.staff-detail-dialog :deep(.el-dialog) {
  border-radius: 1rem;
  box-shadow: 0 20px 80px rgba(0, 0, 0, 0.2);
  margin: 5vh auto;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.staff-detail-dialog :deep(.el-dialog__body) {
  flex: 1;
  overflow-y: auto;
  max-height: calc(90vh - 120px);
  padding: 1rem;
}

.staff-detail-modal {
  padding: 1rem 0;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}

.profile-section {
  display: block;
}

.profile-section-flex {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.profile-avatar {
  background: linear-gradient(135deg, #475569, #64748b);
  color: white;
  font-weight: 700;
  font-size: 2rem;
  border: 3px solid #e2e8f0;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.profile-info {
  flex: 1;
}

.profile-name {
  font-size: 1.75rem;
  font-weight: 800;
  margin: 0 0 0.5rem 0;
  color: #1e293b;
}

.profile-username {
  font-size: 1rem;
  color: #64748b;
  margin: 0 0 0.75rem 0;
  font-family: "Monaco", "Menlo", monospace;
  font-weight: 500;
}

.profile-role {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 700;
  padding: 0.5rem 1rem;
}

.profile-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

/* Optimized Profile Detail Grid - Two Column Layout (Consistent with Admin) */
.profile-detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  background: #fafafa;
}

/* Detail Section Styling (Supervisor Theme) */
.detail-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  font-size: 1.1rem;
  color: #1e293b;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #334155;
  margin-bottom: 1rem;
}

.detail-items {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  padding: 0.75rem 0;
  border-bottom: 1px solid #e5e7eb;
  gap: 0.5rem;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item label {
  font-weight: 500;
  color: #475569;
  font-size: 0.875rem;
}

.detail-item span {
  color: #1e293b;
  font-weight: 500;
  word-break: break-word;
}

/* Extended Sections Container - Unified Height Management */
.extended-sections {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  min-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
  flex: 1;
}

/* Dialog Optimization - Content-Aware Height */
.staff-detail-dialog :deep(.el-dialog) {
  margin: 3vh auto;
  min-height: 70vh;
  max-height: 95vh;
  display: flex;
  flex-direction: column;
  border-radius: 1rem;
  box-shadow: 0 20px 80px rgba(0, 0, 0, 0.2);
}

.staff-detail-dialog :deep(.el-dialog__body) {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
}

/* Content Flow Optimization */
.staff-details {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  flex: 1;
}

/* Performance Section Specific */
.performance-section {
  background: #f8fafc;
  border-radius: 1rem;
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
}

.performance-section .section-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.125rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 1.25rem 0;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 0.75rem;
}

/* Responsive Layout - Mobile First Approach */
@media (max-width: 768px) {
  .staff-detail-dialog :deep(.el-dialog) {
    width: 95% !important;
    margin: 2vh auto;
    min-height: 80vh;
    max-height: 96vh;
  }

  .extended-sections {
    min-height: 50vh;
    gap: 1rem;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .staff-detail-dialog :deep(.el-dialog) {
    width: 90% !important;
    margin: 3vh auto;
    min-height: 75vh;
    max-height: 94vh;
  }
}

@media (min-width: 1025px) {
  .staff-detail-dialog :deep(.el-dialog) {
    width: 900px !important;
    margin: 3vh auto;
    min-height: 70vh;
    max-height: 94vh;
  }

  .extended-sections {
    min-height: 65vh;
  }
}

@media (max-width: 1024px) {
  .staff-detail-dialog :deep(.el-dialog) {
    margin: 3vh auto;
    max-height: 94vh;
  }

  .staff-detail-dialog :deep(.el-dialog__body) {
    max-height: calc(94vh - 100px);
  }

  .extended-sections {
    max-height: calc(94vh - 180px);
  }
}

/* Performance Grid (Supervisor Interface - Vertical Layout per user request) */
.performance-grid {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.performance-metrics-row {
  display: flex;
  flex-direction: row;
  gap: 1rem;
  justify-content: space-between;
}

.performance-item {
  flex: 1;
  min-width: 200px;
  text-align: center;
  padding: 1.5rem;
  background: white;
  border-radius: 0.75rem;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.performance-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.performance-value {
  font-size: 2rem;
  font-weight: 800;
  color: #1e293b;
  line-height: 1;
  margin-bottom: 0.5rem;
}

.performance-label {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.performance-detail {
  font-size: 0.75rem;
  color: #94a3b8;
  margin-top: 0.25rem;
  font-style: italic;
}

.performance-loading {
  text-align: center;
  padding: 2rem;
  color: #64748b;
}

.performance-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  align-items: center;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

/* Attendance Records Styles */
.attendance-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.attendance-table-container {
  border-radius: 0.5rem;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.attendance-table {
  font-size: 0.875rem;
}

.attendance-table .no-data {
  color: #94a3b8;
  font-style: italic;
}

.no-records {
  text-align: center;
  padding: 2rem 1rem;
  color: #64748b;
}

/* Profile Loading State */
.profile-loading {
  padding: 2rem;
  text-align: center;
}

/* Modal Notice */
.modal-notice {
  margin-top: 2rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

/* Task Management Styles */
.task-section {
  grid-column: 1 / -1; /* Span full width */
  max-width: 100%;
  overflow: hidden;
}

.task-stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.task-stat-item {
  text-align: center;
  padding: 1rem;
  background: white;
  border-radius: 0.75rem;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.task-stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.task-stat-item.completed {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-color: #10b981;
}

.task-stat-item.pending {
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border-color: #f59e0b;
}

.task-stat-item.progress {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border-color: #3b82f6;
}

.task-stat-item.overdue {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  border-color: #ef4444;
}

.task-stat-value {
  font-size: 1.5rem;
  font-weight: 800;
  color: #1e293b;
  line-height: 1;
  margin-bottom: 0.25rem;
}

.task-stat-label {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.task-completion-rate {
  margin: 1.5rem 0;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 0.75rem;
  border: 1px solid #e2e8f0;
}

.task-completion-rate label {
  display: block;
  margin-bottom: 0.75rem;
  font-weight: 600;
  color: #374151;
}

.completion-percentage {
  display: inline-block;
  margin-left: 1rem;
  font-weight: 700;
  color: #1e293b;
}

.task-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  justify-content: flex-start;
}

.task-controls .el-select {
  min-width: 160px;
}

.task-list-container {
  border-radius: 0.75rem;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  background: white;
}

.tasks-table {
  font-size: 0.875rem;
}

.task-title-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.task-title {
  font-weight: 600;
  color: #1e293b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-tasks {
  text-align: center;
  padding: 2rem 1rem;
  color: #64748b;
  background: #f8fafc;
  border-radius: 0.75rem;
  border: 1px solid #e2e8f0;
}

.task-loading {
  text-align: center;
  padding: 2rem;
  color: #64748b;
  background: #f8fafc;
  border-radius: 0.75rem;
  border: 1px solid #e2e8f0;
}

/* Responsive Design - Enhanced Height Management */
@media (max-width: 1400px) {
  .staff-detail-dialog :deep(.el-dialog) {
    width: 90% !important;
    max-width: 900px;
    max-height: 88vh;
    margin: 6vh auto;
  }

  .staff-detail-dialog :deep(.el-dialog__body) {
    max-height: calc(88vh - 120px);
  }

  .extended-sections {
    max-height: calc(88vh - 200px);
  }
}

@media (max-width: 1200px) {
  .stats-dashboard {
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  }

  .filter-content {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }

  .filter-left {
    flex-wrap: wrap;
  }

  .search-input {
    width: 100%;
  }

  .task-stats-overview {
    grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  }

  .task-controls {
    justify-content: flex-start;
    gap: 0.75rem;
  }

  .task-controls .el-select {
    min-width: 140px;
  }

  .staff-detail-dialog :deep(.el-dialog) {
    width: 90% !important;
    max-height: 92vh;
    margin: 4vh auto;
  }

  .staff-detail-dialog :deep(.el-dialog__body) {
    max-height: calc(92vh - 100px);
  }

  .extended-sections {
    max-height: calc(92vh - 180px);
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
    gap: 1.5rem;
  }

  .header-actions {
    align-items: center;
  }

  .stats-dashboard {
    grid-template-columns: 1fr;
  }

  .profile-detail-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
    padding: 1rem;
  }

  .performance-grid {
    flex-direction: column;
    gap: 1rem;
  }

  .performance-metrics-row {
    flex-direction: column;
    gap: 0.75rem;
  }

  .pagination-container {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .staff-detail-dialog :deep(.el-dialog) {
    width: 95% !important;
    margin: 2vh auto;
    max-height: 96vh;
  }

  .staff-detail-dialog :deep(.el-dialog__body) {
    max-height: calc(96vh - 80px);
  }

  .extended-sections {
    max-height: calc(96vh - 160px);
  }

  .task-controls {
    flex-direction: column;
    align-items: stretch;
    gap: 0.75rem;
  }

  .task-controls > * {
    width: 100%;
  }

  .task-stats-overview {
    grid-template-columns: repeat(auto-fit, minmax(90px, 1fr));
  }
}
</style>
