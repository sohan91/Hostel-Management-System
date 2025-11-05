package com.example.HostelManagement.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.HostelManagement.dto.SharingDetailsDTO;

@Repository
public class AddNewSharingTypeRepo {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean isSharingTypeExists(Integer adminId, Integer capacity) {
        System.out.println("🔍 Checking sharing type uniqueness: Capacity " + capacity + ", Admin: " + adminId);

        try {
            String sql = "SELECT COUNT(*) FROM SharingType WHERE admin_id = ? AND capacity = ?";

            Integer count = jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{adminId, capacity},
                    Integer.class
            );

            boolean exists = count != null && count > 0;
            System.out.println("🔍 Sharing type uniqueness check: " + (exists ? "EXISTS" : "UNIQUE"));

            return exists;

        } catch (Exception e) {
            System.out.println("❌ Error checking sharing type uniqueness: " + e.getMessage());
            return false;
        }
    }

    public boolean saveSharingType(SharingDetailsDTO sharingDTO) {
        System.out.println("💾 NewSharingTypeRepo.saveSharingType() called: " + sharingDTO.getSharingCapacity() + "-Sharing");

        try {
            String sql = """
            INSERT INTO sharingtype(admin_id, type_name, capacity, sharing_fee, description, created_at)
            VALUES (?, ?, ?, ?, ?, NOW())
            """;

            // Generate type name from capacity
            String typeName = sharingDTO.getSharingCapacity() + "-Sharing";

            System.out.println("📝 Executing sharing type insert SQL for: " + typeName);

            int rowsAffected = jdbcTemplate.update(
                    sql,
                    sharingDTO.getAdminId(),
                    typeName,
                    sharingDTO.getSharingCapacity(),
                    sharingDTO.getSharingFee(),
                    sharingDTO.getDescription()
            );

            boolean success = rowsAffected > 0;
            System.out.println("✅ Sharing type insert result: " + (success ? "SUCCESS" : "FAILED"));

            return success;

        } catch (org.springframework.dao.DuplicateKeyException e) {
            System.out.println("❌ Sharing type already exists for capacity: " + sharingDTO.getSharingCapacity());
            return false;
        } catch (Exception e) {
            System.out.println("❌ Error saving sharing type: " + e.getMessage());
            return false;
        }
    }

    // Optional: Method to get all sharing types for an admin
    public java.util.List<SharingDetailsDTO> getSharingTypesByAdmin(Integer adminId) {
        System.out.println("🔍 NewSharingTypeRepo.getSharingTypesByAdmin() called for admin: " + adminId);

        try {
            String sql = """
                SELECT sharing_type_id, admin_id, type_name, capacity, sharing_fee, description 
                FROM SharingType
                WHERE admin_id = ?
                ORDER BY capacity
                """;

            return jdbcTemplate.query(sql, new Object[]{adminId}, (rs, rowNum) -> {
                SharingDetailsDTO sharingType = new SharingDetailsDTO();
                sharingType.setSharingTypeId(rs.getInt("sharing_type_id"));
                sharingType.setAdminId(rs.getInt("admin_id"));
                sharingType.setTypeName(rs.getString("type_name"));
                sharingType.setSharingCapacity(rs.getInt("capacity"));
                sharingType.setSharingFee(rs.getBigDecimal("sharing_fee"));
                sharingType.setDescription(rs.getString("description"));
                return sharingType;
            });

        } catch (Exception e) {
            System.out.println("❌ Error fetching sharing types: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
}