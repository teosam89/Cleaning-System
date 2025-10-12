package com.cleaningsystem.backend.dto;

public class CheckOutRequest {
    
    private String location; // Optional: human-readable location name
    
    private String notes; // Optional notes from user
    
    private Double breakTime; // Break time in hours (optional)
    
    // Default constructor
    public CheckOutRequest() {}
    
    // Constructor with location
    public CheckOutRequest(String location) {
        this.location = location;
    }
    
    // Constructor with all fields
    public CheckOutRequest(String location, String notes, Double breakTime) {
        this.location = location;
        this.notes = notes;
        this.breakTime = breakTime;
    }
    
    // Getters and Setters
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Double getBreakTime() {
        return breakTime;
    }
    
    public void setBreakTime(Double breakTime) {
        this.breakTime = breakTime;
    }
    
    @Override
    public String toString() {
        return "CheckOutRequest{" +
                "location='" + location + '\'' +
                ", notes='" + notes + '\'' +
                ", breakTime=" + breakTime +
                '}';
    }
}