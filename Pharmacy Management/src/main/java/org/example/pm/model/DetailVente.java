package org.example.pm.model;


import java.math.BigDecimal;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DetailVente {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty venteId = new SimpleIntegerProperty();
    private final ObjectProperty<Medicament> medicament = new SimpleObjectProperty<>();
    private final IntegerProperty medicamentId = new SimpleIntegerProperty();
    private final ObjectProperty<Lot> lot = new SimpleObjectProperty<>(); // Store Lot object
    private final IntegerProperty lotId = new SimpleIntegerProperty();
    private final IntegerProperty quantite = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> prixUnitaire = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> sousTotal = new SimpleObjectProperty<>();

    // For display in TableView
    private final StringProperty medicamentNom = new SimpleStringProperty();
    private final StringProperty lotNumero = new SimpleStringProperty();


    public DetailVente() {}

    public DetailVente(Medicament medicament, Lot lot, int quantite, BigDecimal prixUnitaire) {
        setMedicament(medicament);
        setLot(lot);
        setQuantite(quantite);
        setPrixUnitaire(prixUnitaire);
        calculateSousTotal();
    }


    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public int getVenteId() { return venteId.get(); }
    public IntegerProperty venteIdProperty() { return venteId; }
    public void setVenteId(int venteId) { this.venteId.set(venteId); }

    public Medicament getMedicament() { return medicament.get(); }
    public ObjectProperty<Medicament> medicamentProperty() { return medicament; }
    public void setMedicament(Medicament medicament) {
        this.medicament.set(medicament);
        if (medicament != null) {
            this.medicamentId.set(medicament.getId());
            this.medicamentNom.set(medicament.getNom());
        }
    }

    public int getMedicamentId() { return medicamentId.get(); }
    public IntegerProperty medicamentIdProperty() { return medicamentId; }
    // public void setMedicamentId(int medicamentId) { this.medicamentId.set(medicamentId); }


    public Lot getLot() { return lot.get(); }
    public ObjectProperty<Lot> lotProperty() { return lot; }
    public void setLot(Lot lot) {
        this.lot.set(lot);
        if (lot != null) {
            this.lotId.set(lot.getId());
            this.lotNumero.set(lot.getNumeroLot());
        }
    }

    public int getLotId() { return lotId.get(); }
    public IntegerProperty lotIdProperty() { return lotId; }
    // public void setLotId(int lotId) { this.lotId.set(lotId); }


    public int getQuantite() { return quantite.get(); }
    public IntegerProperty quantiteProperty() { return quantite; }
    public void setQuantite(int quantite) {
        this.quantite.set(quantite);
        calculateSousTotal();
    }

    public BigDecimal getPrixUnitaire() { return prixUnitaire.get(); }
    public ObjectProperty<BigDecimal> prixUnitaireProperty() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire.set(prixUnitaire);
        calculateSousTotal();
    }

    public BigDecimal getSousTotal() { return sousTotal.get(); }
    public ObjectProperty<BigDecimal> sousTotalProperty() { return sousTotal; }

    public String getMedicamentNom() { return medicamentNom.get(); }
    public StringProperty medicamentNomProperty() { return medicamentNom; }

    public String getLotNumero() { return lotNumero.get(); }
    public StringProperty lotNumeroProperty() { return lotNumero; }

    private void calculateSousTotal() {
        if (getPrixUnitaire() != null && getQuantite() > 0) {
            this.sousTotal.set(getPrixUnitaire().multiply(BigDecimal.valueOf(getQuantite())));
        } else {
            this.sousTotal.set(BigDecimal.ZERO);
        }
    }
}
