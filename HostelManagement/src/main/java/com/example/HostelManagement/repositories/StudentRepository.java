package com.example.HostelManagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import com.example.HostelManagement.entities.hostel.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    
    // Check if email exists for specific admin (composite unique constraint)
    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.adminId = :adminId AND s.studentEmail = :studentEmail")
    boolean existsByAdminIdAndStudentEmail(@Param("adminId") Integer adminId, @Param("studentEmail") String studentEmail);
    
    // Check if phone exists for specific admin (composite unique constraint)
    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.adminId = :adminId AND s.studentPhone = :studentPhone")
    boolean existsByAdminIdAndStudentPhone(@Param("adminId") Integer adminId, @Param("studentPhone") String studentPhone);
    
    // Check if student already exists with same email or phone for admin (for hostler validation)
    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.adminId = :adminId AND (s.studentEmail = :studentEmail OR s.studentPhone = :studentPhone)")
    boolean existsByAdminIdAndEmailOrPhone(@Param("adminId") Integer adminId, 
                                         @Param("studentEmail") String studentEmail, 
                                         @Param("studentPhone") String studentPhone);
    
    // Find student by email and admin ID
    Optional<Student> findByAdminIdAndStudentEmail(Integer adminId, String studentEmail);
    
    // Find student by phone and admin ID
    Optional<Student> findByAdminIdAndStudentPhone(Integer adminId, String studentPhone);
    
    // Find all students by admin ID
    List<Student> findByAdminId(Integer adminId);
    
    // Find all students by room ID
    List<Student> findByRoomId(Integer roomId);
    
    // Find active students by admin ID
    List<Student> findByAdminIdAndIsActiveTrue(Integer adminId);
    
    // Count active students by room ID (for occupancy check)
    @Query("SELECT COUNT(s) FROM Student s WHERE s.roomId = :roomId AND s.isActive = true")
    long countActiveStudentsByRoomId(@Param("roomId") Integer roomId);
    
    // Find active students by room ID
    List<Student> findByRoomIdAndIsActiveTrue(Integer roomId);
    
    // Count total active students by admin ID
    long countByAdminIdAndIsActiveTrue(Integer adminId);
    
    // Find students by room ID and active status (for room occupancy management)
    List<Student> findByRoomIdAndIsActive(Integer roomId, Boolean isActive);
    
    // Check if student exists with same name, email or phone for specific admin (comprehensive check)
    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.adminId = :adminId AND " +
           "(s.studentName = :studentName OR s.studentEmail = :studentEmail OR s.studentPhone = :studentPhone)")
    boolean existsByAdminIdAndNameOrEmailOrPhone(@Param("adminId") Integer adminId,
                                               @Param("studentName") String studentName,
                                               @Param("studentEmail") String studentEmail,
                                               @Param("studentPhone") String studentPhone);
    
    // Find duplicate students (same name, email, or phone within same admin)
    @Query("SELECT s FROM Student s WHERE s.adminId = :adminId AND " +
           "(s.studentName = :studentName OR s.studentEmail = :studentEmail OR s.studentPhone = :studentPhone)")
    List<Student> findPotentialDuplicates(@Param("adminId") Integer adminId,
                                        @Param("studentName") String studentName,
                                        @Param("studentEmail") String studentEmail,
                                        @Param("studentPhone") String studentPhone);
}