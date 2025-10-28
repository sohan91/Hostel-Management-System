package com.example.HostelManagement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.HostelManagement.dao.AdminDAO;
import com.example.HostelManagement.entities.hostel.admin.Admin;

@Service
public class AdminService {

    private final AdminDAO adminDAO;

    public AdminService(AdminDAO adminDAO) {
        this.adminDAO = adminDAO;
    }

    @Transactional
    public void saveAdmin(Admin admin) {
        adminDAO.saveAdmin(admin);
    }
}
