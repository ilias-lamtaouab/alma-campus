CREATE DATABASE IF NOT EXISTS scolaris_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE scolaris_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL,
    status ENUM('ACTIVE', 'BLOCKED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cne VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(20),
    enrollment_date DATE NOT NULL,
    status ENUM('ACTIVE', 'SUSPENDED', 'GRADUATED') DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS teachers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cin VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(20),
    specialty VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS modules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    semester INT NOT NULL
);

CREATE TABLE IF NOT EXISTS subjects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    coefficient DECIMAL(4,2) NOT NULL DEFAULT 1.0,
    module_id INT,
    teacher_id INT,
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS grades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    grade_value DECIMAL(5,2) NOT NULL CHECK (grade_value >= 0 AND grade_value <= 20),
    evaluation_type ENUM('CC', 'EXAM', 'PROJECT') NOT NULL,
    evaluation_date DATE NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    UNIQUE KEY unique_student_subject_eval (student_id, subject_id, evaluation_type)
);

-- Insert default admin (password is 'admin123' using BCrypt hash)
INSERT IGNORE INTO users (username, password_hash, role) VALUES ('admin', '$2a$10$xyz...', 'ADMIN');
-- We'll actually generate a proper bcrypt hash in code or replace this later if needed.
-- Let's put a real bcrypt hash for 'admin' -> $2a$10$OKRiICrOPFDW8yFdbZ.2kemWj.WpXWAfu41sjhFw1LTmylA7ZA8du (hash for admin)
-- Wait, let's fix the insert:
DELETE FROM users WHERE username = 'admin';
INSERT INTO users (username, password_hash, role) VALUES ('admin', '$2a$10$OKRiICrOPFDW8yFdbZ.2kemWj.WpXWAfu41sjhFw1LTmylA7ZA8du', 'ADMIN');
