package com.cleaningsystem.backend.config;

import com.cleaningsystem.backend.security.JwtAuthenticationEntryPoint;
import com.cleaningsystem.backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security Configuration for JWT Authentication
 * Configures security filters, CORS, and endpoint protection
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);
    
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * Configure HTTP Security with JWT authentication
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST APIs
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure session management (stateless for JWT)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/check-username").permitAll()

                // All image files - public access (images are user content that should be viewable)
                .requestMatchers(HttpMethod.GET, "/api/files/**").permitAll()
                
                // Task endpoints - authenticated users
                .requestMatchers("/api/tasks").authenticated()
                .requestMatchers("/api/tasks/").authenticated() 
                .requestMatchers("/api/tasks/**").authenticated()
                
                // Supervisor endpoints - require SUPERVISOR role
                .requestMatchers("/api/supervisor/**").hasRole("SUPERVISOR")
                
                // Admin-only endpoints (user management, critical operations)
                .requestMatchers(HttpMethod.POST, "/api/admin/create-admin").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/admin/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/admin/users/**").hasRole("ADMIN")
                
                // All admin endpoints - ADMIN ONLY (strict role hierarchy)
                .requestMatchers(HttpMethod.GET, "/api/admin/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/admin/assignable-users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/admin/reports/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/admin/dashboard/**").hasRole("ADMIN")
                
                // All other admin endpoints - ADMIN only
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Janitor endpoints - require ADMIN, SUPERVISOR, JANITOR, or CLEANER role
                .requestMatchers(HttpMethod.GET, "/api/janitors/**").hasAnyRole("ADMIN", "SUPERVISOR", "JANITOR", "CLEANER")
                .requestMatchers(HttpMethod.POST, "/api/janitors").hasRole("ADMIN") // Only admin can create janitors
                .requestMatchers(HttpMethod.PUT, "/api/janitors/**").hasRole("ADMIN") // Only admin can update janitors
                .requestMatchers(HttpMethod.DELETE, "/api/janitors/**").hasRole("ADMIN") // Only admin can delete janitors
                
                // Attendance endpoints - authenticated users (require JANITOR or CLEANER role)
                .requestMatchers("/api/attendance/**").hasAnyRole("JANITOR", "CLEANER", "ADMIN", "SUPERVISOR")
                
                // Image upload endpoints - authenticated users only
                .requestMatchers(HttpMethod.POST, "/api/upload/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/images/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/images/**").authenticated()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Configure JWT authentication entry point
            .exceptionHandling(ex -> 
                ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Configure CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (configure for production)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Allow specific headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Cache-Control", "Content-Type", "Accept", "X-Requested-With"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}