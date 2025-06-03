package org.example.efm.controller;

import java.io.IOException; // Ensure Main is imported

import org.example.efm.Main;
import org.example.efm.service.UserService;
import org.example.efm.model.User; // Import User model

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color; // Import IOException

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label loginMessageLabel;

    @FXML
    private Label registerMessageLabel;

    private final UserService userService = new UserService();

    @FXML
    protected void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            if (isInputInvalid(username, password, loginMessageLabel, "Username and password cannot be empty.")) {
                return;
            }

            User user = userService.loginUserAndGetDetails(username, password); // Get User object

            if (user != null) {
                Main.setCurrentUser(user); // Store User object
                showMessage(loginMessageLabel, "Login successful! Role: " + Main.getCurrentUserRole(), Color.GREEN);
                System.out.println("Login successful for user: " + Main.getCurrentUsername() + " with role: " + Main.getCurrentUserRole());
                try {
                    Main.showBookListView(); // Navigate to Book List View
                } catch (IOException ioe) {
                    showMessage(loginMessageLabel, "Error loading book list.", Color.RED);
                    System.err.println("Error loading book list view after login for user: " + username);
                    ioe.printStackTrace(); // Prints stack trace to terminal
                }
            } else {
                // This case is hit if:
                // 1. Username/password is incorrect (UserService.loginUserAndGetRole returns null, no exception).
                // 2. An SQLException occurred in UserService (UserService prints its own error log and returns null).
                showMessage(loginMessageLabel, "Invalid username or password.", Color.RED);
                System.out.println("Login attempt failed for username: " + username + ". (Invalid credentials or see previous logs for system errors)");
            }
        } catch (Exception e) {
            // Catch any other unexpected exceptions during the login action
            showMessage(loginMessageLabel, "An unexpected error occurred during login. Please check logs.", Color.RED);
            System.err.println("Unexpected error during login attempt for username: " + username);
            e.printStackTrace(); // Prints stack trace to terminal
        }
    }

    @FXML
    protected void handleRegisterButtonAction() {
        try {
            Main.showRegisterView();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage(registerMessageLabel, "Error opening registration page.", Color.RED);
        }
    }

    private boolean isInputInvalid(String username, String password, Label messageLabel, String errorMessage) {
        if (username.isEmpty() || password.isEmpty()) {
            showMessage(messageLabel, errorMessage, Color.RED);
            return true;
        }
        return false;
    }

    private void showMessage(Label label, String message, Color color) {
        label.setText(message);
        label.setTextFill(color);
    }

    private void clearFieldsAndMessages(boolean clearLoginMessage) {
        usernameField.clear();
        passwordField.clear();
        if (clearLoginMessage) {
            loginMessageLabel.setText("");
        }
        registerMessageLabel.setText("");
    }
}
