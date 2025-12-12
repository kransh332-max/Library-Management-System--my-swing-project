package ui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import db.DBConnection;

public class StudentLoginFrame extends JFrame {

    private JTextField nameField, emailField;
    private JButton loginBtn, backBtn;

    public StudentLoginFrame() {
        setTitle("🎓 Student Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Title
        JLabel titleLabel = new JLabel("🎓 Student Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));

        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        loginBtn = new JButton("Login");
        backBtn = new JButton("Back");

        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        buttonPanel.add(loginBtn);
        buttonPanel.add(backBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event listeners
        loginBtn.addActionListener(e -> validateStudent());
        backBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private void validateStudent() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Please fill in both fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM student WHERE name = ? AND email = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("student_id");
                JOptionPane.showMessageDialog(this, "✅ Welcome, " + name + "!");
                new StudentDashboard(id, name).setVisible(true); // opens the student's personal dashboard
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Invalid name or email! Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "⚠️ Database Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentLoginFrame().setVisible(true));
    }
}
