package com.cleaningsystem.backend.service;

import com.cleaningsystem.backend.entity.User;
import com.cleaningsystem.backend.repository.UserRepository;
import com.cleaningsystem.backend.dto.LoginRequest;
import com.cleaningsystem.backend.dto.LoginResponse;
import com.cleaningsystem.backend.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    /**
     * User login authentication with secure password verification
     * @param loginRequest Login request object containing username and password
     * @return Login response object with user information
     */
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            // Input validation
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                return new LoginResponse(false, "Username cannot be empty");
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return new LoginResponse(false, "Password cannot be empty");
            }
            
            String username = loginRequest.getUsername().trim();
            String password = loginRequest.getPassword().trim();
            
            
            // Find user by username
            Optional<User> userByUsername = userRepository.findByUsername(username);
            if (userByUsername.isPresent()) {
                User user = userByUsername.get();
                
                // Secure password comparison using BCrypt
                if (passwordEncoder.matches(password, user.getPassword())) {
                    // Generate JWT token
                    String jwtToken = jwtTokenProvider.generateToken(user);
                    
                    // Create user info object (without password)
                    LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                        user.getUserId(),
                        user.getUsername(),
                        user.getRole(),
                        user.getEmail(),
                        user.getFullName()
                    );
                    
                    return new LoginResponse(true, "Login successful", userInfo, jwtToken);
                } else {
                    return new LoginResponse(false, "Invalid username or password");
                }
            } else {
                return new LoginResponse(false, "Invalid username or password");
            }
            
        } catch (Exception e) {
            return new LoginResponse(false, "Login error occurred, please try again");
        }
    }
    
    /**
     * Check if username exists in database
     * @param username Username to check
     * @return True if username exists, false otherwise
     */
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Find user by username
     * @param username Username to search for
     * @return Optional User object
     */
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}