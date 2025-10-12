<template>
  <div class="supervisor-layout">
    <!-- Top Header -->
    <div class="supervisor-header">
      <div class="header-left">
        <h1 class="page-title">Team Management Portal</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>Supervisor</el-breadcrumb-item>
          <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleMenuCommand">
          <div class="user-profile">
            <el-avatar size="small" :src="userAvatar">
              {{ userInfo.fullName.charAt(0) }}
            </el-avatar>
            <div class="user-info">
              <span class="user-name">{{ userInfo.fullName }}</span>
              <span class="user-role">Team Supervisor</span>
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

    <div class="supervisor-content">
      <!-- Sidebar Navigation -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h3>Team Navigation</h3>
        </div>
        <div class="main-menu">
          <h4>SUPERVISION MENU</h4>
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            background-color="transparent"
            text-color="#cbd5e1"
            active-text-color="#e2e8f0"
          >
            <el-menu-item index="dashboard" @click="navigateTo('dashboard')">
              <el-icon><Monitor /></el-icon>
              <span>Team Dashboard</span>
            </el-menu-item>
            <el-menu-item index="staff" @click="navigateTo('staff')">
              <el-icon><User /></el-icon>
              <span>Staff Overview</span>
            </el-menu-item>
            <el-menu-item index="calendar" @click="navigateTo('calendar')">
              <el-icon><Calendar /></el-icon>
              <span>Task Scheduler</span>
            </el-menu-item>
            <el-menu-item
              index="announcements"
              @click="navigateTo('announcements')"
            >
              <el-icon><BellFilled /></el-icon>
              <span>Team Tasks & Notes</span>
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
  BellFilled,
  ArrowDown,
} from "@element-plus/icons-vue";

