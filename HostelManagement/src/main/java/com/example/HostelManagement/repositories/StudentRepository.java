package com.example.HostelManagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.HostelManagement.entities.hostel.Student;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    
    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.adminId = :adminId AND s.studentEmail = :studentEmail")
    boolean existsByAdminIdAndStudentEmail(@Param("adminId") Integer adminId, @Param("studentEmail") String studentEmail);
    
    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.adminId = :adminId AND s.studentPhone = :studentPhone")
    boolean existsByAdminIdAndStudentPhone(@Param("adminId") Integer adminId, @Param("studentPhone") String studentPhone);
    
    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.adminId = :adminId AND (s.studentEmail = :studentEmail OR s.studentPhone = :studentPhone)")
    boolean existsByAdminIdAndEmailOrPhone(@Param("adminId") Integer adminId, 
                                         @Param("studentEmail") String studentEmail, 
                                         @Param("studentPhone") String studentPhone);
    
    Optional<Student> findByAdminIdAndStudentEmail(Integer adminId, String studentEmail);
    
    Optional<Student> findByAdminIdAndStudentPhone(Integer adminId, String studentPhone);
    
    List<Student> findByAdminId(Integer adminId);
    
    List<Student> findByRoomId(Integer roomId);
    
    List<Student> findByAdminIdAndIsActiveTrue(Integer adminId);
    
    @Query("SELECT COUNT(s) FROM Student s WHERE s.roomId = :roomId AND s.isActive = true")
    long countActiveStudentsByRoomId(@Param("roomId") Integer roomId);
    
    List<Student> findByRoomIdAndIsActiveTrue(Integer roomId);
    
    long countByAdminIdAndIsActiveTrue(Integer adminId);
    
    List<Student> findByRoomIdAndIsActive(Integer roomId, Boolean isActive);
    
    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.adminId = :adminId AND " +
           "(s.studentName = :studentName OR s.studentEmail = :studentEmail OR s.studentPhone = :studentPhone)")
    boolean existsByAdminIdAndNameOrEmailOrPhone(@Param("adminId") Integer adminId,
                                               @Param("studentName") String studentName,
                                               @Param("studentEmail") String studentEmail,
                                               @Param("studentPhone") String studentPhone);
    
    @Query("SELECT s FROM Student s WHERE s.adminId = :adminId AND " +
           "(s.studentName = :studentName OR s.studentEmail = :studentEmail OR s.studentPhone = :studentPhone)")
    List<Student> findPotentialDuplicates(@Param("adminId") Integer adminId,
                                        @Param("studentName") String studentName,
                                        @Param("studentEmail") String studentEmail,
                                        @Param("studentPhone") String studentPhone);

    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.adminId = :adminId AND s.studentEmail = :studentEmail AND s.roomId != :roomId")
    boolean existsByAdminIdAndEmailInDifferentRoom(@Param("adminId") Integer adminId, 
                                                  @Param("studentEmail") String studentEmail, 
                                                  @Param("roomId") Integer roomId);

    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.adminId = :adminId AND s.studentPhone = :studentPhone AND s.roomId != :roomId")
    boolean existsByAdminIdAndPhoneInDifferentRoom(@Param("adminId") Integer adminId, 
                                                  @Param("studentPhone") String studentPhone, 
                                                  @Param("roomId") Integer roomId);
}