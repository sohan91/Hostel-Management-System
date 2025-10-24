package com.example.HostelManagement.entities.admin;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "Admin", schema = "HostelManagement")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank
    @Email
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$")
    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @NotBlank
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @NotBlank
    @Size(max = 200)
    @Column(name = "hostel_name", nullable = false)
    private String hostelName;

    @NotBlank
    @Size(max = 300)
    @Column(name = "hostel_address", nullable = false)
    private String hostelAddress;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Admin() {}

    public Admin(String firstName, String lastName, String email, String phoneNumber, String password, String hostelName, String hostelAddress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.hostelName = hostelName;
        this.hostelAddress = hostelAddress;
    }

    public Long getAdminId()
     {
         return adminId; 
        }
    public void setAdminId(Long adminId) {
         this.adminId = adminId; 
        }
    public String getFirstName() { 
        return firstName; 
    }
    public void setFirstName(String firstName) 
    {
         this.firstName = firstName; 
        }
    public String getLastName() {
         return lastName;
         }
    public void setLastName(String lastName) { 
        this.lastName = lastName; 
    }
    public String getEmail() {
        return email; 
    }
    public void setEmail(String email) {
         this.email = email;
         }
    public String getPhoneNumber() {
         return phoneNumber; 
        }
    public void setPhoneNumber(String phoneNumber) { 
        this.phoneNumber = phoneNumber; 
    }
    public String getPassword() { 
        return password; 
    }
    public void setPassword(String password) { 
        this.password = password;
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
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
}
