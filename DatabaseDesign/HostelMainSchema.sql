-- ===========================================
-- ⚙️  Hostel Management Schema (Recreate Script)
-- ===========================================

-- 1️⃣ Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS StudentMenuResponse;
DROP TABLE IF EXISTS DailyMenu;
DROP TABLE IF EXISTS Feedback;
DROP TABLE IF EXISTS Complaint;
DROP TABLE IF EXISTS Payment;
DROP TABLE IF EXISTS Student;
DROP TABLE IF EXISTS Rooms;
DROP TABLE IF EXISTS SharingType;
DROP TABLE IF EXISTS Notice;
DROP TABLE IF EXISTS Admin;

-- 2️⃣ Create schema (if not exists)
CREATE SCHEMA IF NOT EXISTS HostelManagement;
USE HostelManagement;

-- ===========================================
-- 🧱  TABLE DEFINITIONS
-- ===========================================

-- 1. Admin table
CREATE TABLE Admin (
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

-- 2. SharingType table
CREATE TABLE SharingType (
    sharing_type_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL,
    type_name VARCHAR(50) NOT NULL,
    sharing_fee DECIMAL(10,2) NOT NULL,
    capacity INT NOT NULL DEFAULT 1,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES Admin(admin_id) ON DELETE CASCADE
);

-- 3. Rooms table
CREATE TABLE Rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL,
    sharing_type_id INT NOT NULL,
    room_number VARCHAR(10) NOT NULL,
    floor_number INT NOT NULL,
    room_status ENUM('Available','Occupied','Maintenance') DEFAULT 'Available',
    current_occupancy INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_admin_room (admin_id, room_number),
    FOREIGN KEY (admin_id) REFERENCES Admin(admin_id) ON DELETE CASCADE,
    FOREIGN KEY (sharing_type_id) REFERENCES SharingType(sharing_type_id) ON DELETE CASCADE
);

-- 4. Student table
CREATE TABLE Student (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL,
    room_id INT NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    student_email VARCHAR(100) UNIQUE,
    student_phone VARCHAR(15),
    student_password VARCHAR(255),
    date_of_birth DATE,
    parent_name VARCHAR(100),
    parent_phone VARCHAR(15),
    join_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    payment_status ENUM('Paid','Pending','Overdue') DEFAULT 'Pending',
    is_active BOOLEAN DEFAULT TRUE,
    last_login DATETIME,
    FOREIGN KEY (admin_id) REFERENCES Admin(admin_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES Rooms(room_id) ON DELETE CASCADE
);

-- 5. Payment table
CREATE TABLE Payment (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    admin_id INT NOT NULL,
    room_id INT NOT NULL,
    sharing_type_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    due_date DATE,
    payment_mode ENUM('Cash','Card','UPI','NetBanking') DEFAULT 'Cash',
    payment_status ENUM('Paid','Pending','Failed') DEFAULT 'Pending',
    transaction_id VARCHAR(100),
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES Admin(admin_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES Rooms(room_id) ON DELETE CASCADE,
    FOREIGN KEY (sharing_type_id) REFERENCES SharingType(sharing_type_id) ON DELETE RESTRICT
);

-- 6. Complaint table
CREATE TABLE Complaint (
    complaint_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    admin_id INT NOT NULL,
    room_id INT NOT NULL,
    complaint_type ENUM('Maintenance','Food','Cleanliness','Staff','WiFi','Other') DEFAULT 'Other',
    complaint_text TEXT NOT NULL,
    status ENUM('Open','In Progress','Resolved') DEFAULT 'Open',
    priority ENUM('Low','Medium','High') DEFAULT 'Medium',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME NULL,
    resolution_notes TEXT,
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES Admin(admin_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES Rooms(room_id) ON DELETE RESTRICT
);

-- 7. Feedback table
CREATE TABLE Feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    admin_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    feedback_type ENUM('Hostel','Food','Staff','Facilities','Other'),
    feedback_text TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_anonymous BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES Admin(admin_id) ON DELETE CASCADE
);

-- 8. DailyMenu table
CREATE TABLE DailyMenu (
    menu_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL,
    menu_date DATE NOT NULL,
    breakfast_items TEXT,
    lunch_items TEXT,
    dinner_items TEXT,
    voting_status ENUM('Active', 'Closed', 'Expired') DEFAULT 'Active',
    voting_deadline DATETIME,
    total_students INT DEFAULT 0,
    breakfast_interested INT DEFAULT 0,
    lunch_interested INT DEFAULT 0,
    dinner_interested INT DEFAULT 0,
    special_note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_admin_menu_date (admin_id, menu_date),
    FOREIGN KEY (admin_id) REFERENCES Admin(admin_id) ON DELETE CASCADE
);

-- 9. StudentMenuResponse table
CREATE TABLE StudentMenuResponse (
    response_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    menu_id INT NOT NULL,
    breakfast_response ENUM('Interested', 'Not_Interested', 'Not_Responded') DEFAULT 'Not_Responded',
    lunch_response ENUM('Interested', 'Not_Interested', 'Not_Responded') DEFAULT 'Not_Responded',
    dinner_response ENUM('Interested', 'Not_Interested', 'Not_Responded') DEFAULT 'Not_Responded',
    response_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_student_menu (student_id, menu_id),
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES DailyMenu(menu_id) ON DELETE CASCADE
);

-- 10. Notice table
CREATE TABLE Notice (
    notice_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    notice_text TEXT NOT NULL,
    notice_type ENUM('General','Payment','Maintenance','Event','Urgent') DEFAULT 'General',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expiry_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (admin_id) REFERENCES Admin(admin_id) ON DELETE CASCADE
);
