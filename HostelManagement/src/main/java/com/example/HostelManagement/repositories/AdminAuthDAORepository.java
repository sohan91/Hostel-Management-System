package com.example.HostelManagement.repositories;

import org.springframework.stereotype.Repository;

import com.example.HostelManagement.dao.AdminAuthDAO;
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
            // ✅ CORRECT - Use the actual field name 'email' from your entity
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
        // ✅ CORRECT - Use getPassword() which matches your entity field
        return admin != null && admin.getPassword().equals(password);
    }
}