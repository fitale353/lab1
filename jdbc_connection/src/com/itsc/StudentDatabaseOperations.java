package com.itsc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC Lab - Practice Tasks 1 to 6
 *
 * Task 1: Connect to MySQL, create the StudentsDB database and the students table
 * Task 2: Insert one row, then ten more rows
 * Task 3: Retrieve and display five rows
 * Task 4: Update the firstname of a student with a given id
 * Task 5: Delete a student with a given id
 * Task 6: Calculate and display the average grade
 */
public class StudentDatabaseOperations {

    // ---- Change these to match YOUR MySQL installation ----
    private static final String DB_NAME    = "StudentsDB";
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";            // the server, no database
    private static final String DB_URL     = "jdbc:mysql://localhost:3306/StudentsDB";  // the StudentsDB database
    private static final String USERNAME   = "root";
    private static final String PASSWORD   = "Fit12$!*#&";   // your MySQL root password

    public static void main(String[] args) {
        try {
            // STEP 1: Register (load) the driver class.
            // Optional since JDBC 4.0, but the lab asks for it, so we keep it.
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded.");

            // ===== TASK 1a: create the database =====
            // We connect to the SERVER first (no database in the URL),
            // because StudentsDB does not exist yet.
            createDatabase();

            // From here on we connect to StudentsDB itself.
            // try-with-resources closes the connection automatically at the end.
            try (Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
                System.out.println("Established connection to " + DB_NAME + ".\n");

                // ===== TASK 1b: create the table =====
                createTable(connection);

                // ===== TASK 2: insert data =====
                insertSingleStudent(connection);
                insertTenStudents(connection);

                // ===== TASK 3: retrieve five rows =====
                System.out.println("\n--- Task 3: First five students ---");
                retrieveData(connection, 5);

                // ===== TASK 4: update a student's firstname =====
                System.out.println("\n--- Task 4: Update ---");
                updateStudentName(connection, 1, "Abebe");

                // ===== TASK 5: delete a student =====
                System.out.println("\n--- Task 5: Delete ---");
                deleteStudent(connection, 2);

                // Show the whole table so you can see the effect of Tasks 4 and 5
                System.out.println("\n--- Table after update and delete ---");
                retrieveData(connection, 100);

                // ===== TASK 6: average grade =====
                System.out.println("\n--- Task 6: Average grade ---");
                calculateAverageGrade(connection);
            }
            System.out.println("\nConnection closed. All tasks finished.");

        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found. Add the MySQL Connector/J JAR to the Classpath (see guide).");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ---------------- TASK 1 ----------------

    private static void createDatabase() throws SQLException {
        try (Connection serverConn = DriverManager.getConnection(SERVER_URL, USERNAME, PASSWORD);
             Statement statement = serverConn.createStatement()) {

            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("Database '" + DB_NAME + "' is ready.");
        }
    }
    private static void createTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Drop the old table first so the program can be run again and again
            // without "Duplicate entry" errors on the primary key.
            statement.executeUpdate("DROP TABLE IF EXISTS students");

            String createTableSQL = "CREATE TABLE students ("
                    + "id INT PRIMARY KEY, "
                    + "firstname VARCHAR(255), "
                    + "lastname VARCHAR(255), "
                    + "grade INT)";
            statement.executeUpdate(createTableSQL);
            System.out.println("Table 'students' created.");
        }
    }

    // ---------------- TASK 2 ----------------

    private static final String INSERT_SQL =
            "INSERT INTO students (id, firstname, lastname, grade) VALUES (?, ?, ?, ?)";

    private static void insertSingleStudent(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {
            ps.setInt(1, 1);            // id
            ps.setString(2, "John");    // firstname
            ps.setString(3, "Doe");     // lastname
            ps.setInt(4, 90);           // grade
            ps.executeUpdate();
            System.out.println("Inserted 1 student (John Doe).");
        }
    }

    private static void insertTenStudents(Connection connection) throws SQLException {
        int[]    ids        = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        String[] firstNames = {"Aster", "Jemal", "Haile", "Teddy", "Teklay",
                "Memar", "Selam", "Dawit", "Hana", "Yonas"};
        String[] lastNames  = {"Nega", "Edris", "Anaol", "Habtu", "Gebre",
                "Alebachew", "Tesfaye", "Bekele", "Girma", "Kebede"};
        int[]    grades     = {85, 72, 64, 91, 58, 77, 88, 69, 95, 81};

        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {
            for (int i = 0; i < ids.length; i++) {
                ps.setInt(1, ids[i]);
                ps.setString(2, firstNames[i]);
                ps.setString(3, lastNames[i]);
                ps.setInt(4, grades[i]);
                ps.executeUpdate();     // same prepared statement, new values each time
            }
            System.out.println("Inserted " + ids.length + " more students.");
        }
    }

    // ---------------- TASK 3 ----------------

    private static void retrieveData(Connection connection, int limit) throws SQLException {
        String sql = "SELECT id, firstname, lastname, grade FROM students ORDER BY id LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-4s %-12s %-12s %s%n", "ID", "First name", "Last name", "Grade");
                while (rs.next()) {                       // moves to the next row; false when no rows left
                    int id           = rs.getInt("id");
                    String firstname = rs.getString("firstname");
                    String lastname  = rs.getString("lastname");
                    int grade        = rs.getInt("grade");
                    System.out.printf("%-4d %-12s %-12s %d%n", id, firstname, lastname, grade);
                }
            }
        }
    }

    // ---------------- TASK 4 ----------------

    private static void updateStudentName(Connection connection, int id, String newFirstName) throws SQLException {
        String sql = "UPDATE students SET firstname = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newFirstName);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();                // returns how many rows changed
            System.out.println("Updated firstname of student " + id + " to '" + newFirstName
                    + "' (" + rows + " row affected).");
        }
    }

// ---------------- TASK 5 ----------------
private static void deleteStudent(Connection connection, int id) throws SQLException {
    String sql = "DELETE FROM students WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setInt(1, id);
        int rows = ps.executeUpdate();
        System.out.println("Deleted student " + id + " (" + rows + " row affected).");
    }
}

    // ---------------- TASK 6 ----------------

    private static void calculateAverageGrade(Connection connection) throws SQLException {
        String sql = "SELECT AVG(grade) AS average_grade FROM students";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {                              // AVG returns exactly one row
                double average = rs.getDouble("average_grade");
                System.out.printf("Average grade: %.2f%n", average);
            }
        }
    }
}