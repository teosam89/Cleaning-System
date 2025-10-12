package com.cleaningsystem.backend.dto;

import java.time.LocalDateTime;

public class AttendanceStatusResponse {
    
    private boolean checkedIn;
    private LocalDateTime checkInTime;
    private String checkInLocation;
    private LocalDateTime checkOutTime;
    private String checkOutLocation;
    private Double currentWorkHours;
    private String status;
    private String message;
    
    // Default constructor
    public AttendanceStatusResponse() {}
    
    // Constructor for checked in status
    public AttendanceStatusResponse(boolean checkedIn, LocalDateTime checkInTime, String checkInLocation,
                                  Double currentWorkHours, String status) {
        this.checkedIn = checkedIn;
        this.checkInTime = checkInTime;
        this.checkInLocation = checkInLocation;
        this.currentWorkHours = currentWorkHours;
        this.status = status;
    }
    
    // Constructor for not checked in
    public AttendanceStatusResponse(boolean checkedIn, String message) {
        this.checkedIn = checkedIn;
        this.message = message;
    }
    
    // Static factory method for checked in response
    public static AttendanceStatusResponse checkedIn(LocalDateTime checkInTime, String checkInLocation,
                                                   Double currentWorkHours, String status) {
        return new AttendanceStatusResponse(true, checkInTime, checkInLocation, currentWorkHours, status);
    }
    
    // Static factory method for not checked in response
    public static AttendanceStatusResponse notCheckedIn(String message) {
        return new AttendanceStatusResponse(false, message);
    }
    
    // Getters and Setters
    public boolean isCheckedIn() {
        return checkedIn;
    }
    
    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
    
    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }
    
    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }
    
    public String getCheckInLocation() {
        return checkInLocation;
    }
    
    public void setCheckInLocation(String checkInLocation) {
        this.checkInLocation = checkInLocation;
    }
    
    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }
    
    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }
    
    public String getCheckOutLocation() {
        return checkOutLocation;
    }
    
    public void setCheckOutLocation(String checkOutLocation) {
        this.checkOutLocation = checkOutLocation;
    }
    
    public Double getCurrentWorkHours() {
        return currentWorkHours;
    }
    
    public void setCurrentWorkHours(Double currentWorkHours) {
        this.currentWorkHours = currentWorkHours;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}