package com.example.HostelManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.HostelManagement.entities.hostel.Student;
import com.example.HostelManagement.entities.hostel.Rooms;
import com.example.HostelManagement.repositories.StudentRepository;
import com.example.HostelManagement.repositories.AddHostlerToRoom;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AddHostlerToRoom addHostlerToRoom;

    /**
     * Add a new hostler to room with comprehensive validation
     * - Check if student already exists (email/phone)
     * - Check if room exists and has available space
     * - Insert student and increment room occupancy
     */
    @Transactional
    public String addHostlerToRoom(Student student) {
        try {
            // 1. Validate input
            if (student == null) {
                return "Error: Student data cannot be null";
            }
            
            if (student.getAdminId() == null || student.getRoomId() == null) {
                return "Error: Admin ID and Room ID are required";
            }

            // 2. Check if student already exists with same email or phone for this admin
            boolean studentExists = studentRepository.existsByAdminIdAndEmailOrPhone(
                student.getAdminId(), 
                student.getStudentEmail(), 
                student.getStudentPhone()
            );
            
            if (studentExists) {
                // Get duplicate records for detailed error message
                List<Student> duplicates = studentRepository.findPotentialDuplicates(
                    student.getAdminId(), 
                    student.getStudentName(), 
                    student.getStudentEmail(), 
                    student.getStudentPhone()
                );
                
                StringBuilder errorMsg = new StringBuilder("Error: Student already exists. ");
                if (!duplicates.isEmpty()) {
                    errorMsg.append("Matching records found: ");
                    for (Student duplicate : duplicates) {
                        errorMsg.append(String.format("ID=%d (Name: %s, Email: %s, Phone: %s)", 
                            duplicate.getStudentId(), duplicate.getStudentName(), 
                            duplicate.getStudentEmail(), duplicate.getStudentPhone()));
                    }
                }
                return errorMsg.toString();
            }

            // 3. Check if room exists and has available space
            Optional<Rooms> roomOpt = addHostlerToRoom.findByRoomIdWithSharingType(student.getRoomId());
            if (roomOpt.isEmpty()) {
                return "Error: Room not found with ID: " + student.getRoomId();
            }

            Rooms room = roomOpt.get();
            
            // Verify room belongs to the same admin
            if (!room.getAdmin().getAdminId().equals(student.getAdminId())) {
                return "Error: Room does not belong to the specified admin";
            }

            // Check if room has available space
            if (!addHostlerToRoom.hasAvailableSpace(student.getRoomId())) {
                int capacity = room.getSharingType().getCapacity();
                return "Error: Room is already full. Current occupancy: " + room.getCurrentOccupancy() + "/" + capacity;
            }

            // 4. Set default values for new student
            student.setJoinDate(LocalDateTime.now());
            student.setIsActive(true);
            if (student.getPaymentStatus() == null) {
                student.setPaymentStatus(Student.PaymentStatus.Pending);
            }
            if (student.getTotalAmount() == null) {
                student.setTotalAmount(0.0);
            }

            // 5. Save the student
            Student savedStudent = studentRepository.save(student);

            // 6. Increment room occupancy
            addHostlerToRoom.incrementOccupancy(student.getRoomId());

            // 7. Check and update room status if full
            addHostlerToRoom.updateRoomStatusToOccupied(student.getRoomId());

            // 8. Get updated room info for response
            Rooms updatedRoom = addHostlerToRoom.findByRoomIdWithSharingType(student.getRoomId()).get();
            int newOccupancy = updatedRoom.getCurrentOccupancy();
            int capacity = updatedRoom.getSharingType().getCapacity();

            return String.format(
                "Success: Student '%s' added to room successfully! " +
                "Student ID: %d, Room: %s (Floor %d), Occupancy: %d/%d",
                savedStudent.getStudentName(),
                savedStudent.getStudentId(),
                updatedRoom.getRoomNumber(),
                updatedRoom.getFloorNumber(),
                newOccupancy,
                capacity
            );

        } catch (Exception e) {
            throw new RuntimeException("Error adding hostler to room: " + e.getMessage(), e);
        }
    }

    /**
     * Alternative method with detailed parameters for adding hostler to room
     */
    @Transactional
    public String addHostlerToRoomWithDetails(Integer adminId, Integer roomId, String studentName,
                                            String studentEmail, String studentPhone, String studentPassword,
                                            String parentName, String parentPhone, String bloodGroup,
                                            Double totalAmount) {
        try {
            // 1. Validate required parameters
            if (adminId == null || roomId == null || studentName == null || studentEmail == null) {
                return "Error: Admin ID, Room ID, Student Name, and Email are required";
            }

            // 2. Check if email exists
            if (studentRepository.existsByAdminIdAndStudentEmail(adminId, studentEmail)) {
                return "Error: Student with email '" + studentEmail + "' already exists for this admin";
            }

            // 3. Check if phone exists
            if (studentPhone != null && studentRepository.existsByAdminIdAndStudentPhone(adminId, studentPhone)) {
                return "Error: Student with phone '" + studentPhone + "' already exists for this admin";
            }

            // 4. Check room availability with sharing type
            Optional<Rooms> roomOpt = addHostlerToRoom.findByRoomIdWithSharingType(roomId);
            if (roomOpt.isEmpty()) {
                return "Error: Room not found with ID: " + roomId;
            }

            Rooms room = roomOpt.get();
            int capacity = room.getSharingType().getCapacity();
            
            // Verify room belongs to the same admin
            if (!room.getAdmin().getAdminId().equals(adminId)) {
                return "Error: Room does not belong to the specified admin";
            }
            
            if (room.getCurrentOccupancy() >= capacity) {
                return "Error: Room is full. Capacity: " + capacity + ", Current: " + room.getCurrentOccupancy();
            }

            // 5. Create and save student
            Student student = new Student();
            student.setAdminId(adminId);
            student.setRoomId(roomId);
            student.setStudentName(studentName);
            student.setStudentEmail(studentEmail);
            student.setStudentPhone(studentPhone);
            student.setStudentPassword(studentPassword); // Should be encrypted in production
            student.setParentName(parentName);
            student.setParentPhone(parentPhone);
            student.setBloodGroup(bloodGroup);
            student.setTotalAmount(totalAmount != null ? totalAmount : 0.0);
            student.setJoinDate(LocalDateTime.now());
            student.setIsActive(true);
            student.setPaymentStatus(Student.PaymentStatus.Pending);

            Student savedStudent = studentRepository.save(student);

            // 6. Update room occupancy
            addHostlerToRoom.incrementOccupancy(roomId);

            // 7. Update room status if full
            addHostlerToRoom.updateRoomStatusToOccupied(roomId);

            // 8. Get updated occupancy for response
            int newOccupancy = addHostlerToRoom.getCurrentOccupancy(roomId);

            return String.format(
                "Success: Student '%s' added to room successfully! " +
                "Student ID: %d, Room: %s (Floor %d), Occupancy: %d/%d",
                studentName, savedStudent.getStudentId(), room.getRoomNumber(), 
                room.getFloorNumber(), newOccupancy, capacity
            );

        } catch (Exception e) {
            throw new RuntimeException("Error adding hostler to room: " + e.getMessage(), e);
        }
    }

    /**
     * Get available rooms for adding hostlers
     */
    public List<Rooms> getAvailableRoomsForHostler(Integer adminId) {
        try {
            return addHostlerToRoom.findAvailableRooms(adminId);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching available rooms: " + e.getMessage(), e);
        }
    }

    /**
     * Remove hostler from room and update occupancy
     */
    @Transactional
    public String removeHostlerFromRoom(Integer studentId) {
        try {
            Optional<Student> studentOpt = studentRepository.findById(studentId);
            if (studentOpt.isEmpty()) {
                return "Error: Student not found with ID: " + studentId;
            }

            Student student = studentOpt.get();
            
            if (!student.getIsActive()) {
                return "Error: Student is already inactive";
            }

            Integer roomId = student.getRoomId();

            // Deactivate student
            student.setIsActive(false);
            studentRepository.save(student);

            // Decrement room occupancy
            addHostlerToRoom.decrementOccupancy(roomId);

            return String.format(
                "Success: Student '%s' (ID: %d) removed from room. Occupancy updated.",
                student.getStudentName(), studentId
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Error removing hostler from room: " + e.getMessage(), e);
        }
    }

    /**
     * Get room availability info before adding hostler
     */
    public String checkRoomAvailability(Integer roomId) {
        try {
            Optional<Rooms> roomOpt = addHostlerToRoom.findByRoomIdWithSharingType(roomId);
            if (roomOpt.isEmpty()) {
                return "Error: Room not found with ID: " + roomId;
            }
            
            Rooms room = roomOpt.get();
            int capacity = room.getSharingType().getCapacity();
            int available = capacity - room.getCurrentOccupancy();
            boolean hasSpace = addHostlerToRoom.hasAvailableSpace(roomId);
            
            return String.format(
                "Room %s (Floor %d): %d/%d occupied, %d available, Has Space: %s, Status: %s",
                room.getRoomNumber(), room.getFloorNumber(), 
                room.getCurrentOccupancy(), capacity, available, 
                hasSpace ? "Yes" : "No", room.getRoomStatus()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error checking room availability: " + e.getMessage(), e);
        }
    }

    /**
     * Get student by ID
     */
    public Student getStudentById(Integer studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));
    }

    /**
     * Get all students by admin ID
     */
    public List<Student> getStudentsByAdmin(Integer adminId) {
        return studentRepository.findByAdminId(adminId);
    }

    /**
     * Get active students by room ID
     */
    public List<Student> getActiveStudentsByRoom(Integer roomId) {
        return studentRepository.findByRoomIdAndIsActiveTrue(roomId);
    }

    /**
     * Update student payment status
     */
    @Transactional
    public String updatePaymentStatus(Integer studentId, Student.PaymentStatus paymentStatus) {
        try {
            Optional<Student> studentOpt = studentRepository.findById(studentId);
            if (studentOpt.isEmpty()) {
                return "Error: Student not found with ID: " + studentId;
            }

            Student student = studentOpt.get();
            student.setPaymentStatus(paymentStatus);
            studentRepository.save(student);

            return String.format(
                "Success: Payment status updated to '%s' for student '%s'",
                paymentStatus, student.getStudentName()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error updating payment status: " + e.getMessage(), e);
        }
    }

    /**
     * Count active students in a room
     */
    public long countActiveStudentsInRoom(Integer roomId) {
        return studentRepository.countActiveStudentsByRoomId(roomId);
    }

    /**
     * Check if student can be added to room (validation only)
     */
    public String validateStudentAddition(Integer adminId, Integer roomId, String studentEmail, String studentPhone) {
        try {
            // Check if student exists
            if (studentRepository.existsByAdminIdAndEmailOrPhone(adminId, studentEmail, studentPhone)) {
                return "Error: Student with same email or phone already exists";
            }

            // Check room availability
            Optional<Rooms> roomOpt = addHostlerToRoom.findByRoomIdWithSharingType(roomId);
            if (roomOpt.isEmpty()) {
                return "Error: Room not found";
            }

            Rooms room = roomOpt.get();
            if (!room.getAdmin().getAdminId().equals(adminId)) {
                return "Error: Room does not belong to the specified admin";
            }

            if (!addHostlerToRoom.hasAvailableSpace(roomId)) {
                int capacity = room.getSharingType().getCapacity();
                return "Error: Room is full. Current occupancy: " + room.getCurrentOccupancy() + "/" + capacity;
            }

            return "Success: Student can be added to room";
            
        } catch (Exception e) {
            throw new RuntimeException("Error validating student addition: " + e.getMessage(), e);
        }
    }
}