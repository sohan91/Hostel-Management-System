package com.example.HostelManagement.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.HostelManagement.dto.DashboardAdminDTO;
import com.example.HostelManagement.entities.hostel.admin.Admin;
import com.example.HostelManagement.entities.hostel.admin.LoginRequestDAO;
import com.example.HostelManagement.entities.hostel.admin.LoginResponseDAO;
import com.example.HostelManagement.service.AdminAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class AdminRestController {
    @Autowired
    private AdminAuthService authService;
@PostMapping("/login")
public ResponseEntity<LoginResponseDAO> login(@RequestBody LoginRequestDAO loginRequest, HttpServletRequest request) {
    try {
        boolean admin = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        if (admin) {
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) oldSession.invalidate();
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("email", loginRequest.getEmail());
            return ResponseEntity.ok(new LoginResponseDAO(true, "Login successful!", "admin"));
        } else {
            return ResponseEntity.ok(new LoginResponseDAO(false, "Invalid email or password"));
        }
    } catch (Exception e) {
        return ResponseEntity.ok(new LoginResponseDAO(false, "Login failed: " + e.getMessage()));
    }
}


    @GetMapping("/admin-details")
    public ResponseEntity<?> getDetails(HttpSession session) {
        String email = (String) session.getAttribute("email");
        session.setAttribute("email",email);
        if (email == null) {
            return ResponseEntity.status(404).body("Admin not found");
        }
        Admin admin = authService.getAdminByEmail(email);
        return ResponseEntity.ok(new DashboardAdminDTO(
            admin.getFirstName(),
            admin.getLastName(),
            admin.getHostelName()
        ));
    }

   @GetMapping("/admin-profile")
    public ResponseEntity<?> adminProfile(HttpSession session) {
        try {
            String email = (String) session.getAttribute("email");
            if (email != null) {
                Admin admin = authService.getAdminByEmail(email);
                if (admin != null) {
                    return ResponseEntity.ok(new Admin(admin.getAdminId(),
                    admin.getFirstName(),admin.getLastName(),
                    admin.getEmail(),admin.getPhoneNumber(),admin.getHostelName(),admin.getHostelAddress(),admin.getCreatedAt()));
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body("Admin not found for email: " + email);
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                    .body("User not logged in or session expired");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error fetching admin profile: " + e.getMessage());
        }
    }

}
