import { createRouter, createWebHashHistory } from "vue-router";
import { AuthUtils } from "@/utils/auth";
import { ElMessage } from "element-plus";
import Login from "../views/Login.vue";
import AdminDash from "../views/AdminDash.vue";
import AdminStaffPro from "../views/AdminStaffPro.vue";
import TaskCalendar from "@/views/TaskCalendar.vue";
import Announcements from "@/views/Announcements.vue";
import JobMonitor from "@/views/JobMonitor.vue";
import JanitorDash from "../views/JanitorDash.vue";

// Supervisor pages
import SupervisorDash from "@/views/SupervisorDash.vue";
import SupervisorStaffPro from "@/views/SupervisorStaffPro.vue";
import SupervisorTaskCalendar from "@/views/SupervisorTaskCalendar.vue";
import SupervisorTeamTasks from "@/views/SupervisorTeamTasks.vue";

// Janitor pages
import TaskList from "@/views/janitor/TaskList.vue";
import TaskDetail from "@/views/janitor/TaskDetail.vue";
import TaskPhotos from "@/views/janitor/TaskPhotos.vue";
import TaskWall from "@/views/janitor/TaskWall.vue";
import Attendance from "@/views/janitor/Attendance.vue";
import AttendanceHistory from "@/views/janitor/AttendanceHistory.vue";
import JanitorAnnouncements from "@/views/janitor/Announcements.vue";
import Profile from "@/views/janitor/Profile.vue";
import FAQ from "@/views/janitor/FAQ.vue";

const routes = [
  {
    path: "/",
    name: "Login",
    component: Login,
    meta: { requiresAuth: false },
  },
  {
    path: "/admin",
    name: "AdminDash",
    component: AdminDash,
    meta: { requiresAuth: true, role: "admin" },
  },
  {
    path: "/admin/staff",
    name: "AdminStaffPro",
    component: AdminStaffPro,
    meta: { requiresAuth: true, role: "admin" },
  },
  {
    path: "/admin/calendar",
    name: "TaskCalendar",
    component: TaskCalendar,
    meta: { requiresAuth: true, role: "admin" },
  },
  {
    path: "/admin/announcements",
    name: "Announcements",
    component: Announcements,
    meta: { requiresAuth: true, role: "admin" },
  },
  {
    path: "/admin/monitor",
    name: "JobMonitor",
    component: JobMonitor,
    meta: { requiresAuth: true, role: "admin" },
  },
  // Supervisor routes
  {
    path: "/supervisor",
    name: "SupervisorDash",
    component: SupervisorDash,
    meta: { requiresAuth: true, role: "supervisor" },
  },
  {
    path: "/supervisor/staff",
    name: "SupervisorStaffPro",
    component: SupervisorStaffPro,
    meta: { requiresAuth: true, role: "supervisor" },
  },
  {
    path: "/supervisor/calendar",
    name: "SupervisorTaskCalendar",
    component: SupervisorTaskCalendar,
    meta: { requiresAuth: true, role: "supervisor" },
  },
  {
    path: "/supervisor/team-tasks",
    name: "SupervisorTeamTasks",
    component: SupervisorTeamTasks,
    meta: { requiresAuth: true, role: "supervisor" },
  },
  {
    path: "/janitor",
    name: "JanitorDash",
    component: JanitorDash,
    meta: { requiresAuth: true, role: "janitor" },
  },
  {
    path: "/janitor/tasks",
    name: "TaskList",
    component: TaskList,
    meta: { requiresAuth: true, role: "janitor" },
  },
  {
    path: "/janitor/tasks/:id",
    name: "TaskDetail",
    component: TaskDetail,
    meta: { requiresAuth: true, role: "janitor" },
  },
  {
    path: "/janitor/tasks/:taskId/photos",
    name: "TaskPhotos",
    component: TaskPhotos,
    meta: { requiresAuth: true, role: "janitor" },
  },
  {
    path: "/janitor/task-wall",
    name: "TaskWall",
    component: TaskWall,
    meta: { requiresAuth: true, role: "janitor" },
  },
  {
    path: "/janitor/attendance",
    name: "Attendance",
    component: Attendance,
    meta: { requiresAuth: true, role: "janitor" },
  },
  {
    path: "/janitor/attendance/history",
    name: "AttendanceHistory",
    component: AttendanceHistory,
    meta: { requiresAuth: true, role: "janitor" },
  },
  {
    path: "/janitor/announcements",
    name: "JanitorAnnouncements",
    component: JanitorAnnouncements,
    meta: { requiresAuth: true, role: "janitor" },
  },
  {
    path: "/janitor/profile",
    name: "Profile",
    component: Profile,
    meta: { requiresAuth: true, role: "janitor" },
  },
  {
    path: "/janitor/faq",
    name: "FAQ",
    component: FAQ,
    meta: { requiresAuth: true, role: "janitor" },
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

/**
 * Global Navigation Guard
 * Protects routes based on JWT authentication and role permissions
 */
router.beforeEach((to, from, next) => {
  // Check if route requires authentication
  if (to.meta.requiresAuth) {
    // Verify JWT authentication
    if (!AuthUtils.isAuthenticated()) {
      console.warn("🚫 Access denied: No valid JWT token");
      // Only show login message if not already on login page
      if (from.path !== "/") {
        ElMessage.error("Please login to access this page");
      }
      next("/");
      return;
    }

    // Check role-based permissions
    if (to.meta.role) {
      const userRole = AuthUtils.getUserRole();

      if (to.meta.role === "admin" && !AuthUtils.isAdmin()) {
        console.warn(
          `🚫 Access denied: Admin role required, user role: ${userRole}`
        );
        ElMessage.error("Admin access required");
        next("/");
        return;
      }

      if (to.meta.role === "supervisor" && !AuthUtils.isSupervisor()) {
        console.warn(
          `🚫 Access denied: Supervisor role required, user role: ${userRole}`
        );
        ElMessage.error("Supervisor access required");
        next("/");
        return;
      }

      if (to.meta.role === "janitor" && !AuthUtils.isJanitor()) {
        console.warn(
          `🚫 Access denied: Janitor role required, user role: ${userRole}`
        );
        ElMessage.error("Janitor access required");
        next("/");
        return;
      }
    }
  }

  // If user is already authenticated and tries to access login page, redirect to dashboard
  if (to.path === "/" && AuthUtils.isAuthenticated()) {
    const userRole = AuthUtils.getUserRole();
    if (userRole === "admin") {
      console.log("🔄 Authenticated admin user redirected to admin dashboard");
      next("/admin");
      return;
    } else if (userRole === "supervisor") {
      console.log(
        "🔄 Authenticated supervisor user redirected to supervisor dashboard"
      );
      next("/supervisor");
      return;
    } else if (["janitor", "cleaner"].includes(userRole)) {
      console.log(
        "🔄 Authenticated janitor user redirected to janitor dashboard"
      );
      next("/janitor");
      return;
    }
  }

  console.log(`✅ Navigation allowed: ${to.path}`);
  next();
});

export default router;
