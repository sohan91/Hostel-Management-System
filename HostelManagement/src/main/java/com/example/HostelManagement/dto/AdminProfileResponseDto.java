package com.example.HostelManagement.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class AdminProfileResponseDto {
    private Long adminId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    
    private HostelInfo hostel;

    public AdminProfileResponseDto() {}

    public AdminProfileResponseDto(Long adminId, String firstName, String lastName, 
                              String email, String phoneNumber, LocalDateTime createdAt, 
                              HostelInfo hostel) {
        this.adminId = adminId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.hostel = hostel;
    }
    public static class HostelInfo {
        private String name;
        private String address;
        private Long hostelId;

        public HostelInfo() {}

        public HostelInfo(String name, String address) {
            this.name = name;
            this.address = address;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public Long getHostelId() { return hostelId; }
        public void setHostelId(Long hostelId) { this.hostelId = hostelId; }
    }
    
}