create schema polling;

use polling;
CREATE TABLE IF NOT EXISTS MealType (
    MealTypeId INT AUTO_INCREMENT PRIMARY KEY,
    MealName VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO MealType (MealName) VALUES ('Breakfast'), ('Lunch'), ('Dinner');

CREATE TABLE IF NOT EXISTS Poll (
    PollId INT AUTO_INCREMENT PRIMARY KEY,
    AdminId INT NOT NULL,         
    MealTypeId INT NOT NULL,
    PollDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    Status ENUM('Active','Closed') DEFAULT 'Active',
    UNIQUE KEY UQ_Admin_PollDate (AdminId, MealTypeId, PollDate),
    FOREIGN KEY (AdminId) REFERENCES hostelmanagement.Admin(AdminId) ON DELETE CASCADE,
    FOREIGN KEY (MealTypeId) REFERENCES MealType(MealTypeId) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS PollOption (
    PollOptionId INT AUTO_INCREMENT PRIMARY KEY,
    PollId INT NOT NULL,
    MenuItem VARCHAR(100) NOT NULL,
    Description VARCHAR(255),
    FOREIGN KEY (PollId) REFERENCES Poll(PollId) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS PollResponse (
    ResponseId INT AUTO_INCREMENT PRIMARY KEY,
    PollId INT NOT NULL,
    StudentId INT NOT NULL,
    Response ENUM('Interested','Not Interested'),
    ResponseTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY UQ_Student_Poll (PollId, StudentId),
    FOREIGN KEY (PollId) REFERENCES Poll(PollId) ON DELETE CASCADE,
    FOREIGN KEY (StudentId) REFERENCES hostelmanagement.Student(StudentId) ON DELETE CASCADE
);

