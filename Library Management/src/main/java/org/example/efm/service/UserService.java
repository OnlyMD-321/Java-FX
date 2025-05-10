package org.example.efm.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.example.efm.db.DatabaseManager;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    public boolean registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            System.err.println("Username or password cannot be empty.");
            return false;
        }
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // Check for unique constraint violation (username already exists)
            if (e.getSQLState().equals("23505")) { // H2 unique constraint violation code
                System.err.println("Registration failed: Username already exists.");
            } else {
                System.err.println("Registration failed: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }

    // Returns String[]{username, role} on success, null on failure
    public String[] loginUserAndGetRole(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        String sql = "SELECT password_hash, role FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String role = rs.getString("role");
                if (BCrypt.checkpw(password, storedHash)) {
                    return new String[]{username, role};
                }
            }
            return null; // User not found or password mismatch
        } catch (SQLException e) {
            System.err.println("Login failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Keep the old loginUser method if it's used elsewhere, or deprecate/remove
    public boolean loginUser(String username, String password) {
        return loginUserAndGetRole(username, password) != null;
    }
}
