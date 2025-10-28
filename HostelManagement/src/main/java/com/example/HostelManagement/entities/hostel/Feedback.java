package com.example.HostelManagement.entities.hostel;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.example.HostelManagement.entities.hostel.admin.Admin;

@Entity
@Table(name = "Feedback")
@Data
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type")
    private FeedbackType feedbackType;

    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    public enum FeedbackType {
        Hostel, Food, Staff, Facilities, Other
    }

     public Feedback() {}
    
    public Feedback(Student student, Admin admin, Integer rating, FeedbackType feedbackType, 
                    String feedbackText) {
        this.student = student;
        this.admin = admin;
        this.rating = rating;
        this.feedbackType = feedbackType;
        this.feedbackText = feedbackText;
        this.createdAt = LocalDateTime.now();
        this.isAnonymous = false;
    }
}