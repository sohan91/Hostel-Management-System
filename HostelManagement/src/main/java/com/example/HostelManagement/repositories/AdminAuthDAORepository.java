package com.example.HostelManagement.repositories;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.example.HostelManagement.dao.AdminAuthDAO;
import com.example.HostelManagement.entities.hostel.SharingType;
import com.example.HostelManagement.entities.hostel.admin.Admin;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

@Repository
public class AdminAuthDAORepository implements AdminAuthDAO {

    private final EntityManager entityManager;

    public AdminAuthDAORepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Admin findByAdminEmail(String email) {
        try {
            String jpql = "SELECT a FROM Admin a WHERE a.email = :email";
            TypedQuery<Admin> query = entityManager.createQuery(jpql, Admin.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean validateAdminCredentials(String email, String password) {
        Admin admin = findByAdminEmail(email);
        return admin != null && admin.getPassword().equals(password);
    }

    @Override
    public Admin getAdminByEmail(String email) {
        return findByAdminEmail(email);
    }

    // ✅ Get all sharing types for a given admin email
    @Override
    public List<SharingType> getSharingTypesByAdminId(String email) {
        Admin admin = findByAdminEmail(email);
        if (admin == null) return List.of();

        String jpql = "SELECT s FROM SharingType s WHERE s.admin.adminId = :adminId";
        TypedQuery<SharingType> query = entityManager.createQuery(jpql, SharingType.class);
        query.setParameter("adminId", admin.getAdminId());
        return query.getResultList();
    }

    // ✅ Optional variant: directly by adminId
    @Override
    public List<SharingType> sharingTypesList(int adminId) {
        String jpql = "SELECT s FROM SharingType s WHERE s.admin.adminId = :adminId";
        TypedQuery<SharingType> query = entityManager.createQuery(jpql, SharingType.class);
        query.setParameter("adminId", adminId);
        return query.getResultList();
    }
}
    