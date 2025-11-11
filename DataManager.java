import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataManager {

    private static final String DB_URL = "jdbc:sqlite:nptel_tracker.db";

    public DataManager() {
        // When DataManager is created, it initializes the database.
        initializeDatabase();
    }

    /**
     * Creates a connection to the SQLite database.
     * @return a Connection object
     */
    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Creates the STUDENTS and ATTEMPTS tables if they don't exist.
     */
    private void initializeDatabase() {
        // SQL for creating the students table
        String sqlStudents = "CREATE TABLE IF NOT EXISTS STUDENTS ("
                + " id TEXT PRIMARY KEY NOT NULL,"
                + " name TEXT NOT NULL,"
                + " email TEXT,"
                + " batch TEXT,"
                + " department TEXT,"
                + " currentSemester TEXT"
                + ");";
        
        // SQL for creating the attempts table
        String sqlAttempts = "CREATE TABLE IF NOT EXISTS ATTEMPTS ("
                + " id TEXT PRIMARY KEY NOT NULL,"
                + " studentId TEXT NOT NULL,"
                + " nptelId TEXT,"
                + " courseName TEXT,"
                + " examDate TEXT," // Store as TEXT in YYYY-MM-DD format
                + " score INTEGER,"
                + " semester TEXT,"
                + " status TEXT,"
                + " FOREIGN KEY (studentId) REFERENCES STUDENTS (id) ON DELETE CASCADE"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            // Execute both table creation statements
            stmt.execute(sqlStudents);
            stmt.execute(sqlAttempts);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // --- Student Methods (Now with SQL) ---

    public List<Student> getStudents() {
        String sql = "SELECT * FROM STUDENTS";
        List<Student> students = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student student = new Student(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("batch"),
                        rs.getString("department"),
                        rs.getString("currentSemester")
                );
                students.add(student);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return students;
    }

    public Optional<Student> findStudentById(String id) {
        String sql = "SELECT * FROM STUDENTS WHERE id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Student student = new Student(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("batch"),
                        rs.getString("department"),
                        rs.getString("currentSemester")
                );
                return Optional.of(student);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    public boolean addStudent(Student student) {
        if (findStudentById(student.getId()).isPresent()) {
            return false; // Student ID already exists
        }

        String sql = "INSERT INTO STUDENTS(id, name, email, batch, department, currentSemester) VALUES(?,?,?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getId());
            pstmt.setString(2, student.getName());
            pstmt.setString(3, student.getEmail());
            pstmt.setString(4, student.getBatch());
            pstmt.setString(5, student.getDepartment());
            pstmt.setString(6, student.getCurrentSemester());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public void deleteStudent(String studentId) {
        // Because of "ON DELETE CASCADE" in the table definition,
        // deleting a student will automatically delete their attempts.
        String sql = "DELETE FROM STUDENTS WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // --- Attempt Methods (Now with SQL) ---

    public List<Attempt> getAttempts() {
        String sql = "SELECT * FROM ATTEMPTS";
        List<Attempt> attempts = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // --- CORRECTION ---
                // Use the new constructor to load the *actual* DB id
                Attempt attempt = new Attempt(
                        rs.getString("id"), // Pass the ID from the database
                        rs.getString("studentId"),
                        rs.getString("nptelId"),
                        rs.getString("courseName"),
                        LocalDate.parse(rs.getString("examDate")), // Convert text back to LocalDate
                        rs.getInt("score"),
                        rs.getString("semester"),
                        rs.getString("status")
                );
                attempts.add(attempt);
                // ------------------
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return attempts;
    }

    public void addAttempt(Attempt attempt) {
        String sql = "INSERT INTO ATTEMPTS(id, studentId, nptelId, courseName, examDate, score, semester, status) VALUES(?,?,?,?,?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, attempt.getId()); // Use the ID from the Attempt object
            pstmt.setString(2, attempt.getStudentId());
            pstmt.setString(3, attempt.getNptelId());
            pstmt.setString(4, attempt.getCourseName());
            pstmt.setString(5, attempt.getExamDate().toString()); // Store LocalDate as TEXT
            pstmt.setInt(6, attempt.getScore());
            pstmt.setString(7, attempt.getSemester());
            pstmt.setString(8, attempt.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteAttempt(String attemptId) {
        String sql = "DELETE FROM ATTEMPTS WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, attemptId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}