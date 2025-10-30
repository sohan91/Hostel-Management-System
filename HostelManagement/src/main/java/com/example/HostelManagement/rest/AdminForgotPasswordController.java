package com.example.HostelManagement.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.HostelManagement.dao.AdminDAO;
import com.example.HostelManagement.entities.hostel.admin.Admin;

@RestController
@RequestMapping("/hostel/api")
@CrossOrigin(origins = "*")
public class AdminForgotPasswordController {

    @Autowired
    private AdminDAO adminDAO;


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
