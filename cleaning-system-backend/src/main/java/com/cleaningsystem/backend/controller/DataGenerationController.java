package com.cleaningsystem.backend.controller;

import com.cleaningsystem.backend.util.AttendanceDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for generating test/simulation data
 * This controller provides endpoints to programmatically insert attendance data
 * without relying on batch scripts or direct SQL execution.
 *
 * Endpoints:
 * - POST /api/data/generate-attendance - Generate and insert all attendance records
 * - GET /api/data/status - Check current data generation status
 */
@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*")
public class DataGenerationController {

    @Autowired
    private AttendanceDataGenerator attendanceDataGenerator;

    /**
     * Generate and insert all attendance data
     * POST /api/data/generate-attendance
     *
     * This endpoint triggers the generation of attendance records for janitors 194-197
     * for the period August-October 2025 (first 5 days of October)
     *
     * @return Response with number of records inserted and status
     */
    @PostMapping("/generate-attendance")
    public ResponseEntity<Map<String, Object>> generateAttendanceData() {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("===========================================");
            System.out.println("API Call: Generate Attendance Data");
            System.out.println("===========================================");

            int recordsInserted = attendanceDataGenerator.generateAndSaveAllAttendanceData();

            response.put("success", true);
            response.put("message", "Attendance data generated successfully");
            response.put("recordsInserted", recordsInserted);
            response.put("janitorIds", new Long[]{194L, 195L, 196L, 197L});
            response.put("dateRange", "2025-08-01 to 2025-10-05");

            System.out.println("API Response: Success - " + recordsInserted + " records inserted");
            System.out.println("===========================================");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("API Error: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Failed to generate attendance data");
            response.put("error", e.getMessage());
            response.put("recordsInserted", 0);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get status information about the data generation capability
     * GET /api/data/status
     *
     * @return Status information and available operations
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();

        response.put("available", true);
        response.put("message", "Data generation service is available");
        response.put("operations", new String[]{
            "POST /api/data/generate-attendance - Generate attendance records for janitors 194-197"
        });
        response.put("targetJanitors", new Long[]{194L, 195L, 196L, 197L});
        response.put("dateRange", "August 2025 - October 5, 2025");
        response.put("expectedRecords", "~226 attendance records");

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     * GET /api/data/health
     *
     * @return Simple health check response
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Data Generation Service");
        return ResponseEntity.ok(response);
    }
}
