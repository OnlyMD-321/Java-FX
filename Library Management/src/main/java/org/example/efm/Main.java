package org.example.efm;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.example.efm.db.DatabaseManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage; // Store the primary stage
    private static String currentUsername;
    private static String currentUserRole;

    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void setCurrentUser(String username, String role) {
        currentUsername = username;
        currentUserRole = role;
    }

    public static void clearCurrentUser() {
        currentUsername = null;
        currentUserRole = null;
    }

    @Override
    public void start(Stage stage) { // Renamed parameter for clarity
        Main.primaryStage = stage; // Assign to static field

        // Initialize database
        DatabaseManager.initDatabase();

        // Check database connection
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("SELECT 1");
            System.out.println("Database connection successful for UI.");
        } catch (SQLException e) {
            System.err.println("Database connection failed for UI: " + e.getMessage());
        }

        try {
            showLoginView(); // Show initial login view
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load initial LoginView.fxml: " + e.getMessage());
        }
    }

    private static void loadScene(String fxmlFile, String title, int width, int height) throws IOException {
        String fxmlPath = "/org/example/efm/" + fxmlFile;
        URL fxmlUrl = Main.class.getResource(fxmlPath);

        if (fxmlUrl == null) {
            System.err.println("Cannot load FXML file: " + fxmlPath + ". URL is null.");
            throw new IOException("Cannot find FXML resource: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        Scene scene = new Scene(root, width, height);

        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void showLoginView() throws IOException {
        clearCurrentUser(); // Clear user session when showing login
        loadScene("LoginView.fxml", "Library Management - Login", 400, 350);
    }

    public static void showRegisterView() throws IOException {
        loadScene("RegisterView.fxml", "Library Management - Register", 400, 400); // Adjusted height
    }

    public static void showBookListView() throws IOException {
        loadScene("BookListView.fxml", "Library - Book List", 700, 500); // Adjusted size
    }

    public static void main(String[] args) {
        launch(args);
    }
}
