package com.example.HostelManagement.entities.hostel;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.example.HostelManagement.entities.hostel.admin.Admin;

@Entity
@Table(name = "SharingType")
@Data
public class SharingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sharing_type_id")
    private Integer sharingTypeId;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName;

   @Column(name = "sharing_fee", nullable = false) 
    private BigDecimal sharingFee;

    @Column(name = "capacity", nullable = false)
    private Integer capacity = 1;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

        public SharingType() {}
    
    public SharingType(Admin admin, String typeName, BigDecimal sharingFee, Integer capacity, String description) 
    {
        this.admin = admin;
        this.typeName = typeName;
        this.sharingFee = sharingFee;
        this.capacity = capacity;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getSharingTypeId() {
        return sharingTypeId;
    }

    public void setSharingTypeId(Integer sharingTypeId) {
        this.sharingTypeId = sharingTypeId;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public BigDecimal getSharingFee() {
        return sharingFee;
    }

    public void setSharingFee(BigDecimal sharingFee) {
        this.sharingFee = sharingFee;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}