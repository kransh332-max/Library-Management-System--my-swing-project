package test;

import db.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                // Test: Fetch some data
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM book LIMIT 5");

                System.out.println("\n📚 SAMPLE BOOK RECORDS:");
                System.out.println("------------------------------------------");
                while (rs.next()) {
                    System.out.println(
                        "ID: " + rs.getInt("book_id") +
                        ", Title: " + rs.getString("title") +
                        ", Author: " + rs.getString("author") +
                        ", Genre: " + rs.getString("genre") +
                        ", Quantity: " + rs.getInt("quantity")
                    );
                }
                System.out.println("------------------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
