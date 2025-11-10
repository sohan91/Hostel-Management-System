package com.example.HostelManagement.entities.hostel;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "student", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"admin_id", "student_email"}, name = "unique_admin_student_email"),
           @UniqueConstraint(columnNames = {"admin_id", "student_phone"}, name = "unique_admin_student_phone")
       })
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Integer studentId;
    
    @Column(name = "admin_id", nullable = false)
    private Integer adminId;
    
    @Column(name = "room_id", nullable = false)
    private Integer roomId;
    
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
    @Column(name = "payment_status", columnDefinition = "ENUM('Paid','Pending','Overdue')")
    private PaymentStatus paymentStatus;
    
    @Column(name = "payment_method", length = 100)
    private String paymentMethod;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "blood_group", length = 5)
    private String bloodGroup;
    
    @Column(name = "total_amount")
    private Double totalAmount;
    
    // Enum for payment status
    public enum PaymentStatus {
        Paid, Pending, Overdue
    }
    
    // Default constructor
    public Student() {}
    
    // Parameterized constructor
    public Student(Integer adminId, Integer roomId, String studentName, 
                  String studentEmail, String studentPhone, String studentPassword, 
                  LocalDate dateOfBirth, String parentName, String parentPhone, 
                  LocalDateTime joinDate, PaymentStatus paymentStatus, String paymentMethod, 
                  Boolean isActive, LocalDateTime lastLogin, String bloodGroup, 
                  Double totalAmount) {
        this.adminId = adminId;
        this.roomId = roomId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.studentPhone = studentPhone;
        this.studentPassword = studentPassword;
        this.dateOfBirth = dateOfBirth;
        this.parentName = parentName;
        this.parentPhone = parentPhone;
        this.joinDate = joinDate;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.isActive = isActive;
        this.lastLogin = lastLogin;
        this.bloodGroup = bloodGroup;
        this.totalAmount = totalAmount;
    }
    
    // Getters and Setters
    public Integer getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    
    public Integer getAdminId() {
        return adminId;
    }
    
    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }
    
    public Integer getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public String getStudentEmail() {
        return studentEmail;
    }
    
    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }
    
    public String getStudentPhone() {
        return studentPhone;
    }
    
    public void setStudentPhone(String studentPhone) {
        this.studentPhone = studentPhone;
    }
    
    public String getStudentPassword() {
        return studentPassword;
    }
    
    public void setStudentPassword(String studentPassword) {
        this.studentPassword = studentPassword;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getParentName() {
        return parentName;
    }
    
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
    
    public String getParentPhone() {
        return parentPhone;
    }
    
    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }
    
    public LocalDateTime getJoinDate() {
        return joinDate;
    }
    
    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public String getBloodGroup() {
        return bloodGroup;
    }
    
    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }
    
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", adminId=" + adminId +
                ", roomId=" + roomId +
                ", studentName='" + studentName + '\'' +
                ", studentEmail='" + studentEmail + '\'' +
                ", studentPhone='" + studentPhone + '\'' +
                ", studentPassword='" + studentPassword + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", parentName='" + parentName + '\'' +
                ", parentPhone='" + parentPhone + '\'' +
                ", joinDate=" + joinDate +
                ", paymentStatus=" + paymentStatus +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", isActive=" + isActive +
                ", lastLogin=" + lastLogin +
                ", bloodGroup='" + bloodGroup + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}