package com.example.HostelManagement.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.example.HostelManagement.dto.CreateNewSharingType;
import com.example.HostelManagement.dto.DashboardAdminDTO;
import com.example.HostelManagement.dto.RoomDTO;
import com.example.HostelManagement.dto.SharingDetailsDTO;
import com.example.HostelManagement.entities.hostel.admin.Admin;
import com.example.HostelManagement.entities.hostel.admin.LoginRequestDAO;
import com.example.HostelManagement.repositories.AddNewSharingTypeRepo;
import com.example.HostelManagement.service.AdminAuthService;
import com.example.HostelManagement.service.RoomService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final AdminAuthService authService;
    private final JwtUtil jwtUtil;
    private final RoomService roomService;
    private final AddNewSharingTypeRepo newSharingTypeRepo;

    public AuthController(AdminAuthService authService, JwtUtil jwtUtil, RoomService roomService, AddNewSharingTypeRepo newSharingTypeRepo) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.roomService = roomService;
        this.newSharingTypeRepo = newSharingTypeRepo;
       
    }
    @PostMapping("/add-sharing-type")
    public ResponseEntity<Map<String, Object>> addSharingType(@RequestBody CreateNewSharingType request,
                                                              HttpServletRequest httpRequest) {
        System.out.println("=== ADD SHARING TYPE API CALLED ===");
        System.out.println("Sharing type data received: " + request);

        Map<String, Object> response = new HashMap<>();

        try {
            // Get admin from security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = getEmailFromSecurityContext(auth);

            if (email == null) {
                System.out.println(" Unauthorized: No email found in security context");
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Admin admin = authService.getAdminByEmail(email);
            if (admin == null) {
                System.out.println("Admin not found for email: " + email);
                response.put("success", false);
                response.put("message", "Admin not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Create SharingDetailsDTO from request
            SharingDetailsDTO sharingDTO = new SharingDetailsDTO();
            sharingDTO.setAdminId(admin.getAdminId());
            sharingDTO.setSharingCapacity(request.getCapacity());
            sharingDTO.setSharingFee(request.getSharingFee());
            sharingDTO.setDescription(request.getDescription());

            System.out.println("Admin ID set: " + sharingDTO.getAdminId());

            // Validate required fields
            if (sharingDTO.getSharingCapacity() == null || sharingDTO.getSharingCapacity() <= 0) {
                System.out.println(" Capacity validation failed");
                response.put("success", false);
                response.put("message", "Valid capacity is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (sharingDTO.getSharingFee() == null || sharingDTO.getSharingFee().doubleValue() <= 0) {
                System.out.println("Sharing fee validation failed");
                response.put("success", false);
                response.put("message", "Valid price per bed is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Create the sharing type
            System.out.println("Creating sharing type with details:");
            System.out.println("   - Capacity: " + sharingDTO.getSharingCapacity());
            System.out.println("   - Fee: " + sharingDTO.getSharingFee());
            System.out.println("   - Description: " + sharingDTO.getDescription());

            boolean isCreated = newSharingTypeRepo.saveSharingType(sharingDTO);

            if (isCreated) {
                response.put("success", true);
                response.put("message", sharingDTO.getSharingCapacity() + "-Sharing type added successfully!");
                System.out.println(" Sharing type created successfully: " + sharingDTO.getSharingCapacity() + "-Sharing");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", sharingDTO.getSharingCapacity() + "-Sharing type already exists");
                System.out.println("Failed to create sharing type: " + sharingDTO.getSharingCapacity() + "-Sharing");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            System.out.println("Error in add-sharing-type endpoint: " + e.getMessage());
            
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/room-details")
    public ResponseEntity<List<RoomDTO>> getRoomDetails() {
        System.out.println("=== ROOM DETAILS API CALLED ===");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromSecurityContext(auth);
        System.out.println("Room details - email from SecurityContext: " + email);

        if (email == null) {
            System.out.println(" Unauthorized: No email found in security context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        try {
            Admin admin = authService.getAdminByEmail(email);
            System.out.println("Found admin for room details: " + (admin != null ? admin.getAdminId() : "null"));

            if (admin == null) {
                System.out.println("Admin not found for email: " + email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            // 1. Fetches all rooms (flat list) for the specific admin.
            List<RoomDTO> roomDetails = roomService.getAllRooms(admin.getAdminId());
            System.out.println("Found rooms: " + roomDetails.size());

            // 2. Enhanced debugging: Print room details to see what's being returned
            if (!roomDetails.isEmpty()) {
                System.out.println(" DEBUG - Room Details Returned:");
                for (RoomDTO room : roomDetails) {
                    System.out.println(" Room: " + room.getRoomNumber() +
                            " | SharingType: " + room.getSharingTypeName() +
                            " | Capacity: " + room.getSharingCapacity() +
                            " | Price: " + room.getPrice() +
                            " | Floor: " + room.getFloorNumber() +
                            " | Status: " + room.getRoomStatus());
                }
            } else {
                System.out.println(" No rooms found for admin ID: " + admin.getAdminId());
            }

            System.out.println("Returning room details: " + roomDetails.size() + " rooms");
            // 3. Return the flat list. The frontend handles the rest.
            return ResponseEntity.ok(roomDetails);

        } catch (Exception e) {
            e.getMessage();
            System.out.println("Error in room-details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PostMapping("/add-room")
    public ResponseEntity<Map<String, Object>> addRoom(@RequestBody RoomDTO roomDTO,
                                                       HttpServletRequest request) {
        System.out.println("=== ADD ROOM API CALLED ===");
    
    // Detailed logging
    System.out.println("Room data received:");
    System.out.println("   - Room Number: " + roomDTO.getRoomNumber());
    System.out.println("   - Floor: " + roomDTO.getFloorNumber());
    System.out.println("   - Sharing Type ID: " + roomDTO.getSharingTypeId());
    System.out.println("   - Price: " + roomDTO.getPrice());
    System.out.println("   - Capacity: " + roomDTO.getSharingCapacity());
    System.out.println("   - Admin ID: " + roomDTO.getAdminId());
    System.out.println("   - Room Status: " + roomDTO.getRoomStatus());

        Map<String, Object> response = new HashMap<>();

        try {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = getEmailFromSecurityContext(auth);

            if (email == null) {
                System.out.println("Unauthorized: No email found in security context");
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Admin admin = authService.getAdminByEmail(email);
            if (admin == null) {
                System.out.println("Admin not found for email: " + email);
                response.put("success", false);
                response.put("message", "Admin not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Set admin ID to the room DTO
            roomDTO.setAdminId(admin.getAdminId());
            System.out.println("Admin ID set: " + roomDTO.getAdminId());

            // Validate required fields
            if (roomDTO.getRoomNumber() == null || roomDTO.getRoomNumber().trim().isEmpty()) {
                System.out.println(" Room number validation failed");
                response.put("success", false);
                response.put("message", "Room number is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (roomDTO.getFloorNumber() == null) {
                System.out.println(" Floor number validation failed");
                response.put("success", false);
                response.put("message", "Floor number is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (roomDTO.getSharingTypeId() == null) {
                System.out.println(" Sharing type ID validation failed");
                response.put("success", false);
                response.put("message", "Sharing type is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (roomDTO.getPrice() == null || roomDTO.getPrice() <= 0) {
                System.out.println("Price validation failed");
                response.put("success", false);
                response.put("message", "Valid price is required");
                return ResponseEntity.badRequest().body(response);
            }

            
            System.out.println("Creating room with details:");
            System.out.println("   - Room Number: " + roomDTO.getRoomNumber());
            System.out.println("   - Floor: " + roomDTO.getFloorNumber());
            System.out.println("   - Sharing Type ID: " + roomDTO.getSharingTypeId());
            System.out.println("   - Price: " + roomDTO.getPrice());
            System.out.println("   - Capacity: " + roomDTO.getSharingCapacity());

            boolean isCreated = roomService.createRoom(roomDTO);

            if (isCreated) {
                response.put("success", true);
                response.put("message", "Room " + roomDTO.getRoomNumber() + " added successfully!");
                System.out.println("Room created successfully: " + roomDTO.getRoomNumber());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to create room. Room might already exist on this floor.");
                System.out.println(" Failed to create room: " + roomDTO.getRoomNumber());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            e.getMessage();
            System.out.println("Error in add-room endpoint: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

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
            e.getMessage();
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
    public ResponseEntity<List<SharingDetailsDTO>> getSharingDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromSecurityContext(auth);

        System.out.println("=== SHARING DETAILS API CALLED ===");
        System.out.println("Extracted email: " + email);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        try {
            Admin admin = authService.getAdminByEmail(email);
            System.out.println("Found admin: " + (admin != null ? admin.getAdminId() : "null"));

            if (admin == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            List<SharingDetailsDTO> sharingDetails = newSharingTypeRepo.getSharingTypesByAdmin(admin.getAdminId());
            System.out.println("Found sharing types: " + sharingDetails.size());

            if (sharingDetails.isEmpty()) {
                System.out.println("No sharing types found for admin");
            }

            System.out.println("Returning sharing details: " + sharingDetails.size() + " items");
            return ResponseEntity.ok(sharingDetails);

        } catch (Exception e) {
            e.getMessage();
            System.out.println("Error in sharing-details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
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
            e.getMessage();
            System.out.println(" Error fetching admin details: " + e.getMessage());
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
            System.out.println("Unauthorized: No email found for profile details");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            Admin admin = authService.getAdminByEmail(email);
            if (admin == null) {
                System.out.println("Admin not found for profile details");
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
            System.out.println(" Unauthorized: Cannot refresh token");
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
            System.out.println(" Error refreshing token: " + e.getMessage());
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
            String email = auth.getName();
            System.out.println("Security Context - Authenticated user: " + email);
            return email;
        }
        System.out.println("Security Context - No authenticated user found");
        return null;
    }
}