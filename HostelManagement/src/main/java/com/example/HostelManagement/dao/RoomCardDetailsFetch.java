package com.example.HostelManagement.dao;

import lombok.Data;

@Data
public class RoomCardDetailsFetch {
    private Integer roomId;
    private Integer sharingTypeId;
    private String roomNumber;  // Changed from Integer to String
    private Integer floorNumber;
    private String sharingType; // Added this missing field
    private Integer adminId;    // Added this field for security
    
    public RoomCardDetailsFetch() {}
    
    public RoomCardDetailsFetch(Integer roomId, Integer sharingTypeId, String roomNumber, Integer floorNumber) {
        this.roomId = roomId;
        this.sharingTypeId = sharingTypeId;
        this.roomNumber = roomNumber;
        this.floorNumber = floorNumber;
    }
    
    // Additional constructor for convenience
    public RoomCardDetailsFetch(Integer roomId, Integer sharingTypeId, String roomNumber, 
                               Integer floorNumber, String sharingType, Integer adminId) {
        this.roomId = roomId;
        this.sharingTypeId = sharingTypeId;
        this.roomNumber = roomNumber;
        this.floorNumber = floorNumber;
        this.sharingType = sharingType;
        this.adminId = adminId;
    }
    
    // Constructor with just roomId (for minimal usage)
    public RoomCardDetailsFetch(Integer roomId) {
        this.roomId = roomId;
    }
}