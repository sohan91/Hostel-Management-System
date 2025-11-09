package com.example.HostelManagement.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostlerDto {
    private Integer studentId;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private String dateOfBirth;
    private String parentName;
    private String parentPhone;
    private LocalDateTime joinDate;
    private String paymentStatus;
    private Boolean isActive;
     private String bloodGroup;
    // Utility method to create from database result set
    public static HostlerDto fromMap(Map<String, Object> row) {
        if (row == null || row.get("student_id") == null) {
            return null;
        }
        
        return HostlerDto.builder()
            .studentId(getInteger(row, "student_id"))
            .studentName(getString(row, "student_name"))
            .studentEmail(getString(row, "student_email"))
            .studentPhone(getString(row, "student_phone"))
            .dateOfBirth(row.get("date_of_birth") != null ? row.get("date_of_birth").toString() : null)
            .parentName(getString(row, "parent_name"))
            .parentPhone(getString(row, "parent_phone"))
            .joinDate(getLocalDateTime(row, "join_date"))
            .paymentStatus(getString(row, "payment_status"))
            .isActive(getBoolean(row, "is_active"))
            .bloodGroup(getString(row, "blood_group"))
            .build();
    }
    
    // Utility methods for safe data extraction
    private static Integer getInteger(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
    
    private static String getString(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString() : null;
    }
    
    private static Boolean getBoolean(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }
        return null;
    }
    
    private static LocalDateTime getLocalDateTime(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        return null;
    }
    
    // Additional utility methods for frontend formatting
    public String getFormattedJoinDate() {
        if (joinDate != null) {
            return joinDate.toLocalDate().toString();
        }
        return "N/A";
    }
    
    public String getFormattedPaymentStatus() {
        if (paymentStatus != null) {
            return paymentStatus.substring(0, 1).toUpperCase() + paymentStatus.substring(1).toLowerCase();
        }
        return "Unknown";
    }
    
    public boolean hasParentInfo() {
        return parentName != null && !parentName.trim().isEmpty();
    }
}