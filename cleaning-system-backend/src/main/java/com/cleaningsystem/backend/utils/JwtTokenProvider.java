package com.cleaningsystem.backend.utils;

import com.cleaningsystem.backend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Provider
 * Handles JWT token generation, validation, and parsing
 */
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${jwt.issuer}")
    private String jwtIssuer;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    /**
     * Generate JWT token from User entity
     * @param user User entity containing user information
     * @return JWT token string
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("role", user.getRole());
        claims.put("fullName", user.getFullName());
        claims.put("email", user.getEmail());
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuer(jwtIssuer)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    
    /**
     * Extract username from JWT token
     * @param token JWT token string
     * @return Username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }
    
    /**
     * Extract user ID from JWT token
     * @param token JWT token string
     * @return User ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }
    
    /**
     * Extract user role from JWT token
     * @param token JWT token string
     * @return User role
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (String) claims.get("role");
    }
    
    /**
     * Extract full name from JWT token
     * @param token JWT token string
     * @return Full name
     */
    public String getFullNameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (String) claims.get("fullName");
    }
    
    /**
     * Extract email from JWT token
     * @param token JWT token string
     * @return Email
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (String) claims.get("email");
    }
    
    /**
     * Get token expiration date
     * @param token JWT token string
     * @return Expiration date
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
    
    /**
     * Parse and validate JWT token, return claims
     * @param token JWT token string
     * @return JWT Claims
     * @throws JwtException if token is invalid
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new JwtException("Invalid JWT token: " + e.getMessage());
        }
    }
    
    /**
     * Validate JWT token
     * @param token JWT token string
     * @return True if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            // Token validation failed
        }
        return false;
    }
    
    /**
     * Check if token is expired
     * @param token JWT token string
     * @return True if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true; // Consider invalid tokens as expired
        }
    }
    
    /**
     * Extract token from Authorization header
     * @param authHeader Authorization header value
     * @return JWT token string without Bearer prefix, or null if invalid
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}