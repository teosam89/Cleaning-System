package com.cleaningsystem.backend.security;

import com.cleaningsystem.backend.utils.JwtTokenProvider;
import com.cleaningsystem.backend.service.AuthService;
import com.cleaningsystem.backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * JWT Authentication Filter
 * Processes JWT tokens in incoming requests and sets up Spring Security context
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private AuthService authService;
    
    /**
     * List of public endpoints that don't require authentication
     */
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/api/login",
        "/api/health",
        "/api/check-username",
        "/api/files/"  // All image files - public access for image display
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Skip authentication for public endpoints
            String requestPath = request.getServletPath();
            
            // Debug logging
            logger.info("JWT Filter - Request path: '{}', Method: {}", requestPath, request.getMethod());
            logger.info("JWT Filter - Checking against public endpoints: {}", PUBLIC_ENDPOINTS);
            
            if (isPublicEndpoint(requestPath)) {
                logger.info("JWT Filter - Path '{}' is public, skipping authentication", requestPath);
                filterChain.doFilter(request, response);
                return;
            } else {
                logger.info("JWT Filter - Path '{}' requires authentication", requestPath);
            }
            
            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");
            String token = jwtTokenProvider.extractTokenFromHeader(authHeader);
            
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // Extract user information from token
                String username = jwtTokenProvider.getUsernameFromToken(token);
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                String role = jwtTokenProvider.getRoleFromToken(token);
                
                // Verify user still exists in database (optional security check)
                Optional<User> userOpt = authService.findUserByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    // Create Spring Security authentication
                    // Map database roles to Spring Security roles
                    String springRole = mapDatabaseRoleToSpringRole(role);
                    List<SimpleGrantedAuthority> authorities = Arrays.asList(
                        new SimpleGrantedAuthority(springRole)
                    );
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(user, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set authentication in Security Context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Check if the request path is a public endpoint
     * @param requestPath The servlet path from the request
     * @return true if the endpoint is public, false otherwise
     */
    private boolean isPublicEndpoint(String requestPath) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(publicEndpoint -> requestPath.equals(publicEndpoint) || 
                         requestPath.startsWith(publicEndpoint));
    }
    
    /**
     * Map database role to Spring Security role with comprehensive validation and logging
     * @param databaseRole Role from database (admin, janitor, etc.)
     * @return Spring Security role with ROLE_ prefix
     */
    private String mapDatabaseRoleToSpringRole(String databaseRole) {
        // Enhanced null and empty validation
        if (databaseRole == null || databaseRole.trim().isEmpty()) {
            logger.warn("Null or empty database role provided, defaulting to ROLE_USER");
            return "ROLE_USER";
        }
        
        String normalizedRole = databaseRole.toLowerCase().trim();
        String mappedRole;
        
        switch (normalizedRole) {
            case "admin":
                mappedRole = "ROLE_ADMIN";
                logger.debug("Mapped database role '{}' to Spring role '{}'", databaseRole, mappedRole);
                break;
            case "supervisor":
                mappedRole = "ROLE_SUPERVISOR";
                logger.debug("Mapped database role '{}' to Spring role '{}'", databaseRole, mappedRole);
                break;
            case "janitor":
                mappedRole = "ROLE_JANITOR";
                logger.debug("Mapped database role '{}' to Spring role '{}'", databaseRole, mappedRole);
                break;
            case "cleaner":
                mappedRole = "ROLE_CLEANER";
                logger.debug("Mapped database role '{}' to Spring role '{}'", databaseRole, mappedRole);
                break;
            default:
                logger.warn("Unknown database role '{}', defaulting to ROLE_USER", databaseRole);
                mappedRole = "ROLE_USER";
                break;
        }
        
        // Security audit log for role mappings
        logger.info("Security role mapping: {} -> {} for authentication", databaseRole, mappedRole);
        return mappedRole;
    }
    
    /**
     * Validate role hierarchy and permissions
     * @param role The user's role
     * @param requestURI The requested endpoint
     * @return true if access should be allowed
     */
    private boolean validateRoleAccess(String role, String requestURI) {
        // Enhanced role-based access validation
        if (requestURI == null || role == null) {
            logger.warn("Invalid parameters for role access validation - role: {}, URI: {}", role, requestURI);
            return false;
        }
        
        // Admin endpoints - only for ROLE_ADMIN
        if (requestURI.startsWith("/api/admin/")) {
            boolean hasAccess = "ROLE_ADMIN".equals(role);
            if (!hasAccess) {
                logger.warn("Unauthorized admin access attempt - role: {}, URI: {}", role, requestURI);
            }
            return hasAccess;
        }
        
        // Supervisor endpoints - only for ROLE_SUPERVISOR
        if (requestURI.startsWith("/api/supervisor/")) {
            boolean hasAccess = "ROLE_SUPERVISOR".equals(role);
            if (!hasAccess) {
                logger.warn("Unauthorized supervisor access attempt - role: {}, URI: {}", role, requestURI);
            }
            return hasAccess;
        }
        
        // Janitor endpoints - ROLE_JANITOR or higher
        if (requestURI.startsWith("/api/janitors/")) {
            boolean hasAccess = Arrays.asList("ROLE_ADMIN", "ROLE_SUPERVISOR", "ROLE_JANITOR").contains(role);
            if (!hasAccess) {
                logger.warn("Unauthorized janitor access attempt - role: {}, URI: {}", role, requestURI);
            }
            return hasAccess;
        }
        
        // Task endpoints - all authenticated users
        if (requestURI.startsWith("/api/tasks/")) {
            return true; // All authenticated users can access tasks
        }
        
        // Default allow for other endpoints
        return true;
    }
}