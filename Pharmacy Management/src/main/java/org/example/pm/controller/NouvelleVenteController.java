package org.example.pm.controller;

import org.example.pm.dao.ClientDAO;
import org.example.pm.dao.LotDAO;
import org.example.pm.dao.MedicamentDAO;
import org.example.pm.dao.VenteDAO;
import org.example.pm.model.*;
import org.example.pm.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Optional;

public class NouvelleVenteController {

    @FXML
    private ComboBox<Client> clientComboBox;
    @FXML
    private ComboBox<Medicament> medicamentComboBox;
    @FXML
    private ComboBox<Lot> lotComboBox;
    @FXML
    private Spinner<Integer> quantiteSpinner;
    @FXML
    private Label stockLotLabel;
    @FXML
    private Button ajouterAuPanierButton;

    @FXML
    private TableView<DetailVente> panierTable;
    @FXML
    private TableColumn<DetailVente, String> medicamentPanierColumn;
    @FXML
    private TableColumn<DetailVente, String> lotPanierColumn;
    @FXML
    private TableColumn<DetailVente, Integer> quantitePanierColumn;
    @FXML
    private TableColumn<DetailVente, BigDecimal> prixUnitairePanierColumn;
    @FXML
    private TableColumn<DetailVente, BigDecimal> sousTotalPanierColumn;
    @FXML
    private TableColumn<DetailVente, Void> actionPanierColumn;

    @FXML
    private Label montantTotalLabel;
    @FXML
    private Button annulerVenteButton;
    @FXML
    private Button validerVenteButton;

    private ClientDAO clientDAO;
    private MedicamentDAO medicamentDAO;
    private LotDAO lotDAO;
    private VenteDAO venteDAO;

    private ObservableList<Client> clientList;
    private ObservableList<Medicament> medicamentList;
    private ObservableList<Lot> lotListForSelectedMedicament;
    private ObservableList<DetailVente> panierItems = FXCollections.observableArrayList();

    public NouvelleVenteController() {
        clientDAO = new ClientDAO();
        medicamentDAO = new MedicamentDAO();
        lotDAO = new LotDAO();
        venteDAO = new VenteDAO();
    }

