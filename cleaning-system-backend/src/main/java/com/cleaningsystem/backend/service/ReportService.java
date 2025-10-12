package com.cleaningsystem.backend.service;

import com.cleaningsystem.backend.entity.Attendance;
import com.cleaningsystem.backend.entity.Task;
import com.cleaningsystem.backend.entity.User;
import com.cleaningsystem.backend.repository.AttendanceRepository;
import com.cleaningsystem.backend.repository.TaskRepository;
import com.cleaningsystem.backend.repository.UserRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Generate Job Monitor CSV report
     */
    public String generateJobMonitorReport(LocalDate startDate, LocalDate endDate) {
        try {
            StringWriter writer = new StringWriter();
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

            // Date formatter for UK/Malaysia format (dd/MM/yyyy)
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            // Report Header
            csvPrinter.printRecord("Job Monitor Report");
            csvPrinter.printRecord("Generated", LocalDate.now().format(dateFormatter));
            csvPrinter.printRecord("Date Range", startDate.format(dateFormatter) + " to " + endDate.format(dateFormatter));
            csvPrinter.println();

            // Get data for analysis
            List<Attendance> attendances = attendanceRepository.findByWorkDateBetween(startDate, endDate);
            List<Task> tasks = taskRepository.findByCreatedAtBetween(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
            );
            List<User> janitors = userRepository.findByRoleIn(Arrays.asList("janitor", "cleaner"));
            Map<Long, String> userNames = getUserNamesMap();

            // === SECTION 1: KPI SUMMARY ===
            csvPrinter.printRecord("=== Key Performance Indicators (KPI) ===");
            csvPrinter.printRecord("Metric", "Value");

            long totalJanitors = janitors.size();
            long activeTasks = tasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count();
            long completedTasks = tasks.stream().filter(t -> "completed".equals(t.getStatus())).count();
            long totalTasks = tasks.size();
            double taskCompletionRate = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0.0;

            // Calculate total days in date range (inclusive)
            long daysInRange = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;

            // Total expected attendance = days in range × total janitors
            long totalExpectedAttendance = daysInRange * totalJanitors;

            // Count actual checked-in records (only count if checkInTime is not null)
            long actualCheckedInCount = attendances.stream()
                .filter(a -> a.getCheckInTime() != null)
                .count();

            // Calculate overall attendance rate
            double attendanceRate = totalExpectedAttendance > 0
                ? (actualCheckedInCount * 100.0 / totalExpectedAttendance)
                : 0.0;

            csvPrinter.printRecord("Total Janitors", totalJanitors);
            csvPrinter.printRecord("Active Tasks", activeTasks);
            csvPrinter.printRecord("Total Tasks", totalTasks);
            csvPrinter.printRecord("Completed Tasks", completedTasks);
            csvPrinter.printRecord("Task Completion Rate (%)", String.format("%.1f", taskCompletionRate));
            csvPrinter.printRecord("Overall Attendance Rate (%)", String.format("%.1f", attendanceRate));
            csvPrinter.println();

            // === SECTION 2: ATTENDANCE STATISTICS ===
            csvPrinter.printRecord("=== Attendance Status Distribution ===");
            csvPrinter.printRecord("Status", "Count", "Percentage");

            long checkedIn = attendances.stream().filter(a -> a.getCheckInTime() != null && a.getCheckOutTime() == null).count();
            long checkedOut = attendances.stream().filter(a -> a.getCheckOutTime() != null).count();
            long notCheckedIn = totalExpectedAttendance - actualCheckedInCount;

            csvPrinter.printRecord("Checked In", checkedIn, totalExpectedAttendance > 0 ? String.format("%.1f%%", checkedIn * 100.0 / totalExpectedAttendance) : "0.0%");
            csvPrinter.printRecord("Checked Out", checkedOut, totalExpectedAttendance > 0 ? String.format("%.1f%%", checkedOut * 100.0 / totalExpectedAttendance) : "0.0%");
            csvPrinter.printRecord("Not Checked In", notCheckedIn, totalExpectedAttendance > 0 ? String.format("%.1f%%", notCheckedIn * 100.0 / totalExpectedAttendance) : "0.0%");
            csvPrinter.println();

            // === SECTION 3: TASK STATUS DISTRIBUTION ===
            csvPrinter.printRecord("=== Task Status Distribution ===");
            csvPrinter.printRecord("Status", "Count", "Percentage");

            long pendingTasks = tasks.stream().filter(t -> "pending".equals(t.getStatus())).count();
            long inProgressTasks = tasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count();
            long overdueTasks = tasks.stream().filter(t -> "overdue".equals(t.getStatus())).count();

            csvPrinter.printRecord("Completed", completedTasks, totalTasks > 0 ? String.format("%.1f%%", completedTasks * 100.0 / totalTasks) : "0.0%");
            csvPrinter.printRecord("In Progress", inProgressTasks, totalTasks > 0 ? String.format("%.1f%%", inProgressTasks * 100.0 / totalTasks) : "0.0%");
            csvPrinter.printRecord("Pending", pendingTasks, totalTasks > 0 ? String.format("%.1f%%", pendingTasks * 100.0 / totalTasks) : "0.0%");
            csvPrinter.printRecord("Overdue", overdueTasks, totalTasks > 0 ? String.format("%.1f%%", overdueTasks * 100.0 / totalTasks) : "0.0%");
            csvPrinter.println();

            // === SECTION 4: JANITOR PERFORMANCE SUMMARY ===
            csvPrinter.printRecord("=== Janitor Performance Summary ===");
            csvPrinter.printRecord("Employee ID", "Full Name", "Attendance Rate %", "Task Completion Rate %");

            for (User janitor : janitors) {
                // Count actual days this janitor checked in (checkInTime is not null)
                long janitorCheckedInDays = attendances.stream()
                    .filter(a -> janitor.getUserId().equals(a.getJanitorId()) && a.getCheckInTime() != null)
                    .count();

                // Calculate attendance rate based on date range days
                // Example: If date range is 7 days and janitor checked in 5 days, rate = 5/7 = 71.4%
                double janitorAttendanceRate = daysInRange > 0
                    ? (janitorCheckedInDays * 100.0 / daysInRange)
                    : 0.0;

                long janitorTasks = tasks.stream()
                    .filter(t -> janitor.getUserId().equals(t.getAssignedTo()))
                    .count();
                long janitorCompletedTasks = tasks.stream()
                    .filter(t -> janitor.getUserId().equals(t.getAssignedTo()) && "completed".equals(t.getStatus()))
                    .count();
                double janitorTaskRate = janitorTasks > 0 ? (janitorCompletedTasks * 100.0 / janitorTasks) : 0.0;

                csvPrinter.printRecord(
                    janitor.getUserId(),
                    janitor.getFullName(),
                    String.format("%.1f", janitorAttendanceRate),
                    String.format("%.1f", janitorTaskRate)
                );
            }
            csvPrinter.println();

            // === SECTION 5: DETAILED ATTENDANCE RECORDS ===
            csvPrinter.printRecord("=== Detailed Attendance Records ===");
            csvPrinter.printRecord("Date", "Employee ID", "Full Name", "Check In", "Check Out", "Work Hours");

            for (Attendance attendance : attendances) {
                csvPrinter.printRecord(
                    attendance.getWorkDate() != null ? attendance.getWorkDate().format(dateFormatter) : "N/A",
                    attendance.getJanitorId(),
                    userNames.getOrDefault(attendance.getJanitorId(), "Unknown"),
                    attendance.getCheckInTime() != null ? attendance.getCheckInTime().format(timeFormatter) : "N/A",
                    attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().format(timeFormatter) : "Not Checked Out",
                    attendance.getWorkHours() != null ? String.format("%.1f", attendance.getWorkHours()) : "0.0"
                );
            }

            csvPrinter.println();

            // === SECTION 6: DETAILED TASK RECORDS ===
            csvPrinter.printRecord("=== Detailed Task Records ===");
            csvPrinter.printRecord("Task ID", "Title", "Assigned To", "Assigned By", "Priority", "Status", "Due Date", "Completed Date");

            for (Task task : tasks) {
                csvPrinter.printRecord(
                    task.getTaskId(),
                    task.getTitle(),
                    task.getAssignedTo() != null ? userNames.getOrDefault(task.getAssignedTo(), "Public") : "Public",
                    task.getAssignedBy() != null ? userNames.getOrDefault(task.getAssignedBy(), "System") : "System",
                    task.getPriority(),
                    task.getStatus(),
                    task.getDueDate() != null ? task.getDueDate().format(dateFormatter) : "N/A",
                    task.getCompletedAt() != null ? task.getCompletedAt().format(dateTimeFormatter) : "N/A"
                );
            }

            csvPrinter.flush();
            return writer.toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV report due to I/O error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV report: " + e.getMessage(), e);
        }
    }

    /**
     * Generate Supervisor Team Report CSV
     */
    public String generateSupervisorTeamReport(Long supervisorId, LocalDate startDate, LocalDate endDate) {
        try {
            StringWriter writer = new StringWriter();
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

            // Date formatters
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            // Report Header
            csvPrinter.printRecord("Supervisor Team Report");
            csvPrinter.printRecord("Generated", LocalDate.now().format(dateFormatter));
            csvPrinter.printRecord("Date Range", startDate.format(dateFormatter) + " to " + endDate.format(dateFormatter));
            csvPrinter.println();

            // Get team members (janitors/cleaners)
            List<User> teamMembers = userRepository.findByRoleIn(Arrays.asList("janitor", "cleaner"));
            Map<Long, String> userNames = getUserNamesMap();

            // Get all team member IDs
            List<Long> teamMemberIds = teamMembers.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

            // Section 1: Team Task Completion - Get ALL tasks assigned to team members within date range
            csvPrinter.printRecord("=== Team Task Completion Summary ===");
            csvPrinter.printRecord("Task ID", "Title", "Assigned To", "Priority", "Status", "Due Date", "Completed Date");

            // Query all tasks assigned to any team member within date range
            List<Task> allTasks = taskRepository.findByCreatedAtBetween(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
            );

            // Filter tasks to only include those assigned to team members
            List<Task> teamTasks = allTasks.stream()
                .filter(task -> task.getAssignedTo() != null && teamMemberIds.contains(task.getAssignedTo()))
                .collect(Collectors.toList());

            for (Task task : teamTasks) {
                csvPrinter.printRecord(
                    task.getTaskId(),
                    task.getTitle(),
                    task.getAssignedTo() != null ? userNames.getOrDefault(task.getAssignedTo(), "Unassigned") : "Unassigned",
                    task.getPriority(),
                    task.getStatus(),
                    task.getDueDate() != null ? task.getDueDate().format(dateFormatter) : "N/A",
                    task.getCompletedAt() != null ? task.getCompletedAt().format(dateTimeFormatter) : "N/A"
                );
            }

            csvPrinter.println();

            // Section 2: Team Attendance Summary
            csvPrinter.printRecord("=== Team Attendance Summary ===");
            csvPrinter.printRecord("Date", "Employee ID", "Full Name", "Check In", "Check Out", "Work Hours");

            List<Attendance> attendances = attendanceRepository.findByWorkDateBetween(startDate, endDate);

            for (Attendance attendance : attendances) {
                csvPrinter.printRecord(
                    attendance.getWorkDate() != null ? attendance.getWorkDate().format(dateFormatter) : "N/A",
                    attendance.getJanitorId(),
                    userNames.getOrDefault(attendance.getJanitorId(), "Unknown"),
                    attendance.getCheckInTime() != null ? attendance.getCheckInTime().format(timeFormatter) : "N/A",
                    attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().format(timeFormatter) : "Not Checked Out",
                    attendance.getWorkHours() != null ? String.format("%.1f", attendance.getWorkHours()) : "0.0"
                );
            }

            csvPrinter.println();

            // Section 3: Individual Performance Comparisons
            csvPrinter.printRecord("=== Individual Performance Comparisons ===");
            csvPrinter.printRecord("Employee ID", "Full Name", "Tasks Assigned", "Tasks Completed", "Completion Rate %");

            for (User member : teamMembers) {
                long tasksAssigned = teamTasks.stream()
                    .filter(t -> member.getUserId().equals(t.getAssignedTo()))
                    .count();

                long tasksCompleted = teamTasks.stream()
                    .filter(t -> member.getUserId().equals(t.getAssignedTo()) && "completed".equals(t.getStatus()))
                    .count();

                double completionRate = tasksAssigned > 0 ? (tasksCompleted * 100.0 / tasksAssigned) : 0.0;

                csvPrinter.printRecord(
                    member.getUserId(),
                    member.getFullName(),
                    tasksAssigned,
                    tasksCompleted,
                    String.format("%.1f", completionRate)
                );
            }

            csvPrinter.flush();
            return writer.toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate supervisor team report due to I/O error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate supervisor team report: " + e.getMessage(), e);
        }
    }

    /**
     * Get user names map for lookup
     */
    private Map<Long, String> getUserNamesMap() {
        return userRepository.findAll().stream()
            .collect(Collectors.toMap(User::getUserId, User::getFullName));
    }
}
