package com.example.HostelManagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.HostelManagement.entities.hostel.Rooms;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddHostlerToRoom extends JpaRepository<Rooms, Integer> {
    
    // Find room by ID with sharing type and admin
    @Query("SELECT r FROM Rooms r JOIN FETCH r.sharingType JOIN FETCH r.admin WHERE r.roomId = :roomId")
    Optional<Rooms> findByRoomIdWithSharingType(@Param("roomId") Integer roomId);
    
    // Find room by ID
    Optional<Rooms> findByRoomId(Integer roomId);
    
    // Find all rooms by admin ID (using the relationship)
    @Query("SELECT r FROM Rooms r WHERE r.admin.adminId = :adminId")
    List<Rooms> findByAdminId(@Param("adminId") Integer adminId);
    
    // Increment room occupancy when adding hostler
    @Modifying
    @Query("UPDATE Rooms r SET r.currentOccupancy = r.currentOccupancy + 1 WHERE r.roomId = :roomId")
    void incrementOccupancy(@Param("roomId") Integer roomId);
    
    // Decrement room occupancy when removing hostler
    @Modifying
    @Query("UPDATE Rooms r SET r.currentOccupancy = r.currentOccupancy - 1 WHERE r.roomId = :roomId AND r.currentOccupancy > 0")
    void decrementOccupancy(@Param("roomId") Integer roomId);
    
    // Check if room has available space before adding hostler
    @Query("SELECT r.currentOccupancy < st.capacity FROM Rooms r JOIN r.sharingType st WHERE r.roomId = :roomId")
    boolean hasAvailableSpace(@Param("roomId") Integer roomId);
    
    // Get room capacity from sharing type
    @Query("SELECT st.capacity FROM Rooms r JOIN r.sharingType st WHERE r.roomId = :roomId")
    Integer getRoomCapacity(@Param("roomId") Integer roomId);
    
    // Get current occupancy
    @Query("SELECT r.currentOccupancy FROM Rooms r WHERE r.roomId = :roomId")
    Integer getCurrentOccupancy(@Param("roomId") Integer roomId);
    
    // Find available rooms for a specific admin - FIXED (using admin relationship)
    @Query("SELECT r FROM Rooms r JOIN r.sharingType st WHERE r.admin.adminId = :adminId AND r.currentOccupancy < st.capacity AND r.roomStatus = com.example.HostelManagement.entities.hostel.Rooms$RoomStatus.Available")
    List<Rooms> findAvailableRooms(@Param("adminId") Integer adminId);
    
    // Update room status when full
    @Modifying
    @Query("UPDATE Rooms r SET r.roomStatus = 'Occupied' WHERE r.roomId = :roomId AND r.currentOccupancy >= (SELECT st.capacity FROM r.sharingType st)")
    void updateRoomStatusToOccupied(@Param("roomId") Integer roomId);
    
    // Alternative: Use native query for findAvailableRooms
    @Query(value = "SELECT r.* FROM rooms r JOIN sharing_type st ON r.sharing_type_id = st.sharing_type_id WHERE r.admin_id = :adminId AND r.current_occupancy < st.capacity AND r.room_status = 'Available'", nativeQuery = true)
    List<Rooms> findAvailableRoomsNative(@Param("adminId") Integer adminId);
}