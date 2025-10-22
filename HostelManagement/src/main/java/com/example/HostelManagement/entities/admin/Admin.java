package com.example.HostelManagement.entities.admin;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "Admin", schema = "HostelManagement")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "AdminId")
    private Long adminId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    @Column(name = "AdminName", nullable = false, length = 100)
    private String adminName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String adminEmail;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character"
    )
    @Column(name = "Password", nullable = false, length = 255)
    private String adminPassword;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Number must be exactly 10 digits")
    @Column(name = "AdminPhNo", nullable = false, length = 15)
    private String adminPhoneNumber;

    @NotBlank(message = "Hostel name is required")
    @Size(max = 150, message = "Hostel name must be less than 150 characters")
    @Column(name = "HostelName", nullable = false, length = 150)
    private String hostelName;

    @NotBlank(message = "Hostel address is required")
    @Size(max = 300, message = "Hostel address must be less than 300 characters")
    @Column(name = "HostelAddress", nullable = false, length = 300)
    private String hostelAddress;

    @Column(name = "CreatedDate", insertable = false, updatable = false)
    private LocalDateTime createHostelDate;

    // Default constructor
    public Admin() { }

    // Constructor with fields
    public Admin(String adminName, String adminEmail, String adminPassword, String adminPhoneNumber,
                 String hostelName, String hostelAddress) {
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminPhoneNumber = adminPhoneNumber;
        this.hostelName = hostelName;
        this.hostelAddress = hostelAddress;
    }

    public Long getAdminId() {
        return adminId;
    }
    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }
    public String getAdminName() {
        return adminName;
    }
    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
    public String getAdminEmail() {
        return adminEmail;
    }
    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }
    public String getAdminPassword() {
        return adminPassword;
    }
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
    public String getAdminPhoneNumber() {
        return adminPhoneNumber;
    }
    public void setAdminPhoneNumber(String adminPhoneNumber) {
        this.adminPhoneNumber = adminPhoneNumber;
    }
    public String getHostelName() {
        return hostelName;
    }
    public void setHostelName(String hostelName) {
        this.hostelName = hostelName;
    }
    public String getHostelAddress() {
        return hostelAddress;
    }
    public void setHostelAddress(String hostelAddress) {
        this.hostelAddress = hostelAddress;
    }
    public LocalDateTime getCreateHostelDate() {
        return createHostelDate;
    }
    public void setCreateHostelDate(LocalDateTime createHostelDate) {
        this.createHostelDate = createHostelDate;
    }
}
