package com.example.HostelManagement.dto;

import java.math.BigDecimal;

public class SharingDetailsDTO {
    private Integer adminId;
    private Integer sharingTypeId;
    private Integer sharingCapacity;
    private BigDecimal sharingFee;
    private String description; // For creation
    private String typeName;    // For display
    private String success;     // Added this field

    // Default constructor
    public SharingDetailsDTO() {}

    // Constructor for query results (matches your existing code)
    public SharingDetailsDTO(Integer adminId, Integer sharingTypeId, Integer sharingCapacity,
                             BigDecimal sharingFee, String success) {
        this.adminId = adminId;
        this.sharingTypeId = sharingTypeId;
        this.sharingCapacity = sharingCapacity;
        this.sharingFee = sharingFee;
        this.success = success;
        this.typeName = sharingCapacity + "-Sharing";
    }

    // Constructor for creation (from frontend)
    public SharingDetailsDTO(Integer adminId, Integer sharingCapacity, BigDecimal sharingFee, String description) {
        this.adminId = adminId;
        this.sharingCapacity = sharingCapacity;
        this.sharingFee = sharingFee;
        this.description = description;
        this.typeName = sharingCapacity + "-Sharing";
    }

    // Full constructor
    public SharingDetailsDTO(Integer sharingTypeId, Integer adminId, Integer sharingCapacity,
                             BigDecimal sharingFee, String description, String typeName, String success) {
        this.sharingTypeId = sharingTypeId;
        this.adminId = adminId;
        this.sharingCapacity = sharingCapacity;
        this.sharingFee = sharingFee;
        this.description = description;
        this.typeName = typeName;
        this.success = success;
    }

    // Getters and Setters
    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public Integer getSharingTypeId() { return sharingTypeId; }
    public void setSharingTypeId(Integer sharingTypeId) { this.sharingTypeId = sharingTypeId; }

    public Integer getSharingCapacity() { return sharingCapacity; }
    public void setSharingCapacity(Integer sharingCapacity) {
        this.sharingCapacity = sharingCapacity;
        if (sharingCapacity != null) {
            this.typeName = sharingCapacity + "-Sharing";
        }
    }

    public BigDecimal getSharingFee() { return sharingFee; }
    public void setSharingFee(BigDecimal sharingFee) { this.sharingFee = sharingFee; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getSuccess() { return success; }
    public void setSuccess(String success) { this.success = success; }

    @Override
    public String toString() {
        return "SharingDetailsDTO{" +
                "adminId=" + adminId +
                ", sharingTypeId=" + sharingTypeId +
                ", sharingCapacity=" + sharingCapacity +
                ", sharingFee=" + sharingFee +
                ", description='" + description + '\'' +
                ", typeName='" + typeName + '\'' +
                ", success='" + success + '\'' +
                '}';
    }
}