package org.example.efm.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.example.efm.db.DatabaseManager;
import org.example.efm.model.User;
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

    // Returns User object on success, null on failure
    public User loginUserAndGetDetails(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            System.out.println("[UserService.loginUserAndGetDetails] Username or password was empty.");
            return null;
        }
        System.out.println("[UserService.loginUserAndGetDetails] Attempting login for username: " + username);
        String sql = "SELECT id, password_hash, role FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                long id = rs.getLong("id");
                String storedHash = rs.getString("password_hash");
                String role = rs.getString("role");
                System.out.println("[UserService.loginUserAndGetDetails] User found: " + username + ", ID: " + id + ", Role: " + role);

                boolean passwordMatch = BCrypt.checkpw(password, storedHash);
                System.out.println("[UserService.loginUserAndGetDetails] BCrypt.checkpw result for " + username + ": " + passwordMatch);

                if (passwordMatch) {
                    System.out.println("[UserService.loginUserAndGetDetails] Password match for user: " + username);
                    return new User(id, username, role);
                } else {
                    System.out.println("[UserService.loginUserAndGetDetails] Password mismatch for user: " + username);
                }
            } else {
                System.out.println("[UserService.loginUserAndGetDetails] User not found: " + username);
            }
            return null; // User not found or password mismatch
        } catch (SQLException e) {
            System.err.println("[UserService.loginUserAndGetDetails] SQLException during login for " + username + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("[UserService.loginUserAndGetDetails] Unexpected error during login for " + username + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Keep the old loginUser method if it's used elsewhere, or deprecate/remove
    // For now, let's update it to use the new method for consistency, though it loses role info.
    @Deprecated
    public boolean loginUser(String username, String password) {
        return loginUserAndGetDetails(username, password) != null;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT id, username, role FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by username: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllBorrowers() {
        List<User> borrowers = new ArrayList<>();
        String sql = "SELECT id, username, role FROM users WHERE role = 'borrower'";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                borrowers.add(new User(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching borrowers: " + e.getMessage());
            e.printStackTrace();
        }
        return borrowers;
    }
}
