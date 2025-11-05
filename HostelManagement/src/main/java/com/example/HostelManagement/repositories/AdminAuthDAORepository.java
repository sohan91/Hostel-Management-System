package com.example.HostelManagement.repositories;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.example.HostelManagement.dao.AdminAuthDAO;
import com.example.HostelManagement.dto.SharingDetailsDTO;
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
    public Integer findAdminIdEmail(String email) {
        String query = "SELECT a.id FROM Admin a WHERE a.email = :email";
        try {
            TypedQuery<Integer> typedQuery = entityManager.createQuery(query, Integer.class);
            typedQuery.setParameter("email", email);
            return typedQuery.getSingleResult();

        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    @Override
    public List<SharingDetailsDTO> sharingTypesList(Integer adminId) {
        entityManager.clear();

        String jpql = "SELECT s FROM sharingtype s WHERE s.admin.adminId = :adminId";
        TypedQuery<SharingType> query = entityManager.createQuery(jpql, SharingType.class);
        query.setParameter("adminId", adminId);

        List<SharingType> results = query.getResultList();

        System.out.println("DAO Debug: Fetched " + results.size() + " sharing types by admin ID. Admin ID: " + adminId);
        for (SharingType st : results) {
            System.out.println("  -> ID: " + st.getSharingTypeId() + ", Type: " + st.getTypeName() + ", Capacity: " + st.getCapacity());
        }

        // DTO Mapping Implementation
        return results.stream()
                .map(st -> {
                    // Ensure adminId is correctly extracted, handling possible null Admin reference
                    Integer currentAdminId = (st.getAdmin() != null) ? st.getAdmin().getAdminId() : null;

                    // Assumes SharingType has getSharingTypeId, getCapacity, and getSharingFee methods
                    return new SharingDetailsDTO(
                            currentAdminId,
                            st.getSharingTypeId(),
                            st.getCapacity(),
                            st.getSharingFee(),
                            "success"
                    );
                })
                .collect(Collectors.toList());
    }
}