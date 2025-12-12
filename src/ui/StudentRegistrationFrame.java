package ui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import db.DBConnection;

public class StudentRegistrationFrame extends JFrame {

    private JTextField nameField, emailField, phoneField;
    private JTextArea addressArea;
    private JButton registerBtn, backBtn;

    public StudentRegistrationFrame() {
        setTitle("📘 Register New Student");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Title
        JLabel title = new JLabel("Register New Student", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Form panel
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        form.add(new JLabel("Name:"));
        nameField = new JTextField();
        form.add(nameField);

        form.add(new JLabel("Email:"));
        emailField = new JTextField();
        form.add(emailField);

        form.add(new JLabel("Phone:"));
        phoneField = new JTextField();
        form.add(phoneField);

        form.add(new JLabel("Address:"));
        addressArea = new JTextArea(3, 15);
        JScrollPane sp = new JScrollPane(addressArea);
        form.add(sp);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel();
        registerBtn = new JButton("Register");
        backBtn = new JButton("Back");

        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnPanel.add(registerBtn);
        btnPanel.add(backBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // Listeners
        registerBtn.addActionListener(e -> registerStudent());
        backBtn.addActionListener(e -> dispose());
    }

    private void registerStudent() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressArea.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Please fill in all fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO student (name, email, phone, address) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "✅ Student Registered Successfully!");
                nameField.setText("");
                emailField.setText("");
                phoneField.setText("");
                addressArea.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "❌ Registration failed. Try again!");
            }
        } catch (SQLIntegrityConstraintViolationException dup) {
            JOptionPane.showMessageDialog(this, "⚠️ Email already exists in database!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentRegistrationFrame().setVisible(true));
    }
}
