<template>
  <JanitorLayout>
    <div class="faq-container">
      <!-- Page Header -->
      <div class="faq-header">
        <div class="header-icon">
          <el-icon :size="48" color="#667eea">
            <QuestionFilled />
          </el-icon>
        </div>
        <h1 class="page-title">Frequently Asked Questions</h1>
        <p class="page-subtitle">
          Find answers to common questions about using the CleanMS system
        </p>
      </div>

      <!-- Search Box (Optional) -->
      <div class="search-section">
        <el-input
          v-model="searchQuery"
          placeholder="Search for questions..."
          :prefix-icon="Search"
          size="large"
          clearable
          class="search-input"
        />
      </div>

      <!-- FAQ Content -->
      <div class="faq-content">
        <!-- Filter FAQ items based on search query -->
        <el-collapse v-model="activeNames" accordion>
          <!-- Getting Started Section -->
          <el-collapse-item
            name="getting-started"
            v-if="shouldShowSection('Getting Started')"
          >
            <template #title>
              <div class="collapse-title">
                <el-icon color="#10b981"><Connection /></el-icon>
                <span>Getting Started</span>
              </div>
            </template>

            <!-- FAQ Item: Login -->
            <div class="faq-item" v-if="shouldShowQuestion('login')">
              <h4 class="question">How do I login to the system?</h4>
              <div class="answer">
                <p>Follow these steps to login:</p>
                <ol>
                  <li>Open the CleanMS application in your web browser</li>
                  <li>Enter your username and password</li>
                  <li>Click the "Login" button</li>
                  <li>
                    You will be redirected to your janitor dashboard
                    automatically
                  </li>
                </ol>
                <p class="note">
                  <strong>Note:</strong> If you forgot your password, contact
                  your administrator for assistance.
                </p>
              </div>
            </div>

            <!-- FAQ Item: Dashboard -->
            <div class="faq-item" v-if="shouldShowQuestion('dashboard')">
              <h4 class="question">What is the Dashboard?</h4>
              <div class="answer">
                <p>
                  The Dashboard is your home page that shows an overview of your
                  work:
                </p>
                <ul>
                  <li>
                    <strong>Welcome Section:</strong> Shows current date and
                    check-in/check-out button
                  </li>
                  <li>
                    <strong>System Announcements:</strong> Important messages
                    from administrators
                  </li>
                  <li>
                    <strong>Quick Actions:</strong> Fast access to common tasks
                  </li>
                  <li>
                    <strong>Recent Tasks:</strong> Preview of your latest
                    assigned tasks
                  </li>
                </ul>
              </div>
            </div>

            <!-- FAQ Item: Check-in -->
            <div class="faq-item" v-if="shouldShowQuestion('check-in')">
              <h4 class="question">How do I check in for work?</h4>
              <div class="answer">
                <p>To mark your attendance:</p>
                <ol>
                  <li>Go to the Dashboard (Home page)</li>
                  <li>Click the green "Check In" button in the welcome section</li>
                  <li>Confirm your check-in in the popup dialog</li>
                  <li>
                    The button will turn red and show "Check Out" when you're
                    checked in
                  </li>
                </ol>
                <p class="tip">
                  <strong>Tip:</strong> Always remember to check in when you
                  start work and check out when you finish!
                </p>
              </div>
            </div>
          </el-collapse-item>

          <!-- Task Management Section -->
          <el-collapse-item
            name="task-management"
            v-if="shouldShowSection('Task Management')"
          >
            <template #title>
              <div class="collapse-title">
                <el-icon color="#667eea"><Document /></el-icon>
                <span>Task Management</span>
              </div>
            </template>

            <!-- FAQ Item: View Tasks -->
            <div class="faq-item" v-if="shouldShowQuestion('view tasks')">
              <h4 class="question">How do I view my assigned tasks?</h4>
              <div class="answer">
                <p>There are two ways to view your tasks:</p>
                <ol>
                  <li>
                    Click "My Tasks" in the bottom navigation bar to see all
                    tasks assigned to you
                  </li>
                  <li>
                    View the "Recent Tasks" section on your Dashboard for a
                    quick preview
                  </li>
                </ol>
                <p>
                  Each task shows important information like task name, time,
                  status, and priority level.
                </p>
              </div>
            </div>

            <!-- FAQ Item: Task Wall -->
            <div class="faq-item" v-if="shouldShowQuestion('task wall')">
              <h4 class="question">What is the Task Wall?</h4>
              <div class="answer">
                <p>
                  The Task Wall shows all available tasks that haven't been
                  assigned yet. You can:
                </p>
                <ul>
                  <li>Browse unassigned cleaning tasks</li>
                  <li>Claim tasks that match your schedule</li>
                  <li>View task details before claiming</li>
                  <li>Filter tasks by location or type</li>
                </ul>
                <p class="note">
                  <strong>Note:</strong> Once you claim a task from the Task
                  Wall, it will appear in your "My Tasks" list.
                </p>
              </div>
            </div>

            <!-- FAQ Item: Update Status -->
            <div class="faq-item" v-if="shouldShowQuestion('update status')">
              <h4 class="question">How do I update a task status?</h4>
              <div class="answer">
                <p>To update task progress:</p>
                <ol>
                  <li>Go to "My Tasks" from the navigation bar</li>
                  <li>Click "View" button on the task you want to update</li>
                  <li>Click the "Update Status" or "Complete Task" button</li>
                  <li>
                    The task status will change (Pending → In Progress →
                    Completed)
                  </li>
                </ol>
              </div>
            </div>

            <!-- FAQ Item: Upload Photos -->
            <div class="faq-item" v-if="shouldShowQuestion('upload photo')">
              <h4 class="question">
                How do I upload photos for completed tasks?
              </h4>
              <div class="answer">
                <p>To upload task completion photos:</p>
                <ol>
                  <li>Open the task detail page</li>
                  <li>Click the "Upload Photos" or camera icon button</li>
                  <li>Select photos from your device</li>
                  <li>Add optional descriptions for each photo</li>
                  <li>Click "Submit" to upload</li>
                </ol>
                <p class="tip">
                  <strong>Tip:</strong> Upload clear photos showing the cleaned
                  area for better verification.
                </p>
              </div>
            </div>
          </el-collapse-item>

          <!-- Attendance Section -->
          <el-collapse-item
            name="attendance"
            v-if="shouldShowSection('Attendance')"
          >
            <template #title>
              <div class="collapse-title">
                <el-icon color="#f59e0b"><Clock /></el-icon>
                <span>Attendance & Time Tracking</span>
              </div>
            </template>

            <!-- FAQ Item: Check-in System -->
            <div class="faq-item" v-if="shouldShowQuestion('check-in work')">
              <h4 class="question">How does the check-in system work?</h4>
              <div class="answer">
                <p>The attendance system tracks your work hours:</p>
                <ul>
                  <li>
                    <strong>Check In:</strong> Click when you start your shift
                  </li>
                  <li>
                    <strong>Check Out:</strong> Click when you finish your shift
                  </li>
                  <li>
                    System automatically calculates total work hours
                  </li>
                  <li>All attendance records are saved in the system</li>
                </ul>
              </div>
            </div>

            <!-- FAQ Item: Attendance History -->
            <div class="faq-item" v-if="shouldShowQuestion('history')">
              <h4 class="question">Where can I view my attendance history?</h4>
              <div class="answer">
                <p>To view your attendance records:</p>
                <ol>
                  <li>Click "Attendance" in the bottom navigation bar</li>
                  <li>You'll see your current check-in status</li>
                  <li>
                    Click "View History" to see all past attendance records
                  </li>
                  <li>
                    Filter by date range to find specific records
                  </li>
                </ol>
              </div>
            </div>

            <!-- FAQ Item: Forgot Check-out -->
            <div class="faq-item" v-if="shouldShowQuestion('forgot check')">
              <h4 class="question">What if I forgot to check out?</h4>
              <div class="answer">
                <p>If you forgot to check out:</p>
                <ol>
                  <li>Contact your supervisor or administrator immediately</li>
                  <li>Explain the situation and provide your actual work hours</li>
                  <li>
                    Admin can manually adjust your attendance record
                  </li>
                  <li>Set a reminder to always check out on time</li>
                </ol>
                <p class="note">
                  <strong>Important:</strong> Always remember to check out to
                  ensure accurate time tracking.
                </p>
              </div>
            </div>
          </el-collapse-item>

          <!-- Announcements Section -->
          <el-collapse-item
            name="announcements"
            v-if="shouldShowSection('Announcements')"
          >
            <template #title>
              <div class="collapse-title">
                <el-icon color="#ef4444"><BellFilled /></el-icon>
                <span>System Announcements</span>
              </div>
            </template>

            <!-- FAQ Item: View Announcements -->
            <div class="faq-item" v-if="shouldShowQuestion('announcement')">
              <h4 class="question">How do I view system announcements?</h4>
              <div class="answer">
                <p>You can view announcements in two ways:</p>
                <ol>
                  <li>
                    <strong>Dashboard:</strong> Latest announcement preview
                    appears in the "System Announcements" section
                  </li>
                  <li>
                    <strong>Announcements Page:</strong> Click "Announcements"
                    in navigation to see all announcements
                  </li>
                </ol>
              </div>
            </div>

            <!-- FAQ Item: Priority Levels -->
            <div class="faq-item" v-if="shouldShowQuestion('priority')">
              <h4 class="question">What do the priority levels mean?</h4>
              <div class="answer">
                <p>Announcements have different priority levels:</p>
                <ul>
                  <li>
                    <el-tag type="danger" size="small">URGENT</el-tag> -
                    Immediate action required
                  </li>
                  <li>
                    <el-tag type="warning" size="small">HIGH</el-tag> - Important,
                    read soon
                  </li>
                  <li>
                    <el-tag type="success" size="small">NORMAL</el-tag> -
                    Standard information
                  </li>
                  <li>
                    <el-tag type="info" size="small">LOW</el-tag> - Optional
                    reading
                  </li>
                </ul>
                <p class="tip">
                  <strong>Tip:</strong> Always check URGENT and HIGH priority
                  announcements immediately!
                </p>
              </div>
            </div>
          </el-collapse-item>

          <!-- Profile Management Section -->
          <el-collapse-item
            name="profile"
            v-if="shouldShowSection('Profile')"
          >
            <template #title>
              <div class="collapse-title">
                <el-icon color="#3b82f6"><User /></el-icon>
                <span>Profile Management</span>
              </div>
            </template>

            <!-- FAQ Item: Update Profile -->
            <div class="faq-item" v-if="shouldShowQuestion('update profile')">
              <h4 class="question">How do I update my profile information?</h4>
              <div class="answer">
                <p>To update your profile:</p>
                <ol>
                  <li>Click "Profile" in the bottom navigation bar</li>
                  <li>Click the "Edit Profile" button</li>
                  <li>Update your information (name, email, phone, etc.)</li>
                  <li>Click "Save Changes" to update</li>
                </ol>
                <p class="note">
                  <strong>Note:</strong> Some fields may be locked by your
                  administrator for security reasons.
                </p>
              </div>
            </div>

            <!-- FAQ Item: Change Avatar -->
            <div class="faq-item" v-if="shouldShowQuestion('avatar')">
              <h4 class="question">How do I change my profile picture?</h4>
              <div class="answer">
                <p>To update your avatar:</p>
                <ol>
                  <li>Go to your Profile page</li>
                  <li>Click on your current avatar or the camera icon</li>
                  <li>Select a new photo from your device</li>
                  <li>Crop or adjust if needed</li>
                  <li>Save the changes</li>
                </ol>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>

        <!-- No Results Message -->
        <div v-if="!hasVisibleItems" class="no-results">
          <el-empty
            description="No FAQ items match your search"
            :image-size="120"
          >
            <el-button type="primary" @click="searchQuery = ''">
              Clear Search
            </el-button>
          </el-empty>
        </div>
      </div>

      <!-- Help Footer -->
      <div class="help-footer">
        <el-alert
          title="Still need help?"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>
              If you can't find the answer you're looking for, please contact
              your supervisor or system administrator for assistance.
            </p>
          </template>
        </el-alert>
      </div>
    </div>
  </JanitorLayout>
