package com.cleaningsystem.backend.service;

import com.cleaningsystem.backend.dto.BatchTaskRequest;
import com.cleaningsystem.backend.dto.PagedTaskResponse;
import com.cleaningsystem.backend.dto.TaskFilterRequest;
import com.cleaningsystem.backend.dto.TaskSummaryResponse;
import com.cleaningsystem.backend.entity.Image;
import com.cleaningsystem.backend.entity.Task;
import com.cleaningsystem.backend.entity.User;
import com.cleaningsystem.backend.entity.UserProfile;
import com.cleaningsystem.backend.repository.ImageRepository;
import com.cleaningsystem.backend.repository.TaskRepository;
import com.cleaningsystem.backend.repository.UserProfileRepository;
import com.cleaningsystem.backend.repository.UserRepository;
import com.cleaningsystem.backend.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageService imageService;
    
    // Create a new task
    public Task createTask(Task task) {
        // Validate date logic before saving
        validateTaskDates(task);
        
        task.setCreatedAt(DateTimeUtils.nowUtc());
        if (task.getStatus() == null) {
            task.setStatus("pending");
        }
        if (task.getProgressPercentage() == null) {
            task.setProgressPercentage(0);
        }
        return taskRepository.save(task);
    }
    
    /**
     * Validate task date logic to ensure business rules are followed
     * Uses UTC timezone for consistent validation across different server timezones
     * Enhanced with detailed error messaging for debugging timezone issues
     * @param task Task to validate
     * @throws IllegalArgumentException if date logic is invalid
     */
    private void validateTaskDates(Task task) {
        // Validate scheduled time using utility class with enhanced error messaging
        // Use 10 minutes tolerance to handle timezone edge cases, network latency, and practical scheduling needs
        if (!DateTimeUtils.isValidScheduledTime(task.getScheduledTime(), 10)) {
            throw new IllegalArgumentException(DateTimeUtils.getScheduledTimeErrorMessage(task.getScheduledTime(), 10));
        }
        
        // Validate future scheduling limit (not more than 1 year in advance)
        if (!DateTimeUtils.isWithinFutureLimit(task.getScheduledTime())) {
            throw new IllegalArgumentException(DateTimeUtils.getFutureLimitErrorMessage(task.getScheduledTime()));
        }
        
        // Validate task duration if both start and end times are provided
        if (task.getScheduledTime() != null && task.getDueDate() != null) {
            if (!DateTimeUtils.isValidTaskDuration(task.getScheduledTime(), task.getDueDate())) {
                throw new IllegalArgumentException(DateTimeUtils.getDurationErrorMessage(task.getScheduledTime(), task.getDueDate()));
            }
        }
    }
    
    // Get all tasks
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    // Get task by ID
    public Optional<Task> getTaskById(Long taskId) {
        return taskRepository.findById(taskId);
    }
    
    // Get tasks assigned to a janitor
    public List<Task> getTasksByJanitor(Long janitorId) {
        return taskRepository.findByAssignedToOrderByScheduledTimeAsc(janitorId);
    }
    
    // Get tasks by status
    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findByStatusOrderByScheduledTimeAsc(status);
    }
    
    // Get tasks by janitor and status
    public List<Task> getTasksByJanitorAndStatus(Long janitorId, String status) {
        return taskRepository.findByAssignedToAndStatusOrderByScheduledTimeAsc(janitorId, status);
    }
    
    // Get tasks created by admin
    public List<Task> getTasksByAdmin(Long adminId) {
        return taskRepository.findByAssignedByOrderByCreatedAtDesc(adminId);
    }
    
    // Get overdue tasks
    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks(DateTimeUtils.nowUtc());
    }
    
    // Get tasks for janitor on specific date
    public List<Task> getTasksForJanitorOnDate(Long janitorId, LocalDateTime date) {
        return taskRepository.findTasksForJanitorOnDate(janitorId, date);
    }
    
    // Update task
    public Task updateTask(Long taskId, Task updatedTask) {
        Optional<Task> existingTaskOpt = taskRepository.findById(taskId);
        if (existingTaskOpt.isPresent()) {
            Task existingTask = existingTaskOpt.get();
            
            // Update fields
            if (updatedTask.getTitle() != null) {
                existingTask.setTitle(updatedTask.getTitle());
            }
            if (updatedTask.getDescription() != null) {
                existingTask.setDescription(updatedTask.getDescription());
            }
            if (updatedTask.getLocation() != null) {
                existingTask.setLocation(updatedTask.getLocation());
            }
            if (updatedTask.getStatus() != null) {
                existingTask.setStatus(updatedTask.getStatus());
            }
            if (updatedTask.getPriority() != null) {
                existingTask.setPriority(updatedTask.getPriority());
            }
            if (updatedTask.getScheduledTime() != null) {
                existingTask.setScheduledTime(updatedTask.getScheduledTime());
            }
            if (updatedTask.getEstimatedDuration() != null) {
                existingTask.setEstimatedDuration(updatedTask.getEstimatedDuration());
            }
            if (updatedTask.getProgressPercentage() != null) {
                existingTask.setProgressPercentage(updatedTask.getProgressPercentage());
            }
            if (updatedTask.getInstructions() != null) {
                existingTask.setInstructions(updatedTask.getInstructions());
            }
            if (updatedTask.getNotes() != null) {
                existingTask.setNotes(updatedTask.getNotes());
            }
            if (updatedTask.getToolsRequired() != null) {
                existingTask.setToolsRequired(updatedTask.getToolsRequired());
            }
            if (updatedTask.getAssignedTo() != null) {
                existingTask.setAssignedTo(updatedTask.getAssignedTo());
            }
            if (updatedTask.getDueDate() != null) {
                existingTask.setDueDate(updatedTask.getDueDate());
            }
            
            // Validate dates after all updates are applied
            validateTaskDates(existingTask);
            
            return taskRepository.save(existingTask);
        }
        return null;
    }
    
    // Start task (change status to in_progress)
    public Task startTask(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatus("in_progress");
            task.setStartedAt(DateTimeUtils.nowUtc());
            if (task.getProgressPercentage() == null || task.getProgressPercentage() == 0) {
                task.setProgressPercentage(0);
            }
            return taskRepository.save(task);
        }
        return null;
    }
    
    // Complete task (change status to completed)
    public Task completeTask(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatus("completed");
            task.setCompletedAt(DateTimeUtils.nowUtc());
            task.setProgressPercentage(100);
            
            // Calculate actual duration if started
            if (task.getStartedAt() != null) {
                java.time.Duration duration = java.time.Duration.between(task.getStartedAt(), task.getCompletedAt());
                long seconds = duration.getSeconds();
                // Round up to nearest minute (e.g., 37 seconds = 1 minute)
                int minutes = (int) Math.ceil(seconds / 60.0);
                task.setActualDuration(Math.max(minutes, 1)); // Minimum 1 minute
            }
            
            return taskRepository.save(task);
        }
        return null;
    }

    // Complete task with additional data (photos and notes)
    public Task completeTaskWithData(Long taskId, String completionNotes, List<Map<String, String>> photos) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatus("completed");
            task.setCompletedAt(DateTimeUtils.nowUtc());
            task.setProgressPercentage(100);

            // Update completion notes if provided
            if (completionNotes != null && !completionNotes.trim().isEmpty()) {
                task.setCompletionNotes(completionNotes);
            }

            // Calculate actual duration if started
            if (task.getStartedAt() != null) {
                java.time.Duration duration = java.time.Duration.between(task.getStartedAt(), task.getCompletedAt());
                long seconds = duration.getSeconds();
                // Round up to nearest minute (e.g., 37 seconds = 1 minute)
                int minutes = (int) Math.ceil(seconds / 60.0);
                task.setActualDuration(Math.max(minutes, 1)); // Minimum 1 minute
            }

            return taskRepository.save(task);
        }
        return null;
    }

    // Undo task completion (revert to in_progress)
    public Task undoTaskCompletion(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            
            // Only allow undo if task was recently completed (within last 30 minutes)
            if (task.getStatus().equals("completed") && task.getCompletedAt() != null) {
                LocalDateTime thirtyMinutesAgo = DateTimeUtils.nowUtc().minusMinutes(30);
                if (task.getCompletedAt().isAfter(thirtyMinutesAgo)) {
                    task.setStatus("in_progress");
                    task.setCompletedAt(null);
                    // Keep the progress but don't set it to 100%
                    if (task.getProgressPercentage() == 100) {
                        task.setProgressPercentage(80); // Set to a reasonable in-progress value
                    }
                    task.setActualDuration(null); // Clear actual duration
                    
                    // Add a note about the undo action
                    String undoNote = "\n\nTask completion was undone on " + DateTimeUtils.nowUtc().toString();
                    String existingNotes = task.getNotes() != null ? task.getNotes() : "";
                    task.setNotes(existingNotes + undoNote);
                    
                    return taskRepository.save(task);
                }
            }
        }
        return null;
    }
    
    
    // Assign task to janitor
    public Task assignTask(Long taskId, Long janitorId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setAssignedTo(janitorId);
            return taskRepository.save(task);
        }
        return null;
    }
    
    // Delete task
    public boolean deleteTask(Long taskId) {
        if (taskRepository.existsById(taskId)) {
            taskRepository.deleteById(taskId);
            return true;
        }
        return false;
    }
    
    // Get task statistics
    public List<Object[]> getTaskStatistics() {
        return taskRepository.getTaskStatistics();
    }
    
    // Get task statistics for specific janitor
    public List<Object[]> getTaskStatisticsByJanitor(Long janitorId) {
        return taskRepository.getTaskStatisticsByJanitor(janitorId);
    }
    
    // Count tasks by janitor and status
    public Long countTasksByJanitorAndStatus(Long janitorId, String status) {
        return taskRepository.countTasksByJanitorAndStatus(janitorId, status);
    }
    
    // Update overdue tasks
    public void updateOverdueTasks() {
        List<Task> overdueTasks = taskRepository.findOverdueTasks(DateTimeUtils.nowUtc());
        for (Task task : overdueTasks) {
            if (!"completed".equals(task.getStatus())) {
                task.setStatus("overdue");
                taskRepository.save(task);
            }
        }
    }
    
    // Count methods for dashboard statistics
    public long countByStatus(String status) {
        return taskRepository.countByStatus(status);
    }
    
    public long countAllTasks() {
        return taskRepository.count();
    }
    
    // Get recent tasks (for dashboard)
    public List<Task> getRecentTasks(int limit) {
        return taskRepository.findTop5ByOrderByCreatedAtDesc();
    }
    
    // Get recent tasks by status (for dashboard activities)
    public List<Task> getRecentTasksByStatus(String status, int limit) {
        return taskRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Count overdue tasks (for dashboard alerts)
    public long countOverdueTasks() {
        return taskRepository.countOverdueTasks(DateTimeUtils.nowUtc());
    }
    
    // Advanced search with filtering and pagination
    public PagedTaskResponse searchTasksWithFilter(TaskFilterRequest filterRequest) {
        try {
            // Create Pageable object
            Sort.Direction direction = "ASC".equalsIgnoreCase(filterRequest.getSortDirection()) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, getSortField(filterRequest.getSortBy()));
            PageRequest pageRequest = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);
            
            // Execute search query
            Page<Task> taskPage = taskRepository.findTasksWithAdvancedFilter(
                filterRequest.getSearchTerm(),
                filterRequest.getStatuses(),
                filterRequest.getPriorities(),
                filterRequest.getAssignedTo(),
                filterRequest.getAssignedBy(),
                filterRequest.getLocation(),
                filterRequest.getScheduledStartDate(),
                filterRequest.getScheduledEndDate(),
                filterRequest.getCreatedStartDate(),
                filterRequest.getCreatedEndDate(),
                filterRequest.getMinProgress(),
                filterRequest.getMaxProgress(),
                filterRequest.getIsOverdue(),
                DateTimeUtils.nowUtc(),
                pageRequest
            );
            
            // Convert to summary response
            List<TaskSummaryResponse> taskSummaries = taskPage.getContent().stream()
                .map(this::convertToSummaryResponse)
                .collect(Collectors.toList());
            
            // Create paginated response
            PagedTaskResponse response = new PagedTaskResponse(
                taskSummaries,
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.getNumber(),
                taskPage.getSize()
            );
            
            // Add statistics for filtered results
            PagedTaskResponse.TaskStatsResponse stats = calculateFilteredStats(filterRequest);
            response.setStats(stats);
            
            return response;
            
        } catch (Exception e) {
            // Log error and return empty response
            System.err.println("Error in searchTasksWithFilter: " + e.getMessage());
            return new PagedTaskResponse(new ArrayList<>(), 0, 0, 0, filterRequest.getSize());
        }
    }
    
    // Batch operations
    @Transactional
    public Map<String, Object> performBatchOperation(BatchTaskRequest batchRequest) {
        Map<String, Object> result = new HashMap<>();
        
        if (!batchRequest.hasValidTaskIds()) {
            result.put("success", false);
            result.put("message", "Invalid task IDs provided");
            return result;
        }
        
        try {
            List<Task> tasks = taskRepository.findAllById(batchRequest.getTaskIds());
            int successCount = 0;
            List<String> errors = new ArrayList<>();
            
            for (Task task : tasks) {
                try {
                    switch (batchRequest.getOperation()) {
                        case "UPDATE_STATUS":
                            if (batchRequest.isValidStatusUpdate()) {
                                task.setStatus(batchRequest.getNewStatus());
                                taskRepository.save(task);
                                successCount++;
                            } else {
                                errors.add("Invalid status for task " + task.getTaskId());
                            }
                            break;
                            
                        case "REASSIGN":
                            if (batchRequest.isValidReassign()) {
                                task.setAssignedTo(batchRequest.getNewAssigneeId());
                                taskRepository.save(task);
                                successCount++;
                            } else {
                                errors.add("Invalid assignee for task " + task.getTaskId());
                            }
                            break;
                            
                        case "UPDATE_PRIORITY":
                            if (batchRequest.isValidPriorityUpdate()) {
                                task.setPriority(batchRequest.getNewPriority());
                                taskRepository.save(task);
                                successCount++;
                            } else {
                                errors.add("Invalid priority for task " + task.getTaskId());
                            }
                            break;
                            
                        case "DELETE":
                            if (batchRequest.isValidDelete()) {
                                taskRepository.delete(task);
                                successCount++;
                            }
                            break;
                            
                        default:
                            errors.add("Unknown operation: " + batchRequest.getOperation());
                    }
                } catch (Exception e) {
                    errors.add("Error processing task " + task.getTaskId() + ": " + e.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("successCount", successCount);
            result.put("totalRequested", batchRequest.getTaskIds().size());
            result.put("errors", errors);
            result.put("message", successCount + " tasks processed successfully");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Batch operation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    // Helper methods
    private TaskSummaryResponse convertToSummaryResponse(Task task) {
        // Get user names and avatar URLs
        String assignedToName = "Unassigned";
        String assignedToAvatarUrl = null;
        String assignedByName = "Unknown";
        String assignedByAvatarUrl = null;

        if (task.getAssignedTo() != null) {
            Optional<User> assignedUser = userRepository.findById(task.getAssignedTo());
            if (assignedUser.isPresent()) {
                User user = assignedUser.get();
                assignedToName = user.getFullName() != null ? user.getFullName() : user.getUsername();
                // Get avatar URL from images table
                assignedToAvatarUrl = getUserAvatarUrl(task.getAssignedTo());
            } else {
                assignedToName = "Unknown User";
            }
        }

        if (task.getAssignedBy() != null) {
            Optional<User> assignedByUser = userRepository.findById(task.getAssignedBy());
            if (assignedByUser.isPresent()) {
                User user = assignedByUser.get();
                assignedByName = user.getFullName() != null ? user.getFullName() : user.getUsername();
                // Get avatar URL from images table
                assignedByAvatarUrl = getUserAvatarUrl(task.getAssignedBy());
            } else {
                assignedByName = "Unknown Admin";
            }
        }

        return new TaskSummaryResponse(
            task.getTaskId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            task.getLocation(),
            task.getScheduledTime(),
            task.getDueDate(),
            task.getCreatedAt(),
            task.getProgressPercentage(),
            task.getEstimatedDuration(),
            task.getAssignedTo(),
            assignedToName,
            assignedToAvatarUrl,
            task.getAssignedBy(),
            assignedByName,
            assignedByAvatarUrl,
            task.getCompletedAt(),
            task.getActualDuration(),
            task.getCompletionNotes()
        );
    }

    // Helper method to get user avatar URL from user_profiles table
    private String getUserAvatarUrl(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            Optional<UserProfile> userProfile = userProfileRepository.findByUserId(userId);
            if (userProfile.isPresent() && userProfile.get().getAvatarUrl() != null) {
                return userProfile.get().getAvatarUrl();
            }
        } catch (Exception e) {
            System.err.println("Error fetching avatar for user " + userId + ": " + e.getMessage());
        }
        return null;
    }
    
    private PagedTaskResponse.TaskStatsResponse calculateFilteredStats(TaskFilterRequest filterRequest) {
        try {
            // Get status statistics
            List<Object[]> statusStats = taskRepository.getFilteredTaskStatsByStatus(
                filterRequest.getSearchTerm(),
                filterRequest.getStatuses(),
                filterRequest.getPriorities(),
                filterRequest.getAssignedTo(),
                filterRequest.getAssignedBy(),
                filterRequest.getLocation(),
                filterRequest.getScheduledStartDate(),
                filterRequest.getScheduledEndDate(),
                filterRequest.getCreatedStartDate(),
                filterRequest.getCreatedEndDate(),
                filterRequest.getMinProgress(),
                filterRequest.getMaxProgress(),
                filterRequest.getIsOverdue(),
                LocalDateTime.now()
            );
            
            // Get priority statistics
            List<Object[]> priorityStats = taskRepository.getFilteredTaskStatsByPriority(
                filterRequest.getSearchTerm(),
                filterRequest.getStatuses(),
                filterRequest.getPriorities(),
                filterRequest.getAssignedTo(),
                filterRequest.getAssignedBy(),
                filterRequest.getLocation(),
                filterRequest.getScheduledStartDate(),
                filterRequest.getScheduledEndDate(),
                filterRequest.getCreatedStartDate(),
                filterRequest.getCreatedEndDate(),
                filterRequest.getMinProgress(),
                filterRequest.getMaxProgress(),
                filterRequest.getIsOverdue(),
                LocalDateTime.now()
            );
            
            // Convert to stats object
            long totalTasks = 0;
            long pendingTasks = 0;
            long inProgressTasks = 0;
            long completedTasks = 0;
            long overdueTasks = 0;
            
            for (Object[] stat : statusStats) {
                String status = (String) stat[0];
                Long count = (Long) stat[1];
                totalTasks += count;
                
                switch (status.toLowerCase()) {
                    case "pending":
                        pendingTasks = count;
                        break;
                    case "in_progress":
                        inProgressTasks = count;
                        break;
                    case "completed":
                        completedTasks = count;
                        break;
                    case "overdue":
                        overdueTasks = count;
                        break;
                }
            }
            
            long highPriorityTasks = 0;
            long mediumPriorityTasks = 0;
            long lowPriorityTasks = 0;
            
            for (Object[] stat : priorityStats) {
                String priority = (String) stat[0];
                Long count = (Long) stat[1];
                
                switch (priority.toLowerCase()) {
                    case "high":
                    case "urgent":
                        highPriorityTasks = count;
                        break;
                    case "medium":
                    case "normal":
                        mediumPriorityTasks = count;
                        break;
                    case "low":
                        lowPriorityTasks = count;
                        break;
                }
            }
            
            return new PagedTaskResponse.TaskStatsResponse(
                totalTasks, pendingTasks, inProgressTasks, completedTasks,
                overdueTasks, highPriorityTasks, mediumPriorityTasks, lowPriorityTasks
            );
            
        } catch (Exception e) {
            System.err.println("Error calculating filtered stats: " + e.getMessage());
            return new PagedTaskResponse.TaskStatsResponse(0, 0, 0, 0, 0, 0, 0, 0);
        }
    }
    
    private String getSortField(String sortBy) {
        // Map frontend sort fields to entity fields
        switch (sortBy.toLowerCase()) {
            case "title":
                return "title";
            case "status":
                return "status";
            case "priority":
                return "priority";
            case "location":
                return "location";
            case "scheduledtime":
            case "scheduled_time":
                return "scheduledTime";
            case "duedate":
            case "due_date":
                return "dueDate";
            case "progress":
            case "progresspercentage":
                return "progressPercentage";
            case "assignedto":
            case "assigned_to":
                return "assignedTo";
            case "createdat":
            case "created_at":
            default:
                return "createdAt";
        }
    }
    
    // ===============================
    // PUBLIC TASK FUNCTIONALITY
    // ===============================
    
    // Get all public tasks (unassigned tasks that can be claimed)
    public List<Task> getPublicTasks() {
        // Public tasks are tasks where assignedTo is null and status is pending
        return taskRepository.findAll().stream()
            .filter(task -> task.getAssignedTo() == null && "pending".equals(task.getStatus()))
            .sorted((t1, t2) -> {
                // Sort by priority (urgent > high > normal > low) then by scheduled time
                Map<String, Integer> priorityOrder = Map.of(
                    "urgent", 4, "high", 3, "normal", 2, "low", 1
                );
                int priority1 = priorityOrder.getOrDefault(t1.getPriority(), 2);
                int priority2 = priorityOrder.getOrDefault(t2.getPriority(), 2);
                
                if (priority1 != priority2) {
                    return Integer.compare(priority2, priority1); // Descending priority
                }
                
                // If same priority, sort by scheduled time
                if (t1.getScheduledTime() != null && t2.getScheduledTime() != null) {
                    return t1.getScheduledTime().compareTo(t2.getScheduledTime());
                }
                
                return t1.getCreatedAt().compareTo(t2.getCreatedAt());
            })
            .collect(Collectors.toList());
    }
    
    // Claim a public task (assign it to a janitor)
    @Transactional
    public Task claimPublicTask(Long taskId, Long janitorId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found with ID: " + taskId);
        }
        
        Task task = taskOpt.get();
        
        // Verify this is a public task that can be claimed
        if (task.getAssignedTo() != null) {
            throw new IllegalArgumentException("Task is already assigned to another janitor");
        }
        
        if (!"pending".equals(task.getStatus())) {
            throw new IllegalArgumentException("Only pending tasks can be claimed");
        }
        
        // Assign the task to the janitor
        task.setAssignedTo(janitorId);
        task.setStatus("pending"); // Keep status as pending until started
        
        return taskRepository.save(task);
    }
    
    // Release a claimed task back to public pool
    @Transactional
    public Task releaseTaskToPublic(Long taskId, Long janitorId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found with ID: " + taskId);
        }
        
        Task task = taskOpt.get();
        
        // Verify this janitor owns the task
        if (!janitorId.equals(task.getAssignedTo())) {
            throw new IllegalArgumentException("You can only release tasks assigned to you");
        }
        
        // Can only release tasks that haven't been started
        if (!"pending".equals(task.getStatus())) {
            throw new IllegalArgumentException("Only pending tasks can be released back to public pool");
        }
        
        // Release the task back to public
        task.setAssignedTo(null);
        
        return taskRepository.save(task);
    }
    
    // Get tasks claimed by a specific janitor
    public List<Task> getClaimedTasksByJanitor(Long janitorId) {
        return taskRepository.findByAssignedToOrderByScheduledTimeAsc(janitorId);
    }
    
    // Get public task statistics
    public Map<String, Object> getPublicTaskStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Task> publicTasks = getPublicTasks();
        
        stats.put("totalPublicTasks", publicTasks.size());
        
        // Count by priority
        Map<String, Long> priorityStats = publicTasks.stream()
            .collect(Collectors.groupingBy(
                Task::getPriority,
                Collectors.counting()
            ));
        stats.put("publicTasksByPriority", priorityStats);
        
        // Count by location
        Map<String, Long> locationStats = publicTasks.stream()
            .collect(Collectors.groupingBy(
                Task::getLocation,
                Collectors.counting()
            ));
        stats.put("publicTasksByLocation", locationStats);
        
        // Count overdue public tasks
        LocalDateTime now = DateTimeUtils.nowUtc();
        long overduePublicTasks = publicTasks.stream()
            .filter(task -> task.getScheduledTime() != null && task.getScheduledTime().isBefore(now))
            .count();
        stats.put("overduePublicTasks", overduePublicTasks);
        
        return stats;
    }
    
    // Search public tasks
    public List<Task> searchPublicTasks(String searchTerm, String priority, String location) {
        List<Task> publicTasks = getPublicTasks();
        
        return publicTasks.stream()
            .filter(task -> {
                // Search term filter
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    String term = searchTerm.toLowerCase();
                    boolean matches = (task.getTitle() != null && task.getTitle().toLowerCase().contains(term)) ||
                                    (task.getDescription() != null && task.getDescription().toLowerCase().contains(term)) ||
                                    (task.getLocation() != null && task.getLocation().toLowerCase().contains(term));
                    if (!matches) return false;
                }
                
                // Priority filter
                if (priority != null && !priority.trim().isEmpty()) {
                    if (!priority.equals(task.getPriority())) return false;
                }
                
                // Location filter
                if (location != null && !location.trim().isEmpty()) {
                    if (task.getLocation() == null || !task.getLocation().toLowerCase().contains(location.toLowerCase())) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }
    
    // Check if a task can be claimed by a janitor
    public boolean canClaimTask(Long taskId, Long janitorId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        
        if (taskOpt.isEmpty()) {
            return false;
        }
        
        Task task = taskOpt.get();
        
        // Task must be unassigned and pending
        return task.getAssignedTo() == null && "pending".equals(task.getStatus());
    }
    
    // Get recently claimed tasks for activity feed
    public List<Task> getRecentlyClaimedTasks(int limit) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24); // Last 24 hours
        
        return taskRepository.findAll().stream()
            .filter(task -> task.getAssignedTo() != null)
            .filter(task -> task.getCreatedAt().isAfter(cutoff))
            .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Get task with associated images for detailed view
     */
    public Map<String, Object> getTaskWithImages(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            List<Image> images = imageService.getImagesForEntity(Image.EntityType.TASK_COMPLETION, taskId);
            
            // Convert to HashMap for safe JSON serialization
            Map<String, Object> taskData = convertTaskToMap(task);
            
            // Add image data
            List<Map<String, Object>> imageData = images.stream()
                .map(this::convertImageToMap)
                .toList();
            
            taskData.put("images", imageData);
            taskData.put("imageCount", images.size());
            
            return taskData;
        }
        return null;
    }
    
    /**
     * Complete task and update associated data - now integrates with image system
     */
    public Map<String, Object> completeTaskWithImages(Long taskId, String notes) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatus("completed");
            task.setCompletedAt(DateTimeUtils.nowUtc());
            task.setProgressPercentage(100);
            
            // Update notes if provided
            if (notes != null && !notes.trim().isEmpty()) {
                task.setNotes(notes);
            }
            
            // Calculate actual duration if started
            if (task.getStartedAt() != null) {
                java.time.Duration duration = java.time.Duration.between(task.getStartedAt(), task.getCompletedAt());
                long seconds = duration.getSeconds();
                // Round up to nearest minute (e.g., 37 seconds = 1 minute)
                int minutes = (int) Math.ceil(seconds / 60.0);
                task.setActualDuration(Math.max(minutes, 1)); // Minimum 1 minute
            }
            
            Task savedTask = taskRepository.save(task);
            
            // Get task with images for response
            return getTaskWithImages(taskId);
        }
        return null;
    }
    
    /**
     * Delete task and associated images
     */
    @Transactional
    public boolean deleteTaskWithImages(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            // Soft delete associated images first
            imageService.deleteImagesForEntity(Image.EntityType.TASK_COMPLETION, taskId);
            
            // Delete the task
            taskRepository.deleteById(taskId);
            return true;
        }
        return false;
    }
    
    // Helper methods for safe JSON serialization
    
    private Map<String, Object> convertTaskToMap(Task task) {
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", task.getTaskId());
        map.put("title", task.getTitle());
        map.put("description", task.getDescription());
        map.put("location", task.getLocation());
        map.put("status", task.getStatus());
        map.put("priority", task.getPriority());
        map.put("scheduledTime", task.getScheduledTime());
        map.put("estimatedDuration", task.getEstimatedDuration());
        map.put("actualDuration", task.getActualDuration());
        map.put("progressPercentage", task.getProgressPercentage());
        map.put("instructions", task.getInstructions());
        map.put("notes", task.getNotes());
        map.put("toolsRequired", task.getToolsRequired());
        map.put("assignedTo", task.getAssignedTo());
        map.put("assignedBy", task.getAssignedBy());
        map.put("createdAt", task.getCreatedAt());
        map.put("startedAt", task.getStartedAt());
        map.put("completedAt", task.getCompletedAt());
        map.put("dueDate", task.getDueDate());
        return map;
    }

    /**
     * Get all tasks created by a specific supervisor
     * This method allows supervisors to see their own created tasks including pending/unassigned ones
     */
    public List<Task> getTasksCreatedBy(Long supervisorId, String status, String priority, int limit, int offset) {
        try {
            List<Task> allTasks;

            // First get all tasks created by this supervisor (assignedBy = supervisorId)
            if (status != null && !status.equals("all")) {
                if (priority != null && !priority.equals("all")) {
                    // Filter by both status and priority
                    allTasks = taskRepository.findByAssignedByAndStatusAndPriorityOrderByScheduledTimeAsc(
                        supervisorId, status, priority);
                } else {
                    // Filter by status only
                    allTasks = taskRepository.findByAssignedByAndStatusOrderByScheduledTimeAsc(
                        supervisorId, status);
                }
            } else {
                if (priority != null && !priority.equals("all")) {
                    // Filter by priority only
                    allTasks = taskRepository.findByAssignedByAndPriorityOrderByScheduledTimeAsc(
                        supervisorId, priority);
                } else {
                    // No filters - get all tasks created by supervisor
                    allTasks = taskRepository.findByAssignedByOrderByScheduledTimeAsc(supervisorId);
                }
            }

            // Apply pagination manually (since we might need custom logic)
            int startIndex = offset;
            int endIndex = Math.min(startIndex + limit, allTasks.size());

            if (startIndex >= allTasks.size()) {
                return new ArrayList<>();
            }

            List<Task> paginatedTasks = allTasks.subList(startIndex, endIndex);

            // Note: assignedBy names are resolved in the controller layer

            return paginatedTasks;

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving tasks created by supervisor: " + e.getMessage(), e);
        }
    }


    private Map<String, Object> convertImageToMap(Image image) {
        Map<String, Object> map = new HashMap<>();
        map.put("imageId", image.getImageId());
        map.put("originalName", image.getOriginalName());
        map.put("storedName", image.getStoredName());
        map.put("publicUrl", image.getPublicUrl());
        map.put("fileSize", image.getFileSize());
        map.put("mimeType", image.getMimeType());
        map.put("createdAt", image.getCreatedAt());
        map.put("createdBy", image.getCreatedBy());
        return map;
    }
}