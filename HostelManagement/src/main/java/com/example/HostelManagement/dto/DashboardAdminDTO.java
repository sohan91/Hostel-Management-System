// DashboardAdminDAO.java (New file)
package com.example.HostelManagement.dto; // Example package

public class DashboardAdminDTO {
    private String firstName;
    private String lastName;
    private String hostelName;

    // All-args constructor
    public DashboardAdminDTO(String firstName, String lastName, String hostelName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.hostelName = hostelName;
    }

    // Getters and Setters (omitted for brevity, but needed for Jackson/JSON)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getHostelName() {
        return hostelName;
    }

    public void setHostelName(String hostelName) {
        this.hostelName = hostelName;
    }
}