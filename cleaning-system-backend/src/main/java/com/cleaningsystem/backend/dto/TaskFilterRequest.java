package com.cleaningsystem.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for advanced task filtering and pagination requests
 */
public class TaskFilterRequest {
    
    // Pagination parameters
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC"; // ASC or DESC
    
    // Filter parameters
    private String searchTerm; // Search in title, description, location
    private List<String> statuses; // Multiple status filter
    private List<String> priorities; // Multiple priority filter
    private List<Long> assignedTo; // Multiple janitor filter
    private List<Long> assignedBy; // Multiple admin filter
    private String location; // Location contains filter
    private LocalDateTime scheduledStartDate;
    private LocalDateTime scheduledEndDate;
    private LocalDateTime createdStartDate;
    private LocalDateTime createdEndDate;
    private Boolean isOverdue;
    private Integer minProgress;
    private Integer maxProgress;
    
    // Constructors
    public TaskFilterRequest() {}
    
    public TaskFilterRequest(int page, int size, String sortBy, String sortDirection) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }
    
    // Getters and setters
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
    
    public String getSearchTerm() {
        return searchTerm;
    }
    
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
    
    public List<String> getStatuses() {
        return statuses;
    }
    
    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }
    
    public List<String> getPriorities() {
        return priorities;
    }
    
    public void setPriorities(List<String> priorities) {
        this.priorities = priorities;
    }
    
    public List<Long> getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(List<Long> assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public List<Long> getAssignedBy() {
        return assignedBy;
    }
    
    public void setAssignedBy(List<Long> assignedBy) {
        this.assignedBy = assignedBy;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public LocalDateTime getScheduledStartDate() {
        return scheduledStartDate;
    }
    
    public void setScheduledStartDate(LocalDateTime scheduledStartDate) {
        this.scheduledStartDate = scheduledStartDate;
    }
    
    public LocalDateTime getScheduledEndDate() {
        return scheduledEndDate;
    }
    
    public void setScheduledEndDate(LocalDateTime scheduledEndDate) {
        this.scheduledEndDate = scheduledEndDate;
    }
    
    public LocalDateTime getCreatedStartDate() {
        return createdStartDate;
    }
    
    public void setCreatedStartDate(LocalDateTime createdStartDate) {
        this.createdStartDate = createdStartDate;
    }
    
    public LocalDateTime getCreatedEndDate() {
        return createdEndDate;
    }
    
    public void setCreatedEndDate(LocalDateTime createdEndDate) {
        this.createdEndDate = createdEndDate;
    }
    
    public Boolean getIsOverdue() {
        return isOverdue;
    }
    
    public void setIsOverdue(Boolean isOverdue) {
        this.isOverdue = isOverdue;
    }
    
    public Integer getMinProgress() {
        return minProgress;
    }
    
    public void setMinProgress(Integer minProgress) {
        this.minProgress = minProgress;
    }
    
    public Integer getMaxProgress() {
        return maxProgress;
    }
    
    public void setMaxProgress(Integer maxProgress) {
        this.maxProgress = maxProgress;
    }
}