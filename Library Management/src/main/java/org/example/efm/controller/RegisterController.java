package org.example.efm.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.example.efm.Main; // For navigation
import org.example.efm.service.UserService;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    protected void handleRegisterAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("All fields are required.", Color.RED);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match.", Color.RED);
            return;
        }

        boolean registered = userService.registerUser(username, password);

        if (registered) {
            showMessage("Registration successful! You can now login.", Color.GREEN);
            // Optionally, navigate back to login after a short delay or directly
            // For now, user can click "Back to Login"
            clearFields();
        } else {
            // UserService already prints specific errors (e.g., username exists)
            showMessage("Registration failed. Username might already exist or an error occurred.", Color.RED);
        }
    }

    @FXML
    protected void handleBackToLoginAction() {
        try {
            Main.showLoginView();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error navigating back to login.", Color.RED);
        }
    }

    private void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setTextFill(color);
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}
