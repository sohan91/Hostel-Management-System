package com.example.HostelManagement.repositories;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.HostelManagement.dto.HostlerDto;
import com.example.HostelManagement.dto.RoomWithHostlersDto;
import com.example.HostelManagement.service.RoomWithHostlersRowMapper;

@Repository
public class RoomRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public List<Map<String, Object>> fetchRoomWithHostlers(Integer roomId, Integer floorNumber, 
                                                         Integer sharingTypeId, Integer adminId) {
        String sql = """
            SELECT 
                r.room_id,
                r.room_number,
                r.floor_number,
                r.room_status,
                r.current_occupancy,
                st.sharing_type_id,
                st.type_name,
                st.capacity as sharing_capacity,
                (st.capacity - r.current_occupancy) as available_beds,
                s.student_id,
                s.student_name,
                s.student_email,
                s.student_phone,
                s.date_of_birth,
                s.parent_name,
                s.parent_phone,
                s.join_date,
                s.payment_status,
                s.is_active,
                s.blood_group
            FROM Rooms r
            INNER JOIN sharing_type st ON r.sharing_type_id = st.sharing_type_id
            LEFT JOIN Student s ON r.room_id = s.room_id AND s.is_active = TRUE
            WHERE r.room_id = ? 
                AND r.floor_number = ?
                AND r.sharing_type_id = ?
                AND r.admin_id = ?
            ORDER BY s.student_name
            """;
            
        return jdbcTemplate.queryForList(sql, roomId, floorNumber, sharingTypeId, adminId);
    }
    
    // Alternative method using RowMapper for better type safety
    public RoomWithHostlersDto fetchRoomWithHostlersDynamic(Integer roomId, Integer adminId) {
        String sql = """
            SELECT 
                r.room_id,
                r.room_number,
                r.floor_number,
                r.room_status,
                r.current_occupancy,
                r.sharing_type_id,
                st.type_name,
                st.capacity as sharing_capacity,
                (st.capacity - r.current_occupancy) as available_beds,
                -- Student fields
                s.student_id,
                s.student_name,
                s.student_email,
                s.student_phone,
                s.date_of_birth,
                s.parent_name,
                s.parent_phone,
                s.join_date,
                s.payment_status,
                s.is_active,s.blood_group
            FROM Rooms r
            INNER JOIN sharing_type st ON r.sharing_type_id = st.sharing_type_id
            LEFT JOIN Student s ON r.room_id = s.room_id AND s.is_active = TRUE
            WHERE r.room_id = ? AND r.admin_id = ?
            ORDER BY s.student_name
            """;
            
        List<RoomWithHostlersDto> results = jdbcTemplate.query(sql, new Object[]{roomId, adminId}, new RoomWithHostlersRowMapper());
        
        if (results.isEmpty()) {
            return null;
        }
        
        // Since we're getting multiple rows (one per student), we need to consolidate
        RoomWithHostlersDto consolidated = results.get(0);
        if (results.size() > 1) {
            List<HostlerDto> allHostlers = results.stream()
                .filter(r -> r.getHostlers() != null && !r.getHostlers().isEmpty())
                .flatMap((var r) -> r.getHostlers().stream())
                .collect(Collectors.toList());
            consolidated.setHostlers(allHostlers);
        }
        
        return consolidated;
    }
}