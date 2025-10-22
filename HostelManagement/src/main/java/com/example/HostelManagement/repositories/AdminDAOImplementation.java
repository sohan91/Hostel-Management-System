package com.example.HostelManagement.repositories;

import com.example.HostelManagement.dao.AdminDAO;
import com.example.HostelManagement.entities.admin.Admin;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
}
