package com.cleaningsystem.backend.dto;

import java.util.List;

/**
 * DTO for batch task operations
 */
public class BatchTaskRequest {
    
    private List<Long> taskIds;
    private String operation; // UPDATE_STATUS, REASSIGN, DELETE, UPDATE_PRIORITY
    private String newStatus;
    private String newPriority;
    private Long newAssigneeId;
    private String reason; // Optional reason for audit log
    
    // Constructors
    public BatchTaskRequest() {}
    
    public BatchTaskRequest(List<Long> taskIds, String operation) {
        this.taskIds = taskIds;
        this.operation = operation;
    }
    
    // Getters and setters
    public List<Long> getTaskIds() {
        return taskIds;
    }
    
    public void setTaskIds(List<Long> taskIds) {
        this.taskIds = taskIds;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    public String getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
    
    public String getNewPriority() {
        return newPriority;
    }
    
    public void setNewPriority(String newPriority) {
        this.newPriority = newPriority;
    }
    
    public Long getNewAssigneeId() {
        return newAssigneeId;
    }
    
    public void setNewAssigneeId(Long newAssigneeId) {
        this.newAssigneeId = newAssigneeId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    // Validation methods
    public boolean isValidStatusUpdate() {
        return "UPDATE_STATUS".equals(operation) && newStatus != null && !newStatus.trim().isEmpty();
    }
    
    public boolean isValidReassign() {
        return "REASSIGN".equals(operation) && newAssigneeId != null && newAssigneeId > 0;
    }
    
    public boolean isValidDelete() {
        return "DELETE".equals(operation);
    }
    
    public boolean isValidPriorityUpdate() {
        return "UPDATE_PRIORITY".equals(operation) && newPriority != null && !newPriority.trim().isEmpty();
    }
    
    public boolean hasValidTaskIds() {
        return taskIds != null && !taskIds.isEmpty() && taskIds.stream().allMatch(id -> id != null && id > 0);
    }
}