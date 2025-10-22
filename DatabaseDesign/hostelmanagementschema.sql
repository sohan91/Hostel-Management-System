CREATE TABLE HostelManagement.Admin (
    AdminId INT AUTO_INCREMENT PRIMARY KEY,
    AdminName VARCHAR(100) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,
    HostelName VARCHAR(200) NOT NULL,
    HostelAddress VARCHAR(300) NOT NULL,
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE HostelManagement.SharingType (
    SharingTypeId INT AUTO_INCREMENT PRIMARY KEY,
    TypeName VARCHAR(50) NOT NULL,
    SharingFee DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS HostelManagement.Rooms (
    RoomNo INT AUTO_INCREMENT PRIMARY KEY,
    AdminId INT NOT NULL,
    SharingTypeId INT NOT NULL,
    FOREIGN KEY (AdminId) REFERENCES Admin(AdminId) ON DELETE CASCADE,
    FOREIGN KEY (SharingTypeId) REFERENCES SharingType(SharingTypeId) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS HostelManagement.Student (
    StudentId INT AUTO_INCREMENT PRIMARY KEY,
    AdminId INT NOT NULL,
    RoomNo INT NOT NULL,
    StudentName VARCHAR(100) NOT NULL,
    StudentPhone VARCHAR(15),
    JoinDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    PaymentStatus ENUM('Paid','Pending') DEFAULT 'Pending',
    FOREIGN KEY (AdminId) REFERENCES Admin(AdminId) ON DELETE CASCADE,
    FOREIGN KEY (RoomNo) REFERENCES Rooms(RoomNo) ON DELETE CASCADE
);

use HostelManagement;
show tables;