CREATE TABLE Complaints.Complaint (
    ComplaintId INT IDENTITY(1,1) PRIMARY KEY,
    StudentId INT NOT NULL,
    AdminId INT NOT NULL,
    RoomNo INT NOT NULL,
    ComplaintText NVARCHAR(500) NOT NULL,
    Status NVARCHAR(20) DEFAULT 'Open' CHECK (Status IN ('Open', 'In Progress', 'Closed')),
    CreatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (StudentId) REFERENCES HostelManagement.Student(StudentId) ON DELETE CASCADE,
    FOREIGN KEY (AdminId) REFERENCES HostelManagement.Admin(AdminId) ON DELETE CASCADE,
    FOREIGN KEY (RoomNo) REFERENCES HostelManagement.Rooms(RoomNo) ON DELETE NO ACTION
);
