CREATE TABLE Polling.Poll(
   PollId INT IDENTITY(1,1) PRIMARY KEY,
   AdminId INT NOT NULL,
   MealTypeId INT NOT NULL,
   PollDate DATE DEFAULT CAST(GETDATE() AS DATE),
   Status VARCHAR(20) DEFAULT 'Active' CHECK (Status IN('Active', 'Closed')),
   FOREIGN KEY (AdminId) REFERENCES HostelManagement.Admin(AdminId) ON DELETE CASCADE,
    FOREIGN KEY (MealTypeId) REFERENCES Polling.MealType(MealTypeId),
    CONSTRAINT UQ_Admin_PollDate UNIQUE (AdminId, MealTypeId, PollDate)
    );


CREATE TABLE Polling.PollOption(
  PollOptioned INT IDENTITY(1,1) PRIMARY KEY,
  PollId INT NOT NULL,
  MenuItem VARCHAR(100) NOT NULL,
  Description VARCHAR(255),
  FOREIGN KEY(PollId) REFERENCES Polling.Poll(PollId) ON DELETE cascade
);

-- Poll table
CREATE TABLE Polling.Poll(
   PollId INT IDENTITY(1,1) PRIMARY KEY,
   AdminId INT NOT NULL,
   MealTypeId INT NOT NULL,
   PollDate DATE DEFAULT CAST(GETDATE() AS DATE),
   Status VARCHAR(20) DEFAULT 'Active' CHECK (Status IN('Active', 'Closed')),
   FOREIGN KEY (AdminId) REFERENCES HostelManagement.Admin(AdminId) ON DELETE CASCADE,
   FOREIGN KEY (MealTypeId) REFERENCES Polling.MealType(MealTypeId),
   CONSTRAINT UQ_Admin_PollDate UNIQUE (AdminId, MealTypeId, PollDate)
);

-- PollOption table
CREATE TABLE Polling.PollOption(
  PollOptionId INT IDENTITY(1,1) PRIMARY KEY,
  PollId INT NOT NULL,
  MenuItem VARCHAR(100) NOT NULL,
  Description VARCHAR(255),
  FOREIGN KEY(PollId) REFERENCES Polling.Poll(PollId) ON DELETE CASCADE
);

-- PollResponse table
CREATE TABLE Polling.PollResponse (
    ResponseId INT IDENTITY(1,1) PRIMARY KEY,
    PollId INT NOT NULL,
    StudentId INT NOT NULL,
    Response NVARCHAR(20) CHECK (Response IN ('Interested', 'Not Interested')),
    ResponseTime DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (PollId) REFERENCES Polling.Poll(PollId) ON DELETE CASCADE,
    FOREIGN KEY (StudentId) REFERENCES HostelManagement.Student(StudentId) ON DELETE NO ACTION,
    CONSTRAINT UQ_Student_Poll UNIQUE (PollId, StudentId)
);


