package com.cleaningsystem.backend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks", indexes = {
    // Index for text search across title and location only (excluding TEXT column description)
    @Index(name = "idx_task_search_text", columnList = "title, location"),
    
    // Index for status and priority filtering
    @Index(name = "idx_task_status_priority", columnList = "status, priority"),
    
    // Index for assigned users
    @Index(name = "idx_task_assigned_users", columnList = "assigned_to, assigned_by"),
    
    // Index for scheduled time queries
    @Index(name = "idx_task_scheduled_time", columnList = "scheduled_time"),
    
    // Index for created_at queries
    @Index(name = "idx_task_created_at", columnList = "created_at"),
    
    // Index for due_date queries
    @Index(name = "idx_task_due_date", columnList = "due_date"),
    
    // Index for progress queries
    @Index(name = "idx_task_progress", columnList = "progress_percentage"),
    
    // Composite index for overdue task queries
    @Index(name = "idx_task_overdue", columnList = "scheduled_time, status, due_date"),
    
    // Composite index for status and scheduled time
    @Index(name = "idx_task_status_scheduled", columnList = "status, scheduled_time"),
    
    // Composite index for assigned_to and status (janitor queries)
    @Index(name = "idx_task_assigned_status", columnList = "assigned_to, status"),
    
    // Composite index for priority and status
    @Index(name = "idx_task_priority_status", columnList = "priority, status"),
    
    // Index for location-based searches
    @Index(name = "idx_task_location", columnList = "location"),
    
    // Admin dashboard composite index
    @Index(name = "idx_task_admin_dashboard", columnList = "status, priority, created_at"),
    
    // Calendar queries composite index
    @Index(name = "idx_task_calendar", columnList = "scheduled_time, status, assigned_to")
})
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")  
    private Long taskId;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, length = 200)
    private String location;
    
    @Column(nullable = false, length = 20)
    private String status; // pending, in_progress, completed, overdue
    
    @Column(nullable = false, length = 20)  
    private String priority; // low, normal, high, urgent
    
    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;
    
    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // in minutes
    
    @Column(name = "actual_duration")
    private Integer actualDuration; // in minutes
    
    @Column(name = "progress_percentage")
    private Integer progressPercentage; // 0-100
    
    @Column(columnDefinition = "TEXT")
    private String instructions;
    
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "completion_notes", columnDefinition = "TEXT")
    private String completionNotes;
    
    @Column(name = "tools_required", columnDefinition = "TEXT")
    private String toolsRequired; // JSON array as string
    
    @Column(name = "assigned_to")
    private Long assignedTo; // references User.userId (janitor)
    
    @Column(name = "assigned_by")  
    private Long assignedBy; // references User.userId (admin)
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    // Relationships - JsonIgnore prevents Jackson serialization issues with Hibernate proxies
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", insertable = false, updatable = false)
    @JsonIgnore
    private User janitor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", insertable = false, updatable = false)
    @JsonIgnore
    private User admin;
    
    // Default constructor
    public Task() {
        this.createdAt = LocalDateTime.now();
        this.status = "pending";
        this.progressPercentage = 0;
    }
    
    // Constructor with basic parameters
    public Task(String title, String description, String location, String priority, 
                LocalDateTime scheduledTime, Integer estimatedDuration, Long assignedTo, Long assignedBy) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.priority = priority;
        this.scheduledTime = scheduledTime;
        this.estimatedDuration = estimatedDuration;
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
        this.createdAt = LocalDateTime.now();
        this.status = "pending";
        this.progressPercentage = 0;
    }
    
    // Getters and Setters
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
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }
    
    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    
    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }
    
    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }
    
    public Integer getActualDuration() {
        return actualDuration;
    }
    
    public void setActualDuration(Integer actualDuration) {
        this.actualDuration = actualDuration;
    }
    
    public Integer getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCompletionNotes() {
        return completionNotes;
    }

    public void setCompletionNotes(String completionNotes) {
        this.completionNotes = completionNotes;
    }
    
    public String getToolsRequired() {
        return toolsRequired;
    }
    
    public void setToolsRequired(String toolsRequired) {
        this.toolsRequired = toolsRequired;
    }
    
    public Long getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public Long getAssignedBy() {
        return assignedBy;
    }
    
    public void setAssignedBy(Long assignedBy) {
        this.assignedBy = assignedBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public User getJanitor() {
        return janitor;
    }
    
    public void setJanitor(User janitor) {
        this.janitor = janitor;
    }
    
    public User getAdmin() {
        return admin;
    }
    
    public void setAdmin(User admin) {
        this.admin = admin;
    }
    
    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", assignedTo=" + assignedTo +
                ", assignedBy=" + assignedBy +
                ", progressPercentage=" + progressPercentage +
                ", createdAt=" + createdAt +
                '}';
    }
}