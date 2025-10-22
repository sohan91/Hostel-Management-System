create schema complaint;

CREATE TABLE IF NOT EXISTS Complaint (
    ComplaintId INT AUTO_INCREMENT PRIMARY KEY,
    StudentId INT NOT NULL,
    AdminId INT NOT NULL,
    RoomNo INT NOT NULL,
    ComplaintText VARCHAR(500) NOT NULL,
    Status ENUM('Open','In Progress','Closed') DEFAULT 'Open',
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (StudentId) REFERENCES hostelmanagement.Student(StudentId) ON DELETE CASCADE,
    FOREIGN KEY (AdminId) REFERENCES hostelmanagement.Admin(AdminId) ON DELETE CASCADE,
    FOREIGN KEY (RoomNo) REFERENCES hostelmanagement.Rooms(RoomNo) ON DELETE RESTRICT
);