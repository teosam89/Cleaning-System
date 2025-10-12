package com.cleaningsystem.backend.service;

import com.cleaningsystem.backend.entity.WeekRoutine;
import com.cleaningsystem.backend.entity.Task;
import com.cleaningsystem.backend.repository.WeekRoutineRepository;
import com.cleaningsystem.backend.repository.TaskRepository;
import com.cleaningsystem.backend.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeekRoutineService {
    
    @Autowired
    private WeekRoutineRepository weekRoutineRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    // Create a new week routine
    @Transactional
    public WeekRoutine createWeekRoutine(WeekRoutine routine) {
        // Validate routine data
        validateWeekRoutine(routine);
        
        routine.setCreatedAt(LocalDateTime.now());
        routine.setUpdatedAt(LocalDateTime.now());
        
        if (routine.getActive() == null) {
            routine.setActive(true);
        }
        
        if (routine.getTaskType() == null) {
            routine.setTaskType("public");
        }
        
        return weekRoutineRepository.save(routine);
    }
    
    // Update an existing week routine
    @Transactional
    public WeekRoutine updateWeekRoutine(Long routineId, WeekRoutine updatedRoutine) {
        Optional<WeekRoutine> existingRoutine = weekRoutineRepository.findById(routineId);
        
        if (existingRoutine.isEmpty()) {
            throw new IllegalArgumentException("Week routine not found with ID: " + routineId);
        }
        
        WeekRoutine routine = existingRoutine.get();
        
        // Update fields
        if (updatedRoutine.getTitle() != null) {
            routine.setTitle(updatedRoutine.getTitle());
        }
        if (updatedRoutine.getDescription() != null) {
            routine.setDescription(updatedRoutine.getDescription());
        }
        if (updatedRoutine.getLocation() != null) {
            routine.setLocation(updatedRoutine.getLocation());
        }
        if (updatedRoutine.getPriority() != null) {
            routine.setPriority(updatedRoutine.getPriority());
        }
        if (updatedRoutine.getWeekDays() != null) {
            routine.setWeekDays(updatedRoutine.getWeekDays());
        }
        if (updatedRoutine.getScheduledTime() != null) {
            routine.setScheduledTime(updatedRoutine.getScheduledTime());
        }
        if (updatedRoutine.getEstimatedDuration() != null) {
            routine.setEstimatedDuration(updatedRoutine.getEstimatedDuration());
        }
        if (updatedRoutine.getInstructions() != null) {
            routine.setInstructions(updatedRoutine.getInstructions());
        }
        if (updatedRoutine.getToolsRequired() != null) {
            routine.setToolsRequired(updatedRoutine.getToolsRequired());
        }
        if (updatedRoutine.getTaskType() != null) {
            routine.setTaskType(updatedRoutine.getTaskType());
        }
        if (updatedRoutine.getAssignedTo() != null) {
            routine.setAssignedTo(updatedRoutine.getAssignedTo());
        }
        if (updatedRoutine.getActive() != null) {
            routine.setActive(updatedRoutine.getActive());
        }
        
        // Validate updated routine
        validateWeekRoutine(routine);
        
        routine.setUpdatedAt(LocalDateTime.now());
        return weekRoutineRepository.save(routine);
    }
    
    // Delete a week routine
    @Transactional
    public void deleteWeekRoutine(Long routineId) {
        if (!weekRoutineRepository.existsById(routineId)) {
            throw new IllegalArgumentException("Week routine not found with ID: " + routineId);
        }
        weekRoutineRepository.deleteById(routineId);
    }
    
    // Get all week routines
    public List<WeekRoutine> getAllWeekRoutines() {
        return weekRoutineRepository.findAll();
    }
    
    // Get week routine by ID
    public Optional<WeekRoutine> getWeekRoutineById(Long routineId) {
        return weekRoutineRepository.findById(routineId);
    }
    
    // Get active week routines
    public List<WeekRoutine> getActiveWeekRoutines() {
        return weekRoutineRepository.findByActiveTrueOrderByCreatedAtDesc();
    }
    
    // Get week routines by creator (admin)
    public List<WeekRoutine> getWeekRoutinesByCreator(Long adminId) {
        return weekRoutineRepository.findByCreatedByOrderByCreatedAtDesc(adminId);
    }
    
    // Get active public routines (for task wall)
    public List<WeekRoutine> getActivePublicRoutines() {
        return weekRoutineRepository.findByTaskTypeAndActiveTrueOrderByScheduledTimeAsc("public");
    }
    
    // Get routines assigned to specific janitor
    public List<WeekRoutine> getRoutinesByJanitor(Long janitorId) {
        return weekRoutineRepository.findByAssignedToAndActiveTrueOrderByScheduledTimeAsc(janitorId);
    }
    
    // Toggle routine active status
    @Transactional
    public WeekRoutine toggleRoutineStatus(Long routineId) {
        Optional<WeekRoutine> optionalRoutine = weekRoutineRepository.findById(routineId);
        
        if (optionalRoutine.isEmpty()) {
            throw new IllegalArgumentException("Week routine not found with ID: " + routineId);
        }
        
        WeekRoutine routine = optionalRoutine.get();
        routine.setActive(!routine.getActive());
        routine.setUpdatedAt(LocalDateTime.now());
        
        return weekRoutineRepository.save(routine);
    }
    
    // CORE FEATURE: Generate tasks from week routines
    @Transactional
    public List<Task> generateTasksFromRoutines() {
        List<Task> generatedTasks = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // Get current day of week (1=Monday, 7=Sunday)
        int currentDayOfWeek = now.getDayOfWeek().getValue();
        String dayOfWeekStr = String.valueOf(currentDayOfWeek);
        
        // Find routines that should generate tasks today and haven't generated recently
        // Use 23-hour cutoff to prevent duplicate generation within the same day
        LocalDateTime cutoffTime = now.minusHours(23);
        List<WeekRoutine> routinesNeedingGeneration = weekRoutineRepository
            .findRoutinesNeedingGeneration(dayOfWeekStr, cutoffTime);
        
        for (WeekRoutine routine : routinesNeedingGeneration) {
            try {
                Task generatedTask = createTaskFromRoutine(routine, now);
                Task savedTask = taskRepository.save(generatedTask);
                generatedTasks.add(savedTask);
                
                // Update routine's last generated timestamp
                routine.setLastGenerated(now);
                weekRoutineRepository.save(routine);
                
            } catch (Exception e) {
                // Log error but continue processing other routines
                System.err.println("Error generating task from routine " + routine.getRoutineId() + ": " + e.getMessage());
            }
        }
        
        return generatedTasks;
    }
    
    // Generate tasks for specific routine (manual trigger)
    @Transactional
    public Task generateTaskFromRoutine(Long routineId) {
        Optional<WeekRoutine> optionalRoutine = weekRoutineRepository.findById(routineId);
        
        if (optionalRoutine.isEmpty()) {
            throw new IllegalArgumentException("Week routine not found with ID: " + routineId);
        }
        
        WeekRoutine routine = optionalRoutine.get();
        if (!routine.getActive()) {
            throw new IllegalArgumentException("Cannot generate task from inactive routine");
        }
        
        LocalDateTime now = LocalDateTime.now();
        Task generatedTask = createTaskFromRoutine(routine, now);
        Task savedTask = taskRepository.save(generatedTask);
        
        // Update routine's last generated timestamp
        routine.setLastGenerated(now);
        weekRoutineRepository.save(routine);
        
        return savedTask;
    }
    
    // Helper method to create a task from a routine
    private Task createTaskFromRoutine(WeekRoutine routine, LocalDateTime baseTime) {
        Task task = new Task();
        
        // Copy basic information from routine
        task.setTitle(routine.getTitle());
        task.setDescription(routine.getDescription());
        task.setLocation(routine.getLocation());
        task.setPriority(routine.getPriority());
        task.setInstructions(routine.getInstructions());
        task.setToolsRequired(routine.getToolsRequired());
        task.setEstimatedDuration(routine.getEstimatedDuration());
        
        // Set task scheduling
        LocalDateTime scheduledDateTime = calculateScheduledDateTime(routine, baseTime);
        task.setScheduledTime(scheduledDateTime);
        
        // Set due date (routine duration + 2 hours buffer)
        if (routine.getEstimatedDuration() != null) {
            task.setDueDate(scheduledDateTime.plusMinutes(routine.getEstimatedDuration() + 120));
        }
        
        // Set assignment based on routine type
        if ("assigned".equals(routine.getTaskType()) && routine.getAssignedTo() != null) {
            task.setAssignedTo(routine.getAssignedTo());
        } else {
            // Public task - no specific assignment
            task.setAssignedTo(null);
        }
        
        // Set creator
        task.setAssignedBy(routine.getCreatedBy());
        
        // Set default task properties
        task.setStatus("pending");
        task.setProgressPercentage(0);
        task.setCreatedAt(baseTime);
        
        return task;
    }
    
    // Calculate the scheduled date/time for a task based on routine and current time
    private LocalDateTime calculateScheduledDateTime(WeekRoutine routine, LocalDateTime baseTime) {
        LocalTime routineTime = routine.getScheduledTime();
        
        // If the routine time for today has already passed, schedule for next occurrence
        LocalDateTime todayAtRoutineTime = baseTime.toLocalDate().atTime(routineTime);
        
        if (todayAtRoutineTime.isBefore(baseTime) || todayAtRoutineTime.isEqual(baseTime)) {
            // Find next occurrence of this day
            int[] weekDays = routine.getWeekDaysArray();
            if (weekDays.length == 0) {
                throw new IllegalArgumentException("Routine has no valid week days configured");
            }
            
            // Find next occurrence
            LocalDateTime nextOccurrence = findNextOccurrence(baseTime, weekDays, routineTime);
            return nextOccurrence;
        } else {
            // Schedule for today at routine time
            return todayAtRoutineTime;
        }
    }
    
    // Find the next occurrence of the routine
    private LocalDateTime findNextOccurrence(LocalDateTime from, int[] weekDays, LocalTime routineTime) {
        LocalDateTime candidate = from.plusDays(1);
        
        // Look for next valid day within the next 7 days
        for (int i = 0; i < 7; i++) {
            int dayOfWeek = candidate.getDayOfWeek().getValue();
            
            for (int validDay : weekDays) {
                if (dayOfWeek == validDay) {
                    return candidate.toLocalDate().atTime(routineTime);
                }
            }
            
            candidate = candidate.plusDays(1);
        }
        
        // Fallback to tomorrow at routine time
        return from.plusDays(1).toLocalDate().atTime(routineTime);
    }
    
    // Get routine statistics for admin dashboard
    public Map<String, Object> getRoutineStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalRoutines", weekRoutineRepository.count());
        stats.put("activeRoutines", weekRoutineRepository.countByActiveTrue());
        stats.put("publicRoutines", weekRoutineRepository.countByTaskType("public"));
        stats.put("assignedRoutines", weekRoutineRepository.countByTaskType("assigned"));
        
        // Get statistics by type
        List<Object[]> typeStats = weekRoutineRepository.getRoutineStatisticsByType();
        Map<String, Long> typeStatsMap = new HashMap<>();
        for (Object[] stat : typeStats) {
            typeStatsMap.put((String) stat[0], (Long) stat[1]);
        }
        stats.put("routinesByType", typeStatsMap);
        
        // Get statistics by priority
        List<Object[]> priorityStats = weekRoutineRepository.getActiveRoutineStatisticsByPriority();
        Map<String, Long> priorityStatsMap = new HashMap<>();
        for (Object[] stat : priorityStats) {
            priorityStatsMap.put((String) stat[0], (Long) stat[1]);
        }
        stats.put("routinesByPriority", priorityStatsMap);
        
        return stats;
    }
    
    // Search routines with filters
    public List<WeekRoutine> searchRoutines(String searchTerm, String taskType, String priority, 
                                          Boolean active, Long createdBy, Long assignedTo) {
        return weekRoutineRepository.findRoutinesWithFilter(searchTerm, taskType, priority, 
                                                          active, createdBy, assignedTo);
    }
    
    // Count search results
    public long countSearchResults(String searchTerm, String taskType, String priority, 
                                 Boolean active, Long createdBy, Long assignedTo) {
        return weekRoutineRepository.countRoutinesWithFilter(searchTerm, taskType, priority, 
                                                           active, createdBy, assignedTo);
    }
    
    // Get recent routines for dashboard
    public List<WeekRoutine> getRecentRoutines() {
        return weekRoutineRepository.findTop5ByOrderByCreatedAtDesc();
    }
    
    // Validate week routine data
    private void validateWeekRoutine(WeekRoutine routine) {
        if (routine.getTitle() == null || routine.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Routine title is required");
        }
        
        if (routine.getLocation() == null || routine.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Routine location is required");
        }
        
        if (routine.getPriority() == null || routine.getPriority().trim().isEmpty()) {
            throw new IllegalArgumentException("Routine priority is required");
        }
        
        // Validate priority values
        if (!Arrays.asList("low", "normal", "high", "urgent").contains(routine.getPriority())) {
            throw new IllegalArgumentException("Invalid priority. Must be: low, normal, high, or urgent");
        }
        
        if (routine.getWeekDays() == null || routine.getWeekDays().trim().isEmpty()) {
            throw new IllegalArgumentException("Week days are required");
        }
        
        // Validate week days format (comma-separated numbers 1-7)
        validateWeekDaysFormat(routine.getWeekDays());
        
        if (routine.getScheduledTime() == null) {
            throw new IllegalArgumentException("Scheduled time is required");
        }
        
        if (routine.getTaskType() == null || routine.getTaskType().trim().isEmpty()) {
            throw new IllegalArgumentException("Task type is required");
        }
        
        // Validate task type
        if (!Arrays.asList("public", "assigned").contains(routine.getTaskType())) {
            throw new IllegalArgumentException("Invalid task type. Must be: public or assigned");
        }
        
        // If assigned task, assignedTo must be provided
        if ("assigned".equals(routine.getTaskType()) && routine.getAssignedTo() == null) {
            throw new IllegalArgumentException("Assigned task type requires assignedTo user ID");
        }
        
        if (routine.getCreatedBy() == null) {
            throw new IllegalArgumentException("Creator ID is required");
        }
        
        // Validate estimated duration
        if (routine.getEstimatedDuration() != null && routine.getEstimatedDuration() <= 0) {
            throw new IllegalArgumentException("Estimated duration must be positive");
        }
    }
    
    // Validate week days format
    private void validateWeekDaysFormat(String weekDays) {
        try {
            String[] days = weekDays.split(",");
            for (String day : days) {
                int dayNum = Integer.parseInt(day.trim());
                if (dayNum < 1 || dayNum > 7) {
                    throw new IllegalArgumentException("Week day numbers must be between 1 (Monday) and 7 (Sunday)");
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Week days must be comma-separated numbers (e.g., '1,3,5' for Mon,Wed,Fri)");
        }
    }
}