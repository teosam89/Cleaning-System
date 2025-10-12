package com.cleaningsystem.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Data Transfer Object for creating new tasks
 */
public class CreateTaskRequest {
    
    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    @NotBlank(message = "Task description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;
    
    @NotNull(message = "Assigned user ID is required")
    private Long assignedToId;
    
    @NotBlank(message = "Priority is required")
    @Pattern(regexp = "low|medium|high|urgent", message = "Priority must be one of: low, medium, high, urgent")
    private String priority;
    
    @NotBlank(message = "Location is required")
    @Size(min = 3, max = 200, message = "Location must be between 3 and 200 characters")
    private String location;
    
    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
    
    // Constructors
    public CreateTaskRequest() {}
    
    public CreateTaskRequest(String title, String description, Long assignedToId, 
                           String priority, String location, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.assignedToId = assignedToId;
        this.priority = priority;
        this.location = location;
        this.dueDate = dueDate;
    }
    
    // Getters and Setters
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
    
    public Long getAssignedToId() {
        return assignedToId;
    }
    
    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    @Override
    public String toString() {
        return "CreateTaskRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", assignedToId=" + assignedToId +
                ", priority='" + priority + '\'' +
                ", location='" + location + '\'' +
                ", dueDate=" + dueDate +
                '}';
    }
}