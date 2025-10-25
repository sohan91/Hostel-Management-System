package com.example.HostelManagement.dao;

import com.example.HostelManagement.entities.admin.Admin;

public interface AdminDAO {
    Admin saveAdmin(Admin admin);
    Admin findByEmail(String email);
     Admin updateAdmin(Admin admin);
}
