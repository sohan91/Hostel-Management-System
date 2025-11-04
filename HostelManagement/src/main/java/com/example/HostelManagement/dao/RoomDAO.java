package com.example.HostelManagement.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.HostelManagement.dto.RoomDTO;

@Repository
public class RoomDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean saveRoom(RoomDTO roomDTO) {
        System.out.println("💾 RoomDAO.saveRoom() called: " + roomDTO.getRoomNumber());

        // Add debug to see what's actually in the database
        debugRoomExistence(roomDTO.getAdminId(), roomDTO.getRoomNumber(), roomDTO.getFloorNumber());

        try {
            String sql = """
            INSERT INTO Rooms (admin_id, sharing_type_id, room_number, floor_number, room_status, current_occupancy, created_at)
            VALUES (?, ?, ?, ?, 'Available', 0, NOW())
            """;

            System.out.println("📝 Executing room insert SQL for room: " + roomDTO.getRoomNumber());

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

        } catch (org.springframework.dao.DuplicateKeyException e) {
            System.out.println("❌ Room already exists on this floor: " + roomDTO.getRoomNumber() + ", Floor: " + roomDTO.getFloorNumber());
            return false;
        } catch (Exception e) {
            System.out.println("❌ Error saving room: " + e.getMessage());
            return false;
        }
    }
    // Optional: Add a method to check if room already exists (for uniqueness validation)
    public boolean isRoomNumberExists(Integer adminId, String roomNumber, Integer floorNumber, Integer sharingTypeId) {
        System.out.println("🔍 Checking room uniqueness: " + roomNumber + ", Floor: " + floorNumber + ", Admin: " + adminId);

        try {
            // Remove sharing_type_id from the check since your constraint is only on (admin_id, room_number, floor_number)
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
            System.out.println("🔍 Room uniqueness check: " + (exists ? "EXISTS" : "UNIQUE"));

            return exists;

        } catch (Exception e) {
            System.out.println("❌ Error checking room uniqueness: " + e.getMessage());
            return false;
        }
    }
    public void debugRoomExistence(Integer adminId, String roomNumber, Integer floorNumber) {
        System.out.println("🐛 DEBUG: Checking if room exists - Room: " + roomNumber + ", Floor: " + floorNumber + ", Admin: " + adminId);

        try {
            String sql = "SELECT room_id, room_number, floor_number FROM Rooms WHERE admin_id = ? AND room_number = ? AND floor_number = ?";

            jdbcTemplate.query(sql, new Object[]{adminId, roomNumber, floorNumber}, (rs, rowNum) -> {
                System.out.println("🐛 DEBUG - FOUND EXISTING ROOM: ID=" + rs.getInt("room_id") +
                        ", Room=" + rs.getString("room_number") +
                        ", Floor=" + rs.getInt("floor_number"));
                return null;
            });

            // Also check all rooms for this admin to see what exists
            String allRoomsSql = "SELECT room_id, room_number, floor_number FROM Rooms WHERE admin_id = ? ORDER BY floor_number, room_number";
            jdbcTemplate.query(allRoomsSql, new Object[]{adminId}, (rs, rowNum) -> {
                System.out.println("🐛 DEBUG ALL ROOMS: ID=" + rs.getInt("room_id") +
                        ", Room=" + rs.getString("room_number") +
                        ", Floor=" + rs.getInt("floor_number"));
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

        // FIXED: Improved SQL with better NULL handling and explicit column names
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
            LEFT JOIN SharingType st ON r.sharing_type_id = st.sharing_type_id AND r.admin_id = st.admin_id
            WHERE r.admin_id = ?
            ORDER BY r.floor_number, r.room_number
            """;

        System.out.println("📝 Executing SQL query for adminId: " + adminId);

        try {
            List<RoomDTO> result = jdbcTemplate.query(sql, new Object[]{adminId}, this::mapRoomDTO);
            System.out.println("✅ SQL query completed, found " + result.size() + " rooms");

            // Debug: Print all retrieved rooms
            if (!result.isEmpty()) {
                System.out.println("🐛 DEBUG - Retrieved Rooms:");
                result.forEach(room -> {
                    System.out.println("🐛 Room: " + room.getRoomNumber() +
                            " | Sharing: " + room.getSharingTypeName() +
                            " | Capacity: " + room.getSharingCapacity() +
                            " | Price: " + room.getPrice() +
                            " | Floor: " + room.getFloorNumber());
                });
            }

            return result;
        } catch (Exception e) {
            System.out.println("❌ SQL query failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch rooms", e); // Don't silently return empty list
        }
    }

    // Debug method to check database connection and data
    public void debugDatabaseConnection(Integer adminId) {
        System.out.println("🐛 DEBUG: Checking database connection and data for adminId: " + adminId);

        // 1. Check Rooms table directly
        String roomsSql = "SELECT COUNT(*) as room_count FROM Rooms WHERE admin_id = ?";
        try {
            Integer roomCount = jdbcTemplate.queryForObject(roomsSql, new Object[]{adminId}, Integer.class);
            System.out.println("🐛 DEBUG: Direct Rooms query count: " + roomCount);

            // Show actual room data with sharing type info
            String roomDataSql = """
                SELECT r.room_id, r.room_number, r.floor_number, r.room_status, 
                       r.current_occupancy, r.sharing_type_id, r.admin_id,
                       st.type_name, st.capacity, st.sharing_fee
                FROM Rooms r 
                LEFT JOIN SharingType st ON r.sharing_type_id = st.sharing_type_id 
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
                        ", AdminId: " + rs.getInt("admin_id"));
                return null;
            });
        } catch (Exception e) {
            System.out.println("❌ DEBUG: Direct Rooms query failed: " + e.getMessage());
        }

        // 2. Check SharingType table for this admin
        String sharingSql = "SELECT COUNT(*) as sharing_count FROM SharingType WHERE admin_id = ?";
        try {
            Integer sharingCount = jdbcTemplate.queryForObject(sharingSql, new Object[]{adminId}, Integer.class);
            System.out.println("🐛 DEBUG: SharingType count for admin: " + sharingCount);

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
            System.out.println("❌ DEBUG: SharingType query failed: " + e.getMessage());
        }
    }

    public List<RoomDTO> getAvailableRooms(Integer adminId) {
        System.out.println("🔍 RoomDAO.getAvailableRooms() called with adminId: " + adminId);

        // FIXED: Improved SQL with better fallback
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
            LEFT JOIN SharingType st ON r.sharing_type_id = st.sharing_type_id AND r.admin_id = st.admin_id
            WHERE r.admin_id = ? AND r.room_status = 'Available'
            ORDER BY r.floor_number, r.room_number
            """;

        System.out.println("📝 Executing available rooms SQL query for adminId: " + adminId);

        try {
            List<RoomDTO> result = jdbcTemplate.query(sql, new Object[]{adminId}, this::mapRoomDTO);
            System.out.println("✅ Available rooms SQL query completed, found " + result.size() + " rooms");
            return result;
        } catch (Exception e) {
            System.out.println("❌ Available rooms query failed: " + e.getMessage());
            throw new RuntimeException("Failed to fetch available rooms", e);
        }
    }

    public List<RoomDTO> getRoomsByFloor(Integer adminId, Integer floorNumber) {
        System.out.println("🔍 RoomDAO.getRoomsByFloor() called with adminId: " + adminId + ", floor: " + floorNumber);

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
            LEFT JOIN SharingType st ON r.sharing_type_id = st.sharing_type_id AND r.admin_id = st.admin_id
            WHERE r.admin_id = ? AND r.floor_number = ?
            ORDER BY r.room_number
            """;

        System.out.println("📝 Executing floor rooms SQL query for adminId: " + adminId + ", floor: " + floorNumber);

        try {
            List<RoomDTO> result = jdbcTemplate.query(sql, new Object[]{adminId, floorNumber}, this::mapRoomDTO);
            System.out.println("✅ Floor rooms SQL query completed, found " + result.size() + " rooms");
            return result;
        } catch (Exception e) {
            System.out.println("❌ Floor rooms query failed: " + e.getMessage());
            throw new RuntimeException("Failed to fetch floor rooms", e);
        }
    }

    public RoomDTO getRoomById(Integer roomId, Integer adminId) {
        System.out.println("🔍 RoomDAO.getRoomById() called with roomId: " + roomId + ", adminId: " + adminId);

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
            LEFT JOIN SharingType st ON r.sharing_type_id = st.sharing_type_id AND r.admin_id = st.admin_id
            WHERE r.room_id = ? AND r.admin_id = ?
            """;

        System.out.println("📝 Executing room by ID SQL query for roomId: " + roomId + ", adminId: " + adminId);

        try {
            RoomDTO result = jdbcTemplate.queryForObject(sql, new Object[]{roomId, adminId}, this::mapRoomDTO);
            System.out.println("✅ Room by ID SQL query completed, found room: " + result.getRoomNumber());
            return result;
        } catch (EmptyResultDataAccessException e) {
            System.out.println("❌ Room by ID SQL query completed, no room found");
            return null;
        } catch (Exception e) {
            System.out.println("❌ Room by ID query failed: " + e.getMessage());
            throw new RuntimeException("Failed to fetch room by ID", e);
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
                    " - Floor: " + room.getFloorNumber());

            return room;
        } catch (SQLException e) {
            System.out.println("❌ Error mapping RoomDTO: " + e.getMessage());
            throw e;
        }
    }
}