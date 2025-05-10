package org.example.fxml; // Matching the file path

import java.sql.SQLException;
import java.util.logging.Level; // Import Logger
import java.util.logging.Logger; // Import Logger

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Window;

public class RegisterController {

    private static final Logger LOGGER = Logger.getLogger(RegisterController.class.getName()); // Added logger

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailIdField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button submitButton;

    @FXML
    public void register(ActionEvent event) { // Removed "throws SQLException" as we will handle it locally
        Window owner = submitButton.getScene().getWindow();

        // Basic form validation
        if (fullNameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, owner, "Form Error!",
                    "Please enter your full name");
            return;
        }
        if (emailIdField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, owner, "Form Error!",
                    "Please enter your email id");
            return;
        }
        // Basic email format validation (optional, can be more sophisticated)
        if (!emailIdField.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showAlert(Alert.AlertType.ERROR, owner, "Form Error!",
                    "Please enter a valid email address");
            return;
        }
        if (passwordField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, owner, "Form Error!",
                    "Please enter a password");
            return;
        }
        // Optional: Password strength validation
        if (passwordField.getText().length() < 6) {
            showAlert(Alert.AlertType.ERROR, owner, "Form Error!",
                    "Password must be at least 6 characters long");
            return;
        }

        String fullName = fullNameField.getText();
        String emailId = emailIdField.getText();
        String password = passwordField.getText();

        JdbcDao jdbcDao = new JdbcDao();
        try {
            jdbcDao.insertRecord(fullName, emailId, password);
            // If insertRecord is successful, show confirmation
            showAlert(Alert.AlertType.CONFIRMATION, owner, "Registration Successful!",
                    "Welcome " + fullName + "!"); // Use the variable fullName for consistency

            // Optionally, clear the fields after successful registration
            clearFields();

        } catch (SQLException e) {
            // Log the exception
            LOGGER.log(Level.SEVERE, "Database error during registration for user: " + emailId, e);
            // Show an error message to the user
            showAlert(Alert.AlertType.ERROR, owner, "Registration Failed",
                    "Could not register user. A database error occurred. Please try again later or contact support.");
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            LOGGER.log(Level.SEVERE, "An unexpected error occurred during registration for user: " + emailId, e);
            showAlert(Alert.AlertType.ERROR, owner, "Registration Failed",
                    "An unexpected error occurred. Please try again later.");
        }
    }

    private void clearFields() {
        fullNameField.clear();
        emailIdField.clear();
        passwordField.clear();
    }

    private static void showAlert(Alert.AlertType alertType, Window owner, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text for simpler alerts
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait(); // Use showAndWait to make the dialog modal
    }
}
