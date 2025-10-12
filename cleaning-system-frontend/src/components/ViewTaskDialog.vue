<template>
  <el-dialog
    v-model="dialogVisible"
    title="View Tasks"
    :width="isFullscreen ? '95%' : '90%'"
    :fullscreen="isFullscreen"
    :z-index="1200"
    @closed="handleClose"
    destroy-on-close
  >
    <template #header>
      <div class="dialog-header">
        <div class="header-left">
          <h2 class="dialog-title">Task Management</h2>
          <p class="dialog-subtitle">View and manage all cleaning tasks</p>
        </div>
        <div class="header-right">
          <el-button
            type="text"
            @click="toggleFullscreen"
            class="fullscreen-btn"
          >
            <el-icon size="16">
              <FullScreen v-if="!isFullscreen" />
              <Aim v-else />
            </el-icon>
          </el-button>
        </div>
      </div>
    </template>

    <div class="view-task-container">
      <!-- Search and Filter Section -->
      <el-card class="filter-card" shadow="never">
        <template #header>
          <div class="filter-header">
            <h3>Search & Filter</h3>
            <el-button
              type="text"
              @click="toggleFilters"
              class="toggle-filters-btn"
            >
              <el-icon><Filter /></el-icon>
              {{ showAdvancedFilters ? "Hide Filters" : "Show Filters" }}
            </el-button>
          </div>
        </template>

        <!-- Basic Search -->
        <div class="basic-search">
          <el-row :gutter="16">
            <el-col :span="8">
              <el-input
                v-model="filters.searchTerm"
                placeholder="Search tasks by title, description, or location..."
                @input="debounceSearch"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="4">
              <el-select
                v-model="filters.statuses"
                placeholder="Status"
                multiple
                collapse-tags
                collapse-tags-tooltip
                @change="onFilterChange"
                clearable
              >
                <el-option
                  v-for="status in statusOptions"
                  :key="status.value"
                  :label="status.label"
                  :value="status.value"
                />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select
                v-model="filters.priorities"
                placeholder="Priority"
                multiple
                collapse-tags
                @change="onFilterChange"
                clearable
              >
                <el-option
                  v-for="priority in priorityOptions"
                  :key="priority.value"
                  :label="priority.label"
                  :value="priority.value"
                />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select
                v-model="filters.assignedTo"
                placeholder="Assigned To"
                multiple
                collapse-tags
                @change="onFilterChange"
                clearable
              >
                <el-option
                  v-for="user in staffList"
                  :key="user.userId"
                  :label="user.name"
                  :value="user.userId"
                />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-button type="primary" @click="searchTasks" :loading="loading">
                <el-icon><Search /></el-icon>
                Search
              </el-button>
            </el-col>
          </el-row>
        </div>

        <!-- Advanced Filters -->
        <div v-if="showAdvancedFilters" class="advanced-filters">
          <el-divider />
          <el-row :gutter="16" class="filter-row">
            <el-col :span="6">
              <el-date-picker
                v-model="filters.scheduledDateRange"
                type="daterange"
                range-separator="To"
                start-placeholder="Scheduled From"
                end-placeholder="Scheduled To"
                @change="onFilterChange"
                class="date-picker"
              />
            </el-col>
            <el-col :span="6">
              <el-date-picker
                v-model="filters.createdDateRange"
                type="daterange"
                range-separator="To"
                start-placeholder="Created From"
                end-placeholder="Created To"
                @change="onFilterChange"
                class="date-picker"
              />
            </el-col>
            <el-col :span="6">
              <el-input
                v-model="filters.location"
                placeholder="Location contains..."
                @input="debounceSearch"
                clearable
              />
            </el-col>
            <el-col :span="6">
              <el-checkbox
                v-model="filters.isOverdue"
                @change="onFilterChange"
                :indeterminate="filters.isOverdue === null"
              >
                Show Overdue Only
              </el-checkbox>
            </el-col>
          </el-row>

          <el-row :gutter="16" class="filter-row">
            <el-col :span="12">
              <div class="progress-filter">
                <span class="progress-label">Progress Range:</span>
                <el-slider
                  v-model="filters.progressRange"
                  range
                  :min="0"
                  :max="100"
                  :step="5"
                  @change="onFilterChange"
                  show-stops
                  :marks="{ 0: '0%', 50: '50%', 100: '100%' }"
                />
              </div>
            </el-col>
            <el-col :span="6">
              <el-button @click="resetFilters">
                <el-icon><RefreshLeft /></el-icon>
                Reset Filters
              </el-button>
            </el-col>
            <el-col :span="6">
              <el-button type="success" @click="exportTasks">
                <el-icon><Download /></el-icon>
                Export Results
              </el-button>
            </el-col>
          </el-row>
        </div>
      </el-card>

      <!-- Statistics Summary -->
      <div class="stats-section">
        <el-row :gutter="16">
          <el-col :span="4">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon stat-icon-blue">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ taskStats.totalTasks || 0 }}</div>
                  <div class="stat-label">Total Tasks</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon stat-icon-yellow">
                  <el-icon><Clock /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">
                    {{ taskStats.pendingTasks || 0 }}
                  </div>
                  <div class="stat-label">Pending</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon stat-icon-orange">
                  <el-icon><Loading /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">
                    {{ taskStats.inProgressTasks || 0 }}
                  </div>
                  <div class="stat-label">In Progress</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon stat-icon-green">
                  <el-icon><CircleCheck /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">
                    {{ taskStats.completedTasks || 0 }}
                  </div>
                  <div class="stat-label">Completed</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon stat-icon-red">
                  <el-icon><Warning /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">
                    {{ taskStats.overdueTasks || 0 }}
                  </div>
                  <div class="stat-label">Overdue</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon stat-icon-purple">
                  <el-icon><Star /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">
                    {{ taskStats.highPriorityTasks || 0 }}
                  </div>
                  <div class="stat-label">High Priority</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- Batch Actions -->
      <div v-if="selectedTasks.length > 0" class="batch-actions">
        <el-alert
          :title="`${selectedTasks.length} task(s) selected`"
          type="info"
          show-icon
          :closable="false"
        >
          <template #default>
            <div class="batch-buttons">
              <el-button
                type="primary"
                size="small"
                @click="showBatchStatusDialog"
              >
                Update Status
              </el-button>
              <el-button
                type="warning"
                size="small"
                @click="showBatchAssignDialog"
              >
                Reassign
              </el-button>
              <el-button
                type="success"
                size="small"
                @click="showBatchPriorityDialog"
              >
                Update Priority
              </el-button>
              <el-button type="danger" size="small" @click="batchDelete">
                Delete Selected
              </el-button>
              <el-button size="small" @click="clearSelection">
                Clear Selection
              </el-button>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- Main Tasks Table -->
      <el-card class="table-card" shadow="never">
        <template #header>
          <div class="table-header">
            <div class="table-title">
              <h3>Task List</h3>
              <span class="task-count">
                {{ totalTasks }} total tasks
                {{
                  currentPage > 0
                    ? `(Page ${currentPage + 1} of ${totalPages})`
                    : ""
                }}
              </span>
            </div>
            <div class="table-actions">
              <el-tooltip content="Refresh Data">
                <el-button
                  type="text"
                  @click="refreshTasks"
                  :loading="loading"
                  circle
                >
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </el-tooltip>
              <el-dropdown @command="handleTableAction">
                <el-button type="text">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="export"
                      >Export Table</el-dropdown-item
                    >
                    <el-dropdown-item command="print"
                      >Print Table</el-dropdown-item
                    >
                    <el-dropdown-item command="columns"
                      >Column Settings</el-dropdown-item
                    >
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </template>

        <el-table
          ref="taskTable"
          :data="tasks"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
          stripe
          class="task-table"
          :default-sort="{ prop: 'createdAt', order: 'descending' }"
          :row-class-name="getRowClassName"
        >
          <!-- Selection Column -->
          <el-table-column type="selection" width="50" align="center" />

          <!-- Task ID Column -->
          <el-table-column
            prop="taskId"
            label="ID"
            width="80"
            sortable="custom"
            align="center"
          />

          <!-- Title Column -->
          <el-table-column
            prop="title"
            label="Title"
            min-width="200"
            sortable="custom"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              <div class="task-title-cell">
                <span class="task-title" @click="openTaskDetail(row)">
                  {{ row.title }}
                </span>
                <div v-if="row.description" class="task-description">
                  {{ row.description }}
                </div>
              </div>
            </template>
          </el-table-column>

          <!-- Status Column -->
          <el-table-column
            prop="status"
            label="Status"
            width="120"
            sortable="custom"
            align="center"
          >
            <template #default="{ row }">
              <el-tag
                :type="getStatusTagType(row.status)"
                effect="plain"
                size="small"
              >
                {{ row.statusDisplay || row.status }}
              </el-tag>
            </template>
          </el-table-column>

          <!-- Priority Column -->
          <el-table-column
            prop="priority"
            label="Priority"
            width="100"
            sortable="custom"
            align="center"
          >
            <template #default="{ row }">
              <el-tag
                :type="getPriorityTagType(row.priority)"
                effect="dark"
                size="small"
              >
                {{ row.priorityDisplay || row.priority }}
              </el-tag>
            </template>
          </el-table-column>

          <!-- Progress Column -->
          <el-table-column
            prop="progressPercentage"
            label="Progress"
            width="140"
            sortable="custom"
            align="center"
          >
            <template #default="{ row }">
              <div class="progress-cell">
                <el-progress
                  :percentage="row.progressPercentage || 0"
                  :stroke-width="8"
                  :show-text="false"
                  :color="getProgressColor(row.progressPercentage)"
                />
                <span class="progress-text"
                  >{{ row.progressPercentage || 0 }}%</span
                >
              </div>
            </template>
          </el-table-column>

          <!-- Assigned To Column -->
          <el-table-column
            prop="assignedToName"
            label="Assigned To"
            width="140"
            sortable="custom"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              <div class="assigned-cell">
                <el-avatar
                  :size="24"
                  class="avatar-small"
                  :src="getUserAvatar(row.assignedToId, row.assignedToName, row.assignedToAvatarUrl)"
                >
                  {{ getInitials(row.assignedToName) }}
                </el-avatar>
                <span class="assigned-name">{{
                  row.assignedToName || "Unassigned"
                }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- Location Column -->
          <el-table-column
            prop="location"
            label="Location"
            width="140"
            sortable="custom"
            show-overflow-tooltip
          />

          <!-- Scheduled Time Column -->
          <el-table-column
            prop="scheduledTime"
            label="Scheduled"
            width="140"
            sortable="custom"
            align="center"
          >
            <template #default="{ row }">
              <div v-if="row.scheduledTime" class="time-cell">
                <div class="date-text">{{ formatDate(row.scheduledTime) }}</div>
                <div class="time-text">{{ formatTime(row.scheduledTime) }}</div>
              </div>
              <span v-else class="no-date">Not scheduled</span>
            </template>
          </el-table-column>

          <!-- Due Date Column -->
          <el-table-column
            prop="dueDate"
            label="Due Date"
            width="140"
            sortable="custom"
            align="center"
          >
            <template #default="{ row }">
              <div v-if="row.dueDate" class="time-cell">
                <div
                  class="date-text"
                  :class="{ 'overdue-text': row.isOverdue }"
                >
                  {{ formatDate(row.dueDate) }}
                </div>
                <div class="time-text">{{ formatTime(row.dueDate) }}</div>
              </div>
              <span v-else class="no-date">No deadline</span>
            </template>
          </el-table-column>

          <!-- Actions Column -->
          <el-table-column
            label="Actions"
            width="120"
            align="center"
            fixed="right"
          >
            <template #default="{ row }">
              <div class="action-buttons">
                <el-tooltip content="View Details">
                  <el-button
                    type="text"
                    size="small"
                    @click="openTaskDetail(row)"
                  >
                    <el-icon><View /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="Edit Task" v-if="canEditTasks">
                  <el-button type="text" size="small" @click="editTask(row)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-dropdown @command="(cmd) => handleRowAction(cmd, row)">
                  <el-button type="text" size="small">
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                        v-if="row.status !== 'completed'"
                        command="complete"
                      >
                        Mark Complete
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="row.status === 'pending'"
                        command="start"
                      >
                        Start Task
                      </el-dropdown-item>
                      <el-dropdown-item command="duplicate">
                        Duplicate
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" divided>
                        Delete
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- Pagination -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="totalTasks"
            :page-sizes="[10, 25, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background
          />
        </div>
      </el-card>
    </div>

    <!-- Batch Operation Dialogs -->
    <el-dialog
      v-model="batchStatusDialogVisible"
      title="Update Status"
      width="400px"
    >
      <el-form label-width="100px">
        <el-form-item label="New Status">
          <el-select v-model="batchOperation.newStatus" style="width: 100%">
            <el-option
              v-for="status in statusOptions"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Reason">
          <el-input
            v-model="batchOperation.reason"
            type="textarea"
            placeholder="Optional reason for this change..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchStatusDialogVisible = false">Cancel</el-button>
        <el-button
          type="primary"
          @click="confirmBatchStatus"
          :loading="batchLoading"
        >
          Update {{ selectedTasks.length }} Task(s)
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="batchAssignDialogVisible"
      title="Reassign Tasks"
      width="400px"
    >
      <el-form label-width="100px">
        <el-form-item label="Assign To">
          <el-select v-model="batchOperation.newAssigneeId" style="width: 100%">
            <el-option
              v-for="user in staffList"
              :key="user.userId"
              :label="user.name"
              :value="user.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Reason">
          <el-input
            v-model="batchOperation.reason"
            type="textarea"
            placeholder="Optional reason for reassignment..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchAssignDialogVisible = false">Cancel</el-button>
        <el-button
          type="primary"
          @click="confirmBatchAssign"
          :loading="batchLoading"
        >
          Reassign {{ selectedTasks.length }} Task(s)
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="batchPriorityDialogVisible"
      title="Update Priority"
      width="400px"
    >
      <el-form label-width="100px">
        <el-form-item label="New Priority">
          <el-select v-model="batchOperation.newPriority" style="width: 100%">
            <el-option
              v-for="priority in priorityOptions"
              :key="priority.value"
              :label="priority.label"
              :value="priority.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Reason">
          <el-input
            v-model="batchOperation.reason"
            type="textarea"
            placeholder="Optional reason for this change..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchPriorityDialogVisible = false"
          >Cancel</el-button
        >
        <el-button
          type="primary"
          @click="confirmBatchPriority"
          :loading="batchLoading"
        >
          Update {{ selectedTasks.length }} Task(s)
        </el-button>
      </template>
    </el-dialog>

    <!-- Task Detail Dialog -->
    <TaskDetailDialog
      v-model:visible="taskDetailVisible"
      :task="selectedTaskForDetail"
      @task-updated="handleTaskDetailUpdated"
      @task-deleted="handleTaskDetailDeleted"
      @edit-task="handleTaskDetailEdit"
    />
  </el-dialog>
</template>

<script>
import { ref, reactive, computed, watch, onMounted, nextTick } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { API } from "@/utils/request";
import { AuthUtils } from "@/utils/auth";
import { getComprehensiveAvatar } from "@/utils/avatar";
import TaskDetailDialog from "./TaskDetailDialog.vue";
import {
  Search,
  Filter,
  Refresh,
  RefreshLeft,
  Download,
  Document,
  Clock,
  Loading,
  CircleCheck,
  Warning,
  Star,
  MoreFilled,
  View,
  Edit,
  FullScreen,
  Aim,
} from "@element-plus/icons-vue";

export default {
  name: "ViewTaskDialog",
  components: {
    TaskDetailDialog,
    Search,
    Filter,
    Refresh,
    RefreshLeft,
    Download,
    Document,
    Clock,
    Loading,
    CircleCheck,
    Warning,
    Star,
    MoreFilled,
    View,
    Edit,
    FullScreen,
    Aim,
  },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    staffList: {
      type: Array,
      default: () => [],
    },
  },
  emits: ["update:visible", "task-updated", "task-deleted", "edit-task"],
  setup(props, { emit }) {
    // User role detection
    const userInfo = AuthUtils.getUserInfo();
    const userRole = userInfo?.role || 'admin';
    const canEditTasks = userRole === 'janitor'; // Only janitors can edit tasks

    // Reactive data
    const loading = ref(false);
    const batchLoading = ref(false);
    const isFullscreen = ref(false);
    const showAdvancedFilters = ref(false);

    // Table data
    const tasks = ref([]);
    const totalTasks = ref(0);
    const totalPages = ref(0);
    const currentPage = ref(0);
    const pageSize = ref(25);

    // Selected tasks for batch operations
    const selectedTasks = ref([]);

    // Task statistics
    const taskStats = ref({
      totalTasks: 0,
      pendingTasks: 0,
      inProgressTasks: 0,
      completedTasks: 0,
      overdueTasks: 0,
      highPriorityTasks: 0,
      mediumPriorityTasks: 0,
      lowPriorityTasks: 0,
    });

    // Filter form
    const filters = reactive({
      searchTerm: "",
      statuses: [],
      priorities: [],
      assignedTo: [],
      assignedBy: [],
      location: "",
      scheduledDateRange: null,
      createdDateRange: null,
      isOverdue: null,
      progressRange: [0, 100],
    });

    // Sorting
    const sortField = ref("createdAt");
    const sortDirection = ref("DESC");

    // Batch operation dialogs
    const batchStatusDialogVisible = ref(false);
    const batchAssignDialogVisible = ref(false);
    const batchPriorityDialogVisible = ref(false);

    const batchOperation = reactive({
      newStatus: "",
      newAssigneeId: null,
      newPriority: "",
      reason: "",
    });

    // Options data
    const statusOptions = [
      { label: "Pending", value: "pending" },
      { label: "In Progress", value: "in_progress" },
      { label: "Completed", value: "completed" },
      { label: "Overdue", value: "overdue" },
    ];

    const priorityOptions = [
      { label: "Low", value: "low" },
      { label: "Medium", value: "medium" },
      { label: "High", value: "high" },
      { label: "Urgent", value: "urgent" },
    ];

    // Debounce timer for search
    let searchTimeout = null;

    // Methods
    const searchTasks = async () => {
      loading.value = true;
      try {
        const filterRequest = {
          page: currentPage.value,
          size: pageSize.value,
          sortBy: sortField.value,
          sortDirection: sortDirection.value,
          searchTerm: filters.searchTerm || null,
          statuses: filters.statuses.length > 0 ? filters.statuses : null,
          priorities: filters.priorities.length > 0 ? filters.priorities : null,
          assignedTo: filters.assignedTo.length > 0 ? filters.assignedTo : null,
          assignedBy: filters.assignedBy.length > 0 ? filters.assignedBy : null,
          location: filters.location || null,
          scheduledStartDate: filters.scheduledDateRange
            ? filters.scheduledDateRange[0]
            : null,
          scheduledEndDate: filters.scheduledDateRange
            ? filters.scheduledDateRange[1]
            : null,
          createdStartDate: filters.createdDateRange
            ? filters.createdDateRange[0]
            : null,
          createdEndDate: filters.createdDateRange
            ? filters.createdDateRange[1]
            : null,
          isOverdue: filters.isOverdue,
          minProgress: filters.progressRange[0],
          maxProgress: filters.progressRange[1],
        };

        const response = await API.post("/api/tasks/search", filterRequest);

        if (response.data) {
          tasks.value = response.data.tasks || [];
          totalTasks.value = response.data.totalElements || 0;
          totalPages.value = response.data.totalPages || 0;

          if (response.data.stats) {
            taskStats.value = response.data.stats;
          }
        }
      } catch (error) {
        console.error("Failed to search tasks:", error);
        ElMessage.error("Failed to load tasks. Please try again.");
      } finally {
        loading.value = false;
      }
    };

    const debounceSearch = () => {
      if (searchTimeout) clearTimeout(searchTimeout);
      searchTimeout = setTimeout(searchTasks, 500);
    };

    const onFilterChange = () => {
      currentPage.value = 0; // Reset to first page
      searchTasks();
    };

    const resetFilters = () => {
      Object.assign(filters, {
        searchTerm: "",
        statuses: [],
        priorities: [],
        assignedTo: [],
        assignedBy: [],
        location: "",
        scheduledDateRange: null,
        createdDateRange: null,
        isOverdue: null,
        progressRange: [0, 100],
      });
      currentPage.value = 0;
      sortField.value = "createdAt";
      sortDirection.value = "DESC";
      searchTasks();
    };

    const refreshTasks = () => {
      searchTasks();
    };

    const handleSelectionChange = (selection) => {
      selectedTasks.value = selection;
    };

    const clearSelection = () => {
      selectedTasks.value = [];
    };

    const handleSortChange = ({ prop, order }) => {
      sortField.value = prop || "createdAt";
      sortDirection.value = order === "ascending" ? "ASC" : "DESC";
      currentPage.value = 0;
      searchTasks();
    };

    const handleSizeChange = (size) => {
      pageSize.value = size;
      currentPage.value = 0;
      searchTasks();
    };

    const handleCurrentChange = (page) => {
      currentPage.value = page - 1; // API uses 0-based page index
      searchTasks();
    };

    // Batch operations
    const showBatchStatusDialog = () => {
      batchOperation.newStatus = "";
      batchOperation.reason = "";
      batchStatusDialogVisible.value = true;
    };

    const showBatchAssignDialog = () => {
      batchOperation.newAssigneeId = null;
      batchOperation.reason = "";
      batchAssignDialogVisible.value = true;
    };

    const showBatchPriorityDialog = () => {
      batchOperation.newPriority = "";
      batchOperation.reason = "";
      batchPriorityDialogVisible.value = true;
    };

    const confirmBatchStatus = async () => {
      if (!batchOperation.newStatus) {
        ElMessage.warning("Please select a new status");
        return;
      }

      await performBatchOperation("UPDATE_STATUS", {
        newStatus: batchOperation.newStatus,
        reason: batchOperation.reason,
      });

      batchStatusDialogVisible.value = false;
    };

    const confirmBatchAssign = async () => {
      if (!batchOperation.newAssigneeId) {
        ElMessage.warning("Please select an assignee");
        return;
      }

      await performBatchOperation("REASSIGN", {
        newAssigneeId: batchOperation.newAssigneeId,
        reason: batchOperation.reason,
      });

      batchAssignDialogVisible.value = false;
    };

    const confirmBatchPriority = async () => {
      if (!batchOperation.newPriority) {
        ElMessage.warning("Please select a new priority");
        return;
      }

      await performBatchOperation("UPDATE_PRIORITY", {
        newPriority: batchOperation.newPriority,
        reason: batchOperation.reason,
      });

      batchPriorityDialogVisible.value = false;
    };

    const batchDelete = async () => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete ${selectedTasks.value.length} selected task(s)?`,
          "Delete Tasks",
          {
            confirmButtonText: "Delete",
            cancelButtonText: "Cancel",
            type: "error",
          }
        );

        await performBatchOperation("DELETE", {});
      } catch (error) {
        if (error !== "cancel") {
          console.error("Batch delete error:", error);
        }
      }
    };

    const performBatchOperation = async (operation, params) => {
      if (selectedTasks.value.length === 0) {
        ElMessage.warning("No tasks selected");
        return;
      }

      batchLoading.value = true;
      try {
        const request = {
          taskIds: selectedTasks.value.map((task) => task.taskId),
          operation,
          ...params,
        };

        const response = await API.post("/api/tasks/batch", request);

        if (response.data.success) {
          ElMessage.success(response.data.message);
          clearSelection();
          await searchTasks(); // Refresh data
          emit("task-updated");
        } else {
          ElMessage.error(response.data.message || "Batch operation failed");
        }
      } catch (error) {
        console.error("Batch operation error:", error);
        ElMessage.error("Batch operation failed. Please try again.");
      } finally {
        batchLoading.value = false;
      }
    };

    // Row and cell formatting
    const getRowClassName = ({ row }) => {
      if (row.isOverdue) return "overdue-row";
      if (row.status === "completed") return "completed-row";
      if (row.priority === "high" || row.priority === "urgent")
        return "high-priority-row";
      return "";
    };

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

    // Utility methods
    const formatDate = (dateTime) => {
      if (!dateTime) return "";
      return new Date(dateTime).toLocaleDateString();
    };

    const formatTime = (dateTime) => {
      if (!dateTime) return "";
      return new Date(dateTime).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
      });
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

    const getUserAvatar = (userId, userName = null, avatarUrl = null) => {
      // Use provided avatar URL if available
      if (avatarUrl) {
        return avatarUrl;
      }
      // Fallback to comprehensive avatar
      return getComprehensiveAvatar(userId, {
        size: 32,
        userName: userName
      });
    };

    // Task detail dialog
    const taskDetailVisible = ref(false);
    const selectedTaskForDetail = ref(null);

    // Dialog actions
    const openTaskDetail = (task) => {
      console.log('Opening task detail in ViewTaskDialog:', {
        taskId: task.taskId,
        status: task.status,
        completedAt: task.completedAt,
        actualDuration: task.actualDuration,
        completionNotes: task.completionNotes,
        completion_notes: task.completion_notes
      });
      selectedTaskForDetail.value = task;
      taskDetailVisible.value = true;
    };

    const editTask = (task) => {
      // Don't close the ViewTaskDialog, just emit the edit event
      // The parent component (TaskCalendar) should handle opening edit dialog with higher z-index
      emit("edit-task", task);
    };

    const handleRowAction = async (command, row) => {
      switch (command) {
        case "complete":
          await updateTaskStatus(row, "completed");
          break;
        case "start":
          await updateTaskStatus(row, "in_progress");
          break;
        case "duplicate":
          // Implement task duplication
          break;
        case "delete":
          await deleteTask(row);
          break;
      }
    };

    const updateTaskStatus = async (task, newStatus) => {
      try {
        await API.put(`/api/tasks/${task.taskId}`, { status: newStatus });
        ElMessage.success("Task status updated successfully");
        await searchTasks();
        emit("task-updated");
      } catch (error) {
        console.error("Failed to update task status:", error);
        ElMessage.error("Failed to update task status");
      }
    };

    const deleteTask = async (task) => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete "${task.title}"?`,
          "Delete Task",
          {
            confirmButtonText: "Delete",
            cancelButtonText: "Cancel",
            type: "error",
          }
        );

        await API.delete(`/api/tasks/${task.taskId}`);
        ElMessage.success("Task deleted successfully");
        await searchTasks();
        emit("task-deleted", task);
      } catch (error) {
        if (error !== "cancel") {
          console.error("Failed to delete task:", error);
          ElMessage.error("Failed to delete task");
        }
      }
    };

    const handleTableAction = (command) => {
      switch (command) {
        case "export":
          exportTasks();
          break;
        case "print":
          window.print();
          break;
        case "columns":
          // Open column settings dialog
          break;
      }
    };

    const exportTasks = () => {
      // Implement CSV export
      const csvContent = convertToCSV(tasks.value);
      downloadCSV(csvContent, "tasks-export.csv");
    };

    const convertToCSV = (data) => {
      if (!data.length) return "";

      const headers = [
        "ID",
        "Title",
        "Description",
        "Status",
        "Priority",
        "Progress",
        "Assigned To",
        "Location",
        "Scheduled",
        "Due Date",
        "Created",
      ];

      const rows = data.map((task) => [
        task.taskId,
        task.title,
        task.description || "",
        task.status,
        task.priority,
        task.progressPercentage + "%",
        task.assignedToName || "Unassigned",
        task.location || "",
        task.scheduledTime ? formatDate(task.scheduledTime) : "",
        task.dueDate ? formatDate(task.dueDate) : "",
        formatDate(task.createdAt),
      ]);

      return [headers, ...rows]
        .map((row) => row.map((field) => `"${field}"`).join(","))
        .join("\n");
    };

    const downloadCSV = (content, filename) => {
      const blob = new Blob([content], { type: "text/csv" });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    };

    const toggleFullscreen = () => {
      isFullscreen.value = !isFullscreen.value;
    };

    const toggleFilters = () => {
      showAdvancedFilters.value = !showAdvancedFilters.value;
    };

    const handleClose = () => {
      emit("update:visible", false);
    };

    // Task detail dialog handlers
    const handleTaskDetailUpdated = async () => {
      await searchTasks();
      emit("task-updated");
    };

    const handleTaskDetailDeleted = async (deletedTask) => {
      await searchTasks();
      emit("task-deleted", deletedTask);
    };

    const handleTaskDetailEdit = (task) => {
      taskDetailVisible.value = false;
      emit("edit-task", task);
    };

    // Watch for visibility changes
    watch(
      () => props.visible,
      (visible) => {
        if (visible) {
          nextTick(() => {
            searchTasks();
          });
        }
      }
    );

    // Initialize on mount
    onMounted(() => {
      if (props.visible) {
        searchTasks();
      }
    });

    // Computed properties
    const dialogVisible = computed({
      get: () => props.visible,
      set: (value) => emit("update:visible", value),
    });

    return {
      // Computed properties
      dialogVisible,

      // User permissions
      canEditTasks,

      // Reactive data
      loading,
      batchLoading,
      isFullscreen,
      showAdvancedFilters,
      tasks,
      totalTasks,
      totalPages,
      currentPage,
      pageSize,
      selectedTasks,
      taskStats,
      filters,
      sortField,
      sortDirection,
      taskDetailVisible,
      selectedTaskForDetail,

      // Dialogs
      batchStatusDialogVisible,
      batchAssignDialogVisible,
      batchPriorityDialogVisible,
      batchOperation,

      // Options
      statusOptions,
      priorityOptions,

      // Methods
      searchTasks,
      debounceSearch,
      onFilterChange,
      resetFilters,
      refreshTasks,
      handleSelectionChange,
      clearSelection,
      handleSortChange,
      handleSizeChange,
      handleCurrentChange,

      // Batch operations
      showBatchStatusDialog,
      showBatchAssignDialog,
      showBatchPriorityDialog,
      confirmBatchStatus,
      confirmBatchAssign,
      confirmBatchPriority,
      batchDelete,

      // Formatting
      getRowClassName,
      getStatusTagType,
      getPriorityTagType,
      getProgressColor,
      formatDate,
      formatTime,
      getInitials,
      getUserAvatar,

      // Actions
      openTaskDetail,
      editTask,
      handleRowAction,
      handleTableAction,
      exportTasks,
      toggleFullscreen,
      toggleFilters,
      handleClose,
      handleTaskDetailUpdated,
      handleTaskDetailDeleted,
      handleTaskDetailEdit,
    };
  },
};
</script>

