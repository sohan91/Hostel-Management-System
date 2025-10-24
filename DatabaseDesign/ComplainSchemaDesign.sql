CREATE SCHEMA IF NOT EXISTS Complaint;
USE Complaint;

-- Complaint table
CREATE TABLE IF NOT EXISTS Complaint (
    ComplaintId INT AUTO_INCREMENT PRIMARY KEY,
    StudentId INT NOT NULL,
    AdminId bigint NOT NULL,
    RoomNo INT NOT NULL,
    ComplaintText VARCHAR(500) NOT NULL,
    Status ENUM('Open','In Progress','Closed') DEFAULT 'Open',
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (StudentId) REFERENCES HostelManagement.Student(StudentId) ON DELETE CASCADE,
    FOREIGN KEY (AdminId) REFERENCES HostelManagement.Admin(admin_id) ON DELETE CASCADE,
    FOREIGN KEY (RoomNo) REFERENCES HostelManagement.Rooms(RoomNo) ON DELETE RESTRICT
);
