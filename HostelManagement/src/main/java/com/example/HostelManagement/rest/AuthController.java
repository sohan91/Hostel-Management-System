package com.example.HostelManagement.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.HostelManagement.config.JwtUtil;
import com.example.HostelManagement.dto.DashboardAdminDTO;
import com.example.HostelManagement.entities.hostel.admin.Admin;
import com.example.HostelManagement.entities.hostel.admin.LoginRequestDAO;
import com.example.HostelManagement.service.AdminAuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminAuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AdminAuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    // LOGIN - returns JWT token
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequestDAO loginRequest, 
                                                   HttpServletResponse response) {
        System.out.println("=== LOGIN API CALLED ===");
        System.out.println("Email: " + loginRequest.getEmail());
        
        Map<String, Object> responseBody = new HashMap<>();
        try {
            boolean isAuthenticated = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
            System.out.println("Authentication result: " + isAuthenticated);

            if (isAuthenticated) {
                // Generate JWT token
                String jwtToken = jwtUtil.generateToken(loginRequest.getEmail());
                System.out.println("JWT Token generated successfully");
                
                // Set JWT as HTTP-only cookie
                Cookie jwtCookie = new Cookie("jwtToken", jwtToken);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setSecure(false);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(24 * 60 * 60);//for 24 hours
                response.addCookie(jwtCookie);
                
                // Get admin details for response
                Admin admin = authService.getAdminByEmail(loginRequest.getEmail());
                
                responseBody.put("success", true);
                responseBody.put("message", "Login successful!");
                responseBody.put("role", "admin");
                responseBody.put("token", jwtToken);
                responseBody.put("admin", Map.of(
                    "firstName", admin.getFirstName(),
                    "lastName", admin.getLastName(),
                    "hostelName", admin.getHostelName(),
                    "email", admin.getEmail()
                ));
                
                System.out.println("Login successful for: " + loginRequest.getEmail());
                return ResponseEntity.ok(responseBody);
            } else {
                responseBody.put("success", false);
                responseBody.put("message", "Invalid email or password");
                System.out.println("Login failed: Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            responseBody.put("success", false);
            responseBody.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }

    // SINGLE LOGOUT METHOD - Fixed duplicate issue
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== LOGOUT API CALLED ===");
        
        // Clear the JWT cookie
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        
        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            System.out.println("Invalidating session during logout: " + session.getId());
            session.invalidate();
        }
        
        // Clear security context
        SecurityContextHolder.clearContext();
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("message", "Logout successful - session cleared");
        
        System.out.println("Logout successful, session and cookies cleared");
        return ResponseEntity.ok(responseBody);
    }

    // ADMIN DETAILS
    @GetMapping("/admin-details")
    public ResponseEntity<?> getAdminDetails() {
        System.out.println("=== ADMIN DETAILS API CALLED ===");
        
        // Extract email from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromSecurityContext(auth);
        System.out.println("Extracted email from SecurityContext: " + email);
        
        if (email == null) {
            System.out.println("No email found - user not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            Admin admin = authService.getAdminByEmail(email);
            if (admin == null) {
                System.out.println("Admin not found for email: " + email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Admin not found"));
            }
            
            DashboardAdminDTO adminDTO = new DashboardAdminDTO(
                admin.getFirstName(),
                admin.getLastName(),
                admin.getHostelName()
            );
            
            System.out.println("Admin details fetched successfully for: " + email);
            return ResponseEntity.ok(adminDTO);
        } catch (Exception e) {
            System.out.println("Error fetching admin details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching admin details"));
        }
    }

    // SESSION CHECK (verify JWT token is valid)
    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession() {
        System.out.println("=== CHECK SESSION API CALLED ===");
        
        // Get authentication from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromSecurityContext(auth);
        System.out.println("Session check - email from SecurityContext: " + email);
        
        Map<String, Object> sessionInfo = new HashMap<>();
        
        if (email != null) {
            try {
                // Verify admin still exists in database
                Admin admin = authService.getAdminByEmail(email);
                if (admin != null) {
                    sessionInfo.put("authenticated", true);
                    sessionInfo.put("email", email);
                    sessionInfo.put("role", "admin");
                    sessionInfo.put("message", "Valid JWT session");
                    sessionInfo.put("admin", Map.of(
                        "firstName", admin.getFirstName(),
                        "lastName", admin.getLastName(),
                        "hostelName", admin.getHostelName()
                    ));
                    System.out.println("Valid session for: " + email);
                } else {
                    sessionInfo.put("authenticated", false);
                    sessionInfo.put("message", "Admin account not found");
                    System.out.println("Admin account not found for: " + email);
                }
            } catch (Exception e) {
                sessionInfo.put("authenticated", false);
                sessionInfo.put("message", "Error checking session");
                System.out.println("Error checking session: " + e.getMessage());
            }
        } else {
            sessionInfo.put("authenticated", false);
            sessionInfo.put("message", "No valid JWT session");
            System.out.println("No valid JWT session found");
        }
        
        return ResponseEntity.ok(sessionInfo);
    }

    // ADMIN PROFILE DETAILS
    @GetMapping("/admin-profile-details")
    public ResponseEntity<?> getProfileDetails() {
        System.out.println("=== ADMIN PROFILE DETAILS API CALLED ===");
        
        // Get authentication from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromSecurityContext(auth);
        System.out.println("Profile details - email from SecurityContext: " + email);
        
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }
        
        try {
            Admin admin = authService.getAdminByEmail(email);
            if (admin == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Admin not found"));
            }
            
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("adminId", admin.getAdminId());
            profileData.put("firstName", admin.getFirstName());
            profileData.put("lastName", admin.getLastName());
            profileData.put("email", admin.getEmail());
            profileData.put("phoneNumber", admin.getPhoneNumber());
            profileData.put("hostelName", admin.getHostelName());
            profileData.put("hostelAddress", admin.getHostelAddress());
            profileData.put("createdAt", admin.getCreatedAt());
            
            System.out.println("Profile details fetched successfully for: " + email);
            return ResponseEntity.ok(profileData);
        } catch (Exception e) {
            System.out.println("Error fetching profile details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching profile details: " + e.getMessage()));
        }
    }

    // REFRESH TOKEN (optional - for extending session)
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== REFRESH TOKEN API CALLED ===");
        
        // Get authentication from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromSecurityContext(auth);
        System.out.println("Refresh token - email from SecurityContext: " + email);
        
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Cannot refresh token: not authenticated"));
        }

        try {
            // Generate new token
            String newToken = jwtUtil.generateToken(email);
            
            // Update cookie
            Cookie jwtCookie = new Cookie("jwtToken", newToken);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 hours
            response.addCookie(jwtCookie);
            
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", "Token refreshed successfully");
            responseBody.put("token", newToken);
            
            System.out.println("Token refreshed successfully for: " + email);
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            System.out.println("Error refreshing token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to refresh token"));
        }
    }

    // HEALTH CHECK
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        System.out.println("=== AUTH HEALTH CHECK ===");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "AuthController");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // HELPER METHOD to extract email from SecurityContext
    private String getEmailFromSecurityContext(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return (String) auth.getPrincipal();
        }
        return null;
    }
}