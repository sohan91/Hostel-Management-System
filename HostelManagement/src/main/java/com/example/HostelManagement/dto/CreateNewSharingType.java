package com.example.HostelManagement.dto;

import java.math.BigDecimal;

public class CreateNewSharingType {
    private Integer adminId;
    private Integer sharingTypeId;
    private Integer capacity;
    private BigDecimal sharingFee;
    private String description;
    private String typeName;
    public CreateNewSharingType () {}

    public CreateNewSharingType (Integer adminId, Integer capacity, BigDecimal sharingFee, String description) {
        this.adminId = adminId;
        this.capacity = capacity;
        this.sharingFee = sharingFee;
        this.description = description;
        this.typeName = capacity + "-Sharing";
    }

    public CreateNewSharingType (Integer sharingTypeId, Integer adminId, Integer capacity,
                             BigDecimal sharingFee, String description, String typeName) {
        this.sharingTypeId = sharingTypeId;
        this.adminId = adminId;
        this.capacity = capacity;
        this.sharingFee = sharingFee;
        this.description = description;
        this.typeName = typeName;
    }

    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public Integer getSharingTypeId() { return sharingTypeId; }
    public void setSharingTypeId(Integer sharingTypeId) { this.sharingTypeId = sharingTypeId; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
        if (capacity != null) {
            this.typeName = capacity + "-Sharing";
        }
    }

    public BigDecimal getSharingFee() { return sharingFee; }
    public void setSharingFee(BigDecimal sharingFee) { this.sharingFee = sharingFee; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
}