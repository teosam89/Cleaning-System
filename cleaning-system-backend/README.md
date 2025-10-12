# Cleaning Management System - Backend

A production-ready Spring Boot REST API for enterprise cleaning management featuring role-based access control, performance analytics, and comprehensive task management.

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Apache Maven 3.9+
- MySQL 8.0 (running on localhost:3306)

### Installation & Development

```bash
# Navigate to backend directory
cd cleaning-system-backend

# Compile project
mvn clean compile

# Start development server with hot-reload
mvn spring-boot:run

# Server runs on http://localhost:8765
```

For Windows with custom Maven installation:
```bash
"C:\Program Files\apache-maven-3.9.11\bin\mvn" spring-boot:run
```

### Default Test Accounts
- **Admin**: `admin` / `admin123`
- **Supervisor**: `supervisor` / `supervisor123`
- **Janitors**: `maria` / `maria123`, `john` / `john123`

## 🏗️ Technology Stack

- **Framework**: Spring Boot 3.3.2
- **Language**: Java 17
- **Database**: MySQL 8.0 with Spring Data JPA/Hibernate
- **Security**: Spring Security + JWT Authentication
- **Build Tool**: Apache Maven 3.9.11
- **Password Encryption**: BCrypt
- **File Upload**: Multipart with organized filesystem storage

## 📦 Core Features

### Multi-Role Access Control
- **Admin**: Complete system management, analytics dashboard, user administration
- **Supervisor**: Team management, task assignment, performance monitoring
- **Janitor**: Task execution, attendance tracking, profile management

### Authentication & Security
- JWT-based authentication with role validation
- BCrypt password encryption
- CORS configuration with credential support
- Role-based endpoint protection
- Token auto-renewal and expiration handling

### Task Management
- Comprehensive task lifecycle (create, assign, start, complete, cancel)
- Public task wall for open assignments
- Image attachments for task completion verification
- Priority levels and due date tracking
- Bulk operations and filtering

### Attendance System
- Check-in/check-out functionality
- Automatic work hours calculation
- Attendance history and reporting
- Date-based filtering and analytics

### Performance Analytics
- Weighted performance calculation (40% attendance + 60% task completion)
- Monthly performance reports
- Team and individual metrics
- Chart-ready API endpoints for visualization

### Image Management
- Centralized image storage with metadata tracking
- Organized file structure: `uploads/{entity_type}/{year}/{month}/`
- Unique filename generation with timestamps
- Support for profile avatars, task photos, and announcements
- Authenticated file serving

### Additional Features
- Role-targeted announcement system
- Week routine automation for recurring tasks
- Extended user profiles with 40+ fields
- CSV report generation
- Global exception handling with detailed error responses

## 📁 Project Architecture

```
src/main/java/com/cleaningsystem/backend/
├── CleaningSystemBackendApplication.java    # Main application entry point
├── config/
│   ├── SecurityConfig.java                  # JWT & CORS security configuration
│   └── DataInitializer.java                 # Database seed data
├── controller/                              # REST API endpoints
│   ├── AdminController.java                 # Admin-only endpoints
│   ├── SupervisorController.java            # Team management endpoints
│   ├── JanitorController.java               # Janitor operations
│   ├── AuthController.java                  # Authentication endpoints
│   ├── TaskController.java                  # Task CRUD operations
│   ├── AttendanceController.java            # Attendance management
│   ├── ProfileController.java               # User profile management
│   ├── AnnouncementController.java          # System announcements
│   ├── ImageController.java                 # File upload/serving
│   └── WeekRoutineController.java           # Automated task scheduling
├── dto/                                     # Data Transfer Objects
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── CreateTaskRequest.java
│   ├── AttendanceStatusResponse.java
│   └── [Additional DTOs...]
├── entity/                                  # JPA Entities
│   ├── User.java                            # User entity with role support
│   ├── UserProfile.java                     # Extended profile data
│   ├── Task.java                            # Task entity
│   ├── Attendance.java                      # Attendance records
│   ├── Image.java                           # Image metadata
│   ├── Announcement.java                    # System announcements
│   └── WeekRoutine.java                     # Recurring task templates
├── repository/                              # Data Access Layer
│   ├── UserRepository.java
│   ├── TaskRepository.java
│   ├── AttendanceRepository.java
│   └── [Additional repositories...]
├── security/                                # Security infrastructure
│   ├── JwtAuthenticationFilter.java         # JWT token validation filter
│   └── JwtAuthenticationEntryPoint.java     # Authentication error handler
├── service/                                 # Business Logic Layer
│   ├── AuthService.java                     # Authentication service
│   ├── TaskService.java                     # Task management logic
│   ├── AttendanceService.java               # Attendance calculations
│   ├── PerformanceService.java              # Performance analytics
│   ├── ProfileService.java                  # Profile management
│   ├── ImageService.java                    # File upload/management
│   ├── AnnouncementService.java             # Announcement operations
│   ├── ReportService.java                   # CSV report generation
│   └── WeekRoutineService.java              # Automated scheduling
├── utils/                                   # Utility classes
│   ├── JwtTokenProvider.java                # JWT token operations
│   └── DateTimeUtils.java                   # Date/time utilities
├── exception/                               # Exception handling
│   ├── GlobalExceptionHandler.java          # Global error handler
│   ├── BusinessException.java               # Business logic exceptions
│   └── ResourceNotFoundException.java       # 404 exceptions
└── resources/
    └── application.properties               # Application configuration
```

