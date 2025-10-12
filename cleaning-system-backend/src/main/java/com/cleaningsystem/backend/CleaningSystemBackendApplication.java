package com.cleaningsystem.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class CleaningSystemBackendApplication {

    /**
     * Password encoder bean for secure password handling
     * Using BCrypt with default strength (10 rounds)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static void main(String[] args) {
        SpringApplication.run(CleaningSystemBackendApplication.class, args);
        System.out.println("Cleaning System Backend started successfully!");
        System.out.println("API Base URL: http://localhost:8765/api");
        System.out.println("Login endpoint: POST http://localhost:8765/api/login");
        System.out.println("Health check: GET http://localhost:8765/api/health");
    }
}