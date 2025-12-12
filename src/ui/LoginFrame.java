package ui;

import javax.swing.*;
import java.awt.*;
import db.DBConnection;

public class LoginFrame extends JFrame {

    private JButton adminLoginBtn, studentLoginBtn;
    private JLabel titleLabel;

    public LoginFrame() {
        setTitle("Library Management System - Login Portal");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        titleLabel = new JLabel("📚 Library Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1, 20, 20));

        adminLoginBtn = new JButton("Admin Login");
        studentLoginBtn = new JButton("Student Login");

        adminLoginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        studentLoginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));

        centerPanel.add(adminLoginBtn);
        centerPanel.add(studentLoginBtn);

        add(centerPanel, BorderLayout.CENTER);

        // Button actions
        adminLoginBtn.addActionListener(e -> openAdminLogin());
        studentLoginBtn.addActionListener(e -> openStudentLogin());
    }

    private void openAdminLogin() {
        new AdminLoginFrame().setVisible(true);
        dispose();
    }

    private void openStudentLogin() {
        new StudentLoginFrame().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
