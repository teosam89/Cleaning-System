<template>
  <div class="janitor-layout">
    <!-- Mobile-friendly Header -->
    <div class="janitor-header">
      <div class="header-content">
        <div class="header-left">
          <div class="logo-section">
            <div class="logo-icon">🧹</div>
            <h1 class="app-title">CleanMS</h1>
          </div>
          <div class="user-info">
            <el-avatar
              :size="32"
              :src="userProfile.avatarUrl || userProfile.avatar"
            >
              {{ userProfile.fullName.charAt(0) }}
            </el-avatar>
            <div class="user-details">
              <span class="user-name">{{ userProfile.fullName }}</span>
              <span class="user-role">Janitor</span>
            </div>
          </div>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleMenuCommand">
            <el-button circle>
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">Profile</el-dropdown-item>
                <el-dropdown-item command="attendance"
                  >Attendance</el-dropdown-item
                >
                <el-dropdown-item divided command="logout"
                  >Logout</el-dropdown-item
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="janitor-content">
      <!-- Bottom Navigation (Mobile-first) -->
      <div class="bottom-nav">
        <div
          class="nav-item"
          :class="{ active: currentRoute === '/janitor' }"
          @click="navigateTo('/janitor')"
        >
          <el-icon size="20"><Monitor /></el-icon>
          <span>Home</span>
        </div>
        <div
          class="nav-item"
          :class="{ active: currentRoute.includes('/janitor/tasks') }"
          @click="navigateTo('/janitor/tasks')"
        >
          <el-icon size="20"><Document /></el-icon>
          <span>My Tasks</span>
        </div>
        <div
          class="nav-item"
          :class="{ active: currentRoute === '/janitor/task-wall' }"
          @click="navigateTo('/janitor/task-wall')"
        >
          <el-icon size="20"><Postcard /></el-icon>
          <span>Task Wall</span>
        </div>
        <div
          class="nav-item"
          :class="{ active: currentRoute === '/janitor/attendance' }"
          @click="navigateTo('/janitor/attendance')"
        >
          <el-icon size="20"><Clock /></el-icon>
          <span>Attendance</span>
        </div>
        <div
          class="nav-item"
          :class="{ active: currentRoute === '/janitor/announcements' }"
          @click="navigateTo('/janitor/announcements')"
        >
          <el-icon size="20"><BellFilled /></el-icon>
          <span>Announcements</span>
        </div>
        <div
          class="nav-item"
          :class="{ active: currentRoute === '/janitor/faq' }"
          @click="navigateTo('/janitor/faq')"
        >
          <el-icon size="20"><QuestionFilled /></el-icon>
          <span>FAQ</span>
        </div>
        <div
          class="nav-item"
          :class="{ active: currentRoute === '/janitor/profile' }"
          @click="navigateTo('/janitor/profile')"
        >
          <el-icon size="20"><User /></el-icon>
          <span>Profile</span>
        </div>
      </div>

      <!-- Page Content -->
      <div class="page-content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessageBox } from "element-plus";
import { AuthUtils } from "@/utils/auth";
import { API } from "@/utils/request";
import {
  Monitor,
  Document,
  Clock,
  User,
  BellFilled,
  More,
  Postcard,
  QuestionFilled,
} from "@element-plus/icons-vue";

