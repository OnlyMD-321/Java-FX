package org.example.pm.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.example.pm.dao.VenteDAO;
import org.example.pm.model.Client;
import org.example.pm.model.DetailVente;
import org.example.pm.model.Vente;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class HistoriqueVentesController {

    @FXML
    private TableView<Vente> ventesTable;
    @FXML
    private TableColumn<Vente, Integer> idVenteColumn;
    @FXML
    private TableColumn<Vente, LocalDateTime> dateVenteColumn;
    @FXML
    private TableColumn<Vente, Client> clientVenteColumn;
    @FXML
    private TableColumn<Vente, BigDecimal> montantTotalVenteColumn;

    @FXML
    private TableView<DetailVente> detailsVenteTable;
    @FXML
    private TableColumn<DetailVente, String> medicamentDetailColumn;
    @FXML
    private TableColumn<DetailVente, String> lotDetailColumn;
    @FXML
    private TableColumn<DetailVente, Integer> quantiteDetailColumn;
    @FXML
    private TableColumn<DetailVente, BigDecimal> prixUnitaireDetailColumn;
    @FXML
    private TableColumn<DetailVente, BigDecimal> sousTotalDetailColumn;

    private VenteDAO venteDAO;
    private ObservableList<Vente> venteList;

    public HistoriqueVentesController() {
        venteDAO = new VenteDAO();
    }

    @FXML
    public void initialize() {
        setupVentesTable();
        setupDetailsVenteTable();

        loadVentes();

        ventesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> loadDetailsForSelectedVente(newSelection)
        );
    }

    private void setupVentesTable() {
        idVenteColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateVenteColumn.setCellValueFactory(new PropertyValueFactory<>("dateVente"));
        clientVenteColumn.setCellValueFactory(new PropertyValueFactory<>("client")); // Uses Client object
        montantTotalVenteColumn.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));

        // Custom cell factory for date formatting
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        dateVenteColumn.setCellFactory(column -> new TableCell<Vente, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); 
                }else {
                    setText(formatter.format(item));
                }
            }
        });

        // Custom cell factory for client name or "Anonyme"
        clientVenteColumn.setCellFactory(column -> new TableCell<Vente, Client>() {
            @Override
            protected void updateItem(Client item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Vente Anonyme"); 
                }else {
                    setText(item.getNom() + (item.getPrenom() != null ? " " + item.getPrenom() : ""));
                }
            }
        });
    }

    private void setupDetailsVenteTable() {
        medicamentDetailColumn.setCellValueFactory(new PropertyValueFactory<>("medicamentNom"));
        lotDetailColumn.setCellValueFactory(new PropertyValueFactory<>("lotNumero"));
        quantiteDetailColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixUnitaireDetailColumn.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        sousTotalDetailColumn.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
    }

    private void loadVentes() {
        venteList = venteDAO.getAllVentes();
        ventesTable.setItems(venteList);
    }

    private void loadDetailsForSelectedVente(Vente vente) {
        if (vente != null) {
            ObservableList<DetailVente> details = venteDAO.getDetailsForVente(vente.getId());
            detailsVenteTable.setItems(details);
        } else {
            detailsVenteTable.getItems().clear();
        }
    }
}
