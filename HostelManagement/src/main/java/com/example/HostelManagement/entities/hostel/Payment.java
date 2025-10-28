package com.example.HostelManagement.entities.hostel;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

import com.example.HostelManagement.entities.hostel.admin.Admin;

@Entity
@Table(name = "Payment")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Rooms room;

    @ManyToOne
    @JoinColumn(name = "sharing_type_id", nullable = false)
    private SharingType sharingType;

       @Column(name = "amount", nullable = false)
    private Double amount;


    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode")
    private PaymentMode paymentMode = PaymentMode.Cash;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.Pending;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    public enum PaymentMode {
        Cash, Card, UPI, NetBanking
    }

    public enum PaymentStatus {
        Paid, Pending, Failed
    }

     public Payment() {}
    
    public Payment(Student student, Admin admin, Rooms room, SharingType sharingType, 
                   Double amount, PaymentMode paymentMode) {
        this.student = student;
        this.admin = admin;
        this.room = room;
        this.sharingType = sharingType;
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.paymentStatus = PaymentStatus.Pending;
        this.paymentDate = LocalDateTime.now();
    }
}