package com.cleaningsystem.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * UserProfile Entity
 * Comprehensive profile information for users (especially janitors)
 * Extends the basic User entity with detailed profile data
 */
@Entity
@Table(name = "user_profiles")
public class UserProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    // Basic Information
    @Column(name = "employee_number", length = 20)
    private String employeeNumber;
    
    @Column(name = "gender", length = 10)
    private String gender;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;
    
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    @Column(name = "status", length = 20)
    private String status; // active, inactive, leave
    
    @Column(name = "join_date")
    private LocalDate joinDate;
    
    // Work Information
    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "work_area", length = 100)
    private String workArea;

    @Column(name = "work_hours")
    private Integer workHours;

    @Column(name = "rest_days", length = 100)
    private String restDays;
    
    // Performance Metrics (calculated from other tables)
    @Column(name = "monthly_attendance")
    private Integer monthlyAttendance;
    
    @Column(name = "task_completion_rate")
    private Integer taskCompletionRate;
    
    @Column(name = "quality_score")
    private Double qualityScore;
    
    @Column(name = "customer_satisfaction")
    private Integer customerSatisfaction;
    
    // Notification Preferences
    @Column(name = "task_notification")
    private Boolean taskNotification;

    @Column(name = "language", length = 10)
    private String language;
    
    // Timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public UserProfile() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "active";
        this.taskNotification = true;
        this.language = "en";
    }
    
    // Constructor with userId
    public UserProfile(Long userId) {
        this();
        this.userId = userId;
        this.joinDate = LocalDate.now();
    }
    
    // Getters and Setters
    public Long getProfileId() {
        return profileId;
    }
    
    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getEmployeeNumber() {
        return employeeNumber;
    }
    
    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getEmergencyContact() {
        return emergencyContact;
    }
    
    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDate getJoinDate() {
        return joinDate;
    }
    
    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public String getWorkArea() {
        return workArea;
    }

    public void setWorkArea(String workArea) {
        this.workArea = workArea;
    }

    public Integer getWorkHours() {
        return workHours;
    }

    public void setWorkHours(Integer workHours) {
        this.workHours = workHours;
    }
    
    public String getRestDays() {
        return restDays;
    }
    
    public void setRestDays(String restDays) {
        this.restDays = restDays;
    }
    
    public Integer getMonthlyAttendance() {
        return monthlyAttendance;
    }
    
    public void setMonthlyAttendance(Integer monthlyAttendance) {
        this.monthlyAttendance = monthlyAttendance;
    }
    
    public Integer getTaskCompletionRate() {
        return taskCompletionRate;
    }
    
    public void setTaskCompletionRate(Integer taskCompletionRate) {
        this.taskCompletionRate = taskCompletionRate;
    }
    
    public Double getQualityScore() {
        return qualityScore;
    }
    
    public void setQualityScore(Double qualityScore) {
        this.qualityScore = qualityScore;
    }
    
    public Integer getCustomerSatisfaction() {
        return customerSatisfaction;
    }
    
    public void setCustomerSatisfaction(Integer customerSatisfaction) {
        this.customerSatisfaction = customerSatisfaction;
    }
    
    public Boolean getTaskNotification() {
        return taskNotification;
    }

    public void setTaskNotification(Boolean taskNotification) {
        this.taskNotification = taskNotification;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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
    
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "UserProfile{" +
                "profileId=" + profileId +
                ", userId=" + userId +
                ", employeeNumber='" + employeeNumber + '\'' +
                ", position='" + position + '\'' +
                ", status='" + status + '\'' +
                ", joinDate=" + joinDate +
                ", createdAt=" + createdAt +
                '}';
    }
}