# Cleaning Management System - Frontend

A modern Vue 3 single-page application for enterprise cleaning management with role-based interfaces, real-time analytics, and comprehensive task management.

## 🚀 Quick Start

### Prerequisites
- Node.js 16+ and npm
- Backend API running on http://localhost:8765

### Installation & Development

```bash
# Navigate to frontend directory
cd cleaning-system-frontend

# Install dependencies
npm install

# Start development server with hot-reload
npm run serve

# Application runs on http://localhost:3000 (or next available port)
```

### Default Test Accounts
- **Admin**: `admin` / `admin123`
- **Supervisor**: `supervisor` / `supervisor123`
- **Janitors**: `maria` / `maria123`, `john` / `john123`

## 🏗️ Technology Stack

- **Framework**: Vue 3 with Composition API
- **UI Library**: Element Plus
- **Routing**: Vue Router 4 with authentication guards
- **HTTP Client**: Axios with JWT interceptors
- **Charts**: Chart.js + vue-chartjs
- **Build Tool**: Vue CLI 5
- **State Management**: Reactive services pattern
- **Testing**: Jest + Vue Test Utils

## 📱 Role-Based Architecture

### Three-Tier Interface System
The application provides distinct interfaces optimized for each role:

#### Admin Interface
- Complete system oversight
- User management (CRUD operations)
- System-wide analytics dashboard
- Task calendar with all assignments
- Announcement management
- Performance monitoring
- Staff profile management

#### Supervisor Interface
- Professional dark slate theme
- Team management dashboard
- Staff performance analytics
- Task assignment and monitoring
- Team task calendar
- Janitor profile viewing
- Team-scoped announcements

#### Janitor Interface
- Task-focused design
- Personal dashboard with quick actions
- Task list and detail views
- Public task wall (claim open tasks)
- Attendance check-in/check-out
- Profile management with avatar upload
- Task photo upload for completion
- Announcement viewing

## 📦 Core Features

### Authentication & Security
- JWT-based authentication with role validation
- Automatic token injection via Axios interceptors
- Role-based route protection
- Session management with auto-logout
- Secure credential storage

### Task Management
- Comprehensive task lifecycle interface
- Task filtering by status, priority, assignee
- Task detail modal with actions
- Image upload for task completion
- Public task wall with claim functionality
- Task calendar view (admin/supervisor)
- Real-time status updates

### Attendance System
- Check-in/check-out interface
- Real-time attendance status display
- Automatic work hours calculation
- Attendance history with filtering
- Reactive state management across components

### Analytics Dashboard
- Chart.js integration for data visualization
- KPI cards (attendance rate, task completion, active counts)
- Pie chart for attendance distribution
- Doughnut chart for task status breakdown
- Line chart for attendance trends (6-month history)
- Bar chart for individual performance comparison
- Admin system-wide and supervisor team-focused views

### Image Management
- Dual-mode photo upload component
  - Avatar mode: Circular preview with auto-cropping
  - Task mode: Multiple photo uploads with gallery
- Real-time file validation (type, size, quantity)
- Progress indicators during upload
- Image gallery for task completion photos
- Authenticated image serving

### User Profile Management
- Extended profile viewing and editing
- Avatar upload with preview
- Performance metrics display
- Work schedule information
- Contact details management

### Additional Features
- Role-targeted announcement viewing
- Responsive design for mobile/desktop
- Loading states with skeleton screens
- Error handling with user notifications
- Date formatting (dd/MM/yyyy international standard)

## 📁 Project Structure

