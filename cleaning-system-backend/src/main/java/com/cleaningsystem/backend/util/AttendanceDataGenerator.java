package com.cleaningsystem.backend.util;

import com.cleaningsystem.backend.entity.Attendance;
import com.cleaningsystem.backend.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to generate and insert attendance data for janitors
 * This class provides a programmatic way to simulate attendance records
 * for testing and demonstration purposes.
 *
 * Target janitor IDs: 194, 195, 196, 197
 * Time range: August 2025, September 2025, October 1-5, 2025
 *
 * Usage: Call generateAndSaveAllAttendanceData() to insert all records
 */
@Component
public class AttendanceDataGenerator {

    @Autowired
    private AttendanceRepository attendanceRepository;

    /**
     * Main method to generate and save all attendance data
     * @return Number of records inserted
     */
    public int generateAndSaveAllAttendanceData() {
        System.out.println("========================================");
        System.out.println("Starting Attendance Data Generation");
        System.out.println("========================================");

        // Delete existing data for these janitors in the date range
        deleteExistingData();

        List<Attendance> allRecords = new ArrayList<>();

        // Generate data for each janitor
        allRecords.addAll(generateJanitor194Data());
        allRecords.addAll(generateJanitor195Data());
        allRecords.addAll(generateJanitor196Data());
        allRecords.addAll(generateJanitor197Data());

        // Save all records
        List<Attendance> savedRecords = attendanceRepository.saveAll(allRecords);

        System.out.println("Total records inserted: " + savedRecords.size());
        System.out.println("========================================");
        System.out.println("Data Generation Complete!");
        System.out.println("========================================");

        return savedRecords.size();
    }

    /**
     * Delete existing attendance data for janitors 194-197 in the target date range
     */
    private void deleteExistingData() {
        LocalDate startDate = LocalDate.of(2025, 8, 1);
        LocalDate endDate = LocalDate.of(2025, 10, 5);

        for (Long janitorId = 194L; janitorId <= 197L; janitorId++) {
            List<Attendance> existingRecords = attendanceRepository
                .findByJanitorIdAndWorkDateBetween(janitorId, startDate, endDate);
            if (!existingRecords.isEmpty()) {
                attendanceRepository.deleteAll(existingRecords);
                System.out.println("Deleted " + existingRecords.size() + " existing records for janitor " + janitorId);
            }
        }
    }

