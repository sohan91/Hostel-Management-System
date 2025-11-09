package com.example.HostelManagement.service;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.example.HostelManagement.dto.HostlerDto;
import com.example.HostelManagement.dto.RoomWithHostlersDto;

public class RoomWithHostlersRowMapper implements RowMapper<RoomWithHostlersDto> {
    
    @Override
    public RoomWithHostlersDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        RoomWithHostlersDto dto = new RoomWithHostlersDto();
        
        // Room info (same for all rows)
        dto.setRoomId(rs.getInt("room_id"));
        dto.setRoomNumber(rs.getString("room_number"));
        dto.setFloorNumber(rs.getInt("floor_number"));
        dto.setRoomStatus(rs.getString("room_status"));
        dto.setCurrentOccupancy(rs.getInt("current_occupancy"));
        dto.setSharingTypeId(rs.getInt("sharing_type_id"));
        dto.setSharingTypeName(rs.getString("sharing_type_name"));
        dto.setSharingCapacity(rs.getInt("sharing_capacity"));
        dto.setAvailableBeds(rs.getInt("available_beds"));
        
        // Student/hostler info (if exists)
        Integer studentId = rs.getInt("student_id");
        if (!rs.wasNull()) { // Check if student exists
            HostlerDto hostler = new HostlerDto();
            hostler.setStudentId(studentId);
            hostler.setStudentName(rs.getString("student_name"));
            hostler.setStudentEmail(rs.getString("student_email"));
            hostler.setStudentPhone(rs.getString("student_phone"));
            hostler.setDateOfBirth(rs.getDate("date_of_birth") != null ? 
                rs.getDate("date_of_birth").toString() : null);
            hostler.setParentName(rs.getString("parent_name"));
            hostler.setParentPhone(rs.getString("parent_phone"));
            hostler.setJoinDate(rs.getTimestamp("join_date") != null ? 
                rs.getTimestamp("join_date").toLocalDateTime() : null);
            hostler.setPaymentStatus(rs.getString("payment_status"));
            hostler.setIsActive(rs.getBoolean("is_active"));
            
            dto.setHostlers(List.of(hostler));
        } else {
            dto.setHostlers(List.of());
        }
        
        return dto;
    }
}