package com.cleaningsystem.backend.dto;

public class LoginResponse {
    private boolean success;
    private String message;
    private UserInfo user;
    private String token;
    private String tokenType = "Bearer";
    
    // Default constructor
    public LoginResponse() {}
    
    // Constructor for success response with token
    public LoginResponse(boolean success, String message, UserInfo user, String token) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.token = token;
        this.tokenType = "Bearer";
    }
    
    // Constructor for success response without token (backwards compatibility)
    public LoginResponse(boolean success, String message, UserInfo user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }
    
    // Constructor for error response
    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.user = null;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public UserInfo getUser() {
        return user;
    }
    
    public void setUser(UserInfo user) {
        this.user = user;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    // 内部类用于返回用户信息（不包含密码）
    public static class UserInfo {
        private Long userId;
        private String username;
        private String role;
        private String email;
        private String fullName;
        
        public UserInfo() {}
        
        public UserInfo(Long userId, String username, String role, String email, String fullName) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.email = email;
            this.fullName = fullName;
        }
        
        // Getters and Setters
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getFullName() {
            return fullName;
        }
        
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }
}