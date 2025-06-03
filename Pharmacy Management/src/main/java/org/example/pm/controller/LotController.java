package org.example.pm.controller;

import java.time.LocalDate;
import java.util.Optional;

import org.example.pm.dao.LotDAO;
import org.example.pm.model.Lot;
import org.example.pm.model.Medicament;
import org.example.pm.util.AlertUtils;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class LotController {

    @FXML
    private Label medicamentNomLabel;
    @FXML
    private TableView<Lot> lotTable;
    @FXML
    private TableColumn<Lot, Integer> idLotColumn;
    @FXML
    private TableColumn<Lot, String> numeroLotColumn;
    @FXML
    private TableColumn<Lot, LocalDate> dateExpirationColumn;
    @FXML
    private TableColumn<Lot, Integer> quantiteLotColumn;

    @FXML
    private TextField numeroLotField;
    @FXML
    private DatePicker dateExpirationPicker;
    @FXML
    private TextField quantiteField;

    @FXML
    private Button nouveauLotButton;
    @FXML
    private Button ajouterLotButton;
    @FXML
    private Button modifierLotButton;
    @FXML
    private Button supprimerLotButton;

    private Stage dialogStage;
    private Medicament currentMedicament;
    private LotDAO lotDAO;
    private ObservableList<Lot> lotList;
    private boolean okClicked = false; // To track if changes were made that require parent refresh

    public LotController() {
        lotDAO = new LotDAO();
    }

    @FXML
    public void initialize() {
        idLotColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        numeroLotColumn.setCellValueFactory(new PropertyValueFactory<>("numeroLot"));
        dateExpirationColumn.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        quantiteLotColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        lotTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showLotDetails(newValue));

        // Initial button states
        modifierLotButton.setDisable(true);
        supprimerLotButton.setDisable(true);
        ajouterLotButton.setDisable(false);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMedicament(Medicament medicament) {
        this.currentMedicament = medicament;
        medicamentNomLabel.setText("Gestion des Lots pour: " + currentMedicament.getNom());
        loadLots();
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    private void loadLots() {
        if (currentMedicament != null) {
            lotList = lotDAO.getLotsForMedicament(currentMedicament.getId());
            lotTable.setItems(lotList);
        }
    }

    private void showLotDetails(Lot lot) {
        if (lot != null) {
            numeroLotField.setText(lot.getNumeroLot());
            dateExpirationPicker.setValue(lot.getDateExpiration());
            quantiteField.setText(String.valueOf(lot.getQuantite()));

            ajouterLotButton.setDisable(true);
            modifierLotButton.setDisable(false);
            supprimerLotButton.setDisable(false);
        } else {
            clearLotFields();
        }
    }

    @FXML
    private void handleNouveauLot() {
        lotTable.getSelectionModel().clearSelection();
        clearLotFields();
        numeroLotField.requestFocus();
    }

    @FXML
    private void handleAjouterLot() {
        if (isLotInputValid()) {
            Lot newLot = new Lot();
            newLot.setMedicamentId(currentMedicament.getId());
            newLot.setNumeroLot(numeroLotField.getText());
            newLot.setDateExpiration(dateExpirationPicker.getValue());
            newLot.setQuantite(Integer.parseInt(quantiteField.getText()));

            lotDAO.addLot(newLot);
            loadLots(); // Reload to see new lot and updated stock in medicament
            clearLotFields();
            okClicked = true; // Indicate that data has changed
            AlertUtils.showInformation("Succès", "Lot ajouté avec succès.");
        }
    }

    @FXML
    private void handleModifierLot() {
        Lot selectedLot = lotTable.getSelectionModel().getSelectedItem();
        if (selectedLot != null && isLotInputValid()) {
            selectedLot.setNumeroLot(numeroLotField.getText());
            selectedLot.setDateExpiration(dateExpirationPicker.getValue());
            selectedLot.setQuantite(Integer.parseInt(quantiteField.getText()));

            lotDAO.updateLot(selectedLot);
            lotTable.refresh();
            okClicked = true;
            AlertUtils.showInformation("Succès", "Lot modifié avec succès.");
        } else if (selectedLot == null) {
            AlertUtils.showError("Aucune sélection", "Veuillez sélectionner un lot à modifier.");
        }
    }

    @FXML
    private void handleSupprimerLot() {
        Lot selectedLot = lotTable.getSelectionModel().getSelectedItem();
        if (selectedLot != null) {
            Optional<ButtonType> result = AlertUtils.showConfirmation("Confirmation de suppression",
                    "Êtes-vous sûr de vouloir supprimer le lot: " + selectedLot.getNumeroLot() + "?");

            if (result.isPresent() && result.get() == ButtonType.OK) {
                lotDAO.deleteLot(selectedLot.getId(), selectedLot.getMedicamentId());
                lotList.remove(selectedLot);
                clearLotFields();
                okClicked = true;
                AlertUtils.showInformation("Succès", "Lot supprimé.");
            }
        } else {
            AlertUtils.showError("Aucune sélection", "Veuillez sélectionner un lot à supprimer.");
        }
    }

    private void clearLotFields() {
        numeroLotField.clear();
        dateExpirationPicker.setValue(null);
        quantiteField.clear();
        ajouterLotButton.setDisable(false);
        modifierLotButton.setDisable(true);
        supprimerLotButton.setDisable(true);
    }

    private boolean isLotInputValid() {
        String errorMessage = "";
        if (numeroLotField.getText() == null || numeroLotField.getText().trim().isEmpty()) {
            errorMessage += "Numéro de lot invalide!\n";
        }
        if (dateExpirationPicker.getValue() == null) {
            errorMessage += "Date d'expiration invalide!\n";
        } else if (dateExpirationPicker.getValue().isBefore(LocalDate.now())) {
            // Optionally allow past dates if needed for historical data, but generally a warning/error
            // errorMessage += "La date d'expiration ne peut pas être dans le passé!\n";
            AlertUtils.showInformation("Avertissement", "La date d'expiration est dans le passé.");
        }
        try {
            if (quantiteField.getText() == null || quantiteField.getText().trim().isEmpty()) {
                errorMessage += "Quantité invalide!\n";
            } else {
                int qte = Integer.parseInt(quantiteField.getText());
                if (qte < 0) {
                    errorMessage += "La quantité ne peut pas être négative!\n";
                }
            }
        } catch (NumberFormatException e) {
            errorMessage += "Quantité invalide (doit être un entier)!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            AlertUtils.showError("Champs Invalides pour le Lot", errorMessage);
            return false;
        }
    }

    @FXML
    private void handleFermer() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
