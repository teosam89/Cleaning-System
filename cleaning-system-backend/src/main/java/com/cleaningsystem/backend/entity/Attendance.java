package com.cleaningsystem.backend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long attendanceId;

    @Column(name = "janitor_id", nullable = false)
    private Long janitorId; // references User.userId

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "work_hours")
    private Double workHours; // calculated work hours

    // Relationship
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "janitor_id", insertable = false, updatable = false)
    private User janitor;

    // Default constructor
    public Attendance() {
        this.workDate = LocalDate.now();
        this.workHours = 0.0;
    }

    // Constructor for check-in
    public Attendance(Long janitorId, LocalDateTime checkInTime) {
        this.janitorId = janitorId;
        this.checkInTime = checkInTime;
        this.workDate = checkInTime.toLocalDate();
        this.workHours = 0.0;
    }

    // Getters and Setters
    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Long getJanitorId() {
        return janitorId;
    }

    public void setJanitorId(Long janitorId) {
        this.janitorId = janitorId;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public Double getWorkHours() {
        return workHours;
    }

    public void setWorkHours(Double workHours) {
        this.workHours = workHours;
    }

    public User getJanitor() {
        return janitor;
    }

    public void setJanitor(User janitor) {
        this.janitor = janitor;
    }

    // Helper method to calculate work hours
    public void calculateWorkHours() {
        if (checkInTime != null && checkOutTime != null) {
            java.time.Duration duration = java.time.Duration.between(checkInTime, checkOutTime);
            this.workHours = duration.toMinutes() / 60.0;
        }
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "attendanceId=" + attendanceId +
                ", janitorId=" + janitorId +
                ", workDate=" + workDate +
                ", checkInTime=" + checkInTime +
                ", checkOutTime=" + checkOutTime +
                ", workHours=" + workHours +
                '}';
    }
}