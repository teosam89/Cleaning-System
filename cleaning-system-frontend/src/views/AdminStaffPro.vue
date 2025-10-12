<template>
  <AdminLayout>
    <div class="staff-profile-container">
      <!-- Page Header -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">Staff Management</h1>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="showAddStaffDialog">
            <el-icon><Plus /></el-icon>
            Add New Staff
          </el-button>
        </div>
      </div>

      <!-- Filters and Search -->
      <el-card class="filter-card" shadow="never">
        <div class="filter-section">
          <div class="filter-left">
            <el-input
              v-model="searchQuery"
              placeholder="Search by name, username, or email"
              prefix-icon="Search"
              clearable
              class="search-input"
              @input="handleSearch"
            />
          </div>
          <div class="filter-right">
            <el-button @click="refreshData">
              <el-icon><Refresh /></el-icon>
              Refresh
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- Staff Statistics -->
      <div class="stats-row">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon size="24"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalStaff }}</div>
              <div class="stat-label">Total Staff</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon janitor">
              <el-icon size="24"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ janitorStaff }}</div>
              <div class="stat-label">Janitors</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon supervisor">
              <el-icon size="24"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ supervisorStaff }}</div>
              <div class="stat-label">Supervisors</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon recent">
              <el-icon size="24"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ recentStaff }}</div>
              <div class="stat-label">New (30 days)</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- Staff Table -->
      <el-card class="table-card" shadow="never">
        <template #header>
          <div class="table-header">
            <span class="table-title"
              >Staff List ({{ filteredStaffList.length }})</span
            >
            <div class="table-actions">
              <el-button
                size="small"
                :disabled="selectedStaffIds.length === 0"
                @click="bulkUpdateStatus"
              >
                Bulk Update Status
              </el-button>
              <el-button
                size="small"
                type="danger"
                :disabled="selectedStaffIds.length === 0"
                @click="bulkDelete"
              >
                Bulk Delete
              </el-button>
            </div>
          </div>
        </template>

        <el-table
          v-loading="loading"
          :data="paginatedStaffList"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
          :default-sort="{ prop: 'createdAt', order: 'descending' }"
          stripe
          style="width: 100%"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="avatar" label="Avatar" width="80">
            <template #default="{ row }">
              <el-avatar :size="40" :src="row.avatarUrl || row.avatar">
                {{ row.fullName.charAt(0) }}
              </el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="Username" width="120" sortable="custom" />
          <el-table-column prop="fullName" label="Full Name" min-width="150" sortable="custom" />
          <el-table-column prop="role" label="Role" width="120" sortable="custom">
            <template #default="{ row }">
              <el-tag :type="getRoleType(row.role)" size="small">
                {{ getRoleText(row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="Created" width="120" sortable="custom">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="Actions" width="200" fixed="right">
            <template #default="{ row }">
              <el-button-group>
                <el-button size="small" @click="viewStaffDetails(row)">
                  <el-icon><View /></el-icon>
                </el-button>
                <el-button size="small" type="primary" @click="editStaff(row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button size="small" type="danger" @click="deleteStaff(row)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>

        <!-- Pagination -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="filteredStaffList.length"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>

      <!-- Add/Edit Staff Dialog -->
      <el-dialog
        v-model="staffDialogVisible"
        :title="isEditing ? 'Edit Staff' : 'Add New Staff'"
        width="600px"
        @closed="resetStaffForm"
      >
        <el-form
          ref="staffFormRef"
          :model="staffForm"
          :rules="staffFormRules"
          label-width="120px"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Full Name" prop="fullName">
                <el-input v-model="staffForm.fullName" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Username" prop="username">
                <el-input v-model="staffForm.username" :disabled="isEditing" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="Email" prop="email">
            <el-input v-model="staffForm.email" type="email" />
          </el-form-item>

          <el-form-item label="Role" prop="role">
            <el-radio-group v-model="staffForm.role">
              <el-radio label="janitor">Janitor</el-radio>
              <el-radio label="supervisor">Supervisor</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item v-if="!isEditing" label="Password" prop="password">
            <el-input
              v-model="staffForm.password"
              type="password"
              show-password
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="staffDialogVisible = false">Cancel</el-button>
            <el-button type="primary" @click="saveStaff" :loading="saving">
              {{ isEditing ? "Update" : "Create" }}
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Enhanced Staff Details Dialog -->
      <el-dialog
        v-model="detailsDialogVisible"
        title="Staff Profile Details"
        width="900px"
        class="staff-profile-dialog"
        :close-on-click-modal="false"
        :destroy-on-close="true"
        top="5vh"
      >
        <div v-if="selectedStaff" class="staff-details">
          <!-- Profile Header -->
          <div class="detail-header">
            <el-avatar
              :size="80"
              :src="staffProfile?.avatarUrl || selectedStaff.avatar"
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
                        ? formatDate(staffProfile.birthDate)
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
                        ? formatDate(staffProfile.joinDate)
                        : formatDate(selectedStaff.createdAt)
                    }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Extended Information Sections -->
            <div class="extended-sections">
              <!-- Personal Information -->
              <el-card
                class="profile-section"
                shadow="never"
                v-if="staffProfile"
              >
                <template #header>
                  <div class="section-header">
                    <el-icon><User /></el-icon>
                    <span>Additional Information</span>
                  </div>
                </template>
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="Role">
                    {{ getRoleText(selectedStaff.role) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="Status">
                    <el-tag
                      :type="
                        staffProfile?.status === 'active'
                          ? 'success'
                          : 'warning'
                      "
                    >
                      {{ staffProfile?.status || "Active" }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="Account Created">
                    {{ formatDateTime(selectedStaff.createdAt) }}
                  </el-descriptions-item>
                  <el-descriptions-item
                    label="Account Age"
                    v-if="staffProfile && staffProfile.performanceInsights"
                  >
                    {{ staffProfile.performanceInsights.accountAge }} days
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>


              <!-- Enhanced Performance Metrics (Real-time Calculations) -->
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

                <!-- Performance Loading State -->
                <div v-if="performanceLoading" class="performance-loading">
                  <el-skeleton :rows="2" animated />
                  <p>Calculating real-time performance metrics...</p>
                </div>

                <!-- Performance Actions -->
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

              <!-- Task Statistics -->
              <el-card
                class="profile-section"
                shadow="never"
                v-if="staffProfile && staffProfile.taskStatistics"
              >
                <template #header>
                  <div class="section-header">
                    <el-icon><Document /></el-icon>
                    <span>Task Statistics</span>
                  </div>
                </template>
                <div class="stats-grid">
                  <div class="stat-item">
                    <div class="stat-value">
                      {{ staffProfile.taskStatistics.totalTasks }}
                    </div>
                    <div class="stat-label">Total Tasks</div>
                  </div>
                  <div class="stat-item success">
                    <div class="stat-value">
                      {{ staffProfile.taskStatistics.completedTasks }}
                    </div>
                    <div class="stat-label">Completed</div>
                  </div>
                  <div class="stat-item warning">
                    <div class="stat-value">
                      {{ staffProfile.taskStatistics.pendingTasks }}
                    </div>
                    <div class="stat-label">Pending</div>
                  </div>
                  <div class="stat-item info">
                    <div class="stat-value">
                      {{ staffProfile.taskStatistics.inProgressTasks }}
                    </div>
                    <div class="stat-label">In Progress</div>
                  </div>
                </div>
                <div class="completion-rate">
                  <label>Completion Rate:</label>
                  <el-progress
                    :percentage="staffProfile.taskStatistics.completionRate"
                    :stroke-width="8"
                    :color="
                      staffProfile.taskStatistics.completionRate >= 80
                        ? '#67C23A'
                        : staffProfile.taskStatistics.completionRate >= 60
                        ? '#E6A23C'
                        : '#F56C6C'
                    "
                  />
                </div>
              </el-card>

              <!-- Recent Tasks -->
              <el-card
                class="profile-section"
                shadow="never"
                v-if="staffProfile && staffProfile.recentTasks"
              >
                <template #header>
                  <div class="section-header">
                    <el-icon><List /></el-icon>
                    <span>Recent Tasks (Last 5)</span>
                  </div>
                </template>
                <div v-if="staffProfile.recentTasks.length > 0">
                  <el-table
                    :data="staffProfile.recentTasks"
                    size="small"
                    stripe
                  >
                    <el-table-column
                      prop="title"
                      label="Task"
                      min-width="150"
                    />
                    <el-table-column prop="status" label="Status" width="100">
                      <template #default="{ row }">
                        <el-tag
                          :type="getTaskStatusType(row.status)"
                          size="small"
                        >
                          {{ getTaskStatusText(row.status) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column
                      prop="priority"
                      label="Priority"
                      width="100"
                    >
                      <template #default="{ row }">
                        <el-tag
                          :type="getPriorityType(row.priority)"
                          size="small"
                        >
                          {{ row.priority || "Normal" }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column
                      prop="progressPercentage"
                      label="Progress"
                      width="120"
                    >
                      <template #default="{ row }">
                        <el-progress
                          :percentage="row.progressPercentage || 0"
                          :stroke-width="6"
                        />
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
                <div v-else class="no-data">
                  <el-empty description="No recent tasks" :image-size="80" />
                </div>
              </el-card>

              <!-- Attendance Data -->
              <el-card
                class="profile-section"
                shadow="never"
                v-if="staffProfile && staffProfile.attendanceData"
              >
                <template #header>
                  <div class="section-header">
                    <el-icon><Clock /></el-icon>
                    <span>Attendance Information</span>
                  </div>
                </template>
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="Today's Status">
                    <el-tag
                      :type="
                        staffProfile.attendanceData.todayAttendance
                          ? 'success'
                          : 'info'
                      "
                    >
                      {{
                        staffProfile.attendanceData.todayAttendance
                          ? "Checked In"
                          : "Not Checked In"
                      }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="Recent Attendance">
                    {{ staffProfile.attendanceData.recentAttendanceCount }} days
                    (last 7 days)
                  </el-descriptions-item>
                  <el-descriptions-item label="Weekly Hours" span="2">
                    <strong
                      >{{
                        staffProfile.attendanceData.totalWeeklyHours
                      }}
                      hours</strong
                    >
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>

              <!-- Task Records Section -->
              <el-card
                class="profile-section"
                shadow="never"
                v-if="
                  staffProfile &&
                  (selectedStaff.role === 'janitor' ||
                    selectedStaff.role === 'cleaner')
                "
              >
                <template #header>
                  <div class="section-header">
                    <el-icon><List /></el-icon>
                    <span>Task Records</span>
                    <el-tag
                      type="success"
                      size="small"
                      style="margin-left: 10px"
                    >
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
                    @change="filterTasks"
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
                    <el-table-column
                      prop="title"
                      label="Task Title"
                      min-width="150"
                    />
                    <el-table-column prop="status" label="Status" width="120">
                      <template #default="{ row }">
                        <el-tag
                          :type="getTaskStatusType(row.status)"
                          size="small"
                        >
                          {{ formatTaskStatus(row.status) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column
                      prop="priority"
                      label="Priority"
                      width="100"
                    >
                      <template #default="{ row }">
                        <el-tag
                          :type="getTaskPriorityType(row.priority)"
                          size="small"
                        >
                          {{ row.priority }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column
                      prop="assignedDate"
                      label="Assigned"
                      width="120"
                    >
                      <template #default="{ row }">
                        {{ formatTaskDate(row.assignedDate) }}
                      </template>
                    </el-table-column>
                    <el-table-column
                      prop="dueDate"
                      label="Due Date"
                      width="120"
                    >
                      <template #default="{ row }">
                        <span
                          :class="{
                            'text-danger': isTaskOverdue(
                              row.dueDate,
                              row.status
                            ),
                          }"
                        >
                          {{ formatTaskDate(row.dueDate) }}
                        </span>
                      </template>
                    </el-table-column>
                    <el-table-column
                      prop="completedDate"
                      label="Completed"
                      width="120"
                    >
                      <template #default="{ row }">
                        {{
                          row.completedDate
                            ? formatTaskDate(row.completedDate)
                            : "-"
                        }}
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
                <div
                  v-else-if="!tasksLoading && userTasks.length === 0"
                  class="no-data"
                >
                  <el-empty
                    description="No tasks found for this staff member"
                    :image-size="60"
                  />
                </div>

                <!-- Tasks Loading -->
                <div v-else-if="tasksLoading" class="loading-state">
                  <el-skeleton :rows="3" animated />
                  <p style="text-align: center; margin-top: 10px; color: #666">
                    Loading task records...
                  </p>
                </div>
              </el-card>

              <!-- Attendance Records Section -->
              <el-card
                class="profile-section"
                shadow="never"
                v-if="staffProfile"
              >
                <template #header>
                  <div class="section-header">
                    <el-icon><Clock /></el-icon>
                    <span>Attendance Records</span>
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
                    View Records
                  </el-button>
                  <el-date-picker
                    v-model="attendanceDateRange"
                    type="daterange"
                    range-separator="To"
                    start-placeholder="Start date"
                    end-placeholder="End date"
                    size="default"
                    style="margin-left: 10px"
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
                    <el-table-column
                      prop="workDate"
                      label="Date"
                      width="120"
                      sortable
                    >
                      <template #default="{ row }">
                        <span>{{ formatAttendanceDate(row.workDate) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column
                      prop="checkInTime"
                      label="Check In"
                      width="100"
                    >
                      <template #default="{ row }">
                        <span v-if="row.checkInTime">{{
                          formatAttendanceTime(row.checkInTime)
                        }}</span>
                        <span v-else class="no-data">--</span>
                      </template>
                    </el-table-column>
                    <el-table-column
                      prop="checkOutTime"
                      label="Check Out"
                      width="100"
                    >
                      <template #default="{ row }">
                        <span v-if="row.checkOutTime">{{
                          formatAttendanceTime(row.checkOutTime)
                        }}</span>
                        <span v-else class="no-data">--</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="workHours" label="Hours" width="120">
                      <template #default="{ row }">
                        {{
                          calculateWorkHours(row.checkInTime, row.checkOutTime)
                        }}
                      </template>
                    </el-table-column>
                  </el-table>

                  <!-- Attendance Pagination -->
                  <el-pagination
                    v-model:current-page="attendanceCurrentPage"
                    v-model:page-size="attendancePageSize"
                    :page-sizes="[5, 10, 20]"
                    :total="attendanceRecords.length"
                    layout="sizes, prev, pager, next"
                    style="margin-top: 15px; text-align: center"
                    small
                  />
                </div>

                <!-- No Records Message -->
                <div
                  v-else-if="!attendanceLoading && attendanceRecordsLoaded"
                  class="no-records"
                >
                  <el-empty
                    description="No attendance records found for this period"
                    :image-size="80"
                  />
                </div>
              </el-card>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script>
import { ref, reactive, computed, onMounted } from "vue";
import AdminLayout from "../components/AdminLayout.vue";
import { API } from "@/utils/request";
import {
  Plus,
  Refresh,
  User,
  CircleCheck,
  Clock,
  View,
  Edit,
  Delete,
  TrendCharts,
  Document,
  List,
  Search,
  QuestionFilled,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";

export default {
  name: "StaffProfile",
  components: {
    AdminLayout,
    Plus,
    Refresh,
    User,
    CircleCheck,
    Clock,
    View,
    Edit,
    Delete,
    TrendCharts,
    Document,
    List,
    Search,
    QuestionFilled,
  },
  setup() {
    // Reactive data
    const loading = ref(false);
    const saving = ref(false);
    const searchQuery = ref("");
    const currentPage = ref(1);
    const pageSize = ref(20);
    const selectedStaffIds = ref([]);

    // Sorting data
    const sortColumn = ref("createdAt");
    const sortOrder = ref("descending"); // 'ascending' or 'descending'

    // Dialog states
    const staffDialogVisible = ref(false);
    const detailsDialogVisible = ref(false);
    const isEditing = ref(false);
    const selectedStaff = ref(null);
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

    // Task Records
    const userTasks = ref([]);
    const tasksLoading = ref(false);
    const taskStatusFilter = ref("");
    const taskCurrentPage = ref(1);
    const taskPageSize = ref(10);

    // Form refs
    const staffFormRef = ref(null);

    // Available roles
    const roles = ref(["janitor", "supervisor"]);

    // Staff data - will be loaded from API
    const staffList = ref([]);

    // Staff form
    const staffForm = reactive({
      fullName: "",
      username: "",
      email: "",
      role: "janitor",
      password: "",
    });

    // Form validation rules
    const staffFormRules = {
      fullName: [
        { required: true, message: "Please enter full name", trigger: "blur" },
      ],
      username: [
        {
          required: true,
          message: "Please enter username",
          trigger: "blur",
        },
        {
          min: 3,
          max: 50,
          message: "Username must be between 3 and 50 characters",
          trigger: "blur",
        },
      ],
      email: [
        { required: true, message: "Please enter email", trigger: "blur" },
        { type: "email", message: "Please enter valid email", trigger: "blur" },
      ],
      role: [
        {
          required: true,
          message: "Please select role",
          trigger: "change",
        },
      ],
      password: [
        { required: true, message: "Please enter password", trigger: "blur" },
        {
          min: 6,
          message: "Password must be at least 6 characters",
          trigger: "blur",
        },
      ],
    };

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

      // Apply sorting
      if (sortColumn.value && filtered.length > 0) {
        filtered = [...filtered].sort((a, b) => {
          let aValue, bValue;

          // Handle different column types
          switch (sortColumn.value) {
            case 'username':
              aValue = (a.username || '').toLowerCase();
              bValue = (b.username || '').toLowerCase();
              break;
            case 'fullName':
              aValue = (a.fullName || '').toLowerCase();
              bValue = (b.fullName || '').toLowerCase();
              break;
            case 'role':
              // Role priority: admin > supervisor > janitor
              const rolePriority = { admin: 3, supervisor: 2, janitor: 1, cleaner: 1 };
              aValue = rolePriority[a.role] || 0;
              bValue = rolePriority[b.role] || 0;
              break;
            case 'createdAt':
              aValue = new Date(a.createdAt || 0).getTime();
              bValue = new Date(b.createdAt || 0).getTime();
              break;
            default:
              aValue = a[sortColumn.value] || '';
              bValue = b[sortColumn.value] || '';
          }

          // Compare values
          let comparison = 0;
          if (aValue > bValue) comparison = 1;
          else if (aValue < bValue) comparison = -1;

          // Apply sort order
          return sortOrder.value === 'ascending' ? comparison : -comparison;
        });
      }

      return filtered;
    });

    const paginatedStaffList = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value;
      const end = start + pageSize.value;
      return filteredStaffList.value.slice(start, end);
    });

    const totalStaff = computed(() => staffList.value.length);
    const janitorStaff = computed(
      () => staffList.value.filter((staff) => staff.role === "janitor").length
    );
    const supervisorStaff = computed(
      () =>
        staffList.value.filter((staff) => staff.role === "supervisor").length
    );
    const recentStaff = computed(
      () =>
        staffList.value.filter((staff) => {
          const created = new Date(staff.createdAt);
          const now = new Date();
          const daysDiff = (now - created) / (1000 * 60 * 60 * 24);
          return daysDiff <= 30; // Staff added in last 30 days
        }).length
    );

    // Attendance computed properties
    const paginatedAttendanceRecords = computed(() => {
      const start =
        (attendanceCurrentPage.value - 1) * attendancePageSize.value;
      const end = start + attendancePageSize.value;
      return attendanceRecords.value.slice(start, end);
    });

    // Task computed properties
    const filteredTasks = computed(() => {
      if (!taskStatusFilter.value) {
        return userTasks.value;
      }
      return userTasks.value.filter(
        (task) => task.status === taskStatusFilter.value
      );
    });

    const paginatedTasks = computed(() => {
      const start = (taskCurrentPage.value - 1) * taskPageSize.value;
      const end = start + taskPageSize.value;
      return filteredTasks.value.slice(start, end);
    });

    // Methods

    const handleSearch = () => {
      currentPage.value = 1;
    };

    // Sorting functionality
    const handleSortChange = ({ column, prop, order }) => {
      console.log("Sort changed:", { column, prop, order });

      if (order === null) {
        // Reset to default sort (newest first)
        sortColumn.value = "createdAt";
        sortOrder.value = "descending";
      } else {
        sortColumn.value = prop;
        sortOrder.value = order;
      }

      // Reset to first page when sorting
      currentPage.value = 1;
    };

    const handleSelectionChange = (selection) => {
      selectedStaffIds.value = selection.map((item) => item.id);
    };

    const handleSizeChange = (size) => {
      pageSize.value = size;
      currentPage.value = 1;
    };

    const handleCurrentChange = (page) => {
      currentPage.value = page;
    };

    const showAddStaffDialog = () => {
      isEditing.value = false;
      staffDialogVisible.value = true;
    };

    const editStaff = (staff) => {
      isEditing.value = true;
      Object.assign(staffForm, {
        id: staff.userId,
        fullName: staff.fullName,
        username: staff.username,
        email: staff.email,
        role: staff.role,
        password: "", // Don't populate password for editing
      });
      staffDialogVisible.value = true;
    };

    const viewStaffDetails = async (staff) => {
      selectedStaff.value = staff;
      staffProfile.value = null;
      resetAttendanceData();
      resetTaskData();
      detailsDialogVisible.value = true;

      // Load comprehensive profile data
      await loadStaffProfile(staff.userId);

      // Load performance data (only for janitors and cleaners)
      if (staff.role === "janitor" || staff.role === "cleaner") {
        await loadPerformanceData(staff.userId);
      }
    };

    const loadStaffProfile = async (userId) => {
      if (!userId) {
        console.warn("No user ID provided for profile loading");
        return;
      }

      try {
        profileLoading.value = true;
        console.log("Loading comprehensive profile for user:", userId);

        const response = await API.get(`/api/admin/staff/${userId}/profile`);
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

    const resetStaffForm = () => {
      Object.assign(staffForm, {
        fullName: "",
        username: "",
        email: "",
        role: "janitor",
        password: "",
      });
      if (staffFormRef.value) {
        staffFormRef.value.clearValidate();
      }
    };

    const saveStaff = async () => {
      if (!staffFormRef.value) return;

      try {
        await staffFormRef.value.validate();
        saving.value = true;

        if (isEditing.value) {
          // Update existing staff via API
          const staffData = {
            email: staffForm.email,
            fullName: staffForm.fullName,
            role: staffForm.role,
          };

          try {
            const response = await API.put(
              `/api/janitors/${staffForm.id}`,
              staffData
            );

            if (response.data && response.data.success) {
              // Refresh staff list after update
              await loadStaffData();
              ElMessage.success("Staff updated successfully");
            } else {
              throw new Error(response.data?.message || "Update failed");
            }
          } catch (apiError) {
            console.error("API update failed:", apiError);
            ElMessage.error(
              apiError.response?.data?.message ||
                "Failed to update staff. Please try again."
            );
            return; // Don't close dialog on error
          }
        } else {
          // Create new staff via API
          const staffData = {
            username: staffForm.username,
            password: staffForm.password,
            email: staffForm.email,
            fullName: staffForm.fullName,
            role: staffForm.role,
          };

          try {
            const response = await API.post("/api/janitors", staffData);

            if (response.data && response.data.success) {
              // Refresh staff list after creation
              await loadStaffData();
              ElMessage.success(
                "Staff created successfully. New user can now login with username: " +
                  staffForm.username
              );
            } else {
              throw new Error(response.data?.message || "Creation failed");
            }
          } catch (apiError) {
            console.error("API creation failed:", apiError);
            ElMessage.error(
              apiError.response?.data?.message ||
                "Failed to create staff. Please try again."
            );
            return; // Don't close dialog on error
          }
        }

        staffDialogVisible.value = false;
        resetStaffForm();
      } catch (error) {
        console.error("Form validation failed:", error);
      } finally {
        saving.value = false;
      }
    };

    const toggleStaffStatus = async () => {
      ElMessage.info("Staff status management feature coming soon");
    };

    const deleteStaff = async (staff) => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete ${staff.fullName}? This action cannot be undone.`,
          "Delete Staff",
          {
            confirmButtonText: "Delete",
            cancelButtonText: "Cancel",
            type: "error",
          }
        );

        try {
          // Call API to delete staff
          await API.delete(`/api/janitors/${staff.userId}`);

          // Refresh staff list after deletion
          await loadStaffData();
          ElMessage.success("Staff deleted successfully");
        } catch (apiError) {
          console.error("Failed to delete staff:", apiError);
          ElMessage.error(
            apiError.response?.data?.message ||
              "Failed to delete staff. Please try again."
          );
        }
      } catch {
        // User cancelled
      }
    };

    const bulkUpdateStatus = async () => {
      // Implementation for bulk status update
      ElMessage.info("Bulk update feature coming soon");
    };

    const bulkDelete = async () => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete ${selectedStaffIds.value.length} selected staff members?`,
          "Bulk Delete",
          {
            confirmButtonText: "Delete",
            cancelButtonText: "Cancel",
            type: "error",
          }
        );

        staffList.value = staffList.value.filter(
          (staff) => !selectedStaffIds.value.includes(staff.id)
        );
        selectedStaffIds.value = [];
        ElMessage.success("Selected staff deleted successfully");
      } catch {
        // User cancelled
      }
    };

    const loadStaffData = async () => {
      try {
        loading.value = true;
        const response = await API.get("/api/janitors");

        if (response.data && Array.isArray(response.data)) {
          // Map API data to match our frontend structure
          staffList.value = response.data.map((user) => ({
            userId: user.userId,
            username: user.username,
            fullName: user.fullName || user.username,
            phone: "", // This field isn't in our User entity yet
            email: user.email || "",
            role: user.role,
            createdAt: user.createdAt || new Date().toISOString(),
            avatar: user.avatarUrl || user.avatar,
            avatarUrl: user.avatarUrl || user.avatar,
          }));

          console.log("Staff data loaded:", staffList.value.length, "users");
        } else {
          console.warn("No staff data received from API");
          ElMessage.warning("No staff data found");
        }
      } catch (error) {
        console.error("Failed to load staff data from API:", error);
        ElMessage.error("Failed to load staff data. Please try again.");
      } finally {
        loading.value = false;
      }
    };

    const refreshData = async () => {
      await loadStaffData();
    };

    // Utility methods
    const getRoleType = (role) => {
      const types = {
        janitor: "info",
        supervisor: "warning",
      };
      return types[role] || "info";
    };

    const getRoleText = (role) => {
      const texts = {
        janitor: "Janitor",
        supervisor: "Supervisor",
      };
      return texts[role] || role;
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

    // Task status utility functions
    const getTaskStatusType = (status) => {
      const types = {
        completed: "success",
        in_progress: "primary",
        pending: "warning",
        overdue: "danger",
      };
      return types[status] || "info";
    };

    const getTaskStatusText = (status) => {
      const texts = {
        completed: "Completed",
        in_progress: "In Progress",
        pending: "Pending",
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

    const formatDate = (date) => {
      return new Date(date).toLocaleDateString();
    };

    const formatDateTime = (date) => {
      if (!date) return "N/A";
      return new Date(date).toLocaleString();
    };

    // Task-related methods
    const loadUserTasks = async () => {
      if (!selectedStaff.value?.userId) {
        ElMessage.warning("No staff member selected");
        return;
      }

      try {
        tasksLoading.value = true;
        console.log("Loading tasks for user:", selectedStaff.value.userId);

        const response = await API.get(
          `/api/admin/staff/${selectedStaff.value.userId}/tasks`
        );

        if (
          response.data &&
          response.data.success &&
          Array.isArray(response.data.tasks)
        ) {
          // Enhanced data validation and safe mapping
          userTasks.value = response.data.tasks
            .filter((task) => task && task.taskId) // Filter out invalid tasks
            .map((task) => ({
              taskId: task.taskId || 0,
              title: task.title || "Untitled Task",
              description: task.description || "",
              status: task.status || "pending",
              priority: task.priority || "normal",
              assignedDate: task.createdAt || task.assignedDate || "",
              dueDate: task.dueDate || "",
              completedDate: task.completedAt || task.completedDate || "",
              assignedBy: task.assignedBy || null,
              assignedByName: task.assignedByName || "Unknown",
              assignedToName: task.assignedToName || "Unassigned",
              category: task.category || "General",
              location: task.location || "",
              scheduledTime: task.scheduledTime || "",
              startedAt: task.startedAt || "",
              progressPercentage: task.progressPercentage || 0,
              estimatedDuration: task.estimatedDuration || 0,
            }));

          console.log(
            "Successfully loaded tasks:",
            userTasks.value.length,
            "tasks"
          );
          if (userTasks.value.length === 0) {
            ElMessage.info("No tasks found for this staff member");
          }
        } else {
          console.warn("Invalid response format:", response.data);
          userTasks.value = [];
          ElMessage.warning("Invalid task data received from server");
        }
      } catch (error) {
        console.error("Error loading user tasks:", error);

        // Enhanced error handling with specific messages
        if (error.response) {
          const status = error.response.status;
          const message = error.response.data?.message || "Unknown error";

          if (status === 404) {
            ElMessage.warning("User not found or has no tasks assigned");
          } else if (status === 403) {
            ElMessage.error("Access denied. Admin privileges required");
          } else if (status === 500) {
            ElMessage.error(
              "Server error while loading tasks. Please try again"
            );
          } else {
            ElMessage.error(`Failed to load task records: ${message}`);
          }
        } else if (error.request) {
          ElMessage.error("Network error. Please check your connection");
        } else {
          ElMessage.error("Failed to load task records. Please try again");
        }

        userTasks.value = [];
      } finally {
        tasksLoading.value = false;
      }
    };

    const filterTasks = () => {
      taskCurrentPage.value = 1;
    };

    const handleTaskSizeChange = (val) => {
      taskPageSize.value = val;
      taskCurrentPage.value = 1;
    };

    const handleTaskCurrentChange = (val) => {
      taskCurrentPage.value = val;
    };

    const resetTaskData = () => {
      userTasks.value = [];
      taskStatusFilter.value = "";
      taskCurrentPage.value = 1;
    };

    // Additional task utility functions (missing from return)
    const formatTaskStatus = (status) => {
      const statusMap = {
        pending: "Pending",
        in_progress: "In Progress",
        completed: "Completed",
        cancelled: "Cancelled",
        overdue: "Overdue",
      };
      return statusMap[status] || status;
    };

    const getTaskPriorityType = (priority) => {
      const types = {
        low: "info",
        normal: "",
        medium: "warning",
        high: "warning",
        urgent: "danger",
      };
      return types[priority] || "";
    };

    const formatTaskDate = (dateString) => {
      if (!dateString) return "--";
      const date = new Date(dateString);
      return date.toLocaleDateString("en-GB", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
      });
    };

    const isTaskOverdue = (dueDate, status) => {
      if (!dueDate || status === "completed") return false;
      const due = new Date(dueDate);
      const now = new Date();
      return due < now;
    };

    // Attendance-related methods
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

        if (response.data && Array.isArray(response.data)) {
          attendanceRecords.value = response.data;
          attendanceRecordsLoaded.value = true;
          console.log(
            "Attendance records loaded:",
            response.data.length,
            "records"
          );

          if (response.data.length === 0) {
            ElMessage.info(
              "No attendance records found for the selected period"
            );
          }
        } else {
          console.warn("No attendance data received from API");
          attendanceRecords.value = [];
          attendanceRecordsLoaded.value = true;
          ElMessage.info("No attendance records found");
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

    const resetAttendanceData = () => {
      attendanceRecords.value = [];
      attendanceRecordsLoaded.value = false;
      attendanceDateRange.value = [];
      attendanceCurrentPage.value = 1;
    };

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
          `/api/admin/staff/${userId}/performance`
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

    onMounted(() => {
      // Load initial staff data from API
      loadStaffData();
    });

    return {
      // Data
      loading,
      saving,
      searchQuery,
      currentPage,
      pageSize,
      selectedStaffIds,
      staffDialogVisible,
      detailsDialogVisible,
      isEditing,
      selectedStaff,
      profileLoading,
      staffProfile,

      // Sorting data
      sortColumn,
      sortOrder,

      // Performance data
      performanceData,
      performanceLoading,
      staffFormRef,
      roles,
      staffList,
      staffForm,
      staffFormRules,

      // Attendance data
      attendanceRecords,
      attendanceLoading,
      attendanceRecordsLoaded,
      attendanceDateRange,
      attendanceCurrentPage,
      attendancePageSize,

      // Task Records data
      userTasks,
      tasksLoading,
      taskStatusFilter,
      taskCurrentPage,
      taskPageSize,

      // Computed
      filteredStaffList,
      paginatedStaffList,
      paginatedAttendanceRecords,
      filteredTasks,
      paginatedTasks,
      totalStaff,
      janitorStaff,
      supervisorStaff,
      recentStaff,

      // Methods
      handleSearch,
      handleSortChange,
      handleSelectionChange,
      handleSizeChange,
      handleCurrentChange,
      showAddStaffDialog,
      editStaff,
      viewStaffDetails,
      loadStaffProfile,
      resetStaffForm,
      saveStaff,
      toggleStaffStatus,
      deleteStaff,
      bulkUpdateStatus,
      bulkDelete,
      loadStaffData,
      refreshData,
      getRoleType,
      getRoleText,
      getStatusType,
      getStatusText,
      getTaskStatusType,
      getTaskStatusText,
      getPriorityType,
      getDepartment,
      formatDate,
      formatDateTime,

      // Performance functions
      loadPerformanceData,
      refreshPerformanceData,
      getAttendanceDetail,
      getTaskDetail,

      // Task methods
      loadUserTasks,
      filterTasks,
      handleTaskSizeChange,
      handleTaskCurrentChange,
      resetTaskData,
      formatTaskStatus,
      getTaskPriorityType,
      formatTaskDate,
      isTaskOverdue,

      // Attendance methods
      loadAttendanceRecords,
      resetAttendanceData,
      formatAttendanceDate,
      formatAttendanceTime,
      calculateWorkHours,
      getAttendanceStatusType,
    };
  },
};
</script>

<style scoped>
/* Main container */
.staff-profile-container {
  width: 100%;
}

/* Page header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* Filter section */
.filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.filter-left {
  display: flex;
  gap: 12px;
  flex: 1;
}

.search-input {
  width: 300px;
}

.filter-right {
  display: flex;
  gap: 12px;
}

/* Stats section */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.total {
  background: #3b82f6;
}

.stat-icon.janitor {
  background: #10b981;
}

.stat-icon.supervisor {
  background: #f59e0b;
}

.stat-icon.recent {
  background: #3b82f6;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

/* Table section */
.table-card {
  border-radius: 12px;
  border: none;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.table-actions {
  display: flex;
  gap: 8px;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* Dialog styles */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Staff details */
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

/* Enhanced Staff Profile Dialog - Optimized for Content Display */
.staff-profile-dialog .el-dialog__body {
  padding: 1rem;
  overflow-y: auto;
}

.profile-loading {
  padding: 1rem 0;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.profile-section {
  border-radius: 0.75rem;
  border: 1px solid #e5e7eb;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #374151;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  margin-bottom: 1rem;
}

.stat-item {
  text-align: center;
  padding: 1rem;
  border-radius: 0.5rem;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
}

.stat-item.success {
  background: #f0f9ff;
  border-color: #10b981;
}

.stat-item.warning {
  background: #fffbeb;
  border-color: #f59e0b;
}

.stat-item.info {
  background: #eff6ff;
  border-color: #3b82f6;
}

.stat-value {
  font-size: 1.875rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.75rem;
  color: #6b7280;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.completion-rate {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}

.completion-rate label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
}

.no-data {
  padding: 2rem;
  text-align: center;
}

/* Optimized Profile Detail Grid - Two Column Layout */
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

/* Detail Section Styling */
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
  color: #374151;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #10b981;
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
  color: #6b7280;
  font-size: 0.875rem;
}

.detail-item span {
  color: #1f2937;
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
.staff-profile-dialog :deep(.el-dialog) {
  margin: 3vh auto;
  min-height: 70vh;
  max-height: 95vh;
  display: flex;
  flex-direction: column;
  border-radius: 1rem;
  box-shadow: 0 20px 80px rgba(0, 0, 0, 0.2);
}

.staff-profile-dialog :deep(.el-dialog__body) {
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

/* Admin Profile Section Optimizations */
.extended-sections .profile-section {
  margin-bottom: 0;
}

/* Responsive Layout - Mobile First Approach */
@media (max-width: 768px) {
  .staff-profile-dialog :deep(.el-dialog) {
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
  .staff-profile-dialog :deep(.el-dialog) {
    width: 90% !important;
    margin: 3vh auto;
    min-height: 75vh;
    max-height: 94vh;
  }
}

@media (min-width: 1025px) {
  .staff-profile-dialog :deep(.el-dialog) {
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
  .staff-profile-dialog :deep(.el-dialog) {
    margin: 3vh auto;
    max-height: 94vh;
  }

  .staff-profile-dialog :deep(.el-dialog__body) {
    max-height: calc(94vh - 100px);
  }

  .extended-sections {
    max-height: calc(94vh - 180px);
  }
}

/* Responsive design */
@media (max-width: 1024px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    gap: 12px;
  }

  .filter-left {
    flex-direction: column;
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .stats-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .staff-profile-dialog {
    width: 95% !important;
  }

  .profile-detail-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
    padding: 1rem;
  }

  .profile-section .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* Performance Metrics Grid */
.performance-metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
}

.metric-item {
  text-align: center;
  padding: 1.5rem;
  background: #f8fafc;
  border-radius: 0.75rem;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.metric-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.metric-value {
  font-size: 2rem;
  font-weight: 800;
  color: #1e293b;
  line-height: 1;
  margin-bottom: 0.5rem;
}

.metric-label {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

@media (max-width: 480px) {
  .profile-section .stats-grid {
    grid-template-columns: 1fr;
  }

  .performance-metrics-grid {
    grid-template-columns: 1fr;
  }
}

/* Performance Grid (Admin Interface) */
.performance-grid {
  display: flex;
  flex-direction: row;
  gap: 1rem;
  justify-content: space-between;
}

.performance-item {
  flex: 1;
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

@media (max-width: 768px) {
  .performance-grid {
    flex-direction: column;
    gap: 0.75rem;
  }
}

/* Task Records Styles */
.task-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.task-table-container {
  border-radius: 0.5rem;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.task-table {
  border-radius: 0.5rem;
}

.task-pagination {
  margin-top: 1rem;
  text-align: center;
}

.text-danger {
  color: #f56565;
  font-weight: 600;
}

.no-data {
  text-align: center;
  padding: 2rem;
  color: #64748b;
}

.loading-state {
  padding: 2rem;
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
</style>
