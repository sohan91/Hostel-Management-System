create schema HostelManagement;

DROP TABLE IF EXISTS Admin;

CREATE TABLE HostelManagement.Admin (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(15) NOT NULL,
    password VARCHAR(255) NOT NULL,
    hostel_name VARCHAR(200) NOT NULL,
    hostel_address VARCHAR(300) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
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
    FOREIGN KEY (AdminId) REFERENCES Admin(admin_id) ON DELETE CASCADE,
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
    FOREIGN KEY (AdminId) REFERENCES Admin(admin_id) ON DELETE CASCADE,
    FOREIGN KEY (RoomNo) REFERENCES Rooms(RoomNo) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS HostelManagement.Payment (
    PaymentId INT AUTO_INCREMENT PRIMARY KEY,
    StudentId INT NOT NULL,
    AdminId INT NOT NULL,
    RoomNo INT NOT NULL,
    SharingTypeId INT NOT NULL,
    Amount DECIMAL(10,2) NOT NULL,
    PaymentDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    PaymentMode ENUM('Cash','Card','UPI','NetBanking') DEFAULT 'Cash',
    PaymentStatus ENUM('Paid','Pending','Failed') DEFAULT 'Pending',
    FOREIGN KEY (StudentId) REFERENCES HostelManagement.Student(StudentId) ON DELETE CASCADE,
    FOREIGN KEY (AdminId) REFERENCES HostelManagement.Admin(admin_id) ON DELETE CASCADE,
    FOREIGN KEY (RoomNo) REFERENCES HostelManagement.Rooms(RoomNo) ON DELETE CASCADE,
    FOREIGN KEY (SharingTypeId) REFERENCES HostelManagement.SharingType(SharingTypeId) ON DELETE RESTRICT
);

