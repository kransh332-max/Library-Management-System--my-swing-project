package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.temporal.ChronoUnit;
import db.DBConnection;
import utils.DateUtils;

public class AdminDashboard extends JFrame {

    private JPanel sidebar;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // BOOKS
    private JTable booksTable;
    private DefaultTableModel booksModel;
    private JTextField searchTitleField, searchAuthorField, searchGenreField;
    private JTextField addTitleField, addAuthorField, addGenreField, addQtyField;
    private JButton addBookBtn, updateQtyBtn, deleteBookBtn, searchBooksBtn, refreshBooksBtn;

    // STUDENTS
    private JTable studentsTable;
    private DefaultTableModel studentsModel;

    // ISSUES
    private JTable issuesTable;
    private DefaultTableModel issuesModel;
    private JComboBox<String> issueBookCombo;
    private JComboBox<String> issueStudentCombo;
    private JButton issueBookBtn, returnBookBtn, refreshIssuesBtn;
    private JTextField returnIssueIdField;

    // REPORTS
    private JTable overdueTable;
    private DefaultTableModel overdueModel;
    private JButton refreshOverdueBtn;

    public AdminDashboard() {
        setTitle("Admin Dashboard - Library Management System");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initSidebar();
        initMainPanel();

        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        loadAllBooks();
        loadAllStudents();
        loadAllIssues();
        loadOverdue();

        setVisible(true);
    }

    // ===================== SIDEBAR =====================
    private void initSidebar() {
        sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setLayout(new GridLayout(9, 1, 5, 5));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton booksBtn = new JButton("Books");
        JButton studentsBtn = new JButton("Students");
        JButton issuesBtn = new JButton("Issue/Return");
        JButton reportsBtn = new JButton("Overdue Reports");
        JButton registerBtn = new JButton("Register Student");
        JButton refreshBtn = new JButton("Refresh All");
        JButton logoutBtn = new JButton("Logout");

        booksBtn.addActionListener(e -> cardLayout.show(mainPanel, "books"));
        studentsBtn.addActionListener(e -> cardLayout.show(mainPanel, "students"));
        issuesBtn.addActionListener(e -> cardLayout.show(mainPanel, "issues"));
        reportsBtn.addActionListener(e -> cardLayout.show(mainPanel, "reports"));
        registerBtn.addActionListener(e -> new StudentRegistrationFrame().setVisible(true));
        refreshBtn.addActionListener(e -> {
            loadAllBooks();
            loadAllStudents();
            loadAllIssues();
            loadOverdue();
            JOptionPane.showMessageDialog(this, "✅ All data refreshed successfully!");
        });
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        sidebar.add(booksBtn);
        sidebar.add(studentsBtn);
        sidebar.add(issuesBtn);
        sidebar.add(reportsBtn);
        sidebar.add(registerBtn);
        sidebar.add(refreshBtn);
        sidebar.add(new JLabel(""));
        sidebar.add(logoutBtn);
    }

    // ===================== MAIN PANEL =====================
    private void initMainPanel() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(buildBooksPanel(), "books");
        mainPanel.add(buildStudentsPanel(), "students");
        mainPanel.add(buildIssuesPanel(), "issues");
        mainPanel.add(buildReportsPanel(), "reports");

