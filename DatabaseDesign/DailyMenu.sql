CREATE SCHEMA IF NOT EXISTS Polling;
USE Polling;

-- 1. MealType table
CREATE TABLE MealType (
    meal_type_id INT AUTO_INCREMENT PRIMARY KEY,
    meal_name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Poll table
CREATE TABLE Poll (
    poll_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL,
    meal_type_id INT NOT NULL,
    poll_date DATE NOT NULL,
    poll_title VARCHAR(200),
    status ENUM('Active','Closed') DEFAULT 'Active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_admin_meal_date (admin_id, meal_type_id, poll_date),
    FOREIGN KEY (admin_id) REFERENCES HostelManagement.Admin(admin_id) ON DELETE CASCADE,
    FOREIGN KEY (meal_type_id) REFERENCES MealType(meal_type_id) ON DELETE RESTRICT
);

-- 3. PollOption table
CREATE TABLE PollOption (
    poll_option_id INT AUTO_INCREMENT PRIMARY KEY,
    poll_id INT NOT NULL,
    menu_item VARCHAR(100) NOT NULL,
    description TEXT,
    vote_count INT DEFAULT 0,
    FOREIGN KEY (poll_id) REFERENCES Poll(poll_id) ON DELETE CASCADE
);

-- 4. PollResponse table
CREATE TABLE PollResponse (
    response_id INT AUTO_INCREMENT PRIMARY KEY,
    poll_id INT NOT NULL,
    student_id INT NOT NULL,
    poll_option_id INT NOT NULL,
    response_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_student_poll (poll_id, student_id),
    FOREIGN KEY (poll_id) REFERENCES Poll(poll_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES HostelManagement.Student(student_id) ON DELETE CASCADE,
    FOREIGN KEY (poll_option_id) REFERENCES PollOption(poll_option_id) ON DELETE CASCADE
);

-- Insert default meal types
INSERT INTO MealType (meal_name) VALUES 
('Breakfast'), 
('Lunch'), 
('Dinner');