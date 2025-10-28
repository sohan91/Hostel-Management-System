package com.example.HostelManagement.dao;

import com.example.HostelManagement.entities.hostel.admin.Admin;

public interface AdminAuthDAO {
    Admin findByAdminEmail(String email);
    boolean validateAdminCredentials(String email, String password);
}
