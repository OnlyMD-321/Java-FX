package org.example.taskmanager;

import javafx.scene.control.Alert;

public class AlertHelper {

    public static void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showErrorAlert(String title, String content) {
        showAlert(Alert.AlertType.ERROR, title, null, content);
    }

    public static void showInfoAlert(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, null, content);
    }

    // Add more specific alert methods as needed
}