```
src/
├── components/                      # Shared and specialized components
│   ├── AdminLayout.vue              # Admin interface layout
│   ├── SupervisorLayout.vue         # Supervisor dark theme layout
│   ├── JanitorLayout.vue            # Janitor task-focused layout
│   ├── PhotoUpload.vue              # Dual-mode image upload
│   ├── TaskDetailDialog.vue         # Task management modal
│   ├── ViewTaskDialog.vue           # Read-only task viewer
│   └── TaskCompletionGallery.vue    # Photo gallery component
├── views/                           # Route components
│   ├── Login.vue                    # Authentication page
│   ├── AdminDash.vue                # Admin dashboard
│   ├── AdminStaffPro.vue            # User management
│   ├── TaskCalendar.vue             # Admin task calendar
│   ├── JobMonitor.vue               # Analytics dashboard
│   ├── Announcements.vue            # Admin announcements
│   ├── SupervisorDash.vue           # Team dashboard
│   ├── SupervisorStaffPro.vue       # Staff profile viewing
│   ├── SupervisorTaskCalendar.vue   # Team task calendar
│   ├── SupervisorTeamTasks.vue      # Team task management
│   ├── JanitorDash.vue              # Personal dashboard
│   └── janitor/                     # Janitor-specific views
│       ├── TaskList.vue             # Personal tasks
│       ├── TaskDetail.vue           # Task execution
│       ├── TaskWall.vue             # Public task board
│       ├── TaskPhotos.vue           # Photo viewing
│       ├── Attendance.vue           # Check-in/out interface
│       ├── AttendanceHistory.vue    # History records
│       ├── Announcements.vue        # Announcement viewing
│       └── Profile.vue              # Profile management
├── router/
│   └── index.js                     # Route configuration with guards
├── utils/                           # Centralized utilities
│   ├── auth.js                      # JWT token management
│   ├── request.js                   # Axios configuration
│   ├── attendanceService.js         # Reactive attendance state
│   ├── locationService.js           # Location utilities
│   ├── errorHandler.js              # Error handling
│   └── avatar.js                    # Avatar utilities
├── api/
│   └── profile.js                   # Profile API calls
├── App.vue                          # Root component
└── main.js                          # Application entry point
```

## 🔑 Key Features by Role

| Feature | Admin | Supervisor | Janitor |
|---------|-------|------------|---------|
| User Management | ✅ Full CRUD | ❌ | ❌ |
| View All Profiles | ✅ | ✅ Janitors only | ✅ Self only |
| Create/Assign Tasks | ✅ All users | ✅ Team only | ❌ |
| Execute Tasks | ❌ | ❌ | ✅ |
| Claim Public Tasks | ❌ | ❌ | ✅ |
| Attendance Check-in | ❌ | ❌ | ✅ |
| View Attendance Reports | ✅ All | ✅ Team | ✅ Self |
| Analytics Dashboard | ✅ System-wide | ✅ Team-focused | ❌ |
| Create Announcements | ✅ | ✅ Team | ❌ |
| View Announcements | ✅ | ✅ | ✅ |
| Upload Profile Photo | ✅ | ✅ | ✅ |
| Upload Task Photos | ❌ | ❌ | ✅ |

## 🌐 API Integration

