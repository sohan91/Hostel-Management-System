CREATE SCHEMA IF NOT EXISTS Polling;
USE Polling;

-- MealType table
DROP TABLE IF EXISTS MealType;

DROP TABLE IF EXISTS polling.MealType;
CREATE TABLE  polling.MealType (
    meal_type_id INT AUTO_INCREMENT PRIMARY KEY,
    meal_name VARCHAR(50) NOT NULL UNIQUE
);


INSERT INTO polling.MealType (meal_name) VALUES ('Breakfast'), ('Lunch'), ('Dinner');

-- Poll table
DROP TABLE IF EXISTS polling.Poll;
CREATE TABLE Polling.Poll (
    poll_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    meal_type_id INT NOT NULL,
    poll_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Active','Closed') DEFAULT 'Active',
    UNIQUE KEY UQ_Admin_PollDate (admin_id, meal_type_id, poll_date),
    FOREIGN KEY (admin_id) REFERENCES HostelManagement.Admin(admin_id) ON DELETE CASCADE,
    FOREIGN KEY (meal_type_id) REFERENCES Polling.MealType(meal_type_id) ON DELETE RESTRICT
);

-- PollOption table
DROP TABLE IF EXISTS PollOption;
CREATE TABLE polling.PollOption (
    poll_option_id INT AUTO_INCREMENT PRIMARY KEY,
    poll_id INT NOT NULL,
    menu_item VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    FOREIGN KEY (poll_id) REFERENCES Poll(poll_id) ON DELETE CASCADE
);

-- PollResponse table
DROP TABLE IF EXISTS polling.PollResponse;
CREATE TABLE polling.PollResponse (
    response_id INT AUTO_INCREMENT PRIMARY KEY,
    poll_id INT NOT NULL,
    student_id INT NOT NULL,
    response ENUM('Interested','Not Interested'),
    response_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY UQ_Student_Poll (poll_id, student_id),
    FOREIGN KEY (poll_id) REFERENCES Poll(poll_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES HostelManagement.Student(StudentId) ON DELETE CASCADE
);
