package com.example.HostelManagement.dao;

import java.util.List;
import com.example.HostelManagement.entities.hostel.SharingType;
import com.example.HostelManagement.entities.hostel.admin.Admin;

public interface AdminAuthDAO {

    Admin findByAdminEmail(String email);

    boolean validateAdminCredentials(String email, String password);

    Admin getAdminByEmail(String email);
    List<SharingType> getSharingTypesByAdminId(String email);

    List<SharingType> sharingTypesList(int adminId);
}