### Backend Communication
- **Base URL**: `/api` (proxied to http://localhost:8765 in development)
- **Authentication**: JWT token in Authorization header
- **Error Handling**: Global Axios interceptors
- **CORS**: Configured with credentials support

### Key API Endpoints Used
```javascript
// Authentication
POST   /api/login
GET    /api/check-username

// Tasks
GET    /api/tasks
POST   /api/tasks
GET    /api/tasks/{id}
PUT    /api/tasks/{id}
PUT    /api/tasks/{id}/start
PUT    /api/tasks/{id}/complete
PUT    /api/tasks/{id}/cancel
GET    /api/tasks/public
POST   /api/tasks/{id}/claim

// Attendance
GET    /api/attendance/status
POST   /api/attendance/check-in
POST   /api/attendance/check-out
GET    /api/attendance/history

// Profile
GET    /api/profile/{userId}
PUT    /api/profile/{userId}
POST   /api/upload/{entityType}/{entityId}
GET    /api/files/{entityType}/{filename}

// Analytics (Admin)
GET    /api/admin/job-monitor
GET    /api/admin/job-monitor/attendance-chart
GET    /api/admin/job-monitor/task-completion-chart
GET    /api/admin/job-monitor/attendance-trend
GET    /api/admin/job-monitor/janitor-performance

// Supervisor
GET    /api/supervisor/dashboard/{id}
GET    /api/supervisor/staff/{userId}/profile
```

## ⚙️ Configuration

### Vue Configuration (`vue.config.js`)
```javascript
module.exports = {
  devServer: {
    port: process.env.PORT || 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8765',
        changeOrigin: true,
        secure: false
      }
    }
  },
  lintOnSave: false,
  productionSourceMap: false
}
```

### Environment Variables
Create `.env.local` for local configuration:
```bash
VUE_APP_API_BASE_URL=http://localhost:8765/api
VUE_APP_ENVIRONMENT=development
```

## 🛠️ Development

### Available Commands
```bash
npm run serve         # Development server with hot-reload
npm run build         # Production build
npm run lint          # ESLint with auto-fix
npm run test          # Run Jest tests
npm run test:watch    # Test watch mode
npm run test:coverage # Generate coverage report
```

### Hot-Reload Development
- Component changes reload instantly
- CSS/SCSS updates without page refresh
- Route changes apply immediately
- Vue DevTools integration for debugging

### Development Workflow
1. Start backend server: `mvn spring-boot:run`
2. Start frontend dev server: `npm run serve`
3. Login with test account
4. Changes auto-refresh in browser

## 🎨 UI/UX Design

### Layout System
- **AdminLayout**: Comprehensive sidebar with full navigation
- **SupervisorLayout**: Professional dark slate theme with gradient accents
- **JanitorLayout**: Simplified task-focused interface

### Design Principles
- Modern card-based component hierarchy
- Responsive grid system for all screen sizes
- Consistent Element Plus iconography
- Loading states with skeleton screens
- User-friendly error messages
- Intuitive navigation patterns

### Component Highlights

#### PhotoUpload Component
- Dual mode support (avatar circular / task multiple)
- Real-time file validation
- Progress indicators
- Preview before upload
- Error handling with user feedback

#### TaskDetailDialog Component
- Complete task lifecycle management
- Status transitions with validation
- Image upload integration
- Action history tracking
- Role-based action availability

#### Chart Integration
- Responsive chart sizing
- Interactive tooltips
- Legend interactions
- Real-time data updates
- Customizable color schemes

## 📊 Analytics Dashboard Details

### Chart Types and Data
1. **KPI Cards**
   - Attendance Rate (percentage)
   - Task Completion Rate (percentage)
   - Active Janitors (count)
   - Active Tasks (count)

2. **Pie Chart** - Current Month Attendance
   - Present (green)
   - Absent (red)
   - Late (orange)

3. **Doughnut Chart** - Task Status
   - Completed (green)
   - In Progress (blue)
   - Pending (orange)
   - Cancelled (red)

4. **Line Chart** - 6-Month Attendance Trend
   - Monthly attendance percentages
   - Trend line visualization

5. **Bar Chart** - Individual Janitor Performance
   - Performance score comparison
   - Color-coded bars
   - Threshold indicators

## 🔐 Security Features

### Authentication Flow
1. User submits credentials via login form
2. Backend validates and returns JWT token
3. Token stored in localStorage
4. Axios interceptor injects token in all requests
5. Router guards protect role-specific routes
6. Auto-logout on token expiration

### Route Protection
```javascript
// Route guard example
beforeEnter: (to, from, next) => {
  const token = localStorage.getItem('token');
  const role = getUserRole();

  if (!token) {
    next('/login');
  } else if (to.meta.role && to.meta.role !== role) {
    next('/unauthorized');
  } else {
    next();
  }
}
```

### Security Best Practices
- No sensitive data in local storage (except encrypted token)
- HTTPS enforcement in production
- CORS credentials properly configured
- XSS protection via Vue's template escaping
- Input validation before API calls

## 🚀 Production Deployment

### Build for Production
```bash
npm run build
```

Output directory: `dist/`

### Production Checklist
- [ ] Update API base URL for production
- [ ] Enable HTTPS
- [ ] Configure production CORS origins
- [ ] Disable source maps (`productionSourceMap: false`)
- [ ] Optimize assets and images
- [ ] Set proper cache headers
- [ ] Configure CDN for static assets

### Production Environment Variables
```bash
VUE_APP_API_BASE_URL=https://api.yourdomain.com/api
VUE_APP_ENVIRONMENT=production
```

### Deployment Options
- **Static Hosting**: Netlify, Vercel, GitHub Pages
- **Server**: Nginx, Apache
- **Container**: Docker with nginx
- **CDN**: CloudFront, Cloudflare

### Sample Nginx Configuration
```nginx
server {
    listen 80;
    server_name yourdomain.com;
    root /var/www/cleaning-frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8765;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 🧪 Testing

### Unit Tests
```bash
# Run all tests
npm run test

# Watch mode
npm run test:watch

# Coverage report
npm run test:coverage
```

### Test Structure
- Component tests in `*.spec.js` files
- Mock API responses for isolation
- Test utilities and helpers
- Coverage targets for critical paths

### Testing Tools
- **Jest**: Test runner
- **Vue Test Utils**: Component testing
- **Jest Environment JSDOM**: DOM simulation

## 🐛 Troubleshooting

### Common Issues

**Development Server Won't Start**
```
Solution: Check if port 3000 is available or change port in vue.config.js
Alternative: Set PORT environment variable
```

**API Calls Failing**
```
Solution: Ensure backend is running on port 8765
Check proxy configuration in vue.config.js
Verify CORS settings on backend
```

**Login Fails**
```
Solution: Verify backend is running and database is populated
Check console for detailed error messages
Ensure test accounts exist in database
```

**Images Not Displaying**
```
Solution: Verify upload directory exists on backend
Check JWT token is valid and not expired
Ensure file paths are correct
```

**Charts Not Rendering**
```
Solution: Check Chart.js data format
Verify API responses match expected structure
Ensure canvas elements have proper sizing
```

## 📝 Development Best Practices

### Code Standards
- Use Vue 3 Composition API for new components
- Follow PascalCase for component names
- Use camelCase for JavaScript variables/functions
- Implement proper prop validation
- Add comments for complex logic

### Component Guidelines
- Keep components focused and single-purpose
- Use props for parent-child communication
- Emit events for child-parent communication
- Leverage reactive services for cross-component state
- Implement loading and error states

### Performance Optimization
- Lazy load routes for code splitting
- Use v-if for conditional rendering (not v-show)
- Optimize images before upload
- Debounce search inputs
- Implement virtual scrolling for large lists

## 📚 Additional Resources

- [Vue 3 Documentation](https://vuejs.org/)
- [Element Plus Guide](https://element-plus.org/)
- [Vue Router Documentation](https://router.vuejs.org/)
- [Chart.js Documentation](https://www.chartjs.org/)
- [Axios Documentation](https://axios-http.com/)

## 🔄 Recent Updates

### Latest Features
- Performance analytics integration with backend PerformanceService
- TaskCompletionGallery component for photo viewing
- Enhanced supervisor dashboard with team metrics
- Improved date formatting (dd/MM/yyyy standard)
- Reactive attendance service for cross-component sync
- Enhanced error handling with user-friendly messages

### Improvements
- Optimized component modularity
- Enhanced role-based navigation
- Improved chart responsiveness
- Better loading state management
- Streamlined API error handling

## 📄 License

This project is proprietary software for enterprise cleaning management.

---

**Built with Vue 3 | Element Plus | Chart.js | Axios**
