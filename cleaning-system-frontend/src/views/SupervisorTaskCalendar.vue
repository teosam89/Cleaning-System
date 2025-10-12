<template>
  <SupervisorLayout>
    <div class="task-calendar-container">
      <!-- Page Header -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">Task Scheduler</h1>
          <p class="page-subtitle">
            Schedule and coordinate team cleaning tasks
          </p>
        </div>
        <div class="header-actions">
          <el-button type="info" @click="showViewTaskDialog">
            <el-icon><View /></el-icon>
            View Tasks
          </el-button>
          <el-button type="success" @click="forceRefresh" :loading="loading">
            <el-icon><Refresh /></el-icon>
            Refresh Data
          </el-button>
          <el-button type="primary" @click="showAddTaskDialog">
            <el-icon><Plus /></el-icon>
            Add New Task
          </el-button>
        </div>
      </div>

      <!-- Calendar Controls -->
      <el-card class="calendar-controls" shadow="never">
        <div class="controls-section">
          <div class="controls-left">
            <el-button-group>
              <el-button
                :type="currentView === 'month' ? 'primary' : 'default'"
                @click="changeView('month')"
              >
                Month
              </el-button>
              <el-button
                :type="currentView === 'week' ? 'primary' : 'default'"
                @click="changeView('week')"
              >
                Week
              </el-button>
              <el-button
                :type="currentView === 'day' ? 'primary' : 'default'"
                @click="changeView('day')"
              >
                Day
              </el-button>
            </el-button-group>

            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="Select date"
              @change="onDateChange"
              class="date-picker"
            />
          </div>

          <div class="controls-right">
            <el-button @click="goToToday">Today</el-button>
            <el-button-group>
              <el-button @click="previousPeriod">
                <el-icon><ArrowLeft /></el-icon>
              </el-button>
              <el-button @click="nextPeriod">
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </el-button-group>
          </div>
        </div>
      </el-card>

      <!-- Calendar Stats -->
      <div class="stats-row">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon stat-icon-blue">
              <el-icon size="24"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ calendarStats.totalTasks }}</div>
              <div class="stat-label">Total Tasks</div>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon stat-icon-green">
              <el-icon size="24"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ calendarStats.completedTasks }}</div>
              <div class="stat-label">Completed</div>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon stat-icon-orange">
              <el-icon size="24"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ calendarStats.upcomingTasks }}</div>
              <div class="stat-label">Upcoming</div>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon stat-icon-red">
              <el-icon size="24"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ calendarStats.overdueTasks }}</div>
              <div class="stat-label">Overdue</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- Main Calendar -->
      <el-card class="calendar-card" shadow="never">
        <div class="calendar-header">
          <h2 class="calendar-title">{{ currentPeriodTitle }}</h2>
          <div class="calendar-legend">
            <div class="legend-item">
              <div class="legend-color legend-cleaning"></div>
              <span>Task</span>
            </div>
          </div>
        </div>

        <!-- Calendar Grid -->
        <div class="calendar-container">
          <div v-if="currentView === 'month'" class="month-view">
            <!-- Month Header -->
            <div class="month-header">
              <div v-for="day in weekDays" :key="day" class="day-header">
                {{ day }}
              </div>
            </div>

            <!-- Month Grid -->
            <div class="month-grid">
              <div
                v-for="(date, index) in monthDates"
                :key="index"
                class="day-cell"
                :class="{
                  'other-month': !date.isCurrentMonth,
                  today: date.isToday,
                  selected: date.isSelected,
                  'drop-valid': dragState.isDragging && isValidDropTarget(date),
                  'drop-invalid': dragState.isDragging && !isValidDropTarget(date)
                }"
                @click="selectDate(date)"
                @dragover.prevent="handleDragOver(date, $event)"
                @dragenter.prevent="handleDragEnter(date, $event)"
                @dragleave="handleDragLeave(date, $event)"
                @drop.prevent="handleDrop(date, $event)"
              >
                <div class="day-number">{{ date.day }}</div>
                <div class="day-events">
                  <div
                    v-for="event in date.events"
                    :key="event.id"
                    class="event-item"
                    :class="[
                      `event-${event.type}`,
                      `status-${event.status}`,
                      `priority-${event.priority}`,
                      { 'draggable-task': event.status === 'pending' && currentView !== 'day' }
                    ]"
                    :draggable="event.status === 'pending' && currentView !== 'day'"
                    @dragstart="handleDragStart(event, $event)"
                    @dragend="handleDragEnd($event)"
                    @click.stop="openEventDetails(event)"
                  >
                    <div class="event-header">
                      <div class="event-title">{{ event.title }}</div>
                      <div class="event-status-badge">
                        <el-icon
                          v-if="event.status === 'completed'"
                          class="status-icon completed"
                        >
                          <CircleCheck />
                        </el-icon>
                        <el-icon
                          v-else-if="event.status === 'in_progress'"
                          class="status-icon in-progress"
                        >
                          <Clock />
                        </el-icon>
                        <el-icon
                          v-else-if="event.status === 'overdue'"
                          class="status-icon overdue"
                        >
                          <Warning />
                        </el-icon>
                        <el-icon v-else class="status-icon pending">
                          <Calendar />
                        </el-icon>
                      </div>
                    </div>
                    <div class="event-details">
                      <div class="event-time">{{ formatEventTime(event) }}</div>
                      <div class="event-assignee">{{ event.assignee }}</div>
                      <div
                        v-if="event.progressPercentage > 0"
                        class="event-progress"
                      >
                        <div class="progress-bar">
                          <div
                            class="progress-fill"
                            :style="{ width: `${event.progressPercentage}%` }"
                          ></div>
                        </div>
                        <span class="progress-text"
                          >{{ event.progressPercentage }}%</span
                        >
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Week View -->
          <div v-else-if="currentView === 'week'" class="week-view">
            <div class="week-header">
              <div class="time-column"></div>
              <div
                v-for="date in weekDates"
                :key="date.dateString"
                class="week-day-header"
                :class="{ today: date.isToday }"
              >
                <div class="week-day-name">{{ date.dayName }}</div>
                <div class="week-day-number">{{ date.day }}</div>
              </div>
            </div>

            <div class="week-grid">
              <div class="time-slots">
                <div v-for="hour in 24" :key="hour" class="time-slot">
                  {{ formatHour(hour - 1) }}
                </div>
              </div>
              <div
                v-for="date in weekDates"
                :key="date.dateString"
                class="week-day-column"
              >
                <div
                  v-for="hour in 24"
                  :key="hour"
                  class="hour-slot"
                  :class="{
                    'drop-valid': dragState.isDragging && isValidHourDropTarget(date, hour),
                    'drop-invalid': dragState.isDragging && !isValidHourDropTarget(date, hour)
                  }"
                  @dragover.prevent="handleHourDragOver(date, hour, $event)"
                  @dragenter.prevent="handleHourDragEnter(date, hour, $event)"
                  @dragleave="handleHourDragLeave(date, hour, $event)"
                  @drop.prevent="handleHourDrop(date, hour, $event)"
                ></div>
                <div
                  v-for="event in date.events"
                  :key="event.id"
                  class="week-event"
                  :class="[
                    `event-${event.type}`,
                    `status-${event.status}`,
                    `priority-${event.priority}`,
                    { 'draggable-task': event.status === 'pending' && currentView !== 'day' }
                  ]"
                  :style="getEventStyle(event)"
                  :draggable="event.status === 'pending' && currentView !== 'day'"
                  @dragstart="handleDragStart(event, $event)"
                  @dragend="handleDragEnd($event)"
                  @click="openEventDetails(event)"
                >
                  <div class="week-event-content">
                    <div class="event-title">{{ event.title }}</div>
                    <div class="event-time">{{ formatEventTime(event) }}</div>
                    <div class="event-assignee">{{ event.assignee }}</div>
                    <div
                      v-if="event.progressPercentage > 0"
                      class="event-progress-small"
                    >
                      <span class="progress-text-small"
                        >{{ event.progressPercentage }}%</span
                      >
                    </div>
                  </div>
                  <div class="event-status-indicator">
                    <el-icon
                      v-if="event.status === 'completed'"
                      class="status-icon-small completed"
                    >
                      <CircleCheck />
                    </el-icon>
                    <el-icon
                      v-else-if="event.status === 'in_progress'"
                      class="status-icon-small in-progress"
                    >
                      <Clock />
                    </el-icon>
                    <el-icon
                      v-else-if="event.status === 'overdue'"
                      class="status-icon-small overdue"
                    >
                      <Warning />
                    </el-icon>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Day View -->
          <div v-else class="day-view">
            <div class="day-header">
              <h3>{{ formatDate(selectedDate) }}</h3>
            </div>
            <div class="day-events-list">
              <div
                v-for="event in dayEvents"
                :key="event.id"
                class="day-event-item"
                :class="[
                  `event-${event.type}`,
                  `status-${event.status}`,
                  `priority-${event.priority}`,
                ]"
                @click="openEventDetails(event)"
              >
                <div class="event-time-column">
                  <div class="event-time">{{ formatEventTime(event) }}</div>
                  <div class="event-status-icon">
                    <el-icon
                      v-if="event.status === 'completed'"
                      class="status-icon completed"
                    >
                      <CircleCheck />
                    </el-icon>
                    <el-icon
                      v-else-if="event.status === 'in_progress'"
                      class="status-icon in-progress"
                    >
                      <Clock />
                    </el-icon>
                    <el-icon
                      v-else-if="event.status === 'overdue'"
                      class="status-icon overdue"
                    >
                      <Warning />
                    </el-icon>
                    <el-icon v-else class="status-icon pending">
                      <Calendar />
                    </el-icon>
                  </div>
                </div>
                <div class="event-content">
                  <div class="event-header-row">
                    <div class="event-title">{{ event.title }}</div>
                    <div
                      class="event-priority-badge"
                      :class="`priority-${event.priority}`"
                    >
                      {{ event.priority }}
                    </div>
                  </div>
                  <div class="event-description">{{ event.description }}</div>
                  <div class="event-meta">
                    <el-tag size="small" :type="getEventTagType(event.type)">
                      {{ event.type }}
                    </el-tag>
                    <span class="event-assignee">{{ event.assignee }}</span>
                    <span class="event-location" v-if="event.location">{{
                      event.location
                    }}</span>
                  </div>
                  <div
                    v-if="event.progressPercentage > 0"
                    class="event-progress-full"
                  >
                    <div class="progress-bar-full">
                      <div
                        class="progress-fill-full"
                        :style="{ width: `${event.progressPercentage}%` }"
                      ></div>
                    </div>
                    <span class="progress-text-full"
                      >{{ event.progressPercentage }}% Complete</span
                    >
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- Add/Edit Task Dialog -->
      <el-dialog
        v-model="taskDialogVisible"
        :title="isEditing ? 'Edit Task' : 'Add New Task'"
        width="600px"
        :z-index="isEditing ? 1500 : 1000"
        @closed="resetTaskForm"
      >
        <el-form
          ref="taskFormRef"
          :model="taskForm"
          :rules="taskFormRules"
          label-width="120px"
        >
          <el-form-item label="Title" prop="title">
            <el-input v-model="taskForm.title" placeholder="Enter task title" />
          </el-form-item>

          <el-form-item label="Description" prop="description">
            <el-input
              v-model="taskForm.description"
              type="textarea"
              :rows="3"
              placeholder="Enter task description"
            />
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Priority" prop="priority">
                <el-select v-model="taskForm.priority" style="width: 100%">
                  <el-option label="High" value="high" />
                  <el-option label="Medium" value="medium" />
                  <el-option label="Low" value="low" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Start Time" prop="startDate">
                <el-date-picker
                  v-model="taskForm.startDate"
                  type="datetime"
                  placeholder="Choose start date and time"
                  style="width: 100%"
                  :disabled-date="disableStartDate"
                  @change="onStartDateChange"
                  format="YYYY-MM-DD HH:mm"
                  :picker-options="{
                    shortcuts: [
                      {
                        text: 'Now',
                        onClick(picker) {
                          const now = new Date();
                          // Set to next 15-minute interval for practical scheduling
                          const minutes = now.getMinutes();
                          const roundedMinutes = Math.ceil(minutes / 15) * 15;
                          now.setMinutes(roundedMinutes);
                          now.setSeconds(0, 0);
                          picker.$emit('pick', now);
                        },
                      },
                      {
                        text: 'Tomorrow 9AM',
                        onClick(picker) {
                          const tomorrow = new Date();
                          tomorrow.setDate(tomorrow.getDate() + 1);
                          tomorrow.setHours(9, 0, 0, 0);
                          picker.$emit('pick', tomorrow);
                        },
                      },
                    ],
                  }"
                  class="enhanced-datetime-picker"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="End Time" prop="endDate">
                <el-date-picker
                  v-model="taskForm.endDate"
                  type="datetime"
                  placeholder="Choose end date and time"
                  style="width: 100%"
                  :disabled-date="disableEndDate"
                  @change="onEndDateChange"
                  format="YYYY-MM-DD HH:mm"
                  :picker-options="{
                    shortcuts: [
                      {
                        text: '+1 Hour',
                        onClick(picker) {
                          if (!taskForm.startDate) {
                            ElMessage.warning('Please select start date first');
                            return;
                          }
                          const startTime =
                            taskForm.startDate instanceof Date
                              ? taskForm.startDate
                              : new Date(taskForm.startDate);
                          const endTime = new Date(
                            startTime.getTime() + 60 * 60 * 1000
                          );
                          picker.$emit('pick', endTime);
                        },
                      },
                      {
                        text: '+2 Hours',
                        onClick(picker) {
                          if (!taskForm.startDate) {
                            ElMessage.warning('Please select start date first');
                            return;
                          }
                          const startTime =
                            taskForm.startDate instanceof Date
                              ? taskForm.startDate
                              : new Date(taskForm.startDate);
                          const endTime = new Date(
                            startTime.getTime() + 2 * 60 * 60 * 1000
                          );
                          picker.$emit('pick', endTime);
                        },
                      },
                    ],
                  }"
                  class="enhanced-datetime-picker"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="Assignee" prop="assignee">
            <el-select v-model="taskForm.assignee" style="width: 100%">
              <el-option
                v-for="staff in staffList"
                :key="staff.id"
                :label="staff.name"
                :value="staff.userId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="Location" prop="location">
            <el-input
              v-model="taskForm.location"
              placeholder="Enter location"
            />
          </el-form-item>

        </el-form>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="taskDialogVisible = false">Cancel</el-button>
            <el-button type="primary" @click="saveTask" :loading="saving">
              {{ isEditing ? "Update" : "Create" }}
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Event Details Dialog -->
      <el-dialog
        v-model="eventDetailsVisible"
        title="Task Details"
        width="500px"
      >
        <div v-if="selectedEvent" class="event-details">
          <div class="detail-header">
            <h3>{{ selectedEvent.title }}</h3>
            <el-tag :type="getEventTagType(selectedEvent.type)">
              {{ selectedEvent.type }}
            </el-tag>
          </div>

          <el-descriptions :column="1" border>
            <el-descriptions-item label="Description">
              {{ selectedEvent.description || "No description" }}
            </el-descriptions-item>
            <el-descriptions-item label="Start Time">
              {{ formatDateTime(selectedEvent.startDate) }}
            </el-descriptions-item>
            <el-descriptions-item label="End Time">
              {{ formatDateTime(selectedEvent.endDate) }}
            </el-descriptions-item>
            <el-descriptions-item label="Assignee">
              {{ selectedEvent.assignee }}
            </el-descriptions-item>
            <el-descriptions-item label="Location">
              {{ selectedEvent.location || "Not specified" }}
            </el-descriptions-item>
            <el-descriptions-item label="Status">
              <el-tag :type="selectedEvent.completed ? 'success' : 'warning'">
                {{ selectedEvent.completed ? "Completed" : "Pending" }}
              </el-tag>
            </el-descriptions-item>
            <!-- Completion Details (for completed tasks) -->
            <el-descriptions-item v-if="selectedEvent.completed" label="Completed At">
              <span v-if="selectedEvent.completedAt">{{ formatDateTime(selectedEvent.completedAt) }}</span>
              <span v-else class="text-muted">Not recorded</span>
            </el-descriptions-item>
            <el-descriptions-item v-if="selectedEvent.completed" label="Actual Duration">
              <span v-if="selectedEvent.actualDuration !== null && selectedEvent.actualDuration !== undefined">
                {{ selectedEvent.actualDuration }} minute{{ selectedEvent.actualDuration !== 1 ? 's' : '' }}
              </span>
              <span v-else class="text-muted">Not recorded</span>
            </el-descriptions-item>
            <el-descriptions-item v-if="selectedEvent.completed" label="Completion Notes">
              <span v-if="selectedEvent.completionNotes">{{ selectedEvent.completionNotes }}</span>
              <span v-else class="text-muted">No notes</span>
            </el-descriptions-item>
          </el-descriptions>

          <div class="event-actions">
            <el-button
              v-if="!selectedEvent.completed"
              type="success"
              @click="markAsCompleted"
            >
              Mark as Completed
            </el-button>
            <el-button type="primary" @click="editEvent"> Edit Task </el-button>
            <el-button
              v-if="canDeleteTask(selectedEvent)"
              type="danger"
              @click="deleteEvent"
            >
              Delete Task
            </el-button>
          </div>
        </div>
      </el-dialog>

      <!-- View Tasks Dialog -->
      <ViewTaskDialog
        v-model:visible="viewTaskDialogVisible"
        :staff-list="staffList"
        @task-updated="handleTaskUpdated"
        @task-deleted="handleTaskDeleted"
        @edit-task="handleEditTask"
      />

      <!-- Reschedule Confirmation Dialog -->
      <el-dialog
        v-model="rescheduleDialogVisible"
        title="Confirm Task Reschedule"
        width="500px"
        :close-on-click-modal="false"
      >
        <div class="reschedule-dialog-content">
          <div class="reschedule-task-info">
            <div class="reschedule-details">
              <h4>{{ rescheduleConfirmation.task?.title }}</h4>
              <p class="reschedule-times">
                <span class="from-time">{{ rescheduleConfirmation.fromDate }}</span>
                <el-icon><ArrowRight /></el-icon>
                <span class="to-time">{{ formatDateTime(rescheduleConfirmation.toDate) }}</span>
              </p>
            </div>
          </div>

          <!-- Conflict Warning -->
          <div v-if="rescheduleConfirmation.hasConflict" class="conflict-warning">
            <el-alert
              title="Scheduling Conflict Detected"
              type="warning"
              :closable="false"
              show-icon
            >
              <template #default>
                <p>Another task is scheduled for this time slot.</p>
                <div v-if="rescheduleConfirmation.conflictResolution" class="alternative-suggestion">
                  <p><strong>Alternative suggestion:</strong></p>
                  <p>{{ formatDateTime(rescheduleConfirmation.conflictResolution.date) }}</p>
                  <p class="reason">{{ rescheduleConfirmation.conflictResolution.reason }}</p>
                </div>
              </template>
            </el-alert>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="cancelReschedule">Cancel</el-button>
            <el-button
              v-if="rescheduleConfirmation.hasConflict && rescheduleConfirmation.conflictResolution"
              type="primary"
              @click="confirmReschedule(true)"
            >
              Use Alternative Time
            </el-button>
            <el-button
              type="primary"
              @click="confirmReschedule(false)"
            >
              Confirm Reschedule
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Undo Toast -->
      <el-notification
        v-if="showUndoToast"
        title="Task Rescheduled"
        message="Click to undo this action"
        type="info"
        :duration="5000"
        :closable="true"
        position="bottom-right"
        @click="undoLastAction"
        class="undo-notification"
      >
        <template #default>
          <div class="undo-content">
            <span>Task rescheduled successfully</span>
            <el-button size="small" type="primary" @click="undoLastAction">
              Undo
            </el-button>
          </div>
        </template>
      </el-notification>
    </div>
  </SupervisorLayout>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import SupervisorLayout from "@/components/SupervisorLayout.vue";
