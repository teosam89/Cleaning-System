package com.cleaningsystem.backend.dto;

import java.time.LocalDateTime;

/**
 * Optimized DTO for task list display with minimal data transfer
 */
public class TaskSummaryResponse {
    
    private Long taskId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String location;
    private LocalDateTime scheduledTime;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private Integer progressPercentage;
    private Integer estimatedDuration;

    // Completion info
    private LocalDateTime completedAt;
    private Integer actualDuration;
    private String completionNotes;

    // Assignee info
    private Long assignedToId;
    private String assignedToName;
    private String assignedToAvatarUrl;

    // Admin info
    private Long assignedById;
    private String assignedByName;
    private String assignedByAvatarUrl;
    
    // Computed fields
    private Boolean isOverdue;
    private String statusDisplay;
    private String priorityDisplay;
    
    // Constructors
    public TaskSummaryResponse() {}
    
    public TaskSummaryResponse(Long taskId, String title, String description, String status,
                              String priority, String location, LocalDateTime scheduledTime,
                              LocalDateTime dueDate, LocalDateTime createdAt, Integer progressPercentage,
                              Integer estimatedDuration, Long assignedToId, String assignedToName,
                              String assignedToAvatarUrl, Long assignedById, String assignedByName,
                              String assignedByAvatarUrl, LocalDateTime completedAt,
                              Integer actualDuration, String completionNotes) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.location = location;
        this.scheduledTime = scheduledTime;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.progressPercentage = progressPercentage;
        this.estimatedDuration = estimatedDuration;
        this.assignedToId = assignedToId;
        this.assignedToName = assignedToName;
        this.assignedToAvatarUrl = assignedToAvatarUrl;
        this.assignedById = assignedById;
        this.assignedByName = assignedByName;
        this.assignedByAvatarUrl = assignedByAvatarUrl;
        this.completedAt = completedAt;
        this.actualDuration = actualDuration;
        this.completionNotes = completionNotes;

        // Calculate computed fields
        this.isOverdue = calculateIsOverdue();
        this.statusDisplay = formatStatusDisplay();
        this.priorityDisplay = formatPriorityDisplay();
    }
    
    // Helper methods
    private Boolean calculateIsOverdue() {
        if (scheduledTime != null && !isCompleted()) {
            return LocalDateTime.now().isAfter(scheduledTime);
        }
        return false;
    }
    
    private boolean isCompleted() {
        return "completed".equalsIgnoreCase(status);
    }
    
    private String formatStatusDisplay() {
        if (status == null) return "Unknown";
        return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
    }
    
    private String formatPriorityDisplay() {
        if (priority == null) return "Normal";
        return priority.substring(0, 1).toUpperCase() + priority.substring(1).toLowerCase();
    }
    
    // Getters and setters
    public Long getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        this.statusDisplay = formatStatusDisplay();
        this.isOverdue = calculateIsOverdue();
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
        this.priorityDisplay = formatPriorityDisplay();
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }
    
    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
        this.isOverdue = calculateIsOverdue();
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Integer getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }
    
    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }
    
    public Long getAssignedToId() {
        return assignedToId;
    }
    
    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }
    
    public String getAssignedToName() {
        return assignedToName;
    }
    
    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }
    
    public Long getAssignedById() {
        return assignedById;
    }
    
    public void setAssignedById(Long assignedById) {
        this.assignedById = assignedById;
    }
    
    public String getAssignedByName() {
        return assignedByName;
    }
    
    public void setAssignedByName(String assignedByName) {
        this.assignedByName = assignedByName;
    }
    
    public Boolean getIsOverdue() {
        return isOverdue;
    }
    
    public void setIsOverdue(Boolean isOverdue) {
        this.isOverdue = isOverdue;
    }
    
    public String getStatusDisplay() {
        return statusDisplay;
    }
    
    public void setStatusDisplay(String statusDisplay) {
        this.statusDisplay = statusDisplay;
    }
    
    public String getPriorityDisplay() {
        return priorityDisplay;
    }
    
    public void setPriorityDisplay(String priorityDisplay) {
        this.priorityDisplay = priorityDisplay;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getActualDuration() {
        return actualDuration;
    }

    public void setActualDuration(Integer actualDuration) {
        this.actualDuration = actualDuration;
    }

    public String getCompletionNotes() {
        return completionNotes;
    }

    public void setCompletionNotes(String completionNotes) {
        this.completionNotes = completionNotes;
    }

    public String getAssignedToAvatarUrl() {
        return assignedToAvatarUrl;
    }

    public void setAssignedToAvatarUrl(String assignedToAvatarUrl) {
        this.assignedToAvatarUrl = assignedToAvatarUrl;
    }

    public String getAssignedByAvatarUrl() {
        return assignedByAvatarUrl;
    }

    public void setAssignedByAvatarUrl(String assignedByAvatarUrl) {
        this.assignedByAvatarUrl = assignedByAvatarUrl;
    }
}