    @FXML
    public void initialize() {
        setupClientComboBox();
        setupMedicamentComboBox();
        setupLotComboBox();
        setupPanierTable();
        setupQuantiteSpinner();

        medicamentComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldMed, newMed) -> loadLotsForMedicament(newMed));
        lotComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldLot, newLot) -> updateStockLotLabel(newLot));

        updateMontantTotal();
    }

    private void setupClientComboBox() {
        clientList = clientDAO.getAllClients();
        // Add a "null" or "Anonymous" option
        Client anonymousClient = new Client();
        anonymousClient.setNom("(Vente Anonyme)");
        anonymousClient.setId(0); // Or some indicator for no client
        clientList.add(0, anonymousClient);

        clientComboBox.setItems(clientList);
        clientComboBox.setConverter(new StringConverter<Client>() {
            @Override
            public String toString(Client client) {
                return client == null || client.getId() == 0 ? "(Vente Anonyme)" : client.getNom() + " " + (client.getPrenom() != null ? client.getPrenom() : "");
            }

            @Override
            public Client fromString(String string) {
                return null;
                /* Not needed for ComboBox only display */ }
        });
        clientComboBox.getSelectionModel().selectFirst(); // Select anonymous by default
    }

    private void setupMedicamentComboBox() {
        medicamentList = medicamentDAO.getAllMedicaments(); // TODO: Filter out 0 stock medicaments?
        medicamentComboBox.setItems(medicamentList);
        medicamentComboBox.setConverter(new StringConverter<Medicament>() {
            @Override
            public String toString(Medicament medicament) {
                return medicament == null ? "" : medicament.getNom() + " (Stock: " + medicament.getQuantiteStock() + ")";
            }

            @Override
            public Medicament fromString(String string) {
                return null;
            }
        });
    }

    private void setupLotComboBox() {
        lotListForSelectedMedicament = FXCollections.observableArrayList();
        lotComboBox.setItems(lotListForSelectedMedicament);
        lotComboBox.setConverter(new StringConverter<Lot>() {
            @Override
            public String toString(Lot lot) {
                if (lot == null) {
                    return "";
                }
                return lot.getNumeroLot() + " (Exp: "
                        + (lot.getDateExpiration() != null ? lot.getDateExpiration().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy")) : "N/A")
                        + ", Qte: " + lot.getQuantite() + ")";
            }

            @Override
            public Lot fromString(String string) {
                return null;
            }
        });
    }

    private void setupQuantiteSpinner() {
        SpinnerValueFactory<Integer> valueFactory
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1); // min, max, initial
        quantiteSpinner.setValueFactory(valueFactory);
    }

    private void loadLotsForMedicament(Medicament medicament) {
        lotListForSelectedMedicament.clear();
        stockLotLabel.setText("Stock Lot: -");
        if (medicament != null) {
            ObservableList<Lot> lots = lotDAO.getLotsForMedicament(medicament.getId());
            // Filter out lots with 0 quantity or expired lots (FEFO - First Expired, First Out)
            lots.removeIf(lot -> lot.getQuantite() <= 0 || (lot.getDateExpiration() != null && lot.getDateExpiration().isBefore(LocalDate.now())));
            lots.sort((l1, l2) -> { // Sort by expiry date (FEFO)
                if (l1.getDateExpiration() == null && l2.getDateExpiration() == null) {
                    return 0;
                }
                if (l1.getDateExpiration() == null) {
                    return 1;
                }
                if (l2.getDateExpiration() == null) {
                    return -1;
                }
                return l1.getDateExpiration().compareTo(l2.getDateExpiration());
            });
            lotListForSelectedMedicament.addAll(lots);
            if (!lotListForSelectedMedicament.isEmpty()) {
                lotComboBox.getSelectionModel().selectFirst();
            }
        }
    }

    /**
     * Reloads the list of medicaments from the database and updates the ComboBox.
     */
    private void loadMedicaments() {
        medicamentList = medicamentDAO.getAllMedicaments();
        medicamentComboBox.setItems(medicamentList);
        medicamentComboBox.getSelectionModel().clearSelection();
        lotListForSelectedMedicament.clear();
        lotComboBox.getSelectionModel().clearSelection();
        stockLotLabel.setText("Stock Lot: -");
    }

    private void updateStockLotLabel(Lot lot) {
        if (lot != null) {
            stockLotLabel.setText("Stock Lot: " + lot.getQuantite());
            // Adjust spinner max based on lot quantity
            SpinnerValueFactory<Integer> valueFactory
                    = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, lot.getQuantite() > 0 ? lot.getQuantite() : 1, 1);
            quantiteSpinner.setValueFactory(valueFactory);
        } else {
            stockLotLabel.setText("Stock Lot: -");
            SpinnerValueFactory<Integer> valueFactory
                    = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1); // Default if no lot
            quantiteSpinner.setValueFactory(valueFactory);
        }
    }

    private void setupPanierTable() {
        medicamentPanierColumn.setCellValueFactory(new PropertyValueFactory<>("medicamentNom"));
        lotPanierColumn.setCellValueFactory(new PropertyValueFactory<>("lotNumero"));
        quantitePanierColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixUnitairePanierColumn.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        sousTotalPanierColumn.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));

        // Add delete button column
        Callback<TableColumn<DetailVente, Void>, TableCell<DetailVente, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<DetailVente, Void> call(final TableColumn<DetailVente, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Supprimer");

                    {
                        btn.setOnAction(event -> {
                            DetailVente item = getTableView().getItems().get(getIndex());
                            panierItems.remove(item);
                            updateMontantTotal();
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null); 
                        }else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };
        actionPanierColumn.setCellFactory(cellFactory);
        panierTable.setItems(panierItems);
    }

    @FXML
    private void handleAjouterAuPanier() {
        Medicament selectedMedicament = medicamentComboBox.getSelectionModel().getSelectedItem();
        Lot selectedLot = lotComboBox.getSelectionModel().getSelectedItem();
        int quantite = quantiteSpinner.getValue();

        if (selectedMedicament == null) {
            AlertUtils.showError("Erreur", "Veuillez sélectionner un médicament.");
            return;
        }
        if (selectedLot == null) {
            AlertUtils.showError("Erreur", "Veuillez sélectionner un lot pour ce médicament.");
            return;
        }
        if (quantite <= 0) {
            AlertUtils.showError("Erreur", "La quantité doit être supérieure à zéro.");
            return;
        }
        if (quantite > selectedLot.getQuantite()) {
            AlertUtils.showError("Stock Insuffisant", "La quantité demandée (" + quantite + ") dépasse le stock disponible pour ce lot (" + selectedLot.getQuantite() + ").");
            return;
        }

        // Check if this medicament (from this specific lot) is already in the cart
        for (DetailVente item : panierItems) {
            if (item.getMedicament().getId() == selectedMedicament.getId() && item.getLot().getId() == selectedLot.getId()) {
                int newQuantity = item.getQuantite() + quantite;
                if (newQuantity > selectedLot.getQuantite()) {
                    AlertUtils.showError("Stock Insuffisant", "La quantité totale (" + newQuantity + ") dépasse le stock du lot (" + selectedLot.getQuantite() + ").");
                    return;
                }
                item.setQuantite(newQuantity);
                panierTable.refresh();
                updateMontantTotal();
                return;
            }
        }

        // Add new item
        DetailVente newItem = new DetailVente(selectedMedicament, selectedLot, quantite, selectedMedicament.getPrix());
        panierItems.add(newItem);
        updateMontantTotal();

        // Reset selection for next item? Or clear?
        // quantiteSpinner.getValueFactory().setValue(1);
        // lotComboBox.getSelectionModel().clearSelection();
        // medicamentComboBox.getSelectionModel().clearSelection();
    }

    private void updateMontantTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (DetailVente item : panierItems) {
            total = total.add(item.getSousTotal());
        }
        montantTotalLabel.setText(String.format("%.2f €", total));
    }

    @FXML
    private void handleAnnulerVente() {
        Optional<ButtonType> result = AlertUtils.showConfirmation("Annuler Vente", "Êtes-vous sûr de vouloir annuler cette vente et vider le panier ?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            panierItems.clear();
            updateMontantTotal();
            clientComboBox.getSelectionModel().selectFirst(); // Reset client
            medicamentComboBox.getSelectionModel().clearSelection();
            lotComboBox.getItems().clear();
            stockLotLabel.setText("Stock Lot: -");
            quantiteSpinner.getValueFactory().setValue(1);
        }
    }

    @FXML
    private void handleValiderVente() {
        if (panierItems.isEmpty()) {
            AlertUtils.showError("Panier Vide", "Veuillez ajouter des articles au panier avant de valider.");
            return;
        }

        Vente nouvelleVente = new Vente();
        Client selectedClient = clientComboBox.getSelectionModel().getSelectedItem();
        if (selectedClient != null && selectedClient.getId() != 0) { // Check if not anonymous
            nouvelleVente.setClient(selectedClient);
        } else {
            nouvelleVente.setClient(null); // Explicitly set to null for anonymous
        }

        nouvelleVente.setDateVente(LocalDateTime.now());
        nouvelleVente.getDetailsVente().addAll(panierItems);

        BigDecimal totalCalculated = BigDecimal.ZERO;
        for (DetailVente dv : panierItems) {
            totalCalculated = totalCalculated.add(dv.getSousTotal());
        }
        nouvelleVente.setMontantTotal(totalCalculated);

        boolean success = venteDAO.addVente(nouvelleVente);

        if (success) {
            AlertUtils.showInformation("Vente Réussie", "La vente a été enregistrée avec succès. ID Vente: " + nouvelleVente.getId());
            // Refresh data that might have changed (medicament stock, lot quantities)
            loadMedicaments(); // Reload medicaments as their stock might have changed
            // loadLotsForMedicament(medicamentComboBox.getSelectionModel().getSelectedItem()); // Reload current lots if any selected

            // Clear for next sale
            handleAnnulerVente(); // This will clear the form
        } else {
            AlertUtils.showError("Échec de la Vente", "La vente n'a pas pu être enregistrée. Vérifiez les logs pour plus de détails.");
            // Potentially refresh lists to show current state if transaction failed partially or stock check is needed
            loadMedicaments();
        }
    }
}
