CREATE TABLE HostelManagement.Admin (
    AdminId INT IDENTITY(1,1) PRIMARY KEY,
    AdminName NVARCHAR(100) NOT NULL,
    AdminPhNo NVARCHAR(15),
    AdminEmail NVARCHAR(100) UNIQUE NOT NULL,
    AdminPassword NVARCHAR(100) NOT NULL,
    HostelName NVARCHAR(100)
);


select * from HostelManagement.Admin;

CREATE TABLE  HostelManagement.SharingType
(SharingTypeId INT IDENTITY(1,1) PRIMARY KEY,
 TypeName VARCHAR(50) NOT NULL,
  SharingFee DECIMAL (10,2) NOT NULL);

SELECT * FROM HostelManagement.SharingType;

CREATE TABLE HostelManagement.Rooms(
  RoomNo INT IDENTITY(1,1) PRIMARY KEY,
  AdminId INT NOT NULL,
  SharingTypeId INT NOT NULL,
   FOREIGN KEY (AdminId) REFERENCES HostelManagement.Admin(AdminId) ON DELETE CASCADE,
    FOREIGN KEY (SharingTypeId) REFERENCES HostelManagement.SharingType(SharingTypeId)
);

SELECT * FROM HostelManagement.Rooms;

CREATE TABLE HostelManagement.Student
(
 StudentId INT IDENTITY(1,1) PRIMARY KEY,
 AdminId INT NOT NULL,
 RoomNo INT NOT NULL,
 StudentName  VARCHAR(100) NOT NULL,
 StudentPhone VARCHAR(15),
 JoinDate DATE DEFAULT GETDATE(),
 PaymentStatus VARCHAR(20) CHECK(PaymentStatus IN ('Paid','Pending')),
 FOREIGN KEY (AdminId) REFERENCES HostelManagement.Admin(AdminId) ON DELETE CASCADE,
 FOREIGN KEY (RoomNo) REFERENCES HostelManagement.Rooms(RoomNo) ON DELETE NO ACTION
);

SELECT * FROM HostelManagement.Student;

CREATE TABLE Polling.MealType(
MealTypeId INT IDENTITY(1,1) PRIMARY KEY,
MealName VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO Polling.MealType(MealName)
VALUES('Breakfeast'),('Lunch'),('Dinner');

SELECT * FROM Polling.MealType;