<style scoped>
.view-task-container {
  padding: 0;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-left h2.dialog-title {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.dialog-subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.fullscreen-btn {
  padding: 4px;
}

/* Filter Section */
.filter-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.toggle-filters-btn {
  color: #409eff;
}

.basic-search {
  margin-bottom: 16px;
}

.advanced-filters {
  margin-top: 16px;
}

.filter-row {
  margin-bottom: 16px;
}

.progress-filter {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.date-picker {
  width: 100%;
}

/* Statistics Section */
.stats-section {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 8px;
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon-blue {
  background: linear-gradient(135deg, #409eff, #3a8ee6);
}
.stat-icon-yellow {
  background: linear-gradient(135deg, #f4ce42, #e6a23c);
}
.stat-icon-orange {
  background: linear-gradient(135deg, #fd8500, #e6a23c);
}
.stat-icon-green {
  background: linear-gradient(135deg, #67c23a, #529b2e);
}
.stat-icon-red {
  background: linear-gradient(135deg, #f56c6c, #f5222d);
}
.stat-icon-purple {
  background: linear-gradient(135deg, #845ec2, #6f42c1);
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

/* Batch Actions */
.batch-actions {
  margin-bottom: 16px;
}

.batch-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}

/* Table Section */
.table-card {
  border-radius: 8px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #303133;
}

.task-count {
  font-size: 12px;
  color: #909399;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Table styling */
.task-table {
  margin-bottom: 16px;
}

.task-table :deep(.overdue-row) {
  background-color: #fef0f0;
}

.task-table :deep(.completed-row) {
  background-color: #f0f9ff;
}

.task-table :deep(.high-priority-row) {
  border-left: 4px solid #f56c6c;
}

/* Table cell content */
.task-title-cell {
  min-height: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.task-title {
  font-weight: 500;
  color: #409eff;
  cursor: pointer;
  margin-bottom: 2px;
}

.task-title:hover {
  text-decoration: underline;
}

.task-description {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.progress-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.progress-text {
  font-size: 12px;
  color: #606266;
  min-width: 30px;
  text-align: right;
  white-space: nowrap;
  flex-shrink: 0;
}

.assigned-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar-small {
  flex-shrink: 0;
}

.assigned-name {
  font-size: 13px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time-cell {
  text-align: center;
}

.date-text {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.time-text {
  font-size: 11px;
  color: #909399;
}

.overdue-text {
  color: #f56c6c !important;
}

.no-date {
  font-size: 12px;
  color: #c0c4cc;
  font-style: italic;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: center;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* Responsive design */
@media (max-width: 1024px) {
  .basic-search .el-col {
    margin-bottom: 8px;
  }

  .filter-row .el-col {
    margin-bottom: 8px;
  }

  .stats-section .el-col {
    margin-bottom: 8px;
  }
}

@media (max-width: 768px) {
  .dialog-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .batch-buttons {
    justify-content: center;
  }
}
</style>
