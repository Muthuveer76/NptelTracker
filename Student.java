import java.io.Serializable;

public class Student implements Serializable {
    private String id;
    private String name;
    private String email;
    private String batch;
    private String department;
    private String currentSemester;

    public Student(String id, String name, String email, String batch, String department, String currentSemester) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.batch = batch;
        this.department = department;
        this.currentSemester = currentSemester;
    }

    // Getters and Setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getCurrentSemester() { return currentSemester; }
    public void setCurrentSemester(String currentSemester) { this.currentSemester = currentSemester; }

    @Override
    public String toString() {
        return id + " - " + name; // Used for JComboBox display
    }
}