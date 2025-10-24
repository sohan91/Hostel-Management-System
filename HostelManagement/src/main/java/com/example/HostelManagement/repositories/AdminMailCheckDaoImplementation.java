package com.example.HostelManagement.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.HostelManagement.dao.AdminMailExistCheckDao;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Repository
public class AdminMailCheckDaoImplementation implements AdminMailExistCheckDao{

    private EntityManager entityManager;

    @Autowired
    public  AdminMailCheckDaoImplementation(EntityManager entity)
    {
       this.entityManager = entity;
    }
    @Override
    @Transactional
    public boolean emailExists(String email) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(a) FROM Admin a WHERE a.email = :email",Long.class)
        .setParameter("email", email).getSingleResult();
        return count>0;
    }

}
