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

    public List<RoomDTO> getAllRooms(Integer adminId) {
        System.out.println("🔍 RoomDAO.getAllRooms() called with adminId: " + adminId);
        
        // First, debug the database connection
        debugDatabaseConnection(adminId);
        
        // FIXED: Handle the data inconsistency - get rooms for admin_id and try to match sharing types
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
                COALESCE(st.type_name, 'Unknown Type') as sharing_type_name, 
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
            return result;
        } catch (Exception e) {
            System.out.println("❌ SQL query failed: " + e.getMessage());
            e.printStackTrace();
            return List.of();
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
            
            // Show actual room data
            String roomDataSql = "SELECT room_id, room_number, floor_number, room_status, current_occupancy, sharing_type_id, admin_id FROM Rooms WHERE admin_id = ?";
            jdbcTemplate.query(roomDataSql, new Object[]{adminId}, (rs, rowNum) -> {
                System.out.println("🐛 DEBUG Room Data - ID: " + rs.getInt("room_id") + 
                                 ", Number: " + rs.getString("room_number") +
                                 ", Floor: " + rs.getInt("floor_number") +
                                 ", Status: " + rs.getString("room_status") +
                                 ", Occupancy: " + rs.getInt("current_occupancy") +
                                 ", SharingTypeId: " + rs.getInt("sharing_type_id") +
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
        
        // 3. Check ALL SharingType data to see what's available
        String allSharingSql = "SELECT sharing_type_id, type_name, capacity, sharing_fee, admin_id FROM SharingType";
        try {
            jdbcTemplate.query(allSharingSql, (rs, rowNum) -> {
                System.out.println("🐛 DEBUG ALL SharingType - ID: " + rs.getInt("sharing_type_id") + 
                                 ", Name: " + rs.getString("type_name") +
                                 ", Capacity: " + rs.getInt("capacity") +
                                 ", Fee: " + rs.getDouble("sharing_fee") +
                                 ", AdminId: " + rs.getInt("admin_id"));
                return null;
            });
        } catch (Exception e) {
            System.out.println("❌ DEBUG: All SharingType query failed: " + e.getMessage());
        }
    }

    public List<RoomDTO> getAvailableRooms(Integer adminId) {
        System.out.println("🔍 RoomDAO.getAvailableRooms() called with adminId: " + adminId);
        
        // FIXED: Use LEFT JOIN with COALESCE for fallback values
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
                COALESCE(st.type_name, 'Unknown Type') as sharing_type_name, 
                COALESCE(st.capacity, 2) as sharing_capacity, 
                COALESCE(st.sharing_fee, 5000.00) as price,
                CONCAT(r.current_occupancy, '/', COALESCE(st.capacity, 2)) as occupancy_status
            FROM Rooms r
            LEFT JOIN SharingType st ON r.sharing_type_id = st.sharing_type_id AND r.admin_id = st.admin_id
            WHERE r.admin_id = ? AND r.room_status = 'Available'
            ORDER BY r.floor_number, r.room_number
            """;
        
        System.out.println("📝 Executing available rooms SQL query for adminId: " + adminId);
        
        List<RoomDTO> result = jdbcTemplate.query(sql, new Object[]{adminId}, this::mapRoomDTO);
        
        System.out.println("✅ Available rooms SQL query completed, found " + result.size() + " rooms");
        
        return result;
    }

    public List<RoomDTO> getRoomsByFloor(Integer adminId, Integer floorNumber) {
        System.out.println("🔍 RoomDAO.getRoomsByFloor() called with adminId: " + adminId + ", floor: " + floorNumber);
        
        // FIXED: Use LEFT JOIN with COALESCE for fallback values
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
                COALESCE(st.type_name, 'Unknown Type') as sharing_type_name, 
                COALESCE(st.capacity, 2) as sharing_capacity, 
                COALESCE(st.sharing_fee, 5000.00) as price,
                CONCAT(r.current_occupancy, '/', COALESCE(st.capacity, 2)) as occupancy_status
            FROM Rooms r
            LEFT JOIN SharingType st ON r.sharing_type_id = st.sharing_type_id AND r.admin_id = st.admin_id
            WHERE r.admin_id = ? AND r.floor_number = ?
            ORDER BY r.room_number
            """;
        
        System.out.println("📝 Executing floor rooms SQL query for adminId: " + adminId + ", floor: " + floorNumber);
        
        List<RoomDTO> result = jdbcTemplate.query(sql, new Object[]{adminId, floorNumber}, this::mapRoomDTO);
        
        System.out.println("✅ Floor rooms SQL query completed, found " + result.size() + " rooms");
        
        return result;
    }

    public RoomDTO getRoomById(Integer roomId, Integer adminId) {
        System.out.println("🔍 RoomDAO.getRoomById() called with roomId: " + roomId + ", adminId: " + adminId);
        
        // FIXED: Use LEFT JOIN with COALESCE for fallback values
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
                COALESCE(st.type_name, 'Unknown Type') as sharing_type_name, 
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
        }
    }

    private RoomDTO mapRoomDTO(ResultSet rs, int rowNum) throws SQLException {
        System.out.println("🗺️ Mapping ResultSet to RoomDTO for row: " + rowNum);
        
        RoomDTO room = new RoomDTO();
        room.setRoomId(rs.getInt("room_id"));
        room.setAdminId(rs.getInt("admin_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setFloorNumber(rs.getInt("floor_number"));
        room.setRoomStatus(rs.getString("room_status"));
        room.setCurrentOccupancy(rs.getInt("current_occupancy"));
        room.setCreatedAt(rs.getString("created_at"));
        room.setSharingTypeId(rs.getInt("sharing_type_id"));
        room.setSharingTypeName(rs.getString("sharing_type_name"));
        room.setSharingCapacity(rs.getInt("sharing_capacity"));
        room.setPrice(rs.getDouble("price"));
        room.setOccupancyStatus(rs.getString("occupancy_status"));
        
        System.out.println("✅ Mapped RoomDTO: " + room.getRoomNumber() + " - " + room.getSharingTypeName() + " - ₹" + room.getPrice());
        
        return room;
    }
}