package org.example.fxml;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage; // Import for URL to check resource

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println(getClass()); // Prints the class of the current object

        // It's good practice to ensure the FXML file is found
        URL fxmlLocation = getClass().getResource("registration_form.fxml");
        if (fxmlLocation == null) {
            // If registration_form.fxml is in the same package as MainApp.java (org.example.fxml),
            // then the path should be "registration_form.fxml" without the leading slash.
            // If it's at the root of resources, "/registration_form.fxml" is correct.
            // Example: fxmlLocation = getClass().getResource("registration_form.fxml");
            System.err.println("Cannot find FXML file: /registration_form.fxml. Make sure it's in the correct resources folder.");
            throw new java.io.FileNotFoundException("FXML file not found: /registration_form.fxml");
        }

        Parent root = FXMLLoader.load(fxmlLocation);
        stage.setTitle("User Registration"); // Translated title
        stage.setScene(new Scene(root, 800, 500));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
