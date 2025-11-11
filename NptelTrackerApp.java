import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class NptelTrackerApp extends JFrame {

    // Main Colors from CSS
    private static final Color PRIMARY_COLOR = new Color(102, 126, 234);
    private static final Color DARK_TEXT_COLOR = new Color(44, 62, 80);
    private static final Color LIGHT_BG_COLOR = new Color(255, 255, 255, 242);
    private static final Color HEADER_COLOR = new Color(102, 126, 234);

    // Data Manager
    private final DataManager dataManager = new DataManager();

    // UI Components
    private JTable studentsTable, attemptsTable, filteredTable, batchSummaryTable, semesterSummaryTable;
    private DefaultTableModel studentsModel, attemptsModel, filteredModel, batchSummaryModel, semesterSummaryModel;
    private JComboBox<Student> attemptStudentIdCombo;
    private JComboBox<String> filterSemesterCombo, filterBatchCombo, filterStatusCombo;
    private JTextField studentIdField, studentNameField, emailField, departmentField;
    private JComboBox<String> batchCombo, currentSemesterCombo;
    private JTextField nptelIdField, courseNameField, scoreField, examDateField;
    private JComboBox<String> semesterCombo, statusCombo;
    private JTextField searchNptelField;
    private JLabel totalStudentsLabel, totalExamAttemptsLabel, overallPassRateLabel, totalEliteLabel;
    private JLabel totalAttemptsFilteredLabel, passRateFilteredLabel, avgScoreFilteredLabel, eliteCountFilteredLabel;

    public NptelTrackerApp() {
        super("🎓 NPTEL Exam Student Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null); // Center the frame
        
        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(230, 230, 250)); 
        
        // Header
        JLabel headerLabel = new JLabel("NPTEL Exam Student Tracker", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(DARK_TEXT_COLOR);
        headerLabel.setBorder(new EmptyBorder(15, 0, 15, 0));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabbedPane.addTab("Student Profile", createStudentProfilePanel());
        tabbedPane.addTab("Attempt Logger", createAttemptLoggerPanel());
        tabbedPane.addTab("Semester Filter", createFilterPanel());
        tabbedPane.addTab("Reports & Export", createReportsPanel());
        tabbedPane.addTab("Java Concepts", createConceptsPanel());

        // Update reports tab when selected
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 3) { // Reports Tab
                updateReportStats();
                generateBatchSummary();
                generateSemesterSummary();
            } else if (tabbedPane.getSelectedIndex() == 2) { // Filter Tab
                 applyFilters();
            }
        });


        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Load all existing data from the database into the UI on startup
        refreshStudentsTable();
        refreshAttemptsTable();
        updateStudentComboBox();
    
        
        setContentPane(mainPanel);
    }
    
    // Panel Creation Methods
    private JPanel createStudentProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(LIGHT_BG_COLOR);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fields
        studentIdField = new JTextField(15);
        studentNameField = new JTextField(15);
        emailField = new JTextField(15);
        departmentField = new JTextField(15);
        batchCombo = new JComboBox<>(new String[]{"2021", "2022", "2023", "2024","2025"});
        currentSemesterCombo = new JComboBox<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1; formPanel.add(studentIdField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 3; formPanel.add(studentNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; formPanel.add(emailField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Batch Year:"), gbc);
        gbc.gridx = 3; formPanel.add(batchCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1; formPanel.add(departmentField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Current Semester:"), gbc);
        gbc.gridx = 3; formPanel.add(currentSemesterCombo, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = createStyledButton("Add Student", PRIMARY_COLOR);
        JButton clearButton = createStyledButton("Clear", Color.GRAY);
        JButton deleteButton = createStyledButton("Delete Selected", Color.RED);
        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(deleteButton);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4; formPanel.add(buttonPanel, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Table
        String[] studentColumns = {"Student ID", "Name", "Email", "Batch", "Department", "Semester"};
        studentsModel = new DefaultTableModel(studentColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        studentsTable = new JTable(studentsModel);
        styleTable(studentsTable);
        panel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);

        // Action Listeners
        addButton.addActionListener(this::addStudentAction);
        clearButton.addActionListener(e -> clearStudentForm());
        deleteButton.addActionListener(this::deleteStudentAction);

        return panel;
    }

    private JPanel createAttemptLoggerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(LIGHT_BG_COLOR);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fields
        attemptStudentIdCombo = new JComboBox<>();
        nptelIdField = new JTextField(15);
        courseNameField = new JTextField(15);
        examDateField = new JTextField(15);
        examDateField.setText("YYYY-MM-DD");
        scoreField = new JTextField(15);
        semesterCombo = new JComboBox<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
        statusCombo = new JComboBox<>(new String[]{"Pass", "Fail", "Elite", "Elite + Gold"});

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Select Student:"), gbc);
        gbc.gridx = 1; gbc.gridwidth=3; formPanel.add(attemptStudentIdCombo, gbc);
        gbc.gridwidth=1;
        
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("NPTEL Course ID:"), gbc);
        gbc.gridx = 1; formPanel.add(nptelIdField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Course Name:"), gbc);
        gbc.gridx = 3; formPanel.add(courseNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Exam Date:"), gbc);
        gbc.gridx = 1; formPanel.add(examDateField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Score (%):"), gbc);
        gbc.gridx = 3; formPanel.add(scoreField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1; formPanel.add(semesterCombo, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3; formPanel.add(statusCombo, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton logButton = createStyledButton("Log Attempt", PRIMARY_COLOR);
        JButton clearButton = createStyledButton("Clear", Color.GRAY);
        JButton deleteButton = createStyledButton("Delete Selected", Color.RED);
        buttonPanel.add(logButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(deleteButton);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4; formPanel.add(buttonPanel, gbc);

        panel.add(formPanel, BorderLayout.NORTH);
        
        // Table
        String[] attemptColumns = {"Student ID", "NPTEL ID", "Course", "Date", "Score", "Semester", "Status"};
        attemptsModel = new DefaultTableModel(attemptColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        attemptsTable = new JTable(attemptsModel);
        styleTable(attemptsTable);
        panel.add(new JScrollPane(attemptsTable), BorderLayout.CENTER);

        // Action Listeners
        logButton.addActionListener(this::addAttemptAction);
        clearButton.addActionListener(e -> clearAttemptForm());
        deleteButton.addActionListener(this::deleteAttemptAction);
        
        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(LIGHT_BG_COLOR);
        
        // Filter controls
        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterSemesterCombo = new JComboBox<>(new String[]{"All", "1", "2", "3", "4", "5", "6", "7", "8"});
        filterBatchCombo = new JComboBox<>(new String[]{"All", "2021", "2022", "2023", "2024"});
        filterStatusCombo = new JComboBox<>(new String[]{"All", "Pass", "Fail", "Elite", "Elite + Gold"});
        searchNptelField = new JTextField(15);
        JButton applyButton = createStyledButton("Apply Filters", PRIMARY_COLOR);
        JButton clearButton = createStyledButton("Clear Filters", Color.GRAY);
        
        filterControls.add(new JLabel("Semester:"));
        filterControls.add(filterSemesterCombo);
        filterControls.add(new JLabel("Batch:"));
        filterControls.add(filterBatchCombo);
        filterControls.add(new JLabel("Status:"));
        filterControls.add(filterStatusCombo);
        filterControls.add(new JLabel("NPTEL ID:"));
        filterControls.add(searchNptelField);
        filterControls.add(applyButton);
        filterControls.add(clearButton);
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBorder(new EmptyBorder(10,0,10,0));
        totalAttemptsFilteredLabel = createStatLabel("0", "Total Attempts");
        passRateFilteredLabel = createStatLabel("0%", "Pass Rate");
        avgScoreFilteredLabel = createStatLabel("0", "Average Score");
        eliteCountFilteredLabel = createStatLabel("0", "Elite Certificates");
        statsPanel.add(totalAttemptsFilteredLabel.getParent());
        statsPanel.add(passRateFilteredLabel.getParent());
        statsPanel.add(avgScoreFilteredLabel.getParent());
        statsPanel.add(eliteCountFilteredLabel.getParent());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterControls, BorderLayout.NORTH);
        topPanel.add(statsPanel, BorderLayout.CENTER);
        panel.add(topPanel, BorderLayout.NORTH);

        // Table
        String[] filteredColumns = {"Student ID", "Student Name", "NPTEL ID", "Course", "Date", "Score", "Semester", "Status", "Batch"};
        filteredModel = new DefaultTableModel(filteredColumns, 0){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        filteredTable = new JTable(filteredModel);
        styleTable(filteredTable);
        panel.add(new JScrollPane(filteredTable), BorderLayout.CENTER);
        
        // Listeners
        applyButton.addActionListener(e -> applyFilters());
        clearButton.addActionListener(e -> clearFilters());

        return panel;
    }
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(LIGHT_BG_COLOR);

        // Top Panel: Stats + Export Buttons
        JPanel topPanel = new JPanel(new BorderLayout(10,10));
        
        // Stats
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        totalStudentsLabel = createStatLabel("0", "Total Students");
        totalExamAttemptsLabel = createStatLabel("0", "Total Exam Attempts");
        overallPassRateLabel = createStatLabel("0%", "Overall Pass Rate");
        totalEliteLabel = createStatLabel("0", "Elite Certificates");
        statsPanel.add(totalStudentsLabel.getParent());
        statsPanel.add(totalExamAttemptsLabel.getParent());
        statsPanel.add(overallPassRateLabel.getParent());
        statsPanel.add(totalEliteLabel.getParent());
        topPanel.add(statsPanel, BorderLayout.NORTH);

        // Export Buttons
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton exportStudentsBtn = createStyledButton("📥 Export Students CSV", new Color(40, 167, 69));
        JButton exportAttemptsBtn = createStyledButton("📥 Export Attempts CSV", new Color(40, 167, 69));
        exportPanel.add(exportStudentsBtn);
        exportPanel.add(exportAttemptsBtn);
        topPanel.add(exportPanel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        
        // Summary Tables
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // Batch Summary
        JPanel batchPanel = new JPanel(new BorderLayout());
        batchPanel.setBorder(BorderFactory.createTitledBorder("Batch-wise Summary"));
        String[] batchColumns = {"Batch", "Students", "Attempts", "Passed", "Elite", "Pass Rate"};
        batchSummaryModel = new DefaultTableModel(batchColumns, 0){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        batchSummaryTable = new JTable(batchSummaryModel);
        styleTable(batchSummaryTable);
        batchPanel.add(new JScrollPane(batchSummaryTable), BorderLayout.CENTER);
        splitPane.setTopComponent(batchPanel);

        // Semester Summary
        JPanel semesterPanel = new JPanel(new BorderLayout());
        semesterPanel.setBorder(BorderFactory.createTitledBorder("Semester-wise Performance"));
        String[] semesterColumns = {"Semester", "Attempts", "Passed", "Elite", "Pass Rate", "Avg Score"};
        semesterSummaryModel = new DefaultTableModel(semesterColumns, 0){
             @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        semesterSummaryTable = new JTable(semesterSummaryModel);
        styleTable(semesterSummaryTable);
        semesterPanel.add(new JScrollPane(semesterSummaryTable), BorderLayout.CENTER);
        splitPane.setBottomComponent(semesterPanel);
        
        panel.add(splitPane, BorderLayout.CENTER);

        // Action Listeners
        exportStudentsBtn.addActionListener(e -> exportToCSV(studentsTable, "students"));
        exportAttemptsBtn.addActionListener(e -> exportToCSV(attemptsTable, "attempts"));
        
        return panel;
    }

    private JPanel createConceptsPanel() {
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setBackground(LIGHT_BG_COLOR);
        
        String htmlContent = "<html>" +
            "<body style='font-family: Segoe UI, sans-serif; font-size: 12px; padding: 15px;'>" +
            "<h2>📅 Weekly Progress & Java Concepts Mapping</h2>" +
            "This project demonstrates the application of core Java principles learned through a typical semester." +
            "<hr>" +
            "<div style='background-color: #d4edda; border-left: 4px solid #28a745; padding: 10px; margin-bottom: 10px;'>" +
            "<h4>Week 1-2: Classes, Objects & Constructor Overloading</h4>" +
            "<p><strong>Concepts:</strong> Basic OOP principles, class definition, object creation, multiple constructors.</p>" +
            "<p><strong>Project Implementation:</strong> ✅ `Student` and `Attempt` classes serve as data models.</p>" +
            "</div>" +
            "<div style='background-color: #d4edda; border-left: 4px solid #28a745; padding: 10px; margin-bottom: 10px;'>" +
            "<h4>Week 3-4: Arrays, ArrayList & Swing Components</h4>" +
            "<p><strong>Concepts:</strong> Dynamic collections, GUI basics, event handling.</p>" +
            "<p><strong>Project Implementation:</strong> ✅ `DataManager` uses `ArrayList`s to store records. Swing (`JFrame`, `JPanel`, `JTable`) is used for the UI.</p>" +
            "</div>" +
            "<div style='background-color: #d4edda; border-left: 4px solid #28a745; padding: 10px; margin-bottom: 10px;'>" +
            "<h4>Week 5-6: Collections & Search Logic</h4>" +
            "<p><strong>Concepts:</strong> Advanced collections, sorting, filtering, lambda expressions, Streams API.</p>" +
            "<p><strong>Project Implementation:</strong> ✅ Multi-criteria filtering logic uses Java Streams and lambda functions for concise, powerful data manipulation.</p>" +
            "</div>" +
            "<div style='background-color: #d4edda; border-left: 4px solid #28a745; padding: 10px; margin-bottom: 10px;'>" +
            "<h4>Week 7-8: Report Generation & File I/O</h4>" +
            "<p><strong>Concepts:</strong> Data aggregation, statistical calculations, file formatting and writing.</p>" +
            "<p><strong>Project Implementation:</strong> ✅ Comprehensive reporting and CSV export system using `FileWriter`.</p>" +
            "</div>" +
            "<hr>" +
            "<h3>Learning Outcomes Achieved</h3>" +
            "<ul>" +
            "<li>✅ <strong>Object-Oriented Programming:</strong> Implemented classes for Student and Attempt entities.</li>" +
            "<li>✅ <strong>Data Structures:</strong> Used `ArrayList` to manage multiple records dynamically.</li>" +
            "<li>✅ <strong>GUI Development:</strong> Built a multi-tab desktop application using Java Swing.</li>" +
            "<li>✅ <strong>Event Handling:</strong> Used `ActionListener` for button clicks and other events.</li>" +
            "<li>✅ <strong>File Operations:</strong> Implemented data export to CSV files.</li>" +
            "<li>✅ <strong>Search & Filter:</strong> Created multi-criteria search using the Java Streams API.</li>" +
            "<li>✅ <strong>Statistical Analysis:</strong> Calculated pass rates, averages, and other metrics.</li>" +
            "</ul>" +
            "</body>" +
            "</html>";
        
        textPane.setText(htmlContent);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(textPane), BorderLayout.CENTER);
        return panel;
    }

    // Action Methods
    private void addStudentAction(ActionEvent e) {
        String id = studentIdField.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Student ID cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Student newStudent = new Student(
            id,
            studentNameField.getText().trim(),
            emailField.getText().trim(),
            (String) batchCombo.getSelectedItem(),
            departmentField.getText().trim(),
            (String) currentSemesterCombo.getSelectedItem()
        );
        
        if (dataManager.addStudent(newStudent)) {
            JOptionPane.showMessageDialog(this, "Student added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshStudentsTable();
            updateStudentComboBox();
            clearStudentForm();
        } else {
            JOptionPane.showMessageDialog(this, "Student ID already exists.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteStudentAction(ActionEvent e) {
        int selectedRow = studentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String studentId = (String) studentsModel.getValueAt(selectedRow, 0);
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this student and all their attempts?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dataManager.deleteStudent(studentId);
            refreshStudentsTable();
            refreshAttemptsTable();
            updateStudentComboBox();
            JOptionPane.showMessageDialog(this, "Student deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addAttemptAction(ActionEvent e) {
        Student selectedStudent = (Student) attemptStudentIdCombo.getSelectedItem();
        if (selectedStudent == null) {
            JOptionPane.showMessageDialog(this, "Please select a student.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LocalDate date = LocalDate.parse(examDateField.getText().trim());
            int score = Integer.parseInt(scoreField.getText().trim());
            
            Attempt newAttempt = new Attempt(
                selectedStudent.getId(),
                nptelIdField.getText().trim(),
                courseNameField.getText().trim(),
                date,
                score,
                (String) semesterCombo.getSelectedItem(),
                (String) statusCombo.getSelectedItem()
            );

            dataManager.addAttempt(newAttempt);
            JOptionPane.showMessageDialog(this, "Attempt logged successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshAttemptsTable();
            clearAttemptForm();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Score must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAttemptAction(ActionEvent e) {
        int selectedRow = attemptsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an attempt to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Get the Attempt object from the underlying list, using the view-to-model row index
        Attempt attemptToDelete = dataManager.getAttempts().get(attemptsTable.convertRowIndexToModel(selectedRow));
        
        dataManager.deleteAttempt(attemptToDelete.getId());
        refreshAttemptsTable();
        JOptionPane.showMessageDialog(this, "Attempt deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void applyFilters() {
        String semester = (String) filterSemesterCombo.getSelectedItem();
        String batch = (String) filterBatchCombo.getSelectedItem();
        String status = (String) filterStatusCombo.getSelectedItem();
        String nptelId = searchNptelField.getText().trim().toLowerCase();

        List<Attempt> filtered = dataManager.getAttempts().stream()
            .filter(a -> "All".equals(semester) || a.getSemester().equals(semester))
            .filter(a -> "All".equals(status) || a.getStatus().equals(status))
            .filter(a -> nptelId.isEmpty() || a.getNptelId().toLowerCase().contains(nptelId))
            .filter(a -> {
                if ("All".equals(batch)) return true;
                return dataManager.findStudentById(a.getStudentId())
                    .map(s -> s.getBatch().equals(batch))
                    .orElse(false);
            })
            .collect(Collectors.toList());

        // Update table
        filteredModel.setRowCount(0);
        for (Attempt a : filtered) {
            Student s = dataManager.findStudentById(a.getStudentId()).orElse(null);
            filteredModel.addRow(new Object[]{
                a.getStudentId(),
                s != null ? s.getName() : "Unknown",
                a.getNptelId(),
                a.getCourseName(),
                a.getExamDate(),
                a.getScore(),
                a.getSemester(),
                a.getStatus(),
                s != null ? s.getBatch() : "Unknown"
            });
        }
        
        // Update stats
        long passedCount = filtered.stream().filter(a -> !a.getStatus().equals("Fail")).count();
        long eliteCount = filtered.stream().filter(a -> a.getStatus().contains("Elite")).count();
        double avgScore = filtered.stream().mapToInt(Attempt::getScore).average().orElse(0.0);
        
        totalAttemptsFilteredLabel.setText(String.valueOf(filtered.size()));
        passRateFilteredLabel.setText(filtered.isEmpty() ? "0%" : String.format("%.0f%%", (double) passedCount / filtered.size() * 100));
        avgScoreFilteredLabel.setText(String.format("%.1f", avgScore));
        eliteCountFilteredLabel.setText(String.valueOf(eliteCount));
    }
    
    private void clearFilters() {
        filterSemesterCombo.setSelectedItem("All");
        filterBatchCombo.setSelectedItem("All");
        filterStatusCombo.setSelectedItem("All");
        searchNptelField.setText("");
        applyFilters();
    }

    private void updateReportStats() {
        int studentCount = dataManager.getStudents().size();
        int attemptCount = dataManager.getAttempts().size();
        long passedCount = dataManager.getAttempts().stream().filter(a -> !a.getStatus().equals("Fail")).count();
        long eliteCount = dataManager.getAttempts().stream().filter(a -> a.getStatus().contains("Elite")).count();

        totalStudentsLabel.setText(String.valueOf(studentCount));
        totalExamAttemptsLabel.setText(String.valueOf(attemptCount));
        overallPassRateLabel.setText(attemptCount == 0 ? "0%" : String.format("%.0f%%", (double)passedCount / attemptCount * 100));
        totalEliteLabel.setText(String.valueOf(eliteCount));
    }

    private void generateBatchSummary() {
        batchSummaryModel.setRowCount(0);
        Map<String, List<Student>> studentsByBatch = dataManager.getStudents().stream()
            .collect(Collectors.groupingBy(Student::getBatch));

        studentsByBatch.keySet().stream().sorted().forEach(batch -> {
            List<Student> batchStudents = studentsByBatch.get(batch);
            List<Attempt> batchAttempts = dataManager.getAttempts().stream()
                .filter(a -> batchStudents.stream().anyMatch(s -> s.getId().equals(a.getStudentId())))
                .collect(Collectors.toList());
            
            long passed = batchAttempts.stream().filter(a -> !a.getStatus().equals("Fail")).count();
            long elite = batchAttempts.stream().filter(a -> a.getStatus().contains("Elite")).count();
            String passRate = batchAttempts.isEmpty() ? "0%" : String.format("%.0f%%", (double)passed / batchAttempts.size() * 100);

            batchSummaryModel.addRow(new Object[]{
                batch,
                batchStudents.size(),
                batchAttempts.size(),
                passed,
                elite,
                passRate
            });
        });
    }

    private void generateSemesterSummary() {
        semesterSummaryModel.setRowCount(0);
        Map<String, List<Attempt>> attemptsBySemester = dataManager.getAttempts().stream()
            .collect(Collectors.groupingBy(Attempt::getSemester));
        
        for (int i=1; i<=8; i++) {
            String sem = String.valueOf(i);
            List<Attempt> semAttempts = attemptsBySemester.getOrDefault(sem, List.of());

            long passed = semAttempts.stream().filter(a -> !a.getStatus().equals("Fail")).count();
            long elite = semAttempts.stream().filter(a -> a.getStatus().contains("Elite")).count();
            double avgScore = semAttempts.stream().mapToInt(Attempt::getScore).average().orElse(0.0);
            String passRate = semAttempts.isEmpty() ? "0%" : String.format("%.0f%%", (double)passed / semAttempts.size() * 100);

            semesterSummaryModel.addRow(new Object[]{
                "Semester " + sem,
                semAttempts.size(),
                passed,
                elite,
                passRate,
                String.format("%.1f%%", avgScore)
            });
        }
    }

    private void exportToCSV(JTable table, String fileNamePrefix) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CSV File");
        fileChooser.setSelectedFile(new File(fileNamePrefix + ".csv"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                // Write header
                for (int i = 0; i < table.getColumnCount(); i++) {
                    writer.append(escapeCsv(table.getColumnName(i)));
                    if (i < table.getColumnCount() - 1) writer.append(",");
                }
                writer.append("\n");

                // Write data
                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        writer.append(escapeCsv(table.getValueAt(i, j).toString()));
                        if (j < table.getColumnCount() - 1) writer.append(",");
                    }
                    writer.append("\n");
                }
                JOptionPane.showMessageDialog(this, "CSV file exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String escapeCsv(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            escapedData = "\"" + escapedData.replace("\"", "\"\"") + "\"";
        }
        return escapedData;
    }
    
    // UI Helper and Refresh Methods
    private void refreshStudentsTable() {
        studentsModel.setRowCount(0);
        for (Student s : dataManager.getStudents()) {
            studentsModel.addRow(new Object[]{s.getId(), s.getName(), s.getEmail(), s.getBatch(), s.getDepartment(), s.getCurrentSemester()});
        }
    }

    private void refreshAttemptsTable() {
        attemptsModel.setRowCount(0);
        for (Attempt a : dataManager.getAttempts()) {
            attemptsModel.addRow(new Object[]{a.getStudentId(), a.getNptelId(), a.getCourseName(), a.getExamDate(), a.getScore(), a.getSemester(), a.getStatus()});
        }
    }

    private void updateStudentComboBox() {
        attemptStudentIdCombo.removeAllItems();
        for (Student s : dataManager.getStudents()) {
            attemptStudentIdCombo.addItem(s);
        }
    }
    
    private void clearStudentForm() {
        studentIdField.setText("");
        studentNameField.setText("");
        emailField.setText("");
        departmentField.setText("");
        batchCombo.setSelectedIndex(0);
        currentSemesterCombo.setSelectedIndex(0);
    }
    
    private void clearAttemptForm() {
        attemptStudentIdCombo.setSelectedIndex(-1);
        nptelIdField.setText("");
        courseNameField.setText("");
        examDateField.setText("YYYY-MM-DD");
        scoreField.setText("");
        semesterCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        return button;
    }

    private void styleTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_COLOR);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }
    
    private JLabel createStatLabel(String number, String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JLabel numberLabel = new JLabel(number, SwingConstants.CENTER);
        numberLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        numberLabel.setForeground(PRIMARY_COLOR);
        JLabel textLabel = new JLabel(text, SwingConstants.CENTER);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(numberLabel, BorderLayout.CENTER);
        panel.add(textLabel, BorderLayout.SOUTH);
        return numberLabel;
    }

    // Main Method
    public static void main(String[] args) {
        // Set a modern Look and Feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new NptelTrackerApp().setVisible(true));
    }
}