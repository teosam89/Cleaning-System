@echo off
echo Starting Cleaning Management System Full Stack...
echo.

echo 1. Starting Spring Boot backend server...
echo Opening new command window for backend...
start cmd /k "cd /d %~dp0cleaning-system-backend && echo Starting Spring Boot Backend... && timeout /t 3 && mvn org.springframework.boot:spring-boot-maven-plugin:3.2.0:run"
echo.

echo 2. Waiting for backend to start (30 seconds)...
timeout /t 30
echo.

echo 3. Starting Vue frontend development server...
echo Opening new command window for frontend...
start cmd /k "cd /d %~dp0cleaning-system-frontend && echo Starting Vue Frontend... && timeout /t 3 && npm run serve"
echo.

echo 4. System startup complete!
echo.
echo Backend will be available at: http://localhost:8765/api
echo Frontend will be available at: http://localhost:3000 (or next available port like :3001)
echo.

echo Note: Database will auto-initialize with correct roles (admin, janitor)
echo Login authentication has been fixed and should work properly
echo.
pause