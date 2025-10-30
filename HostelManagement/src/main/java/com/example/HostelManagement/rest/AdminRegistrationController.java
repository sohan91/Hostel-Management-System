package com.example.HostelManagement.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.HostelManagement.entities.hostel.admin.Admin;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins="*")
public class AdminRegistrationController {

   
   private final EntityManager entityManager;

   @Autowired
    public AdminRegistrationController(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    @PostMapping("/register")
    @Transactional
    public Map<String,String> registerAdmin(@RequestBody Admin admin)
    {
        Map<String,String> response = new HashMap<>();

        long count = entityManager.createQuery(
            "SELECT COUNT(a) FROM Admin a WHERE a.email = :email", Long.class)
            .setParameter("email",admin.getEmail())
            .getSingleResult();
       if(count>0)
       {
        response.put("status","error");
        response.put("message","Email already registered");
       }
       else
       {
         entityManager.persist(admin);
         response.put("status","success");
         response.put("message","Admin registered successfully");
       }
       return response;
    }
}
