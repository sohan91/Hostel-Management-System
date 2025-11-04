package com.example.HostelManagement.dao;

import java.util.List;

import com.example.HostelManagement.dto.SharingDetailsDTO;
import com.example.HostelManagement.entities.hostel.admin.Admin;

public interface AdminAuthDAO {

    Admin findByAdminEmail(String email);
    Integer findAdminIdEmail(String email);
    boolean validateAdminCredentials(String email, String password);

    Admin getAdminByEmail(String email);

    List<SharingDetailsDTO> sharingTypesList(Integer id);
}
