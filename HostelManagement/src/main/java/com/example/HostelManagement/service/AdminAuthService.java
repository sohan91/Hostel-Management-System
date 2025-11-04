package com.example.HostelManagement.service;

import java.util.List;

import com.example.HostelManagement.dto.SharingDetailsDTO;
import org.springframework.stereotype.Service;
import com.example.HostelManagement.dao.AdminAuthDAO;
import com.example.HostelManagement.entities.hostel.admin.Admin;

@Service
public class AdminAuthService {

    private final AdminAuthDAO adminAuthDAO;

    // Add this constructor for dependency injection
    public AdminAuthService(AdminAuthDAO adminAuthDAO) {
        this.adminAuthDAO = adminAuthDAO;
        System.out.println("AdminAuthService initialized with DAO: " + (adminAuthDAO != null));
    }

    public boolean authenticate(String email, String password) {
        try {
            System.out.println("Authenticating: " + email);
            boolean result = adminAuthDAO.validateAdminCredentials(email, password);
            System.out.println("Authentication result: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Admin getAdminByEmail(String email) {
        try {
            System.out.println("Getting admin by email: " + email);
            // Use getAdminByEmail instead of findByAdminEmail to match your interface
            Admin admin = adminAuthDAO.getAdminByEmail(email);
            System.out.println("Found admin: " + (admin != null ? admin.getEmail() : "null"));
            return admin;
        } catch (Exception e) {
            System.out.println("Error getting admin by email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<SharingDetailsDTO> sharingTypesList(Integer id) {
        try {
            System.out.println("Getting sharing types for admin ID: " + id);
            List<SharingDetailsDTO> sharingTypes = adminAuthDAO.sharingTypesList(id);
            System.out.println("Found sharing types: " + (sharingTypes != null ? sharingTypes.size() : "null"));
            return sharingTypes;
        } catch (Exception e) {
            System.out.println("Error getting sharing types: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

}