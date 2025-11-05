package com.example.HostelManagement.entities.hostel;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

import com.example.HostelManagement.entities.hostel.admin.Admin;

@Entity
@Table(name = "Student", 
       uniqueConstraints = {
           @UniqueConstraint(
               name = "unique_admin_student_email", 
               columnNames = {"admin_id", "student_email"}
           ),
           @UniqueConstraint(
               name = "unique_admin_student_phone", 
               columnNames = {"admin_id", "student_phone"}
           )
       })
@Data
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Integer studentId;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Rooms room;

    @Column(name = "student_name", nullable = false, length = 100)
    private String studentName;

    @Column(name = "student_email", length = 100)
    private String studentEmail;

    @Column(name = "student_phone", length = 15)
    private String studentPhone;

    @Column(name = "student_password", length = 255)
    private String studentPassword;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "parent_name", length = 100)
    private String parentName;

    @Column(name = "parent_phone", length = 15)
    private String parentPhone;

    @Column(name = "join_date")
    private LocalDateTime joinDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.Pending;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    public enum PaymentStatus {
        Paid, Pending, Overdue
    }
    
    public Student(){}
    
    public Student(Admin admin, Rooms room, String studentName, String studentEmail, 
                   String studentPhone, String studentPassword) {
        this.admin = admin;
        this.room = room;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.studentPhone = studentPhone;
        this.studentPassword = studentPassword;
        this.paymentStatus = PaymentStatus.Pending;
        this.isActive = true;
        this.joinDate = LocalDateTime.now();
    }
}