import ViewTaskDialog from "@/components/ViewTaskDialog.vue";
import { API } from "@/utils/request";
import { AuthUtils } from "@/utils/auth";
import {
  Plus,
  Refresh,
  Calendar,
  CircleCheck,
  Clock,
  Warning,
  ArrowLeft,
  ArrowRight,
  View,
} from "@element-plus/icons-vue";

export default {
  name: "SupervisorTaskCalendar",
  components: {
    SupervisorLayout,
    ViewTaskDialog,
    Plus,
    Refresh,
    Calendar,
    CircleCheck,
    Clock,
    Warning,
    ArrowLeft,
    ArrowRight,
    View,
  },
  setup() {
    // Reactive data
    const loading = ref(false);
    const syncing = ref(false);
    const saving = ref(false);
    const currentView = ref("month");
    const selectedDate = ref(new Date());
    const taskDialogVisible = ref(false);
    const eventDetailsVisible = ref(false);
    const isEditing = ref(false);
    const selectedEvent = ref(null);
    const viewTaskDialogVisible = ref(false);

    // Auto-refresh functionality
    const autoRefreshInterval = ref(null);
    const autoRefreshEnabled = ref(true);
    const lastRefreshTime = ref(new Date());

    // Form refs
    const taskFormRef = ref(null);

    // Calendar data
    const weekDays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

    // Dynamic staff data
    const staffList = ref([]);

    // Dynamic tasks data from MySQL database
    const tasks = ref([]);

    // Task form
    const taskForm = reactive({
      title: "",
      description: "",
      type: "cleaning",
      priority: "medium",
      startDate: "",
      endDate: "",
      assignee: "",
      location: "",
    });

    // Enhanced custom validators for comprehensive date logic using UTC
    const validateStartDate = (rule, value, callback) => {
      // Skip validation if value is empty/null/undefined (user hasn't selected date yet)
      if (!value || value === "" || value === null || value === undefined) {
        callback(); // Allow empty values - required validation handled separately
        return;
      }

      // Additional check for invalid values that might trigger validation
      if (typeof value === "string" && value.trim() === "") {
        callback();
        return;
      }

      const selectedDate = new Date(value);

      // Validate date object is valid
      if (isNaN(selectedDate.getTime())) {
        callback(new Error("Invalid date selected"));
        return;
      }

      // Use proper UTC time handling with backend (10 minutes tolerance)
      const now = new Date();
      // Get current UTC time correctly - don't double convert
      const utcNow = now; // Browser Date already handles local timezone
      const minAllowedTime = new Date(utcNow.getTime() - 10 * 60000); // 10 minutes tolerance

      if (selectedDate < minAllowedTime) {
        callback(
          new Error("Start date cannot be more than 10 minutes in the past")
        );
        return;
      }

      // Prevent scheduling more than 1 year in the future
      const maxStartTime = new Date(
        utcNow.getTime() + 365 * 24 * 60 * 60 * 1000
      ); // 1 year from now
      if (selectedDate > maxStartTime) {
        callback(
          new Error("Start date cannot be more than 1 year in the future")
        );
        return;
      }

      callback();
    };

    const validateEndDate = (rule, value, callback) => {
      // Skip validation if value is empty/null/undefined (user hasn't selected date yet)
      if (!value || value === "" || value === null || value === undefined) {
        callback(); // Allow empty values - required validation handled separately
        return;
      }

      // Additional check for invalid values that might trigger validation
      if (typeof value === "string" && value.trim() === "") {
        callback();
        return;
      }

      // Skip validation if start date is not set yet
      if (
        !taskForm.startDate ||
        taskForm.startDate === "" ||
        taskForm.startDate === null ||
        taskForm.startDate === undefined
      ) {
        callback(); // Will be re-validated when start date is set
        return;
      }

      const startTime =
        taskForm.startDate instanceof Date
          ? taskForm.startDate
          : new Date(taskForm.startDate);
      const endTime = value instanceof Date ? value : new Date(value);

      // Validate both dates are valid
      if (isNaN(startTime.getTime()) || isNaN(endTime.getTime())) {
        callback(new Error("Invalid date selected"));
        return;
      }

      const diffMilliseconds = endTime.getTime() - startTime.getTime();
      const diffMinutes = diffMilliseconds / (1000 * 60);

      // End date must be after start date
      if (diffMilliseconds <= 0) {
        callback(new Error("End date must be after start date"));
        return;
      }

      // Minimum 15-minute duration requirement
      if (diffMinutes < 15) {
        callback(new Error("Task duration must be at least 15 minutes"));
        return;
      }

      // Maximum 24-hour duration requirement (reasonable business constraint)
      if (diffMinutes > 1440) {
        callback(
          new Error(
            "Task duration cannot exceed 24 hours. Create separate tasks for longer work."
          )
        );
        return;
      }

      callback();
    };

    // Form validation rules
    const taskFormRules = {
      title: [
        { required: true, message: "Please enter task title", trigger: "blur" },
      ],
      type: [
        {
          required: true,
          message: "Please select task type",
          trigger: "change",
        },
      ],
      startDate: [
        {
          required: true,
          message: "Please select start date",
          trigger: "change",
        },
        { validator: validateStartDate, trigger: "blur" }, // Changed from 'change' to 'blur' to avoid premature validation
      ],
      endDate: [
        {
          required: true,
          message: "Please select end date",
          trigger: "change",
        },
        { validator: validateEndDate, trigger: "blur" }, // Changed from 'change' to 'blur' to avoid premature validation
      ],
      assignee: [
        {
          required: true,
          message: "Please select assignee",
          trigger: "change",
        },
      ],
    };

    // Real-time calendar statistics
    const calendarStats = ref({
      totalTasks: 0,
      completedTasks: 0,
      upcomingTasks: 0,
      overdueTasks: 0,
    });

    // Supervisor-specific permission check for task deletion
    const canDeleteTask = (task) => {
      const currentUserId = AuthUtils.getUserId();
      // Supervisor can only delete tasks they created, not admin-assigned tasks
      return (
        task.assignedByName === "Supervisor" ||
        task.assignedBy === currentUserId
      );
    };

    // Fetch real-time task statistics for supervisor team
    const fetchCalendarStats = async () => {
      try {
        // Calculate stats from team tasks instead of system-wide stats
        const totalTasks = tasks.value.length;
        const completedTasks = tasks.value.filter(t => t.completed || t.status === 'completed').length;
        const upcomingTasks = tasks.value.filter(t => !t.completed && t.status === 'pending').length;
        const overdueTasks = tasks.value.filter(t => {
          if (t.completed || t.status === 'completed') return false;
          const taskEndDate = new Date(t.endDate || t.dueDate);
          return taskEndDate < new Date();
        }).length;

        calendarStats.value = {
          totalTasks,
          completedTasks,
          upcomingTasks,
          overdueTasks,
        };
      } catch (error) {
        console.error("Failed to calculate calendar stats:", error);
      }
    };

    const currentPeriodTitle = computed(() => {
      const date = selectedDate.value;
      const options = { year: "numeric", month: "long" };

      if (currentView.value === "month") {
        return date.toLocaleDateString("en-GB", options);
      } else if (currentView.value === "week") {
        const weekStart = getWeekStart(date);
        const weekEnd = getWeekEnd(date);
        return `${weekStart.toLocaleDateString("en-GB", {
          month: "short",
          day: "numeric",
        })} - ${weekEnd.toLocaleDateString("en-GB", {
          month: "short",
          day: "numeric",
          year: "numeric",
        })}`;
      } else {
        return date.toLocaleDateString("en-GB", {
          weekday: "long",
          year: "numeric",
          month: "long",
          day: "numeric",
        });
      }
    });

    const monthDates = computed(() => {
      const year = selectedDate.value.getFullYear();
      const month = selectedDate.value.getMonth();
      const firstDay = new Date(year, month, 1);
      // const lastDay = new Date(year, month + 1, 0); // 暂时不需要使用
      const startDate = new Date(firstDay);
      startDate.setDate(startDate.getDate() - firstDay.getDay());

      const dates = [];
      const today = new Date();

      for (let i = 0; i < 42; i++) {
        const date = new Date(startDate);
        date.setDate(startDate.getDate() + i);

        const dateEvents = tasks.value.filter((event) => {
          const eventDate = new Date(event.startDate);
          return eventDate.toDateString() === date.toDateString();
        });

        dates.push({
          date: new Date(date),
          day: date.getDate(),
          isCurrentMonth: date.getMonth() === month,
          isToday: date.toDateString() === today.toDateString(),
          isSelected: date.toDateString() === selectedDate.value.toDateString(),
          events: dateEvents,
        });
      }

      return dates;
    });

    const weekDates = computed(() => {
      const weekStart = getWeekStart(selectedDate.value);
      const dates = [];

      for (let i = 0; i < 7; i++) {
        const date = new Date(weekStart);
        date.setDate(weekStart.getDate() + i);

        const dateEvents = tasks.value.filter((event) => {
          const eventDate = new Date(event.startDate);
          return eventDate.toDateString() === date.toDateString();
        });

        dates.push({
          date: new Date(date),
          day: date.getDate(),
          dayName: weekDays[date.getDay()],
          dateString: date.toDateString(),
          isToday: date.toDateString() === new Date().toDateString(),
          events: dateEvents,
        });
      }

      return dates;
    });

    const dayEvents = computed(() => {
      return tasks.value
        .filter((event) => {
          const eventDate = new Date(event.startDate);
          return eventDate.toDateString() === selectedDate.value.toDateString();
        })
        .sort((a, b) => new Date(a.startDate) - new Date(b.startDate));
    });

    // Methods
    const getWeekStart = (date) => {
      const d = new Date(date);
      const day = d.getDay();
      const diff = d.getDate() - day;
      return new Date(d.setDate(diff));
    };

    const getWeekEnd = (date) => {
      const weekStart = getWeekStart(date);
      const weekEnd = new Date(weekStart);
      weekEnd.setDate(weekStart.getDate() + 6);
      return weekEnd;
    };

    const changeView = (view) => {
      currentView.value = view;
      // Scroll to working hours when switching to week view
      if (view === 'week') {
        setTimeout(() => {
          scrollToWorkingHours();
        }, 100);
      }
    };

    // Scroll week view to show working hours (8:00 AM)
    const scrollToWorkingHours = () => {
      const weekGrid = document.querySelector('.week-grid');
      if (weekGrid && currentView.value === 'week') {
        // Scroll to 8:00 AM (480px = 8 hours * 60px per hour)
        const targetScroll = 8 * 60;
        weekGrid.parentElement.scrollTop = targetScroll;
        console.log('Scrolled week view to 8:00 AM position');
      }
    };

    const onDateChange = (date) => {
      if (date) {
        selectedDate.value = new Date(date);
      }
    };

    const goToToday = () => {
      selectedDate.value = new Date();
    };

    const previousPeriod = () => {
      const date = new Date(selectedDate.value);
      if (currentView.value === "month") {
        date.setMonth(date.getMonth() - 1);
      } else if (currentView.value === "week") {
        date.setDate(date.getDate() - 7);
      } else {
        date.setDate(date.getDate() - 1);
      }
      selectedDate.value = date;
    };

    const nextPeriod = () => {
      const date = new Date(selectedDate.value);
      if (currentView.value === "month") {
        date.setMonth(date.getMonth() + 1);
      } else if (currentView.value === "week") {
        date.setDate(date.getDate() + 7);
      } else {
        date.setDate(date.getDate() + 1);
      }
      selectedDate.value = date;
    };

    const selectDate = (dateObj) => {
      selectedDate.value = dateObj.date;
    };

    // Fetch assignable users for task assignment (supervisor-specific endpoint)
    const fetchAssignableUsers = async () => {
      try {
        const response = await API.get("/api/supervisor/assignable-users");
        if (response.data && Array.isArray(response.data)) {
          staffList.value = response.data.map((user) => ({
            id: user.userId,
            name: user.fullName || user.username,
            userId: user.userId,
            username: user.username,
            role: user.role,
          }));
          console.log(
            "Successfully loaded assignable users:",
            staffList.value.length
          );
        } else {
          console.log("No assignable users found");
          staffList.value = [];
        }
      } catch (error) {
        console.error("Failed to fetch assignable users:", error);
        // Don't show error message to user - this is handled by axios interceptor
        // Fallback to empty list
        staffList.value = [];
      }
    };

    // Helper function to get assignee name from user ID
    const getAssigneeName = (userId) => {
      if (!userId || !staffList.value.length) return "Unassigned";
      const staff = staffList.value.find((s) => s.userId === parseInt(userId));
      return staff ? staff.name : "Unknown User";
    };

    // Fetch real tasks from database with enhanced data processing
    const fetchTasks = async () => {
      try {
        loading.value = true;
        // First get supervisor's team members, then get their tasks
        const staffResponse = await API.get("/api/supervisor/staff");
        let allTasks = [];

        // First, get supervisor's own created tasks (including pending/unassigned ones)
        try {
          const myTasksResponse = await API.get("/api/supervisor/my-tasks");
          if (myTasksResponse.data && myTasksResponse.data.tasks) {
            allTasks = allTasks.concat(myTasksResponse.data.tasks);
          }
        } catch (error) {
          console.warn("Failed to fetch supervisor's own tasks:", error);
        }

        if (staffResponse.data && Array.isArray(staffResponse.data)) {
          // Get tasks for each team member
          for (const staffMember of staffResponse.data) {
            try {
              const tasksResponse = await API.get(`/api/supervisor/staff/${staffMember.userId}/tasks`);
              if (tasksResponse.data && tasksResponse.data.tasks) {
                allTasks = allTasks.concat(tasksResponse.data.tasks);
              }
            } catch (error) {
              console.warn(`Failed to fetch tasks for staff member ${staffMember.userId}:`, error);
            }
          }
        }

        // Remove duplicate tasks (in case same task appears in both supervisor's tasks and staff tasks)
        const uniqueTasks = [];
        const seenTaskIds = new Set();

        for (const task of allTasks) {
          const taskId = task.taskId || task.id;
          if (!seenTaskIds.has(taskId)) {
            seenTaskIds.add(taskId);
            uniqueTasks.push(task);
          }
        }

        const response = { data: uniqueTasks };

        if (response.data && Array.isArray(response.data)) {
          // Convert backend Task entities to frontend calendar format
          tasks.value = response.data.map((task) => {
            // Determine task type based on title/description keywords
            let taskType = "cleaning"; // Default
            const titleLower = (task.title || "").toLowerCase();
            const descLower = (task.description || "").toLowerCase();

            if (
              titleLower.includes("meeting") ||
              descLower.includes("meeting")
            ) {
              taskType = "meeting";
            }

            return {
              id: task.taskId,
              title: task.title || "Untitled Task",
              description: task.description || "",
              type: taskType,
              priority: task.priority || "medium",
              startDate: task.scheduledTime
                ? new Date(task.scheduledTime)
                : new Date(),
              endDate: task.dueDate
                ? new Date(task.dueDate)
                : task.scheduledTime
                ? new Date(
                    new Date(task.scheduledTime).getTime() + 2 * 60 * 60 * 1000
                  ) // Default 2 hours duration
                : new Date(new Date().getTime() + 2 * 60 * 60 * 1000),
              // Preserve original backend fields for drag & drop compatibility
              scheduledTime: task.scheduledTime,
              dueDate: task.dueDate,
              assignee: getAssigneeName(task.assignedTo),
              assigneeId: task.assignedTo,
              location: task.location || "Not specified",
              completed: task.status === "completed",
              status: task.status || "pending",
              source: "database",
              progressPercentage: task.progressPercentage || 0,
              // Additional fields for calendar display
              createdAt: task.createdAt,
              startedAt: task.startedAt,
              completedAt: task.completedAt,
              estimatedDuration: task.estimatedDuration || 120, // Default 2 hours in minutes
              actualDuration: task.actualDuration,
              completionNotes: task.completionNotes,
              instructions: task.instructions,
              notes: task.notes,
              assignedBy: task.assignedBy,
              assignedByName: getAssigneeName(task.assignedBy) || "System",
            };
          });

          console.log(
            `Successfully loaded ${tasks.value.length} tasks from database`
          );

          // Sort tasks by scheduled time for better calendar display
          tasks.value.sort(
            (a, b) => new Date(a.startDate) - new Date(b.startDate)
          );
        } else {
          console.log("No tasks found in database");
          tasks.value = [];
        }

        // Always refresh calendar statistics after loading tasks
        await fetchCalendarStats();
      } catch (error) {
        console.error("Failed to fetch tasks:", error);
        ElMessage.warning(
          "Unable to load tasks. Please refresh the page or check your connection."
        );
        // Initialize empty tasks array on error
        tasks.value = [];

        // Still try to load fallback statistics
        try {
          await fetchCalendarStats();
        } catch (statsError) {
          console.error("Failed to fetch calendar stats:", statsError);
        }
      } finally {
        loading.value = false;
      }
    };

    const showAddTaskDialog = () => {
      isEditing.value = false;
      taskDialogVisible.value = true;
    };

    const showViewTaskDialog = () => {
      viewTaskDialogVisible.value = true;
    };

    const openEventDetails = (event) => {
      selectedEvent.value = event;
      eventDetailsVisible.value = true;
    };

    const editEvent = () => {
      if (selectedEvent.value) {
        isEditing.value = true;
        Object.assign(taskForm, selectedEvent.value);
        eventDetailsVisible.value = false;
        taskDialogVisible.value = true;
      }
    };

    const markAsCompleted = async () => {
      if (selectedEvent.value) {
        try {
          // Call API to mark task as completed
          if (selectedEvent.value.source === "database") {
            await API.put(`/tasks/${selectedEvent.value.id}/complete`);
          }

          ElMessage.success("Task marked as completed");
          eventDetailsVisible.value = false;

          // Immediately refresh calendar data
          await refreshTasksAfterOperation();

        } catch (error) {
          console.error("Failed to mark task as completed:", error);
          ElMessage.error(
            "Failed to mark task as completed. Please try again."
          );
        }
      }
    };

    const deleteEvent = async () => {
      try {
        // Check permission first
        if (!canDeleteTask(selectedEvent.value)) {
          ElMessage.error(
            "You can only delete tasks that you created. Admin-assigned tasks cannot be deleted."
          );
          return;
        }

        await ElMessageBox.confirm(
          "Are you sure you want to delete this task?",
          "Delete Task",
          {
            confirmButtonText: "Delete",
            cancelButtonText: "Cancel",
            type: "error",
          }
        );

        // Call API to delete task
        if (selectedEvent.value.source === "database") {
          await API.delete(`/api/tasks/${selectedEvent.value.id}`);
        }

        ElMessage.success("Task deleted successfully");
        eventDetailsVisible.value = false;

        // Immediately refresh calendar data
        await refreshTasksAfterOperation();

      } catch (error) {
        if (error !== "cancel") {
          // User didn't cancel
          console.error("Failed to delete task:", error);
          ElMessage.error("Failed to delete task. Please try again.");
        }
      }
    };

    const resetTaskForm = () => {
      Object.assign(taskForm, {
        title: "",
        description: "",
        type: "cleaning",
        priority: "medium",
        startDate: "",
        endDate: "",
        assignee: "",
        location: "",
        });
      if (taskFormRef.value) {
        taskFormRef.value.clearValidate();
      }
    };

    const saveTask = async () => {
      if (!taskFormRef.value) return;

      try {
        await taskFormRef.value.validate();
        saving.value = true;

        // Prepare task data for backend Task entity with proper date serialization
        const taskData = {
          title: taskForm.title,
          description: taskForm.description,
          location: taskForm.location,
          status: "pending",
          priority: taskForm.priority,
          // Ensure dates are properly serialized as UTC ISO strings for consistent backend processing
          scheduledTime: taskForm.startDate
            ? (() => {
                try {
                  const date =
                    taskForm.startDate instanceof Date
                      ? new Date(taskForm.startDate.getTime())
                      : new Date(taskForm.startDate);
                  if (isNaN(date.getTime())) {
                    throw new Error("Invalid start date");
                  }
                  // Convert to UTC and format as ISO string without timezone offset
                  const utcDate = new Date(
                    date.getTime() - date.getTimezoneOffset() * 60000
                  );
                  return utcDate.toISOString().slice(0, 19); // Remove 'Z' suffix for LocalDateTime compatibility
                } catch (error) {
                  console.error("Start date serialization error:", error);
                  throw new Error("Invalid start date for API submission");
                }
              })()
            : null,
          estimatedDuration: 60, // Default 1 hour in minutes
          progressPercentage: 0,
          instructions: taskForm.description,
          assignedTo: taskForm.assignee ? parseInt(taskForm.assignee) : null,
          assignedBy: AuthUtils.getUserId(),
          // Ensure end date is properly serialized as UTC ISO string for consistent backend processing
          dueDate: taskForm.endDate
            ? (() => {
                try {
                  const date =
                    taskForm.endDate instanceof Date
                      ? new Date(taskForm.endDate.getTime())
                      : new Date(taskForm.endDate);
                  if (isNaN(date.getTime())) {
                    throw new Error("Invalid end date");
                  }
                  // Convert to UTC and format as ISO string without timezone offset
                  const utcDate = new Date(
                    date.getTime() - date.getTimezoneOffset() * 60000
                  );
                  return utcDate.toISOString().slice(0, 19); // Remove 'Z' suffix for LocalDateTime compatibility
                } catch (error) {
                  console.error("End date serialization error:", error);
                  throw new Error("Invalid end date for API submission");
                }
              })()
            : null,
        };

        let response;
        if (isEditing.value) {
          // Update existing task
          response = await API.put(
            `/tasks/${selectedEvent.value.id}`,
            taskData
          );

          ElMessage.success("Task updated successfully");

          // Immediately refresh calendar data
          await refreshTasksAfterOperation();
        } else {
          // Create new task
          response = await API.post("/api/tasks", taskData);

          if (response.data && response.data.success && response.data.taskId) {
            ElMessage.success("Task created successfully");
          } else if (response.status === 201 && response.data) {
            ElMessage.success("Task created successfully");
          } else {
            throw new Error(
              response.data?.message ||
                "Task creation failed - invalid server response"
            );
          }

          // Immediately refresh calendar data for new tasks
          await refreshTasksAfterOperation();
        }


        taskDialogVisible.value = false;
        resetTaskForm();
      } catch (error) {
        console.error("Task operation failed:", error);
        ElMessage.error(
          error.response?.data?.message ||
            error.message ||
            "Failed to save task. Please try again."
        );
      } finally {
        saving.value = false;
      }
    };



    // Utility methods
    const formatEventTime = (event) => {
      const start = new Date(event.startDate);
      const end = new Date(event.endDate);
      return `${start.toLocaleTimeString("en-GB", {
        hour: "2-digit",
        minute: "2-digit",
      })} - ${end.toLocaleTimeString("en-GB", {
        hour: "2-digit",
        minute: "2-digit",
      })}`;
    };

    const formatDateTime = (date) => {
      return new Date(date).toLocaleString("en-GB", {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    };

    const formatDate = (date) => {
      return new Date(date).toLocaleDateString("en-GB", {
        weekday: "long",
        year: "numeric",
        month: "long",
        day: "numeric",
      });
    };

    const formatHour = (hour) => {
      return `${hour.toString().padStart(2, "0")}:00`;
    };

    const getEventTagType = (type) => {
      const types = {
        cleaning: "success",
        meeting: "primary",
      };
      return types[type] || "default";
    };

    const getEventStyle = (event) => {
      const start = new Date(event.startDate);
      const end = new Date(event.endDate);
      const startHour = start.getHours();
      const startMinutes = start.getMinutes();
      const duration = (end - start) / (1000 * 60 * 60); // Duration in hours

      // Calculate precise positioning based on hours and minutes
      const topPosition = startHour * 60 + startMinutes;
      const heightValue = Math.max(duration * 60, 30); // Minimum 30px height for visibility

      return {
        top: `${topPosition}px`,
        height: `${heightValue}px`,
      };
    };

    // Enhanced date picker constraint functions with better logic
    const disableStartDate = (date) => {
      // Only disable dates that are more than 1 day in the past (allow today and future)
      const today = new Date();
      today.setHours(0, 0, 0, 0); // Set to beginning of today

      // Disable dates more than 1 year in the future
      const maxDate = new Date();
      maxDate.setFullYear(maxDate.getFullYear() + 1);

      return date < today || date > maxDate;
    };

    const disableEndDate = (date) => {
      // If start date is selected, only disable dates before the start date
      if (taskForm.startDate) {
        const startDate =
          taskForm.startDate instanceof Date
            ? new Date(taskForm.startDate)
            : new Date(taskForm.startDate);
        // Only disable dates that are before the start date (not time-specific)
        startDate.setHours(0, 0, 0, 0);
        const compareDate = new Date(date);
        compareDate.setHours(0, 0, 0, 0);

        return compareDate.getTime() < startDate.getTime();
      }

      // If no start date, allow today and future dates
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return date < today;
    };

    // Enhanced date change handlers with better validation
    const onStartDateChange = (value) => {
      console.log(
        "Start date changed:",
        value,
        typeof value,
        value instanceof Date
      );
      if (!value) {
        taskForm.endDate = null;
        return;
      }

      // Ensure value is a Date object with comprehensive validation
      let startTime;
      try {
        if (value instanceof Date) {
          startTime = new Date(value.getTime());
        } else if (typeof value === "string") {
          startTime = new Date(value);
        } else {
          throw new Error("Invalid date format");
        }
      } catch (error) {
        ElMessage.error("Invalid start date format selected");
        taskForm.startDate = null;
        return;
      }

      // Validate the date
      if (isNaN(startTime.getTime())) {
        ElMessage.error("Invalid start date selected");
        taskForm.startDate = null;
        return;
      }

      // If end date is already selected and is invalid, suggest a default end time
      if (taskForm.endDate) {
        const endTime =
          taskForm.endDate instanceof Date
            ? taskForm.endDate
            : new Date(taskForm.endDate);

        // If end date is before or same as start date, auto-suggest end time
        if (endTime.getTime() <= startTime.getTime()) {
          // Automatically set end time to 2 hours after start time
          const suggestedEndTime = new Date(
            startTime.getTime() + 2 * 60 * 60 * 1000
          );
          // Ensure proper reactivity by creating a new Date instance
          taskForm.endDate = new Date(suggestedEndTime.getTime());

          ElMessage.info({
            message: "End date automatically set to 2 hours after start date",
            duration: 3000,
          });
        }
      } else {
        // If no end date set, suggest a default 2-hour duration
        const suggestedEndTime = new Date(
          startTime.getTime() + 2 * 60 * 60 * 1000
        );
        // Ensure proper reactivity by creating a new Date instance
        taskForm.endDate = new Date(suggestedEndTime.getTime());
      }

      // Force refresh of end date picker constraints
      setTimeout(() => {
        if (taskFormRef.value) {
          taskFormRef.value.validateField("endDate");
        }
      }, 100);
    };

    const onEndDateChange = (value) => {
      console.log(
        "End date changed:",
        value,
        typeof value,
        value instanceof Date
      );
      if (!value) return;

      // Ensure value is a Date object with comprehensive validation
      let endTime;
      try {
        if (value instanceof Date) {
          endTime = new Date(value.getTime());
        } else if (typeof value === "string") {
          endTime = new Date(value);
        } else {
          throw new Error("Invalid date format");
        }
      } catch (error) {
        ElMessage.error("Invalid end date format selected");
        taskForm.endDate = null;
        return;
      }

      // Validate the date
      if (isNaN(endTime.getTime())) {
        ElMessage.error("Invalid end date selected");
        taskForm.endDate = null;
        return;
      }

      // If no start date selected, suggest user set start date first
      if (!taskForm.startDate) {
        ElMessage.warning({
          message: "Please select a start date first for better task planning",
          duration: 3000,
        });
        return;
      }

      // Safely handle start date with validation
      let startTime;
      try {
        if (taskForm.startDate instanceof Date) {
          startTime = new Date(taskForm.startDate.getTime());
        } else {
          startTime = new Date(taskForm.startDate);
        }
        if (isNaN(startTime.getTime())) {
          throw new Error("Invalid start date");
        }
      } catch (error) {
        ElMessage.error("Invalid start date. Please reselect start date.");
        return;
      }

      const diffMinutes =
        (endTime.getTime() - startTime.getTime()) / (1000 * 60);

      // Provide helpful feedback for validation issues
      if (endTime.getTime() <= startTime.getTime()) {
        ElMessage.warning({
          message:
            "End date must be after start date. Please choose a later time.",
          duration: 3000,
        });
        return;
      }

      if (diffMinutes < 15) {
        ElMessage.warning({
          message:
            "Task duration should be at least 15 minutes for practical scheduling",
          duration: 3000,
        });
        return;
      }

      if (diffMinutes > 1440) {
        ElMessage.info({
          message:
            "Consider splitting tasks longer than 24 hours into multiple tasks for better management",
          duration: 4000,
        });
      }

      // Success feedback for good duration selection
      if (diffMinutes >= 30 && diffMinutes <= 480) {
        const hours = Math.floor(diffMinutes / 60);
        const minutes = Math.floor(diffMinutes % 60);
        const durationText =
          hours > 0 ? `${hours}h ${minutes}m` : `${minutes}m`;
        ElMessage.success({
          message: `Task duration set to ${durationText}`,
          duration: 2000,
        });
      }

      // Validation passed - revalidate form
      if (taskFormRef.value) {
        taskFormRef.value.validateField("endDate");
      }
    };

    // Handle events from ViewTaskDialog
    const handleTaskUpdated = async () => {
      // Refresh calendar tasks when task is updated from View Tasks dialog
      await refreshTasksAfterOperation();
    };

    const handleTaskDeleted = async () => {
      // Immediately refresh calendar data to ensure consistency
      await refreshTasksAfterOperation();
    };

    const handleEditTask = (task) => {
      // Populate form with task data for editing
      isEditing.value = true;
      Object.assign(taskForm, {
        title: task.title,
        description: task.description,
        type: "cleaning", // All tasks are cleaning type
        priority: task.priority,
        startDate: task.scheduledTime ? new Date(task.scheduledTime) : "",
        endDate: task.dueDate ? new Date(task.dueDate) : "",
        assignee: task.assignedToId,
        location: task.location,
        });

      // Set selected event for editing
      selectedEvent.value = {
        id: task.taskId,
        title: task.title,
        description: task.description,
        status: task.status,
        priority: task.priority,
        startDate: task.scheduledTime
          ? new Date(task.scheduledTime)
          : new Date(),
        endDate: task.dueDate ? new Date(task.dueDate) : new Date(),
        assignee: task.assignedToName,
        location: task.location,
        completed: task.status === "completed",
        source: "api",
        progressPercentage: task.progressPercentage || 0,
      };

      // Open edit dialog with higher z-index, don't close view dialog
      taskDialogVisible.value = true;
    };

    // Auto-refresh functions
    const startAutoRefresh = () => {
      if (!autoRefreshEnabled.value) return;

      // Clear existing interval
      if (autoRefreshInterval.value) {
        clearInterval(autoRefreshInterval.value);
      }

      // Set up auto-refresh every 30 seconds
      autoRefreshInterval.value = setInterval(async () => {
        if (!loading.value && !saving.value && autoRefreshEnabled.value) {
          try {
            await refreshCalendarData();
            lastRefreshTime.value = new Date();
            console.log(
              "Calendar auto-refreshed at",
              lastRefreshTime.value.toLocaleTimeString()
            );
          } catch (error) {
            console.warn("Auto-refresh failed:", error);
          }
        }
      }, 30000); // 30 seconds

      console.log("Auto-refresh started (30 seconds interval)");
    };

    const stopAutoRefresh = () => {
      if (autoRefreshInterval.value) {
        clearInterval(autoRefreshInterval.value);
        autoRefreshInterval.value = null;
        console.log("Auto-refresh stopped");
      }
    };

    const refreshCalendarData = async () => {
      // Refresh all data without showing loading indicator to user
      await Promise.all([fetchTasks(), fetchCalendarStats()]);
    };

    const forceRefresh = async () => {
      loading.value = true;
      try {
        await refreshCalendarData();
        lastRefreshTime.value = new Date();
        ElMessage.success("Calendar refreshed successfully");
      } catch (error) {
        console.error("Force refresh failed:", error);
        ElMessage.error("Failed to refresh calendar data");
      } finally {
        loading.value = false;
      }
    };

    // Enhanced task operation with immediate refresh
    const refreshTasksAfterOperation = async () => {
      // Immediately refresh tasks and stats after any task operation
      await Promise.all([fetchTasks(), fetchCalendarStats()]);
      lastRefreshTime.value = new Date();
    };

    onMounted(async () => {
      // Initialize calendar and load real-time data from MySQL
      goToToday();

      // Load all dynamic data from database
      await Promise.all([
        fetchAssignableUsers(),
        fetchTasks(),
        fetchCalendarStats(),
      ]);

      // Start auto-refresh for real-time updates
      startAutoRefresh();

      // Scroll week view to working hours if in week mode
      setTimeout(() => {
        if (currentView.value === 'week') {
          scrollToWorkingHours();
        }
      }, 500);

      console.log(
        "Supervisor Task Calendar initialized with real-time MySQL data and auto-refresh"
      );
    });

    // Cleanup on unmount
    onUnmounted(() => {
      stopAutoRefresh();
    });

    // ===============================
    // DRAG AND DROP FUNCTIONALITY
    // ===============================

    // Drag state management
    const dragState = reactive({
      isDragging: false,
      draggedTask: null,
      draggedTaskOriginalDate: null
    });

    // Reschedule confirmation dialog
    const rescheduleDialogVisible = ref(false);
    const rescheduleConfirmation = reactive({
      task: null,
      fromDate: null,
      toDate: null,
      hasConflict: false,
      conflictResolution: null
    });

    // Undo functionality
    const undoStack = ref([]);
    const showUndoToast = ref(false);

    // Drag and Drop handlers
    const handleDragStart = (task, event) => {
      if (task.status !== 'pending') return;

      console.log("Drag started for task:", task.title);
      dragState.isDragging = true;
      dragState.draggedTask = task;
      dragState.draggedTaskOriginalDate = new Date(task.startDate);

      event.dataTransfer.effectAllowed = "move";
      event.dataTransfer.setData("application/json", JSON.stringify({
        taskId: task.id,
        taskTitle: task.title
      }));

      event.target.style.opacity = "0.6";
    };

    const handleDragEnd = (event) => {
      console.log("Drag ended");
      dragState.isDragging = false;
      dragState.draggedTask = null;
      dragState.draggedTaskOriginalDate = null;

      event.target.style.opacity = "1";
    };

    const isValidDropTarget = (targetDate) => {
      if (!dragState.isDragging || !targetDate) return false;
      // Only enable drag and drop in Month and Week views
      if (currentView.value === 'day') return false;

      const now = new Date();
      const targetDateTime = new Date(targetDate.date);

      // Only allow dropping on dates after current time (including today)
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      targetDateTime.setHours(0, 0, 0, 0);

      return targetDateTime >= today;
    };

    const handleDragOver = (targetDate, event) => {
      if (!dragState.isDragging) return;

      event.preventDefault();
      if (isValidDropTarget(targetDate)) {
        event.dataTransfer.dropEffect = "move";
      } else {
        event.dataTransfer.dropEffect = "none";
      }
    };

    const handleDragEnter = (targetDate, event) => {
      // Visual feedback is handled via CSS classes
    };

    const handleDragLeave = (targetDate, event) => {
      // Visual feedback is handled via CSS classes
    };

    const handleDrop = async (targetDate, event) => {
      console.log("Drop attempted on:", targetDate, "targetDate.date:", targetDate?.date);

      // Enhanced validation
      if (!targetDate || !targetDate.date) {
        console.error("Invalid targetDate object:", targetDate);
        ElMessage.error("Invalid drop target");
        return;
      }

      if (!dragState.isDragging || !isValidDropTarget(targetDate)) {
        ElMessage.warning("Cannot schedule task in the past");
        return;
      }

      const draggedTask = dragState.draggedTask;
      if (!draggedTask) {
        console.error("No dragged task available");
        return;
      }

      try {
        // Validate draggedTask.startDate (frontend field mapping)
        if (!draggedTask.startDate) {
          console.error("Dragged task missing startDate:", draggedTask);
          ElMessage.error("Invalid task data - missing schedule time");
          return;
        }

        // Check for time conflicts
        const hasConflict = await checkTimeConflict(draggedTask, targetDate);

        // Prepare reschedule confirmation
        rescheduleConfirmation.task = draggedTask;
        rescheduleConfirmation.fromDate = formatDateTime(draggedTask.scheduledTime);

        // Enhanced date handling with validation
        const originalTime = new Date(draggedTask.startDate);
        if (isNaN(originalTime.getTime())) {
          console.error("Invalid originalTime from startDate:", draggedTask.startDate);
          ElMessage.error("Invalid original task time");
          return;
        }

        const newDateTime = new Date(targetDate.date);
        if (isNaN(newDateTime.getTime())) {
          console.error("Invalid newDateTime from targetDate.date:", targetDate.date);
          ElMessage.error("Invalid target date");
          return;
        }

        // Preserve original time components
        newDateTime.setHours(originalTime.getHours(), originalTime.getMinutes(), originalTime.getSeconds(), originalTime.getMilliseconds());

        // Final validation before setting
        if (isNaN(newDateTime.getTime())) {
          console.error("Final newDateTime is invalid after setHours:", newDateTime);
          ElMessage.error("Failed to create valid target date");
          return;
        }

        rescheduleConfirmation.toDate = newDateTime;
        rescheduleConfirmation.hasConflict = hasConflict;

        if (hasConflict) {
          // Find alternative time slot
          const alternativeSlot = findAlternativeTimeSlot(targetDate);
          rescheduleConfirmation.conflictResolution = alternativeSlot;
        }

        // Show confirmation dialog
        rescheduleDialogVisible.value = true;

      } catch (error) {
        console.error("Error handling drop:", error);
        ElMessage.error("Failed to process task reschedule");
      }
    };

    // Time conflict detection
    const checkTimeConflict = async (task, targetDate) => {
      // Get all tasks for the target date
      const targetDateTasks = tasks.value.filter(t => {
        if (t.id === task.id) return false; // Exclude the task being moved
        const taskDate = new Date(t.startDate);
        const targetTaskDate = new Date(targetDate.date);

        return (
          taskDate.getFullYear() === targetTaskDate.getFullYear() &&
          taskDate.getMonth() === targetTaskDate.getMonth() &&
          taskDate.getDate() === targetTaskDate.getDate()
        );
      });

      return targetDateTasks.length > 0;
    };

    // Alternative time slot finder
    const findAlternativeTimeSlot = (preferredDate) => {
      // Simple implementation: suggest next day if current day has conflicts
      const nextDay = new Date(preferredDate.date);
      nextDay.setDate(nextDay.getDate() + 1);

      return {
        date: nextDay,
        reason: "Suggested next available day due to scheduling conflict"
      };
    };

    // Hour-level drag and drop methods for Week view (similar to admin implementation)
    const isValidHourDropTarget = (date, hour) => {
      if (!dragState.isDragging || !date || currentView.value !== 'week') return false;

      // Calculate the exact target datetime (date + hour)
      const targetDateTime = new Date(date.date);
      targetDateTime.setHours(hour - 1, 0, 0, 0); // hour is 1-24, convert to 0-23

      // Only allow dropping on times after current time
      const now = new Date();

      return targetDateTime >= now;
    };

    const handleHourDragOver = (date, hour, event) => {
      if (!dragState.isDragging) return;

      if (isValidHourDropTarget(date, hour)) {
        event.dataTransfer.dropEffect = "move";
      } else {
        event.dataTransfer.dropEffect = "none";
      }
    };

    const handleHourDragEnter = (date, hour, event) => {
      // Visual feedback is handled via CSS classes
    };

    const handleHourDragLeave = (date, hour, event) => {
      // Visual feedback is handled via CSS classes
    };

    const handleHourDrop = async (date, hour, event) => {
      console.log(`Hour drop attempted on: ${date.date} at hour ${hour}`);

      // Enhanced validation for hour drops
      if (!date || !date.date) {
        console.error("Invalid date object for hour drop:", date);
        ElMessage.error("Invalid drop target");
        return;
      }

      if (!dragState.isDragging || !isValidHourDropTarget(date, hour)) {
        ElMessage.warning("Cannot schedule task in the past");
        return;
      }

      const draggedTask = dragState.draggedTask;
      if (!draggedTask) {
        console.error("No dragged task available for hour drop");
        return;
      }

      try {
        // Validate draggedTask.startDate for hour drops
        if (!draggedTask.startDate) {
          console.error("Dragged task missing startDate for hour drop:", draggedTask);
          ElMessage.error("Invalid task data - missing schedule time");
          return;
        }

        // Calculate precise target datetime
        const targetDateTime = new Date(date.date);
        targetDateTime.setHours(hour - 1, 0, 0, 0); // Convert 1-24 to 0-23

        // Validate targetDateTime
        if (isNaN(targetDateTime.getTime())) {
          console.error("Invalid targetDateTime for hour drop:", targetDateTime);
          ElMessage.error("Failed to create valid target time");
          return;
        }

        // Calculate task duration to maintain the same duration
        const originalStart = new Date(draggedTask.startDate);
        const originalEnd = new Date(draggedTask.endDate);

        if (isNaN(originalStart.getTime()) || isNaN(originalEnd.getTime())) {
          console.error("Invalid original task dates:", { originalStart, originalEnd });
          ElMessage.error("Invalid original task times");
          return;
        }

        const taskDuration = originalEnd.getTime() - originalStart.getTime();

        // Calculate new end time
        const newEndTime = new Date(targetDateTime.getTime() + taskDuration);

        // Check for time conflicts (more precise check for hour-level)
        const hasConflict = await checkHourTimeConflict(draggedTask, targetDateTime, newEndTime);

        // Prepare reschedule confirmation with precise time
        rescheduleConfirmation.task = draggedTask;
        rescheduleConfirmation.fromDate = formatDateTime(draggedTask.scheduledTime);
        rescheduleConfirmation.toDate = targetDateTime; // Store raw date object
        rescheduleConfirmation.hasConflict = hasConflict;

        if (hasConflict) {
          // Find alternative time slot for hour-level
          const alternativeSlot = findAlternativeHourSlot(targetDateTime);
          rescheduleConfirmation.conflictResolution = alternativeSlot;
        }

        // Show confirmation dialog
        rescheduleDialogVisible.value = true;

      } catch (error) {
        console.error("Error handling hour drop:", error);
        ElMessage.error("Failed to process task reschedule");
      }
    };

    const checkHourTimeConflict = async (task, newStartTime, newEndTime) => {
      // Get all tasks and check for time overlap (more precise than daily check)
      const conflictingTasks = tasks.value.filter(t => {
        if (t.id === task.id) return false; // Exclude the task being moved

        const taskStart = new Date(t.startDate);
        const taskEnd = new Date(t.endDate);

        // Check for time overlap
        return (newStartTime < taskEnd && newEndTime > taskStart);
      });

      return conflictingTasks.length > 0;
    };

    const findAlternativeHourSlot = (preferredDateTime) => {
      // Find next available hour slot
      const nextHour = new Date(preferredDateTime);
      nextHour.setHours(nextHour.getHours() + 1);

      return {
        date: nextHour,
        reason: "Suggested next available hour due to scheduling conflict"
      };
    };

    // Reschedule confirmation
    const confirmReschedule = async (useAlternative = false) => {
      try {
        const task = rescheduleConfirmation.task;
        let targetDate = rescheduleConfirmation.toDate;

        // Enhanced logging for debugging
        console.log("confirmReschedule called with:", {
          useAlternative,
          task,
          targetDate,
          conflictResolution: rescheduleConfirmation.conflictResolution
        });

        if (useAlternative && rescheduleConfirmation.conflictResolution) {
          targetDate = rescheduleConfirmation.conflictResolution.date;
          console.log("Using alternative date:", targetDate);
        }

        // Enhanced validation with detailed error reporting
        if (!targetDate) {
          console.error("No target date provided");
          throw new Error('No target date provided');
        }

        if (!(targetDate instanceof Date)) {
          console.error("Target date is not a Date object:", typeof targetDate, targetDate);
          throw new Error('Target date must be a Date object');
        }

        if (isNaN(targetDate.getTime())) {
          console.error("Target date is an invalid Date object:", targetDate);
          throw new Error('Invalid target date - date object is invalid');
        }

        const taskScheduledTime = new Date(task.startDate || task.scheduledTime);
        const taskDueDate = new Date(task.endDate || task.dueDate);

        if (isNaN(taskScheduledTime.getTime()) || isNaN(taskDueDate.getTime())) {
          throw new Error('Invalid task dates');
        }

        // Save current state to undo stack
        undoStack.value.push({
          action: 'reschedule',
          taskId: task.id,
          originalStartDate: task.startDate || task.scheduledTime,
          originalEndDate: task.endDate || task.dueDate,
          timestamp: new Date()
        });

        // Calculate new end date maintaining task duration
        const originalDuration = taskDueDate.getTime() - taskScheduledTime.getTime();
        const newEndDate = new Date(targetDate.getTime() + originalDuration);

        // Validate calculated end date
        if (isNaN(newEndDate.getTime())) {
          throw new Error('Invalid calculated end date');
        }

        // Update task via API - use local time format for LocalDateTime
        const formatLocalDateTime = (date) => {
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          const hours = String(date.getHours()).padStart(2, '0');
          const minutes = String(date.getMinutes()).padStart(2, '0');
          const seconds = String(date.getSeconds()).padStart(2, '0');
          return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
        };

        const response = await API.put(`/api/tasks/${task.id}`, {
          ...task,
          scheduledTime: formatLocalDateTime(targetDate),
          dueDate: formatLocalDateTime(newEndDate)
        });

        if (response.data) {
          ElMessage.success(`Task "${task.title}" rescheduled successfully`);

          // Show undo option
          showUndoToast.value = true;
          setTimeout(() => {
            showUndoToast.value = false;
          }, 5000);

          // Refresh calendar
          await refreshTasksAfterOperation();
        }

      } catch (error) {
        console.error("Failed to reschedule task:", error);
        ElMessage.error("Failed to reschedule task. Please try again.");
      } finally {
        rescheduleDialogVisible.value = false;
        resetRescheduleConfirmation();
      }
    };

    const cancelReschedule = () => {
      rescheduleDialogVisible.value = false;
      resetRescheduleConfirmation();
    };

    const resetRescheduleConfirmation = () => {
      rescheduleConfirmation.task = null;
      rescheduleConfirmation.fromDate = null;
      rescheduleConfirmation.toDate = null;
      rescheduleConfirmation.hasConflict = false;
      rescheduleConfirmation.conflictResolution = null;
    };

    // Undo functionality
    const undoLastAction = async () => {
      if (undoStack.value.length === 0) return;

      const lastAction = undoStack.value.pop();

      try {
        if (lastAction.action === 'reschedule') {
          // Restore original task times
          const response = await API.put(`/api/tasks/${lastAction.taskId}`, {
            scheduledTime: lastAction.originalStartDate,
            dueDate: lastAction.originalEndDate
          });

          if (response.data) {
            ElMessage.success("Task reschedule undone");
            showUndoToast.value = false;
            await refreshTasksAfterOperation();
          }
        }
      } catch (error) {
        console.error("Failed to undo action:", error);
        ElMessage.error("Failed to undo action. Please try again.");
      }
    };

    return {
      // Data
      loading,
      syncing,
      saving,
      currentView,
      selectedDate,
      taskDialogVisible,
      eventDetailsVisible,
      isEditing,
      selectedEvent,
      viewTaskDialogVisible,
      taskFormRef,
      weekDays,
      staffList,
      tasks,
      taskForm,
      taskFormRules,
      autoRefreshEnabled,
      lastRefreshTime,

      // Computed
      calendarStats,
      currentPeriodTitle,
      monthDates,
      weekDates,
      dayEvents,

      // Methods
      changeView,
      onDateChange,
      goToToday,
      previousPeriod,
      nextPeriod,
      selectDate,
      fetchAssignableUsers,
      fetchTasks,
      fetchCalendarStats,
      getAssigneeName,
      showAddTaskDialog,
      showViewTaskDialog,
      openEventDetails,
      editEvent,
      markAsCompleted,
      deleteEvent,
      resetTaskForm,
      saveTask,
      forceRefresh,
      startAutoRefresh,
      stopAutoRefresh,
      refreshCalendarData,
      refreshTasksAfterOperation,
      handleTaskUpdated,
      handleTaskDeleted,
      handleEditTask,
      formatEventTime,
      formatDateTime,
      formatDate,
      formatHour,
      getEventTagType,
      getEventStyle,
      disableStartDate,
      disableEndDate,
      onStartDateChange,
      onEndDateChange,
      validateStartDate,
      validateEndDate,
      canDeleteTask,

      // Drag and Drop functionality
      dragState,
      rescheduleDialogVisible,
      rescheduleConfirmation,
      undoStack,
      showUndoToast,
      handleDragStart,
      handleDragEnd,
      isValidDropTarget,
      handleDragOver,
      handleDragEnter,
      handleDragLeave,
      handleDrop,
      checkTimeConflict,
      findAlternativeTimeSlot,
      confirmReschedule,
      cancelReschedule,

      // Hour-level drag and drop methods for Week view
      isValidHourDropTarget,
      handleHourDragOver,
      handleHourDragEnter,
      handleHourDragLeave,
      handleHourDrop,
      checkHourTimeConflict,
      findAlternativeHourSlot,
      resetRescheduleConfirmation,
      undoLastAction,
    };
  },
};
</script>

<style scoped>
/* Main container */
.task-calendar-container {
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

.page-subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* Calendar controls */
.calendar-controls {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;
}

.controls-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.controls-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.controls-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.date-picker {
  width: 200px;
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
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
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

.stat-icon-blue {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.stat-icon-green {
  background: linear-gradient(135deg, #10b981, #059669);
}

.stat-icon-orange {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.stat-icon-red {
  background: linear-gradient(135deg, #ef4444, #dc2626);
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

/* Calendar card */
.calendar-card {
  border-radius: 12px;
  border: none;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.calendar-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.calendar-legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-cleaning {
  background: #10b981;
}


/* Month view */
.month-view {
  width: 100%;
}

.month-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background: #e5e7eb;
  border-radius: 8px 8px 0 0;
}

.day-header {
  background: #f9fafb;
  padding: 12px;
  text-align: center;
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background: #e5e7eb;
  border-radius: 0 0 8px 8px;
}

.day-cell {
  background: white;
  min-height: 100px;
  padding: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;
}

.day-cell:hover {
  background: #f9fafb;
}

.day-cell.other-month {
  background: #f9fafb;
  color: #9ca3af;
}

.day-cell.today {
  background: #ecfdf5;
  border: 2px solid #10b981;
}

.day-cell.selected {
  background: #dbeafe;
  border: 2px solid #3b82f6;
}

.day-number {
  font-weight: 600;
  margin-bottom: 4px;
}

.day-events {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.event-item {
  background: #10b981;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.event-item:hover {
  opacity: 0.8;
}

/* Event type colors */
.event-cleaning {
  background: #10b981;
}


.event-meeting {
  background: #8b5cf6;
}

/* Event status modifications */
.status-completed {
  opacity: 0.8;
  border: 2px solid #059669;
}

.status-in_progress {
  border: 2px solid #3b82f6;
  animation: pulse 2s infinite;
}

.status-overdue {
  border: 2px solid #dc2626;
  background: linear-gradient(135deg, #dc2626, #b91c1c) !important;
}

.status-pending {
  border: 2px solid #f97316;
}

/* Priority indicators */
.priority-urgent {
  box-shadow: 0 0 8px rgba(220, 38, 38, 0.6);
}

.priority-high {
  box-shadow: 0 0 6px rgba(245, 158, 11, 0.6);
}

.priority-medium {
  box-shadow: 0 0 4px rgba(59, 130, 246, 0.4);
}

.priority-low {
  box-shadow: 0 0 2px rgba(156, 163, 175, 0.4);
}

.event-title {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.event-time {
  font-size: 11px;
  opacity: 0.9;
}

/* Week view */
.week-view {
  overflow-x: auto;
}

.week-header {
  display: grid;
  grid-template-columns: 80px repeat(7, 1fr);
  gap: 1px;
  background: #e5e7eb;
  border-radius: 8px 8px 0 0;
}

.time-column {
  background: #f9fafb;
}

.week-day-header {
  background: #f9fafb;
  padding: 12px;
  text-align: center;
}

.week-day-header.today {
  background: #ecfdf5;
  color: #10b981;
  font-weight: 600;
}

.week-day-name {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.week-day-number {
  font-size: 18px;
  font-weight: 600;
}

.week-grid {
  display: grid;
  grid-template-columns: 80px repeat(7, 1fr);
  gap: 1px;
  background: #e5e7eb;
  border-radius: 0 0 8px 8px;
  position: relative;
}

.time-slots {
  background: #f9fafb;
  display: flex;
  flex-direction: column;
}

.time-slot {
  height: 60px;
  padding: 4px 8px;
  font-size: 12px;
  color: #6b7280;
  border-bottom: 1px solid #e5e7eb;
}

.week-day-column {
  background: white;
  position: relative;
  min-height: 1440px; /* 24 hours * 60px */
}

.hour-slot {
  height: 60px;
  border-bottom: 1px solid #f3f4f6;
  position: relative;
  transition: all 0.2s ease;
}

/* Complete drop target styles - unified with admin panel */
.day-cell.drop-valid,
.week-day-column.drop-valid,
.hour-slot.drop-valid {
  background-color: #d1fae5 !important;
  border: 2px dashed #10b981 !important;
  box-shadow: inset 0 0 10px rgba(16, 185, 129, 0.2);
}

.day-cell.drop-invalid,
.week-day-column.drop-invalid,
.hour-slot.drop-invalid {
  background-color: #fee2e2 !important;
  border: 2px dashed #ef4444 !important;
  box-shadow: inset 0 0 10px rgba(239, 68, 68, 0.2);
}

.day-cell.drop-valid::after,
.week-day-column.drop-valid::after,
.hour-slot.drop-valid::after {
  content: "Drop here";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #10b981;
  font-size: 12px;
  font-weight: 600;
  pointer-events: none;
  background: white;
  padding: 4px 8px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.day-cell.drop-invalid::after,
.week-day-column.drop-invalid::after,
.hour-slot.drop-invalid::after {
  content: "Cannot drop here";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #ef4444;
  font-size: 12px;
  font-weight: 600;
  pointer-events: none;
  background: white;
  padding: 4px 8px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* Complete animation feedback - unified with admin panel */
.day-cell.drop-valid,
.day-cell.drop-invalid,
.week-day-column.drop-valid,
.week-day-column.drop-invalid,
.hour-slot.drop-valid,
.hour-slot.drop-invalid {
  animation: dropFeedback 0.3s ease-in-out;
}

@keyframes dropFeedback {
  0% { transform: scale(1); }
  50% { transform: scale(1.02); }
  100% { transform: scale(1); }
}

/* Mobile responsive drop target styles - unified with admin panel */
@media (max-width: 768px) {
  .day-cell.drop-valid::after,
  .day-cell.drop-invalid::after,
  .week-day-column.drop-valid::after,
  .week-day-column.drop-invalid::after,
  .hour-slot.drop-valid::after,
  .hour-slot.drop-invalid::after {
    font-size: 10px;
    padding: 2px 6px;
  }

  /* REMOVED: .hour-slot { height: 40px; } - This was causing week view layout issues
     by reducing hour slot height to 40px while event positioning still used 60px,
     resulting in misaligned tasks and excessive whitespace at bottom */
}

.week-event {
  position: absolute !important;
  left: 4px;
  right: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  z-index: 10;
}

/* Day view */
.day-view {
  padding: 16px;
}

.day-header h3 {
  margin: 0 0 16px 0;
  color: #1f2937;
}

.day-events-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.day-event-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #10b981;
  background: #f9fafb;
  cursor: pointer;
  transition: all 0.2s;
}

.day-event-item:hover {
  background: #f3f4f6;
  transform: translateX(4px);
}


.day-event-item.event-meeting {
  border-left-color: #8b5cf6;
}

.event-content {
  flex: 1;
}

.event-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.event-assignee {
  font-size: 14px;
  color: #6b7280;
}

/* Event details */
.event-details {
  padding: 16px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.detail-header h3 {
  margin: 0;
  color: #1f2937;
}

.event-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  justify-content: flex-end;
}

/* Dialog styles */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Event header and status */
.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
}

.event-status-badge {
  flex-shrink: 0;
}

.status-icon {
  font-size: 12px;
}

.status-icon.completed {
  color: #059669;
}

.status-icon.in-progress {
  color: #3b82f6;
}

.status-icon.overdue {
  color: #dc2626;
}

.status-icon.pending {
  color: #f97316;
}

/* Event details */
.event-details {
  display: flex;
  flex-direction: column;
  gap: 1px;
  font-size: 10px;
}

.event-assignee {
  color: rgba(255, 255, 255, 0.9);
  font-size: 9px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Progress bars for month view */
.event-progress {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-top: 1px;
}

.progress-bar {
  flex: 1;
  height: 2px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 1px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: rgba(255, 255, 255, 0.9);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 8px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

/* Week view enhancements */
.week-event-content {
  flex: 1;
}

.event-status-indicator {
  position: absolute;
  top: 2px;
  right: 2px;
}

.status-icon-small {
  font-size: 10px;
}

.status-icon-small.completed {
  color: #059669;
}

.status-icon-small.in-progress {
  color: #3b82f6;
}

.status-icon-small.overdue {
  color: #dc2626;
}

.status-icon-small.pending {
  color: #f97316;
}

.event-progress-small {
  margin-top: 2px;
}

.progress-text-small {
  font-size: 9px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

/* Day view enhancements */
.day-event-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #10b981;
  background: #f9fafb;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 12px;
}

.event-time-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-width: 80px;
}

.event-status-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.event-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.event-priority-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
}

.event-priority-badge.priority-urgent {
  background: #fee2e2;
  color: #dc2626;
}

.event-priority-badge.priority-high {
  background: #fef3c7;
  color: #d97706;
}

.event-priority-badge.priority-medium {
  background: #dbeafe;
  color: #2563eb;
}

.event-priority-badge.priority-low {
  background: #f3f4f6;
  color: #6b7280;
}

.event-location {
  font-size: 12px;
  color: #9ca3af;
  margin-left: 8px;
}

/* Full progress bar for day view */
.event-progress-full {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.progress-bar-full {
  flex: 1;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill-full {
  height: 100%;
  background: linear-gradient(90deg, #10b981, #059669);
  transition: width 0.3s ease;
}

.progress-text-full {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
  min-width: 80px;
}

/* Animations */
@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

/* Enhanced datetime picker styling with modern UI */
.enhanced-datetime-picker {
  border-radius: 12px;
  position: relative;
}

.enhanced-datetime-picker :deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #e4e7ed;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 12px 16px;
  background: linear-gradient(145deg, #ffffff 0%, #f8f9fa 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.enhanced-datetime-picker :deep(.el-input__wrapper):hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-1px);
}

.enhanced-datetime-picker :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.15),
    0 4px 16px rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.enhanced-datetime-picker :deep(.el-input__inner) {
  font-size: 15px;
  color: #2c3e50;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.enhanced-datetime-picker :deep(.el-input__inner)::placeholder {
  color: #95a5a6;
  font-style: normal;
  font-weight: 400;
}

/* Modern date picker dropdown styling */
:deep(.el-picker-panel) {
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(255, 255, 255, 0.5);
  border: none;
  backdrop-filter: blur(10px);
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.el-date-picker__header) {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a855f7 100%);
  color: white;
  border-radius: 16px 16px 0 0;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

:deep(.el-date-picker__header::before) {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    45deg,
    rgba(255, 255, 255, 0.1) 0%,
    transparent 100%
  );
  pointer-events: none;
}

:deep(.el-date-picker__header .el-picker-panel__icon-btn) {
  color: white;
  transition: all 0.3s ease;
  border-radius: 8px;
  width: 36px;
  height: 36px;
}

:deep(.el-date-picker__header .el-picker-panel__icon-btn):hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}

/* Beautiful shortcuts styling */
:deep(.el-picker-panel__shortcuts) {
  padding: 16px;
  background: linear-gradient(145deg, #f8fafc 0%, #e2e8f0 100%);
  border-radius: 0 0 0 16px;
}

:deep(.el-picker-panel__shortcut) {
  display: block;
  width: 100%;
  margin-bottom: 8px;
  padding: 12px 16px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(145deg, #ffffff 0%, #f1f5f9 100%);
  color: #475569;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

:deep(.el-picker-panel__shortcut):hover {
  background: linear-gradient(145deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(99, 102, 241, 0.25);
}

:deep(.el-picker-panel__shortcut):last-child {
  margin-bottom: 0;
}

/* Beautiful time panel styling */
:deep(.el-time-panel) {
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  background: linear-gradient(145deg, #ffffff 0%, #f8fafc 100%);
}

:deep(.el-time-panel__content) {
  background: transparent;
}

/* Enhanced time spinner columns */
:deep(.el-time-spinner__wrapper) {
  border-radius: 12px;
  margin: 0 4px;
  background: linear-gradient(145deg, #f8fafc 0%, #e2e8f0 100%);
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.06);
}

:deep(.el-time-spinner__wrapper .el-scrollbar__wrap) {
  border-radius: 12px;
}

:deep(.el-time-spinner__list) {
  padding: 8px 0;
}

:deep(.el-time-spinner__item) {
  height: 40px;
  line-height: 40px;
  font-size: 15px;
  font-weight: 500;
  color: #64748b;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  margin: 2px 8px;
}

:deep(.el-time-spinner__item):hover {
  background: linear-gradient(145deg, #e2e8f0 0%, #cbd5e1 100%);
  color: #475569;
  transform: scale(1.05);
}

:deep(.el-time-spinner__item.is-active) {
  background: linear-gradient(145deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  font-weight: 600;
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

/* Enhanced date table styling */
:deep(.el-date-table td) {
  padding: 4px;
}

:deep(.el-date-table td .cell) {
  width: 36px;
  height: 36px;
  line-height: 34px;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
}

:deep(.el-date-table td .cell):hover {
  background: linear-gradient(145deg, #e0e7ff 0%, #c7d2fe 100%);
  transform: scale(1.1);
}

:deep(.el-date-table td.current .cell) {
  background: linear-gradient(145deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

:deep(.el-date-table td.today .cell) {
  border: 2px solid #6366f1;
  color: #6366f1;
  font-weight: 600;
}

/* Month/year picker enhancements */
:deep(.el-month-table td .cell) {
  border-radius: 12px;
  padding: 8px 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
}

:deep(.el-year-table td .cell) {
  border-radius: 12px;
  padding: 8px 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
}

/* Form improvements */
.el-form-item__label {
  font-weight: 600;
  color: #2c3e50;
}

/* Responsive design */
@media (max-width: 1024px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .controls-section {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .controls-left,
  .controls-right {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .stats-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .calendar-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .calendar-legend {
    flex-wrap: wrap;
  }

  .day-cell {
    min-height: 80px;
  }

  .day-event-item {
    flex-direction: column;
    gap: 8px;
  }
}

/* ================================
   DRAG AND DROP STYLES
   ================================ */

/* Draggable task visual feedback */
.draggable-task {
  cursor: move;
  position: relative;
}

.draggable-task:hover {
  opacity: 0.9;
  transform: scale(1.02);
  transition: all 0.2s ease;
}

.draggable-task[draggable="true"] {
  user-select: none;
}

/* Drop target visual feedback */
.drop-valid {
  background: rgba(34, 197, 94, 0.1);
  border: 2px dashed #22c55e !important;
  transition: all 0.2s ease;
}

.drop-invalid {
  background: rgba(239, 68, 68, 0.1);
  border: 2px dashed #ef4444 !important;
  transition: all 0.2s ease;
}

/* Week view drop targets removed - using hour-level precision only */

/* Reschedule dialog styles */
.reschedule-dialog-content {
  padding: 16px 0;
}

.reschedule-task-info {
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.reschedule-details h4 {
  margin: 0 0 8px 0;
  color: #1f2937;
  font-weight: 600;
}

.reschedule-times {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.from-time {
  font-weight: 500;
  color: #dc2626;
}

.to-time {
  font-weight: 500;
  color: #059669;
}

.conflict-warning {
  margin-top: 16px;
}

.alternative-suggestion {
  margin-top: 12px;
  padding: 12px;
  background: #fffbeb;
  border-radius: 6px;
  border-left: 4px solid #f59e0b;
}

.alternative-suggestion p {
  margin: 4px 0;
}

.reason {
  font-style: italic;
  color: #92400e;
}

/* Undo notification styles */
.undo-notification {
  cursor: pointer;
}

.undo-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.undo-content span {
  flex: 1;
}

/* Drag state animations */
@keyframes dragPulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(59, 130, 246, 0.1);
  }
}

.draggable-task:active {
  animation: dragPulse 1s infinite;
}

/* Drag preview styling */
.event-item[draggable="true"]:active,
.week-event[draggable="true"]:active {
  opacity: 0.6;
  transform: rotate(3deg);
  z-index: 1000;
}
</style>
