<template>
  <div class="login-container">
    <!-- Background decorative elements -->
    <div class="background-decorations">
      <div class="decoration decoration-1"></div>
      <div class="decoration decoration-2"></div>
    </div>

    <div class="login-wrapper">
      <el-card class="login-card" shadow="always">
        <!-- Left brand panel -->
        <div class="brand-panel">
          <!-- Background pattern -->
          <div class="brand-bg-pattern">
            <div class="pattern-circle pattern-1"></div>
            <div class="pattern-circle pattern-2"></div>
            <div class="pattern-circle pattern-3"></div>
          </div>

          <div class="brand-content">
            <!-- Logo and title -->
            <div class="brand-header">
              <div class="logo-section">
                <div class="logo-icon">🧹</div>
                <h1 class="brand-name">CleanMS</h1>
              </div>

              <h2 class="brand-title">
                Cleaning<br />
                Management System
              </h2>

              <p class="brand-description">
                Streamline your facility cleaning operations<br />
                with our comprehensive management platform.
              </p>
            </div>

            <!-- Features list -->
            <div class="features-list">
              <div class="feature-item">
                <div class="feature-icon feature-icon-orange">
                  <el-icon><Document /></el-icon>
                </div>
                <h3 class="feature-title">Task Management</h3>
              </div>

              <div class="feature-item">
                <div class="feature-icon feature-icon-green">
                  <el-icon><TrendCharts /></el-icon>
                </div>
                <h3 class="feature-title">Performance Analytics</h3>
              </div>

              <div class="feature-item">
                <div class="feature-icon feature-icon-purple">
                  <el-icon><UserFilled /></el-icon>
                </div>
                <h3 class="feature-title">Team Coordination</h3>
              </div>
            </div>
          </div>
        </div>

        <!-- Right login form panel -->
        <div class="form-panel">
          <div class="form-container">
            <div class="form-header">
              <h2 class="form-title">Welcome Back!</h2>
              <p class="form-subtitle">Please sign in to your account</p>
            </div>

            <el-form
              :model="loginForm"
              :rules="rules"
              ref="loginFormRef"
              class="login-form"
              @submit.prevent="handleLogin"
            >
              <!-- Username input -->
              <el-form-item prop="username">
                <el-input
                  v-model="loginForm.username"
                  placeholder="Enter your username"
                  size="large"
                  prefix-icon="User"
                />
              </el-form-item>

              <!-- Password input -->
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="Enter your password"
                  size="large"
                  prefix-icon="Lock"
                  show-password
                />
              </el-form-item>

              <!-- Remember me and forgot password -->
              <div class="form-options">
                <el-checkbox v-model="loginForm.rememberMe">
                  Remember me
                </el-checkbox>
                <el-button type="primary" link @click="handleForgotPassword">
                  Forgot Password?
                </el-button>
              </div>

              <!-- Login button -->
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  class="login-button"
                  @click="handleLogin"
                  :loading="loading"
                >
                  Sign In
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from "vue";
import { Document, TrendCharts, UserFilled } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { API } from "../utils/request";
import { AuthUtils } from "../utils/auth";

export default {
  name: "LoginComponent",
  components: {
    Document,
    TrendCharts,
    UserFilled,
  },
  setup() {
    // Router for navigation
    const router = useRouter();

    // Reactive data
    const loginFormRef = ref();
    const loading = ref(false);

    const loginForm = reactive({
      username: "",
      password: "",
      rememberMe: false,
    });

    const rules = reactive({
      username: [
        { required: true, message: "Please enter username", trigger: "blur" },
      ],
      password: [
        { required: true, message: "Please enter password", trigger: "blur" },
      ],
    });

    // Methods
    const handleLogin = async () => {
      if (!loginFormRef.value) return;

      await loginFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true;

          try {
            // Call backend login API using JWT authentication
            await API.login({
              username: loginForm.username,
              password: loginForm.password,
            });

            // JWT token and user info are now stored by API.login()
            // Get user role from stored JWT token
            const userRole = AuthUtils.getUserRole();
            const userInfo = AuthUtils.getUserInfo();

            // Show success message with user info
            const welcomeMessage = userInfo?.fullName
              ? `Login successful! Welcome back, ${userInfo.fullName}`
              : `Login successful! Welcome back, ${loginForm.username}`;

            ElMessage.success(welcomeMessage);

            // Navigate based on user role from JWT token (more secure than frontend selection)
            if (userRole === "admin") {
              router.push("/admin");
            } else if (userRole === "supervisor") {
              router.push("/supervisor");
            } else if (userRole === "janitor" || userRole === "cleaner") {
              router.push("/janitor");
            } else {
              ElMessage.error("Unknown user role: " + userRole);
              AuthUtils.removeToken();
              return;
            }
          } catch (error) {
            console.error("Login error:", error);

            // Clear any partial authentication data on error
            AuthUtils.removeToken();

            if (error.response) {
              const errorMessage =
                error.response.data?.message ||
                "Login failed, please check username and password";
              ElMessage.error(errorMessage);
            } else if (error.request) {
              ElMessage.error(
                "Unable to connect to server, please check network connection"
              );
            } else {
              ElMessage.error("An error occurred during login");
            }
          } finally {
            loading.value = false;
          }
        } else {
          ElMessage.error("Please fill in all required fields");
          return false;
        }
      });
    };

    const handleForgotPassword = () => {
      ElMessage.info("Redirecting to password reset...");
    };

    return {
      loginFormRef,
      loginForm,
      rules,
      loading,
      handleLogin,
      handleForgotPassword,
    };
  },
};
</script>