</template>

<script>
import { ref, computed } from "vue";
import JanitorLayout from "@/components/JanitorLayout.vue";
import {
  QuestionFilled,
  Search,
  Connection,
  Document,
  Clock,
  BellFilled,
  User,
} from "@element-plus/icons-vue";

export default {
  name: "JanitorFAQ",
  components: {
    JanitorLayout,
    QuestionFilled,
    Search,
    Connection,
    Document,
    Clock,
    BellFilled,
    User,
  },
  setup() {
    // Active accordion sections
    const activeNames = ref(["getting-started"]);

    // Search query for filtering FAQ items
    const searchQuery = ref("");

    /**
     * Check if a section should be displayed based on search query
     * Returns true if search is empty or matches section name
     */
    const shouldShowSection = (sectionName) => {
      if (!searchQuery.value) return true;
      return sectionName
        .toLowerCase()
        .includes(searchQuery.value.toLowerCase());
    };

    /**
     * Check if a question should be displayed based on search query
     * Searches in question text and answer content
     */
    const shouldShowQuestion = (keywords) => {
      if (!searchQuery.value) return true;
      const query = searchQuery.value.toLowerCase();
      return keywords.toLowerCase().includes(query);
    };

    /**
     * Check if any FAQ items are visible after filtering
     * Used to show "no results" message
     */
    const hasVisibleItems = computed(() => {
      if (!searchQuery.value) return true;

      const sections = [
        "Getting Started",
        "Task Management",
        "Attendance",
        "Announcements",
        "Profile",
      ];

      return sections.some((section) => shouldShowSection(section));
    });

    return {
      activeNames,
      searchQuery,
      shouldShowSection,
      shouldShowQuestion,
      hasVisibleItems,
    };
  },
};
</script>

