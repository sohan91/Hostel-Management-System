package com.example.HostelManagement.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.HostelManagement.config.JwtUtil;
import com.example.HostelManagement.dao.RoomCardDetailsFetch;
import com.example.HostelManagement.dto.BookHostler;
import com.example.HostelManagement.dto.CreateNewSharingType;
import com.example.HostelManagement.dto.DashboardAdminDTO;
import com.example.HostelManagement.dto.HostlerDto;
import com.example.HostelManagement.dto.HostlerListResponseDto;
import com.example.HostelManagement.dto.RoomDTO;
import com.example.HostelManagement.dto.RoomInfoDto;
import com.example.HostelManagement.dto.SharingDetailsDTO;
import com.example.HostelManagement.entities.hostel.Student;
import com.example.HostelManagement.entities.hostel.admin.Admin;
import com.example.HostelManagement.entities.hostel.admin.LoginRequestDAO;
import com.example.HostelManagement.repositories.AddNewSharingTypeRepo;
import com.example.HostelManagement.repositories.RoomRepository;
import com.example.HostelManagement.service.AdminAuthService;
import com.example.HostelManagement.service.RoomService;
import com.example.HostelManagement.service.StudentService;

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
    private final RoomRepository roomRepository;
    private final StudentService studentService;

    public AuthController(AdminAuthService authService, JwtUtil jwtUtil, RoomService roomService, 
                         AddNewSharingTypeRepo newSharingTypeRepo, RoomRepository repository,
                         StudentService studentService) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.roomService = roomService;
        this.newSharingTypeRepo = newSharingTypeRepo;
        this.roomRepository = repository;
        this.studentService = studentService;
    }

    @PostMapping("/book-hosteler")
