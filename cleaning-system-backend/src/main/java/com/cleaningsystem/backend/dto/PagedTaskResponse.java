package com.cleaningsystem.backend.dto;

import java.util.List;

/**
 * DTO for paginated task responses with metadata
 */
public class PagedTaskResponse {
    
    private List<TaskSummaryResponse> tasks;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    
    // Task statistics for current filter
    private TaskStatsResponse stats;
    
    // Constructors
    public PagedTaskResponse() {}
    
    public PagedTaskResponse(List<TaskSummaryResponse> tasks, long totalElements, 
                           int totalPages, int currentPage, int pageSize) {
        this.tasks = tasks;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
    }
    
    // Getters and setters
    public List<TaskSummaryResponse> getTasks() {
        return tasks;
    }
    
    public void setTasks(List<TaskSummaryResponse> tasks) {
        this.tasks = tasks;
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    public TaskStatsResponse getStats() {
        return stats;
    }
    
    public void setStats(TaskStatsResponse stats) {
        this.stats = stats;
    }
    
    /**
     * Inner class for task statistics
     */
    public static class TaskStatsResponse {
        private long totalTasks;
        private long pendingTasks;
        private long inProgressTasks;
        private long completedTasks;
        private long overdueTasks;
        private long highPriorityTasks;
        private long mediumPriorityTasks;
        private long lowPriorityTasks;
        
        // Constructors
        public TaskStatsResponse() {}
        
        public TaskStatsResponse(long totalTasks, long pendingTasks, long inProgressTasks,
                               long completedTasks, long overdueTasks, long highPriorityTasks,
                               long mediumPriorityTasks, long lowPriorityTasks) {
            this.totalTasks = totalTasks;
            this.pendingTasks = pendingTasks;
            this.inProgressTasks = inProgressTasks;
            this.completedTasks = completedTasks;
            this.overdueTasks = overdueTasks;
            this.highPriorityTasks = highPriorityTasks;
            this.mediumPriorityTasks = mediumPriorityTasks;
            this.lowPriorityTasks = lowPriorityTasks;
        }
        
        // Getters and setters
        public long getTotalTasks() {
            return totalTasks;
        }
        
        public void setTotalTasks(long totalTasks) {
            this.totalTasks = totalTasks;
        }
        
        public long getPendingTasks() {
            return pendingTasks;
        }
        
        public void setPendingTasks(long pendingTasks) {
            this.pendingTasks = pendingTasks;
        }
        
        public long getInProgressTasks() {
            return inProgressTasks;
        }
        
        public void setInProgressTasks(long inProgressTasks) {
            this.inProgressTasks = inProgressTasks;
        }
        
        public long getCompletedTasks() {
            return completedTasks;
        }
        
        public void setCompletedTasks(long completedTasks) {
            this.completedTasks = completedTasks;
        }
        
        public long getOverdueTasks() {
            return overdueTasks;
        }
        
        public void setOverdueTasks(long overdueTasks) {
            this.overdueTasks = overdueTasks;
        }
        
        public long getHighPriorityTasks() {
            return highPriorityTasks;
        }
        
        public void setHighPriorityTasks(long highPriorityTasks) {
            this.highPriorityTasks = highPriorityTasks;
        }
        
        public long getMediumPriorityTasks() {
            return mediumPriorityTasks;
        }
        
        public void setMediumPriorityTasks(long mediumPriorityTasks) {
            this.mediumPriorityTasks = mediumPriorityTasks;
        }
        
        public long getLowPriorityTasks() {
            return lowPriorityTasks;
        }
        
        public void setLowPriorityTasks(long lowPriorityTasks) {
            this.lowPriorityTasks = lowPriorityTasks;
        }
    }
}