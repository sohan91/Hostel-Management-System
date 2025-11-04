package com.example.HostelManagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.HostelManagement.dao.RoomDAO;
import com.example.HostelManagement.dto.RoomDTO;

@Service
public class RoomService {

    @Autowired
    private RoomDAO roomDAO;

    public boolean createRoom(RoomDTO roomDTO) {
        System.out.println("🚀 RoomService.createRoom() called for room: " + roomDTO.getRoomNumber());

        try {
            // Validate required fields
            if (roomDTO.getRoomNumber() == null || roomDTO.getRoomNumber().trim().isEmpty()) {
                System.out.println("❌ Room number is required");
                return false;
            }

            if (roomDTO.getFloorNumber() == null) {
                System.out.println("❌ Floor number is required");
                return false;
            }

            if (roomDTO.getSharingTypeId() == null) {
                System.out.println("❌ Sharing type ID is required");
                return false;
            }

            // Save the room
            boolean success = roomDAO.saveRoom(roomDTO);
            System.out.println("✅ Room creation result: " + (success ? "SUCCESS" : "FAILED"));

            return success;

        } catch (Exception e) {
            System.out.println("❌ Error in RoomService.createRoom(): " + e.getMessage());
            return false;
        }
    }

    public boolean isRoomNumberExists(Integer adminId, String roomNumber, Integer floorNumber, Integer sharingTypeId) {
        System.out.println("🔍 RoomService.isRoomNumberExists() called for room: " + roomNumber + ", floor: " + floorNumber);

        try {
            boolean exists = roomDAO.isRoomNumberExists(adminId, roomNumber, floorNumber, sharingTypeId);
            System.out.println("🔍 Room uniqueness check result: " + (exists ? "EXISTS" : "UNIQUE"));
            return exists;

        } catch (Exception e) {
            System.out.println("❌ Error checking room uniqueness: " + e.getMessage());
            return false;
        }
    }

    public List<RoomDTO> getAllRooms(Integer adminId) {
        System.out.println("🚀 RoomService.getAllRooms() called with adminId: " + adminId);
        
        List<RoomDTO> rooms = roomDAO.getAllRooms(adminId);
        
        System.out.println("📊 RoomDAO returned " + rooms.size() + " rooms for adminId: " + adminId);
        
        if (rooms.isEmpty()) {
            System.out.println("❌ NO ROOMS FOUND in database for adminId: " + adminId);
        } else {
            System.out.println("✅ Rooms found: " + rooms.size());
        }
        
        return rooms;
    }

    public List<RoomDTO> getAvailableRooms(Integer adminId) {
        System.out.println("🚀 RoomService.getAvailableRooms() called with adminId: " + adminId);
        
        List<RoomDTO> rooms = roomDAO.getAvailableRooms(adminId);
        
        System.out.println("📊 RoomDAO returned " + rooms.size() + " available rooms for adminId: " + adminId);
        
        return rooms;
    }

    public List<RoomDTO> getRoomsByFloor(Integer adminId, Integer floorNumber) {
        System.out.println("🚀 RoomService.getRoomsByFloor() called with adminId: " + adminId + ", floor: " + floorNumber);
        
        List<RoomDTO> rooms = roomDAO.getRoomsByFloor(adminId, floorNumber);
        
        System.out.println("📊 RoomDAO returned " + rooms.size() + " rooms for floor " + floorNumber + ", adminId: " + adminId);
        
        return rooms;
    }

    public RoomDTO getRoomById(Integer roomId, Integer adminId) {
        System.out.println("🚀 RoomService.getRoomById() called with roomId: " + roomId + ", adminId: " + adminId);
        
        RoomDTO room = roomDAO.getRoomById(roomId, adminId);
        
        if (room == null) {
            System.out.println("❌ Room not found with roomId: " + roomId + ", adminId: " + adminId);
        } else {
            System.out.println("✅ Room found: " + room.getRoomNumber());
        }
        
        return room;
    }

    public List<RoomDTO> getRoomsSummary(Integer adminId) {
        System.out.println("🚀 RoomService.getRoomsSummary() called with adminId: " + adminId);
        
        List<RoomDTO> rooms = roomDAO.getAllRooms(adminId);
        
        rooms.forEach(room -> {
            System.out.println("📋 Room Summary - " + room.getRoomNumber() + ": " + room.getOccupancyStatus());
        });
        
        return rooms;
    }

