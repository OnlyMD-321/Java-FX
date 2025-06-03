package org.example.efm;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.example.efm.db.DatabaseManager;
import org.example.efm.model.User; // Import User model

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage; // Store the primary stage
    private static User currentUser; // Store the full User object

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public static Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public static void setCurrentUser(User user) { // Accept User object
        currentUser = user;
    }

    public static void clearCurrentUser() {
        currentUser = null;
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
        loadScene("BookListView.fxml", "Library - Book List", 800, 600); // Adjusted size for new button
    }

    public static void showMemberManagementView() throws IOException {
        loadScene("MemberManagementView.fxml", "Library - Borrower Management", 650, 450);
    }

    public static void showMyLoansView() throws IOException {
        loadScene("MyLoansView.fxml", "Library - My Borrows", 750, 500);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
