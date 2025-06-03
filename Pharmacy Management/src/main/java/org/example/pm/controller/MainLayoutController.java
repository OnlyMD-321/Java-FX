package org.example.pm.controller;

import java.io.IOException;
import java.util.Objects;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class MainLayoutController {

    @FXML
    private BorderPane mainBorderPane;

    public void setView(String fxmlPath) {
        try {
            // Clear previous content explicitly for good measure, though setCenter usually replaces.
            mainBorderPane.setCenter(null);
            Parent view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            // Show an error dialog to the user
            org.example.pm.util.AlertUtils.showError("Erreur de Chargement", "Impossible de charger la vue : " + fxmlPath + "\n" + e.getMessage());
        }
    }

    @FXML
    private void handleShowMedicamentView(ActionEvent event) {
        setView("/org/example/pm/MedicamentView.fxml");
    }

    @FXML
    private void handleShowClientView(ActionEvent event) {
        setView("/org/example/pm/ClientView.fxml");
    }

    @FXML
    private void handleShowNouvelleVenteView(ActionEvent event) {
        setView("/org/example/pm/NouvelleVenteView.fxml");
    }

    @FXML
    private void handleShowHistoriqueVentesView(ActionEvent event) {
        setView("/org/example/pm/HistoriqueVentesView.fxml");
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Platform.exit();
    }
}
