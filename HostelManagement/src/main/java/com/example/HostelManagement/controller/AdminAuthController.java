package com.example.HostelManagement.controller;

import com.example.HostelManagement.entities.admin.LoginRequestDAO;
import com.example.HostelManagement.entities.admin.LoginResponseDAO;
import com.example.HostelManagement.service.AdminAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController  // Important: @RestController returns JSON automatically
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8080")
public class AdminAuthController {

    @Autowired
    private AdminAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDAO> login(@RequestBody LoginRequestDAO loginRequest) {
        try {
            boolean admin = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());

            if (admin) {
                return ResponseEntity.ok(new LoginResponseDAO(true, "Login successful!", "admin"));
            } else {
                return ResponseEntity.ok(new LoginResponseDAO(false, "Invalid email or password"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new LoginResponseDAO(false, "Login failed: " + e.getMessage()));
        }
    }
}
