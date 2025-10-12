# API Documentation

## Base URL
```
http://localhost:8765/api
```

## Authentication

All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## Authentication Endpoints

### 1. User Login
Authenticate a user and receive a JWT token.

**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userInfo": {
    "id": 1,
    "username": "admin",
    "fullName": "Administrator",
    "role": "ADMIN",
    "email": "admin@example.com"
  }
}
```

**Status Codes:**
- `200 OK` - Login successful
- `401 Unauthorized` - Invalid credentials
- `400 Bad Request` - Missing required fields

---

### 2. Get Current User
Get information about the currently authenticated user.

**Endpoint:** `GET /auth/me`

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "id": 1,
  "username": "admin",
  "fullName": "Administrator",
  "role": "ADMIN",
  "email": "admin@example.com",
  "phoneNumber": "+1234567890"
}
```

---

## User Management Endpoints

### 1. Get All Users
Retrieve a list of all users (Admin only).

**Endpoint:** `GET /admin/users`

**Headers:**
```
Authorization: Bearer <admin-token>
```

**Query Parameters:**
- `role` (optional): Filter by role (ADMIN, SUPERVISOR, JANITOR)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "username": "janitor1",
      "fullName": "John Doe",
      "role": "JANITOR",
      "email": "john@example.com",
      "phoneNumber": "+1234567890",
      "createdAt": "2025-01-01T00:00:00",
      "active": true
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "currentPage": 0
}
```

---

### 2. Create User
Create a new user account (Admin only).

**Endpoint:** `POST /admin/users`

**Headers:**
```
Authorization: Bearer <admin-token>
```

**Request Body:**
```json
{
  "username": "janitor2",
  "password": "password123",
  "fullName": "Jane Smith",
  "role": "JANITOR",
  "email": "jane@example.com",
  "phoneNumber": "+1234567891"
}
```

**Response:**
```json
{
  "id": 2,
  "username": "janitor2",
  "fullName": "Jane Smith",
  "role": "JANITOR",
  "email": "jane@example.com",
  "createdAt": "2025-10-12T10:30:00"
}
```

**Status Codes:**
- `201 Created` - User created successfully
- `400 Bad Request` - Validation error
- `409 Conflict` - Username already exists

---

### 3. Update User
Update user information (Admin only).

**Endpoint:** `PUT /admin/users/{userId}`

**Request Body:**
```json
{
  "fullName": "Jane Doe Smith",
  "email": "jane.smith@example.com",
  "phoneNumber": "+1234567892",
  "role": "SUPERVISOR"
}
```

---

### 4. Delete User
Soft delete a user (Admin only).

**Endpoint:** `DELETE /admin/users/{userId}`

**Response:**
```json
{
  "message": "User deleted successfully"
}
```

---

## Task Management Endpoints

### 1. Get All Tasks
Retrieve tasks with optional filtering.

**Endpoint:** `GET /tasks`

**Query Parameters:**
- `status` (optional): PENDING, IN_PROGRESS, COMPLETED, CANCELLED
- `priority` (optional): LOW, MEDIUM, HIGH, URGENT
- `assignedTo` (optional): User ID
- `dateFrom` (optional): Start date (ISO 8601)
- `dateTo` (optional): End date (ISO 8601)
- `page` (optional): Page number
- `size` (optional): Page size

**Response:**
```json
{
  "tasks": [
    {
      "id": 1,
      "title": "Clean Office Floor 3",
      "description": "Vacuum and mop all areas",
      "location": "Building A, Floor 3",
      "status": "PENDING",
      "priority": "HIGH",
      "assignedTo": {
        "id": 5,
        "fullName": "John Doe"
      },
      "createdBy": {
        "id": 1,
        "fullName": "Administrator"
      },
      "deadline": "2025-10-13T17:00:00",
      "createdAt": "2025-10-12T09:00:00",
      "updatedAt": "2025-10-12T09:00:00"
    }
  ],
  "stats": {
    "total": 100,
    "pending": 30,
    "inProgress": 20,
    "completed": 45,
    "cancelled": 5
  },
  "totalPages": 10,
  "currentPage": 0
}
```

---

### 2. Get Task by ID
Retrieve detailed information about a specific task.

**Endpoint:** `GET /tasks/{taskId}`

**Response:**
```json
{
  "id": 1,
  "title": "Clean Office Floor 3",
  "description": "Vacuum and mop all areas. Pay special attention to conference rooms.",
  "location": "Building A, Floor 3",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "assignedTo": {
    "id": 5,
    "fullName": "John Doe",
    "email": "john@example.com"
  },
  "createdBy": {
    "id": 1,
    "fullName": "Administrator"
  },
  "deadline": "2025-10-13T17:00:00",
  "startedAt": "2025-10-12T10:00:00",
  "completedAt": null,
  "completionNotes": null,
  "images": [],
  "createdAt": "2025-10-12T09:00:00",
  "updatedAt": "2025-10-12T10:00:00"
}
```

---

### 3. Create Task
Create a new cleaning task (Admin/Supervisor only).

**Endpoint:** `POST /tasks`

**Request Body:**
```json
{
  "title": "Clean Lobby Area",
  "description": "Full cleaning of main lobby including windows",
  "location": "Building A, Lobby",
  "priority": "MEDIUM",
  "assignedToId": 5,
  "deadline": "2025-10-14T15:00:00",
  "estimatedDuration": 120
}
```

**Response:**
```json
{
  "id": 101,
  "title": "Clean Lobby Area",
  "status": "PENDING",
  "assignedTo": {
    "id": 5,
    "fullName": "John Doe"
  },
  "createdAt": "2025-10-12T11:00:00"
}
```

---

### 4. Update Task Status
Update the status of a task.

**Endpoint:** `PUT /tasks/{taskId}/status`

**Request Body:**
```json
{
  "status": "IN_PROGRESS",
  "notes": "Started cleaning the lobby"
}
```

---

### 5. Complete Task
Mark a task as completed with optional photos.

**Endpoint:** `POST /tasks/{taskId}/complete`

**Request Body (multipart/form-data):**
```
completionNotes: Task completed successfully. All areas cleaned.
images: [file1.jpg, file2.jpg]
```

**Response:**
```json
{
  "id": 1,
  "status": "COMPLETED",
  "completedAt": "2025-10-12T16:00:00",
  "images": [
    {
      "id": 1,
      "url": "/api/images/task_completion_1_abc123.jpg",
      "uploadedAt": "2025-10-12T16:00:00"
    }
  ]
}
```

---

### 6. Delete Task
Delete a task (Admin only).

**Endpoint:** `DELETE /tasks/{taskId}`

---

## Attendance Endpoints

### 1. Check In
Record employee check-in with GPS verification.

**Endpoint:** `POST /attendance/checkin`

**Request Body:**
```json
{
  "latitude": 3.1390,
  "longitude": 101.6869,
  "accuracy": 15.0
}
```

**Response:**
```json
{
  "id": 1,
  "userId": 5,
  "checkInTime": "2025-10-12T08:00:00",
  "checkInLocation": {
    "latitude": 3.1390,
    "longitude": 101.6869
  },
  "status": "CHECKED_IN",
  "message": "Check-in successful"
}
```

**Status Codes:**
- `200 OK` - Check-in successful
- `400 Bad Request` - Already checked in or invalid location
- `403 Forbidden` - Location outside allowed radius

---

### 2. Check Out
Record employee check-out.

**Endpoint:** `POST /attendance/checkout`

**Request Body:**
```json
{
  "latitude": 3.1390,
  "longitude": 101.6869,
  "notes": "Completed all assigned tasks"
}
```

**Response:**
```json
{
  "id": 1,
  "checkOutTime": "2025-10-12T17:00:00",
  "workDuration": "9 hours 0 minutes",
  "status": "CHECKED_OUT"
}
```

---

### 3. Get Attendance Status
Get current attendance status for the logged-in user.

**Endpoint:** `GET /attendance/status`

**Response:**
```json
{
  "status": "CHECKED_IN",
  "checkInTime": "2025-10-12T08:00:00",
  "duration": "3 hours 25 minutes",
  "canCheckOut": true
}
```

---

### 4. Get Attendance History
Retrieve attendance history for a user.

**Endpoint:** `GET /attendance/history`

**Query Parameters:**
- `userId` (optional): User ID (Admin/Supervisor can view others)
- `dateFrom` (optional): Start date
- `dateTo` (optional): End date
- `page`, `size`: Pagination

**Response:**
```json
{
  "records": [
    {
      "id": 1,
      "date": "2025-10-12",
      "checkInTime": "08:00:00",
      "checkOutTime": "17:00:00",
      "workDuration": "9h 0m",
      "status": "PRESENT"
    },
    {
      "id": 2,
      "date": "2025-10-11",
      "checkInTime": "08:15:00",
      "checkOutTime": "17:05:00",
      "workDuration": "8h 50m",
      "status": "LATE"
    }
  ],
  "summary": {
    "totalDays": 20,
    "presentDays": 18,
    "lateDays": 3,
    "absentDays": 2
  }
}
```

---

## Announcement Endpoints

### 1. Get All Announcements
Retrieve announcements for the current user.

**Endpoint:** `GET /announcements`

**Query Parameters:**
- `targetRole` (optional): Filter by target role
- `unreadOnly` (optional): true/false

**Response:**
```json
{
  "announcements": [
    {
      "id": 1,
      "title": "Office Closed Tomorrow",
      "content": "The office will be closed for maintenance.",
      "priority": "HIGH",
      "targetRole": "ALL",
      "createdBy": {
        "id": 1,
        "fullName": "Administrator"
      },
      "createdAt": "2025-10-12T09:00:00",
      "read": false
    }
  ]
}
```

---

### 2. Create Announcement
Create a new announcement (Admin/Supervisor only).

**Endpoint:** `POST /announcements`

**Request Body:**
```json
{
  "title": "New Safety Procedures",
  "content": "Please review the updated safety guidelines...",
  "priority": "MEDIUM",
  "targetRole": "JANITOR"
}
```

---

### 3. Mark as Read
Mark an announcement as read.

**Endpoint:** `PUT /announcements/{announcementId}/read`

---

## Image Management Endpoints

### 1. Upload Image
Upload an image (task completion, profile, etc.).

**Endpoint:** `POST /images/upload`

**Request (multipart/form-data):**
```
file: image.jpg
entityType: TASK_COMPLETION
entityId: 1
```

**Response:**
```json
{
  "id": 1,
  "url": "/api/images/task_completion_1_abc123.jpg",
  "fileName": "task_completion_1_abc123.jpg",
  "uploadedAt": "2025-10-12T14:30:00"
}
```

---

### 2. Get Image
Retrieve an image file.

**Endpoint:** `GET /images/{imageName}`

**Response:** Image file (JPEG, PNG, etc.)

---

## Profile Management Endpoints

### 1. Get User Profile
Get detailed profile information.

**Endpoint:** `GET /profile`

**Response:**
```json
{
  "id": 5,
  "username": "janitor1",
  "fullName": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "+1234567890",
  "role": "JANITOR",
  "avatar": "/api/images/profile_5_xyz789.jpg",
  "joinedDate": "2025-01-01",
  "stats": {
    "tasksCompleted": 150,
    "averageCompletionTime": "2.5 hours",
    "attendanceRate": "95%"
  }
}
```

---

### 2. Update Profile
Update user profile information.

**Endpoint:** `PUT /profile`

**Request Body:**
```json
{
  "fullName": "John A. Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890"
}
```

---

### 3. Change Password
Change user password.

**Endpoint:** `PUT /profile/password`

**Request Body:**
```json
{
  "currentPassword": "oldpassword123",
  "newPassword": "newpassword456"
}
```

---

### 4. Upload Profile Picture
Upload a profile picture.

**Endpoint:** `POST /profile/avatar`

**Request (multipart/form-data):**
```
file: avatar.jpg
```

---

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request
```json
{
  "timestamp": "2025-10-12T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ]
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2025-10-12T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2025-10-12T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Insufficient permissions"
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-10-12T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task with ID 999 not found"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2025-10-12T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Rate Limiting

Currently, there are no rate limits imposed. This may change in future versions.

---

## Pagination

Endpoints that return lists support pagination with the following query parameters:

- `page`: Page number (0-indexed, default: 0)
- `size`: Number of items per page (default: 20, max: 100)

Paginated responses include:
```json
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 8,
  "currentPage": 0,
  "pageSize": 20
}
```

---

## Testing the API

### Using cURL

```bash
# Login
curl -X POST http://localhost:8765/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Get tasks (with token)
curl -X GET http://localhost:8765/api/tasks \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Using Postman

1. Import the API endpoints
2. Set up an environment variable for the base URL
3. Create a variable for the JWT token
4. Use pre-request scripts to automatically set the token

---

## Changelog

### Version 1.0.0 (Current)
- Initial API release
- Authentication endpoints
- Task management
- Attendance tracking
- Announcements
- Image upload
- Profile management

---

For more information, please refer to the [main README](../README.md) or contact the development team.
