package com.cleaningsystem.backend.controller;

import com.cleaningsystem.backend.dto.BatchTaskRequest;
import com.cleaningsystem.backend.dto.PagedTaskResponse;
import com.cleaningsystem.backend.dto.TaskFilterRequest;
import com.cleaningsystem.backend.entity.Task;
import com.cleaningsystem.backend.entity.User;
import com.cleaningsystem.backend.entity.UserProfile;
import com.cleaningsystem.backend.service.TaskService;
import com.cleaningsystem.backend.repository.UserProfileRepository;
import com.cleaningsystem.backend.repository.UserRepository;
import com.cleaningsystem.backend.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    
    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    // Create a new task
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody Task task,
                                                         HttpServletRequest request) {
        try {
            // Get current user info from JWT token
            Map<String, Object> userInfo = getUserInfo(request);
            Long currentUserId = (Long) userInfo.get("userId");
            String currentUserRole = (String) userInfo.get("role");

            if (currentUserId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User authentication failed, please login again");
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
            }

            // Set assignedBy from JWT token
            task.setAssignedBy(currentUserId);

            Task createdTask = taskService.createTask(task);
            
            // Return response format expected by frontend
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("taskId", createdTask.getTaskId());
            response.put("message", "Task created successfully");
            
            // Include task data for immediate use
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("taskId", createdTask.getTaskId());
            taskData.put("title", createdTask.getTitle());
            taskData.put("description", createdTask.getDescription());
            taskData.put("status", createdTask.getStatus());
            taskData.put("priority", createdTask.getPriority());
            taskData.put("location", createdTask.getLocation());
            taskData.put("assignedTo", createdTask.getAssignedTo());
            taskData.put("assignedBy", createdTask.getAssignedBy());
            taskData.put("scheduledTime", createdTask.getScheduledTime());
            taskData.put("dueDate", createdTask.getDueDate());
            taskData.put("createdAt", createdTask.getCreatedAt());
            
            response.put("data", taskData);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Handle date validation errors specifically
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", "VALIDATION_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create task: " + e.getMessage());
            errorResponse.put("errorType", "INTERNAL_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get all tasks - Fixed to prevent Jackson serialization issues with Hibernate proxies
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllTasks(@RequestParam(required = false) Long janitorId,
                                                                 @RequestParam(required = false) String status,
                                                                 @RequestParam(required = false) String date) {
        try {
            List<Task> tasks;
            
            if (janitorId != null && status != null) {
                tasks = taskService.getTasksByJanitorAndStatus(janitorId, status);
            } else if (janitorId != null && date != null) {
                LocalDateTime dateTime = LocalDateTime.parse(date + "T00:00:00");
                tasks = taskService.getTasksForJanitorOnDate(janitorId, dateTime);
            } else if (janitorId != null) {
                tasks = taskService.getTasksByJanitor(janitorId);
            } else if (status != null) {
                tasks = taskService.getTasksByStatus(status);
            } else {
                tasks = taskService.getAllTasks();
            }
            
            // Convert Task entities to safe HashMap format to prevent Jackson serialization issues
            List<Map<String, Object>> safeTasks = tasks.stream().map(this::convertTaskToSafeFormat).collect(Collectors.toList());
            
            return new ResponseEntity<>(safeTasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get task by ID - Fixed to prevent Jackson serialization issues with Hibernate proxies
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTaskById(@PathVariable Long id) {
        try {
            // Use the new method that includes images
            Map<String, Object> taskWithImages = taskService.getTaskWithImages(id);
            if (taskWithImages != null) {
                return new ResponseEntity<>(taskWithImages, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Update task - Fixed to prevent Jackson serialization issues with Hibernate proxies
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('JANITOR')")
    public ResponseEntity<Map<String, Object>> updateTask(@PathVariable Long id, @RequestBody Task task,
                                                         HttpServletRequest request) {
        try {
            // Get current user info from JWT token
            Map<String, Object> userInfo = getUserInfo(request);
            Long currentUserId = (Long) userInfo.get("userId");
            String currentUserRole = (String) userInfo.get("role");

            if (currentUserId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User authentication failed, please login again");
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
            }

            // Get existing task to check permissions
            Optional<Task> existingTaskOpt = taskService.getTaskById(id);
            if (existingTaskOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Task not found");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            Task existingTask = existingTaskOpt.get();

            // Permission check: Only admin or task creator can edit
            if (!"admin".equals(currentUserRole) && !currentUserId.equals(existingTask.getAssignedBy())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Permission denied: You can only edit tasks you created");
                errorResponse.put("errorType", "PERMISSION_DENIED");
                errorResponse.put("currentUser", currentUserId);
                errorResponse.put("taskCreator", existingTask.getAssignedBy());
                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }

            Task updatedTask = taskService.updateTask(id, task);
            if (updatedTask != null) {
                Map<String, Object> safeTask = convertTaskToSafeFormat(updatedTask);
                return new ResponseEntity<>(safeTask, HttpStatus.OK);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Task not found");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            // Handle date validation errors specifically
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", "VALIDATION_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update task: " + e.getMessage());
            errorResponse.put("errorType", "INTERNAL_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Start task - Fixed to prevent Jackson serialization issues with Hibernate proxies
    @PutMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startTask(@PathVariable Long id) {
        try {
            Task startedTask = taskService.startTask(id);
            if (startedTask != null) {
                Map<String, Object> safeTask = convertTaskToSafeFormat(startedTask);
                return new ResponseEntity<>(safeTask, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Complete task with photos and notes - Enhanced for photo handling
    @PutMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeTask(@PathVariable Long id, 
                                                           @RequestBody(required = false) Map<String, Object> completionData) {
        try {
            // Extract completion data
            String completionNotes = null;
            List<Map<String, String>> photos = null;

            if (completionData != null) {
                completionNotes = (String) completionData.get("notes");
                photos = (List<Map<String, String>>) completionData.get("photos");
            }

            Task completedTask = taskService.completeTaskWithData(id, completionNotes, photos);
            if (completedTask != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Task completed successfully");
                response.put("data", convertTaskToSafeFormat(completedTask));
                
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Task not found");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to complete task: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
    // Assign task to janitor - Fixed to prevent Jackson serialization issues with Hibernate proxies
    @PutMapping("/{id}/assign")
    public ResponseEntity<Map<String, Object>> assignTask(@PathVariable Long id, 
                                                          @RequestBody Map<String, Long> assignmentData) {
        try {
            Long janitorId = assignmentData.get("janitorId");
            if (janitorId == null) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            
            Task assignedTask = taskService.assignTask(id, janitorId);
            if (assignedTask != null) {
                Map<String, Object> safeTask = convertTaskToSafeFormat(assignedTask);
                return new ResponseEntity<>(safeTask, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Undo task completion
    @PutMapping("/{id}/undo-completion")
    public ResponseEntity<Map<String, Object>> undoTaskCompletion(@PathVariable Long id) {
        try {
            Task revertedTask = taskService.undoTaskCompletion(id);
            if (revertedTask != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Task completion undone successfully");
                response.put("data", convertTaskToSafeFormat(revertedTask));
                
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Task not found or cannot be reverted");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to undo task completion: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Upload photos for task completion
    @PostMapping("/{id}/photos")
    public ResponseEntity<Map<String, Object>> uploadTaskPhotos(@PathVariable Long id,
                                                               @RequestParam("files") List<org.springframework.web.multipart.MultipartFile> files,
                                                               @RequestParam(required = false) String uploadType) {
        try {
            // For now, we'll simulate photo upload by storing metadata
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Photos uploaded successfully");
            
            // Simulate photo data
            Map<String, Object> photoData = new HashMap<>();
            photoData.put("photoId", System.currentTimeMillis());
            photoData.put("url", "/uploads/tasks/" + id + "/" + System.currentTimeMillis() + ".jpg");
            photoData.put("filename", files.get(0).getOriginalFilename());
            photoData.put("size", files.get(0).getSize());
            photoData.put("uploadedAt", LocalDateTime.now().toString());
            
            response.put("data", photoData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to upload photos: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Delete task
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTask(@PathVariable Long id) {
        try {
            boolean deleted = taskService.deleteTask(id);
            Map<String, String> response = new HashMap<>();
            if (deleted) {
                response.put("message", "Task deleted successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("message", "Task not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error deleting task");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get overdue tasks - Fixed to prevent Jackson serialization issues with Hibernate proxies
    @GetMapping("/overdue")
    public ResponseEntity<List<Map<String, Object>>> getOverdueTasks() {
        try {
            List<Task> overdueTasks = taskService.getOverdueTasks();
            List<Map<String, Object>> safeOverdueTasks = overdueTasks.stream()
                    .map(this::convertTaskToSafeFormat)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(safeOverdueTasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
    // Get task statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTaskStatistics(@RequestParam(required = false) Long janitorId) {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            if (janitorId != null) {
                // Get statistics for specific janitor
                List<Object[]> stats = taskService.getTaskStatisticsByJanitor(janitorId);
                for (Object[] stat : stats) {
                    statistics.put((String) stat[0], stat[1]);
                }
            } else {
                // Get overall statistics
                List<Object[]> stats = taskService.getTaskStatistics();
                for (Object[] stat : stats) {
                    statistics.put((String) stat[0], stat[1]);
                }
            }
            
            return new ResponseEntity<>(statistics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get tasks assigned by admin - Fixed to prevent Jackson serialization issues with Hibernate proxies
    @GetMapping("/assigned-by/{adminId}")
    public ResponseEntity<List<Map<String, Object>>> getTasksByAdmin(@PathVariable Long adminId) {
        try {
            List<Task> tasks = taskService.getTasksByAdmin(adminId);
            List<Map<String, Object>> safeTasks = tasks.stream()
                    .map(this::convertTaskToSafeFormat)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(safeTasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Update overdue tasks (utility endpoint)
    @PostMapping("/update-overdue")
    public ResponseEntity<Map<String, String>> updateOverdueTasks() {
        try {
            taskService.updateOverdueTasks();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Overdue tasks updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error updating overdue tasks");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Advanced search endpoint for View Tasks functionality
    @PostMapping("/search")
    public ResponseEntity<PagedTaskResponse> searchTasks(@RequestBody TaskFilterRequest filterRequest) {
        try {
            PagedTaskResponse response = taskService.searchTasksWithFilter(filterRequest);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // Return empty response with error indication
            PagedTaskResponse errorResponse = new PagedTaskResponse();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Batch operations endpoint
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> performBatchOperation(@RequestBody BatchTaskRequest batchRequest) {
        try {
            Map<String, Object> result = taskService.performBatchOperation(batchRequest);
            HttpStatus status = (Boolean) result.get("success") ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(result, status);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Batch operation failed: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get task options for filters (statuses, priorities, users)
    @GetMapping("/filter-options")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        try {
            Map<String, Object> options = new HashMap<>();
            
            // Task statuses
            options.put("statuses", List.of("pending", "in_progress", "completed", "overdue"));
            
            // Task priorities  
            options.put("priorities", List.of("low", "medium", "high", "urgent"));
            
            // This could be enhanced to get actual users from database
            options.put("message", "Filter options retrieved successfully");
            
            return new ResponseEntity<>(options, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to get filter options");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Quick stats endpoint for dashboard widgets
    @GetMapping("/quick-stats")
    public ResponseEntity<Map<String, Object>> getQuickStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Get basic counts
            stats.put("totalTasks", taskService.countAllTasks());
            stats.put("pendingTasks", taskService.countByStatus("pending"));
            stats.put("inProgressTasks", taskService.countByStatus("in_progress"));
            stats.put("completedTasks", taskService.countByStatus("completed"));
            stats.put("overdueTasks", taskService.countOverdueTasks());
            
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to get quick stats");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Helper method to convert Task entity to safe HashMap format
    // Prevents Jackson serialization issues with Hibernate lazy-loaded relationships
    private Map<String, Object> convertTaskToSafeFormat(Task task) {
        Map<String, Object> safeTask = new HashMap<>();
        
        // Basic task information
        safeTask.put("taskId", task.getTaskId());
        safeTask.put("title", task.getTitle());
        safeTask.put("description", task.getDescription());
        safeTask.put("location", task.getLocation());
        safeTask.put("status", task.getStatus());
        safeTask.put("priority", task.getPriority());
        
        // Time fields
        safeTask.put("scheduledTime", task.getScheduledTime());
        safeTask.put("dueDate", task.getDueDate());
        safeTask.put("createdAt", task.getCreatedAt());
        safeTask.put("startedAt", task.getStartedAt());
        safeTask.put("completedAt", task.getCompletedAt());
        
        // Progress and duration
        safeTask.put("progressPercentage", task.getProgressPercentage());
        safeTask.put("estimatedDuration", task.getEstimatedDuration());
        safeTask.put("actualDuration", task.getActualDuration());

        // Completion information
        safeTask.put("completionNotes", task.getCompletionNotes());

        // Assignment information (safely extract from potentially lazy-loaded relationships)
        safeTask.put("assignedTo", task.getAssignedTo());
        safeTask.put("assignedBy", task.getAssignedBy());
        
        // Get user names and avatar URLs for assignment information
        String assignedToName = "Unassigned";
        String assignedToAvatarUrl = null;
        String assignedByName = "Unknown";
        String assignedByAvatarUrl = null;

        if (task.getAssignedTo() != null) {
            try {
                Optional<User> assignedUser = userRepository.findById(task.getAssignedTo());
                assignedToName = assignedUser.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                    .orElse("Unknown User");

                // Get avatar URL from user_profiles
                Optional<UserProfile> assignedProfile = userProfileRepository.findByUserId(task.getAssignedTo());
                if (assignedProfile.isPresent() && assignedProfile.get().getAvatarUrl() != null) {
                    assignedToAvatarUrl = assignedProfile.get().getAvatarUrl();
                }
            } catch (Exception e) {
                System.err.println("Error loading assigned user: " + e.getMessage());
            }
        }

        if (task.getAssignedBy() != null) {
            try {
                Optional<User> assignedByUser = userRepository.findById(task.getAssignedBy());
                assignedByName = assignedByUser.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                    .orElse("Unknown Admin");

                // Get avatar URL from user_profiles
                Optional<UserProfile> assignedByProfile = userProfileRepository.findByUserId(task.getAssignedBy());
                if (assignedByProfile.isPresent() && assignedByProfile.get().getAvatarUrl() != null) {
                    assignedByAvatarUrl = assignedByProfile.get().getAvatarUrl();
                }
            } catch (Exception e) {
                System.err.println("Error loading assigning user: " + e.getMessage());
            }
        }

        safeTask.put("assignedToName", assignedToName);
        safeTask.put("assignedToAvatarUrl", assignedToAvatarUrl);
        safeTask.put("assignedByName", assignedByName);
        safeTask.put("assignedByAvatarUrl", assignedByAvatarUrl);
        
        // Additional fields
        safeTask.put("instructions", task.getInstructions());
        safeTask.put("notes", task.getNotes());
        safeTask.put("completionNotes", task.getCompletionNotes());
        safeTask.put("toolsRequired", task.getToolsRequired());
        
        return safeTask;
    }

    // Helper method to extract user info from JWT token
    private Map<String, Object> getUserInfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenProvider.extractTokenFromHeader(authHeader);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new SecurityException("Invalid token");
        }
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", jwtTokenProvider.getUserIdFromToken(token));
        userInfo.put("role", jwtTokenProvider.getRoleFromToken(token));
        return userInfo;
    }

    // ===============================
    // PUBLIC TASK API ENDPOINTS
    // ===============================
    
    // Get all public tasks (task wall)
    @GetMapping("/public")
    public ResponseEntity<List<Map<String, Object>>> getPublicTasks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String location) {
        try {
            List<Task> publicTasks;
            
            if (search != null || priority != null || location != null) {
                publicTasks = taskService.searchPublicTasks(search, priority, location);
            } else {
                publicTasks = taskService.getPublicTasks();
            }
            
            // Convert to safe format
            List<Map<String, Object>> safeTasks = publicTasks.stream()
                .map(this::convertTaskToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeTasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Claim a public task
    @PostMapping("/{taskId}/claim")
    public ResponseEntity<Map<String, Object>> claimPublicTask(@PathVariable Long taskId, 
                                                              @RequestParam Long janitorId) {
        try {
            Task claimedTask = taskService.claimPublicTask(taskId, janitorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Task claimed successfully");
            response.put("data", convertTaskToSafeFormat(claimedTask));
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to claim task: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Release a task back to public pool
    @PostMapping("/{taskId}/release")
    public ResponseEntity<Map<String, Object>> releaseTaskToPublic(@PathVariable Long taskId, 
                                                                  @RequestParam Long janitorId) {
        try {
            Task releasedTask = taskService.releaseTaskToPublic(taskId, janitorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Task released back to public pool");
            response.put("data", convertTaskToSafeFormat(releasedTask));
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to release task: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Check if a task can be claimed
    @GetMapping("/{taskId}/can-claim")
    public ResponseEntity<Map<String, Object>> canClaimTask(@PathVariable Long taskId, 
                                                           @RequestParam Long janitorId) {
        try {
            boolean canClaim = taskService.canClaimTask(taskId, janitorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("canClaim", canClaim);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("canClaim", false);
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get public task statistics
    @GetMapping("/public/statistics")
    public ResponseEntity<Map<String, Object>> getPublicTaskStatistics() {
        try {
            Map<String, Object> stats = taskService.getPublicTaskStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get public task statistics: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get recently claimed tasks
    @GetMapping("/recently-claimed")
    public ResponseEntity<List<Map<String, Object>>> getRecentlyClaimedTasks(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Task> recentTasks = taskService.getRecentlyClaimedTasks(limit);
            
            List<Map<String, Object>> safeTasks = recentTasks.stream()
                .map(this::convertTaskToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeTasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get recent tasks for a specific janitor (dashboard support)
    @GetMapping("/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentTasks(
            @RequestParam Long janitorId, 
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Task> tasks = taskService.getTasksByJanitor(janitorId);
            
            // Get most recent tasks, sorted by creation date descending
            List<Task> recentTasks = tasks.stream()
                .sorted((a, b) -> {
                    LocalDateTime aTime = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
                    LocalDateTime bTime = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
                    return bTime.compareTo(aTime);
                })
                .limit(limit)
                .collect(Collectors.toList());
            
            List<Map<String, Object>> safeTasks = recentTasks.stream()
                .map(this::convertTaskToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeTasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}