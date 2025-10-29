package com.example.HostelManagement.dto;
import lombok.Data;

@Data
public class DashBoardResponseDto
 {
    private Long adminId;
    private String firstName;
    private String lastName;
    private String email;
    private String hostelName;
    private Long hostelId;

    public DashBoardResponseDto
    () {}
}