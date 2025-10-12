package com.cleaningsystem.backend.config;

import com.cleaningsystem.backend.entity.User;
import com.cleaningsystem.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Simple data initializer - creates test users if database is empty
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no users exist
        if (userRepository.count() == 0) {
            initializeUsers();
        }
    }
    
    private void initializeUsers() {
        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole("admin");
        admin.setEmail("admin@cleaning.com");
        admin.setFullName("System Admin");
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);
        
        // Create test janitors
        User maria = new User();
        maria.setUsername("maria");
        maria.setPassword(passwordEncoder.encode("maria123"));
        maria.setRole("janitor");
        maria.setEmail("maria@cleaning.com");
        maria.setFullName("Maria Santos");
        maria.setCreatedAt(LocalDateTime.now());
        userRepository.save(maria);
        
        User john = new User();
        john.setUsername("john");
        john.setPassword(passwordEncoder.encode("john123"));
        john.setRole("janitor");
        john.setEmail("john@cleaning.com");
        john.setFullName("John Smith");
        john.setCreatedAt(LocalDateTime.now());
        userRepository.save(john);
        
        // Create test supervisors
        User supervisor = new User();
        supervisor.setUsername("supervisor");
        supervisor.setPassword(passwordEncoder.encode("supervisor123"));
        supervisor.setRole("supervisor");
        supervisor.setEmail("supervisor@cleaning.com");
        supervisor.setFullName("Team Supervisor");
        supervisor.setCreatedAt(LocalDateTime.now());
        userRepository.save(supervisor);
        
        User sarah = new User();
        sarah.setUsername("sarah");
        sarah.setPassword(passwordEncoder.encode("sarah123"));
        sarah.setRole("supervisor");
        sarah.setEmail("sarah@cleaning.com");
        sarah.setFullName("Sarah Johnson");
        sarah.setCreatedAt(LocalDateTime.now());
        userRepository.save(sarah);
        
        // Create additional test cleaners
        User mike = new User();
        mike.setUsername("mike");
        mike.setPassword(passwordEncoder.encode("mike123"));
        mike.setRole("cleaner");
        mike.setEmail("mike@cleaning.com");
        mike.setFullName("Mike Wilson");
        mike.setCreatedAt(LocalDateTime.now());
        userRepository.save(mike);
    }
}