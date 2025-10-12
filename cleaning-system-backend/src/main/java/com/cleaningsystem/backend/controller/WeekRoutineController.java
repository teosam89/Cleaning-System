package com.cleaningsystem.backend.controller;

import com.cleaningsystem.backend.entity.WeekRoutine;
import com.cleaningsystem.backend.entity.Task;
import com.cleaningsystem.backend.entity.User;
import com.cleaningsystem.backend.service.WeekRoutineService;
import com.cleaningsystem.backend.utils.JwtTokenProvider;
import com.cleaningsystem.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/week-routines")
@CrossOrigin(origins = "*")
public class WeekRoutineController {
    
    @Autowired
    private WeekRoutineService weekRoutineService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

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

    // Permission check method
    private boolean canEditRoutine(WeekRoutine routine, String currentUserRole, Long currentUserId) {
        // Admin can edit all routines
        if ("admin".equals(currentUserRole)) {
            return true;
        }

        // Supervisor can only edit their own routines
        if ("supervisor".equals(currentUserRole)) {
            return routine.getCreatedBy().equals(currentUserId);
        }

        return false;
    }

    // Create a new week routine
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> createWeekRoutine(@RequestBody WeekRoutine routine,
                                                               HttpServletRequest request) {
        try {
            // Get current user info from JWT token
            Map<String, Object> userInfo = getUserInfo(request);
            Long currentUserId = (Long) userInfo.get("userId");
            String currentUserRole = (String) userInfo.get("role");

            // Force set correct creator ID from JWT token (ignore frontend value)
            routine.setCreatedBy(currentUserId);

            WeekRoutine createdRoutine = weekRoutineService.createWeekRoutine(routine);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("routineId", createdRoutine.getRoutineId());
            response.put("message", "Week routine created successfully");
            response.put("data", convertRoutineToSafeFormat(createdRoutine));

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (SecurityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "认证失败，请重新登录");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", "VALIDATION_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create week routine: " + e.getMessage());
            errorResponse.put("errorType", "INTERNAL_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get all week routines with optional filters
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllWeekRoutines(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Long createdBy,
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(required = false) String search) {
        try {
            List<WeekRoutine> routines;
            
            if (active != null || taskType != null || createdBy != null || assignedTo != null || search != null) {
                routines = weekRoutineService.searchRoutines(search, taskType, null, active, createdBy, assignedTo);
            } else {
                routines = weekRoutineService.getAllWeekRoutines();
            }
            
            // Convert to safe format to prevent serialization issues
            List<Map<String, Object>> safeRoutines = routines.stream()
                .map(this::convertRoutineToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeRoutines, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get week routine by ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getWeekRoutineById(@PathVariable Long id) {
        try {
            Optional<WeekRoutine> routine = weekRoutineService.getWeekRoutineById(id);
            
            if (routine.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", convertRoutineToSafeFormat(routine.get()));
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Week routine not found");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving week routine: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Update week routine
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> updateWeekRoutine(@PathVariable Long id,
                                                               @RequestBody WeekRoutine routine,
                                                               HttpServletRequest request) {
        try {
            // Get current user info
            Map<String, Object> userInfo = getUserInfo(request);
            Long currentUserId = (Long) userInfo.get("userId");
            String currentUserRole = (String) userInfo.get("role");

            // Find existing routine
            WeekRoutine existingRoutine = weekRoutineService.getWeekRoutineById(id)
                .orElseThrow(() -> new IllegalArgumentException("Routine not found"));

            // Permission check
            if (!canEditRoutine(existingRoutine, currentUserRole, currentUserId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "无权限编辑此任务");
                errorResponse.put("errorCode", "PERMISSION_DENIED");
                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }

            // Preserve original creator and creation time
            routine.setCreatedBy(existingRoutine.getCreatedBy());
            routine.setCreatedAt(existingRoutine.getCreatedAt());

            WeekRoutine updatedRoutine = weekRoutineService.updateWeekRoutine(id, routine);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Week routine updated successfully");
            response.put("data", convertRoutineToSafeFormat(updatedRoutine));

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SecurityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "认证失败");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", "VALIDATION_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update week routine: " + e.getMessage());
            errorResponse.put("errorType", "INTERNAL_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Delete week routine
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> deleteWeekRoutine(@PathVariable Long id,
                                                               HttpServletRequest request) {
        try {
            // Get current user info
            Map<String, Object> userInfo = getUserInfo(request);
            Long currentUserId = (Long) userInfo.get("userId");
            String currentUserRole = (String) userInfo.get("role");

            // Find existing routine
            WeekRoutine existingRoutine = weekRoutineService.getWeekRoutineById(id)
                .orElseThrow(() -> new IllegalArgumentException("Routine not found"));

            // Permission check
            if (!canEditRoutine(existingRoutine, currentUserRole, currentUserId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "无权限删除此任务");
                errorResponse.put("errorCode", "PERMISSION_DENIED");
                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }

            weekRoutineService.deleteWeekRoutine(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Week routine deleted successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SecurityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "认证失败");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete week routine: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Toggle routine active status
    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleRoutineStatus(@PathVariable Long id) {
        try {
            WeekRoutine updatedRoutine = weekRoutineService.toggleRoutineStatus(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Routine status updated successfully");
            response.put("data", convertRoutineToSafeFormat(updatedRoutine));
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to toggle routine status: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get active public routines (for task wall)
    @GetMapping("/public")
    public ResponseEntity<List<Map<String, Object>>> getActivePublicRoutines() {
        try {
            List<WeekRoutine> publicRoutines = weekRoutineService.getActivePublicRoutines();
            
            List<Map<String, Object>> safeRoutines = publicRoutines.stream()
                .map(this::convertRoutineToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeRoutines, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get routines by janitor
    @GetMapping("/janitor/{janitorId}")
    public ResponseEntity<List<Map<String, Object>>> getRoutinesByJanitor(@PathVariable Long janitorId) {
        try {
            List<WeekRoutine> routines = weekRoutineService.getRoutinesByJanitor(janitorId);
            
            List<Map<String, Object>> safeRoutines = routines.stream()
                .map(this::convertRoutineToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeRoutines, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get routines by creator (admin)
    @GetMapping("/creator/{adminId}")
    public ResponseEntity<List<Map<String, Object>>> getRoutinesByCreator(@PathVariable Long adminId) {
        try {
            List<WeekRoutine> routines = weekRoutineService.getWeekRoutinesByCreator(adminId);
            
            List<Map<String, Object>> safeRoutines = routines.stream()
                .map(this::convertRoutineToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeRoutines, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // CORE FEATURE: Generate tasks from all active routines
    @PostMapping("/generate-tasks")
    public ResponseEntity<Map<String, Object>> generateTasksFromRoutines() {
        try {
            List<Task> generatedTasks = weekRoutineService.generateTasksFromRoutines();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tasks generated successfully");
            response.put("tasksGenerated", generatedTasks.size());
            
            // Include summary of generated tasks
            List<Map<String, Object>> taskSummaries = generatedTasks.stream()
                .map(task -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("taskId", task.getTaskId());
                    summary.put("title", task.getTitle());
                    summary.put("location", task.getLocation());
                    summary.put("scheduledTime", task.getScheduledTime());
                    summary.put("assignedTo", task.getAssignedTo());
                    summary.put("priority", task.getPriority());
                    return summary;
                })
                .collect(Collectors.toList());
            
            response.put("generatedTasks", taskSummaries);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to generate tasks: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Generate task from specific routine
    @PostMapping("/{id}/generate-task")
    public ResponseEntity<Map<String, Object>> generateTaskFromRoutine(@PathVariable Long id) {
        try {
            Task generatedTask = weekRoutineService.generateTaskFromRoutine(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Task generated successfully from routine");
            response.put("taskId", generatedTask.getTaskId());
            
            // Include task details
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("taskId", generatedTask.getTaskId());
            taskData.put("title", generatedTask.getTitle());
            taskData.put("description", generatedTask.getDescription());
            taskData.put("location", generatedTask.getLocation());
            taskData.put("priority", generatedTask.getPriority());
            taskData.put("scheduledTime", generatedTask.getScheduledTime());
            taskData.put("assignedTo", generatedTask.getAssignedTo());
            taskData.put("status", generatedTask.getStatus());
            
            response.put("task", taskData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to generate task: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get routine statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRoutineStatistics() {
        try {
            Map<String, Object> stats = weekRoutineService.getRoutineStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get statistics: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get recent routines for dashboard
    @GetMapping("/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentRoutines() {
        try {
            List<WeekRoutine> recentRoutines = weekRoutineService.getRecentRoutines();
            
            List<Map<String, Object>> safeRoutines = recentRoutines.stream()
                .map(this::convertRoutineToSafeFormat)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safeRoutines, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Convert WeekRoutine entity to safe format to prevent Jackson serialization issues
    private Map<String, Object> convertRoutineToSafeFormat(WeekRoutine routine) {
        Map<String, Object> safeRoutine = new HashMap<>();

        safeRoutine.put("routineId", routine.getRoutineId());
        safeRoutine.put("title", routine.getTitle());
        safeRoutine.put("description", routine.getDescription());
        safeRoutine.put("location", routine.getLocation());
        safeRoutine.put("priority", routine.getPriority());
        safeRoutine.put("weekDays", routine.getWeekDays());
        safeRoutine.put("scheduledTime", routine.getScheduledTime());
        safeRoutine.put("estimatedDuration", routine.getEstimatedDuration());
        safeRoutine.put("instructions", routine.getInstructions());
        safeRoutine.put("toolsRequired", routine.getToolsRequired());
        safeRoutine.put("taskType", routine.getTaskType());
        safeRoutine.put("assignedTo", routine.getAssignedTo());
        safeRoutine.put("createdBy", routine.getCreatedBy());
        safeRoutine.put("active", routine.getActive());
        safeRoutine.put("createdAt", routine.getCreatedAt());
        safeRoutine.put("updatedAt", routine.getUpdatedAt());
        safeRoutine.put("lastGenerated", routine.getLastGenerated());

        // Add helper information
        safeRoutine.put("weekDaysArray", routine.getWeekDaysArray());

        // Add creator role information for permission checks
        if (routine.getCreatedBy() != null) {
            User creator = userRepository.findById(routine.getCreatedBy()).orElse(null);
            if (creator != null) {
                safeRoutine.put("createdByRole", creator.getRole());
                safeRoutine.put("createdByName", creator.getFullName());
            }
        }

        return safeRoutine;
    }
}