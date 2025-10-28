package com.example.HostelManagement.entities.hostel;
import com.example.HostelManagement.entities.hostel.admin.Admin;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Complaint")
@Data
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_id")
    private Integer complaintId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Rooms room;

    @Enumerated(EnumType.STRING)
    @Column(name = "complaint_type")
    private ComplaintType complaintType = ComplaintType.Other;

    @Column(name = "complaint_text", nullable = false, columnDefinition = "TEXT")
    private String complaintText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ComplaintStatus status = ComplaintStatus.Open;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority = Priority.Medium;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    public enum ComplaintType {
        Maintenance, Food, Cleanliness, Staff, WiFi, Other
    }

    public enum ComplaintStatus {
        Open, In_Progress, Resolved
    }

    public enum Priority {
        Low, Medium, High
    }

      public Complaint() {}
    
    public Complaint(Student student, Admin admin, Rooms room, ComplaintType complaintType, 
                     String complaintText, Priority priority) {
        this.student = student;
        this.admin = admin;
        this.room = room;
        this.complaintType = complaintType;
        this.complaintText = complaintText;
        this.priority = priority;
        this.status = ComplaintStatus.Open;
        this.createdAt = LocalDateTime.now();
    }
}