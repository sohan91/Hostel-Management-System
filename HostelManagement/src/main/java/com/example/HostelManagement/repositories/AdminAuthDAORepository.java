package com.example.HostelManagement.repositories;

import com.example.HostelManagement.dao.AdminAuthDAO;
import com.example.HostelManagement.entities.admin.Admin;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

@Repository
public class AdminAuthDAORepository implements AdminAuthDAO {

    private final EntityManager entityManager;

    public AdminAuthDAORepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Admin findByAdminEmail(String email) {
        try {
            String jpql = "SELECT a FROM Admin a WHERE a.adminEmail = :email";
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
        return admin != null && admin.getAdminPassword().equals(password);
    }
}