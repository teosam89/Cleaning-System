<div align="center">

# 🧹 Cleaning Management System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-brightgreen.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)

**A comprehensive, enterprise-grade cleaning management system with time-based attendance tracking, real-time task management, and role-based access control.**

[Features](#-features) • [Quick Start](#-quick-start) • [Documentation](#-documentation) • [Architecture](#-architecture) • [Contributing](#-contributing)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [System Architecture](#-system-architecture)
- [Quick Start](#-quick-start)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Usage](#-usage)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [License](#-license)
- [Support](#-support)

---

## 🌟 Overview

The **Cleaning Management System** is a full-stack web application designed to streamline cleaning operations for organizations of any size. Built with modern technologies and best practices, it provides real-time task management, time-based attendance tracking, and comprehensive reporting capabilities.

### Why This System?

- **⏰ Time-Based Attendance**: Simple and efficient check-in/check-out tracking
- **⚡ Real-Time Updates**: Track task progress instantly across all devices
- **🔐 Secure Authentication**: JWT-based authentication with role-based access control
- **📊 Comprehensive Reporting**: Generate detailed performance and attendance reports
- **📱 Mobile-Friendly**: Responsive design works seamlessly on all devices
- **🔄 RESTful API**: Well-documented API for easy integration

---

## ✨ Features

### For Administrators
- 👥 **Staff Management**: Add, edit, and manage staff profiles with role assignments
- 📋 **Task Assignment**: Create and assign cleaning tasks with detailed specifications
- 📊 **Dashboard Analytics**: View real-time statistics and performance metrics
- 📢 **Announcements**: Broadcast important messages to all staff members
- 📈 **Reports**: Generate comprehensive reports on tasks, attendance, and performance
- 🗓️ **Calendar View**: Visualize tasks and schedules in an interactive calendar
- 👁️ **Task Monitoring**: Real-time tracking of task status and completion

### For Supervisors
- 👀 **Team Oversight**: Monitor assigned team members and their tasks
- ✅ **Task Verification**: Review and approve completed tasks with photo evidence
- 📊 **Team Performance**: Track team statistics and productivity metrics
- 📝 **Task Management**: Assign and reassign tasks to team members
- 🎯 **Performance Tracking**: Monitor individual and team KPIs

### For Janitors
- 📱 **Task Dashboard**: View assigned tasks with priorities and deadlines
- ✓ **Task Completion**: Mark tasks complete with photo uploads
- 📸 **Photo Documentation**: Upload before/after photos for task verification
- ⏰ **Attendance System**: Simple time-based check-in/check-out functionality
- 📜 **Attendance History**: View personal attendance records
- 📢 **View Announcements**: Stay updated with organizational news
- ❓ **FAQ Access**: Quick access to common questions and procedures

### Core Features
- 🔐 **JWT Authentication**: Secure token-based authentication system
- ⏰ **Time Tracking**: Simple and reliable attendance time recording
- 🖼️ **Image Management**: Upload and manage task completion photos
- 🔄 **Real-Time Sync**: Instant updates across all connected devices
- 📱 **Responsive Design**: Works on desktop, tablet, and mobile
- 🎨 **Modern UI/UX**: Clean, intuitive interface built with Vuetify
- 🔍 **Advanced Filtering**: Search and filter tasks by multiple criteria
- 📅 **Weekly Routines**: Set up recurring weekly cleaning schedules

---

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Security**: Spring Security + JWT
- **Database**: MySQL 8.0+
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **API**: RESTful architecture

### Frontend
- **Framework**: Vue.js 3.x
- **UI Library**: Vuetify 3.x
- **State Management**: Vue Router
- **HTTP Client**: Axios
- **Build Tool**: Vue CLI / Webpack
- **Maps**: Geolocation API

### DevOps & Tools
- **Version Control**: Git
- **Database Migration**: SQL Scripts
- **API Testing**: Postman (recommended)
- **Code Quality**: ESLint, Prettier

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Frontend Layer                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  Admin   │  │Supervisor│  │ Janitor  │  │  Login   │  │
│  │   View   │  │   View   │  │   View   │  │   View   │  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │
│              Vue.js + Vuetify + Vue Router                 │
└─────────────────────────────────────────────────────────────┘
                             │
                    HTTP/HTTPS (REST API)
                             │
┌─────────────────────────────────────────────────────────────┐
│                      Backend Layer (Spring Boot)            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Controllers (REST Endpoints)            │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │         Security Layer (JWT + Spring Security)       │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │                  Service Layer                       │  │
│  │  (Business Logic, Validation, GPS Verification)      │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │              Repository Layer (JPA)                  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                             │
┌─────────────────────────────────────────────────────────────┐
│                      Database Layer                         │
│                      MySQL 8.0+                             │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐  │
│  │ Users  │ │ Tasks  │ │Attend. │ │ Images │ │Announce│  │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Key Design Patterns
- **MVC Architecture**: Clear separation of concerns
- **Repository Pattern**: Data access abstraction
- **Service Layer Pattern**: Business logic encapsulation
- **DTO Pattern**: Data transfer objects for API responses
- **JWT Authentication**: Stateless authentication mechanism

---

## 🚀 Quick Start

### Prerequisites

Ensure you have the following installed:

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 17 or higher | [Download](https://www.oracle.com/java/technologies/downloads/) |
| Maven | 3.6+ | [Download](https://maven.apache.org/download.cgi) |
| Node.js | 14+ | [Download](https://nodejs.org/) |
| MySQL | 8.0+ | [Download](https://dev.mysql.com/downloads/) |
| Git | Latest | [Download](https://git-scm.com/) |

### One-Click Setup (Windows)

1. **Clone the repository**
   ```bash
   git clone https://github.com/teosam89/Cleaning-System.git
   cd Cleaning-System
   ```

2. **Set up the database**
   ```sql
   CREATE DATABASE cleaning_system;
   USE cleaning_system;
   SOURCE cleaning_system_2025_10_06_14_42_40-dump.sql;
   ```

3. **Run the application**
   ```bash
   # Simply double-click
   start-full-stack.bat
   ```

4. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8765/api

---

## 📦 Installation

### Backend Setup

```bash
# Navigate to backend directory
cd cleaning-system-backend

# Install dependencies and build
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend server will start on `http://localhost:8765`

### Frontend Setup

```bash
# Navigate to frontend directory
cd cleaning-system-frontend

# Install dependencies
npm install

# Run development server
npm run serve
```

The frontend will be available at `http://localhost:3000`

---

## ⚙️ Configuration

### Backend Configuration

Edit `cleaning-system-backend/src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/cleaning_system
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Configuration
app.jwt.secret=your-secret-key-here
app.jwt.expiration=86400000

# Server Configuration
server.port=8765

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Frontend Configuration

Edit `cleaning-system-frontend/src/utils/request.js`:

```javascript
const API_BASE_URL = 'http://localhost:8765/api';
```

---

## 💻 Usage

### Default Credentials

After database setup, use these credentials to log in:

| Role | Username | Password | Description |
|------|----------|----------|-------------|
| Admin | admin | admin123 | Full system access |
| Supervisor | supervisor1 | super123 | Team management access |
| Janitor | janitor1 | janitor123 | Task execution access |

> ⚠️ **Security Note**: Change default passwords immediately in production!

### Common Operations

#### Creating a Task (Admin)
1. Navigate to Task Management
2. Click "Create Task"
3. Fill in task details (title, description, location, priority)
4. Assign to janitor
5. Set deadline
6. Submit

#### Checking In (Janitor)
1. Navigate to Attendance page
2. Click "Check In"
3. System records current time
4. Optional: Add location notes
5. Check-in confirmed

#### Verifying Task Completion (Supervisor)
1. Navigate to Task Monitor
2. View completed tasks
3. Review uploaded photos
4. Approve or request revision

---

## 📚 API Documentation

### Authentication Endpoints

```
POST   /api/auth/login          # User login
POST   /api/auth/register       # User registration
GET    /api/auth/me             # Get current user
```

### Task Management

```
GET    /api/tasks               # Get all tasks
GET    /api/tasks/{id}          # Get task by ID
POST   /api/tasks               # Create new task
PUT    /api/tasks/{id}          # Update task
DELETE /api/tasks/{id}          # Delete task
GET    /api/tasks/filter        # Filter tasks
```

### Attendance

```
POST   /api/attendance/checkin  # Check in
POST   /api/attendance/checkout # Check out
GET    /api/attendance/status   # Get attendance status
GET    /api/attendance/history  # Get attendance history
```

For complete API documentation, see [docs/API.md](docs/API.md)

---

## 🧪 Testing

### Backend Testing

```bash
cd cleaning-system-backend
mvn test
```

### Frontend Testing

```bash
cd cleaning-system-frontend
npm run test:unit
```

---

## 🚢 Deployment

### Production Build

#### Backend
```bash
cd cleaning-system-backend
mvn clean package
java -jar target/cleaning-system-backend-1.0.0.jar
```

#### Frontend
```bash
cd cleaning-system-frontend
npm run build
# Deploy the 'dist' folder to your web server
```

### Docker Deployment (Optional)

```bash
# Coming soon
docker-compose up -d
```

For detailed deployment instructions, see [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📞 Support

### Documentation
- [Installation Guide](docs/INSTALLATION.md)
- [API Documentation](docs/API.md)
- [Architecture Guide](docs/ARCHITECTURE.md)
- [Deployment Guide](docs/DEPLOYMENT.md)

### Contact
- **Issues**: [GitHub Issues](https://github.com/teosam89/Cleaning-System/issues)
- **Email**: support@cleaningsystem.com
- **Documentation**: [Wiki](https://github.com/teosam89/Cleaning-System/wiki)

---

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Vue.js community for the reactive framework
- Vuetify for the beautiful Material Design components
- All contributors who have helped this project grow

---

## 📊 Project Status

- ✅ Core Features: Complete
- ✅ Authentication: Complete
- ✅ Time-Based Attendance: Complete
- ✅ Task Management: Complete
- 🚧 Reporting System: In Progress
- 📅 Mobile App: Planned

---

<div align="center">

**Made with ❤️ by the Cleaning System Team**

⭐ Star us on GitHub — it helps!

[Report Bug](https://github.com/teosam89/Cleaning-System/issues) • [Request Feature](https://github.com/teosam89/Cleaning-System/issues)

</div>
