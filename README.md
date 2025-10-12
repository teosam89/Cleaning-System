# Cleaning Management System

A full-stack web application for managing cleaning tasks and staff, built with Spring Boot (backend) and Vue.js (frontend).

## Project Structure

```
MiniProject/
├── cleaning-system-backend/     # Spring Boot backend application
├── cleaning-system-frontend/    # Vue.js frontend application
├── cleaning_system_2025_10_06_14_42_40-dump.sql  # Database dump file
├── start-full-stack.bat         # One-click startup script
└── README.md                    # This file
```

## Prerequisites

Before running this application, ensure you have the following installed:

- **Java Development Kit (JDK) 17 or higher**
- **Maven 3.6+**
- **Node.js 14+ and npm**
- **MySQL 8.0+**

## Database Setup

1. Create a new MySQL database named `cleaning_system`:
   ```sql
   CREATE DATABASE cleaning_system;
   ```

2. Import the provided database dump:
   ```bash
   mysql -u root -p cleaning_system < cleaning_system_2025_10_06_14_42_40-dump.sql
   ```

3. Update database credentials in `cleaning-system-backend/src/main/resources/application.properties` if needed:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

## Installation

### Backend Setup
```bash
cd cleaning-system-backend
mvn clean install
```

### Frontend Setup
```bash
cd cleaning-system-frontend
npm install
```

## Running the Application

### Option 1: One-Click Startup (Recommended)

Simply double-click the `start-full-stack.bat` file. This will:
1. Start the Spring Boot backend server
2. Wait 30 seconds for backend initialization
3. Start the Vue.js frontend development server
4. Open both servers in separate command windows

### Option 2: Manual Startup

**Terminal 1 - Backend:**
```bash
cd cleaning-system-backend
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd cleaning-system-frontend
npm run serve
```

## Access the Application

- **Frontend:** http://localhost:3000 (or next available port like :3001)
- **Backend API:** http://localhost:8765/api

## Default Login Credentials

The database includes pre-configured user roles:
- **Admin Account:** Check the database dump for admin credentials
- **Janitor Account:** Check the database dump for janitor credentials

## Features

- User authentication with role-based access (Admin/Janitor)
- Task management and assignment
- Staff management
- Real-time task status updates

## Troubleshooting

- **Port conflicts:** If port 8765 or 3000 is already in use, the frontend will automatically try the next available port
- **Database connection errors:** Verify MySQL is running and credentials are correct
- **Build errors:** Ensure all prerequisites are installed with correct versions

## Notes

- The database will auto-initialize with correct roles (admin, janitor)
- Login authentication has been configured and tested
- Both frontend and backend must be running for full functionality
