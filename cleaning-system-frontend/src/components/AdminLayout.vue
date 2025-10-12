<template>
  <div class="admin-layout">
    <!-- Top Header -->
    <div class="admin-header">
      <div class="header-left">
        <h1 class="page-title">Cleaning Management System</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>Admin</el-breadcrumb-item>
          <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <!-- Search functionality removed as requested -->
        <el-dropdown @command="handleMenuCommand">
          <div class="user-profile">
            <el-avatar size="small" :src="userAvatar">
              {{ userInfo.fullName.charAt(0) }}
            </el-avatar>
            <div class="user-info">
              <span class="user-name">{{ userInfo.fullName }}</span>
              <span class="user-role">{{
                userInfo.role === "admin"
                  ? "System Administrator"
                  : userInfo.role
              }}</span>
            </div>
            <el-icon class="dropdown-arrow">
              <ArrowDown />
            </el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">Profile</el-dropdown-item>
              <el-dropdown-item command="settings">Settings</el-dropdown-item>
              <el-dropdown-item divided command="logout"
                >Logout</el-dropdown-item
              >
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="admin-content">
      <!-- Sidebar Navigation -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h3>Navigation Bar</h3>
        </div>
        <div class="main-menu">
          <h4>MAIN MENU</h4>
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            background-color="transparent"
            text-color="#ffffff"
            active-text-color="#409EFF"
          >
            <el-menu-item index="dashboard" @click="navigateTo('dashboard')">
              <el-icon><Monitor /></el-icon>
              <span>Dashboard</span>
            </el-menu-item>
            <el-menu-item index="staff" @click="navigateTo('staff')">
              <el-icon><User /></el-icon>
              <span>Staff Profiles</span>
            </el-menu-item>
            <el-menu-item index="calendar" @click="navigateTo('calendar')">
              <el-icon><Calendar /></el-icon>
              <span>Task Calendar</span>
            </el-menu-item>
            <el-menu-item
              index="announcements"
              @click="navigateTo('announcements')"
            >
              <el-icon><BellFilled /></el-icon>
              <span>Announcements & Tasks</span>
            </el-menu-item>
            <el-menu-item index="monitor" @click="navigateTo('monitor')">
              <el-icon><DataAnalysis /></el-icon>
              <span>Job Monitor</span>
            </el-menu-item>
          </el-menu>
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="main-content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { AuthUtils } from "@/utils/auth";
import { API } from "@/utils/request";
import {
  Monitor,
  User,
  Calendar,
  DataAnalysis,
  BellFilled,
  ArrowDown,
} from "@element-plus/icons-vue";

export default {
  name: "AdminLayout",
  components: {
    Monitor,
    User,
    Calendar,
    DataAnalysis,
    BellFilled,
    ArrowDown,
  },
  setup() {
    const router = useRouter();
    const route = useRoute();

    // Search functionality removed as requested
    const userAvatar = ref(
      "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
    );

    // Get user info from JWT token
    const userInfo = ref({
      fullName: "Admin User",
      role: "admin",
    });

    // Initialize user info on component mount
    const initUserInfo = () => {
      const tokenUserInfo = AuthUtils.getUserInfo();
      if (tokenUserInfo) {
        userInfo.value = {
          fullName: tokenUserInfo.fullName || "Admin User",
          role: tokenUserInfo.role || "admin",
        };
      }
    };

    // Initialize user info
    initUserInfo();

    const activeMenu = computed(() => {
      if (route.path === "/admin") return "dashboard";
      if (route.path === "/admin/staff") return "staff";
      if (route.path === "/admin/calendar") return "calendar";
      if (route.path === "/admin/announcements") return "announcements";
      if (route.path === "/admin/monitor") return "monitor";
      return "dashboard";
    });

    const currentPageTitle = computed(() => {
      if (route.path === "/admin") return "Dashboard";
      if (route.path === "/admin/staff") return "Staff Management";
      if (route.path === "/admin/calendar") return "Task Calendar";
      if (route.path === "/admin/announcements") return "Announcements & Tasks";
      if (route.path === "/admin/monitor") return "Job Monitor";
      return "Dashboard";
    });

    const navigateTo = (menu) => {
      switch (menu) {
        case "dashboard":
          router.push("/admin");
          break;
        case "staff":
          router.push("/admin/staff");
          break;
        case "calendar":
          router.push("/admin/calendar");
          break;
        case "announcements":
          router.push("/admin/announcements");
          break;
        case "monitor":
          router.push("/admin/monitor");
          break;
      }
    };

    const handleMenuCommand = async (command) => {
      switch (command) {
        case "profile":
          ElMessage.info("Profile page coming soon");
          break;
        case "settings":
          ElMessage.info("Settings page coming soon");
          break;
        case "logout":
          await handleLogout();
          break;
      }
    };

    const handleLogout = async () => {
      try {
        await ElMessageBox.confirm(
          "Are you sure you want to logout?",
          "Logout Confirmation",
          {
            confirmButtonText: "Confirm",
            cancelButtonText: "Cancel",
            type: "warning",
          }
        );

        // Use API.logout() which will clear JWT tokens and redirect
        API.logout();
      } catch {
        // User cancelled logout
      }
    };

    return {
      userAvatar,
      userInfo,
      activeMenu,
      currentPageTitle,
      navigateTo,
      handleMenuCommand,
    };
  },
};
</script>

