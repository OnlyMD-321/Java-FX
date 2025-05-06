package org.example.tp2;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class App extends Application {
    private final MongoService mongoService = new MongoService();

    @Override
    public void start(Stage primaryStage) {
        TextField nameField = new TextField();
        nameField.setPromptText("Nom");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        Button addButton = new Button("Ajouter");
        TextArea displayArea = new TextArea();
        displayArea.setEditable(false);

        addButton.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            if (!name.isEmpty() && !email.isEmpty()) {
                mongoService.insertUser(new User(name, email));
                nameField.clear();
                emailField.clear();
                displayUsers(displayArea);
            }
        });

        VBox layout = new VBox(10, nameField, emailField, addButton, displayArea);
        layout.setPadding(new javafx.geometry.Insets(10));

        displayUsers(displayArea);

        primaryStage.setTitle("MongoDB + JavaFX");
        primaryStage.setScene(new Scene(layout, 400, 400));
        primaryStage.show();
    }

    private void displayUsers(TextArea area) {
        StringBuilder sb = new StringBuilder();
        for (User user : mongoService.getAllUsers()) {
            sb.append(user.getName()).append(" - ").append(user.getEmail()).append("\n");
        }
        area.setText(sb.toString());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
