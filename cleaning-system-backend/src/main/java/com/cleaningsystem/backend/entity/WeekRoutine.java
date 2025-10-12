package com.cleaningsystem.backend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "week_routines", indexes = {
    // Index for active status queries
    @Index(name = "idx_routine_active", columnList = "active"),
    
    // Index for created_by queries (admin queries)
    @Index(name = "idx_routine_created_by", columnList = "created_by"),
    
    // Index for week_days queries (day-specific searches)
    @Index(name = "idx_routine_week_days", columnList = "week_days"),
    
    // Index for task_type queries (public vs assigned)
    @Index(name = "idx_routine_task_type", columnList = "task_type"),
    
    // Composite index for active routines by type
    @Index(name = "idx_routine_active_type", columnList = "active, task_type"),
    
    // Index for priority queries
    @Index(name = "idx_routine_priority", columnList = "priority"),
    
    // Index for location-based searches
    @Index(name = "idx_routine_location", columnList = "location"),
    
    // Composite index for admin dashboard queries
    @Index(name = "idx_routine_admin_dashboard", columnList = "created_by, active, created_at")
})
public class WeekRoutine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "routine_id")
    private Long routineId;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, length = 200)
    private String location;
    
    @Column(nullable = false, length = 20)
    private String priority; // low, normal, high, urgent
    
    @Column(name = "week_days", nullable = false, length = 20)
    private String weekDays; // Comma-separated values: "1,3,5" for Mon,Wed,Fri
    
    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime; // Time of day for the task (e.g., 09:00)
    
    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // Duration in minutes
    
    @Column(columnDefinition = "TEXT")
    private String instructions;
    
    @Column(name = "tools_required", columnDefinition = "TEXT")
    private String toolsRequired; // JSON array as string
    
    @Column(name = "task_type", nullable = false, length = 20)
    private String taskType; // "assigned" or "public"
    
    @Column(name = "assigned_to")
    private Long assignedTo; // For assigned tasks, references User.userId (janitor)
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy; // References User.userId (admin who created this routine)
    
    @Column(nullable = false)
    private Boolean active; // Whether this routine is currently active
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_generated")
    private LocalDateTime lastGenerated; // Last time tasks were generated from this routine
    
    // Relationships - JsonIgnore prevents Jackson serialization issues
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", insertable = false, updatable = false)
    @JsonIgnore
    private User assignedJanitor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    @JsonIgnore
    private User admin;
    
    // Default constructor
    public WeekRoutine() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
        this.taskType = "public"; // Default to public tasks
    }
    
    // Constructor with basic parameters
    public WeekRoutine(String title, String description, String location, String priority,
                      String weekDays, LocalTime scheduledTime, Integer estimatedDuration,
                      String taskType, Long assignedTo, Long createdBy) {
        this();
        this.title = title;
        this.description = description;
        this.location = location;
        this.priority = priority;
        this.weekDays = weekDays;
        this.scheduledTime = scheduledTime;
        this.estimatedDuration = estimatedDuration;
        this.taskType = taskType;
        this.assignedTo = assignedTo;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public Long getRoutineId() {
        return routineId;
    }
    
    public void setRoutineId(Long routineId) {
        this.routineId = routineId;
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
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getWeekDays() {
        return weekDays;
    }
    
    public void setWeekDays(String weekDays) {
        this.weekDays = weekDays;
    }
    
    public LocalTime getScheduledTime() {
        return scheduledTime;
    }
    
    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    
    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }
    
    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public String getToolsRequired() {
        return toolsRequired;
    }
    
    public void setToolsRequired(String toolsRequired) {
        this.toolsRequired = toolsRequired;
    }
    
    public String getTaskType() {
        return taskType;
    }
    
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    
    public Long getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLastGenerated() {
        return lastGenerated;
    }
    
    public void setLastGenerated(LocalDateTime lastGenerated) {
        this.lastGenerated = lastGenerated;
    }
    
    public User getAssignedJanitor() {
        return assignedJanitor;
    }
    
    public void setAssignedJanitor(User assignedJanitor) {
        this.assignedJanitor = assignedJanitor;
    }
    
    public User getAdmin() {
        return admin;
    }
    
    public void setAdmin(User admin) {
        this.admin = admin;
    }
    
    // Helper methods for working with week days
    public boolean isScheduledForDay(int dayOfWeek) {
        if (weekDays == null) return false;
        return weekDays.contains(String.valueOf(dayOfWeek));
    }
    
    public int[] getWeekDaysArray() {
        if (weekDays == null || weekDays.isEmpty()) {
            return new int[0];
        }
        
        String[] dayStrings = weekDays.split(",");
        int[] days = new int[dayStrings.length];
        
        for (int i = 0; i < dayStrings.length; i++) {
            try {
                days[i] = Integer.parseInt(dayStrings[i].trim());
            } catch (NumberFormatException e) {
                // Skip invalid day numbers
            }
        }
        
        return days;
    }
    
    // Update the updatedAt timestamp when saving
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "WeekRoutine{" +
                "routineId=" + routineId +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", priority='" + priority + '\'' +
                ", weekDays='" + weekDays + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", taskType='" + taskType + '\'' +
                ", assignedTo=" + assignedTo +
                ", createdBy=" + createdBy +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}