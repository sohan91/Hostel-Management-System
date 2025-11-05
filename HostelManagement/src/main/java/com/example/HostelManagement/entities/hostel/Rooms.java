package com.example.HostelManagement.entities.hostel;

import jakarta.persistence.*;

import lombok.Data;

import java.time.LocalDateTime;

import com.example.HostelManagement.entities.hostel.admin.Admin;

@Data
@Entity
@Table(name = "Rooms", 
       uniqueConstraints = @UniqueConstraint(
           name = "unique_admin_room_floor", 
           columnNames = {"admin_id", "room_number", "floor_number"}
       ))
public class Rooms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Integer roomId;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @ManyToOne
    @JoinColumn(name = "sharing_type_id", nullable = false)
    private SharingType sharingType;

    @Column(name = "room_number", nullable = false, length = 10)
    private String roomNumber;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_status")
    private RoomStatus roomStatus = RoomStatus.Available;

    @Column(name = "current_occupancy")
    private Integer currentOccupancy = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum RoomStatus {
        Available, Occupied, Maintenance
    }
    
    public Rooms() {}
    
    public Rooms(Admin admin, SharingType sharingType, String roomNumber, Integer floorNumber) {
        this.admin = admin;
        this.sharingType = sharingType;
        this.roomNumber = roomNumber;
        this.floorNumber = floorNumber;
        this.roomStatus = RoomStatus.Available;
        this.currentOccupancy = 0;
        this.createdAt = LocalDateTime.now();
    }
}