<style scoped>
/* Main container */
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #374151 0%, #1f2937 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  position: relative;
  overflow: hidden;
}

/* Background decorations */
.background-decorations {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.decoration {
  position: absolute;
  width: 20rem;
  height: 20rem;
  border-radius: 50%;
  opacity: 0.15;
  filter: blur(3rem);
}

.decoration-1 {
  background: #0ea5e9;
  top: -10rem;
  right: -10rem;
}

.decoration-2 {
  background: #34d399;
  bottom: -10rem;
  left: -10rem;
}

/* Login wrapper */
.login-wrapper {
  width: 100%;
  max-width: 770px;
  position: relative;
  z-index: 10;
}

/* Login card */
.login-card {
  border-radius: 1.5rem !important;
  overflow: hidden;
  display: flex;
  min-height: 550px;
  border: none !important;
}

.login-card :deep(.el-card__body) {
  padding: 0;
  display: flex;
  min-height: 550px;
}

/* Left brand panel */
.brand-panel {
  flex: 0.8;
  background: linear-gradient(135deg, #1f2937 0%, #111827 100%);
  padding: 3.5rem;
  color: white;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
}

/* Brand background pattern */
.brand-bg-pattern {
  position: absolute;
  inset: 0;
  opacity: 0.1;
}

.pattern-circle {
  position: absolute;
  border: 2px solid white;
  border-radius: 50%;
}

.pattern-1 {
  width: 8rem;
  height: 8rem;
  top: 2rem;
  left: 2rem;
}

.pattern-2 {
  width: 6rem;
  height: 6rem;
  bottom: 3rem;
  right: 2rem;
}

.pattern-3 {
  width: 4rem;
  height: 4rem;
  top: 50%;
  left: 30%;
}

/* Brand content */
.brand-content {
  position: relative;
  z-index: 10;
  width: 100%;
}

/* Logo section */
.brand-header {
  margin-bottom: 2.5rem;
}

.logo-section {
  display: flex;
  align-items: center;
  margin-bottom: 1.25rem;
}

.logo-icon {
  width: 2.5rem;
  height: 2.5rem;
  background: #06b6d4;
  border: 2px solid #22d3ee;
  box-shadow: 0 4px 12px rgba(6, 182, 212, 0.4);
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 0.75rem;
  font-size: 1.25rem;
}

.brand-name {
  font-size: 1.875rem;
  font-weight: bold;
  margin: 0;
}

.brand-title {
  font-size: 2rem;
  font-weight: bold;
  margin: 0 0 0.75rem 0;
  line-height: 1.2;
}

.brand-description {
  color: #d1d5db;
  font-size: 0.95rem;
  margin: 0;
  line-height: 1.5;
}

/* Features list */
.features-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.feature-item {
  display: flex;
  align-items: center;
}

.feature-icon {
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 0.875rem;
  font-size: 1.125rem;
}

.feature-icon-orange {
  background: rgba(249, 115, 22, 0.2);
}

.feature-icon-green {
  background: rgba(34, 197, 94, 0.2);
}

.feature-icon-purple {
  background: rgba(168, 85, 247, 0.2);
}

.feature-title {
  font-size: 0.95rem;
  font-weight: 500;
  margin: 0;
}

/* Right form panel */
.form-panel {
  flex: 1.2;
  padding: 2rem 2.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-container {
  width: 100%;
  max-width: 420px;
}

/* Form header */
.form-header {
  text-align: center;
  margin-bottom: 1.5rem;
}

.form-title {
  font-size: 1.75rem;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 0.375rem 0;
}

.form-subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 0.875rem;
}

/* Login form */
.login-form {
  margin-bottom: 2rem;
}

.login-form :deep(.el-input__inner) {
  padding-left: 2.75rem;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 1.25rem;
}

.login-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.login-form :deep(.el-input__prefix) {
  left: 0.75rem;
}

/* Form options */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
}

/* Login button */
.login-button {
  width: 100%;
  background: #06b6d4 !important;
  border-color: #06b6d4 !important;
}

.login-button:hover {
  background: #0891b2 !important;
  border-color: #0891b2 !important;
}

/* Responsive design */
@media (max-width: 768px) {
  .login-card :deep(.el-card__body) {
    flex-direction: column;
    min-height: auto;
  }

  .brand-panel {
    padding: 2rem;
    min-height: 280px;
  }

  .form-panel {
    padding: 1.5rem;
  }

  .brand-title {
    font-size: 1.75rem;
  }

  .features-list {
    flex-direction: row;
    justify-content: space-around;
    flex-wrap: wrap;
    gap: 0.75rem;
  }

  .feature-item {
    flex-direction: column;
    align-items: center;
    text-align: center;
    flex: 0 0 30%;
  }

  .feature-icon {
    margin-right: 0;
    margin-bottom: 0.375rem;
  }

  .feature-title {
    font-size: 0.8rem;
  }
}
</style>
