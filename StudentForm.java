import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentForm extends JFrame {
    private JTextField nameField, ageField, courseField;
    private JButton addButton, updateButton, deleteButton;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private int selectedStudentId = -1;

    public StudentForm() {
        setTitle("Student Management System");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top heading
        JLabel heading = new JLabel("*** CRUD ***", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(heading, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Student Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        formPanel.add(nameField, gbc);

        // Age
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1;
        ageField = new JTextField(15);
        formPanel.add(ageField, gbc);

        // Course
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        courseField = new JTextField(15);
        formPanel.add(courseField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.WEST);

        // Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Age", "Course"}, 0);
        studentTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("All Students"));
        add(scrollPane, BorderLayout.CENTER);

        // Load data
        loadStudents();

        // Listeners
        addButton.addActionListener(e -> insertStudent());
        updateButton.addActionListener(e -> updateStudent());
        deleteButton.addActionListener(e -> deleteStudent());

        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && studentTable.getSelectedRow() != -1) {
                int row = studentTable.getSelectedRow();
                selectedStudentId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                ageField.setText(tableModel.getValueAt(row, 2).toString());
                courseField.setText(tableModel.getValueAt(row, 3).toString());
                updateButton.setEnabled(true);
                deleteButton.setEnabled(true);
                addButton.setEnabled(false);
            }
        });

        setVisible(true);
    }

    private void loadStudents() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Students")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("course")
                });
            }
        } catch (Exception e) {
            showError("Failed to load students: " + e.getMessage());
        }
    }

    private void insertStudent() {
        String name = nameField.getText();
        String ageText = ageField.getText();
        String course = courseField.getText();

        if (name.isEmpty() || ageText.isEmpty() || course.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Students (name, age, course) VALUES (?, ?, ?)");
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, course);
            pstmt.executeUpdate();

            showInfo("Student added successfully.");
            clearForm();
            loadStudents();

            pstmt.close();
            conn.close();
        } catch (NumberFormatException e) {
            showError("Age must be a number.");
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void updateStudent() {
        if (selectedStudentId == -1) return;

        String name = nameField.getText();
        String ageText = ageField.getText();
        String course = courseField.getText();

        if (name.isEmpty() || ageText.isEmpty() || course.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("UPDATE Students SET name=?, age=?, course=? WHERE id=?");
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, course);
            pstmt.setInt(4, selectedStudentId);
            pstmt.executeUpdate();

            showInfo("Student updated successfully.");
            clearForm();
            loadStudents();

            pstmt.close();
            conn.close();
        } catch (NumberFormatException e) {
            showError("Age must be a number.");
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void deleteStudent() {
        if (selectedStudentId == -1) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this student?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Students WHERE id=?");
            pstmt.setInt(1, selectedStudentId);
            pstmt.executeUpdate();

            showInfo("Student deleted successfully.");
            clearForm();
            loadStudents();

            pstmt.close();
            conn.close();
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText("");
        ageField.setText("");
        courseField.setText("");
        selectedStudentId = -1;
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        studentTable.clearSelection();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "❌ Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "✅ Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.out.println("MySQL Driver not found");
        }

        SwingUtilities.invokeLater(StudentForm::new);
    }
}
