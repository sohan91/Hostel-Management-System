package com.example.HostelManagement.entities.admin;

public class LoginResponseDAO {
    private boolean success;
    private String message;
    private String userType;

    public LoginResponseDAO() {

    }

    public LoginResponseDAO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoginResponseDAO(boolean success, String message, String userType)
    {
        this.success = success;
        this.message = message;
        this.userType = userType;
    }

    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getUserType() {
        return userType;
    }
    public void setUserType(String userType) {
        this.userType = userType;
    }
}