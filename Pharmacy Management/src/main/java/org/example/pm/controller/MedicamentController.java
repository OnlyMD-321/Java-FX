package org.example.pm.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.example.pm.dao.MedicamentDAO;
import org.example.pm.model.Medicament;
import org.example.pm.util.AlertUtils;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MedicamentController {

    @FXML
    private TableView<Medicament> medicamentTable;
    @FXML
    private TableColumn<Medicament, Integer> idColumn;
    @FXML
    private TableColumn<Medicament, String> nomColumn;
    @FXML
    private TableColumn<Medicament, String> categorieColumn;
    @FXML
    private TableColumn<Medicament, BigDecimal> prixColumn;
    @FXML
    private TableColumn<Medicament, Integer> stockColumn; // Displays total stock
    @FXML
    private TableColumn<Medicament, Integer> seuilColumn;
    @FXML
    private TableColumn<Medicament, String> descriptionColumn;
    @FXML
    private TableColumn<Medicament, Void> stockStatusColumn;
    @FXML
    private TableColumn<Medicament, LocalDate> expiryStatusColumn;

    @FXML
    private TextField nomField;
    @FXML
    private TextField categorieField;
    @FXML
    private TextField prixField;
    @FXML
    private TextField seuilAlerteField;
    @FXML
    private TextArea descriptionArea;

    @FXML
    private ImageView medicamentImageView;
    @FXML
    private Button selectImageButton;
    @FXML
    private Label imagePathLabel; // Hidden, to temporarily store selected image path

    @FXML
    private Button nouveauButton;
    @FXML
    private Button ajouterButton;
    @FXML
    private Button modifierButton;
    @FXML
    private Button supprimerButton;
    @FXML
    private Button gererLotsButton;
    @FXML
    private Label alerteLabel;

    private MedicamentDAO medicamentDAO;
    private ObservableList<Medicament> medicamentList;
    private static final String IMAGE_STORAGE_DIR = "medicament_images";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MedicamentController() {
        medicamentDAO = new MedicamentDAO();
        Path storagePath = Paths.get(IMAGE_STORAGE_DIR);
        if (!Files.exists(storagePath)) {
            try {
                Files.createDirectories(storagePath);
            } catch (IOException e) {
                System.err.println("Erreur Système: Impossible de créer le répertoire pour les images: " + e.getMessage());
                // AlertUtils might not be appropriate here if this is constructor phase and UI not ready
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
        seuilColumn.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        setupStockStatusColumn();
        setupExpiryStatusColumn();

        loadMedicaments();

        medicamentTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showMedicamentDetails(newValue));

        modifierButton.setDisable(true);
        supprimerButton.setDisable(true);
        gererLotsButton.setDisable(true);
        ajouterButton.setDisable(false);

        checkGeneralAlerts();
    }

    private void loadMedicaments() {
        if (medicamentDAO == null) {
            medicamentDAO = new MedicamentDAO(); // Defensive

                }medicamentList = medicamentDAO.getAllMedicaments();
        medicamentTable.setItems(medicamentList);
        // medicamentTable.refresh(); // Usually not strictly necessary after setItems
    }

    private void showMedicamentDetails(Medicament medicament) {
        if (medicament != null) {
            nomField.setText(medicament.getNom());
            categorieField.setText(medicament.getCategorie());
            prixField.setText(medicament.getPrix() != null ? medicament.getPrix().toString() : "");
            seuilAlerteField.setText(String.valueOf(medicament.getSeuilAlerte()));
            descriptionArea.setText(medicament.getDescription());

            imagePathLabel.setText(medicament.getImagePath());
            if (medicament.getImagePath() != null && !medicament.getImagePath().isEmpty()) {
                try {
                    File imgFile = new File(IMAGE_STORAGE_DIR, medicament.getImagePath());
                    if (imgFile.exists() && imgFile.isFile()) {
                        Image image = new Image(imgFile.toURI().toString());
                        medicamentImageView.setImage(image);
                    } else {
                        medicamentImageView.setImage(null);
                        if (!imgFile.exists()) {
                            System.err.println("Image file not found: " + imgFile.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    medicamentImageView.setImage(null);
                    System.err.println("Error loading image " + medicament.getImagePath() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                medicamentImageView.setImage(null);
            }

            ajouterButton.setDisable(true);
            modifierButton.setDisable(false);
            supprimerButton.setDisable(false);
            gererLotsButton.setDisable(false);
        } else {
            clearFields();
        }
    }

    @FXML
    private void handleNouveau() {
        medicamentTable.getSelectionModel().clearSelection();
        clearFields();
        nomField.requestFocus();
        ajouterButton.setDisable(false); // Enable ajouter when clearing for new
        modifierButton.setDisable(true);
        supprimerButton.setDisable(true);
        gererLotsButton.setDisable(true);
    }

        @FXML
    private void handleAjouter() {
        if (isInputValid()) {
            try {
                // Attempt to parse numbers first, as these are common input errors
                BigDecimal prix = new BigDecimal(prixField.getText().replace(",", "."));
                int seuil = Integer.parseInt(seuilAlerteField.getText().trim());

                Medicament newMedicament = new Medicament(
                        nomField.getText().trim(),
                        descriptionArea.getText().trim(),
                        prix,
                        0, // Initial stock managed by lots
                        seuil,
                        categorieField.getText().trim(),
                        imagePathLabel.getText()
                );

                medicamentDAO.addMedicament(newMedicament); // This can throw SQLException

                // If addMedicament was successful (no exception thrown):
                loadMedicaments();
                clearFields();
                checkGeneralAlerts();
                AlertUtils.showInformation("Succès", "Médicament ajouté avec succès. ID: " + newMedicament.getId());

            } catch (NumberFormatException e) {
                AlertUtils.showError("Erreur de Format", "Le prix ou le seuil d'alerte n'est pas un nombre valide. Vérifiez les entrées.");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur Base de Données", "Impossible d'ajouter le médicament : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleModifier() {
        Medicament selectedMedicament = medicamentTable.getSelectionModel().getSelectedItem();
        if (selectedMedicament == null) { // Moved this check up for clarity
            AlertUtils.showError("Aucune sélection", "Veuillez sélectionner un médicament à modifier.");
            return;
        }

        if (isInputValid()) {
            try {
                // Attempt to parse numbers first
                BigDecimal prix = new BigDecimal(prixField.getText().replace(",", "."));
                int seuil = Integer.parseInt(seuilAlerteField.getText().trim());

                // Keep a temporary copy of values in case update fails and we need to revert UI (more advanced)
                // Or, only update the model object *after* successful DB operation.
                // For now, we update the selectedMedicament object directly.

                selectedMedicament.setNom(nomField.getText().trim());
                selectedMedicament.setDescription(descriptionArea.getText().trim());
                selectedMedicament.setPrix(prix);
                selectedMedicament.setSeuilAlerte(seuil);
                selectedMedicament.setCategorie(categorieField.getText().trim());
                selectedMedicament.setImagePath(imagePathLabel.getText());

                medicamentDAO.updateMedicament(selectedMedicament); // This can throw SQLException

                // If updateMedicament was successful:
                // Efficiently update the item in the list instead of full reload
                int selectedIndex = medicamentList.indexOf(selectedMedicament); // Get index *before* potential modification by DAO
                
                // Re-fetch the updated medicament from DB to get latest state (e.g., if DB has triggers or computed fields)
                // and also to get the potentially updated earliestActiveExpiryDate
                Medicament updatedMedFromDB = medicamentDAO.getMedicamentById(selectedMedicament.getId());

                if (updatedMedFromDB != null && selectedIndex >= 0) {
                    medicamentList.set(selectedIndex, updatedMedFromDB); // Replace item in list
                } else {
                    // If medicament not found in list or DB (e.g., deleted by another user/process), reload all
                    loadMedicaments();
                    selectedIndex = -1; // Reset index as list is new
                    // Try to find and re-select by ID in the new list
                    Optional<Medicament> reselectOpt = medicamentList.stream().filter(m -> m.getId() == selectedMedicament.getId()).findFirst();
                    if (reselectOpt.isPresent()) {
                        Medicament reselectedMedicament = reselectOpt.get(); // Use a new variable inside the lambda
                        selectedIndex = medicamentList.indexOf(reselectedMedicament);
                    }
                }
                
                medicamentTable.refresh(); // Refresh visual representation of the table
                if (selectedIndex >= 0) {
                    medicamentTable.getSelectionModel().select(selectedIndex); // Re-select the item
                    medicamentTable.scrollTo(selectedIndex); // Scroll to the item
                } else {
                    medicamentTable.getSelectionModel().clearSelection();
                }

                checkGeneralAlerts();
                AlertUtils.showInformation("Succès", "Médicament modifié avec succès.");

            } catch (NumberFormatException e) {
                AlertUtils.showError("Erreur de Format", "Le prix ou le seuil d'alerte n'est pas un nombre valide. Vérifiez les entrées.");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur Base de Données", "Impossible de modifier le médicament : " + e.getMessage());
                // Optionally, reload data to revert UI if the in-memory selectedMedicament object was changed
                // but DB update failed.
                loadMedicaments(); // Refresh from DB to ensure UI consistency after error
            }
        }
    }

    @FXML
    private void handleSupprimer() {
        Medicament selectedMedicament = medicamentTable.getSelectionModel().getSelectedItem();
        if (selectedMedicament != null) {
            Optional<ButtonType> result = AlertUtils.showConfirmation("Confirmation de suppression",
                    "Êtes-vous sûr de vouloir supprimer le médicament: " + selectedMedicament.getNom() + "?\n"
                    + "Cela supprimera également tous ses lots associés (si la base de données est configurée avec ON DELETE CASCADE pour les lots).\n"
                    + "La suppression échouera si le médicament est référencé dans des ventes.");

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // Delete associated image file if it exists
                    if (selectedMedicament.getImagePath() != null && !selectedMedicament.getImagePath().isEmpty()) {
                        try {
                            Path imageFile = Paths.get(IMAGE_STORAGE_DIR, selectedMedicament.getImagePath());
                            Files.deleteIfExists(imageFile);
                        } catch (IOException e) {
                            System.err.println("Avertissement: Impossible de supprimer le fichier image " + selectedMedicament.getImagePath() + ": " + e.getMessage());
                            // Log error but continue with DB deletion
                        }
                    }
                    medicamentDAO.deleteMedicament(selectedMedicament.getId());
                    // loadMedicaments(); // Reload the list from DB more robustly
                    medicamentList.remove(selectedMedicament); // Optimistic UI update
                    clearFields();
                    checkGeneralAlerts();
                    AlertUtils.showInformation("Succès", "Médicament supprimé.");
                } catch (SQLException e) {
                    AlertUtils.showError("Erreur de Suppression", e.getMessage()); // DAO provides specific error
                    loadMedicaments(); // Refresh from DB to ensure UI consistency after error
                }
            }
        } else {
            AlertUtils.showError("Aucune sélection", "Veuillez sélectionner un médicament à supprimer.");
        }
    }

    @FXML
    private void handleGererLots() {
        Medicament selectedMedicament = medicamentTable.getSelectionModel().getSelectedItem();
        if (selectedMedicament == null) {
            // Assuming AlertUtils is in org.example.pm.util
            org.example.pm.util.AlertUtils.showError("Aucun médicament sélectionné", "Veuillez sélectionner un médicament pour gérer ses lots.");
            return;
        }
        try {
            // ----- THIS IS THE MOST CRITICAL LINE TO VERIFY -----
            String fxmlPath = "/org/example/pm/LotView.fxml"; // Path based on your error message
            // String fxmlPath = "/org/example/pm/fxml/LotView.fxml"; // USE THIS IF IT'S IN AN 'fxml' SUBFOLDER

            System.out.println("Attempting to load FXML from: " + fxmlPath); // DEBUG LINE
            java.net.URL fxmlUrl = getClass().getResource(fxmlPath);

            if (fxmlUrl == null) {
                System.err.println("FXML resource not found at path: " + fxmlPath);
                org.example.pm.util.AlertUtils.showError("Erreur Fichier FXML",
                        "La ressource FXML LotView.fxml n'a pas été trouvée à l'emplacement attendu:\n" + fxmlPath +
                        "\nVérifiez la structure de votre projet et le chemin dans le code.");
                return;
            }

            System.out.println("FXML Resource URL found: " + fxmlUrl.toExternalForm()); // DEBUG LINE

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            // Assuming LotView.fxml's root is AnchorPane. Change if different.
            AnchorPane lotViewPage = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Gestion des Lots pour: " + selectedMedicament.getNom());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Stage ownerStage = (Stage) gererLotsButton.getScene().getWindow(); // Ensure gererLotsButton is not null
            if (ownerStage == null) {
                System.err.println("Owner stage could not be determined. 'gererLotsButton' might be null or not part of a scene.");
                // Fallback or error
                return;
            }
            dialogStage.initOwner(ownerStage);
            Scene scene = new Scene(lotViewPage);
            dialogStage.setScene(scene);

            // Ensure LotController is in org.example.pm.controller package
            LotController controller = loader.getController();
            if (controller == null) {
                 org.example.pm.util.AlertUtils.showError("Erreur Critique",
                         "Le contrôleur pour LotView (attendu: org.example.pm.controller.LotController) n'a pas pu être chargé.\n" +
                         "Vérifiez l'attribut fx:controller dans votre fichier LotView.fxml.");
                 return;
            }
            controller.setDialogStage(dialogStage);
            controller.setMedicament(selectedMedicament);
            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                int selectedId = selectedMedicament.getId();
                loadMedicaments();
                medicamentTable.getItems().stream()
                    .filter(m -> m.getId() == selectedId)
                    .findFirst()
                    .ifPresent(medToSelect -> {
                        medicamentTable.getSelectionModel().select(medToSelect);
                        medicamentTable.scrollTo(medToSelect);
                    });
                checkGeneralAlerts();
            }
        } catch (IOException e) {
            e.printStackTrace(); // Print full stack trace to console
            org.example.pm.util.AlertUtils.showError("Erreur de chargement FXML (IOException)",
                    "Impossible de charger la vue des lots.\n" +
                    "Fichier: " + (getClass().getResource("/org/example/pm/LotView.fxml") != null ? "/org/example/pm/LotView.fxml" : "CHEMIN_INCORRECT_OU_FICHIER_MANQUANT") + "\n" +
                    "Message: " + e.getMessage());
        } catch (IllegalStateException e) {
             e.printStackTrace(); // Print full stack trace to console
            org.example.pm.util.AlertUtils.showError("Erreur de chargement FXML (IllegalStateException)",
                    "Erreur interne lors du chargement de la vue des lots (Problème FXML ou liaison contrôleur).\n" +
                    "Message: " + e.getMessage());
        } catch (Exception e) { // Catch any other unexpected exceptions
            e.printStackTrace();
            org.example.pm.util.AlertUtils.showError("Erreur Inattendue",
                    "Une erreur inattendue est survenue lors du chargement de la vue des lots.\n" +
                    "Message: " + e.getMessage());
        }
    }

    // Make sure these methods are present and correct
    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image pour le médicament");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        File selectedFile = fileChooser.showOpenDialog(selectImageButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                Path sourcePath = selectedFile.toPath();
                String originalFileName = selectedFile.getName();
                String fileExtension = "";
                int i = originalFileName.lastIndexOf('.');
                if (i > 0) {
                    fileExtension = originalFileName.substring(i); // .jpg, .png
                }
                String fileName = System.currentTimeMillis() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "") + fileExtension; // Sanitize and make unique
                fileName = fileName.length() > 100 ? fileName.substring(fileName.length() - 100) : fileName; // Limit length

                Path destinationPath = Paths.get(IMAGE_STORAGE_DIR, fileName);
                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

                imagePathLabel.setText(fileName);
                Image image = new Image(destinationPath.toUri().toString());
                medicamentImageView.setImage(image);

            } catch (IOException e) {
                e.printStackTrace();
                AlertUtils.showError("Erreur Fichier", "Impossible de copier l'image: " + e.getMessage());
            }
        }
    }

    private void clearFields() {
        nomField.clear();
        descriptionArea.clear();
        prixField.clear();
        seuilAlerteField.clear();
        categorieField.clear();
        medicamentImageView.setImage(null);
        imagePathLabel.setText("");

        ajouterButton.setDisable(false);
        modifierButton.setDisable(true);
        supprimerButton.setDisable(true);
        gererLotsButton.setDisable(true);
        medicamentTable.getSelectionModel().clearSelection();
    }

    private boolean isInputValid() {
        String errorMessage = "";
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errorMessage += "Nom invalide!\n";
        }
        if (categorieField.getText() == null || categorieField.getText().trim().isEmpty()) {
            errorMessage += "Catégorie invalide!\n";
        }
        try {
            String prixStr = prixField.getText().replace(",", ".");
            if (prixStr == null || prixStr.trim().isEmpty()) {
                errorMessage += "Prix invalide!\n"; 
            }else {
                BigDecimal prix = new BigDecimal(prixStr);
                if (prix.compareTo(BigDecimal.ZERO) < 0) {
                    errorMessage += "Le prix ne peut pas être négatif.\n";
                }
            }
        } catch (NumberFormatException e) {
            errorMessage += "Prix invalide (doit être un nombre)!\n";
        }
        try {
            if (seuilAlerteField.getText() == null || seuilAlerteField.getText().trim().isEmpty()) {
                errorMessage += "Seuil d'alerte invalide!\n"; 
            }else {
                int seuil = Integer.parseInt(seuilAlerteField.getText().trim());
                if (seuil < 0) {
                    errorMessage += "Le seuil d'alerte ne peut pas être négatif.\n";
                }
            }
        } catch (NumberFormatException e) {
            errorMessage += "Seuil d'alerte invalide (doit être un entier)!\n";
        }

        if (errorMessage.isEmpty()) {
            return true; 
        }else {
            AlertUtils.showError("Champs Invalides", errorMessage);
            return false;
        }
    }

    private void checkGeneralAlerts() {
        StringBuilder alertMessage = new StringBuilder();
        boolean lowStockFound = false;
        boolean expiryAlertFound = false;
        LocalDate today = LocalDate.now();
        LocalDate soonExpiryLimit = today.plusMonths(1); // Example: 1 month for "soon"

        if (medicamentList == null) {
            return; // Guard against null list
        }
        for (Medicament med : medicamentList) {
            if (med == null) {
                continue; // Guard against null medicament in list
            }            // Stock Alert
            if (med.getQuantiteStock() <= med.getSeuilAlerte()) {
                if (!lowStockFound) {
                    alertMessage.append("ALERTE STOCK FAIBLE:\n");
                }
                alertMessage.append("- ").append(med.getNom()).append(" (Stock: ").append(med.getQuantiteStock()).append(", Seuil: ").append(med.getSeuilAlerte()).append(")\n");
                lowStockFound = true;
            }
            // Expiry Alert
            if (med.getEarliestActiveExpiryDate() != null) {
                if (med.getEarliestActiveExpiryDate().isBefore(today)) {
                    if (!expiryAlertFound && !lowStockFound) {
                        alertMessage.append((lowStockFound ? "\n" : "") + "ALERTE EXPIRATION:\n"); 
                    }else if (!expiryAlertFound) {
                        alertMessage.append("\nALERTE EXPIRATION:\n");
                    }
                    alertMessage.append("- ").append(med.getNom()).append(" (Expiré le: ").append(med.getEarliestActiveExpiryDate().format(dateFormatter)).append(")\n");
                    expiryAlertFound = true;
                } else if (!med.getEarliestActiveExpiryDate().isAfter(soonExpiryLimit)) {
                    if (!expiryAlertFound && !lowStockFound) {
                        alertMessage.append((lowStockFound ? "\n" : "") + "ALERTE EXPIRATION PROCHE:\n"); 
                    }else if (!expiryAlertFound) {
                        alertMessage.append("\nALERTE EXPIRATION PROCHE:\n");
                    }
                    alertMessage.append("- ").append(med.getNom()).append(" (Expire le: ").append(med.getEarliestActiveExpiryDate().format(dateFormatter)).append(")\n");
                    expiryAlertFound = true;
                }
            }
        }

        if (lowStockFound || expiryAlertFound) {
            alerteLabel.setText(alertMessage.toString().trim());
            alerteLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            alerteLabel.setText("Aucune alerte de stock ou d'expiration.");
            alerteLabel.setStyle("-fx-text-fill: green; -fx-font-weight: normal;");
        }
    }

    private void setupStockStatusColumn() {
        stockStatusColumn.setCellFactory(param -> new TableCell<Medicament, Void>() {
            private final StackPane pane = new StackPane();
            private final Rectangle indicator = new Rectangle(80, 15);
            private final Label textLabel = new Label();

            {
                pane.getChildren().addAll(indicator, textLabel);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Medicament med = (Medicament) getTableRow().getItem();
                    int stock = med.getQuantiteStock();
                    int seuil = med.getSeuilAlerte();
                    String statusText;

                    if (stock <= 0) {
                        indicator.setFill(Color.DARKRED);
                        statusText = "Rupture";
                    } else if (stock <= seuil) {
                        indicator.setFill(Color.ORANGERED);
                        statusText = "Faible";
                    } else if (stock <= seuil * 1.5) {
                        indicator.setFill(Color.GOLD);
                        statusText = "Attention";
                    } else {
                        indicator.setFill(Color.LIGHTGREEN);
                        statusText = "OK";
                    }
                    textLabel.setText(statusText);
                    textLabel.setTextFill(Color.BLACK); // Ensure text is visible on colored background
                    textLabel.setStyle("-fx-font-size: 10px;"); // Adjust font size if needed
                    setGraphic(pane);
                }
            }
        });
    }

    private void setupExpiryStatusColumn() {
        expiryStatusColumn.setCellValueFactory(new PropertyValueFactory<>("earliestActiveExpiryDate"));
        expiryStatusColumn.setCellFactory(column -> new TableCell<Medicament, LocalDate>() {
            @Override
            protected void updateItem(LocalDate itemDate, boolean empty) {
                super.updateItem(itemDate, empty);
                if (empty || itemDate == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(itemDate.format(dateFormatter));
                    LocalDate today = LocalDate.now();
                    LocalDate soonLimit = today.plusMonths(1);

                    if (itemDate.isBefore(today)) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        setText(getText() + " (Expiré)");
                    } else if (!itemDate.isAfter(soonLimit)) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        setText(getText() + " (Proche)");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });
    }
}
