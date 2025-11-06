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

            // UPDATED: Room number uniqueness is now handled by DAO (per floor, regardless of sharing type)
            // The DAO will check if room number already exists on this floor for any sharing type

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

    // UPDATED: Simplified - sharingTypeId parameter is no longer needed for uniqueness check
    public boolean isRoomNumberExists(Integer adminId, String roomNumber, Integer floorNumber) {
        System.out.println("🔍 RoomService.isRoomNumberExists() called for Admin " + adminId + 
                         ": " + roomNumber + ", floor: " + floorNumber);

        try {
            // The DAO method now properly checks (admin_id, room_number, floor_number) constraint
            // Room numbers must be unique per floor regardless of sharing type
            boolean exists = roomDAO.isRoomNumberExistsOnFloor(adminId, roomNumber, floorNumber);
            System.out.println("🔍 Room uniqueness check for Admin " + adminId + ": " + (exists ? "EXISTS" : "UNIQUE"));
            return exists;

        } catch (Exception e) {
            System.out.println("❌ Error checking room uniqueness for Admin " + adminId + ": " + e.getMessage());
            return false;
        }
    }

    // UPDATED: Enhanced with better logging
    public boolean isRoomNumberExistsOnFloor(Integer adminId, String roomNumber, Integer floorNumber) {
        System.out.println("🔍 RoomService.isRoomNumberExistsOnFloor() called for Admin " + adminId + 
                         ": " + roomNumber + ", floor: " + floorNumber);

        try {
            boolean exists = roomDAO.isRoomNumberExistsOnFloor(adminId, roomNumber, floorNumber);
            System.out.println("🔍 Room on floor check for Admin " + adminId + ": " + (exists ? "EXISTS" : "UNIQUE"));
            
            if (exists) {
                System.out.println("⚠️  Room number '" + roomNumber + "' already exists on floor " + floorNumber + 
                                 " for admin " + adminId + " (regardless of sharing type)");
            }
            
            return exists;

        } catch (Exception e) {
            System.out.println("❌ Error checking room on floor for Admin " + adminId + ": " + e.getMessage());
            return false;
        }
    }

    // UPDATED: Enhanced with better logging
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

    // NEW: Get used room numbers on a specific floor
    public List<String> getUsedRoomNumbersOnFloor(Integer adminId, Integer floorNumber) {
        System.out.println("🔍 RoomService.getUsedRoomNumbersOnFloor() called for Admin " + adminId + ", floor: " + floorNumber);

        try {
            List<String> usedNumbers = roomDAO.getUsedRoomNumbersOnFloor(adminId, floorNumber);
            System.out.println("📊 Used room numbers on floor " + floorNumber + " for admin " + adminId + ": " + usedNumbers);
            return usedNumbers;
        } catch (Exception e) {
            System.out.println("❌ Error fetching used room numbers for Admin " + adminId + ": " + e.getMessage());
            return List.of();
        }
    }

    // NEW: Suggest available room numbers for a floor
    public List<String> suggestAvailableRoomNumbers(Integer adminId, Integer floorNumber, int count) {
        System.out.println("💡 RoomService.suggestAvailableRoomNumbers() called for Admin " + adminId + 
                         ", floor: " + floorNumber + ", count: " + count);

        try {
            List<String> suggestions = roomDAO.suggestAvailableRoomNumbers(adminId, floorNumber, count);
            System.out.println("💡 Suggested room numbers for floor " + floorNumber + ": " + suggestions);
            return suggestions;
        } catch (Exception e) {
            System.out.println("❌ Error suggesting room numbers for Admin " + adminId + ": " + e.getMessage());
            return List.of();
        }
    }

    // NEW: Comprehensive room validation before creation
    public RoomValidationResult validateRoomForCreation(RoomDTO roomDTO) {
        System.out.println("🔍 RoomService.validateRoomForCreation() called for room: " + 
                         roomDTO.getRoomNumber() + ", floor: " + roomDTO.getFloorNumber() + 
                         ", admin: " + roomDTO.getAdminId());

        RoomValidationResult result = new RoomValidationResult();
        
        // Check required fields
        if (roomDTO.getRoomNumber() == null || roomDTO.getRoomNumber().trim().isEmpty()) {
            result.setValid(false);
            result.setMessage("Room number is required");
            result.setErrorType("MISSING_FIELD");
            return result;
        }

        if (roomDTO.getFloorNumber() == null) {
            result.setValid(false);
            result.setMessage("Floor number is required");
            result.setErrorType("MISSING_FIELD");
            return result;
        }

        if (roomDTO.getSharingTypeId() == null) {
            result.setValid(false);
            result.setMessage("Sharing type is required");
            result.setErrorType("MISSING_FIELD");
            return result;
        }

        if (roomDTO.getAdminId() == null) {
            result.setValid(false);
            result.setMessage("Admin ID is required");
            result.setErrorType("MISSING_FIELD");
            return result;
        }

        // Check room number format (basic validation)
        if (!isValidRoomNumber(roomDTO.getRoomNumber())) {
            result.setValid(false);
            result.setMessage("Room number format is invalid. Use numbers only (e.g., 101, 102)");
            result.setErrorType("INVALID_FORMAT");
            return result;
        }

        // Check floor number range
        if (roomDTO.getFloorNumber() < 1 || roomDTO.getFloorNumber() > 20) {
            result.setValid(false);
            result.setMessage("Floor number must be between 1 and 20");
            result.setErrorType("INVALID_RANGE");
            return result;
        }

        // Check room number uniqueness on this floor (regardless of sharing type)
        if (isRoomNumberExistsOnFloor(roomDTO.getAdminId(), roomDTO.getRoomNumber(), roomDTO.getFloorNumber())) {
            result.setValid(false);
            result.setMessage("Room number '" + roomDTO.getRoomNumber() + "' already exists on floor " + roomDTO.getFloorNumber() + 
                            ". Room numbers must be unique per floor.");
            result.setErrorType("DUPLICATE_ROOM");
            
            // Get suggestions for available room numbers
            List<String> suggestions = suggestAvailableRoomNumbers(roomDTO.getAdminId(), roomDTO.getFloorNumber(), 5);
            result.setSuggestedRoomNumbers(suggestions);
            
            return result;
        }

        // Check sharing type ownership
        if (!sharingTypeRepo.isSharingTypeBelongsToAdmin(roomDTO.getSharingTypeId(), roomDTO.getAdminId())) {
            result.setValid(false);
            result.setMessage("Selected sharing type is not available for your account");
            result.setErrorType("INVALID_SHARING_TYPE");
            return result;
        }

        // All validations passed
        result.setValid(true);
        result.setMessage("Room validation passed successfully");
        result.setErrorType("VALID");
        return result;
    }

    // Helper method to validate room number format
    private boolean isValidRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            return false;
        }
        // Basic validation: room number should contain only digits
        return roomNumber.matches("\\d+");
    }

    // Existing methods remain the same with minor enhancements
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
                             "): " + room.getOccupancyStatus() + " - " + room.getRoomStatus() +
                             " - " + room.getSharingTypeName());
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
                         " - Occupancy: " + room.getCurrentOccupancy() + "/" + room.getSharingCapacity() +
                         " - Sharing Type: " + room.getSharingTypeName());
        
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

    // NEW: Enhanced validation result class
    public static class RoomValidationResult {
        private boolean valid;
        private String message;
        private String errorType;
        private List<String> suggestedRoomNumbers;

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getErrorType() { return errorType; }
        public void setErrorType(String errorType) { this.errorType = errorType; }
        
        public List<String> getSuggestedRoomNumbers() { return suggestedRoomNumbers; }
        public void setSuggestedRoomNumbers(List<String> suggestedRoomNumbers) { 
            this.suggestedRoomNumbers = suggestedRoomNumbers; 
        }
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