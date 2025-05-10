package org.example.efm.db;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String URL = "jdbc:h2:./library;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";

    private static Connection conn;

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USER, PASS);
        }
        return conn;
    }

    public static void initDatabase() {
        try (Connection c = getConnection(); Statement stmt = c.createStatement()) {
            // Load and execute schema
            String schemaSql = new String(Files.readAllBytes(Paths.get(DatabaseManager.class.getResource("/schema.sql").toURI())));
            stmt.execute(schemaSql);
            System.out.println("Database schema initialized/verified.");

            // Check if books table is empty before inserting sample data
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM books");
            rs.next();
            int count = rs.getInt("rowcount");
            rs.close();

            if (count == 0) {
                System.out.println("Books table is empty. Inserting sample data...");
                // It's better to load this from a file, but for simplicity, embedding a few here.
                // For a larger list, load from a .sql file.
                String sampleBooksSql
                        = "INSERT INTO books (title, author, isbn, \"year\", available) VALUES "
                        + "('The Lord of the Rings', 'J.R.R. Tolkien', '9780618640157', 1954, TRUE),"
                        + "('Pride and Prejudice', 'Jane Austen', '9780141439518', 1813, TRUE),"
                        + "('To Kill a Mockingbird', 'Harper Lee', '9780061120084', 1960, FALSE),"
                        + "('1984', 'George Orwell', '9780451524935', 1949, TRUE),"
                        + "('The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565', 1925, TRUE);"; // Add more as needed
                // For the full list, you would read 'sample_books.sql' like schema.sql
                // String sampleBooksSql = new String(Files.readAllBytes(Paths.get(DatabaseManager.class.getResource("/sample_books.sql").toURI())));
                stmt.execute(sampleBooksSql);
                System.out.println("Sample books inserted.");
            }

        } catch (Exception e) {
            System.err.println("Error during database initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
