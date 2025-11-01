package com.example.HostelManagement.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.HostelManagement.config.JwtUtil;
import com.example.HostelManagement.dto.DashboardAdminDTO;
import com.example.HostelManagement.dto.RoomDTO;
import com.example.HostelManagement.dto.SharingDetailsDTO;
import com.example.HostelManagement.entities.hostel.SharingType;
import com.example.HostelManagement.entities.hostel.admin.Admin;
import com.example.HostelManagement.entities.hostel.admin.LoginRequestDAO;
import com.example.HostelManagement.service.AdminAuthService;
import com.example.HostelManagement.service.RoomService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AdminAuthService authService;
    private final JwtUtil jwtUtil;
    private final RoomService roomService; // Add RoomService dependency

    public AuthController(AdminAuthService authService, JwtUtil jwtUtil, RoomService roomService) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.roomService = roomService; // Initialize RoomService
    }

    // Add the new room-details endpoint
    @GetMapping("/room-details")
    public ResponseEntity<?> getRoomDetails() {
        System.out.println("=== ROOM DETAILS API CALLED ===");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromSecurityContext(auth);
        System.out.println("Room details - email from SecurityContext: " + email);
        
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Authentication required"));
        }

        try {
            Admin admin = authService.getAdminByEmail(email);
            System.out.println("Found admin for room details: " + (admin != null ? admin.getAdminId() : "null"));
            
            if (admin == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "Admin not found"));
            }

            // Get room details using RoomService
            List<RoomDTO> roomDetails = roomService.getAllRooms(admin.getAdminId());
            System.out.println("Found rooms: " + roomDetails.size());
            
            // Debug: Print each room detail
            for (RoomDTO room : roomDetails) {
                System.out.println("Room - ID: " + room.getRoomId() + 
                                 ", Number: " + room.getRoomNumber() + 
                                 ", Floor: " + room.getFloorNumber() +
                                 ", Status: " + room.getRoomStatus() +
                                 ", Occupancy: " + room.getOccupancyStatus() +
                                 ", Type: " + room.getSharingTypeName() +
                                 ", Price: " + room.getPrice());
            }
            
            if (roomDetails.isEmpty()) {
                System.out.println("No rooms found for admin");
                return ResponseEntity.ok(Collections.singletonMap("message", "No rooms found for this admin"));
            }

            System.out.println("Returning room details: " + roomDetails.size() + " rooms");
            return ResponseEntity.ok(roomDetails);
            
        } catch (Exception e) {
            System.out.println("Error in room-details: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An error occurred while fetching room details"));
        }
    }

    // Existing endpoints remain the same...
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
                String jwtToken = jwtUtil.generateToken(loginRequest.getEmail());
                System.out.println("JWT Token generated successfully");
                
                Cookie jwtCookie = new Cookie("jwtToken", jwtToken);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setSecure(false);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(24 * 60 * 60);
                response.addCookie(jwtCookie);
                
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

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== LOGOUT API CALLED ===");
        
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            System.out.println("Invalidating session during logout: " + session.getId());
            session.invalidate();
        }
        
        SecurityContextHolder.clearContext();
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("message", "Logout successful - session cleared");
        
        System.out.println("Logout successful, session and cookies cleared");
        return ResponseEntity.ok(responseBody);
    }

   @GetMapping("/sharing-details")
public ResponseEntity<?> getSharingDetails() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = getEmailFromSecurityContext(auth);
    
    System.out.println("=== SHARING DETAILS API CALLED ===");
    System.out.println("Extracted email: " + email);

    if (email == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Collections.singletonMap("error", "Authentication required"));
    }

    try {
        Admin admin = authService.getAdminByEmail(email);
        System.out.println("Found admin: " + (admin != null ? admin.getAdminId() : "null"));
        
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Admin not found"));
        }

        List<SharingType> sharingTypes = authService.sharingTypesList(admin.getAdminId());
        System.out.println("Found sharing types: " + sharingTypes.size());
        
        // Debug: Print each sharing type
        for (SharingType sharingType : sharingTypes) {
            System.out.println("SharingType - ID: " + sharingType.getSharingTypeId() + 
                             ", Capacity: " + sharingType.getCapacity() +  // ✅ Use getCapacity()
                             ", Name: " + sharingType.getTypeName() +
                             ", Fee: " + sharingType.getSharingFee());
        }
        
        if (sharingTypes.isEmpty()) {
            System.out.println("No sharing types found for admin");
            return ResponseEntity.ok(Collections.singletonMap("message", "No sharing types found for this admin"));
        }

        List<SharingDetailsDTO> sharingDetails = sharingTypes.stream()
                .map(this::convertToSharingDetailsDTO)
                .collect(Collectors.toList());

        System.out.println("Returning sharing details: " + sharingDetails.size() + " items");
        return ResponseEntity.ok(sharingDetails);
        
    } catch (Exception e) {
        System.out.println("Error in sharing-details: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "An error occurred while fetching sharing details"));
    }
  }

    private SharingDetailsDTO convertToSharingDetailsDTO(SharingType sharingType) {
        return new SharingDetailsDTO(
            sharingType.getCapacity(),
            sharingType.getSharingFee(),
            "Success"
        );
    }


    @GetMapping("/admin-details")
    public ResponseEntity<?> getAdminDetails() {
        System.out.println("=== ADMIN DETAILS API CALLED ===");
        
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

    @GetMapping("/admin-profile-details")
    public ResponseEntity<?> getProfileDetails() {
        System.out.println("=== ADMIN PROFILE DETAILS API CALLED ===");
        
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

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== REFRESH TOKEN API CALLED ===");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromSecurityContext(auth);
        System.out.println("Refresh token - email from SecurityContext: " + email);
        
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Cannot refresh token: not authenticated"));
        }

        try {
            String newToken = jwtUtil.generateToken(email);
            
            Cookie jwtCookie = new Cookie("jwtToken", newToken);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60);
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

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        System.out.println("=== AUTH HEALTH CHECK ===");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "AuthController");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    private String getEmailFromSecurityContext(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return auth.getName();
        }
        return null;
    }
}