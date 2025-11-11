import java.io.Serializable;
import java.time.LocalDate;

public class Attempt implements Serializable {
    private String id;
    private String studentId;
    private String nptelId;
    private String courseName;
    private LocalDate examDate;
    private int score;
    private String semester;
    private String status;

    // Original constructor (for NEW attempts)
    public Attempt(String studentId, String nptelId, String courseName, LocalDate examDate, int score, String semester, String status) {
        this.id = String.valueOf(System.currentTimeMillis()); // Unique ID
        this.studentId = studentId;
        this.nptelId = nptelId;
        this.courseName = courseName;
        this.examDate = examDate;
        this.score = score;
        this.semester = semester;
        this.status = status;
    }

    // New constructor (for loading EXISTING attempts from DB)
    public Attempt(String id, String studentId, String nptelId, String courseName, LocalDate examDate, int score, String semester, String status) {
        this.id = id; // Use the ID from the database
        this.studentId = studentId;
        this.nptelId = nptelId;
        this.courseName = courseName;
        this.examDate = examDate;
        this.score = score;
        this.semester = semester;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getNptelId() { return nptelId; }
    public String getCourseName() { return courseName; }
    public LocalDate getExamDate() { return examDate; }
    public int getScore() { return score; }
    public String getSemester() { return semester; }
    public String getStatus() { return status; }
}