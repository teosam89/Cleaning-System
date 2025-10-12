package com.cleaningsystem.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler for the Cleaning Management System
 * Handles all application exceptions and provides consistent error responses
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle validation errors from @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logger.warn("Validation error occurred: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Validation Failed",
            HttpStatus.BAD_REQUEST,
            request.getServletPath()
        );
        
        // Collect field-specific errors
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (existing, replacement) -> existing
                ));
        
        response.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle bind exceptions
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(
            BindException ex, HttpServletRequest request) {
        
        logger.warn("Binding error occurred: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Invalid request data",
            HttpStatus.BAD_REQUEST,
            request.getServletPath()
        );
        
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage
                ));
        
        response.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        logger.warn("Constraint violation: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Validation constraints violated",
            HttpStatus.BAD_REQUEST,
            request.getServletPath()
        );
        
        String violations = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        
        response.put("violations", violations);
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle authentication errors
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        logger.warn("Authentication failed: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Authentication failed",
            HttpStatus.UNAUTHORIZED,
            request.getServletPath()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Handle bad credentials
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        
        logger.warn("Bad credentials provided: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Invalid username or password",
            HttpStatus.UNAUTHORIZED,
            request.getServletPath()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Handle access denied errors
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        
        logger.warn("Access denied: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Access denied - insufficient permissions",
            HttpStatus.FORBIDDEN,
            request.getServletPath()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * Handle unsupported HTTP method
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        logger.warn("Unsupported HTTP method: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "HTTP method not supported: " + ex.getMethod(),
            HttpStatus.METHOD_NOT_ALLOWED,
            request.getServletPath()
        );
        
        response.put("supportedMethods", ex.getSupportedHttpMethods());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }
    
    /**
     * Handle argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        logger.warn("Type mismatch error: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            String.format("Invalid value for parameter '%s': %s", ex.getName(), ex.getValue()),
            HttpStatus.BAD_REQUEST,
            request.getServletPath()
        );
        
        response.put("parameter", ex.getName());
        response.put("expectedType", ex.getRequiredType().getSimpleName());
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle custom business logic exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        logger.warn("Business logic error: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            ex.getMessage(),
            ex.getHttpStatus(),
            request.getServletPath()
        );
        
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }
    
    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        logger.warn("Resource not found: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            ex.getMessage(),
            HttpStatus.NOT_FOUND,
            request.getServletPath()
        );
        
        response.put("resource", ex.getResourceName());
        response.put("resourceId", ex.getResourceId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        logger.error("Unexpected error occurred", ex);
        
        Map<String, Object> response = createErrorResponse(
            "An internal server error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR,
            request.getServletPath()
        );
        
        // In development, include stack trace
        if (isDevelopmentMode()) {
            response.put("exceptionType", ex.getClass().getSimpleName());
            response.put("details", ex.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Create standardized error response
     */
    private Map<String, Object> createErrorResponse(String message, HttpStatus status, String path) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        response.put("status", status.value());
        response.put("path", path);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * Check if running in development mode
     */
    private boolean isDevelopmentMode() {
        String profile = System.getProperty("spring.profiles.active", "");
        return profile.contains("dev") || profile.contains("local");
    }
}