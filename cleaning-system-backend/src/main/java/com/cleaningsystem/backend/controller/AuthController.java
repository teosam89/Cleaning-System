package com.cleaningsystem.backend.controller;

import com.cleaningsystem.backend.dto.LoginRequest;
import com.cleaningsystem.backend.dto.LoginResponse;
import com.cleaningsystem.backend.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Validated
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * User login endpoint with input validation
     * @param loginRequest Login request object (validated)
     * @return Login response
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(401).body(response);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            LoginResponse errorResponse = new LoginResponse(false, "Internal server error, please try again later");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Check if username exists endpoint
     * @param username Username to check
     * @return Boolean result
     */
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(
            @RequestParam 
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String username) {
        boolean exists = authService.isUsernameExists(username);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * Health check endpoint
     * @return Service status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is running normally");
    }
}