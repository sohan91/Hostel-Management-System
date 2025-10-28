package com.example.HostelManagement.entities.hostel;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.Data;



@Data
@Entity
@Table(name = "StudentMenuResponse", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"StudentId", "MenuId"})
})
public class StudentMenuResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer responseId;

    @ManyToOne
    @JoinColumn(name = "StudentId", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "MenuId", nullable = false)
    private DailyMenu menu;

    @Enumerated(EnumType.STRING)
    @Column(name = "BreakfastResponse")
    private MealResponse breakfastResponse = MealResponse.Not_Responded;

    @Enumerated(EnumType.STRING)
    @Column(name = "LunchResponse")
    private MealResponse lunchResponse = MealResponse.Not_Responded;

    @Enumerated(EnumType.STRING)
    @Column(name = "DinnerResponse")
    private MealResponse dinnerResponse = MealResponse.Not_Responded;

    @Column(name = "ResponseTime")
    private LocalDateTime responseTime;

         public StudentMenuResponse() {}
    
    public StudentMenuResponse(Student student, DailyMenu menu) {
        this.student = student;
        this.menu = menu;
        this.responseTime = LocalDateTime.now();
    }
    public enum MealResponse {
        Interested, Not_Interested, Not_Responded
    }


}