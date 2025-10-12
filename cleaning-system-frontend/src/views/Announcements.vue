<template>
  <AdminLayout>
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
            <h2 class="page-title">Announcements & Tasks</h2>
            <p class="page-subtitle">
              Manage system announcements and weekly routine tasks
            </p>
          </div>
          <div class="header-right">
            <el-button type="primary" :loading="loading" @click="refreshData">
              Refresh Data
            </el-button>
          </div>
        </div>

        <!-- Content Sections -->
        <div class="content-grid">
          <!-- Announcements Section -->
          <el-card class="content-section announcements-section" shadow="hover">
            <template #header>
              <div class="section-header">
                <div class="section-title">
                  <el-icon><Bell /></el-icon>
                  <span>System Announcements</span>
                </div>
                <el-button
                  type="primary"
                  size="small"
                  @click="createAnnouncement"
                >
                  + Create Announcement
                </el-button>
              </div>
            </template>

            <div class="announcements-list">
              <div v-if="announcements.length === 0" class="empty-state">
                <el-empty description="No announcements yet" />
              </div>
              <div v-else>
                <div
                  v-for="announcement in announcements"
                  :key="announcement.id"
                  class="announcement-item"
                >
                  <div class="announcement-content">
                    <h4 class="announcement-title">{{ announcement.title }}</h4>
                    <p class="announcement-desc">{{ announcement.content }}</p>
                    <div class="announcement-meta">
                      <span class="announcement-time">{{
                        formatTime(announcement.createdAt)
                      }}</span>
                      <span class="announcement-author">{{
                        announcement.createdBy
                      }}</span>
                    </div>
                  </div>
                  <div class="announcement-actions">
                    <el-button
                      size="small"
                      text
                      @click="editAnnouncement(announcement)"
                    >
                      Edit
                    </el-button>
                    <el-button
                      size="small"
                      text
                      type="danger"
                      @click="deleteAnnouncement(announcement.id)"
                    >
                      Delete
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- Task Wall Section -->
          <el-card class="content-section task-wall-section" shadow="hover">
            <template #header>
              <div class="section-header">
                <div class="section-title">
                  <el-icon><List /></el-icon>
                  <span>Task Wall</span>
                </div>
                <el-button
                  type="success"
                  size="small"
                  @click="createPublicTask"
                >
                  + Create Public Task
                </el-button>
              </div>
            </template>

            <div class="task-wall-list">
              <div v-if="publicTasks.length === 0" class="empty-state">
                <el-empty description="No public tasks available" />
              </div>
              <div v-else>
                <div
                  v-for="task in publicTasks"
                  :key="task.id"
                  class="task-wall-item"
                  :class="{ 'task-claimed': task.claimedBy }"
                >
                  <div class="task-content">
                    <h4 class="task-title">{{ task.title }}</h4>
                    <p class="task-desc">{{ task.description }}</p>
                    <div class="task-meta">
                      <el-tag
                        :type="getPriorityType(task.priority)"
                        size="small"
                      >
                        {{ task.priority }}
                      </el-tag>
                      <span class="task-location">{{ task.location }}</span>
                      <span class="task-time">{{
                        formatTime(task.scheduledTime)
                      }}</span>
                    </div>
                    <div v-if="task.claimedBy" class="task-claimed-info">
                      <el-tag type="info" size="small">
                        Claimed by {{ task.claimedByName }}
                      </el-tag>
                    </div>
                  </div>
                  <div class="task-actions">
                    <el-button size="small" text @click="editPublicTask(task)">
                      Edit
                    </el-button>
                    <el-button
                      size="small"
                      text
                      type="danger"
                      @click="deletePublicTask(task.id)"
                    >
                      Delete
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- Week Routine Tasks Section -->
          <el-card
            class="content-section routine-section full-width"
            shadow="hover"
          >
            <template #header>
              <div class="section-header">
                <div class="section-title">
                  <el-icon><Calendar /></el-icon>
                  <span>Weekly Routine Tasks</span>
                </div>
                <div class="routine-header-actions">
                  <el-button
                    type="success"
                    size="small"
                    @click="generateAllTasks"
                    :loading="generatingTasks"
                  >
                    🔄 Generate Tasks
                  </el-button>
                  <el-button
                    type="warning"
                    size="small"
                    @click="createWeekRoutine"
                  >
                    + Create Routine
                  </el-button>
                </div>
              </div>
            </template>

            <div class="routine-tasks-grid">
              <div v-if="weekRoutines.length === 0" class="empty-state">
                <el-empty description="No weekly routines configured" />
              </div>
              <div v-else class="routine-list">
                <div
                  v-for="routine in weekRoutines"
                  :key="routine.id"
                  class="routine-item"
                >
                  <div class="routine-content">
                    <h4 class="routine-title">{{ routine.title }}</h4>
                    <p class="routine-desc">{{ routine.description }}</p>
                    <div class="routine-schedule">
                      <el-tag
                        v-for="day in routine.weekDays"
                        :key="day"
                        type="primary"
                        size="small"
                        class="day-tag"
                      >
                        {{ getDayName(day) }}
                      </el-tag>
                      <span class="routine-time">{{ routine.timeSlot }}</span>
                    </div>
                    <div class="routine-status">
                      <el-switch
                        v-model="routine.active"
                        @change="toggleRoutine(routine.id, routine.active)"
                        active-text="Active"
                        inactive-text="Inactive"
                      />
                    </div>
                  </div>
                  <div class="routine-actions">
                    <el-button
                      size="small"
                      text
                      @click="editWeekRoutine(routine)"
                    >
                      Edit
                    </el-button>
                    <el-button
                      size="small"
                      text
                      type="danger"
                      @click="deleteWeekRoutine(routine.id)"
                    >
                      Delete
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <!-- Announcement Creation Dialog -->
    <el-dialog
      v-model="announcementDialogVisible"
      :title="isAnnouncementEditMode ? '📝 Edit Announcement' : '📢 Create New Announcement'"
      width="700px"
      :close-on-click-modal="false"
      :z-index="4000"
      append-to-body
      :destroy-on-close="true"
      class="modern-dialog"
    >
      <el-form
        :model="announcementForm"
        label-width="120px"
        label-position="left"
      >
        <el-form-item label="Title" required>
          <el-input
            v-model="announcementForm.title"
            placeholder="Enter announcement title"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="Content">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            placeholder="Enter announcement content"
            :rows="6"
            maxlength="10000"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Priority">
              <el-select
                v-model="announcementForm.priority"
                placeholder="Select priority level"
                class="full-width-select contained-select"
                :teleported="false"
                :append-to-body="false"
                :popper-append-to-body="false"
                placement="bottom"
                popper-class="contained-dropdown"
              >
                <el-option
                  label="🟢 Low Priority"
                  value="low"
                  :disabled="isPriorityDisabled('low')"
                />
                <el-option
                  label="🟡 Normal Priority"
                  value="normal"
                  :disabled="isPriorityDisabled('normal')"
                />
                <el-option
                  label="🟠 High Priority"
                  value="high"
                  :disabled="isPriorityDisabled('high')"
                />
                <el-option
                  label="🔴 Urgent Priority"
                  value="urgent"
                  :disabled="isPriorityDisabled('urgent')"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="Type">
              <el-select
                v-model="announcementForm.announcementType"
                placeholder="Select announcement type"
                class="full-width-select contained-select"
                :teleported="false"
                :append-to-body="false"
                :popper-append-to-body="false"
                placement="bottom"
                popper-class="contained-dropdown"
                @change="adjustPriorityForType"
              >
                <el-option label="📢 General Announcement" value="general" />
                <el-option label="🔧 Maintenance Notice" value="maintenance" />
                <el-option label="📋 Policy Update" value="policy" />
                <el-option label="🚨 Emergency Alert" value="emergency" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="announcementDialogVisible = false"
            >Cancel</el-button
          >
          <el-button type="primary" @click="submitAnnouncement">
            {{ isAnnouncementEditMode ? 'Update Announcement' : 'Create Announcement' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Week Routine Creation Dialog -->
    <el-dialog
      v-model="routineDialogVisible"
      :title="
        isViewOnlyMode
          ? '👁️ View Weekly Routine Task'
          : isEditMode
          ? '✏️ Edit Weekly Routine Task'
          : '📅 Create Weekly Routine Task'
      "
      width="900px"
      :close-on-click-modal="false"
      :z-index="4000"
      append-to-body
      :destroy-on-close="true"
      class="modern-dialog"
    >
      <el-form
        :model="routineForm"
        label-width="140px"
        label-position="left"
        :rules="routineFormRules"
        ref="routineFormRef"
        :disabled="isViewOnlyMode"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Title" prop="title">
              <el-input
                v-model="routineForm.title"
                placeholder="Enter routine title"
                maxlength="255"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Location">
              <el-input
                v-model="routineForm.location"
                placeholder="Enter task location"
                maxlength="100"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="Description">
          <el-input
            v-model="routineForm.description"
            type="textarea"
            placeholder="Enter routine description"
            :rows="4"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Week Days" prop="weekDays">
              <el-checkbox-group v-model="routineForm.weekDays">
                <el-checkbox :value="1">Monday</el-checkbox>
                <el-checkbox :value="2">Tuesday</el-checkbox>
                <el-checkbox :value="3">Wednesday</el-checkbox>
                <el-checkbox :value="4">Thursday</el-checkbox>
                <el-checkbox :value="5">Friday</el-checkbox>
                <el-checkbox :value="6">Saturday</el-checkbox>
                <el-checkbox :value="7">Sunday</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Scheduled Time" prop="scheduledTime">
              <el-time-picker
                v-model="routineForm.scheduledTime"
                format="HH:mm"
                value-format="HH:mm"
                placeholder="Select time"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="Task Type" prop="taskType">
              <el-select
                v-model="routineForm.taskType"
                placeholder="Select task type"
                class="full-width-select contained-select"
                :teleported="false"
                :append-to-body="false"
                :popper-append-to-body="false"
                placement="bottom"
                popper-class="contained-dropdown"
              >
                <el-option label="🏢 Public (Task Wall)" value="public" />
                <el-option label="👤 Assigned to Janitor" value="assigned" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Priority">
              <el-select
                v-model="routineForm.priority"
                placeholder="Select priority level"
                class="full-width-select contained-select"
                :teleported="false"
                :append-to-body="false"
                :popper-append-to-body="false"
                placement="bottom"
                popper-class="contained-dropdown"
              >
                <el-option label="🟢 Low Priority" value="low" />
                <el-option label="🟡 Normal Priority" value="normal" />
                <el-option label="🟠 High Priority" value="high" />
                <el-option label="🔴 Urgent Priority" value="urgent" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Duration (min)">
              <el-input-number
                v-model="routineForm.estimatedDuration"
                :min="15"
                :max="480"
                :step="15"
                placeholder="Minutes"
                class="full-width-input-number"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item
          v-if="routineForm.taskType === 'assigned'"
          label="Assign To"
          prop="assignedTo"
        >
          <el-select
            v-model="routineForm.assignedTo"
            placeholder="Select janitor"
            filterable
            class="full-width-select contained-select"
            :teleported="false"
            :append-to-body="false"
            :popper-append-to-body="false"
            placement="bottom"
            popper-class="contained-dropdown"
          >
            <el-option
              v-for="janitor in janitors"
              :key="janitor.userId"
              :label="janitor.fullName || janitor.username"
              :value="janitor.userId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Instructions">
          <el-input
            v-model="routineForm.instructions"
            type="textarea"
            placeholder="Detailed task instructions"
            :rows="3"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="Tools Required">
          <el-input
            v-model="routineForm.toolsRequired"
            placeholder="List required tools/equipment"
            maxlength="500"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="routineDialogVisible = false">
            {{ isViewOnlyMode ? "Close" : "Cancel" }}
          </el-button>
          <el-button
            v-if="!isViewOnlyMode"
            type="primary"
            @click="submitWeekRoutine"
          >
            {{ isEditMode ? "Update Routine" : "Create Routine" }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Public Task Creation Dialog -->
    <el-dialog
      v-model="publicTaskDialogVisible"
      :title="editingTaskId ? '🔧 Edit Public Task' : '🏢 Create Public Task'"
      width="800px"
      :close-on-click-modal="false"
      class="modern-dialog"
      :z-index="4000"
      append-to-body
      :destroy-on-close="true"
    >
      <el-form
        :model="publicTaskForm"
        label-width="140px"
        label-position="left"
        :rules="publicTaskFormRules"
        ref="publicTaskFormRef"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Title" prop="title" label-width="80px">
              <el-input
                v-model="publicTaskForm.title"
                placeholder="Enter task title"
                maxlength="255"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Location" label-width="80px">
              <el-input
                v-model="publicTaskForm.location"
                placeholder="Enter task location"
                maxlength="100"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="Description">
          <el-input
            v-model="publicTaskForm.description"
            type="textarea"
            placeholder="Enter task description"
            :rows="4"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Start Date & Time" prop="startDate" label-width="auto">
              <el-date-picker
                v-model="publicTaskForm.startDate"
                type="datetime"
                placeholder="Choose start date and time"
                style="width: 100%"
                :disabled-date="disableStartDate"
                @change="onStartDateChange"
                format="YYYY-MM-DD HH:mm"
                popper-class="high-z-index-picker"
                :teleported="false"
                :picker-options="{
                  shortcuts: [
                    {
                      text: 'Now',
                      onClick(picker) {
                        picker.$emit('pick', new Date());
                      },
                    },
                    {
                      text: 'Tomorrow 9AM',
                      onClick(picker) {
                        const date = new Date();
                        date.setDate(date.getDate() + 1);
                        date.setHours(9, 0, 0, 0);
                        picker.$emit('pick', date);
                      },
                    },
                    {
                      text: 'Next Week',
                      onClick(picker) {
                        const date = new Date();
                        date.setDate(date.getDate() + 7);
                        date.setHours(9, 0, 0, 0);
                        picker.$emit('pick', date);
                      },
                    },
                  ],
                }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="End Date & Time" prop="endDate" label-width="auto">
              <el-date-picker
                v-model="publicTaskForm.endDate"
                type="datetime"
                placeholder="Choose end date and time"
                style="width: 100%"
                :disabled-date="disableEndDate"
                @change="onEndDateChange"
                format="YYYY-MM-DD HH:mm"
                popper-class="high-z-index-picker"
                :teleported="false"
                :picker-options="{
                  shortcuts: [
                    {
                      text: '+1 Hour',
                      onClick(picker) {
                        if (publicTaskForm.startDate) {
                          const date = new Date(publicTaskForm.startDate);
                          date.setHours(date.getHours() + 1);
                          picker.$emit('pick', date);
                        }
                      },
                    },
                    {
                      text: '+2 Hours',
                      onClick(picker) {
                        if (publicTaskForm.startDate) {
                          const date = new Date(publicTaskForm.startDate);
                          date.setHours(date.getHours() + 2);
                          picker.$emit('pick', date);
                        }
                      },
                    },
                    {
                      text: '+4 Hours',
                      onClick(picker) {
                        if (publicTaskForm.startDate) {
                          const date = new Date(publicTaskForm.startDate);
                          date.setHours(date.getHours() + 4);
                          picker.$emit('pick', date);
                        }
                      },
                    },
                  ],
                }"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Priority" label-width="80px">
              <el-select
                v-model="publicTaskForm.priority"
                placeholder="Select priority level"
                style="width: 100%"
                :teleported="false"
                popper-class="high-z-index-picker"
              >
                <el-option label="🟢 Low Priority" value="low" />
                <el-option label="🟡 Normal Priority" value="normal" />
                <el-option label="🟠 High Priority" value="high" />
                <el-option label="🔴 Urgent Priority" value="urgent" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Duration (min)" label-width="100px">
              <el-input-number
                v-model="publicTaskForm.estimatedDuration"
                :min="15"
                :max="480"
                :step="15"
                placeholder="Minutes"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="Task Type">
          <el-select
            v-model="publicTaskForm.taskType"
            placeholder="Select task type"
            style="width: 100%"
            :teleported="false"
            popper-class="high-z-index-picker"
          >
            <el-option label="🏢 Public (Task Wall)" value="public" />
            <el-option label="👤 Assigned to Janitor" value="assigned" />
          </el-select>
        </el-form-item>

        <el-form-item
          v-if="publicTaskForm.taskType === 'assigned'"
          label="Assign To"
          prop="assignedTo"
        >
          <el-select
            v-model="publicTaskForm.assignedTo"
            placeholder="Select janitor"
            filterable
            style="width: 100%"
            :teleported="false"
            popper-class="high-z-index-picker"
          >
            <el-option
              v-for="janitor in janitors"
              :key="janitor.userId"
              :label="janitor.fullName || janitor.username"
              :value="janitor.userId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Instructions">
          <el-input
            v-model="publicTaskForm.instructions"
            type="textarea"
            placeholder="Detailed task instructions"
            :rows="3"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="Tools Required">
          <el-input
            v-model="publicTaskForm.toolsRequired"
            placeholder="List required tools/equipment"
            maxlength="500"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="publicTaskDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitPublicTask">
            {{ editingTaskId ? "Update Task" : "Create Task" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script>
import { ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AdminLayout from "@/components/AdminLayout.vue";
import { Bell, List, Calendar } from "@element-plus/icons-vue";
import { API } from "@/utils/request";
import { AuthUtils } from "@/utils/auth";

export default {
  name: "AnnouncementsView",
  components: {
    AdminLayout,
    Bell,
    List,
    Calendar,
  },
  setup() {
    const loading = ref(false);
    const generatingTasks = ref(false);
    const announcements = ref([]);
    const publicTasks = ref([]);
    const weekRoutines = ref([]);
    const janitors = ref([]);

    // Load data from APIs
    const loadAnnouncements = async () => {
      try {
        const response = await API.get("/api/announcements");
        announcements.value = response.data.map((announcement) => ({
          id: announcement.announcementId,
          title: announcement.title,
          content: announcement.content,
          priority: announcement.priority,
          announcementType: announcement.announcementType,
          targetAudience: announcement.targetAudience,
          createdAt: new Date(announcement.createdAt),
          createdBy: announcement.createdByName || "Unknown Admin",
          isActive: announcement.isActive,
        }));
      } catch (error) {
        console.error("Failed to load announcements:", error);
        ElMessage.error("Failed to load announcements");
        // Fallback to mock data if API fails
        announcements.value = [
          {
            id: 1,
            title: "System Maintenance Notice",
            content:
              "System maintenance will be performed this Friday at 10 PM, estimated 2 hours downtime",
            createdAt: new Date("2025-08-13T10:00:00"),
            createdBy: "System Administrator",
          },
        ];
      }
    };

    const loadPublicTasks = async () => {
      try {
        const response = await API.get("/api/tasks/public");
        publicTasks.value = response.data.map((task) => ({
          id: task.taskId,
          title: task.title,
          description: task.description,
          priority: task.priority,
          location: task.location,
          scheduledTime: new Date(task.scheduledTime),
          claimedBy: task.assignedTo,
          claimedByName: task.assignedToName || null,
          status: task.status,
        }));
      } catch (error) {
        console.error("Failed to load public tasks:", error);
        ElMessage.error("Failed to load public tasks");
        // Fallback to mock data
        publicTasks.value = [
          {
            id: 1,
            title: "Clean Office Area",
            description:
              "Clean main office area including desks, floors and trash removal",
            priority: "medium",
            location: "Office Area A",
            scheduledTime: new Date("2025-08-14T09:00:00"),
            claimedBy: null,
            claimedByName: null,
          },
        ];
      }
    };

    const loadWeekRoutines = async () => {
      try {
        const response = await API.get("/api/week-routines");
        weekRoutines.value = response.data.map((routine) => ({
          id: routine.routineId,
          title: routine.title,
          description: routine.description,
          weekDays: routine.weekDaysArray || [],
          timeSlot: routine.scheduledTime,
          active: routine.active,
          location: routine.location,
          priority: routine.priority,
          taskType: routine.taskType,
        }));
      } catch (error) {
        console.error("Failed to load week routines:", error);
        ElMessage.error("Failed to load week routines");
        // Fallback to mock data
        weekRoutines.value = [
          {
            id: 1,
            title: "Monday Floor Cleaning",
            description:
              "Comprehensive floor cleaning and mopping every Monday",
            weekDays: [1],
            timeSlot: "09:00",
            active: true,
          },
        ];
      }
    };

    const loadJanitors = async () => {
      try {
        const response = await API.get("/api/janitors");
        janitors.value = response.data.map((janitor) => ({
          userId: janitor.userId,
          username: janitor.username,
          fullName: janitor.fullName,
          email: janitor.email,
        }));
      } catch (error) {
        console.error("Failed to load janitors:", error);
        ElMessage.error("Failed to load janitors list");
        // Fallback to mock data
        janitors.value = [
          { userId: 193, username: "maria", fullName: "Maria Santos" },
          { userId: 194, username: "john", fullName: "John Doe" },
          { userId: 195, username: "sarah", fullName: "Sarah Johnson" },
          { userId: 196, username: "mike", fullName: "Mike Wilson" },
        ];
      }
    };

    const loadAllData = async () => {
      await Promise.all([
        loadAnnouncements(),
        loadPublicTasks(),
        loadWeekRoutines(),
        loadJanitors(),
      ]);
    };

    const refreshData = async () => {
      loading.value = true;
      try {
        await loadAllData();
        ElMessage.success("Data refreshed successfully");
      } catch (error) {
        ElMessage.error("Failed to refresh data");
        console.error("Refresh error:", error);
      } finally {
        loading.value = false;
      }
    };

    const formatTime = (date) => {
      return new Date(date).toLocaleString("en-GB");
    };

    const getPriorityType = (priority) => {
      const typeMap = {
        high: "danger",
        medium: "warning",
        low: "info",
      };
      return typeMap[priority] || "info";
    };

    const getDayName = (dayNumber) => {
      const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
      return days[dayNumber];
    };

    // Announcement dialog state
    const announcementDialogVisible = ref(false);
    const isAnnouncementEditMode = ref(false);
    const editingAnnouncementId = ref(null);
    const announcementForm = ref({
      title: "",
      content: "",
      priority: "normal",
      announcementType: "general",
      targetAudience: "all",
      expiresAt: null,
    });

    // Week routine dialog state
    const routineDialogVisible = ref(false);
    const routineFormRef = ref(null);
    const isEditMode = ref(false);
    const isViewOnlyMode = ref(false);
    const editingRoutineId = ref(null);
    const routineForm = ref({
      title: "",
      description: "",
      location: "",
      weekDays: [],
      scheduledTime: "",
      taskType: "public",
      priority: "normal",
      estimatedDuration: 60,
      assignedTo: null,
      instructions: "",
      toolsRequired: "",
    });

    const routineFormRules = {
      title: [
        {
          required: true,
          message: "Routine title is required",
          trigger: "blur",
        },
        {
          min: 1,
          max: 255,
          message: "Title length should be 1-255 characters",
          trigger: "blur",
        },
      ],
      weekDays: [
        {
          required: true,
          type: "array",
          min: 1,
          message: "Select at least one day",
          trigger: "change",
        },
      ],
      scheduledTime: [
        {
          required: true,
          message: "Scheduled time is required",
          trigger: "change",
        },
      ],
      taskType: [
        { required: true, message: "Task type is required", trigger: "change" },
      ],
      assignedTo: [
        {
          validator: (rule, value, callback) => {
            if (routineForm.value.taskType === "assigned" && !value) {
              callback(new Error("Please select a janitor for assigned tasks"));
            } else {
              callback();
            }
          },
          trigger: "change",
        },
      ],
    };

    // Public task dialog state
    const publicTaskDialogVisible = ref(false);
    const publicTaskFormRef = ref(null);
    const editingTaskId = ref(null);
    const publicTaskForm = ref({
      title: "",
      description: "",
      location: "",
      startDate: null,
      endDate: null,
      priority: "normal",
      estimatedDuration: 60,
      taskType: "public",
      assignedTo: null,
      instructions: "",
      toolsRequired: "",
    });

    const publicTaskFormRules = {
      title: [
        {
          required: true,
          message: "Task title is required",
          trigger: "blur",
        },
        {
          min: 1,
          max: 255,
          message: "Title length should be 1-255 characters",
          trigger: "blur",
        },
      ],
      startDate: [
        {
          required: true,
          message: "Start date and time is required",
          trigger: "change",
        },
      ],
      endDate: [
        {
          required: true,
          message: "End date and time is required",
          trigger: "change",
        },
      ],
      assignedTo: [
        {
          validator: (rule, value, callback) => {
            if (publicTaskForm.value.taskType === "assigned" && !value) {
              callback(new Error("Please select a janitor for assigned tasks"));
            } else {
              callback();
            }
          },
          trigger: "change",
        },
      ],
    };

    // Enhanced date picker constraint functions with better logic (from TaskCalendar)
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
      if (publicTaskForm.value.startDate) {
        const startDate =
          publicTaskForm.value.startDate instanceof Date
            ? new Date(publicTaskForm.value.startDate)
            : new Date(publicTaskForm.value.startDate);
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

    // Enhanced date change handlers with better validation (from TaskCalendar)
    const onStartDateChange = (value) => {
      console.log(
        "Start date changed:",
        value,
        typeof value,
        value instanceof Date
      );
      if (!value) {
        publicTaskForm.value.endDate = null;
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
        publicTaskForm.value.startDate = null;
        return;
      }

      // Validate the date
      if (isNaN(startTime.getTime())) {
        ElMessage.error("Invalid start date selected");
        publicTaskForm.value.startDate = null;
        return;
      }

      // If end date is already selected and is invalid, suggest a default end time
      if (publicTaskForm.value.endDate) {
        const endTime =
          publicTaskForm.value.endDate instanceof Date
            ? publicTaskForm.value.endDate
            : new Date(publicTaskForm.value.endDate);

        // If end date is before or same as start date, auto-suggest end time
        if (endTime.getTime() <= startTime.getTime()) {
          // Automatically set end time to 2 hours after start time
          const suggestedEndTime = new Date(
            startTime.getTime() + 2 * 60 * 60 * 1000
          );
          // Ensure proper reactivity by creating a new Date instance
          publicTaskForm.value.endDate = new Date(suggestedEndTime.getTime());

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
        publicTaskForm.value.endDate = new Date(suggestedEndTime.getTime());
      }

      // Force refresh of end date picker constraints
      setTimeout(() => {
        if (publicTaskFormRef.value) {
          publicTaskFormRef.value.validateField("endDate");
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
        publicTaskForm.value.endDate = null;
        return;
      }

      // Validate the date
      if (isNaN(endTime.getTime())) {
        ElMessage.error("Invalid end date selected");
        publicTaskForm.value.endDate = null;
        return;
      }

      // If no start date selected, suggest user set start date first
      if (!publicTaskForm.value.startDate) {
        ElMessage.warning({
          message: "Please select a start date first for better task planning",
          duration: 3000,
        });
        return;
      }

      // Safely handle start date with validation
      let startTime;
      try {
        if (publicTaskForm.value.startDate instanceof Date) {
          startTime = new Date(publicTaskForm.value.startDate.getTime());
        } else {
          startTime = new Date(publicTaskForm.value.startDate);
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
      if (publicTaskFormRef.value) {
        publicTaskFormRef.value.validateField("endDate");
      }
    };

    // Priority restriction logic based on announcement type
    const allowedPriorities = computed(() => {
      const type = announcementForm.value.announcementType;

      switch (type) {
        case 'general':
          // General Announcement: Urgent priority is disabled
          return ['low', 'normal', 'high'];

        case 'maintenance':
        case 'policy':
          // Maintenance Notice & Policy Update: All priorities allowed
          return ['low', 'normal', 'high', 'urgent'];

        case 'emergency':
          // Emergency Alert: Only urgent and high allowed
          return ['high', 'urgent'];

        default:
          return ['low', 'normal', 'high', 'urgent'];
      }
    });

    // Check if priority option should be disabled
    const isPriorityDisabled = (priority) => {
      return !allowedPriorities.value.includes(priority);
    };

    // Auto-adjust priority when type changes and current priority is not allowed
    const adjustPriorityForType = () => {
      const currentPriority = announcementForm.value.priority;
      const allowed = allowedPriorities.value;

      if (!allowed.includes(currentPriority)) {
        // Auto-select the highest allowed priority for emergency, otherwise normal
        if (announcementForm.value.announcementType === 'emergency') {
          announcementForm.value.priority = 'urgent';
        } else {
          announcementForm.value.priority = 'normal';
        }
      }
    };

    // Announcement functions
    const createAnnouncement = () => {
      // Reset form and edit state
      isAnnouncementEditMode.value = false;
      editingAnnouncementId.value = null;
      announcementForm.value = {
        title: "",
        content: "",
        priority: "normal",
        announcementType: "general",
        targetAudience: "all", // Set default but don't show in form
        expiresAt: null, // Set default but don't show in form
      };
      announcementDialogVisible.value = true;
    };

    const submitAnnouncement = async () => {
      try {
        // Validate form
        if (!announcementForm.value.title.trim()) {
          ElMessage.error("Announcement title is required");
          return;
        }

        // Validate user authentication using AuthUtils (consistent with task creation)
        const currentUserId = AuthUtils.getUserId();
        if (!currentUserId) {
          ElMessage.error("用户身份验证失败，请重新登录");
          return;
        }

        // Prepare announcement data (backend will set createdBy from JWT)
        const announcementData = {
          ...announcementForm.value,
          // Remove createdBy - backend will set it from JWT token
          // Ensure these fields are set even though not visible in form
          targetAudience: announcementForm.value.targetAudience || "all",
          expiresAt: announcementForm.value.expiresAt || null,
        };

        if (isAnnouncementEditMode.value) {
          // Edit mode: update existing announcement
          await API.put(`/api/announcements/${editingAnnouncementId.value}`, announcementData);
          ElMessage.success("Announcement updated successfully");
        } else {
          // Create mode: create new announcement
          await API.post("/api/announcements", announcementData);
          ElMessage.success("Announcement created successfully");
        }

        announcementDialogVisible.value = false;

        // Refresh announcements list
        await loadAnnouncements();
      } catch (error) {
        const actionText = isAnnouncementEditMode.value ? "update" : "create";
        console.error(`Failed to ${actionText} announcement:`, error);
        ElMessage.error(`Failed to ${actionText} announcement`);
      }
    };

    const editAnnouncement = (announcement) => {
      try {
        // Get current user information for permission check
        const currentUserRole = AuthUtils.getUserRole();
        const currentUserId = AuthUtils.getUserId();

        if (!currentUserId) {
          ElMessage.error("Authentication failed, please login again");
          return;
        }

        // Role-based permission check
        if (currentUserRole === 'supervisor') {
          // Supervisor can only edit their own announcements
          // Convert both to numbers for comparison to handle type mismatches
          const announcementCreatorId = Number(announcement.createdBy);
          const currentUserIdNum = Number(currentUserId);

          console.log('Permission check debug (Admin page):', {
            currentUserRole,
            announcementCreatorId,
            currentUserIdNum,
            rawAnnouncementCreatedBy: announcement.createdBy,
            rawCurrentUserId: currentUserId
          });

          if (announcementCreatorId !== currentUserIdNum) {
            ElMessage.error("You can only edit announcements created by yourself");
            return;
          }
        }
        // Admin has full permissions (no additional check needed)

        // Set edit mode and store announcement ID
        isAnnouncementEditMode.value = true;
        editingAnnouncementId.value = announcement.id;

        // Pre-fill form with existing announcement data
        announcementForm.value = {
          title: announcement.title || "",
          content: announcement.content || "",
          priority: announcement.priority || "normal",
          announcementType: announcement.announcementType || "general",
          targetAudience: announcement.targetAudience || "all",
          expiresAt: announcement.expiresAt || null,
        };

        // Open dialog for editing
        announcementDialogVisible.value = true;

        ElMessage.success(`Editing announcement: ${announcement.title}`);
      } catch (error) {
        console.error("Failed to initialize announcement editing:", error);
        ElMessage.error("Failed to load announcement for editing");
      }
    };

    const deleteAnnouncement = async (id) => {
      try {
        await ElMessageBox.confirm(
          "Are you sure you want to delete this announcement?",
          "Delete Confirmation",
          {
            confirmButtonText: "Confirm",
            cancelButtonText: "Cancel",
            type: "warning",
          }
        );

        await API.delete(`/api/announcements/${id}`);
        await loadAnnouncements();
        ElMessage.success("Announcement deleted successfully");
      } catch (error) {
        if (error !== "cancel") {
          console.error("Failed to delete announcement:", error);
          ElMessage.error("Failed to delete announcement");
        }
      }
    };

    // Public task functions
    const createPublicTask = () => {
      // Reset form with default values
      // Set default start date to tomorrow 9 AM
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(9, 0, 0, 0);

      // Set default end date to tomorrow 11 AM (2 hours duration)
      const tomorrowEnd = new Date();
      tomorrowEnd.setDate(tomorrowEnd.getDate() + 1);
      tomorrowEnd.setHours(11, 0, 0, 0);

      publicTaskForm.value = {
        title: "",
        description: "",
        location: "",
        startDate: new Date(tomorrow.getTime()), // Default to tomorrow 9 AM
        endDate: new Date(tomorrowEnd.getTime()), // Default to tomorrow 11 AM
        priority: "normal",
        estimatedDuration: 60,
        taskType: "public",
        assignedTo: null,
        instructions: "",
        toolsRequired: "",
      };
      editingTaskId.value = null; // Reset editing state
      publicTaskDialogVisible.value = true;
    };

    const submitPublicTask = async () => {
      try {
        // Validate form
        if (!publicTaskFormRef.value) return;
        await publicTaskFormRef.value.validate();

        // Prepare task data for API
        const userInfo = JSON.parse(localStorage.getItem("userInfo") || "{}");
        console.log("Current user info:", userInfo); // Debug log

        // Use the new datetime fields directly
        const startDateTime = publicTaskForm.value.startDate.toISOString();
        const endDateTime = publicTaskForm.value.endDate.toISOString();

        // Get actual user ID, with validation
        const currentUserId = AuthUtils.getUserId();
        console.log("Current user ID from AuthUtils:", currentUserId); // Debug log
        if (!currentUserId) {
          ElMessage.error("User authentication error. Please login again.");
          return;
        }

        const taskData = {
          title: publicTaskForm.value.title.trim(),
          description: publicTaskForm.value.description?.trim() || "",
          location: publicTaskForm.value.location?.trim() || "",
          scheduledTime: startDateTime,
          dueDate: endDateTime,
          priority: publicTaskForm.value.priority,
          status: "pending",
          estimatedDuration: publicTaskForm.value.estimatedDuration,
          assignedTo:
            publicTaskForm.value.taskType === "assigned"
              ? publicTaskForm.value.assignedTo
              : null,
          instructions: publicTaskForm.value.instructions?.trim() || "",
          toolsRequired: publicTaskForm.value.toolsRequired?.trim() || "",
          assignedBy: currentUserId, // Use actual logged-in user ID
        };

        console.log("Task data being sent:", taskData); // Debug log

        if (editingTaskId.value) {
          // Update existing task
          await API.put(`/api/tasks/${editingTaskId.value}`, taskData);
          ElMessage.success("Public task updated successfully");
        } else {
          // Create new task
          await API.post("/api/tasks", taskData);
          ElMessage.success("Public task created successfully");
        }

        publicTaskDialogVisible.value = false;
        editingTaskId.value = null; // Reset editing state

        // Refresh public tasks list
        await loadPublicTasks();
      } catch (error) {
        console.error("Failed to create public task:", error);

        // Check if it's a validation error from the form
        if (error.name === "ValidationError") {
          ElMessage.error("Please check the form for errors");
          return;
        }

        // API error
        const errorMessage =
          error.response?.data?.message ||
          error.message ||
          "Failed to create public task";
        ElMessage.error(errorMessage);
      }
    };

    const editPublicTask = (task) => {
      // Populate form with existing task data
      publicTaskForm.value = {
        title: task.title || "",
        description: task.description || "",
        location: task.location || "",
        startDate: task.scheduledTime ? new Date(task.scheduledTime) : null,
        endDate: task.scheduledTime
          ? new Date(
              new Date(task.scheduledTime).getTime() + 2 * 60 * 60 * 1000
            )
          : null, // Default 2 hours
        priority: task.priority || "normal",
        estimatedDuration: 60, // Default duration
        taskType: "public",
        assignedTo: null,
        instructions: "",
        toolsRequired: "",
      };

      // Store the editing task ID
      editingTaskId.value = task.id;
      publicTaskDialogVisible.value = true;
    };

    const deletePublicTask = async (id) => {
      try {
        await ElMessageBox.confirm(
          "Are you sure you want to delete this task?",
          "Delete Confirmation",
          {
            confirmButtonText: "Confirm",
            cancelButtonText: "Cancel",
            type: "warning",
          }
        );

        await API.delete(`/api/tasks/${id}`);
        await loadPublicTasks(); // Refresh the task list
        ElMessage.success("Task deleted successfully");
      } catch (error) {
        if (error !== "cancel") {
          // User didn't cancel
          console.error("Failed to delete task:", error);
          ElMessage.error("Failed to delete task");
        }
      }
    };

    // Week routine functions
    const generateAllTasks = async () => {
      try {
        generatingTasks.value = true;

        const response = await API.post("/api/week-routines/generate-tasks");
        const { tasksGenerated, generatedTasks } = response.data;

        if (tasksGenerated > 0) {
          ElMessage.success(
            `Successfully generated ${tasksGenerated} tasks from active routines`
          );

          // Show task summary if available
          if (generatedTasks && generatedTasks.length > 0) {
            console.log("Generated tasks:", generatedTasks);
          }

          // Refresh public tasks to show newly generated tasks
          await loadPublicTasks();
        } else {
          ElMessage.info(
            "No new tasks generated - all routines may have already been processed today"
          );
        }
      } catch (error) {
        console.error("Failed to generate tasks:", error);
        ElMessage.error("Failed to generate tasks from routines");
      } finally {
        generatingTasks.value = false;
      }
    };

    const createWeekRoutine = () => {
      // Ensure we're in create mode
      isEditMode.value = false;
      isViewOnlyMode.value = false; // Admin never in view-only mode
      editingRoutineId.value = null;

      // Reset form
      routineForm.value = {
        title: "",
        description: "",
        location: "",
        weekDays: [],
        scheduledTime: "",
        taskType: "public",
        priority: "normal",
        estimatedDuration: 60,
        assignedTo: null,
        instructions: "",
        toolsRequired: "",
      };
      routineDialogVisible.value = true;
    };

    const submitWeekRoutine = async () => {
      try {
        // Validate form
        if (!routineFormRef.value) return;
        await routineFormRef.value.validate();

        // Validate user authentication using AuthUtils (consistent with task creation)
        const currentUserId = AuthUtils.getUserId();
        if (!currentUserId) {
          ElMessage.error("用户身份验证失败，请重新登录");
          return;
        }

        // Prepare routine data for API (backend will set createdBy from JWT)
        const routineData = {
          title: routineForm.value.title.trim(),
          description: routineForm.value.description?.trim() || "",
          location: routineForm.value.location?.trim() || "",
          weekDays: routineForm.value.weekDays.join(","), // Convert array to comma-separated string
          scheduledTime: routineForm.value.scheduledTime,
          taskType: routineForm.value.taskType,
          priority: routineForm.value.priority,
          estimatedDuration: routineForm.value.estimatedDuration,
          assignedTo:
            routineForm.value.taskType === "assigned"
              ? routineForm.value.assignedTo
              : null,
          instructions: routineForm.value.instructions?.trim() || "",
          toolsRequired: routineForm.value.toolsRequired?.trim() || "",
          // Remove createdBy - backend will set it from JWT token
          active: true,
        };

        // Choose API call based on mode
        if (isEditMode.value) {
          // Edit mode: update existing routine
          await API.put(
            `/api/week-routines/${editingRoutineId.value}`,
            routineData
          );
          ElMessage.success("Weekly routine updated successfully");
        } else {
          // Create mode: create new routine
          await API.post("/api/week-routines", routineData);
          ElMessage.success("Weekly routine created successfully");
        }

        // Reset state and close dialog
        routineDialogVisible.value = false;
        isEditMode.value = false;
        isViewOnlyMode.value = false;
        editingRoutineId.value = null;

        // Refresh routines list
        await loadWeekRoutines();
      } catch (error) {
        console.error("Failed to create week routine:", error);

        // Check if it's a validation error from the form
        if (error.name === "ValidationError") {
          ElMessage.error("Please check the form for errors");
          return;
        }

        // API error
        const errorMessage =
          error.response?.data?.message ||
          error.message ||
          (isEditMode.value
            ? "Failed to update week routine"
            : "Failed to create week routine");
        ElMessage.error(errorMessage);
      }
    };

    const editWeekRoutine = (routine) => {
      // Admin always has edit permission
      isEditMode.value = true;
      isViewOnlyMode.value = false; // Admin never in view-only mode
      editingRoutineId.value = routine.id;

      // Pre-fill form with existing routine data
      routineForm.value = {
        title: routine.title || "",
        description: routine.description || "",
        location: routine.location || "",
        weekDays: routine.weekDays || [], // Already in array format
        scheduledTime: routine.timeSlot || "",
        taskType: routine.taskType || "public",
        priority: routine.priority || "normal",
        estimatedDuration: routine.estimatedDuration || 60,
        assignedTo: routine.assignedTo || null,
        instructions: routine.instructions || "",
        toolsRequired: routine.toolsRequired || "",
      };

      // Show dialog (reuse existing dialog)
      routineDialogVisible.value = true;
    };

    const deleteWeekRoutine = async (id) => {
      try {
        await ElMessageBox.confirm(
          "Are you sure you want to delete this routine?",
          "Delete Confirmation",
          {
            confirmButtonText: "Confirm",
            cancelButtonText: "Cancel",
            type: "warning",
          }
        );

        await API.delete(`/api/week-routines/${id}`);
        await loadWeekRoutines(); // Refresh the routine list
        ElMessage.success("Routine deleted successfully");
      } catch (error) {
        if (error !== "cancel") {
          // User didn't cancel
          console.error("Failed to delete routine:", error);
          ElMessage.error("Failed to delete routine");
        }
      }
    };

    const toggleRoutine = async (id, active) => {
      try {
        await API.put(`/api/week-routines/${id}/toggle`);
        const status = active ? "activated" : "deactivated";
        ElMessage.success(`Routine ${status} successfully`);
        // Refresh routines to show updated status
        await loadWeekRoutines();
      } catch (error) {
        console.error("Failed to toggle routine:", error);
        ElMessage.error("Failed to toggle routine status");
      }
    };

    onMounted(() => {
      loadAllData();
    });

    return {
      loading,
      generatingTasks,
      announcements,
      publicTasks,
      weekRoutines,
      janitors,
      refreshData,
      formatTime,
      getPriorityType,
      getDayName,
      // Announcement functions
      createAnnouncement,
      editAnnouncement,
      deleteAnnouncement,
      announcementDialogVisible,
      announcementForm,
      submitAnnouncement,
      isAnnouncementEditMode,
      editingAnnouncementId,
      // Priority restriction functions
      allowedPriorities,
      isPriorityDisabled,
      adjustPriorityForType,
      // Task wall functions
      createPublicTask,
      editPublicTask,
      deletePublicTask,
      // Week routine functions
      generateAllTasks,
      createWeekRoutine,
      editWeekRoutine,
      deleteWeekRoutine,
      toggleRoutine,
      // Week routine dialog
      routineDialogVisible,
      routineForm,
      routineFormRef,
      routineFormRules,
      isEditMode,
      isViewOnlyMode,
      editingRoutineId,
      submitWeekRoutine,
      // Public task dialog
      publicTaskDialogVisible,
      publicTaskForm,
      publicTaskFormRef,
      publicTaskFormRules,
      submitPublicTask,
      editingTaskId,
      disableStartDate,
      disableEndDate,
      onStartDateChange,
      onEndDateChange,
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

/* Header styles - 方案A: 企业中性灰调 */
.announcements-header {
  background: linear-gradient(135deg, #1e293b 0%, #374151 100%);
  color: white;
  padding: 2rem;
  border-radius: 12px;
  margin-bottom: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8px 32px rgba(30, 41, 59, 0.3);
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

/* Content grid */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-gap: 1.5rem;
  margin-bottom: 2rem;
}

.content-section {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.content-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.full-width {
  grid-column: 1 / -1;
}

/* Section headers */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.routine-header-actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  font-size: 1.1rem;
  color: #2c3e50;
}

/* Announcements section - 方案A: 企业中性灰调 */
.announcements-section {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
}

.announcements-section :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.announcements-section .section-title {
  color: white;
}

.announcement-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  margin-bottom: 1rem;
  transition: background 0.3s ease;
}

.announcement-item:hover {
  background: rgba(255, 255, 255, 1);
  transform: translateX(4px);
}

.announcement-title {
  font-weight: 600;
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
}

.announcement-desc {
  color: #5a6c7d;
  margin: 0 0 0.5rem 0;
  line-height: 1.5;
}

.announcement-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.875rem;
  color: #8892a6;
}

.announcement-actions {
  display: flex;
  gap: 0.5rem;
  align-items: flex-start;
}

/* Task wall section - 方案A: 企业中性灰调 */
.task-wall-section {
  background: linear-gradient(135deg, #1e293b 0%, #374151 100%);
}

.task-wall-section :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.task-wall-section .section-title {
  color: white;
}

.task-wall-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  margin-bottom: 1rem;
  transition: all 0.3s ease;
}

.task-wall-item:hover {
  background: rgba(255, 255, 255, 1);
  transform: translateX(4px);
}

.task-wall-item.task-claimed {
  background: rgba(255, 255, 255, 0.85);
  border-left: 4px solid #67c23a;
}

.task-title {
  font-weight: 600;
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
}

.task-desc {
  color: #5a6c7d;
  margin: 0 0 0.5rem 0;
  line-height: 1.5;
}

.task-meta {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  margin-bottom: 0.5rem;
}

.task-location,
.task-time {
  font-size: 0.875rem;
  color: #8892a6;
}

.task-claimed-info {
  margin-top: 0.5rem;
}

.task-actions {
  display: flex;
  gap: 0.5rem;
  align-items: flex-start;
}

/* Week routine section */
.routine-section {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
}

.routine-section :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.routine-section .section-title {
  color: white;
}

.routine-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  grid-gap: 1rem;
}

.routine-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.routine-item:hover {
  background: rgba(255, 255, 255, 1);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.routine-title {
  font-weight: 600;
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
}

.routine-desc {
  color: #6b7280;
  margin: 0 0 0.75rem 0;
  line-height: 1.5;
}

.routine-schedule {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  margin-bottom: 0.75rem;
  flex-wrap: wrap;
}

.day-tag {
  margin: 0;
}

.routine-time {
  font-size: 0.875rem;
  color: #8892a6;
  margin-left: 0.5rem;
}

.routine-status {
  margin-top: 0.5rem;
}

.routine-actions {
  display: flex;
  gap: 0.5rem;
  align-items: flex-start;
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 2rem;
  color: rgba(255, 255, 255, 0.8);
}

/* Dialog styling improvements */
.modern-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.modern-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #1e293b 0%, #374151 100%);
  color: white;
  padding: 1.5rem 2rem;
  margin: 0;
}

.modern-dialog :deep(.el-dialog__title) {
  font-size: 1.25rem;
  font-weight: 600;
  color: white;
}

.modern-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(255, 255, 255, 0.8);
  font-size: 1.2rem;
}

.modern-dialog :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: white;
}

.modern-dialog :deep(.el-dialog__body) {
  padding: 2rem;
  background: #fafafa;
}

.modern-dialog :deep(.el-dialog__footer) {
  background: #f5f5f5;
  padding: 1.5rem 2rem;
  border-top: 1px solid #e8e8e8;
}

/* Form improvements */
.modern-dialog :deep(.el-form) {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.modern-dialog :deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c3e50;
}

.modern-dialog :deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.modern-dialog :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.modern-dialog :deep(.el-textarea__inner) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.modern-dialog :deep(.el-textarea__inner:hover) {
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

/* Select dropdown improvements */
.full-width-select {
  width: 100%;
}

.full-width-select :deep(.el-select__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
  min-width: 200px;
}

.full-width-select :deep(.el-select__wrapper:hover) {
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

/* Enhanced dialog containment with strict overflow control */
/* Dialog z-index without overflow restrictions */
.modern-dialog {
  z-index: 4000 !important;
}

.modern-dialog :deep(.el-dialog) {
  position: relative;
  z-index: 4000;
}

.modern-dialog :deep(.el-dialog__body) {
  position: relative;
}

.modern-dialog :deep(.el-form) {
  position: relative;
  z-index: 4001;
}

/* Removed all contained-dialog CSS - these rules were blocking dropdowns */

/* Button improvements */
.dialog-footer :deep(.el-button) {
  border-radius: 8px;
  padding: 0.75rem 1.5rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.dialog-footer :deep(.el-button--primary) {
  background: linear-gradient(135deg, #1e293b 0%, #374151 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.dialog-footer :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #5a67d8 0%, #6c5b93 100%);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  transform: translateY(-1px);
}

.dialog-footer :deep(.el-button:not(.el-button--primary)) {
  background: white;
  border: 2px solid #e0e0e0;
  color: #666;
}

.dialog-footer :deep(.el-button:not(.el-button--primary):hover) {
  border-color: #1e293b;
  color: #1e293b;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

/* Checkbox group styling */
:deep(.el-checkbox-group) {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 0.75rem;
}

:deep(.el-checkbox) {
  margin-right: 0;
  white-space: nowrap;
}

:deep(.el-checkbox__label) {
  font-weight: 500;
  color: #2c3e50;
}

/* Time picker styling */
:deep(.el-time-picker) {
  width: 100%;
}

:deep(.el-date-editor) {
  width: 100%;
}

:deep(.el-date-editor .el-input__wrapper) {
  border-radius: 8px;
}

/* Responsive design */
@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .routine-list {
    grid-template-columns: 1fr;
  }

  /* Adjust dialog width for smaller screens */
  .modern-dialog :deep(.el-dialog) {
    width: 90% !important;
    margin: 5vh auto;
  }
}

@media (max-width: 768px) {
  .announcements-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .announcement-item,
  .task-wall-item,
  .routine-item {
    flex-direction: column;
    gap: 1rem;
  }

  .announcement-actions,
  .task-actions,
  .routine-actions {
    align-self: stretch;
    justify-content: flex-end;
  }

  /* Mobile dialog adjustments */
  .modern-dialog :deep(.el-dialog) {
    width: 95% !important;
    margin: 2vh auto;
  }

  .modern-dialog :deep(.el-dialog__body) {
    padding: 1rem;
  }

  /* Ensure mobile dropdowns have proper spacing */
  .full-width-select :deep(.el-select-dropdown) {
    max-width: 90vw;
  }
}

/* Fix for very small screens */
@media (max-width: 480px) {
  .modern-dialog :deep(.el-dialog) {
    width: 98% !important;
    margin: 1vh auto;
  }

  /* Stack form columns on mobile */
  .modern-dialog :deep(.el-row) {
    flex-direction: column;
  }

  .modern-dialog :deep(.el-col) {
    max-width: 100% !important;
  }
}

/* Critical: Fix z-index issues for date picker in dialogs */
.high-z-index-picker {
  z-index: 9999 !important;
}

/* Ensure date picker panels appear above dialog */
.modern-dialog :deep(.el-date-picker) {
  z-index: 9999 !important;
}

.modern-dialog :deep(.el-picker-panel) {
  z-index: 9999 !important;
}

.modern-dialog :deep(.el-date-picker__editor) {
  z-index: 9999 !important;
}

/* Force all date picker popups to highest z-index */
:deep(.el-picker-panel) {
  z-index: 9999 !important;
}

:deep(.el-date-picker__header) {
  z-index: 9999 !important;
}

:deep(.el-date-picker__time-header) {
  z-index: 9999 !important;
}

/* 方案A: 统一状态标签和按钮颜色 - 专业暗沉系 (高优先级覆盖) */
:deep(.el-tag--success) {
  background-color: #27ae60 !important;
  border-color: #27ae60 !important;
  color: white !important;
}

:deep(.el-tag--warning) {
  background-color: #d68910 !important;
  border-color: #d68910 !important;
  color: white !important;
}

:deep(.el-tag--danger) {
  background-color: #c0392b !important;
  border-color: #c0392b !important;
  color: white !important;
}

:deep(.el-tag--info) {
  background-color: #2980b9 !important;
  border-color: #2980b9 !important;
  color: white !important;
}

:deep(.el-tag--primary) {
  background-color: #2c3e50 !important;
  border-color: #2c3e50 !important;
  color: white !important;
}

/* 按钮统一颜色 - 方案A (高优先级) */
:deep(.el-button--primary) {
  background-color: #2c3e50 !important;
  border-color: #2c3e50 !important;
  color: white !important;
}

:deep(.el-button--primary:hover) {
  background-color: #34495e !important;
  border-color: #34495e !important;
  color: white !important;
}

:deep(.el-button--success) {
  background-color: #27ae60 !important;
  border-color: #27ae60 !important;
  color: white !important;
}

:deep(.el-button--warning) {
  background-color: #d68910 !important;
  border-color: #d68910 !important;
  color: white !important;
}

:deep(.el-button--danger) {
  background-color: #c0392b !important;
  border-color: #c0392b !important;
  color: white !important;
}

/* 表单元素统一 */
:deep(.el-input__wrapper) {
  border: 1px solid #7f8c8d;
}

:deep(.el-input__wrapper:hover) {
  border-color: #2c3e50;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #2c3e50 !important;
  box-shadow: 0 0 0 1px rgba(44, 62, 80, 0.2) !important;
}
</style>
