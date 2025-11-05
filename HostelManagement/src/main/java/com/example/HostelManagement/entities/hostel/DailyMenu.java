package com.example.HostelManagement.entities.hostel;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

import com.example.HostelManagement.entities.hostel.admin.Admin;

@Entity
@Table(name = "DailyMenu", 
       uniqueConstraints = @UniqueConstraint(
           name = "unique_admin_menu_date", 
           columnNames = {"admin_id", "menu_date"}
       ))
@Data
public class DailyMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Integer menuId;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "menu_date", nullable = false)
    private LocalDate menuDate;

    @Column(name = "breakfast_items", columnDefinition = "TEXT")
    private String breakfastItems;

    @Column(name = "lunch_items", columnDefinition = "TEXT")
    private String lunchItems;

    @Column(name = "dinner_items", columnDefinition = "TEXT")
    private String dinnerItems;

    @Enumerated(EnumType.STRING)
    @Column(name = "voting_status")
    private VotingStatus votingStatus = VotingStatus.Active;

    @Column(name = "voting_deadline")
    private LocalDateTime votingDeadline;

    @Column(name = "total_students")
    private Integer totalStudents = 0;

    @Column(name = "breakfast_interested")
    private Integer breakfastInterested = 0;

    @Column(name = "lunch_interested")
    private Integer lunchInterested = 0;

    @Column(name = "dinner_interested")
    private Integer dinnerInterested = 0;

    @Column(name = "special_note", columnDefinition = "TEXT")
    private String specialNote;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum VotingStatus {
        Active, Closed, Expired
    }

    public DailyMenu() {}
    
    public DailyMenu(Admin admin, LocalDate menuDate, String breakfastItems, 
                     String lunchItems, String dinnerItems, LocalDateTime votingDeadline) {
        this.admin = admin;
        this.menuDate = menuDate;
        this.breakfastItems = breakfastItems;
        this.lunchItems = lunchItems;
        this.dinnerItems = dinnerItems;
        this.votingDeadline = votingDeadline;
        this.votingStatus = VotingStatus.Active;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}