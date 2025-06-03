package org.example.pm.model;

import java.time.LocalDate;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Lot {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty medicamentId = new SimpleIntegerProperty();
    private final StringProperty numeroLot = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateExpiration = new SimpleObjectProperty<>();
    private final IntegerProperty quantite = new SimpleIntegerProperty();

    // To display medicament name in LotView if needed, though not strictly part of Lot table
    private final StringProperty medicamentNom = new SimpleStringProperty();


    public Lot() {}

    public Lot(int medicamentId, String numeroLot, LocalDate dateExpiration, int quantite) {
        setMedicamentId(medicamentId);
        setNumeroLot(numeroLot);
        setDateExpiration(dateExpiration);
        setQuantite(quantite);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public int getMedicamentId() { return medicamentId.get(); }
    public IntegerProperty medicamentIdProperty() { return medicamentId; }
    public void setMedicamentId(int medicamentId) { this.medicamentId.set(medicamentId); }

    public String getNumeroLot() { return numeroLot.get(); }
    public StringProperty numeroLotProperty() { return numeroLot; }
    public void setNumeroLot(String numeroLot) { this.numeroLot.set(numeroLot); }

    public LocalDate getDateExpiration() { return dateExpiration.get(); }
    public ObjectProperty<LocalDate> dateExpirationProperty() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration.set(dateExpiration); }

    public int getQuantite() { return quantite.get(); }
    public IntegerProperty quantiteProperty() { return quantite; }
    public void setQuantite(int quantite) { this.quantite.set(quantite); }

    public String getMedicamentNom() { return medicamentNom.get(); }
    public StringProperty medicamentNomProperty() { return medicamentNom; }
    public void setMedicamentNom(String medicamentNom) { this.medicamentNom.set(medicamentNom); }

    @Override
    public String toString() {
        return "Lot{" +
               "id=" + getId() +
               ", medicamentId=" + getMedicamentId() +
               ", numeroLot='" + getNumeroLot() + '\'' +
               ", dateExpiration=" + getDateExpiration() +
               ", quantite=" + getQuantite() +
               '}';
    }
}