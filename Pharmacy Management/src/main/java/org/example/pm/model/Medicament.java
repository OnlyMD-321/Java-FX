package org.example.pm.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import javafx.beans.property.IntegerProperty; // Import LocalDate
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Medicament {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> prix = new SimpleObjectProperty<>();
    private final IntegerProperty quantiteStock = new SimpleIntegerProperty();
    private final IntegerProperty seuilAlerte = new SimpleIntegerProperty();
    private final StringProperty categorie = new SimpleStringProperty();
    private final StringProperty imagePath = new SimpleStringProperty(); // New property for image path
    private final ObjectProperty<LocalDate> earliestActiveExpiryDate = new SimpleObjectProperty<>(); // New property

    // Constructors
    public Medicament() {}

    public Medicament(String nom, String description, BigDecimal prix, int quantiteStock, int seuilAlerte, String categorie, String imagePath) {
        setNom(nom);
        setDescription(description);
        setPrix(prix);
        setQuantiteStock(quantiteStock);
        setSeuilAlerte(seuilAlerte);
        setCategorie(categorie);
        setImagePath(imagePath);
        // earliestActiveExpiryDate would typically be set by the DAO
    }


    // Getters and Setters for properties
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getNom() { return nom.get(); }
    public StringProperty nomProperty() { return nom; }
    public void setNom(String nom) { this.nom.set(nom); }

    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }
    public void setDescription(String description) { this.description.set(description); }

    public BigDecimal getPrix() { return prix.get(); }
    public ObjectProperty<BigDecimal> prixProperty() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix.set(prix); }

    public int getQuantiteStock() { return quantiteStock.get(); }
    public IntegerProperty quantiteStockProperty() { return quantiteStock; }
    public void setQuantiteStock(int quantiteStock) { this.quantiteStock.set(quantiteStock); }

    public int getSeuilAlerte() { return seuilAlerte.get(); }
    public IntegerProperty seuilAlerteProperty() { return seuilAlerte; }
    public void setSeuilAlerte(int seuilAlerte) { this.seuilAlerte.set(seuilAlerte); }

    public String getCategorie() { return categorie.get(); }
    public StringProperty categorieProperty() { return categorie; }
    public void setCategorie(String categorie) { this.categorie.set(categorie); }

    public String getImagePath() { return imagePath.get(); }
    public StringProperty imagePathProperty() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath.set(imagePath); }

    public LocalDate getEarliestActiveExpiryDate() { return earliestActiveExpiryDate.get(); }
    public ObjectProperty<LocalDate> earliestActiveExpiryDateProperty() { return earliestActiveExpiryDate; }
    public void setEarliestActiveExpiryDate(LocalDate date) { this.earliestActiveExpiryDate.set(date); }


    @Override
    public String toString() {
        return getNom();
    }
}