package com.example.HostelManagement.dao;

import com.example.HostelManagement.entities.hostel.admin.Admin;

import com.example.HostelManagement.entities.hostel.admin.Admin;

public interface AdminDAO {
    Admin saveAdmin(Admin admin);
    Admin findByEmail(String email);
     Admin updateAdmin(Admin admin);
}
