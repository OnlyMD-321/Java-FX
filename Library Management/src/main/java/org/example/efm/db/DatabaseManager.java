package org.example.efm.db;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
            // Ensure H2 driver is loaded (optional for modern JDBC, but good practice)
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("H2 JDBC Driver not found: " + e.getMessage());
                e.printStackTrace(); // Or throw a runtime exception
            }
            conn = DriverManager.getConnection(URL, USER, PASS);
        }
        return conn;
    }

    public static void initDatabase() {
        try (Connection c = getConnection(); Statement stmt = c.createStatement()) {
            // Load and execute schema using InputStream and specifying UTF-8
            InputStream schemaStream = DatabaseManager.class.getResourceAsStream("/schema.sql");
            if (schemaStream == null) {
                throw new RuntimeException("Cannot find schema.sql in the classpath. Make sure it's in src/main/resources/");
            }
            String schemaSql = new String(schemaStream.readAllBytes(), StandardCharsets.UTF_8);
            schemaStream.close();

            // Execute the schema script
            // H2's execute method can handle multiple statements in a single string if separated by semicolons
            stmt.execute(schemaSql);
            System.out.println("Database schema initialized/verified.");

            // The sample data insertion logic previously here can be kept if desired,
            // or ensure all data is in schema.sql
            // For example, checking if books table is empty and inserting:
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM books");
            if (rs.next()) {
                int count = rs.getInt("rowcount");
                if (count == 0) {
                    System.out.println("Books table is empty. The schema.sql should contain INSERT statements for books.");
                    // If your schema.sql already has book inserts, this check might be redundant
                    // or you can remove book inserts from schema.sql and handle them here.
                }
            }
            rs.close();

        } catch (Exception e) {
            System.err.println("Error during database initialization: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
        }
    }

    public static void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
