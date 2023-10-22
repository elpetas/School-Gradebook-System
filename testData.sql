INSERT IGNORE INTO Class (course_number, term, section_number, description) VALUES
    ("CS410", "Sp2023", 1, "Database Management Systems"),
    ("MATH101", "F2023", 2, "Introduction to Calculus");

INSERT IGNORE INTO Category (class_id, name, weight) VALUES
    (1, "Homework", 0.4),
    (1, "Midterm", 0.3),
    (1, "Final Exam", 0.3),
    (2, "Homework", 0.5),
    (2, "Quizzes", 0.2),
    (2, "Final Exam", 0.3);

INSERT IGNORE INTO Assignment (class_id, category_id, name, description, points) VALUES
    (1, 1, "Homework1", "Chapter 1 exercises", 50),
    (1, 1, "Homework2", "Chapter 2 exercises", 50),
    (1, 2, "MidtermExam", "", 100),
    (1, 3, "FinalExam", "", 200),
    (2, 4, "Homework1", "Chapter 1 exercises", 50),
    (2, 4, "Homework2", "Chapter 2 exercises", 50),
    (2, 5, "Quiz1", "", 20),
    (2, 5, "Quiz2", "", 20),
    (2, 5, "Quiz3", "", 20),
    (2, 6, "FinalExam", "", 200);

INSERT IGNORE INTO Student (username, student_id, first_name, last_name) VALUES
    ("jdoe", 12345, "John", "Doe"),
    ("asmith", 54321, "Alice", "Smith");

INSERT IGNORE INTO Enrollment (class_id, student_id) VALUES
    (1, 1),
    (1, 2),
    (2, 1),
    (2, 2);

INSERT IGNORE INTO StudentGrade (student_id, assignment_id, grade) VALUES
    (1, 1, 40),
    (1, 2, 45),
    (1, 3, 80),
    (1, 4, 180),
    (1, 7, 15),
    (1, 8, 20),
    (1, 9, 18),
    (1, 10, 180),
    (2, 1, 45),
    (2, 2, 40),
    (2, 3, 90),
    (2, 4, 180),
    (2, 7, 20),
    (2, 8, 18),
    (2, 9, 16),
    (2, 10, 180);
