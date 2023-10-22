import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
/**
 * @author Johann Vargas & KC Kircher
 */
public class GradeManagementSystem {
    private static Connection connection;
    private static String activeClass = null;
    //sets activeClassId to invalid option by default
    private static int activeClassId = -1;

    public static void main(String[] args) {
        try {
            // Connect to the database
            String url = "jdbc:mysql://localhost:53791/grademanagementsystem?verifyServerCertificate=false&useSSL=true";
            String user = "msandbox";
            String password = "fiertt33";
            connection = DriverManager.getConnection(url, user, password);
    
            // Set up the database schema
            setupDatabase();
    
            // Ask user if they want to load the sample data
            System.out.println("\n\nWould you like to load the sample data? (y/n)");
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine();
            if (answer.equalsIgnoreCase("y")) {
                loadSampleData();
            }
    
            // Test the connection
            String query = "SELECT COUNT(*) FROM Class";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
    
            while (resultSet.next()) {
                int count = resultSet.getInt(1);
                System.out.println("Test Connection Made. \n \nThe total number of classes in the DB is: ->" + count + "<-");
                
            }
    
            // Command loop
            while (true) {
                System.out.println("\n\n-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n");
                System.out.println("Select an option:");
                System.out.println("exit: exit the program");
                System.out.println("new-class <course_number> <term> <section_number> <description>: create a new class");
                System.out.println("list-classes: list all classes");
                System.out.println("select-class <course_number> <term> <section_number>: select a class");
                System.out.println("show-class: show the active class\n\n");
    
                if (activeClassId != -1) {
                    System.out.println("show-categories: list the categories with their weights");
                    System.out.println("add-category <name> <weight>: add a new category");
                    System.out.println("show-assignments: list the assignments with their point values, grouped by category");
                    System.out.println("add-assignment <name> , <category> , <description> , <points>: add a new assignment");
                    System.out.println("\n\nshow-students: list the students enrolled in the class");
                    System.out.println("add-student <username> <student_id> <first_name> <last_name>: add a new student and enroll them in the current class.");
                    System.out.println("add-student <username>: enroll an existing student in the current class.");
                    System.out.println("grade <assignment name> <username> <grade>: grade an assignment for a student");
                    System.out.println("student-grades <username>: list all grades for a student");
                    System.out.println("gradebook: show the gradebook for the current class");
                }
    
                System.out.print("\n\n> ");
                String command = scanner.nextLine();
            
                if (command.equalsIgnoreCase("exit")) {
                    break;
                } else if (command.startsWith("new-class ")) {
                    createNewClass(command);
                } else if (command.startsWith("list-classes")) {
                    listClasses();
                } else if (command.startsWith("select-class ")) {
                    selectClass(command);
                } else if (command.startsWith("show-class")) {
                    showClass();
                } else if (command.startsWith("show-categories")) {
                    showCategories();
                } else if (command.startsWith("add-category ")) {
                    addCategory(command);
                } else if (command.startsWith("show-assignments")) {
                    showAssignments();
                } else if (command.startsWith("add-assignment ")) {
                    addAssignment(command);
                } else if (command.startsWith("show-students")) {
                    showStudents(command);
                } else if (command.startsWith("add-student ")) {
                    addStudent(command);
                } else if (command.startsWith("grade ")) {
                    grade(command);
                } else if (command.startsWith("student-grades ")) {
                    studentGrades(command);
                } else if (command.startsWith("gradebook")) {
                    gradebook();
                } else {
                    System.out.println("Unknown command.");
                }
            }
            connection.close();
            
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    


    private static void setupDatabase() {
        try {
            // Read SQL statements from the file
            String sql = new String(Files.readAllBytes(Paths.get("part2Schema.sql")));
    
            // Split the SQL statements by semicolon
            String[] statements = sql.split(";");
    
            // Execute each statement
            Statement statement = connection.createStatement();
            for (String s : statements) {
                if (!s.trim().isEmpty()) {
                    statement.executeUpdate(s);
                }
            }
            System.out.println("Database schema created successfully.");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void loadSampleData() {
        try {
            // Read SQL statements from the file
            String sql = new String(Files.readAllBytes(Paths.get("testData.sql")));
    
            // Split the SQL statements by semicolon
            String[] statements = sql.split(";");
    
            // Execute each statement
            Statement statement = connection.createStatement();
            for (String s : statements) {
                if (!s.trim().isEmpty()) {
                    statement.executeUpdate(s);
                }
            }
            System.out.println("\nSample data loaded successfully.");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    

    private static void createNewClass(String command) {
        String[] parts = command.split(" ");
        if (parts.length < 5) {
            System.out.println("Invalid command. Usage: new-class <course_number> <term> <section_number> <description>");
            return;
        }
    
        String courseNumber = parts[1];
        String term = parts[2];
        String sectionNumber = parts[3];
        String description = String.join(" ", Arrays.copyOfRange(parts, 4, parts.length));
        
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Class (course_number, term, section_number, description) VALUES (?, ?, ?, ?)");
            preparedStatement.setString(1, courseNumber);
            preparedStatement.setString(2, term);
            preparedStatement.setString(3, sectionNumber);
            preparedStatement.setString(4, description);
    
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " rows affected.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    private static void listClasses() {
        try {
            String query = "SELECT c.id, c.course_number, c.term, c.section_number, c.description, COUNT(e.student_id) AS num_students "
                         + "FROM Class c "
                         + "LEFT JOIN Enrollment e ON c.id = e.class_id "
                         + "GROUP BY c.id";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
    
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String courseNumber = resultSet.getString("course_number");
                String term = resultSet.getString("term");
                int sectionNumber = resultSet.getInt("section_number");
                String description = resultSet.getString("description");
                int numStudents = resultSet.getInt("num_students");
                System.out.printf("%d. %s - %s (Section %d) [%d Students]\n\t%s\n", id, courseNumber, term, sectionNumber, numStudents, description);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    

    private static void selectClass(String command) {
        try {
            String[] tokens = command.split(" ");
            if (tokens.length == 2) {
                String courseNumber = tokens[1];
                String query = "SELECT * FROM Class WHERE course_number='" + courseNumber + "' ORDER BY term DESC LIMIT 1";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    int sectionNumber = resultSet.getInt("section_number");
                    activeClass = courseNumber + " " + resultSet.getString("term") + " " + sectionNumber;
                    activeClassId = resultSet.getInt("id"); // add this line
                    System.out.println("\nActivated class -> " + activeClass);
                } else {
                    System.out.println("No class found with course number " + courseNumber);
                }
            } else if (tokens.length == 3) {
                String courseNumber = tokens[1];
                String term = tokens[2];
                String query = "SELECT * FROM Class WHERE course_number='" + courseNumber + "' AND term='" + term + "' ORDER BY section_number LIMIT 1";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    int sectionNumber = resultSet.getInt("section_number");
                    activeClass = courseNumber + " " + term + " " + sectionNumber;
                    activeClassId = resultSet.getInt("id"); // add this line
                    System.out.println("\nActivated class -> " + activeClass);
                } else {
                    System.out.println("No class found with course number " + courseNumber + " and term " + term);
                }
            } else if (tokens.length == 4) {
                String courseNumber = tokens[1];
                String term = tokens[2];
                int sectionNumber = Integer.parseInt(tokens[3]);
                String query = "SELECT * FROM Class WHERE course_number='" + courseNumber + "' AND term='" + term + "' AND section_number=" + sectionNumber;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    activeClass = courseNumber + " " + term + " " + sectionNumber;
                    activeClassId = resultSet.getInt("id"); // add this line
                    System.out.println("\nActivated class -> " + activeClass);
                } else {
                    System.out.println("No class found with course number " + courseNumber + ", term " + term + ", and section number " + sectionNumber);
                }
            } else {
                System.out.println("Invalid command");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    

    private static void showClass() {
        if (activeClass != null) {
            System.out.println("Currently active class:");
            System.out.println(activeClass.toString());
        } else {
            System.out.println("No class has been selected.");
        }
    }


    // field to store the currently-active class
// 

    private static void showCategories() {
        try {
            System.out.println("The selected class ID is -> " + activeClassId);
            String query = "SELECT * FROM Category WHERE class_id=" + activeClassId;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            System.out.println("Categories:");
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                double weight = resultSet.getDouble("weight");
                System.out.println(name + " (" + weight + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addCategory(String command) {
        String[] tokens = command.split(" ");
        if (tokens.length != 3) {
            System.out.println("Invalid command");
            return;
        }

        String name = tokens[1];
        double weight;
        try {
            weight = Double.parseDouble(tokens[2]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid weight");
            return;
        }

        try {
            String query = "INSERT INTO Category (class_id, name, weight) VALUES (" + activeClassId + ", '" + name + "', " + weight + ")";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            System.out.println("Category added successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showAssignments() {
        try {
            String query = "SELECT c.name AS category_name, a.name, a.description, a.points FROM Assignment a JOIN Category c ON a.category_id=c.id WHERE c.class_id=" + activeClassId;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            System.out.println("Assignments:");
            String currentCategory = "";
            while (resultSet.next()) {
                String category = resultSet.getString("category_name");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                int points = resultSet.getInt("points");

                if (!category.equals(currentCategory)) {
                    System.out.println("Category: " + category);
                    currentCategory = category;
                }

                System.out.println("\t" + name + " (" + points + " pts)");
                if (!description.isEmpty()) {
                    System.out.println("\t\t" + description);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addAssignment(String command) {
        String[] tokens = command.trim().split("\\s*,\\s*");
        if (tokens.length != 4) {
            System.out.println("Invalid command");
            return;
        }

        String name = tokens[0].trim().substring("add-assignment".length()).trim();
        String categoryName = tokens[1].trim();
        String description = tokens[2].trim();
        int points;
        try {
            points = Integer.parseInt(tokens[3].trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid point value");
            return;
        }

        try {
            // get the category id
            String categoryIdQuery = "SELECT id FROM Category WHERE name='" + categoryName + "' AND class_id=" + activeClassId;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(categoryIdQuery);
            if (!resultSet.next()) {
                System.out.println("Category not found");
                return;
            }
            int categoryId = resultSet.getInt("id");

            // insert the new assignment
            String query = "INSERT INTO Assignment (class_id, category_id, name, description, points) VALUES (" + activeClassId + ", " + categoryId + ", '" + name + "', '" + description + "', " + points + ")";
            statement.executeUpdate(query);
            System.out.println("Assignment added successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showStudents(String command) {
        try {
            System.out.println("The selected class ID is -> " + activeClassId + "\n\n");
            // System.out.println("Command is -> " + command);
    
            String searchQuery;
            if (command == null || command.trim().isEmpty() || command.trim().equalsIgnoreCase("show-students")) {
                System.out.println("Showing all students");
                searchQuery = "";
            } else {
                
                String[] commandParts = command.trim().split("\\s+");
                System.out.println("searching for -> " + commandParts[1]);
                searchQuery = commandParts.length > 1 ? commandParts[1].toLowerCase() : "";
            }
    
            String query = "SELECT * FROM Student JOIN Enrollment ON Student.id = Enrollment.student_id WHERE Enrollment.class_id = " + activeClassId + " AND (LOWER(username) LIKE '%" + searchQuery + "%' OR LOWER(first_name) LIKE '%" + searchQuery + "%' OR LOWER(last_name) LIKE '%" + searchQuery + "%')";
    
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            System.out.println("Students:");
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String studentId = resultSet.getString("student_id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                System.out.println(username + " (" + studentId + "): " + firstName + " " + lastName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    
    
    
    //TODO this works but it needs a check if a username does not exist in the DB to let the user know.
    private static void addStudent(String command) {
        String[] tokens = command.split(" ");
        if (tokens.length < 2 || tokens.length > 5) {
            System.out.println("Invalid command");
            return;
        }
    
        String username = tokens[1];
    
        try {
            // Check if student already exists
            String query = "SELECT * FROM Student WHERE username = '" + username + "'";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
    
            if (resultSet.next()) {
                int studentId = resultSet.getInt("id");
    
                // Check if student is already enrolled in the current class
                query = "SELECT * FROM Enrollment WHERE student_id = " + studentId + " AND class_id = " + activeClassId;
                resultSet = statement.executeQuery(query);
    
                if (resultSet.next()) {
                    System.out.println("Student is already enrolled in the current class.");
                } else {
                    // Enroll student in the current class
                    query = "INSERT INTO Enrollment (class_id, student_id) VALUES (" + activeClassId + ", " + studentId + ")";
                    statement.executeUpdate(query);
                    System.out.println("Student enrolled in the current class.");
                }
            } else {
                // Create new student and enroll in the current class
                String studentId = tokens.length >= 3 ? tokens[2] : "";
                String firstName = tokens.length >= 4 ? tokens[3] : "";
                String lastName = tokens.length == 5 ? tokens[4] : "";
                query = "INSERT INTO Student (username, student_id, first_name, last_name) VALUES ('" + username + "', '" + studentId + "', '" + firstName + "', '" + lastName + "')";
                statement.executeUpdate(query);
    
                // Get ID of the new student
                query = "SELECT * FROM Student WHERE username = '" + username + "'";
                resultSet = statement.executeQuery(query);
                resultSet.next();
                int studentIdInt = resultSet.getInt("id");
    
                // Enroll new student in the current class
                query = "INSERT INTO Enrollment (class_id, student_id) VALUES (" + activeClassId + ", " + studentIdInt + ")";
                statement.executeUpdate(query);
                System.out.println("New student added and enrolled in the current class.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void grade(String command) {
        String[] tokens = command.split(" ");
        if (tokens.length != 4) {
            System.out.println("Invalid command");
            return;
        }
    
        String assignmentName = tokens[1];
        String username = tokens[2];
        int grade = Integer.parseInt(tokens[3]);
    
        try {
            // Check if student and assignment exist
            String query = "SELECT * FROM StudentGrade, Assignment, Student " +
                    "WHERE StudentGrade.assignment_id = Assignment.id AND " +
                    "StudentGrade.student_id = Student.id AND " +
                    "Assignment.name = '" + assignmentName + "' AND " +
                    "Student.username = '" + username + "'";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
    
            if (resultSet.next()) {
                int assignmentId = resultSet.getInt("Assignment.id");
                int studentGradeId = resultSet.getInt("StudentGrade.id");
                int points = resultSet.getInt("Assignment.points");
    
                // Check if grade already exists for the student and assignment
                query = "SELECT * FROM StudentGrade WHERE id = " + studentGradeId;
                resultSet = statement.executeQuery(query);
                resultSet.next();
                int existingGrade = resultSet.getInt("grade");
    
                if (existingGrade == grade) {
                    System.out.println("Grade is already set to " + grade + ".");
                } else {
                    // Check if the number of points exceeds the number of points configured for the assignment
                    if (grade > points) {
                        System.out.println("Warning: number of points entered exceeds the number of points configured for the assignment (" + points + ").");
                    }
    
                    // Update grade for the student and assignment
                    query = "UPDATE StudentGrade SET grade = " + grade + " WHERE id = " + studentGradeId;
                    statement.executeUpdate(query);
    
                    System.out.println("Grade updated for " + username + " for assignment " + assignmentName + ".");
                }
            } else {
                System.out.println("Error: Student or assignment not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //TODO this works but it needs a check if a username does not exist in the DB to let the user know.
    private static void studentGrades(String command) {
        String[] tokens = command.split(" ");
        if (tokens.length != 2) {
            System.out.println("Invalid command");
            return;
        }
        String username = tokens[1];
        try {
            // Get all the assignments for the active class
            String query = "SELECT * FROM Assignment WHERE class_id = " + activeClassId + " ORDER BY category_id";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            
    
            double totalGrade = 0;
            int totalPoints = 0;
    
            // Group the assignments by category
            int currentCategoryId = -1;
            double categoryGrade = 0;
            int categoryPoints = 0;
    
            while (resultSet.next()) {
                int assignmentId = resultSet.getInt("id");
                int categoryId = resultSet.getInt("category_id");
                String assignmentName = resultSet.getString("name");
                int points = resultSet.getInt("points");
    
                // Get the grade for the assignment and student
                query = "SELECT grade FROM StudentGrade WHERE student_id = (SELECT id FROM Student WHERE username = '" + username + "') AND assignment_id = " + assignmentId;
                Statement gradeStatement = connection.createStatement();
                ResultSet gradeResultSet = gradeStatement.executeQuery(query);
    
                if (gradeResultSet.next()) {
                    int grade = gradeResultSet.getInt("grade");
    
                    // Calculate the grade for the assignment
                    double gradePercentage = ((double) grade) / points;
                    double assignmentGrade = gradePercentage * 100;
    
                    // Print the assignment grade
                    System.out.println(assignmentName + ": " + grade + " / " + points + " (" + assignmentGrade + "%)");
    
                    // Update the total grade and points
                    totalGrade += gradePercentage * resultSet.getInt("points");
                    totalPoints += resultSet.getInt("points");
    
                    // Update the category grade and points
                    if (currentCategoryId == -1) {
                        currentCategoryId = categoryId;
                    } else if (categoryId != currentCategoryId) {
                        double categoryGradePercentage = ((double) categoryGrade) / categoryPoints;
                        double categoryGradePercentageRounded = Math.round(categoryGradePercentage * 10000.0) / 100.0;
    
                        System.out.println("Category subtotal: " + categoryGrade + " / " + categoryPoints + " (" + categoryGradePercentageRounded + "%)\n");
    
                        currentCategoryId = categoryId;
                        categoryGrade = 0;
                        categoryPoints = 0;
                    }
    
                    categoryGrade += grade;
                    categoryPoints += points;
                } else {
                    // Print that the assignment has not been graded yet
                    System.out.println(assignmentName + ": - / " + points + " (-%)");
                }
    
                // Close the gradeResultSet and gradeStatement
                gradeResultSet.close();
                gradeStatement.close();
            }
    
            // Print the final category subtotal
            double categoryGradePercentage = ((double) categoryGrade) / categoryPoints;
            double categoryGradePercentageRounded = Math.round(categoryGradePercentage * 10000.0) / 100.0;
            System.out.println("Category subtotal: " + categoryGrade + " / " + categoryPoints + " (" + categoryGradePercentageRounded + "%)\n");
    
            // Print the final total grade
            double finalGradePercentage = totalPoints == 0 ? 0 : totalGrade / totalPoints;
            double finalGradePercentageRounded = Math.round(finalGradePercentage * 10000.0) / 100.0;
            System.out.println("Total grade: " + totalGrade + " / " + totalPoints + " (" + finalGradePercentageRounded + "%)");
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void gradebook() {
        try {
            // Get all the students in the active class
            String query = "SELECT * FROM Student WHERE id IN (SELECT student_id FROM Enrollment WHERE class_id = " + activeClassId + ")";

            Statement statement = connection.createStatement();
            ResultSet studentResultSet = statement.executeQuery(query);
    
            while (studentResultSet.next()) {
                int studentId = studentResultSet.getInt("id");
                String username = studentResultSet.getString("username");
                String firstName = studentResultSet.getString("first_name");
                String lastName = studentResultSet.getString("last_name");
    
                // Get the student's total grade in the class
                Statement innerStatement = connection.createStatement();
                String innerQuery = "SELECT SUM(grade) AS total_grade FROM StudentGrade WHERE student_id = " + studentId;
                ResultSet gradeResultSet = innerStatement.executeQuery(innerQuery);
    
                double totalGrade = 0;
                if (gradeResultSet.next()) {
                    totalGrade = gradeResultSet.getDouble("total_grade");
                }
    
                // Print the student's information and total grade
                System.out.println("Username: " + username);
                System.out.println("Student ID: " + studentId);
                System.out.println("Name: " + firstName + " " + lastName);
                System.out.println("Total Grade: " + totalGrade + "\n");
    
                // Close the inner statement and result set
                gradeResultSet.close();
                innerStatement.close();
            }
    
            // Close the outer statement and result set
            studentResultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    

    
    
    
    
    
    

}
