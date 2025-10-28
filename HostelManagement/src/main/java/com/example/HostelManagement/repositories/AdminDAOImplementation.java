package com.example.HostelManagement.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.HostelManagement.dao.AdminDAO;
import com.example.HostelManagement.entities.hostel.admin.Admin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

@Repository
public class AdminDAOImplementation implements AdminDAO {

    private final EntityManager entityManager;

    public AdminDAOImplementation(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Admin saveAdmin(Admin admin) {
        entityManager.persist(admin);
        return admin;
    }

    @Override
    public Admin findByEmail(String email) {
        try {
            // ✅ Use JPQL to query based on email
            TypedQuery<Admin> query = entityManager.createQuery(
                "SELECT a FROM Admin a WHERE a.email = :email", Admin.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null; // return null if no admin found
        }
    }

    @Override
    @Transactional
    public Admin updateAdmin(Admin admin) {
        return entityManager.merge(admin);
    }
}
