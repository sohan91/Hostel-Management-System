package com.example.HostelManagement.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException; // <-- NEW IMPORT
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.HostelManagement.dto.RoomDTO;

@Repository
public class RoomDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean saveRoom(RoomDTO roomDTO) {
        System.out.println("💾 RoomDAO.saveRoom() called: " + roomDTO.getRoomNumber() + " for Admin: " + roomDTO.getAdminId());

        // First, verify that the sharing type belongs to the same admin
        if (!isSharingTypeBelongsToAdmin(roomDTO.getSharingTypeId(), roomDTO.getAdminId())) {
            System.out.println("❌ Sharing type " + roomDTO.getSharingTypeId() + " does not belong to admin " + roomDTO.getAdminId());
            return false;
        }

        // Check if room number already exists for this specific admin and floor
        if (isRoomNumberExistsOnFloor(roomDTO.getAdminId(), roomDTO.getRoomNumber(), roomDTO.getFloorNumber())) {
            System.out.println("❌ Room " + roomDTO.getRoomNumber() + " already exists on floor " + 
                             roomDTO.getFloorNumber() + " for admin " + roomDTO.getAdminId());
            return false;
        }

        try {
            String sql = """
            INSERT INTO Rooms (admin_id, sharing_type_id, room_number, floor_number, room_status, current_occupancy, created_at)
            VALUES (?, ?, ?, ?, 'Available', 0, NOW())
            """;

            System.out.println("📝 Executing room insert SQL for room: " + roomDTO.getRoomNumber() + 
                              ", Admin: " + roomDTO.getAdminId() + ", Floor: " + roomDTO.getFloorNumber());

            int rowsAffected = jdbcTemplate.update(
                    sql,
                    roomDTO.getAdminId(),
                    roomDTO.getSharingTypeId(),
                    roomDTO.getRoomNumber(),
                    roomDTO.getFloorNumber()
            );

            boolean success = rowsAffected > 0;
            System.out.println("✅ Room insert result: " + (success ? "SUCCESS" : "FAILED") +
                              " - Rows affected: " + rowsAffected);

            return success;

        } catch (DuplicateKeyException e) {
            System.out.println("❌ Room already exists on this floor for admin " + roomDTO.getAdminId() + 
                              ": " + roomDTO.getRoomNumber() + ", Floor: " + roomDTO.getFloorNumber());
            return false;
        } catch (DataIntegrityViolationException e) { // <-- SPECIFICALLY CATCHING FOREIGN KEY ERROR
            System.out.println("❌ Critical Database Error (Foreign Key Constraint Failed on sharing_type_id): " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("❌ Generic Error saving room: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Check if sharing type belongs to the same admin
    private boolean isSharingTypeBelongsToAdmin(Integer sharingTypeId, Integer adminId) {
        if (sharingTypeId == null || adminId == null) {
            System.out.println("❌ Sharing type ID or Admin ID is null");
            return false;
        }

        try {
            // Try with lowercase table name first
            String sql = "SELECT COUNT(*) FROM sharing_type WHERE sharing_type_id = ? AND admin_id = ?";
            Integer count = jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{sharingTypeId, adminId},
                    Integer.class
            );
            boolean belongs = count != null && count > 0;
            System.out.println("🔍 Sharing type " + sharingTypeId + " belongs to admin " + adminId + ": " + belongs);
            
            // Enhanced debugging - show what sharing types actually exist
            if (!belongs) {
                System.out.println("❌ Sharing type validation failed! Checking available sharing types...");
                debugAvailableSharingTypes(adminId);
                
                // Also check if the sharing type ID exists at all
                String checkExistsSql = "SELECT COUNT(*) FROM sharing_type WHERE sharing_type_id = ?";
                Integer existsCount = jdbcTemplate.queryForObject(checkExistsSql, new Object[]{sharingTypeId}, Integer.class);
                System.out.println("🔍 Sharing type ID " + sharingTypeId + " exists in database: " + (existsCount != null && existsCount > 0));
            }
            
            return belongs;
        } catch (Exception e) {
            System.out.println("❌ Error checking sharing type ownership: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void debugAvailableSharingTypes(Integer adminId) {
        try {
            String sql = "SELECT sharing_type_id, type_name, capacity, admin_id FROM sharing_type WHERE admin_id = ?";
            List<String> sharingTypes = jdbcTemplate.query(sql, new Object[]{adminId}, (rs, rowNum) -> {
                return "ID=" + rs.getInt("sharing_type_id") + 
                        ", Name=" + rs.getString("type_name") + 
                        ", Capacity=" + rs.getInt("capacity") +
                        ", Admin=" + rs.getInt("admin_id");
            });
            
            if (sharingTypes.isEmpty()) {
                System.out.println("🐛 DEBUG - No sharing types found for admin " + adminId);
            } else {
                System.out.println("🐛 DEBUG - Available Sharing Types for Admin " + adminId + ":");
                sharingTypes.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.out.println("❌ DEBUG: Error fetching available sharing types: " + e.getMessage());
        }
    }

   
    public boolean isRoomNumberExistsOnFloor(Integer adminId, String roomNumber, Integer floorNumber) {
        System.out.println("🔍 Checking room on floor for Admin " + adminId + ": " + roomNumber + ", Floor: " + floorNumber);

        try {
            String sql = """
            SELECT COUNT(*) FROM Rooms 
            WHERE admin_id = ? AND room_number = ? AND floor_number = ?
            """;

            Integer count = jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{adminId, roomNumber, floorNumber},
                    Integer.class
            );

            boolean exists = count != null && count > 0;
            System.out.println("🔍 Room on floor check for Admin " + adminId + ": " + roomNumber + 
                              " on floor " + floorNumber + " = " + (exists ? "EXISTS" : "UNIQUE"));
            return exists;

        } catch (Exception e) {
            System.out.println("❌ Error checking room on floor: " + e.getMessage());
            return false;
        }
    }

    // Keep the old method for backward compatibility (updated parameter order)
    public boolean isRoomNumberExists(Integer adminId, String roomNumber, Integer floorNumber, Integer sharingTypeId) {
        System.out.println("🔍 Checking room uniqueness: " + roomNumber + ", Floor: " + floorNumber + ", Admin: " + adminId);

        // Now we only check room number and floor (ignore sharingTypeId)
        return isRoomNumberExistsOnFloor(adminId, roomNumber, floorNumber);
    }

    // New method: Check if room number exists anywhere for this admin
    public boolean isRoomNumberExistsAnywhere(Integer adminId, String roomNumber) {
        System.out.println("🔍 Checking if room exists anywhere for Admin " + adminId + ": " + roomNumber);

        try {
            String sql = """
            SELECT COUNT(*) FROM Rooms 
            WHERE admin_id = ? AND room_number = ?
            """;

            Integer count = jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{adminId, roomNumber},
                    Integer.class
            );

            boolean exists = count != null && count > 0;
            System.out.println("🔍 Room exists anywhere for Admin " + adminId + ": " + roomNumber + " = " + (exists ? "EXISTS" : "UNIQUE"));
            return exists;

        } catch (Exception e) {
            System.out.println("❌ Error checking room existence: " + e.getMessage());
            return false;
        }
    }

    public void debugRoomExistence(Integer adminId, String roomNumber, Integer floorNumber) {
        System.out.println("🐛 DEBUG: Checking if room exists - Admin: " + adminId + ", Room: " + roomNumber + ", Floor: " + floorNumber);

        try {
            String sql = "SELECT room_id, room_number, floor_number, admin_id FROM Rooms WHERE admin_id = ? AND room_number = ? AND floor_number = ?";

            jdbcTemplate.query(sql, new Object[]{adminId, roomNumber, floorNumber}, (rs, rowNum) -> {
                System.out.println("🐛 DEBUG - FOUND EXISTING ROOM: ID=" + rs.getInt("room_id") +
                        ", Room=" + rs.getString("room_number") +
                        ", Floor=" + rs.getInt("floor_number") +
                        ", Admin=" + rs.getInt("admin_id"));
                return null;
            });

            // Also check all rooms for this admin to see what exists
            String allRoomsSql = "SELECT room_id, room_number, floor_number, admin_id FROM Rooms WHERE admin_id = ? ORDER BY floor_number, room_number";
            jdbcTemplate.query(allRoomsSql, new Object[]{adminId}, (rs, rowNum) -> {
                System.out.println("🐛 DEBUG ALL ROOMS: ID=" + rs.getInt("room_id") +
                        ", Room=" + rs.getString("room_number") +
                        ", Floor=" + rs.getInt("floor_number") +
                        ", Admin=" + rs.getInt("admin_id"));
                return null;
            });

        } catch (Exception e) {
            System.out.println("❌ DEBUG: Error checking room existence: " + e.getMessage());
        }
    }

    public List<RoomDTO> getAllRooms(Integer adminId) {
        System.out.println("🔍 RoomDAO.getAllRooms() called with adminId: " + adminId);

        // First, debug the database connection
        debugDatabaseConnection(adminId);

        // FIXED: Updated table name to SharingType (capital S)
        String sql = """
            SELECT 
                r.room_id, 
                r.admin_id, 
                r.room_number, 
                r.floor_number, 
                r.room_status, 
                r.current_occupancy,
                r.created_at,
                r.sharing_type_id,
                COALESCE(st.type_name, CONCAT(st.capacity, '-Sharing')) as sharing_type_name, 
                COALESCE(st.capacity, 2) as sharing_capacity, 
                COALESCE(st.sharing_fee, 5000.00) as price,
                CONCAT(r.current_occupancy, '/', COALESCE(st.capacity, 2)) as occupancy_status
            FROM Rooms r
            LEFT JOIN sharing_type st ON r.sharing_type_id = st.sharing_type_id AND r.admin_id = st.admin_id
            WHERE r.admin_id = ?
            ORDER BY r.floor_number, r.room_number
            """;

        System.out.println("📝 Executing SQL query for adminId: " + adminId);

        try {
            List<RoomDTO> result = jdbcTemplate.query(sql, new Object[]{adminId}, this::mapRoomDTO);
            System.out.println("✅ SQL query completed, found " + result.size() + " rooms for admin " + adminId);

            // Debug: Print all retrieved rooms
            if (!result.isEmpty()) {
                System.out.println("🐛 DEBUG - Retrieved Rooms for Admin " + adminId + ":");
                result.forEach(room -> {
                    System.out.println("🐛 Room: " + room.getRoomNumber() +
                                 " | Sharing: " + room.getSharingTypeName() +
                                 " | Capacity: " + room.getSharingCapacity() +
                                 " | Price: " + room.getPrice() +
                                 " | Floor: " + room.getFloorNumber() +
                                 " | Admin: " + room.getAdminId());
                });
            }

            return result;
        } catch (Exception e) {
            System.out.println("❌ SQL query failed for admin " + adminId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch rooms for admin " + adminId, e);
        }
    }

    // Debug method to check database connection and data
    public void debugDatabaseConnection(Integer adminId) {
        System.out.println("🐛 DEBUG: Checking database connection and data for adminId: " + adminId);

        // 1. Check Rooms table directly
        String roomsSql = "SELECT COUNT(*) as room_count FROM Rooms WHERE admin_id = ?";
        try {
            Integer roomCount = jdbcTemplate.queryForObject(roomsSql, new Object[]{adminId}, Integer.class);
            System.out.println("🐛 DEBUG: Direct Rooms query count for admin " + adminId + ": " + roomCount);

            // Show actual room data with sharing type info
            String roomDataSql = """
                SELECT r.room_id, r.room_number, r.floor_number, r.room_status, 
                        r.current_occupancy, r.sharing_type_id, r.admin_id,
                        st.type_name, st.capacity, st.sharing_fee, st.admin_id as sharing_admin_id
                FROM Rooms r 
                LEFT JOIN sharing_type st ON r.sharing_type_id = st.sharing_type_id AND r.admin_id = st.admin_id
                WHERE r.admin_id = ?
                """;
            jdbcTemplate.query(roomDataSql, new Object[]{adminId}, (rs, rowNum) -> {
                System.out.println("🐛 DEBUG Room Data - ID: " + rs.getInt("room_id") +
                        ", Number: " + rs.getString("room_number") +
                        ", Floor: " + rs.getInt("floor_number") +
                        ", Status: " + rs.getString("room_status") +
                        ", Occupancy: " + rs.getInt("current_occupancy") +
                        ", SharingTypeId: " + rs.getInt("sharing_type_id") +
                        ", TypeName: " + rs.getString("type_name") +
                        ", Capacity: " + rs.getInt("capacity") +
                        ", Fee: " + rs.getDouble("sharing_fee") +
                        ", RoomAdminId: " + rs.getInt("admin_id") +
                        ", SharingAdminId: " + rs.getInt("sharing_admin_id"));
                return null;
            });
        } catch (Exception e) {
            System.out.println("❌ DEBUG: Direct Rooms query failed for admin " + adminId + ": " + e.getMessage());
        }

        // 2. Check SharingType table for this admin
        String sharingSql = "SELECT COUNT(*) as sharing_count FROM SharingType WHERE admin_id = ?";
        try {
            Integer sharingCount = jdbcTemplate.queryForObject(sharingSql, new Object[]{adminId}, Integer.class);
            System.out.println("🐛 DEBUG: SharingType count for admin " + adminId + ": " + sharingCount);

            // Show actual sharing type data
            String sharingDataSql = "SELECT sharing_type_id, type_name, capacity, sharing_fee, admin_id FROM SharingType WHERE admin_id = ?";
            jdbcTemplate.query(sharingDataSql, new Object[]{adminId}, (rs, rowNum) -> {
                System.out.println("🐛 DEBUG SharingType - ID: " + rs.getInt("sharing_type_id") +
                        ", Name: " + rs.getString("type_name") +
                        ", Capacity: " + rs.getInt("capacity") +
                        ", Fee: " + rs.getDouble("sharing_fee") +
                        ", AdminId: " + rs.getInt("admin_id"));
                return null;
            });
        } catch (Exception e) {
            System.out.println("❌ DEBUG: SharingType query failed for admin " + adminId + ": " + e.getMessage());
        }
    }

    public List<RoomDTO> getAvailableRooms(Integer adminId) {
        System.out.println("🔍 RoomDAO.getAvailableRooms() called with adminId: " + adminId);

        // FIXED: Updated table name to SharingType
        String sql = """
            SELECT 
                r.room_id, 
                r.admin_id, 
                r.room_number, 
                r.floor_number, 
                r.room_status, 
                r.current_occupancy,
                r.created_at,
                r.sharing_type_id,
                COALESCE(st.type_name, CONCAT(st.capacity, '-Sharing')) as sharing_type_name, 
                COALESCE(st.capacity, 2) as sharing_capacity, 
                COALESCE(st.sharing_fee, 5000.00) as price,
                CONCAT(r.current_occupancy, '/', COALESCE(st.capacity, 2)) as occupancy_status
            FROM Rooms r
            LEFT JOIN sharing_type st ON r.sharing_type_id = st.sharing_type_id AND r.admin_id = st.admin_id
            WHERE r.admin_id = ? AND r.room_status = 'Available'
            ORDER BY r.floor_number, r.room_number
            """;

        System.out.println("📝 Executing available rooms SQL query for adminId: " + adminId);

        try {
            List<RoomDTO> result = jdbcTemplate.query(sql, new Object[]{adminId}, this::mapRoomDTO);
            System.out.println("✅ Available rooms SQL query completed, found " + result.size() + " rooms for admin " + adminId);
            return result;
        } catch (Exception e) {
            System.out.println("❌ Available rooms query failed for admin " + adminId + ": " + e.getMessage());
            throw new RuntimeException("Failed to fetch available rooms for admin " + adminId, e);
        }
    }

    public List<RoomDTO> getRoomsByFloor(Integer adminId, Integer floorNumber) {
        System.out.println("🔍 RoomDAO.getRoomsByFloor() called with adminId: " + adminId + ", floor: " + floorNumber);

        // FIXED: Updated table name to SharingType
        String sql = """
            SELECT 
                r.room_id, 
                r.admin_id, 
                r.room_number, 
                r.floor_number, 
                r.room_status, 
                r.current_occupancy,
                r.created_at,
                r.sharing_type_id,
                COALESCE(st.type_name, CONCAT(st.capacity, '-Sharing')) as sharing_type_name, 
                COALESCE(st.capacity, 2) as sharing_capacity, 
                COALESCE(st.sharing_fee, 5000.00) as price,
                CONCAT(r.current_occupancy, '/', COALESCE(st.capacity, 2)) as occupancy_status
            FROM Rooms r
            LEFT JOIN sharing_type st ON r.sharing_type_id = st.sharing_type_id AND r.admin_id = st.admin_id
            WHERE r.admin_id = ? AND r.floor_number = ?
            ORDER BY r.room_number
            """;

        System.out.println("📝 Executing floor rooms SQL query for adminId: " + adminId + ", floor: " + floorNumber);

        try {
            List<RoomDTO> result = jdbcTemplate.query(sql, new Object[]{adminId, floorNumber}, this::mapRoomDTO);
            System.out.println("✅ Floor rooms SQL query completed, found " + result.size() + " rooms for admin " + adminId);
            return result;
        } catch (Exception e) {
            System.out.println("❌ Floor rooms query failed for admin " + adminId + ": " + e.getMessage());
            throw new RuntimeException("Failed to fetch floor rooms for admin " + adminId, e);
        }
    }

    public RoomDTO getRoomById(Integer roomId, Integer adminId) {
        System.out.println("🔍 RoomDAO.getRoomById() called with roomId: " + roomId + ", adminId: " + adminId);

        // FIXED: Updated table name to SharingType
        String sql = """
            SELECT 
                r.room_id, 
                r.admin_id, 
                r.room_number, 
                r.floor_number, 
                r.room_status, 
                r.current_occupancy,
                r.created_at,
                r.sharing_type_id,
                COALESCE(st.type_name, CONCAT(st.capacity, '-Sharing')) as sharing_type_name, 
                COALESCE(st.capacity, 2) as sharing_capacity, 
                COALESCE(st.sharing_fee, 5000.00) as price,
                CONCAT(r.current_occupancy, '/', COALESCE(st.capacity, 2)) as occupancy_status
            FROM Rooms r
            LEFT JOIN sharing_type st ON r.sharing_type_id = st.sharing_type_id AND r.admin_id = st.admin_id
            WHERE r.room_id = ? AND r.admin_id = ?
            """;

        System.out.println("📝 Executing room by ID SQL query for roomId: " + roomId + ", adminId: " + adminId);

        try {
            RoomDTO result = jdbcTemplate.queryForObject(sql, new Object[]{roomId, adminId}, this::mapRoomDTO);
            System.out.println("✅ Room by ID SQL query completed, found room: " + result.getRoomNumber() + " for admin " + adminId);
            return result;
        } catch (EmptyResultDataAccessException e) {
            System.out.println("❌ Room by ID SQL query completed, no room found for admin " + adminId);
            return null;
        } catch (Exception e) {
            System.out.println("❌ Room by ID query failed for admin " + adminId + ": " + e.getMessage());
            throw new RuntimeException("Failed to fetch room by ID for admin " + adminId, e);
        }
    }

    private RoomDTO mapRoomDTO(ResultSet rs, int rowNum) throws SQLException {
        System.out.println("🗺️ Mapping ResultSet to RoomDTO for row: " + rowNum);

        try {
            RoomDTO room = new RoomDTO();
            room.setRoomId(rs.getInt("room_id"));
            room.setAdminId(rs.getInt("admin_id"));
            room.setRoomNumber(rs.getString("room_number"));
            room.setFloorNumber(rs.getInt("floor_number"));
            room.setRoomStatus(rs.getString("room_status"));
            room.setCurrentOccupancy(rs.getInt("current_occupancy"));

            // Handle potential NULL values for timestamp
            String createdAt = rs.getString("created_at");
            room.setCreatedAt(createdAt != null ? createdAt : "Unknown");

            room.setSharingTypeId(rs.getInt("sharing_type_id"));

            // FIXED: Better handling for sharing type name
            String sharingTypeName = rs.getString("sharing_type_name");
            room.setSharingTypeName(sharingTypeName != null ? sharingTypeName : "Unknown Type");

            room.setSharingCapacity(rs.getInt("sharing_capacity"));

            // FIXED: Handle potential NULL for price
            double price = rs.getDouble("price");
            room.setPrice(price > 0 ? price : 5000.00); // Default price if NULL

            String occupancyStatus = rs.getString("occupancy_status");
            room.setOccupancyStatus(occupancyStatus != null ? occupancyStatus : "0/2");

            System.out.println("✅ Mapped RoomDTO: " + room.getRoomNumber() +
                    " - " + room.getSharingTypeName() +
                    " - Capacity: " + room.getSharingCapacity() +
                    " - ₹" + room.getPrice() +
                    " - Floor: " + room.getFloorNumber() +
                    " - Admin: " + room.getAdminId());

            return room;
        } catch (SQLException e) {
            System.out.println("❌ Error mapping RoomDTO: " + e.getMessage());
            throw e;
        }
    }
}