export default {
  name: "SupervisorLayout",
  components: {
    Monitor,
    User,
    Calendar,
    BellFilled,
    ArrowDown,
  },
  setup() {
    const router = useRouter();
    const route = useRoute();

    const userAvatar = ref(
      "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
    );

    // Get user info from JWT token
    const userInfo = ref({
      fullName: "Supervisor User",
      role: "supervisor",
    });

    // Initialize user info with comprehensive error handling
    const initUserInfo = () => {
      try {
        const tokenUserInfo = AuthUtils.getUserInfo();
        if (tokenUserInfo && typeof tokenUserInfo === "object") {
          // Validate user role for supervisor access
          if (tokenUserInfo.role !== "supervisor") {
            console.warn(
              "Non-supervisor user accessing supervisor layout:",
              tokenUserInfo.role
            );
            ElMessage.error(
              "Insufficient permissions for supervisor interface"
            );
            API.logout();
            return;
          }

          userInfo.value = {
            fullName: String(
              tokenUserInfo.fullName || "Supervisor User"
            ).trim(),
            role: String(tokenUserInfo.role || "supervisor").toLowerCase(),
          };

          console.log(
            "Supervisor user info initialized:",
            userInfo.value.fullName
          );
        } else {
          console.warn("Invalid or missing user token information");
          // Apply fallback but warn user
          userInfo.value = {
            fullName: "Supervisor User",
            role: "supervisor",
          };
          ElMessage.warning(
            "Session data incomplete. Please refresh if needed."
          );
        }
      } catch (error) {
        console.error("Error initializing user info:", error);
        ElMessage.error("Authentication error. Please login again.");
        API.logout();
      }
    };

    // Initialize user info with error handling
    initUserInfo();

    const activeMenu = computed(() => {
      if (route.path === "/supervisor") return "dashboard";
      if (route.path === "/supervisor/staff") return "staff";
      if (route.path === "/supervisor/calendar") return "calendar";
      if (route.path === "/supervisor/team-tasks") return "announcements";
      return "dashboard";
    });

    const currentPageTitle = computed(() => {
      if (route.path === "/supervisor") return "Team Dashboard";
      if (route.path === "/supervisor/staff") return "Staff Overview";
      if (route.path === "/supervisor/calendar") return "Task Scheduler";
      if (route.path === "/supervisor/team-tasks") return "Team Tasks & Notes";
      return "Team Dashboard";
    });

    const navigateTo = async (menu) => {
      try {
        // Basic authentication check
        if (!AuthUtils.isAuthenticated()) {
          ElMessage.error("Session expired. Please login again.");
          API.logout();
          return;
        }

        let targetRoute = "";
        switch (menu) {
          case "dashboard":
            targetRoute = "/supervisor";
            break;
          case "staff":
            targetRoute = "/supervisor/staff";
            break;
          case "calendar":
            targetRoute = "/supervisor/calendar";
            break;
          case "announcements":
            targetRoute = "/supervisor/team-tasks";
            break;
          default:
            console.warn("Unknown navigation menu:", menu);
            ElMessage.warning("Unknown page requested");
            return;
        }

        console.log("Navigating to:", targetRoute);

        // Perform navigation with simplified error handling
        await router.push(targetRoute);
        console.log("Successfully navigated to:", targetRoute);
      } catch (error) {
        console.error("Navigation error:", error);

        if (error.name === "NavigationDuplicated") {
          // User is already on this page - not an error
          console.log("Already on target page");
        } else if (
          error.message &&
          error.message.includes("Avoided redundant navigation")
        ) {
          // Vue Router redundant navigation warning - not a real error
          console.log("Navigation to same route avoided");
        } else {
          console.error("Actual navigation error:", error);
          ElMessage.error(
            `Navigation failed: ${error.message || "Unknown error"}`
          );
        }
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

        console.log("User confirmed logout");

        // Enhanced logout with error handling
        try {
          // Clear any auto-refresh intervals or pending requests
          if (window.supervisorIntervals) {
            window.supervisorIntervals.forEach((interval) =>
              clearInterval(interval)
            );
            window.supervisorIntervals = [];
          }

          // Use API.logout() which will clear JWT tokens and redirect
          API.logout();
          ElMessage.success("Successfully logged out");
        } catch (logoutError) {
          console.error("Logout process error:", logoutError);
          // Force logout even if API call fails
          AuthUtils.clearToken();
          router.push("/");
          ElMessage.warning("Logged out (with some cleanup issues)");
        }
      } catch (confirmError) {
        // User cancelled logout or dialog error
        if (confirmError !== "cancel") {
          console.error("Logout confirmation error:", confirmError);
          ElMessage.error("Logout dialog error. Please try again.");
        }
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
/* Main container - Professional Theme */
.supervisor-layout {
  min-height: 100vh;
  background-color: #f8fafc;
  display: flex;
  flex-direction: column;
}

/* Header styles - Professional Dark Theme */
.supervisor-header {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);
  color: white;
  padding: 1.25rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid rgba(71, 85, 105, 0.3);
  backdrop-filter: blur(10px);
}

.header-left .page-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 0.375rem 0;
  color: #f1f5f9;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
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

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  padding: 0.75rem 1rem;
  border-radius: 1rem;
  background: rgba(51, 65, 85, 0.4);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(71, 85, 105, 0.4);
  transition: all 0.3s ease;
  min-width: 180px;
}

.user-profile:hover {
  background: rgba(51, 65, 85, 0.6);
  border-color: rgba(71, 85, 105, 0.6);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
}

.user-profile :deep(.el-avatar) {
  border: 2px solid rgba(71, 85, 105, 0.5);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
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
  color: #f1f5f9;
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
.supervisor-content {
  display: flex;
  flex: 1;
}

/* Sidebar styles - Professional Dark Theme */
.sidebar {
  width: 280px;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 50%, #334155 100%);
  color: white;
  padding: 2rem 0;
  box-shadow: 4px 0 32px rgba(0, 0, 0, 0.4);
  border-right: 1px solid rgba(71, 85, 105, 0.3);
}

.sidebar-header h3 {
  color: #e2e8f0;
  margin: 0 0 2.5rem 1.5rem;
  font-size: 1.375rem;
  font-weight: 700;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
  letter-spacing: -0.025em;
}

.main-menu h4 {
  color: #94a3b8;
  margin: 0 0 1.25rem 1.5rem;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  opacity: 0.9;
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
  background: linear-gradient(135deg, #64748b, #475569);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(71, 85, 105, 0.3);
  color: #94a3b8;
  transform: translateX(4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.sidebar-menu .el-menu-item:hover::before {
  transform: scaleY(1);
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(
    135deg,
    rgba(71, 85, 105, 0.4),
    rgba(51, 65, 85, 0.3)
  );
  color: #e2e8f0;
  font-weight: 600;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(71, 85, 105, 0.5);
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

/* Main content styles - Professional Theme */
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

  .user-profile {
    min-width: 160px;
  }
}

@media (max-width: 768px) {
  .supervisor-header {
    padding: 1rem 1.5rem;
  }

  .header-left .page-title {
    font-size: 1.5rem;
  }

  .supervisor-content {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    padding: 1.5rem 0;
    position: relative;
  }

  .user-profile {
    min-width: 140px;
    padding: 0.5rem 0.75rem;
  }
}

@media (max-width: 480px) {
  .supervisor-header {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }

  .header-right {
    justify-content: center;
    gap: 1rem;
  }

}
</style>
