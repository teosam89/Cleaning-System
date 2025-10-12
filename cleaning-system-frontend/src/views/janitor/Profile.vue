<template>
  <JanitorLayout>
    <div class="profile-container">
      <!-- Page Header -->
      <div class="page-header">
        <h1 class="page-title">Profile</h1>
        <p class="page-subtitle">
          Manage your personal information and account settings
        </p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="8" animated />
        <div class="loading-text">Loading profile data...</div>
      </div>

      <!-- Error State -->
      <div v-else-if="profileError" class="error-container">
        <el-alert
          :title="profileError"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button
          @click="loadProfileData"
          type="primary"
          style="margin-top: 1rem"
        >
          <el-icon><RefreshRight /></el-icon>
          Retry Loading
        </el-button>
      </div>

      <!-- Profile Overview -->
      <el-card v-else class="profile-overview-card" shadow="never">
        <div class="profile-overview">
          <div class="avatar-section">
            <el-avatar
              :size="120"
              :src="getValidAvatarUrl(profileData.avatar)"
              class="profile-avatar"
              @error="handleAvatarError"
            >
              {{ profileData.fullName.charAt(0) }}
            </el-avatar>
            <el-button @click="changeAvatar" class="change-avatar-btn" circle>
              <el-icon><Camera /></el-icon>
            </el-button>
          </div>
          <div class="profile-info">
            <h2 class="profile-name">{{ profileData.fullName }}</h2>
            <div class="profile-meta">
              <el-tag type="success" size="large">{{
                profileData.role
              }}</el-tag>
              <el-tag :type="getStatusType(profileData.status)" size="large">
                {{ getStatusText(profileData.status) }}
              </el-tag>
            </div>
            <div class="profile-details">
              <div class="detail-item">
                <el-icon><User /></el-icon>
                <span>Employee ID: {{ profileData.employeeNumber }}</span>
              </div>
              <div class="detail-item">
                <el-icon><Message /></el-icon>
                <span>{{ profileData.email }}</span>
              </div>
              <div class="detail-item">
                <el-icon><Phone /></el-icon>
                <span>{{ profileData.phone || "Not set" }}</span>
              </div>
              <div class="detail-item">
                <el-icon><Calendar /></el-icon>
                <span>Join Date: {{ formatDate(profileData.joinDate) }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- Main Content Tabs -->
      <el-card class="content-card" shadow="never">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <!-- Basic Information -->
          <el-tab-pane label="Basic Information" name="basic">
            <div class="tab-content">
              <el-form
                ref="basicFormRef"
                :model="basicForm"
                :rules="basicRules"
                label-width="150px"
                class="profile-form"
              >
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="Full Name" prop="fullName">
                      <el-input
                        v-model="basicForm.fullName"
                        :disabled="true"
                        placeholder="Only admin can modify full name"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="Gender" prop="gender">
                      <el-select
                        v-model="basicForm.gender"
                        :disabled="!editMode"
                        style="width: 100%"
                      >
                        <el-option label="Male" value="male" />
                        <el-option label="Female" value="female" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="Phone Number" prop="phone">
                      <el-input
                        v-model="basicForm.phone"
                        :disabled="!editMode"
                        placeholder="e.g., 012-1234567"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="Email" prop="email">
                      <el-input
                        v-model="basicForm.email"
                        :disabled="!editMode"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="Birth Date" prop="birthDate">
                      <el-date-picker
                        v-model="basicForm.birthDate"
                        type="date"
                        placeholder="Select Date"
                        :disabled="!editMode"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item
                      label="Emergency Contact"
                      prop="emergencyContact"
                      class="emergency-contact-item"
                    >
                      <el-input
                        v-model="basicForm.emergencyContact"
                        :disabled="!editMode"
                        placeholder="e.g., 012-1234567"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="Home Address" prop="address">
                  <el-input
                    v-model="basicForm.address"
                    type="textarea"
                    :rows="2"
                    :disabled="!editMode"
                  />
                </el-form-item>

                <div class="form-actions">
                  <el-button
                    v-if="!editMode"
                    @click="enableEdit"
                    type="primary"
                  >
                    <el-icon><Edit /></el-icon>
                    Edit Profile
                  </el-button>
                  <template v-else>
                    <el-button @click="cancelEdit">Cancel</el-button>
                    <el-button
                      type="primary"
                      @click="saveBasicInfo"
                      :loading="saving"
                    >
                      <el-icon><Check /></el-icon>
                      Save Changes
                    </el-button>
                  </template>
                </div>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <!-- Avatar Upload Dialog -->
    <el-dialog
      v-model="avatarDialogVisible"
      title="Change Avatar"
      width="600px"
    >
      <div class="avatar-upload-container">
        <PhotoUpload
          ref="avatarUploadRef"
          :user-id="currentUserId"
          upload-type="avatar"
          :max-files="1"
          :max-file-size="2 * 1024 * 1024"
          accepted-types="image/jpeg,image/png"
          :current-avatar="profileData.avatar"
          @upload-success="handleAvatarUploadSuccess"
          @upload-error="handleAvatarUploadError"
        />
      </div>
    </el-dialog>
  </JanitorLayout>
</template>

<script>
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import JanitorLayout from "@/components/JanitorLayout.vue";
import PhotoUpload from "@/components/PhotoUpload.vue";
import { ProfileAPI } from "@/api/profile";
import { AuthUtils } from "@/utils/auth";
import {
  Camera,
  User,
  Message,
  Phone,
  Calendar,
  Edit,
  Check,
  RefreshRight,
} from "@element-plus/icons-vue";

export default {
  name: "ProfileView",
  components: {
    JanitorLayout,
    PhotoUpload,
    Camera,
    User,
    Message,
    Phone,
    Calendar,
    Edit,
    Check,
    RefreshRight,
  },
  setup() {
    // Reactive data
    const activeTab = ref("basic");
    const editMode = ref(false);
    const saving = ref(false);
    const avatarDialogVisible = ref(false);
    const avatarUploadRef = ref();

    // Current user info from JWT
    const currentUser = AuthUtils.getUserInfo();
    const currentUserId = AuthUtils.getUserId() || currentUser?.userId;

    // Profile data (will be loaded from API)
    const profileData = ref({
      fullName: currentUser?.fullName || "Loading...",
      employeeNumber: "Loading...",
      role: currentUser?.role || "Loading...",
      status: "active",
      email: currentUser?.email || "Loading...",
      phone: "Loading...",
      joinDate: "Loading...",
      avatar: "",
    });

    // Loading states
    const loading = ref(true);
    const profileError = ref(null);

    // Forms
    const basicFormRef = ref();

    const basicForm = reactive({
      fullName: "",
      gender: "",
      phone: "",
      email: "",
      birthDate: "",
      emergencyContact: "",
      address: "",
    });

    // Validation rules
    const phoneValidator = (rule, value, callback) => {
      if (!value) {
        callback();
        return;
      }

      // Remove all non-digit and non-dash characters for validation
      const cleanValue = value.replace(/[^\d-]/g, "");
      const digitsOnly = cleanValue.replace(/-/g, "");

      // Check if it contains only digits and dashes
      if (!/^[\d-]+$/.test(cleanValue)) {
        callback(
          new Error("Phone number can only contain digits and dashes (-)")
        );
        return;
      }

      // Check digit count (5-13 digits, excluding dashes)
      if (digitsOnly.length < 5 || digitsOnly.length > 13) {
        callback(new Error("Phone number must have between 5-13 digits"));
        return;
      }

      callback();
    };

    const basicRules = {
      fullName: [
        { required: true, message: "Please enter full name", trigger: "blur" },
      ],
      phone: [
        {
          validator: phoneValidator,
          trigger: "blur",
        },
      ],
      emergencyContact: [
        {
          validator: phoneValidator,
          trigger: "blur",
        },
      ],
      email: [
        {
          type: "email",
          message: "Please enter a valid email address",
          trigger: "blur",
        },
      ],
    };

    // Load profile data from API
    const loadProfileData = async () => {
      if (!currentUserId) {
        ElMessage.error("User not authenticated");
        profileError.value = "Authentication required";
        loading.value = false;
        return;
      }

      try {
        loading.value = true;
        profileError.value = null;

        console.log("Loading profile for user:", currentUserId);
        const response = await ProfileAPI.getProfile(currentUserId);

        if (response.success && response.data) {
          const data = response.data;

          // Update profile data
          profileData.value = {
            fullName: data.fullName || "Not set",
            employeeNumber: data.employeeNumber || "Not assigned",
            role: data.role || "Janitor",
            status: data.status || "active",
            email: data.email || "Not set",
            phone: data.phone || "Not set",
            joinDate: data.joinDate || new Date().toISOString().split("T")[0],
            avatar: data.avatarUrl || "",
          };

          // Store the raw API data for form initialization (without "Not set" placeholders)
          profileData.value._rawData = {
            fullName: data.fullName || null,
            email: data.email || null,
            phone: data.phone || null,
            gender: data.gender || null,
            birthDate: data.birthDate || null,
            emergencyContact: data.emergencyContact || null,
            address: data.address || null,
          };

          // Initialize basic form
          initializeBasicForm();

          console.log("Profile data loaded successfully");
        } else {
          throw new Error(response.message || "Failed to load profile data");
        }
      } catch (error) {
        console.error("Error loading profile data:", error);
        profileError.value = error.message || "Failed to load profile data";
        ElMessage.error(
          "Failed to load profile data: " + (error.message || "Unknown error")
        );
      } finally {
        loading.value = false;
      }
    };

    // Methods
    const initializeBasicForm = () => {
      // Use raw data for form initialization (null/undefined becomes empty string)
      const rawData = profileData.value._rawData || {};

      basicForm.fullName = rawData.fullName || "";
      basicForm.phone = rawData.phone || "";
      basicForm.email = rawData.email || "";
      basicForm.gender = rawData.gender || "";
      basicForm.birthDate = rawData.birthDate || "";
      basicForm.emergencyContact = rawData.emergencyContact || "";
      basicForm.address = rawData.address || "";
    };

    const enableEdit = () => {
      editMode.value = true;
    };

    const cancelEdit = () => {
      editMode.value = false;
      initializeBasicForm();
    };

    const saveBasicInfo = async () => {
      if (!currentUserId) {
        ElMessage.error("User not authenticated");
        return;
      }

      try {
        await basicFormRef.value.validate();
        saving.value = true;

        console.log("Saving basic profile info for user:", currentUserId);
        // Note: fullName is excluded from janitor updates (admin-only permission)
        const response = await ProfileAPI.updateBasicProfile(currentUserId, {
          phone: basicForm.phone,
          email: basicForm.email,
          gender: basicForm.gender,
          birthDate: basicForm.birthDate,
          emergencyContact: basicForm.emergencyContact,
          address: basicForm.address,
        });

        if (response.success) {
          // Update local profile data (fullName excluded - admin-only permission)
          profileData.value.phone = basicForm.phone;
          profileData.value.email = basicForm.email;

          editMode.value = false;
          ElMessage.success("Personal information saved successfully");
          console.log("Profile updated successfully");
        } else {
          throw new Error(response.message || "Failed to save profile");
        }
      } catch (error) {
        console.error("Error saving basic info:", error);
        ElMessage.error(
          "Save failed: " +
            (error.message || "Please check the input information")
        );
      } finally {
        saving.value = false;
      }
    };

    const changeAvatar = () => {
      avatarDialogVisible.value = true;
    };

    const handleAvatarUploadSuccess = (result) => {
      console.log("Avatar upload successful:", result);

      // Update the profile data with new avatar URL
      if (result.avatarUrl) {
        profileData.value.avatar = result.avatarUrl;
        console.log("Profile avatar updated to:", result.avatarUrl);
      }

      // Close the dialog
      avatarDialogVisible.value = false;

      // Show success message (PhotoUpload component already shows one, but we can add our own)
      ElMessage.success("Profile avatar updated successfully!");
    };

    const handleAvatarUploadError = (error) => {
      console.error("Avatar upload error:", error);
      ElMessage.error("Failed to upload avatar. Please try again.");
    };

    // Utility functions
    const getStatusType = (status) => {
      const types = {
        active: "success",
        inactive: "danger",
        leave: "warning",
      };
      return types[status] || "info";
    };

    const getStatusText = (status) => {
      const texts = {
        active: "Active",
        inactive: "Inactive",
        leave: "On Leave",
      };
      return texts[status] || status;
    };

    const formatDate = (date) => {
      return new Date(date).toLocaleDateString("en-GB");
    };

    // Avatar error handling
    const getValidAvatarUrl = (avatarUrl) => {
      // If avatar URL is empty or null, return null to show fallback
      if (!avatarUrl || avatarUrl.trim() === '') {
        return null;
      }
      // Return the avatar URL for el-avatar to try loading
      return avatarUrl;
    };

    const handleAvatarError = (event) => {
      console.warn("Avatar failed to load:", profileData.value.avatar);
      // Clear the avatar URL to prevent further 404 requests
      profileData.value.avatar = "";
    };

    // Lifecycle
    onMounted(() => {
      console.log("Profile component mounted, loading data...");
      loadProfileData();
    });

    return {
      activeTab,
      editMode,
      saving,
      avatarDialogVisible,
      avatarUploadRef,
      profileData,
      basicFormRef,
      basicForm,
      basicRules,
      loading,
      profileError,
      currentUserId,
      loadProfileData,
      enableEdit,
      cancelEdit,
      saveBasicInfo,
      changeAvatar,
      handleAvatarUploadSuccess,
      handleAvatarUploadError,
      getStatusType,
      getStatusText,
      formatDate,
      getValidAvatarUrl,
      handleAvatarError,
    };
  },
};
</script>

<style scoped>
/* Container */
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Loading and Error States */
.loading-container {
  text-align: center;
  padding: 3rem;
}

.loading-text {
  margin-top: 1rem;
  color: #6b7280;
  font-size: 1rem;
}

.error-container {
  text-align: center;
  padding: 3rem;
  max-width: 500px;
  margin: 0 auto;
}

/* Page Header */
.page-header {
  margin-bottom: 1.5rem;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 1rem;
}

/* Profile Overview Card */
.profile-overview-card {
  margin-bottom: 2rem;
  border-radius: 1.5rem;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.profile-overview {
  display: flex;
  align-items: center;
  gap: 2rem;
  padding: 1rem;
}

.avatar-section {
  position: relative;
  flex-shrink: 0;
}

.profile-avatar {
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.change-avatar-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  background: #10b981;
  border-color: #10b981;
  color: white;
}

.change-avatar-btn:hover {
  background: #059669;
  border-color: #059669;
}

.profile-info {
  flex: 1;
}

.profile-name {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 1rem 0;
  color: white;
}

.profile-meta {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.profile-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 0.75rem;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: rgba(255, 255, 255, 0.9);
}

/* Content Card */
.content-card {
  border-radius: 1rem;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.profile-tabs {
  margin-top: 1rem;
}

.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: 2rem;
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: #e5e7eb;
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(135deg, #10b981, #059669);
}

.profile-tabs :deep(.el-tabs__item) {
  color: #6b7280;
  font-weight: 500;
  font-size: 1rem;
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: #10b981;
  font-weight: 600;
}

/* Tab Content */
.tab-content {
  padding: 1rem 0;
}

/* Profile Form */
.profile-form {
  max-width: 800px;
}

/* Fix form label width and visibility issues */
.profile-form :deep(.el-form-item__label) {
  width: 150px !important;
  min-width: 150px !important;
  white-space: nowrap;
  overflow: visible !important;
  text-overflow: none !important;
  padding-right: 12px;
  text-align: right;
  line-height: 32px;
}

/* Ensure Emergency Contact label has proper width */
.emergency-contact-item :deep(.el-form-item__label) {
  width: 150px !important;
  min-width: 150px !important;
  white-space: nowrap;
  overflow: visible !important;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}

/* Avatar Upload */
.avatar-upload-container {
  padding: 0.5rem 0;
}

/* Responsive Design */
@media (max-width: 768px) {
  .profile-overview {
    flex-direction: column;
    text-align: center;
  }

  .profile-details {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .profile-meta {
    justify-content: center;
  }

  .profile-form {
    max-width: none;
  }
}
</style>
