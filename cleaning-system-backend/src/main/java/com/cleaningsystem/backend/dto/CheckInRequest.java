package com.cleaningsystem.backend.dto;

public class CheckInRequest {
    
    private String location; // Optional: human-readable location name
    
    private String notes; // Optional notes from user
    
    // Default constructor
    public CheckInRequest() {}
    
    // Constructor with location
    public CheckInRequest(String location) {
        this.location = location;
    }
    
    // Constructor with all fields
    public CheckInRequest(String location, String notes) {
        this.location = location;
        this.notes = notes;
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
    
    @Override
    public String toString() {
        return "CheckInRequest{" +
                "location='" + location + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}