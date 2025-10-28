package com.example.HostelManagement.entities.hostel.admin;



public class LoginRequestDAO {
    private String email;
    private String password;

    public LoginRequestDAO() {}

    public LoginRequestDAO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}