        cardLayout.show(mainPanel, "books");
    }

    // ===================== BOOKS PANEL =====================
    private JPanel buildBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel top = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchTitleField = new JTextField(12);
        searchAuthorField = new JTextField(12);
        searchGenreField = new JTextField(10);
        searchBooksBtn = new JButton("Search");
        refreshBooksBtn = new JButton("Refresh");

        searchPanel.add(new JLabel("Title:"));
        searchPanel.add(searchTitleField);
        searchPanel.add(new JLabel("Author:"));
        searchPanel.add(searchAuthorField);
        searchPanel.add(new JLabel("Genre:"));
        searchPanel.add(searchGenreField);
        searchPanel.add(searchBooksBtn);
        searchPanel.add(refreshBooksBtn);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addTitleField = new JTextField(12);
        addAuthorField = new JTextField(12);
        addGenreField = new JTextField(10);
        addQtyField = new JTextField(4);
        addBookBtn = new JButton("Add Book");
        updateQtyBtn = new JButton("Update Qty");
        deleteBookBtn = new JButton("Delete");

        addPanel.add(new JLabel("Title:"));
        addPanel.add(addTitleField);
        addPanel.add(new JLabel("Author:"));
        addPanel.add(addAuthorField);
        addPanel.add(new JLabel("Genre:"));
        addPanel.add(addGenreField);
        addPanel.add(new JLabel("Qty:"));
        addPanel.add(addQtyField);
        addPanel.add(addBookBtn);
        addPanel.add(updateQtyBtn);
        addPanel.add(deleteBookBtn);

        top.add(searchPanel);
        top.add(addPanel);

        booksModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Genre", "Qty"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        booksTable = new JTable(booksModel);
        JScrollPane sp = new JScrollPane(booksTable);

        panel.add(top, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        searchBooksBtn.addActionListener(e -> searchBooks());
        refreshBooksBtn.addActionListener(e -> loadAllBooks());
        addBookBtn.addActionListener(e -> addBook());
        deleteBookBtn.addActionListener(e -> deleteSelectedBook());
        updateQtyBtn.addActionListener(e -> updateQuantityDialog());

        return panel;
    }

    private void searchBooks() {
        String title = searchTitleField.getText().trim();
        String author = searchAuthorField.getText().trim();
        String genre = searchGenreField.getText().trim();

        StringBuilder sql = new StringBuilder("SELECT * FROM book WHERE 1=1");
        if (!title.isEmpty()) sql.append(" AND title LIKE ?");
        if (!author.isEmpty()) sql.append(" AND author LIKE ?");
        if (!genre.isEmpty()) sql.append(" AND genre LIKE ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (!title.isEmpty()) ps.setString(index++, "%" + title + "%");
            if (!author.isEmpty()) ps.setString(index++, "%" + author + "%");
            if (!genre.isEmpty()) ps.setString(index++, "%" + genre + "%");

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

            if (booksModel.getRowCount() == 0)
                JOptionPane.showMessageDialog(this, "❌ No books found for given filters.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "⚠️ Search failed: " + ex.getMessage());
        }
    }

    private void loadAllBooks() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM book ORDER BY title");
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
        populateIssueCombos();
    }

    private void addBook() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO book(title, author, genre, quantity) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, addTitleField.getText().trim());
            ps.setString(2, addAuthorField.getText().trim());
            ps.setString(3, addGenreField.getText().trim());
            ps.setInt(4, Integer.parseInt(addQtyField.getText().trim()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Book added!");
            loadAllBooks();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void deleteSelectedBook() {
        int row = booksTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "⚠️ Select a book."); return; }
        int id = (int) booksModel.getValueAt(row, 0);
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM book WHERE book_id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Book deleted!");
            loadAllBooks();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void updateQuantityDialog() {
        int row = booksTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "⚠️ Select a book."); return; }
        int id = (int) booksModel.getValueAt(row, 0);
        String newQty = JOptionPane.showInputDialog(this, "Enter new quantity:");
        if (newQty == null) return;
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE book SET quantity=? WHERE book_id=?");
            ps.setInt(1, Integer.parseInt(newQty));
            ps.setInt(2, id);
            ps.executeUpdate();
            loadAllBooks();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ===================== STUDENTS PANEL =====================
    private JPanel buildStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        studentsModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Phone", "Address"}, 0);
        studentsTable = new JTable(studentsModel);
        JScrollPane sp = new JScrollPane(studentsTable);

        JButton refreshBtn = new JButton("Refresh Students");
        refreshBtn.addActionListener(e -> loadAllStudents());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    private void loadAllStudents() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM student ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            studentsModel.setRowCount(0);
            while (rs.next()) {
                studentsModel.addRow(new Object[]{
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        populateIssueCombos();
    }

    // ===================== ISSUES PANEL =====================
    private JPanel buildIssuesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        issueBookCombo = new JComboBox<>();
        issueStudentCombo = new JComboBox<>();
        issueBookBtn = new JButton("Issue Book (14 days)");
        returnBookBtn = new JButton("Return Book");
        returnIssueIdField = new JTextField(6);
        refreshIssuesBtn = new JButton("Refresh Issues");

        top.add(new JLabel("Book:"));
        top.add(issueBookCombo);
        top.add(new JLabel("Student:"));
        top.add(issueStudentCombo);
        top.add(issueBookBtn);
        top.add(new JLabel("Issue ID:"));
        top.add(returnIssueIdField);
        top.add(returnBookBtn);
        top.add(refreshIssuesBtn);

        issuesModel = new DefaultTableModel(new String[]{
                "IssueID", "Book", "Student", "IssueDate", "DueDate", "ReturnDate", "Fine"
        }, 0);
        issuesTable = new JTable(issuesModel);
        JScrollPane sp = new JScrollPane(issuesTable);

        panel.add(top, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        issueBookBtn.addActionListener(e -> issueSelectedBook());
        returnBookBtn.addActionListener(e -> returnBookByIssueId());
        refreshIssuesBtn.addActionListener(e -> loadAllIssues());

        return panel;
    }

    private void populateIssueCombos() {
        issueBookCombo.removeAllItems();
        issueStudentCombo.removeAllItems();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs1 = conn.prepareStatement("SELECT * FROM book").executeQuery();
            while (rs1.next()) issueBookCombo.addItem(rs1.getInt("book_id") + " - " + rs1.getString("title"));
            ResultSet rs2 = conn.prepareStatement("SELECT * FROM student").executeQuery();
            while (rs2.next()) issueStudentCombo.addItem(rs2.getInt("student_id") + " - " + rs2.getString("name"));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void issueSelectedBook() {
        if (issueBookCombo.getItemCount() == 0 || issueStudentCombo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "⚠️ No books or students available to issue.");
            return;
        }

        String selectedBook = (String) issueBookCombo.getSelectedItem();
        String selectedStudent = (String) issueStudentCombo.getSelectedItem();

        int bookId = Integer.parseInt(selectedBook.split(" - ")[0]);
        int studentId = Integer.parseInt(selectedStudent.split(" - ")[0]);

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement checkQty = conn.prepareStatement("SELECT quantity FROM book WHERE book_id=?");
            checkQty.setInt(1, bookId);
            ResultSet rsQty = checkQty.executeQuery();
            if (rsQty.next() && rsQty.getInt("quantity") <= 0) {
                JOptionPane.showMessageDialog(this, "❌ Book not available. Quantity = 0");
                return;
            }

            Date issueDate = DateUtils.today();
            Date dueDate = DateUtils.plusDays(14);

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO issue (book_id, student_id, issue_date, due_date, fine) VALUES (?, ?, ?, ?, 0)"
            );
            ps.setInt(1, bookId);
            ps.setInt(2, studentId);
            ps.setDate(3, issueDate);
            ps.setDate(4, dueDate);
            ps.executeUpdate();

            PreparedStatement updateQty = conn.prepareStatement("UPDATE book SET quantity = quantity - 1 WHERE book_id=?");
            updateQty.setInt(1, bookId);
            updateQty.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Book issued successfully!\nDue date: " + dueDate);
            loadAllBooks();
            loadAllIssues();
            populateIssueCombos();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "⚠️ Could not issue book: " + ex.getMessage());
        }
    }

    private void loadAllIssues() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT i.issue_id, b.title AS book, s.name AS student, i.issue_date, i.due_date, i.return_date, i.fine " +
                             "FROM issue i JOIN book b ON i.book_id=b.book_id JOIN student s ON i.student_id=s.student_id");
             ResultSet rs = ps.executeQuery()) {
            issuesModel.setRowCount(0);
            while (rs.next()) {
                issuesModel.addRow(new Object[]{
                        rs.getInt("issue_id"),
                        rs.getString("book"),
                        rs.getString("student"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        rs.getDate("return_date"),
                        rs.getDouble("fine")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void returnBookByIssueId() {
        String idText = returnIssueIdField.getText().trim();
        if (idText.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Issue ID."); return; }
        int issueId = Integer.parseInt(idText);
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM issue WHERE issue_id=?");
            ps.setInt(1, issueId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { JOptionPane.showMessageDialog(this, "❌ Issue ID not found."); return; }

            Date dueDate = rs.getDate("due_date");
            Date returnDate = DateUtils.today();
            double fine = DateUtils.calculateFine(dueDate, returnDate);

            PreparedStatement update = conn.prepareStatement("UPDATE issue SET return_date=?, fine=? WHERE issue_id=?");
            update.setDate(1, returnDate);
            update.setDouble(2, fine);
            update.setInt(3, issueId);
            update.executeUpdate();

            PreparedStatement inc = conn.prepareStatement("UPDATE book SET quantity = quantity + 1 WHERE book_id=?");
            inc.setInt(1, rs.getInt("book_id"));
            inc.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Book returned.\nFine: ₹" + fine);
            loadAllBooks();
            loadAllIssues();
            loadOverdue();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ===================== REPORTS PANEL =====================
    private JPanel buildReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        overdueModel = new DefaultTableModel(new String[]{
                "IssueID", "Book", "Student", "IssueDate", "DueDate", "Days Late", "Fine (₹)"
        }, 0);
        overdueTable = new JTable(overdueModel);
        JScrollPane sp = new JScrollPane(overdueTable);
        refreshOverdueBtn = new JButton("Refresh Overdue");
        refreshOverdueBtn.addActionListener(e -> loadOverdue());
        panel.add(refreshOverdueBtn, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private void loadOverdue() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT i.issue_id, b.title, s.name, i.issue_date, i.due_date " +
                             "FROM issue i JOIN book b ON i.book_id=b.book_id " +
                             "JOIN student s ON i.student_id=s.student_id " +
                             "WHERE i.return_date IS NULL AND i.due_date < CURDATE()");
             ResultSet rs = ps.executeQuery()) {
            overdueModel.setRowCount(0);
            while (rs.next()) {
                Date due = rs.getDate("due_date");
                long daysLate = ChronoUnit.DAYS.between(due.toLocalDate(), java.time.LocalDate.now());
                double fine = daysLate * 10;
                overdueModel.addRow(new Object[]{
                        rs.getInt("issue_id"),
                        rs.getString("title"),
                        rs.getString("name"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        daysLate,
                        fine
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
