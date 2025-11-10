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

  @Transactional
public String addHostlerToRoom(Student student) {
    try {
        if (student == null) {
            return "Error: Student data cannot be null";
        }
        
        if (student.getAdminId() == null || student.getRoomId() == null) {
            return "Error: Admin ID and Room ID are required";
        }

        boolean studentExists = studentRepository.existsByAdminIdAndEmailOrPhone(
            student.getAdminId(), 
            student.getStudentEmail(), 
            student.getStudentPhone()
        );
        
        if (studentExists) {
            return "Student already exists";
        }

        Optional<Rooms> roomOpt = addHostlerToRoom.findByRoomIdWithSharingType(student.getRoomId());
        if (roomOpt.isEmpty()) {
            return "Error: Room not found with ID: " + student.getRoomId();
        }

        Rooms room = roomOpt.get();
        
        if (!room.getAdmin().getAdminId().equals(student.getAdminId())) {
            return "Error: Room does not belong to the specified admin";
        }

        if (!addHostlerToRoom.hasAvailableSpace(student.getRoomId())) {
            int capacity = room.getSharingType().getCapacity();
            return "Error: Room is already full. Current occupancy: " + room.getCurrentOccupancy() + "/" + capacity;
        }

        student.setJoinDate(LocalDateTime.now());
        student.setIsActive(true);
        if (student.getPaymentStatus() == null) {
            student.setPaymentStatus(Student.PaymentStatus.Pending);
        }
        if (student.getTotalAmount() == null) {
            student.setTotalAmount(0.0);
        }

        Student savedStudent = studentRepository.save(student);

        addHostlerToRoom.incrementOccupancy(student.getRoomId());

        addHostlerToRoom.updateRoomStatusToOccupied(student.getRoomId());

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

    @Transactional
public String addHostlerToRoomWithDetails(Integer adminId, Integer roomId, String studentName,
                                        String studentEmail, String studentPhone, String studentPassword,
                                        String parentName, String parentPhone, String bloodGroup,
                                        Double totalAmount) {
    try {
        if (adminId == null || roomId == null || studentName == null || studentEmail == null) {
            return "Error: Admin ID, Room ID, Student Name, and Email are required";
        }

        boolean studentExists = studentRepository.existsByAdminIdAndEmailOrPhone(adminId, studentEmail, studentPhone);
        
        if (studentExists) {
            return "Student already exists";
        }

        Optional<Rooms> roomOpt = addHostlerToRoom.findByRoomIdWithSharingType(roomId);
        if (roomOpt.isEmpty()) {
            return "Error: Room not found with ID: " + roomId;
        }

        Rooms room = roomOpt.get();
        int capacity = room.getSharingType().getCapacity();
        
        if (!room.getAdmin().getAdminId().equals(adminId)) {
            return "Error: Room does not belong to the specified admin";
        }
        
        if (room.getCurrentOccupancy() >= capacity) {
            return "Error: Room is full. Capacity: " + capacity + ", Current: " + room.getCurrentOccupancy();
        }

        Student student = new Student();
        student.setAdminId(adminId);
        student.setRoomId(roomId);
        student.setStudentName(studentName);
        student.setStudentEmail(studentEmail);
        student.setStudentPhone(studentPhone);
        student.setStudentPassword(studentPassword);
        student.setParentName(parentName);
        student.setParentPhone(parentPhone);
        student.setBloodGroup(bloodGroup);
        student.setTotalAmount(totalAmount != null ? totalAmount : 0.0);
        student.setJoinDate(LocalDateTime.now());
        student.setIsActive(true);
        student.setPaymentStatus(Student.PaymentStatus.Pending);

        Student savedStudent = studentRepository.save(student);

        addHostlerToRoom.incrementOccupancy(roomId);

        addHostlerToRoom.updateRoomStatusToOccupied(roomId);

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

    public List<Rooms> getAvailableRoomsForHostler(Integer adminId) {
        try {
            return addHostlerToRoom.findAvailableRooms(adminId);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching available rooms: " + e.getMessage(), e);
        }
    }

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

            student.setIsActive(false);
            studentRepository.save(student);

            addHostlerToRoom.decrementOccupancy(roomId);

            return String.format(
                "Success: Student '%s' (ID: %d) removed from room. Occupancy updated.",
                student.getStudentName(), studentId
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Error removing hostler from room: " + e.getMessage(), e);
        }
    }

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

    public Student getStudentById(Integer studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));
    }

    public List<Student> getStudentsByAdmin(Integer adminId) {
        return studentRepository.findByAdminId(adminId);
    }

    public List<Student> getActiveStudentsByRoom(Integer roomId) {
        return studentRepository.findByRoomIdAndIsActiveTrue(roomId);
    }

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

    public long countActiveStudentsInRoom(Integer roomId) {
        return studentRepository.countActiveStudentsByRoomId(roomId);
    }

    public String validateStudentAddition(Integer adminId, Integer roomId, String studentEmail, String studentPhone) {
        try {
            if (studentRepository.existsByAdminIdAndEmailOrPhone(adminId, studentEmail, studentPhone)) {
                return "Error: Student with same email or phone already exists in another room";
            }

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

    @Transactional
    public String transferStudentToRoom(Integer studentId, Integer newRoomId) {
        try {
            Optional<Student> studentOpt = studentRepository.findById(studentId);
            if (studentOpt.isEmpty()) {
                return "Error: Student not found with ID: " + studentId;
            }

            Student student = studentOpt.get();
            Integer oldRoomId = student.getRoomId();

            Optional<Rooms> newRoomOpt = addHostlerToRoom.findByRoomIdWithSharingType(newRoomId);
            if (newRoomOpt.isEmpty()) {
                return "Error: New room not found with ID: " + newRoomId;
            }

            Rooms newRoom = newRoomOpt.get();
            if (!newRoom.getAdmin().getAdminId().equals(student.getAdminId())) {
                return "Error: New room does not belong to the same admin";
            }

            if (!addHostlerToRoom.hasAvailableSpace(newRoomId)) {
                int capacity = newRoom.getSharingType().getCapacity();
                return "Error: New room is full. Current occupancy: " + newRoom.getCurrentOccupancy() + "/" + capacity;
            }

            student.setRoomId(newRoomId);
            studentRepository.save(student);

            addHostlerToRoom.decrementOccupancy(oldRoomId);
            addHostlerToRoom.incrementOccupancy(newRoomId);

            addHostlerToRoom.updateRoomStatusToOccupied(newRoomId);

            return String.format(
                "Success: Student '%s' transferred from Room %d to Room %d",
                student.getStudentName(), oldRoomId, newRoomId
            );

        } catch (Exception e) {
            throw new RuntimeException("Error transferring student: " + e.getMessage(), e);
        }
    }
}