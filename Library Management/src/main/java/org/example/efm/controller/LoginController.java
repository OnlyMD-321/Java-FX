package org.example.efm.controller;

import java.io.IOException; // Ensure Main is imported

import org.example.efm.Main;
import org.example.efm.service.UserService;

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

        if (isInputInvalid(username, password, loginMessageLabel, "Username and password cannot be empty.")) {
            return;
        }

        String[] userInfo = userService.loginUserAndGetRole(username, password);

        if (userInfo != null) {
            Main.setCurrentUser(userInfo[0], userInfo[1]); // Store username and role
            showMessage(loginMessageLabel, "Login successful! Role: " + Main.getCurrentUserRole(), Color.GREEN);
            System.out.println("Login successful for user: " + Main.getCurrentUsername() + " with role: " + Main.getCurrentUserRole());
            try {
                Main.showBookListView(); // Navigate to Book List View
            } catch (IOException e) {
                showMessage(loginMessageLabel, "Error loading book list.", Color.RED);
                e.printStackTrace();
            }
        } else {
            showMessage(loginMessageLabel, "Invalid username or password.", Color.RED);
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
