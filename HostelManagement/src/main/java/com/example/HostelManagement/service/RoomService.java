package com.example.HostelManagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.HostelManagement.dao.RoomDAO;
import com.example.HostelManagement.dto.RoomDTO;
import com.example.HostelManagement.repositories.AddNewSharingTypeRepo;

@Service
public class RoomService {

    @Autowired
    private RoomDAO roomDAO;

    @Autowired
    private AddNewSharingTypeRepo sharingTypeRepo;

    public boolean createRoom(RoomDTO roomDTO) {
        System.out.println("🚀 RoomService.createRoom() called for room: " + roomDTO.getRoomNumber() + 
                         ", Admin: " + roomDTO.getAdminId());

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

            if (roomDTO.getAdminId() == null) {
                System.out.println("❌ Admin ID is required");
                return false;
            }

            // NEW: Validate that sharing type belongs to the same admin
            if (!sharingTypeRepo.isSharingTypeBelongsToAdmin(roomDTO.getSharingTypeId(), roomDTO.getAdminId())) {
                System.out.println("❌ Sharing type " + roomDTO.getSharingTypeId() + " does not belong to admin " + roomDTO.getAdminId());
                return false;
            }

            // Save the room
            boolean success = roomDAO.saveRoom(roomDTO);
            System.out.println("✅ Room creation result for Admin " + roomDTO.getAdminId() + ": " + (success ? "SUCCESS" : "FAILED"));

            return success;

        } catch (Exception e) {
            System.out.println("❌ Error in RoomService.createRoom() for Admin " + roomDTO.getAdminId() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Updated: Now uses the correct constraint checking
    public boolean isRoomNumberExists(Integer adminId, String roomNumber, Integer floorNumber, Integer sharingTypeId) {
        System.out.println("🔍 RoomService.isRoomNumberExists() called for Admin " + adminId + 
                         ": " + roomNumber + ", floor: " + floorNumber);

        try {
            // The DAO method now properly checks (admin_id, room_number, floor_number) constraint
            boolean exists = roomDAO.isRoomNumberExists(adminId, roomNumber, floorNumber, sharingTypeId);
            System.out.println("🔍 Room uniqueness check for Admin " + adminId + ": " + (exists ? "EXISTS" : "UNIQUE"));
            return exists;

        } catch (Exception e) {
            System.out.println("❌ Error checking room uniqueness for Admin " + adminId + ": " + e.getMessage());
            return false;
        }
    }

    // NEW: Method to check if room number exists on specific floor
    public boolean isRoomNumberExistsOnFloor(Integer adminId, String roomNumber, Integer floorNumber) {
        System.out.println("🔍 RoomService.isRoomNumberExistsOnFloor() called for Admin " + adminId + 
                         ": " + roomNumber + ", floor: " + floorNumber);

        try {
            boolean exists = roomDAO.isRoomNumberExistsOnFloor(adminId, roomNumber, floorNumber);
            System.out.println("🔍 Room on floor check for Admin " + adminId + ": " + (exists ? "EXISTS" : "UNIQUE"));
            return exists;

        } catch (Exception e) {
            System.out.println("❌ Error checking room on floor for Admin " + adminId + ": " + e.getMessage());
            return false;
        }
    }

    // NEW: Method to check if room number exists anywhere for admin
    public boolean isRoomNumberExistsAnywhere(Integer adminId, String roomNumber) {
        System.out.println("🔍 RoomService.isRoomNumberExistsAnywhere() called for Admin " + adminId + ": " + roomNumber);

        try {
            boolean exists = roomDAO.isRoomNumberExistsAnywhere(adminId, roomNumber);
            System.out.println("🔍 Room exists anywhere for Admin " + adminId + ": " + (exists ? "EXISTS" : "UNIQUE"));
            return exists;

        } catch (Exception e) {
            System.out.println("❌ Error checking room existence for Admin " + adminId + ": " + e.getMessage());
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
            System.out.println("✅ Rooms found for Admin " + adminId + ": " + rooms.size());
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
            System.out.println("✅ Room found for Admin " + adminId + ": " + room.getRoomNumber());
        }
        
        return room;
    }

    public List<RoomDTO> getRoomsSummary(Integer adminId) {
        System.out.println("🚀 RoomService.getRoomsSummary() called with adminId: " + adminId);
        
        List<RoomDTO> rooms = roomDAO.getAllRooms(adminId);
        
        System.out.println("📋 Room Summary for Admin " + adminId + ":");
        rooms.forEach(room -> {
            System.out.println("📋 - " + room.getRoomNumber() + " (Floor " + room.getFloorNumber() + 
                             "): " + room.getOccupancyStatus() + " - " + room.getRoomStatus());
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
        
        System.out.println("📊 Room " + room.getRoomNumber() + " (Admin " + adminId + ") can accommodate student: " + canAccommodate +
                         " - Occupancy: " + room.getCurrentOccupancy() + "/" + room.getSharingCapacity());
        
        return canAccommodate;
    }

    public List<RoomDTO> getRoomsByStatus(Integer adminId, String status) {
        System.out.println("🚀 RoomService.getRoomsByStatus() called with adminId: " + adminId + ", status: " + status);
        
        List<RoomDTO> allRooms = roomDAO.getAllRooms(adminId);
        List<RoomDTO> filteredRooms = allRooms.stream()
                .filter(room -> status.equalsIgnoreCase(room.getRoomStatus()))
                .collect(Collectors.toList());
        
        System.out.println("📊 Found " + filteredRooms.size() + " rooms with status '" + status + "' for admin " + adminId);
        
        return filteredRooms;
    }

    public List<RoomDTO> getRoomsBySharingType(Integer adminId, String sharingTypeName) {
        System.out.println("🚀 RoomService.getRoomsBySharingType() called with adminId: " + adminId + ", sharingType: " + sharingTypeName);
        
        List<RoomDTO> allRooms = roomDAO.getAllRooms(adminId);
        List<RoomDTO> filteredRooms = allRooms.stream()
                .filter(room -> sharingTypeName.equalsIgnoreCase(room.getSharingTypeName()))
                .collect(Collectors.toList());
        
        System.out.println("📊 Found " + filteredRooms.size() + " rooms with sharing type '" + sharingTypeName + "' for admin " + adminId);
        
        return filteredRooms;
    }

    public long getTotalRoomCount(Integer adminId) {
        System.out.println("🚀 RoomService.getTotalRoomCount() called with adminId: " + adminId);
        
        long count = roomDAO.getAllRooms(adminId).size();
        
        System.out.println("📊 Total room count for admin " + adminId + ": " + count);
        
        return count;
    }

    public long getAvailableRoomCount(Integer adminId) {
        System.out.println("🚀 RoomService.getAvailableRoomCount() called with adminId: " + adminId);
        
        long count = roomDAO.getAvailableRooms(adminId).size();
        
        System.out.println("📊 Available room count for admin " + adminId + ": " + count);
        
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
        
        System.out.println("📊 Room Statistics for Admin " + adminId + 
                          " - Total: " + totalRooms + 
                          ", Available: " + availableRooms + 
                          ", Occupied: " + occupiedRooms + 
                          ", Maintenance: " + maintenanceRooms +
                          ", Occupancy Rate: " + String.format("%.2f", occupancyRate) + "%");
        
        return new RoomStatistics(totalRooms, availableRooms, occupiedRooms, 
                                maintenanceRooms, occupancyRate);
    }

    // NEW: Comprehensive room creation validation
    public RoomCreationResult validateRoomCreation(RoomDTO roomDTO) {
        System.out.println("🔍 RoomService.validateRoomCreation() called for room: " + roomDTO.getRoomNumber() + ", Admin: " + roomDTO.getAdminId());

        RoomCreationResult result = new RoomCreationResult();
        
        // Check required fields
        if (roomDTO.getRoomNumber() == null || roomDTO.getRoomNumber().trim().isEmpty()) {
            result.setValid(false);
            result.setMessage("Room number is required");
            return result;
        }

        if (roomDTO.getFloorNumber() == null) {
            result.setValid(false);
            result.setMessage("Floor number is required");
            return result;
        }

        if (roomDTO.getSharingTypeId() == null) {
            result.setValid(false);
            result.setMessage("Sharing type is required");
            return result;
        }

        // Check room number uniqueness on this floor
        if (isRoomNumberExistsOnFloor(roomDTO.getAdminId(), roomDTO.getRoomNumber(), roomDTO.getFloorNumber())) {
            result.setValid(false);
            result.setMessage("Room number '" + roomDTO.getRoomNumber() + "' already exists on floor " + roomDTO.getFloorNumber());
            return result;
        }

        // Check sharing type ownership
        if (!sharingTypeRepo.isSharingTypeBelongsToAdmin(roomDTO.getSharingTypeId(), roomDTO.getAdminId())) {
            result.setValid(false);
            result.setMessage("Selected sharing type is not available for this admin");
            return result;
        }

        result.setValid(true);
        result.setMessage("Room creation validation passed");
        return result;
    }

    public static class RoomCreationResult {
        private boolean valid;
        private String message;

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
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