<style scoped>
/* Main container */
.admin-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

/* Header styles */
.admin-header {
  background: linear-gradient(135deg, #1e293b 0%, #334155 50%, #475569 100%);
  color: white;
  padding: 1.25rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.header-left .page-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 0.375rem 0;
  color: #f8fafc;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: -0.025em;
}

.header-left :deep(.el-breadcrumb) {
  margin-top: 0.25rem;
}

.header-left :deep(.el-breadcrumb__item) {
  color: #cbd5e1;
  font-weight: 500;
}

.header-left :deep(.el-breadcrumb__separator) {
  color: #94a3b8;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.search-input {
  width: 320px;
}

.search-input :deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__wrapper):hover {
  background-color: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: #10b981;
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
}

.search-input :deep(.el-input__inner) {
  color: white;
  font-weight: 500;
}

.search-input :deep(.el-input__inner::placeholder) {
  color: #cbd5e1;
}

.search-input :deep(.el-input__prefix) {
  color: #94a3b8;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  padding: 0.75rem 1rem;
  border-radius: 1rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  min-width: 180px;
}

.user-profile:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-profile :deep(.el-avatar) {
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.user-name {
  font-weight: 600;
  color: #f8fafc;
  font-size: 0.95rem;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 0.8rem;
  color: #cbd5e1;
  font-weight: 500;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-arrow {
  color: #94a3b8;
  font-size: 0.875rem;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.user-profile:hover .dropdown-arrow {
  transform: rotate(180deg);
  color: #cbd5e1;
}

/* Content layout */
.admin-content {
  display: flex;
  flex: 1;
}

/* Sidebar styles */
.sidebar {
  width: 280px;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 50%, #334155 100%);
  color: white;
  padding: 2rem 0;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h3 {
  color: #f1f5f9;
  margin: 0 0 2.5rem 1.5rem;
  font-size: 1.375rem;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: -0.025em;
}

.main-menu h4 {
  color: #94a3b8;
  margin: 0 0 1.25rem 1.5rem;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  opacity: 0.8;
}

.sidebar-menu {
  border: none;
  background: transparent;
}

.sidebar-menu .el-menu-item {
  border-radius: 0.75rem;
  margin: 0.375rem 1rem;
  padding: 0.75rem 1rem;
  transition: all 0.3s ease;
  color: #cbd5e1;
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

.sidebar-menu .el-menu-item::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 3px;
  background: linear-gradient(135deg, #10b981, #059669);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.15);
}

.sidebar-menu .el-menu-item:hover::before {
  transform: scaleY(1);
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(
    135deg,
    rgba(16, 185, 129, 0.2),
    rgba(5, 150, 105, 0.15)
  );
  color: #10b981;
  font-weight: 600;
  box-shadow: 0 4px 20px rgba(16, 185, 129, 0.2);
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.sidebar-menu .el-menu-item.is-active::before {
  transform: scaleY(1);
}

.sidebar-menu .el-menu-item .el-icon {
  font-size: 1.125rem;
  margin-right: 0.75rem;
  transition: transform 0.3s ease;
}

.sidebar-menu .el-menu-item:hover .el-icon,
.sidebar-menu .el-menu-item.is-active .el-icon {
  transform: scale(1.1);
}

/* Main content styles */
.main-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: calc(100vh - 80px);
}

/* Responsive design */
@media (max-width: 1024px) {
  .sidebar {
    width: 260px;
  }

  .search-input {
    width: 280px;
  }

  .user-profile {
    min-width: 160px;
  }
}

@media (max-width: 768px) {
  .admin-header {
    padding: 1rem 1.5rem;
  }

  .header-left .page-title {
    font-size: 1.5rem;
  }

  .admin-content {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    padding: 1.5rem 0;
  }

  .search-input {
    width: 200px;
  }

  .user-profile {
    min-width: 140px;
    padding: 0.5rem 0.75rem;
  }
}

@media (max-width: 480px) {
  .admin-header {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }

  .header-right {
    justify-content: center;
    gap: 1rem;
  }

  .search-input {
    width: 240px;
  }
}
</style>