<style scoped>
/* Main container */
.faq-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 1rem;
}

/* Page Header */
.faq-header {
  text-align: center;
  margin-bottom: 2rem;
  padding: 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 1rem;
  color: white;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.header-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.header-icon .el-icon {
  background: rgba(255, 255, 255, 0.2);
  padding: 1rem;
  border-radius: 50%;
  backdrop-filter: blur(10px);
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  color: white;
}

.page-subtitle {
  font-size: 1rem;
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
}

/* Search Section */
.search-section {
  margin-bottom: 2rem;
}

.search-input {
  max-width: 600px;
  margin: 0 auto;
}

/* FAQ Content */
.faq-content {
  margin-bottom: 2rem;
}

/* Collapse Title Styling */
.collapse-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.125rem;
  font-weight: 600;
  color: #2c3e50;
}

/* FAQ Item */
.faq-item {
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  background: #f8f9fa;
  border-radius: 0.75rem;
  border-left: 4px solid #667eea;
}

.faq-item:last-child {
  margin-bottom: 0;
}

/* Question Styling */
.question {
  font-size: 1.125rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 1rem 0;
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.question::before {
  content: "Q:";
  color: #667eea;
  font-weight: 700;
  flex-shrink: 0;
}

/* Answer Styling */
.answer {
  font-size: 0.95rem;
  color: #5a6c7d;
  line-height: 1.7;
}

.answer p {
  margin: 0 0 1rem 0;
}

.answer p:last-child {
  margin-bottom: 0;
}

.answer ol,
.answer ul {
  margin: 0.5rem 0;
  padding-left: 1.5rem;
}

.answer li {
  margin: 0.5rem 0;
}

.answer strong {
  color: #2c3e50;
  font-weight: 600;
}

/* Note and Tip Boxes */
.note,
.tip {
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 0.5rem;
  border-left: 3px solid #667eea;
}

.tip {
  background: rgba(16, 185, 129, 0.1);
  border-left-color: #10b981;
}

/* Help Footer */
.help-footer {
  margin-top: 2rem;
}

.help-footer :deep(.el-alert) {
  border-radius: 0.75rem;
}

.help-footer p {
  margin: 0;
  font-size: 0.95rem;
  line-height: 1.6;
}

/* No Results */
.no-results {
  padding: 3rem 1rem;
  text-align: center;
}

/* Collapse Component Overrides */
:deep(.el-collapse) {
  border: none;
}

:deep(.el-collapse-item__header) {
  background: white;
  padding: 1.25rem 1.5rem;
  border-radius: 0.75rem;
  margin-bottom: 0.75rem;
  border: 1px solid #e5e7eb;
  font-weight: 600;
  transition: all 0.3s ease;
}

:deep(.el-collapse-item__header:hover) {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

:deep(.el-collapse-item__wrap) {
  border: none;
  background: transparent;
}

:deep(.el-collapse-item__content) {
  padding: 0 0 1.5rem 0;
}

/* Responsive Design */
@media (max-width: 768px) {
  .faq-container {
    padding: 0.5rem;
  }

  .faq-header {
    padding: 1.5rem 1rem;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .page-subtitle {
    font-size: 0.9rem;
  }

  .collapse-title {
    font-size: 1rem;
  }

  .question {
    font-size: 1rem;
  }

  .faq-item {
    padding: 1rem;
  }

  .answer {
    font-size: 0.9rem;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.25rem;
  }

  .collapse-title span {
    font-size: 0.9rem;
  }
}
</style>
