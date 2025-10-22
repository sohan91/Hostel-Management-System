package com.example.HostelManagement.service;

import com.example.HostelManagement.dao.AdminAuthDAO;
import com.example.HostelManagement.entities.admin.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    @Autowired
    private AdminAuthDAO adminAuthDAO;

    public boolean authenticate(String email, String password) {
        return adminAuthDAO.validateAdminCredentials(email, password);
    }

    public Admin getAdminByEmail(String email) {
        return adminAuthDAO.findByAdminEmail(email);
    }
}