## 🔑 Key API Endpoints

### Authentication
```
POST   /api/login                           # User authentication
GET    /api/check-username                  # Username availability check
```

### Tasks
```
GET    /api/tasks                           # Get filtered tasks
POST   /api/tasks                           # Create new task
GET    /api/tasks/{id}                      # Get task details
PUT    /api/tasks/{id}                      # Update task
DELETE /api/tasks/{id}                      # Delete task
PUT    /api/tasks/{id}/start                # Start task
PUT    /api/tasks/{id}/complete             # Complete task
PUT    /api/tasks/{id}/cancel               # Cancel task
GET    /api/tasks/public                    # Public task wall
POST   /api/tasks/{id}/claim                # Claim public task
```

### Attendance
```
GET    /api/attendance/status               # Current attendance status
POST   /api/attendance/check-in             # Check in
POST   /api/attendance/check-out            # Check out
GET    /api/attendance/history              # Attendance history
```

### Admin Analytics
```
GET    /api/admin/job-monitor                           # Dashboard KPIs
GET    /api/admin/job-monitor/attendance-chart          # Attendance distribution
GET    /api/admin/job-monitor/task-completion-chart     # Task status breakdown
GET    /api/admin/job-monitor/attendance-trend          # Historical attendance
GET    /api/admin/job-monitor/janitor-performance       # Individual performance
```

### Profile Management
```
GET    /api/profile/{userId}                # Get user profile
PUT    /api/profile/{userId}                # Update profile
POST   /api/upload/{entityType}/{entityId}  # Upload images
GET    /api/files/{entityType}/{filename}   # Serve files
```

### Supervisor Operations
```
GET    /api/supervisor/dashboard/{id}                   # Team dashboard
GET    /api/supervisor/staff/{userId}/profile           # Staff profile details
GET    /api/supervisor/team/{supervisorId}/tasks        # Team task list
```

## ⚙️ Configuration

### Database Configuration (`application.properties`)
```properties
# Server
server.port=8765

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/cleaning_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=admin
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT
jwt.secret=CleaningSystemJwtSecretKey2024ForProductionUseChangeThis32CharsMinimum
jwt.expiration=86400000
jwt.issuer=cleaning-system-backend

# File Upload
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
app.upload.dir=uploads/

# Work Hours
work.hours.standard=8.0
```

### Database Schema
The application uses Hibernate DDL auto-update to manage schema. On first run, it creates:
- `users` - User accounts with roles
- `user_profiles` - Extended profile information
- `tasks` - Task records with assignments
- `attendances` - Attendance check-in/out records
- `images` - Image metadata with entity relationships
- `announcements` - System announcements
- `week_routines` - Recurring task templates

## 🔒 Security Features

### JWT Authentication
- Token-based authentication with configurable expiration
- Role claims embedded in JWT payload
- Automatic token validation on protected endpoints
- Secure password storage with BCrypt

### Role-Based Access Control
```java
// Example controller security
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/analytics")
public ResponseEntity<?> getAnalytics() { ... }

@PreAuthorize("hasRole('SUPERVISOR')")
@GetMapping("/supervisor/team")
public ResponseEntity<?> getTeam() { ... }
```

