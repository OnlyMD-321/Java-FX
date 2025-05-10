package org.example.fxml;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class FormulaireController {
    @FXML
    private TextField nomField;
    @FXML
    private TextField emailField;
    @FXML
    private void handleSubmit() {
        String nom = nomField.getText();
        String email = emailField.getText();
        System.out.println("Nom: " + nom);
        System.out.println("Email: " + email);
    }
}