package org.example.pm.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Vente {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final ObjectProperty<Client> client = new SimpleObjectProperty<>(); // Store Client object
    private final IntegerProperty clientId = new SimpleIntegerProperty(); // For DB storage
    private final ObjectProperty<LocalDateTime> dateVente = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> montantTotal = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObservableList<DetailVente> detailsVente = FXCollections.observableArrayList();

    public Vente() {}

    // Getters, Setters, Property methods
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public Client getClient() { return client.get(); }
    public ObjectProperty<Client> clientProperty() { return client; }
    public void setClient(Client client) {
        this.client.set(client);
        if (client != null) {
            this.clientId.set(client.getId());
        } else {
            this.clientId.set(0); // Or handle null client ID appropriately
        }
    }

    public int getClientId() { return clientId.get(); }
    public IntegerProperty clientIdProperty() { return clientId; }
    public void setClientId(int clientId) { this.clientId.set(clientId); }


    public LocalDateTime getDateVente() { return dateVente.get(); }
    public ObjectProperty<LocalDateTime> dateVenteProperty() { return dateVente; }
    public void setDateVente(LocalDateTime dateVente) { this.dateVente.set(dateVente); }

    public BigDecimal getMontantTotal() { return montantTotal.get(); }
    public ObjectProperty<BigDecimal> montantTotalProperty() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal.set(montantTotal); }

    public ObservableList<DetailVente> getDetailsVente() { return detailsVente; }
}
