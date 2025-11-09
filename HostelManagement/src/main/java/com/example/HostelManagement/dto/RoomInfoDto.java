package com.example.HostelManagement.dto;

import java.util.Map;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomInfoDto {
    private Integer roomId;
    private String roomNumber;
    private Integer floorNumber;
    private String roomStatus;
    private Integer currentOccupancy;
    private Integer sharingCapacity;
    private Integer availableBeds;
    private String sharingTypeName;
    private Integer sharingTypeId;
    
    // Utility method to create from database result set
    public static RoomInfoDto fromMap(Map<String, Object> row) {
        if (row == null || row.get("room_id") == null) {
            return null;
        }
        
        return RoomInfoDto.builder()
            .roomId(getInteger(row, "room_id"))
            .roomNumber(getString(row, "room_number"))
            .floorNumber(getInteger(row, "floor_number"))
            .roomStatus(getString(row, "room_status"))
            .currentOccupancy(getInteger(row, "current_occupancy"))
            .sharingCapacity(getInteger(row, "sharing_capacity"))
            .availableBeds(getInteger(row, "available_beds"))
            .sharingTypeName(getString(row, "sharing_type_name"))
            .sharingTypeId(getInteger(row, "sharing_type_id"))
            .build();
    }
    
    private static Integer getInteger(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
    
    private static String getString(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString() : null;
    }
    
    public String getOccupancyText() {
        return currentOccupancy + "/" + sharingCapacity;
    }
    
    public String getAvailabilityText() {
        return availableBeds + " spots available";
    }
    
    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(roomStatus) && availableBeds > 0;
    }
    
    public boolean isFull() {
        return currentOccupancy != null && sharingCapacity != null && 
               currentOccupancy >= sharingCapacity;
    }
}