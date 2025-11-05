package com.example.HostelManagement.dto;

public class RoomDTO {
    private Integer roomId;
    private Integer adminId;
    private Integer sharingTypeId;
    private String roomNumber;
    private Integer floorNumber;
    private String roomStatus;
    private Integer currentOccupancy;
    private String createdAt;
    // Additional fields for display
    private String sharingTypeName;
    private Integer sharingCapacity;
    private Double price;
    private String occupancyStatus; // e.g., "1/2"

    // Constructors
    public RoomDTO() {}

    public RoomDTO(Integer roomId, String roomNumber, Integer floorNumber, 
                  String roomStatus, Integer currentOccupancy, 
                  String sharingTypeName, Integer sharingCapacity, Double price) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.floorNumber = floorNumber;
        this.roomStatus = roomStatus;
        this.currentOccupancy = currentOccupancy;
        this.sharingTypeName = sharingTypeName;
        this.sharingCapacity = sharingCapacity;
        this.price = price;
        this.occupancyStatus = currentOccupancy + "/" + sharingCapacity;
    }

    // Getters and Setters
    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public Integer getSharingTypeId() { return sharingTypeId; }
    public void setSharingTypeId(Integer sharingTypeId) { this.sharingTypeId = sharingTypeId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public String getRoomStatus() { return roomStatus; }
    public void setRoomStatus(String roomStatus) { this.roomStatus = roomStatus; }

    public Integer getCurrentOccupancy() { return currentOccupancy; }
    public void setCurrentOccupancy(Integer currentOccupancy) { 
        this.currentOccupancy = currentOccupancy; 
        if (sharingCapacity != null) {
            this.occupancyStatus = currentOccupancy + "/" + sharingCapacity;
        }
    }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getSharingTypeName() { return sharingTypeName; }
    public void setSharingTypeName(String sharingTypeName) { this.sharingTypeName = sharingTypeName; }

    public Integer getSharingCapacity() { return sharingCapacity; }
    public void setSharingCapacity(Integer sharingCapacity) { 
        this.sharingCapacity = sharingCapacity; 
        if (currentOccupancy != null) {
            this.occupancyStatus = currentOccupancy + "/" + sharingCapacity;
        }
    }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getOccupancyStatus() { return occupancyStatus; }
    public void setOccupancyStatus(String occupancyStatus) { this.occupancyStatus = occupancyStatus; }

    // ADD THIS toString() METHOD TO FIX THE LOGGING ISSUE
    @Override
    public String toString() {
        return "RoomDTO{" +
                "roomId=" + roomId +
                ", adminId=" + adminId +
                ", sharingTypeId=" + sharingTypeId +
                ", roomNumber='" + roomNumber + '\'' +
                ", floorNumber=" + floorNumber +
                ", roomStatus='" + roomStatus + '\'' +
                ", currentOccupancy=" + currentOccupancy +
                ", createdAt='" + createdAt + '\'' +
                ", sharingTypeName='" + sharingTypeName + '\'' +
                ", sharingCapacity=" + sharingCapacity +
                ", price=" + price +
                ", occupancyStatus='" + occupancyStatus + '\'' +
                '}';
    }
}