package org.example.pm.controller;

import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;

import org.example.pm.dao.ClientDAO;
import org.example.pm.model.Client;
import org.example.pm.util.AlertUtils;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ClientController {

    @FXML private TableView<Client> clientTable;
    @FXML private TableColumn<Client, Integer> idClientColumn;
    @FXML private TableColumn<Client, String> nomClientColumn;
    @FXML private TableColumn<Client, String> prenomClientColumn;
    @FXML private TableColumn<Client, String> telephoneClientColumn;
    @FXML private TableColumn<Client, String> emailClientColumn;
    @FXML private TableColumn<Client, LocalDate> dateCreationClientColumn;

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private DatePicker dateCreationPicker;

    @FXML private Button nouveauClientButton;
    @FXML private Button ajouterClientButton;
    @FXML private Button modifierClientButton;
    @FXML private Button supprimerClientButton;
    // @FXML private Button historiqueAchatsButton;


    private ClientDAO clientDAO;
    private ObservableList<Client> clientList;

    // Basic email validation regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );


    public ClientController() {
        clientDAO = new ClientDAO();
    }

    @FXML
    public void initialize() {
        idClientColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomClientColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomClientColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        telephoneClientColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        emailClientColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        dateCreationClientColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        // Format date in table
        dateCreationClientColumn.setCellFactory(column -> new TableCell<Client, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });


        loadClients();

        clientTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showClientDetails(newValue));

        // Initial state of buttons
        modifierClientButton.setDisable(true);
        supprimerClientButton.setDisable(true);
        // historiqueAchatsButton.setDisable(true);
        ajouterClientButton.setDisable(false);
        dateCreationPicker.setValue(LocalDate.now()); // Default for new client
        dateCreationPicker.setDisable(true); // Date creation is usually not user-editable once set
    }

    private void loadClients() {
        clientList = clientDAO.getAllClients();
        clientTable.setItems(clientList);
    }

    private void showClientDetails(Client client) {
        if (client != null) {
            nomField.setText(client.getNom());
            prenomField.setText(client.getPrenom());
            telephoneField.setText(client.getTelephone());
            emailField.setText(client.getEmail());
            dateCreationPicker.setValue(client.getDateCreation());

            ajouterClientButton.setDisable(true);
            modifierClientButton.setDisable(false);
            supprimerClientButton.setDisable(false);
            // historiqueAchatsButton.setDisable(false);
        } else {
            clearClientFields();
        }
    }

    @FXML
    private void handleNouveauClient() {
        clientTable.getSelectionModel().clearSelection();
        clearClientFields();
        nomField.requestFocus();
    }

    @FXML
    private void handleAjouterClient() {
        if (isClientInputValid()) {
            Client newClient = new Client(
                    nomField.getText().trim(),
                    prenomField.getText() != null ? prenomField.getText().trim() : null,
                    telephoneField.getText() != null ? telephoneField.getText().trim() : null,
                    emailField.getText() != null ? emailField.getText().trim() : null,
                    LocalDate.now() // dateCreation is always now for new clients
            );
            clientDAO.addClient(newClient);
            loadClients(); // Reload to see the new entry with ID
            clearClientFields();
            AlertUtils.showInformation("Succès", "Client ajouté avec succès.");
        }
    }

    @FXML
    private void handleModifierClient() {
        Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
        if (selectedClient != null && isClientInputValid()) {
            selectedClient.setNom(nomField.getText().trim());
            selectedClient.setPrenom(prenomField.getText() != null ? prenomField.getText().trim() : null);
            selectedClient.setTelephone(telephoneField.getText() != null ? telephoneField.getText().trim() : null);
            selectedClient.setEmail(emailField.getText() != null ? emailField.getText().trim() : null);
            // selectedClient.setDateCreation(dateCreationPicker.getValue()); // Usually not modified

            clientDAO.updateClient(selectedClient);
            clientTable.refresh(); // Refresh the table view
            AlertUtils.showInformation("Succès", "Client modifié avec succès.");

        } else if (selectedClient == null) {
            AlertUtils.showError("Aucune sélection", "Veuillez sélectionner un client à modifier.");
        }
    }

    @FXML
    private void handleSupprimerClient() {
        Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
        if (selectedClient != null) {
            Optional<ButtonType> result = AlertUtils.showConfirmation("Confirmation de suppression",
                    "Êtes-vous sûr de vouloir supprimer le client: " + selectedClient.getNom() + " " + (selectedClient.getPrenom() != null ? selectedClient.getPrenom() : "") + "?\n" +
                            "Ses ventes associées ne seront pas supprimées mais ne lui seront plus attribuées.");

            if (result.isPresent() && result.get() == ButtonType.OK) {
                clientDAO.deleteClient(selectedClient.getId());
                clientList.remove(selectedClient);
                clearClientFields();
                AlertUtils.showInformation("Succès", "Client supprimé.");
            }
        } else {
            AlertUtils.showError("Aucune sélection", "Veuillez sélectionner un client à supprimer.");
        }
    }

    /*
    @FXML
    private void handleHistoriqueAchats() {
        Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
        if (selectedClient != null) {
            // TODO: Implement opening a new window/view to show purchase history
            AlertUtils.showInformation("Historique", "Affichage de l'historique pour " + selectedClient.getNom());
        }
    }
    */

    private void clearClientFields() {
        nomField.clear();
        prenomField.clear();
        telephoneField.clear();
        emailField.clear();
        dateCreationPicker.setValue(LocalDate.now()); // Reset to today for potential new client

        ajouterClientButton.setDisable(false);
        modifierClientButton.setDisable(true);
        supprimerClientButton.setDisable(true);
        // historiqueAchatsButton.setDisable(true);
    }

    private boolean isClientInputValid() {
        String errorMessage = "";

        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errorMessage += "Le nom du client est obligatoire!\n";
        }
        // Prénom is optional, no validation needed other than trim

        String email = emailField.getText();
        if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errorMessage += "Format d'email invalide!\n";
        }

        String telephone = telephoneField.getText();
        if (telephone != null && !telephone.trim().isEmpty()) {
            // Basic phone validation: allow digits, spaces, +, -
            if (!telephone.trim().matches("^[\\d\\s\\+\\-()]*$")) {
                 errorMessage += "Format de téléphone invalide (chiffres, espaces, +, - autorisés)!\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            AlertUtils.showError("Champs Invalides", errorMessage);
            return false;
        }
    }
}