package com.example.HostelManagement.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.HostelManagement.dto.SharingDetailsDTO;

@Repository
public class AddNewSharingTypeRepo {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Updated: Check if sharing type NAME exists for specific admin (not just capacity)
    public boolean isSharingTypeExists(Integer adminId, Integer capacity) {
        System.out.println(" Checking sharing type uniqueness for Admin " + adminId + ": Capacity " + capacity);

        try {
            String typeName = capacity + "-Sharing";
            String sql = "SELECT COUNT(*) FROM sharing_type WHERE admin_id = ? AND type_name = ?";

            Integer count = jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{adminId, typeName},
                    Integer.class
            );

            boolean exists = count != null && count > 0;
            System.out.println(" Sharing type uniqueness check for Admin " + adminId + ": " + typeName + " = " + (exists ? "EXISTS" : "UNIQUE"));

            return exists;

        } catch (Exception e) {
            System.out.println("Error checking sharing type uniqueness: " + e.getMessage());
            return false;
        }
    }

    public boolean saveSharingType(SharingDetailsDTO sharingDTO) {
        System.out.println(" NewSharingTypeRepo.saveSharingType() called for Admin " + sharingDTO.getAdminId() + 
                         ": " + sharingDTO.getSharingCapacity() + "-Sharing");

        // First check if sharing type name already exists for this admin
        if (isSharingTypeExists(sharingDTO.getAdminId(), sharingDTO.getSharingCapacity())) {
            System.out.println(" Sharing type " + sharingDTO.getSharingCapacity() + "-Sharing already exists for admin " + sharingDTO.getAdminId());
            return false;
        }

        try {
            String sql = """
            INSERT INTO sharing_type (admin_id, type_name, capacity, sharing_fee, description, created_at)
            VALUES (?, ?, ?, ?, ?, NOW())
            """;

            // Generate type name from capacity
            String typeName = sharingDTO.getSharingCapacity() + "-Sharing";

            System.out.println(" Executing sharing type insert SQL for Admin " + sharingDTO.getAdminId() + ": " + typeName);

            int rowsAffected = jdbcTemplate.update(
                    sql,
                    sharingDTO.getAdminId(),
                    typeName,
                    sharingDTO.getSharingCapacity(),
                    sharingDTO.getSharingFee(),
                    sharingDTO.getDescription()
            );

            boolean success = rowsAffected > 0;
            System.out.println(" Sharing type insert result for Admin " + sharingDTO.getAdminId() + ": " + (success ? "SUCCESS" : "FAILED") +
                    " - Rows affected: " + rowsAffected);

            return success;

        } catch (org.springframework.dao.DuplicateKeyException e) {
            System.out.println(" Sharing type name already exists for admin " + sharingDTO.getAdminId() + ": " + sharingDTO.getSharingCapacity() + "-Sharing");
            return false;
        } catch (Exception e) {
            System.out.println(" Error saving sharing type: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Method to get all sharing types for an admin
    public java.util.List<SharingDetailsDTO> getSharingTypesByAdmin(Integer adminId) {
        System.out.println(" NewSharingTypeRepo.getSharingTypesByAdmin() called for admin: " + adminId);

        try {
            String sql = """
                SELECT sharing_type_id, admin_id, type_name, capacity, sharing_fee, description, created_at
                FROM sharing_type
                WHERE admin_id = ?
                ORDER BY capacity
                """;

            java.util.List<SharingDetailsDTO> result = jdbcTemplate.query(sql, new Object[]{adminId}, (rs, rowNum) -> {
                SharingDetailsDTO sharingType = new SharingDetailsDTO();
                sharingType.setSharingTypeId(rs.getInt("sharing_type_id"));
                sharingType.setAdminId(rs.getInt("admin_id"));
                sharingType.setTypeName(rs.getString("type_name"));
                sharingType.setSharingCapacity(rs.getInt("capacity"));
                sharingType.setSharingFee(rs.getBigDecimal("sharing_fee"));
                sharingType.setDescription(rs.getString("description"));
                
                // Handle created_at timestamp
                java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    sharingType.setCreatedAt(createdAt.toLocalDateTime());
                }
                
                return sharingType;
            });

            System.out.println("Found " + result.size() + " sharing types for admin " + adminId);
            return result;

        } catch (Exception e) {
            System.out.println(" Error fetching sharing types for admin " + adminId + ": " + e.getMessage());
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    // NEW: Method to validate if sharing type belongs to admin
    public boolean isSharingTypeBelongsToAdmin(Integer sharingTypeId, Integer adminId) {
        System.out.println(" Checking if sharing type " + sharingTypeId + " belongs to admin " + adminId);

        try {
            String sql = "SELECT COUNT(*) FROM sharing_type WHERE sharing_type_id = ? AND admin_id = ?";
            
            Integer count = jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{sharingTypeId, adminId},
                    Integer.class
            );

            boolean belongs = count != null && count > 0;
            System.out.println("Sharing type ownership check: " + (belongs ? "BELONGS" : "DOES NOT BELONG"));
            
            return belongs;

        } catch (Exception e) {
            System.out.println(" Error checking sharing type ownership: " + e.getMessage());
            return false;
        }
    }

    // NEW: Method to get sharing type by ID with admin validation
    public SharingDetailsDTO getSharingTypeByIdAndAdmin(Integer sharingTypeId, Integer adminId) {
        System.out.println("Getting sharing type by ID: " + sharingTypeId + " for admin: " + adminId);

        try {
            String sql = """
                SELECT sharing_type_id, admin_id, type_name, capacity, sharing_fee, description, created_at
                FROM sharing_type 
                WHERE sharing_type_id = ? AND admin_id = ?
                """;

            return jdbcTemplate.queryForObject(sql, new Object[]{sharingTypeId, adminId}, (rs, rowNum) -> {
                SharingDetailsDTO sharingType = new SharingDetailsDTO();
                sharingType.setSharingTypeId(rs.getInt("sharing_type_id"));
                sharingType.setAdminId(rs.getInt("admin_id"));
                sharingType.setTypeName(rs.getString("type_name"));
                sharingType.setSharingCapacity(rs.getInt("capacity"));
                sharingType.setSharingFee(rs.getBigDecimal("sharing_fee"));
                sharingType.setDescription(rs.getString("description"));
                
                java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    sharingType.setCreatedAt(createdAt.toLocalDateTime());
                }
                
                return sharingType;
            });

        } catch (Exception e) {
            System.out.println(" Sharing type not found or doesn't belong to admin: " + sharingTypeId + " for admin " + adminId);
            return null;
        }
    }
}