public ResponseEntity<?> bookHostler(@RequestBody BookHostler bookHostler) {
    
    System.out.println("============== Book Hostler ==============");
    System.out.println("Hostler Details: " + bookHostler);
    
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

        // Use the authenticated admin's ID instead of the one from request
        Integer adminId = admin.getAdminId();
        Integer roomId = parseInteger(bookHostler.room_id(), "Room ID");
        
        if (roomId == null) {
            response.put("success", false);
            response.put("message", "Valid Room ID is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (bookHostler.student_name() == null || bookHostler.student_name().trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Student name is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (bookHostler.student_email() == null || bookHostler.student_email().trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Student email is required");
            return ResponseEntity.badRequest().body(response);
        }

        Student.PaymentStatus paymentStatus = parsePaymentStatus(bookHostler.payment_status());

        Student student = new Student();
        student.setAdminId(adminId); // Use authenticated admin ID
        student.setRoomId(roomId);
        student.setStudentName(bookHostler.student_name());
        student.setStudentEmail(bookHostler.student_email());
        student.setStudentPhone(bookHostler.student_phone());
        student.setStudentPassword(bookHostler.student_password());
        student.setDateOfBirth(bookHostler.date_of_birth());
        student.setParentName(bookHostler.parent_name());
        student.setParentPhone(bookHostler.parent_phone());
        student.setJoinDate(bookHostler.join_date() != null ? 
            bookHostler.join_date().atStartOfDay() : java.time.LocalDateTime.now());
        student.setPaymentStatus(paymentStatus);
        student.setPaymentMethod(bookHostler.payment_method());
        student.setIsActive(bookHostler.is_active());
        student.setLastLogin(bookHostler.last_login() != null ? 
            bookHostler.last_login().atStartOfDay() : null);
        student.setBloodGroup(bookHostler.blood_group());
        student.setTotalAmount(bookHostler.total_amount());

        System.out.println("Creating student entity: " + student.getStudentName() + " for room: " + roomId + " with admin ID: " + adminId);

        String result = studentService.addHostlerToRoom(student);

        if (result.startsWith("Success:")) {
            response.put("success", true);
            response.put("message", result);
            response.put("status", "BOOKED");
            
            if (result.contains("Student ID:")) {
                String[] parts = result.split("Student ID: ");
                if (parts.length > 1) {
                    String studentIdStr = parts[1].split(",")[0].trim();
                    try {
                        Integer studentId = Integer.parseInt(studentIdStr);
                        response.put("studentId", studentId);
                        System.out.println("Student ID generated: " + studentId);
                    } catch (NumberFormatException e) {
                        System.out.println("Could not parse student ID from success message");
                    }
                }
            }
            
            System.out.println("Hostler booked successfully: " + result);
            return ResponseEntity.ok().body(response);
        } else {
            response.put("success", false);
            response.put("message", result);
            response.put("status", "FAILED");
            
            System.out.println("Hostler booking failed: " + result);
            return ResponseEntity.badRequest().body(response);
        }

    } catch (Exception e) {
        System.err.println("Error booking hostler: " + e.getMessage());
        e.printStackTrace();
        
        response.put("success", false);
        response.put("message", "Internal server error: " + e.getMessage());
        response.put("status", "ERROR");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

    private Integer parseInteger(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            System.err.println(fieldName + " is null or empty");
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid " + fieldName + " format: " + value);
            return null;
        }
    }

    private Student.PaymentStatus parsePaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
            return Student.PaymentStatus.Pending;
        }
        try {
            return Student.PaymentStatus.valueOf(paymentStatus.trim());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid payment status: " + paymentStatus + ", defaulting to Pending");
            return Student.PaymentStatus.Pending;
        }
    }

    @PostMapping("/add-sharing-type")
    public ResponseEntity<Map<String, Object>> addSharingType(@RequestBody CreateNewSharingType request,
                                                              HttpServletRequest httpRequest) {
        System.out.println("=== ADD SHARING TYPE API CALLED ===");
        System.out.println("Sharing type data received: " + request);

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

            SharingDetailsDTO sharingDTO = new SharingDetailsDTO();
            sharingDTO.setAdminId(admin.getAdminId());
            sharingDTO.setSharingCapacity(request.getCapacity());
            sharingDTO.setSharingFee(request.getSharingFee());
            sharingDTO.setDescription(request.getDescription());

            System.out.println("Admin ID set: " + sharingDTO.getAdminId());

            if (sharingDTO.getSharingCapacity() == null || sharingDTO.getSharingCapacity() <= 0) {
                System.out.println("Capacity validation failed");
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

            System.out.println("Creating sharing type with details:");
            System.out.println("   - Capacity: " + sharingDTO.getSharingCapacity());
            System.out.println("   - Fee: " + sharingDTO.getSharingFee());
            System.out.println("   - Description: " + sharingDTO.getDescription());

            boolean isCreated = newSharingTypeRepo.saveSharingType(sharingDTO);

            if (isCreated) {
                response.put("success", true);
                response.put("message", sharingDTO.getSharingCapacity() + "-Sharing type added successfully!");
                System.out.println("Sharing type created successfully: " + sharingDTO.getSharingCapacity() + "-Sharing");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", sharingDTO.getSharingCapacity() + "-Sharing type already exists");
                System.out.println("Failed to create sharing type: " + sharingDTO.getSharingCapacity() + "-Sharing");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            System.out.println("Error in add-sharing-type endpoint: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("Unauthorized: No email found in security context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        try {
            Admin admin = authService.getAdminByEmail(email);
            System.out.println("Found admin for room details: " + (admin != null ? admin.getAdminId() : "null"));

            if (admin == null) {
                System.out.println("Admin not found for email: " + email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            List<RoomDTO> roomDetails = roomService.getAllRooms(admin.getAdminId());
            System.out.println("Found rooms: " + roomDetails.size());

            if (!roomDetails.isEmpty()) {
                System.out.println("DEBUG - Room Details Returned:");
                for (RoomDTO room : roomDetails) {
                    System.out.println(" Room: " + room.getRoomNumber() +
                            " | SharingType: " + room.getSharingTypeName() +
                            " | Capacity: " + room.getSharingCapacity() +
                            " | Price: " + room.getPrice() +
                            " | Floor: " + room.getFloorNumber() +
                            " | Status: " + room.getRoomStatus());
                }
            } else {
                System.out.println("No rooms found for admin ID: " + admin.getAdminId());
            }

            System.out.println("Returning room details: " + roomDetails.size() + " rooms");
            return ResponseEntity.ok(roomDetails);

        } catch (Exception e) {
            System.err.println("Error in room-details: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PostMapping("/add-room")
    public ResponseEntity<Map<String, Object>> addRoom(@RequestBody RoomDTO roomDTO,
                                                       HttpServletRequest request) {
        System.out.println("=== ADD ROOM API CALLED ===");
    
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

            roomDTO.setAdminId(admin.getAdminId());
            System.out.println("Admin ID set: " + roomDTO.getAdminId());

            if (roomDTO.getRoomNumber() == null || roomDTO.getRoomNumber().trim().isEmpty()) {
                System.out.println("Room number validation failed");
                response.put("success", false);
                response.put("message", "Room number is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (roomDTO.getFloorNumber() == null) {
                System.out.println("Floor number validation failed");
                response.put("success", false);
                response.put("message", "Floor number is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (roomDTO.getSharingTypeId() == null) {
                System.out.println("Sharing type ID validation failed");
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
                System.out.println("Failed to create room: " + roomDTO.getRoomNumber());
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
            System.out.println("Unauthorized: Cannot refresh token");
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

    @GetMapping("/hostler-lists")
    public ResponseEntity<HostlerListResponseDto> fetchHostlerList(
            @ModelAttribute RoomCardDetailsFetch roomDetails) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromSecurityContext(auth);
        Admin admin = authService.getAdminByEmail(email);
        Integer adminId = admin.getAdminId();
        
        try {
            System.out.println("=== DEBUG START ===");
            System.out.println("Fetching hostler list for room: " + roomDetails + ", adminId: " + adminId);
            
            List<Map<String, Object>> rawData = roomRepository.fetchRoomWithHostlers(
                roomDetails.getRoomId(),
                roomDetails.getFloorNumber(),
                roomDetails.getSharingTypeId(),
                adminId
            );
            
            System.out.println("Raw data size: " + rawData.size());
            
            if (!rawData.isEmpty()) {
                System.out.println("=== RAW DATA ANALYSIS ===");
                for (int i = 0; i < Math.min(rawData.size(), 5); i++) {
                    Map<String, Object> row = rawData.get(i);
                    System.out.println("--- Row " + i + " ---");
                    System.out.println("Available columns: " + row.keySet());
                    
                    System.out.println("student_id: " + row.get("student_id"));
                    System.out.println("student_name: " + row.get("student_name"));
                    
                    Object joinDateValue = row.get("join_date");
                    System.out.println("join_date value: " + joinDateValue);
                    System.out.println("join_date type: " + 
                        (joinDateValue != null ? joinDateValue.getClass().getName() : "null"));
                    
                    String[] dateFields = {"join_date", "date_of_birth", "last_login"};
                    for (String field : dateFields) {
                        Object fieldValue = row.get(field);
                        if (fieldValue != null) {
                            System.out.println(field + ": " + fieldValue + " (type: " + fieldValue.getClass().getName() + ")");
                        }
                    }
                    System.out.println("-------------------");
                }
            }
            
            if (rawData.isEmpty()) {
                System.out.println("No data found for the given criteria");
                return ResponseEntity.ok(HostlerListResponseDto.notFound(roomDetails));
            }
            
            RoomInfoDto roomInfo = RoomInfoDto.fromMap(rawData.get(0));
            
            System.out.println("=== PROCESSING HOSTLERS ===");
            List<HostlerDto> hostlers = rawData.stream()
                .map(row -> {
                    System.out.println("Processing row for student: " + row.get("student_name"));
                    HostlerDto hostler = HostlerDto.fromMap(row);
                    if (hostler != null) {
                        System.out.println("Created HostlerDto - JoinDate: " + hostler.getJoinDate());
                        System.out.println("Formatted JoinDate: " + hostler.getFormattedJoinDate());
                    }
                    return hostler;
                })
                .filter(hostler -> hostler != null)
                .collect(Collectors.toList());
            
            System.out.println("Successfully processed " + hostlers.size() + " hostlers");
            
            System.out.println("=== FINAL VERIFICATION ===");
            for (HostlerDto hostler : hostlers) {
                System.out.println("Hostler: " + hostler.getStudentName() + 
                    " | Join Date: " + hostler.getJoinDate() +
                    " | Formatted: " + hostler.getFormattedJoinDate());
            }
            
            HostlerListResponseDto response = HostlerListResponseDto.success(roomDetails, roomInfo, hostlers);
            System.out.println("=== DEBUG END ===");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error fetching hostler list: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(HostlerListResponseDto.error(roomDetails, "Failed to fetch hostler list: " + e.getMessage()));
        }
    }

    private RoomInfoDto extractRoomInfoFromRawData(List<Map<String, Object>> rawData) {
        if (rawData.isEmpty()) {
            return null;
        }
        
        Map<String, Object> firstRow = rawData.get(0);
        
        RoomInfoDto roomInfo = new RoomInfoDto();
        roomInfo.setRoomId(getInteger(firstRow, "room_id"));
        roomInfo.setRoomNumber(getString(firstRow, "room_number"));
        roomInfo.setFloorNumber(getInteger(firstRow, "floor_number"));
        roomInfo.setRoomStatus(getString(firstRow, "room_status"));
        roomInfo.setCurrentOccupancy(getInteger(firstRow, "current_occupancy"));
        roomInfo.setSharingCapacity(getInteger(firstRow, "sharing_capacity"));
        roomInfo.setAvailableBeds(getInteger(firstRow, "available_beds"));
        roomInfo.setSharingTypeName(getString(firstRow, "sharing_type_name"));
        roomInfo.setSharingTypeId(getInteger(firstRow, "sharing_type_id"));
        
        return roomInfo;
    }

    private List<HostlerDto> extractHostlersFromRawData(List<Map<String, Object>> rawData) {
        return rawData.stream()
            .filter(row -> row.get("student_id") != null)
            .map(this::mapToHostlerDto)
            .collect(Collectors.toList());
    }

    private HostlerDto mapToHostlerDto(Map<String, Object> row) {
        HostlerDto hostler = new HostlerDto();
        hostler.setStudentId(getInteger(row, "student_id"));
        hostler.setStudentName(getString(row, "student_name"));
        hostler.setStudentEmail(getString(row, "student_email"));
        hostler.setStudentPhone(getString(row, "student_phone"));
        hostler.setDateOfBirth(row.get("date_of_birth") != null ? row.get("date_of_birth").toString() : null);
        hostler.setParentName(getString(row, "parent_name"));
        hostler.setParentPhone(getString(row, "parent_phone"));
        hostler.setPaymentStatus(getString(row, "payment_status"));
        hostler.setIsActive(getBoolean(row, "is_active"));

        if (row.get("join_date") instanceof java.sql.Timestamp) {
            hostler.setJoinDate(((java.sql.Timestamp) row.get("join_date")).toLocalDateTime());
        }
        
        return hostler;
    }

    private Integer getInteger(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? ((Number) value).intValue() : null;
    }

    private String getString(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString() : null;
    }

    private Boolean getBoolean(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? (Boolean) value : null;
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