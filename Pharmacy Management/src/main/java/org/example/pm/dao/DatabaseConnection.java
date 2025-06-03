package org.example.pm.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // --- IMPORTANT: Externalize these settings in a properties file for production ---
    private static final String URL = "jdbc:mysql://localhost:3306/pharmacie?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // Replace with your DB username
    private static final String PASSWORD = ""; // Replace with your DB password

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Ensure the MySQL JDBC driver is loaded (not strictly necessary with modern JDBC)
                // Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                System.err.println("Database connection error: " + e.getMessage());
                throw e; // Re-throw to be handled by caller
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null; // Allow re-connection next time
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}