    /**
     * Generate attendance data for Janitor 194
     * Attendance rate: ~85% (occasional absences, work hours 7-9)
     */
    private List<Attendance> generateJanitor194Data() {
        List<Attendance> records = new ArrayList<>();
        Long janitorId = 194L;

        // August 2025
        records.add(createRecord(janitorId, "2025-08-01", "08:05:00", "17:10:00", 9.08));
        records.add(createRecord(janitorId, "2025-08-02", "08:00:00", "16:45:00", 8.75));
        // Aug 3 - absent
        records.add(createRecord(janitorId, "2025-08-04", "08:10:00", "17:00:00", 8.83));
        records.add(createRecord(janitorId, "2025-08-05", "07:55:00", "16:50:00", 8.92));
        records.add(createRecord(janitorId, "2025-08-06", "08:15:00", "17:05:00", 8.83));
        records.add(createRecord(janitorId, "2025-08-07", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-08", "08:20:00", "16:30:00", 8.17));
        records.add(createRecord(janitorId, "2025-08-09", "08:00:00", "17:15:00", 9.25));
        // Aug 10 - absent
        records.add(createRecord(janitorId, "2025-08-11", "08:05:00", "16:55:00", 8.83));
        records.add(createRecord(janitorId, "2025-08-12", "07:50:00", "17:00:00", 9.17));
        records.add(createRecord(janitorId, "2025-08-13", "08:00:00", "17:05:00", 9.08));
        records.add(createRecord(janitorId, "2025-08-14", "08:10:00", "16:40:00", 8.50));
        records.add(createRecord(janitorId, "2025-08-15", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-16", "08:05:00", "17:10:00", 9.08));
        // Aug 17 - absent
        records.add(createRecord(janitorId, "2025-08-18", "08:15:00", "16:50:00", 8.58));
        records.add(createRecord(janitorId, "2025-08-19", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-20", "07:55:00", "16:55:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-21", "08:10:00", "17:05:00", 8.92));
        records.add(createRecord(janitorId, "2025-08-22", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-23", "08:05:00", "16:45:00", 8.67));
        records.add(createRecord(janitorId, "2025-08-24", "08:20:00", "17:10:00", 8.83));
        // Aug 25 - absent
        records.add(createRecord(janitorId, "2025-08-26", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-27", "08:10:00", "16:50:00", 8.67));
        records.add(createRecord(janitorId, "2025-08-28", "07:55:00", "17:05:00", 9.17));
        records.add(createRecord(janitorId, "2025-08-29", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-30", "08:05:00", "16:55:00", 8.83));
        records.add(createRecord(janitorId, "2025-08-31", "08:15:00", "17:00:00", 8.75));

        // September 2025
        records.add(createRecord(janitorId, "2025-09-01", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-02", "08:10:00", "17:05:00", 8.92));
        // Sep 3 - absent
        records.add(createRecord(janitorId, "2025-09-04", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-05", "08:05:00", "16:50:00", 8.75));
        records.add(createRecord(janitorId, "2025-09-06", "08:15:00", "17:10:00", 8.92));
        // Sep 7 - absent
        records.add(createRecord(janitorId, "2025-09-08", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-09", "08:10:00", "16:40:00", 8.50));
        records.add(createRecord(janitorId, "2025-09-10", "08:00:00", "17:05:00", 9.08));
        records.add(createRecord(janitorId, "2025-09-11", "08:05:00", "17:00:00", 8.92));
        records.add(createRecord(janitorId, "2025-09-12", "08:00:00", "17:00:00", 9.00));
        // Sep 13 - absent
        records.add(createRecord(janitorId, "2025-09-14", "08:20:00", "16:50:00", 8.50));
        records.add(createRecord(janitorId, "2025-09-15", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-16", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-17", "08:10:00", "17:10:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-18", "08:00:00", "17:00:00", 9.00));
        // Sep 19 - absent
        records.add(createRecord(janitorId, "2025-09-20", "08:05:00", "16:55:00", 8.83));
        records.add(createRecord(janitorId, "2025-09-21", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-22", "08:15:00", "17:05:00", 8.83));
        records.add(createRecord(janitorId, "2025-09-23", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-24", "08:00:00", "17:00:00", 9.00));
        // Sep 25 - absent
        records.add(createRecord(janitorId, "2025-09-26", "08:10:00", "16:45:00", 8.58));
        records.add(createRecord(janitorId, "2025-09-27", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-28", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-29", "08:05:00", "17:10:00", 9.08));
        records.add(createRecord(janitorId, "2025-09-30", "08:00:00", "17:00:00", 9.00));

        // October 2025 (1-5)
        records.add(createRecord(janitorId, "2025-10-01", "08:00:00", "17:00:00", 9.00));
        // Oct 2 - absent
        records.add(createRecord(janitorId, "2025-10-03", "08:05:00", "17:05:00", 9.00));
        records.add(createRecord(janitorId, "2025-10-04", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-10-05", "08:10:00", "16:50:00", 8.67));

        return records;
    }

    /**
     * Generate attendance data for Janitor 195
     * Attendance rate: ~90% (few absences, work hours 8-9)
     */
    private List<Attendance> generateJanitor195Data() {
        List<Attendance> records = new ArrayList<>();
        Long janitorId = 195L;

        // August 2025
        records.add(createRecord(janitorId, "2025-08-01", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-02", "08:05:00", "17:05:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-03", "08:00:00", "16:50:00", 8.83));
        records.add(createRecord(janitorId, "2025-08-04", "07:55:00", "17:00:00", 9.08));
        records.add(createRecord(janitorId, "2025-08-05", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-06", "08:10:00", "17:05:00", 8.92));
        records.add(createRecord(janitorId, "2025-08-07", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-08", "08:05:00", "16:55:00", 8.83));
        records.add(createRecord(janitorId, "2025-08-09", "08:00:00", "17:10:00", 9.17));
        records.add(createRecord(janitorId, "2025-08-10", "08:00:00", "17:00:00", 9.00));
        // Aug 11 - absent
        records.add(createRecord(janitorId, "2025-08-12", "08:05:00", "17:00:00", 8.92));
        records.add(createRecord(janitorId, "2025-08-13", "08:00:00", "17:05:00", 9.08));
        records.add(createRecord(janitorId, "2025-08-14", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-15", "07:55:00", "16:55:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-16", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-17", "08:05:00", "17:10:00", 9.08));
        records.add(createRecord(janitorId, "2025-08-18", "08:00:00", "17:00:00", 9.00));
        // Aug 19 - absent
        records.add(createRecord(janitorId, "2025-08-20", "08:10:00", "17:05:00", 8.92));
        records.add(createRecord(janitorId, "2025-08-21", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-22", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-23", "08:05:00", "16:50:00", 8.75));
        records.add(createRecord(janitorId, "2025-08-24", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-25", "07:55:00", "17:05:00", 9.17));
        records.add(createRecord(janitorId, "2025-08-26", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-27", "08:05:00", "17:00:00", 8.92));
        records.add(createRecord(janitorId, "2025-08-28", "08:00:00", "17:00:00", 9.00));
        // Aug 29 - absent
        records.add(createRecord(janitorId, "2025-08-30", "08:00:00", "17:05:00", 9.08));
        records.add(createRecord(janitorId, "2025-08-31", "08:00:00", "17:00:00", 9.00));

        // September 2025
        records.add(createRecord(janitorId, "2025-09-01", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-02", "08:05:00", "17:05:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-03", "08:00:00", "17:00:00", 9.00));
        // Sep 4 - absent
        records.add(createRecord(janitorId, "2025-09-05", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-06", "08:10:00", "17:05:00", 8.92));
        records.add(createRecord(janitorId, "2025-09-07", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-08", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-09", "08:05:00", "16:55:00", 8.83));
        records.add(createRecord(janitorId, "2025-09-10", "08:00:00", "17:00:00", 9.00));
        // Sep 11 - absent
        records.add(createRecord(janitorId, "2025-09-12", "08:00:00", "17:05:00", 9.08));
        records.add(createRecord(janitorId, "2025-09-13", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-14", "08:05:00", "17:00:00", 8.92));
        records.add(createRecord(janitorId, "2025-09-15", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-16", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-17", "08:10:00", "17:10:00", 9.00));
        // Sep 18 - absent
        records.add(createRecord(janitorId, "2025-09-19", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-20", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-21", "08:05:00", "16:50:00", 8.75));
        records.add(createRecord(janitorId, "2025-09-22", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-23", "08:00:00", "17:05:00", 9.08));
        records.add(createRecord(janitorId, "2025-09-24", "08:00:00", "17:00:00", 9.00));
        // Sep 25 - absent
        records.add(createRecord(janitorId, "2025-09-26", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-27", "08:10:00", "17:05:00", 8.92));
        records.add(createRecord(janitorId, "2025-09-28", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-29", "08:00:00", "17:00:00", 9.00));
        // Sep 30 - present (not absent)
        records.add(createRecord(janitorId, "2025-09-30", "08:05:00", "17:00:00", 8.92));

        // October 2025 (1-5)
        records.add(createRecord(janitorId, "2025-10-01", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-10-02", "08:05:00", "17:05:00", 9.00));
        records.add(createRecord(janitorId, "2025-10-03", "08:00:00", "17:00:00", 9.00));
        // Oct 4 - absent
        records.add(createRecord(janitorId, "2025-10-05", "08:00:00", "17:00:00", 9.00));

        return records;
    }

    /**
     * Generate attendance data for Janitor 196
     * Attendance rate: ~80% (more absences, work hours 7-9)
     */
    private List<Attendance> generateJanitor196Data() {
        List<Attendance> records = new ArrayList<>();
        Long janitorId = 196L;

        // August 2025
        records.add(createRecord(janitorId, "2025-08-01", "08:15:00", "17:00:00", 8.75));
        // Aug 2 - absent
        records.add(createRecord(janitorId, "2025-08-03", "08:10:00", "16:40:00", 8.50));
        records.add(createRecord(janitorId, "2025-08-04", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-05", "08:20:00", "16:50:00", 8.50));
        // Aug 6 - absent
        records.add(createRecord(janitorId, "2025-08-07", "08:00:00", "17:10:00", 9.17));
        records.add(createRecord(janitorId, "2025-08-08", "08:05:00", "17:00:00", 8.92));
        records.add(createRecord(janitorId, "2025-08-09", "08:25:00", "16:30:00", 8.08));
        records.add(createRecord(janitorId, "2025-08-10", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-11", "08:15:00", "16:45:00", 8.50));
        // Aug 12 - absent
        records.add(createRecord(janitorId, "2025-08-13", "08:00:00", "17:00:00", 9.00));
        // Aug 14 - absent
        records.add(createRecord(janitorId, "2025-08-15", "08:10:00", "17:05:00", 8.92));
        records.add(createRecord(janitorId, "2025-08-16", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-17", "08:20:00", "16:35:00", 8.25));
        records.add(createRecord(janitorId, "2025-08-18", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-19", "08:15:00", "16:50:00", 8.58));
        // Aug 20 - absent
        records.add(createRecord(janitorId, "2025-08-21", "08:00:00", "17:05:00", 9.08));
        records.add(createRecord(janitorId, "2025-08-22", "08:10:00", "17:00:00", 8.83));
        // Aug 23 - absent
        records.add(createRecord(janitorId, "2025-08-24", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-25", "08:25:00", "16:40:00", 8.25));
        records.add(createRecord(janitorId, "2025-08-26", "08:00:00", "17:10:00", 9.17));
        records.add(createRecord(janitorId, "2025-08-27", "08:15:00", "17:00:00", 8.75));
        records.add(createRecord(janitorId, "2025-08-28", "08:00:00", "17:00:00", 9.00));
        // Aug 29 - absent
        records.add(createRecord(janitorId, "2025-08-30", "08:10:00", "16:50:00", 8.67));
        records.add(createRecord(janitorId, "2025-08-31", "08:00:00", "17:00:00", 9.00));

        // September 2025
        // Sep 1 - absent
        records.add(createRecord(janitorId, "2025-09-02", "08:15:00", "17:00:00", 8.75));
        records.add(createRecord(janitorId, "2025-09-03", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-04", "08:20:00", "16:35:00", 8.25));
        // Sep 5 - absent
        records.add(createRecord(janitorId, "2025-09-06", "08:00:00", "17:10:00", 9.17));
        records.add(createRecord(janitorId, "2025-09-07", "08:10:00", "17:00:00", 8.83));
        // Sep 8 - absent
        records.add(createRecord(janitorId, "2025-09-09", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-10", "08:15:00", "16:45:00", 8.50));
        records.add(createRecord(janitorId, "2025-09-11", "08:00:00", "17:00:00", 9.00));
        // Sep 12 - absent
        records.add(createRecord(janitorId, "2025-09-13", "08:25:00", "16:40:00", 8.25));
        records.add(createRecord(janitorId, "2025-09-14", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-15", "08:10:00", "17:05:00", 8.92));
        // Sep 16 - absent
        records.add(createRecord(janitorId, "2025-09-17", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-18", "08:15:00", "16:50:00", 8.58));
        records.add(createRecord(janitorId, "2025-09-19", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-20", "08:00:00", "17:10:00", 9.17));
        // Sep 21 - absent
        records.add(createRecord(janitorId, "2025-09-22", "08:20:00", "16:35:00", 8.25));
        records.add(createRecord(janitorId, "2025-09-23", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-24", "08:00:00", "17:00:00", 9.00));
        // Sep 25 - absent
        records.add(createRecord(janitorId, "2025-09-26", "08:10:00", "17:00:00", 8.83));
        records.add(createRecord(janitorId, "2025-09-27", "08:00:00", "17:00:00", 9.00));
        // Sep 28 - absent
        records.add(createRecord(janitorId, "2025-09-29", "08:15:00", "16:45:00", 8.50));
        records.add(createRecord(janitorId, "2025-09-30", "08:00:00", "17:00:00", 9.00));

        // October 2025 (1-5)
        records.add(createRecord(janitorId, "2025-10-01", "08:15:00", "17:00:00", 8.75));
        records.add(createRecord(janitorId, "2025-10-02", "08:00:00", "17:00:00", 9.00));
        // Oct 3 - absent
        records.add(createRecord(janitorId, "2025-10-04", "08:20:00", "16:40:00", 8.33));
        records.add(createRecord(janitorId, "2025-10-05", "08:00:00", "17:00:00", 9.00));

        return records;
    }

    /**
     * Generate attendance data for Janitor 197
     * Attendance rate: ~88% (occasional absences, work hours 8-9)
     */
    private List<Attendance> generateJanitor197Data() {
        List<Attendance> records = new ArrayList<>();
        Long janitorId = 197L;

        // August 2025
        records.add(createRecord(janitorId, "2025-08-01", "08:00:00", "17:05:00", 9.08));
        records.add(createRecord(janitorId, "2025-08-02", "08:05:00", "17:00:00", 8.92));
        records.add(createRecord(janitorId, "2025-08-03", "08:00:00", "17:00:00", 9.00));
        // Aug 4 - absent
        records.add(createRecord(janitorId, "2025-08-05", "08:10:00", "17:05:00", 8.92));
        records.add(createRecord(janitorId, "2025-08-06", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-07", "08:05:00", "17:10:00", 9.08));
        records.add(createRecord(janitorId, "2025-08-08", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-09", "08:10:00", "16:55:00", 8.75));
        records.add(createRecord(janitorId, "2025-08-10", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-11", "08:05:00", "17:05:00", 9.00));
        // Aug 12 - absent
        records.add(createRecord(janitorId, "2025-08-13", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-14", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-15", "08:10:00", "16:50:00", 8.67));
        records.add(createRecord(janitorId, "2025-08-16", "08:00:00", "17:05:00", 9.08));
        records.add(createRecord(janitorId, "2025-08-17", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-18", "08:05:00", "17:00:00", 8.92));
        // Aug 19 - absent
        records.add(createRecord(janitorId, "2025-08-20", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-21", "08:10:00", "17:10:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-22", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-23", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-24", "08:05:00", "16:55:00", 8.83));
        // Aug 25 - absent
        records.add(createRecord(janitorId, "2025-08-26", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-27", "08:00:00", "17:05:00", 9.08));
        records.add(createRecord(janitorId, "2025-08-28", "08:10:00", "17:00:00", 8.83));
        records.add(createRecord(janitorId, "2025-08-29", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-30", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-08-31", "08:05:00", "16:50:00", 8.75));

        // September 2025
        records.add(createRecord(janitorId, "2025-09-01", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-02", "08:05:00", "17:05:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-03", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-04", "08:00:00", "17:00:00", 9.00));
        // Sep 5 - absent
        records.add(createRecord(janitorId, "2025-09-06", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-07", "08:10:00", "17:10:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-08", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-09", "08:00:00", "16:50:00", 8.83));
        records.add(createRecord(janitorId, "2025-09-10", "08:05:00", "17:05:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-11", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-12", "08:00:00", "17:00:00", 9.00));
        // Sep 13 - absent
        records.add(createRecord(janitorId, "2025-09-14", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-15", "08:10:00", "16:55:00", 8.75));
        records.add(createRecord(janitorId, "2025-09-16", "08:00:00", "17:05:00", 9.08));
        records.add(createRecord(janitorId, "2025-09-17", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-18", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-19", "08:05:00", "17:00:00", 8.92));
        // Sep 20 - absent
        records.add(createRecord(janitorId, "2025-09-21", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-22", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-23", "08:10:00", "17:10:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-24", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-25", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-26", "08:05:00", "16:50:00", 8.75));
        records.add(createRecord(janitorId, "2025-09-27", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-28", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-29", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-09-30", "08:00:00", "17:05:00", 9.08));

        // October 2025 (1-5)
        records.add(createRecord(janitorId, "2025-10-01", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-10-02", "08:05:00", "17:05:00", 9.00));
        records.add(createRecord(janitorId, "2025-10-03", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-10-04", "08:00:00", "17:00:00", 9.00));
        records.add(createRecord(janitorId, "2025-10-05", "08:10:00", "16:55:00", 8.75));

        return records;
    }

    /**
     * Helper method to create an attendance record
     */
    private Attendance createRecord(Long janitorId, String date, String checkIn, String checkOut, Double workHours) {
        Attendance attendance = new Attendance();
        attendance.setJanitorId(janitorId);
        attendance.setWorkDate(LocalDate.parse(date));
        attendance.setCheckInTime(LocalDateTime.parse(date + "T" + checkIn));
        attendance.setCheckOutTime(LocalDateTime.parse(date + "T" + checkOut));
        attendance.setWorkHours(workHours);
        return attendance;
    }
}