    public boolean canAccommodateStudent(Integer roomId, Integer adminId) {
        System.out.println("🚀 RoomService.canAccommodateStudent() called with roomId: " + roomId + ", adminId: " + adminId);
        
        RoomDTO room = roomDAO.getRoomById(roomId, adminId);
        if (room == null) {
            System.out.println("❌ Room not found, cannot accommodate student");
            return false;
        }
        
        boolean canAccommodate = "Available".equals(room.getRoomStatus()) && 
               room.getCurrentOccupancy() < room.getSharingCapacity();
        
        System.out.println("📊 Room " + room.getRoomNumber() + " can accommodate student: " + canAccommodate);
        
        return canAccommodate;
    }

    public List<RoomDTO> getRoomsByStatus(Integer adminId, String status) {
        System.out.println("🚀 RoomService.getRoomsByStatus() called with adminId: " + adminId + ", status: " + status);
        
        List<RoomDTO> allRooms = roomDAO.getAllRooms(adminId);
        List<RoomDTO> filteredRooms = allRooms.stream()
                .filter(room -> status.equalsIgnoreCase(room.getRoomStatus()))
                .collect(Collectors.toList());
        
        System.out.println("📊 Found " + filteredRooms.size() + " rooms with status: " + status);
        
        return filteredRooms;
    }

    public List<RoomDTO> getRoomsBySharingType(Integer adminId, String sharingTypeName) {
        System.out.println("🚀 RoomService.getRoomsBySharingType() called with adminId: " + adminId + ", sharingType: " + sharingTypeName);
        
        List<RoomDTO> allRooms = roomDAO.getAllRooms(adminId);
        List<RoomDTO> filteredRooms = allRooms.stream()
                .filter(room -> sharingTypeName.equalsIgnoreCase(room.getSharingTypeName()))
                .collect(Collectors.toList());
        
        System.out.println("📊 Found " + filteredRooms.size() + " rooms with sharing type: " + sharingTypeName);
        
        return filteredRooms;
    }

    public long getTotalRoomCount(Integer adminId) {
        System.out.println("🚀 RoomService.getTotalRoomCount() called with adminId: " + adminId);
        
        long count = roomDAO.getAllRooms(adminId).size();
        
        System.out.println("📊 Total room count: " + count);
        
        return count;
    }

    public long getAvailableRoomCount(Integer adminId) {
        System.out.println("🚀 RoomService.getAvailableRoomCount() called with adminId: " + adminId);
        
        long count = roomDAO.getAvailableRooms(adminId).size();
        
        System.out.println("📊 Available room count: " + count);
        
        return count;
    }

    public RoomStatistics getRoomStatistics(Integer adminId) {
        System.out.println("🚀 RoomService.getRoomStatistics() called with adminId: " + adminId);
        
        List<RoomDTO> allRooms = roomDAO.getAllRooms(adminId);
        
        long totalRooms = allRooms.size();
        long availableRooms = allRooms.stream()
                .filter(room -> "Available".equals(room.getRoomStatus()))
                .count();
        long occupiedRooms = allRooms.stream()
                .filter(room -> "Occupied".equals(room.getRoomStatus()))
                .count();
        long maintenanceRooms = allRooms.stream()
                .filter(room -> "Maintenance".equals(room.getRoomStatus()))
                .count();
        
        double occupancyRate = totalRooms > 0 ? 
                (double) occupiedRooms / totalRooms * 100 : 0;
        
        System.out.println("📊 Room Statistics - Total: " + totalRooms + 
                          ", Available: " + availableRooms + 
                          ", Occupied: " + occupiedRooms + 
                          ", Maintenance: " + maintenanceRooms +
                          ", Occupancy Rate: " + occupancyRate + "%");
        
        return new RoomStatistics(totalRooms, availableRooms, occupiedRooms, 
                                maintenanceRooms, occupancyRate);
    }

    public static class RoomStatistics {
        private final long totalRooms;
        private final long availableRooms;
        private final long occupiedRooms;
        private final long maintenanceRooms;
        private final double occupancyRate;

        public RoomStatistics(long totalRooms, long availableRooms, long occupiedRooms, 
                            long maintenanceRooms, double occupancyRate) {
            this.totalRooms = totalRooms;
            this.availableRooms = availableRooms;
            this.occupiedRooms = occupiedRooms;
            this.maintenanceRooms = maintenanceRooms;
            this.occupancyRate = occupancyRate;
        }

        public long getTotalRooms() { return totalRooms; }
        public long getAvailableRooms() { return availableRooms; }
        public long getOccupiedRooms() { return occupiedRooms; }
        public long getMaintenanceRooms() { return maintenanceRooms; }
        public double getOccupancyRate() { return occupancyRate; }
    }
}