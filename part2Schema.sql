USE grademanagementsystem;
DROP TABLE IF EXISTS StudentGrade;
DROP TABLE IF EXISTS Enrollment;
DROP TABLE IF EXISTS Student;
DROP TABLE IF EXISTS Assignment;
DROP TABLE IF EXISTS Category;
DROP TABLE IF EXISTS Class;

CREATE DATABASE IF NOT EXISTS grademanagementsystem;

USE grademanagementsystem;

CREATE TABLE IF NOT EXISTS Class (
    id INT AUTO_INCREMENT PRIMARY KEY,
    course_number VARCHAR(255) NOT NULL,
    term VARCHAR(255) NOT NULL,
    section_number INT NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS Category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_id INT,
    name VARCHAR(255) NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (class_id) REFERENCES Class(id)
);

CREATE TABLE IF NOT EXISTS Assignment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_id INT,
    category_id INT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    points INT NOT NULL,
    UNIQUE (class_id, name),
    FOREIGN KEY (class_id) REFERENCES Class(id),
    FOREIGN KEY (category_id) REFERENCES Category(id)
);

CREATE TABLE IF NOT EXISTS Student (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    student_id INT NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS Enrollment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_id INT,
    student_id INT,
    UNIQUE (class_id, student_id),
    FOREIGN KEY (class_id) REFERENCES Class(id),
    FOREIGN KEY (student_id) REFERENCES Student(id)
);

CREATE TABLE IF NOT EXISTS StudentGrade (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT,
    assignment_id INT,
    grade INT NOT NULL,
    UNIQUE (student_id, assignment_id),
    FOREIGN KEY (student_id) REFERENCES Student(id),
    FOREIGN KEY (assignment_id) REFERENCES Assignment(id)
);
