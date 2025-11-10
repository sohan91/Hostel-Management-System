    package com.example.HostelManagement.dto;

    import java.time.LocalDate;
    public record BookHostler(
        String student_id,
        String admin_id,
        String room_id,
        String student_name,
        String student_email,
        String student_phone,
        String student_password,
        LocalDate date_of_birth,
        String parent_name,
        String parent_phone,
        LocalDate join_date,
        String payment_status,
        String payment_method,
        boolean is_active,
        LocalDate last_login,
        String blood_group,
        double total_amount
    ) {}