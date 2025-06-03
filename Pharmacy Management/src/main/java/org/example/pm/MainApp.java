package org.example.pm;


import org.example.pm.controller.MainLayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout; // This is the BorderPane from MainLayout.fxml

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("PharmaGestion");

        initRootLayout();
        // Optionally, show a default view in the center
        if (rootLayout.getCenter() == null) { // Check if controller already set a view
             showDefaultView();
        }
    }

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Objects.requireNonNull(MainApp.class.getResource("/org/example/pm/MainLayout.fxml")));
            rootLayout = loader.load(); // This is the BorderPane

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();

            // Give the controller access to the main app if needed, or set initial view
            MainLayoutController controller = loader.getController();
            // Example: controller.setMainApp(this);
            controller.setView("/org/example/pm/MedicamentView.fxml"); // Load Medicament view by default

        } catch (IOException e) {
            e.printStackTrace();
            // Handle loading error
        }
    }
    
    private void showDefaultView() {
        // Example: Load medicament view by default if not already loaded by MainLayoutController
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Objects.requireNonNull(MainApp.class.getResource("/org/example/pm/MedicamentView.fxml")));
            BorderPane medicamentView = loader.load();
            rootLayout.setCenter(medicamentView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}