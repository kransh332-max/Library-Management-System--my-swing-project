package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import db.DBConnection;

public class StudentDashboard extends JFrame {

    private int studentId;
    private String studentName;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    // Tables
    private JTable booksTable;
    private DefaultTableModel booksModel;
    private JTable issuesTable;
    private DefaultTableModel issuesModel;

    // Search fields
    private JTextField titleField, authorField, genreField;

    public StudentDashboard(int studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;

        setTitle("📚 Student Dashboard - " + studentName);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initSidebar();
        initMainPanel();

        add(buildHeader(), BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        loadAllBooks();
        loadMyIssues();

        setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("Welcome, " + studentName, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(lbl, BorderLayout.CENTER);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return header;
    }

    private void initSidebar() {
        JPanel sidebar = new JPanel(new GridLayout(4, 1, 10, 10));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JButton booksBtn = new JButton("Search Books");
        JButton issuesBtn = new JButton("My Issues");
        JButton refreshBtn = new JButton("Refresh");
        JButton logoutBtn = new JButton("Logout");

        booksBtn.addActionListener(e -> cardLayout.show(mainPanel, "books"));
        issuesBtn.addActionListener(e -> cardLayout.show(mainPanel, "issues"));
        refreshBtn.addActionListener(e -> {
            loadAllBooks();
            loadMyIssues();
            JOptionPane.showMessageDialog(this, "✅ Data refreshed!");
        });
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        sidebar.add(booksBtn);
        sidebar.add(issuesBtn);
        sidebar.add(refreshBtn);
        sidebar.add(logoutBtn);

        add(sidebar, BorderLayout.WEST);
    }

    private void initMainPanel() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(buildBooksPanel(), "books");
        mainPanel.add(buildIssuesPanel(), "issues");
    }

    // 🔍 Search Books panel
    private JPanel buildBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleField = new JTextField(12);
        authorField = new JTextField(12);
        genreField = new JTextField(10);
        JButton searchBtn = new JButton("Search");
        JButton resetBtn = new JButton("Show All");

        searchPanel.add(new JLabel("Title:"));
        searchPanel.add(titleField);
        searchPanel.add(new JLabel("Author:"));
        searchPanel.add(authorField);
        searchPanel.add(new JLabel("Genre:"));
        searchPanel.add(genreField);
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);

        booksModel = new DefaultTableModel(new String[]{"Book ID", "Title", "Author", "Genre", "Quantity"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        booksTable = new JTable(booksModel);
        JScrollPane sp = new JScrollPane(booksTable);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        searchBtn.addActionListener(e -> searchBooks());
        resetBtn.addActionListener(e -> loadAllBooks());

        return panel;
    }

    private void searchBooks() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String genre = genreField.getText().trim();

        String sql = "SELECT * FROM book WHERE title LIKE ? AND author LIKE ? AND genre LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + title + "%");
            ps.setString(2, "%" + author + "%");
            ps.setString(3, "%" + genre + "%");
            ResultSet rs = ps.executeQuery();

            booksModel.setRowCount(0);
            while (rs.next()) {
                booksModel.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getInt("quantity")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadAllBooks() {
        String sql = "SELECT * FROM book ORDER BY title";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            booksModel.setRowCount(0);
            while (rs.next()) {
                booksModel.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getInt("quantity")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 📦 My Issues panel
    private JPanel buildIssuesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        issuesModel = new DefaultTableModel(new String[]{
                "Issue ID", "Book Title", "Issue Date", "Due Date", "Return Date", "Fine (₹)"
        }, 0);
        issuesTable = new JTable(issuesModel);
        JScrollPane sp = new JScrollPane(issuesTable);

        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private void loadMyIssues() {
        String sql = "SELECT i.issue_id, b.title, i.issue_date, i.due_date, i.return_date, i.fine " +
                "FROM `issue` i JOIN book b ON i.book_id = b.book_id WHERE i.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            issuesModel.setRowCount(0);
            while (rs.next()) {
                issuesModel.addRow(new Object[]{
                        rs.getInt("issue_id"),
                        rs.getString("title"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        rs.getDate("return_date"),
                        rs.getDouble("fine")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