### CORS Configuration
- Configured for frontend integration
- Credential support enabled
- Customizable allowed origins and methods

## 📊 Performance Analytics System

The `PerformanceService` provides comprehensive analytics:

### Calculation Algorithm
```
Performance Score = (Attendance Rate × 40%) + (Task Completion Rate × 60%)
```

### Features
- Monthly attendance percentage calculation (working days only)
- Task completion rate tracking
- Weighted performance scoring
- Bulk performance analysis for teams
- Historical trend analysis

### Working Days Logic
- Monday-Friday considered working days
- Weekends automatically excluded
- Customizable work hour standards

## 🛠️ Development

### Maven Commands
```bash
mvn clean compile              # Compile project
mvn spring-boot:run           # Run development server
mvn test                      # Run tests
mvn package                   # Build production JAR
mvn clean install             # Full build with dependencies
```

### Hot-Reload Development
Spring Boot DevTools is included for automatic reloading:
- Java class changes trigger automatic restart
- Configuration updates apply immediately
- Database schema updates via Hibernate

### Testing
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test -Dmaven.test.coverage=true
```

## 🚀 Production Deployment

### Build Production JAR
```bash
mvn clean package -DskipTests
```

Output: `target/cleaning-system-backend-1.0.0.jar`

### Run Production Server
```bash
java -jar target/cleaning-system-backend-1.0.0.jar
```

### Production Configuration Recommendations
```properties
# Change DDL auto to validate (prevent schema changes)
spring.jpa.hibernate.ddl-auto=validate

# Disable SQL logging
spring.jpa.show-sql=false

# Use strong JWT secret (256-bit minimum)
jwt.secret=your-production-secret-key-here

# Reduce token expiration for security
jwt.expiration=3600000  # 1 hour

# Configure production database
spring.datasource.url=jdbc:mysql://production-host:3306/cleaning_system_prod

# Enable production CORS settings
spring.web.cors.allowed-origins=https://yourdomain.com
```

## 📈 Performance Optimization

### Database Indexing
Strategic indexes applied for performance:
```java
@Table(indexes = {
    @Index(name = "idx_task_status", columnList = "status"),
    @Index(name = "idx_task_assigned_to", columnList = "assignedTo"),
    @Index(name = "idx_attendance_user_date", columnList = "janitorId,workDate")
})
```

### Query Optimization
- Custom repository queries for complex operations
- Pagination support for large datasets
- Lazy loading for entity relationships
- Efficient bulk operations

## 🧪 API Testing

### Using cURL
```bash
# Login
curl -X POST http://localhost:8765/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Get tasks (with JWT token)
curl -X GET http://localhost:8765/api/tasks \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Create task
curl -X POST http://localhost:8765/api/tasks \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean office","description":"Weekly cleaning","priority":"HIGH"}'
```

### Testing Tools
- Postman/Insomnia for API testing
- Swagger UI (can be added via springdoc-openapi)
- JUnit for unit tests
- Testcontainers for integration tests

## 🐛 Troubleshooting

### Common Issues

**Database Connection Error**
```
Solution: Ensure MySQL is running and database 'cleaning_system' exists
MySQL command: CREATE DATABASE cleaning_system;
```

**Port Already in Use**
```
Solution: Change port in application.properties
server.port=8766
```

**JWT Token Expired**
```
Solution: Login again to get new token or increase expiration time
jwt.expiration=86400000  # 24 hours in milliseconds
```

**File Upload Fails**
```
Solution: Check upload directory exists and has write permissions
Default: uploads/ in project root
```

## 📝 Development Best Practices

### Code Structure
- Follow Controller-Service-Repository pattern
- Use DTOs for API communication
- Implement proper exception handling
- Add validation annotations to entities
- Document complex business logic

### Security
- Never commit sensitive credentials
- Use environment variables for secrets
- Keep dependencies updated
- Validate all user inputs
- Implement rate limiting for production

### Performance
- Use database indexing strategically
- Implement caching where appropriate
- Optimize N+1 query problems
- Monitor database query performance
- Use pagination for large datasets

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Reference](https://spring.io/projects/spring-security)
- [Hibernate ORM Guide](https://hibernate.org/orm/documentation/)
- [JWT Introduction](https://jwt.io/introduction)

**Built with Spring Boot 3.3.2 | Java 17 | MySQL 8.0**
