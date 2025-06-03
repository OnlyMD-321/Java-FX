package org.example.pm.model;

import java.time.LocalDate;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Client {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty prenom = new SimpleStringProperty();
    private final StringProperty telephone = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateCreation = new SimpleObjectProperty<>();

    // Optional: for displaying purchase history summary, not directly in DB table
    // private final StringProperty lastPurchaseDate = new SimpleStringProperty();
    // private final ObjectProperty<BigDecimal> totalSpent = new SimpleObjectProperty<>();


    public Client() {}

    public Client(String nom, String prenom, String telephone, String email, LocalDate dateCreation) {
        setNom(nom);
        setPrenom(prenom);
        setTelephone(telephone);
        setEmail(email);
        setDateCreation(dateCreation);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getNom() { return nom.get(); }
    public StringProperty nomProperty() { return nom; }
    public void setNom(String nom) { this.nom.set(nom); }

    public String getPrenom() { return prenom.get(); }
    public StringProperty prenomProperty() { return prenom; }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }

    public String getTelephone() { return telephone.get(); }
    public StringProperty telephoneProperty() { return telephone; }
    public void setTelephone(String telephone) { this.telephone.set(telephone); }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }
    public void setEmail(String email) { this.email.set(email); }

    public LocalDate getDateCreation() { return dateCreation.get(); }
    public ObjectProperty<LocalDate> dateCreationProperty() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation.set(dateCreation); }

    @Override
    public String toString() { // Useful for ComboBoxes
        return getNom() + (getPrenom() != null && !getPrenom().isEmpty() ? " " + getPrenom() : "");
    }
}