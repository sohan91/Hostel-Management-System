package com.example.HostelManagement.rest;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.HostelManagement.entities.hostel.admin.Admin;
import com.example.HostelManagement.repositories.AdminAuthDAORepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins="*")
public class AdminController {


    public AdminController(AdminAuthDAORepository adminRepository) {
    }

 public Admin getAdminProfile(HttpSession session) {
    Admin loggedInAdmin = (Admin) session.getAttribute("loggedInUser");
    if (loggedInAdmin != null) {
        return loggedInAdmin;
    }
    return null;
   }
}