export default {
  name: "JanitorLayout",
  components: {
    Monitor,
    Document,
    Clock,
    User,
    BellFilled,
    More,
    Postcard,
    QuestionFilled,
  },
  setup() {
    const router = useRouter();
    const route = useRoute();

    // User profile data from JWT token
    const userProfile = ref({
      fullName: "Janitor",
      avatar: "",
      avatarUrl: "",
    });

    // Initialize user profile from JWT token
    const initUserProfile = () => {
      const tokenUserInfo = AuthUtils.getUserInfo();
      if (tokenUserInfo) {
        userProfile.value.fullName = tokenUserInfo.fullName || "Janitor";
        userProfile.value.userId = tokenUserInfo.userId;
      }
    };

    // Load user avatar from API
    const loadUserAvatar = async () => {
      try {
        const tokenUserInfo = AuthUtils.getUserInfo();
        if (tokenUserInfo && tokenUserInfo.userId) {
          const response = await API.get(
            `/api/profile/${tokenUserInfo.userId}/avatar`
          );
          if (
            response.data &&
            response.data.success &&
            response.data.avatarUrl
          ) {
            userProfile.value.avatar = response.data.avatarUrl;
            userProfile.value.avatarUrl = response.data.avatarUrl;
          }
        }
      } catch (error) {
        console.log("Could not load user avatar:", error);
        // Avatar loading is optional, don't show error to user
      }
    };

    // Initialize user profile
    initUserProfile();

    // Remove unread message count - no longer needed

    const currentRoute = computed(() => route.path);

    const navigateTo = (path) => {
      router.push(path);
    };

    const handleMenuCommand = async (command) => {
      switch (command) {
        case "profile":
          router.push("/janitor/profile");
          break;
        case "attendance":
          router.push("/janitor/attendance");
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
        // User cancelled
      }
    };

    // Load avatar when component mounts
    onMounted(() => {
      loadUserAvatar();
    });

    return {
      userProfile,
      currentRoute,
      navigateTo,
      handleMenuCommand,
    };
  },
};
</script>

<style scoped>
/* Main layout */
.janitor-layout {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  display: flex;
  flex-direction: column;
}

/* Header */
.janitor-header {
  background: linear-gradient(135deg, #1e293b 0%, #334155 50%, #475569 100%);
  color: white;
  padding: 1rem 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.logo-icon {
  width: 2rem;
  height: 2rem;
  background: #06b6d4;
  border: 2px solid #22d3ee;
  box-shadow: 0 4px 12px rgba(6, 182, 212, 0.4);
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
}

.app-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0;
  color: #f8fafc;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #f8fafc;
}

.user-role {
  font-size: 0.75rem;
  color: #cbd5e1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

/* Main content */
.janitor-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* Page content */
.page-content {
  flex: 1;
  padding: 1rem 1.5rem 6rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

/* Bottom navigation */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-around;
  padding: 0.75rem 0;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  z-index: 50;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #6b7280;
  position: relative;
  min-width: 50px;
}

.nav-item:hover {
  background: #f3f4f6;
  color: #10b981;
}

.nav-item.active {
  color: #10b981;
  background: linear-gradient(
    135deg,
    rgba(16, 185, 129, 0.1),
    rgba(5, 150, 105, 0.05)
  );
}

.nav-item span {
  font-size: 0.75rem;
  font-weight: 500;
}

.nav-badge {
  position: absolute;
  top: 0.25rem;
  right: 0.75rem;
}

.nav-badge :deep(.el-badge__content) {
  font-size: 0.6rem;
  min-width: 14px;
  height: 14px;
  line-height: 14px;
}

/* Responsive design */
@media (min-width: 768px) {
  .page-content {
    padding: 1.5rem 2rem 1.5rem;
  }

  .bottom-nav {
    position: static;
    box-shadow: none;
    border-top: none;
    border-bottom: 1px solid #e5e7eb;
    order: -1;
    background: white;
    justify-content: flex-start;
    gap: 2rem;
    padding: 1rem 2rem;
  }

  .nav-item {
    flex-direction: row;
    gap: 0.5rem;
    padding: 0.75rem 1rem;
    min-width: auto;
  }

  .nav-item span {
    font-size: 0.875rem;
  }

  .janitor-content {
    flex-direction: row;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
  }

  .bottom-nav {
    width: 250px;
    flex-direction: column;
    justify-content: flex-start;
    gap: 0.5rem;
  }
}

@media (max-width: 480px) {
  .header-left {
    gap: 0.5rem;
  }

  .app-title {
    display: none;
  }

  .user-details {
    display: none;
  }

  .page-content {
    padding: 1rem 1rem 6rem;
  }
}
</style>
