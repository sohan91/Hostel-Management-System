package com.example.HostelManagement.service;

import com.example.HostelManagement.dao.AdminDAO;
import com.example.HostelManagement.entities.admin.Admin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
