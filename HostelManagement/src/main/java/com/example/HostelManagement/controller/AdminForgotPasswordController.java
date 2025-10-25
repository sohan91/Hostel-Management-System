package com.example.HostelManagement.controller;

import com.example.HostelManagement.dao.AdminDAO;
import com.example.HostelManagement.entities.admin.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/hostel/api")
@CrossOrigin(origins = "*")
public class AdminForgotPasswordController {

    @Autowired
    private AdminDAO adminDAO;

    /**
     * Step 1: Verify Email Existence
     */
    @PostMapping("/verify-email")
    public Map<String, Object> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, Object> response = new HashMap<>();

        if (email == null || email.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Email cannot be empty");
            return response;
        }

        Admin admin = adminDAO.findByEmail(email);
        if (admin == null) {
            response.put("status", "error");
            response.put("message", "Email not found");
        } else {
            response.put("status", "success");
            response.put("message", "Email verified successfully");
        }

        return response;
    }

    /**
     * Step 2: Reset Password
     */
    @PostMapping("/reset-password")
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String confirmPassword = request.get("confirmPassword");

        Map<String, Object> response = new HashMap<>();

        // Basic validation
        if (email == null || password == null || confirmPassword == null ||
            email.trim().isEmpty() || password.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "All fields are required");
            return response;
        }

        if (!password.equals(confirmPassword)) {
            response.put("status", "error");
            response.put("message", "Passwords do not match");
            return response;
        }

        Admin admin = adminDAO.findByEmail(email);
        if (admin == null) {
            response.put("status", "error");
            response.put("message", "Email not found");
            return response;
        }

        // ✅ Just save plain text password (not encrypted)
        admin.setPassword(password);
        adminDAO.updateAdmin(admin);

        response.put("status", "success");
        response.put("message", "Password updated successfully");
        return response;
    }
}
