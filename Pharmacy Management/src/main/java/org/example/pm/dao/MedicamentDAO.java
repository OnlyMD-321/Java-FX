package org.example.pm.dao; // Changed package

import org.example.pm.model.Medicament; // Changed import
import org.example.pm.dao.DatabaseConnection; // Added import

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.math.BigDecimal; // Added for prix

public class MedicamentDAO {

    public ObservableList<Medicament> getAllMedicaments() {
        ObservableList<Medicament> medicaments = FXCollections.observableArrayList();
        // Query to fetch medicaments along with the earliest expiry date of their *active and non-expired* lots
        String query = "SELECT m.*, " +
                       " (SELECT MIN(l.date_expiration) FROM lots l WHERE l.medicament_id = m.id AND l.quantite > 0 AND l.date_expiration >= CURDATE()) AS earliest_upcoming_expiry_date " +
                       "FROM medicaments m ORDER BY m.nom ASC"; // Added ORDER BY
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Medicament med = new Medicament();
                med.setId(rs.getInt("id"));
                med.setNom(rs.getString("nom"));
                med.setDescription(rs.getString("description"));
                med.setPrix(rs.getBigDecimal("prix"));
                med.setQuantiteStock(rs.getInt("quantite_stock")); // This is updated by LotDAO
                med.setSeuilAlerte(rs.getInt("seuil_alerte"));
                med.setCategorie(rs.getString("categorie"));
                med.setImagePath(rs.getString("image_path"));

                Date expiryDateSql = rs.getDate("earliest_upcoming_expiry_date");
                if (expiryDateSql != null) {
                    med.setEarliestActiveExpiryDate(expiryDateSql.toLocalDate()); // Using the existing property name
                } else {
                    med.setEarliestActiveExpiryDate(null); // Explicitly set to null if no upcoming expiry
                }
                medicaments.add(med);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération de tous les médicaments: " + e.getMessage());
            e.printStackTrace(); // For detailed logging during development
            // In a production app, consider throwing a custom DataAccessException
        }
        return medicaments;
    }

    public void addMedicament(Medicament medicament) throws SQLException {
        // quantite_stock is managed by LotDAO. It should be 0 for a new medicament.
        String query = "INSERT INTO medicaments (nom, description, prix, quantite_stock, seuil_alerte, categorie, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, medicament.getNom());
            ps.setString(2, medicament.getDescription());
            if (medicament.getPrix() == null) {
                 throw new SQLException("Le prix du médicament ne peut pas être nul.");
            }
            ps.setBigDecimal(3, medicament.getPrix());
            ps.setInt(4, 0); // Initial stock is 0, updated via Lots
            ps.setInt(5, medicament.getSeuilAlerte());
            ps.setString(6, medicament.getCategorie());
            ps.setString(7, medicament.getImagePath());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    medicament.setId(generatedKeys.getInt(1)); // Update the medicament object with the new ID
                } else {
                    throw new SQLException("Échec de la création du médicament, aucun ID généré obtenu.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'ajout du médicament '" + medicament.getNom() + "': " + e.getMessage());
            throw e; // Re-throw for the controller to handle
        }
    }

    public void updateMedicament(Medicament medicament) throws SQLException {
        // quantite_stock is managed by LotDAO and should not be updated here directly.
        String query = "UPDATE medicaments SET nom = ?, description = ?, prix = ?, seuil_alerte = ?, categorie = ?, image_path = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, medicament.getNom());
            ps.setString(2, medicament.getDescription());
            if (medicament.getPrix() == null) {
                 throw new SQLException("Le prix du médicament ne peut pas être nul.");
            }
            ps.setBigDecimal(3, medicament.getPrix());
            ps.setInt(4, medicament.getSeuilAlerte());
            ps.setString(5, medicament.getCategorie());
            ps.setString(6, medicament.getImagePath());
            ps.setInt(7, medicament.getId());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de la mise à jour du médicament, aucun médicament trouvé avec ID: " + medicament.getId());
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la mise à jour du médicament ID " + medicament.getId() + ": " + e.getMessage());
            throw e;
        }
    }

    public void deleteMedicament(int medicamentId) throws SQLException {
        // If 'lots' table has ON DELETE CASCADE for medicament_id,
        // explicit deletion of lots is not strictly necessary here but doesn't hurt.
        // The main concern is integrity constraints with 'details_vente'.

        String deleteLotsQuery = "DELETE FROM lots WHERE medicament_id = ?"; // Redundant if ON DELETE CASCADE
        String deleteMedicamentQuery = "DELETE FROM medicaments WHERE id = ?";
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Optional: Explicitly delete lots (if ON DELETE CASCADE is not trusted or for logging)
            // try (PreparedStatement psLots = conn.prepareStatement(deleteLotsQuery)) {
            //     psLots.setInt(1, medicamentId);
            //     psLots.executeUpdate();
            // }

            // Delete medicament
            try (PreparedStatement psMed = conn.prepareStatement(deleteMedicamentQuery)) {
                psMed.setInt(1, medicamentId);
                int affectedRows = psMed.executeUpdate();
                if (affectedRows == 0) {
                    // This can happen if the medicament was already deleted or the ID is wrong.
                    // If it's due to FK constraint (e.g., referenced in details_vente),
                    // an SQLException with SQLState '23xxx' would likely be thrown before this.
                    conn.rollback(); // Rollback, as the intended operation didn't complete.
                    // It's better to let SQLException from FK violation bubble up naturally.
                    throw new SQLException("Échec de la suppression du médicament, aucun médicament trouvé avec ID: " + medicamentId + ", ou il est référencé ailleurs.");
                }
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la suppression du médicament ID " + medicamentId + ": " + e.getMessage() + " (SQLState: " + e.getSQLState() +")");
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur SQL lors du rollback de la suppression du médicament: " + ex.getMessage());
                }
            }
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) { // Integrity constraint violation (e.g., 23000, 23503)
                throw new SQLException("Impossible de supprimer le médicament (ID: " + medicamentId + ") car il est référencé dans l'historique des ventes.", e.getSQLState(), e);
            }
            throw e; // Re-throw original or new specific exception
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Erreur SQL lors de la réinitialisation de auto-commit: " + e.getMessage());
                }
            }
        }
    }

    public ObservableList<Medicament> getLowStockMedicaments() {
        ObservableList<Medicament> lowStockMedicaments = FXCollections.observableArrayList();
        String query = "SELECT m.*, " +
                       "(SELECT MIN(l.date_expiration) FROM lots l WHERE l.medicament_id = m.id AND l.quantite > 0 AND l.date_expiration >= CURDATE()) AS earliest_upcoming_expiry_date " +
                       "FROM medicaments m WHERE m.quantite_stock <= m.seuil_alerte ORDER BY m.nom ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Medicament med = new Medicament();
                med.setId(rs.getInt("id"));
                med.setNom(rs.getString("nom"));
                med.setDescription(rs.getString("description"));
                med.setPrix(rs.getBigDecimal("prix"));
                med.setQuantiteStock(rs.getInt("quantite_stock"));
                med.setSeuilAlerte(rs.getInt("seuil_alerte"));
                med.setCategorie(rs.getString("categorie"));
                med.setImagePath(rs.getString("image_path"));

                Date expiryDateSql = rs.getDate("earliest_upcoming_expiry_date");
                if (expiryDateSql != null) {
                    med.setEarliestActiveExpiryDate(expiryDateSql.toLocalDate());
                } else {
                     med.setEarliestActiveExpiryDate(null);
                }
                lowStockMedicaments.add(med);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération des médicaments à stock faible: " + e.getMessage());
            e.printStackTrace();
        }
        return lowStockMedicaments;
    }

    // Optional: Get a single medicament by ID (useful for various operations)
    public Medicament getMedicamentById(int medicamentId) {
        String query = "SELECT m.*, " +
                       "(SELECT MIN(l.date_expiration) FROM lots l WHERE l.medicament_id = m.id AND l.quantite > 0 AND l.date_expiration >= CURDATE()) AS earliest_upcoming_expiry_date " +
                       "FROM medicaments m WHERE m.id = ?";
        Medicament med = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, medicamentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs..next()) {
                    med = new Medicament();
                    med.setId(rs.getInt("id"));
                    med.setNom(rs.getString("nom"));
                    med.setDescription(rs.getString("description"));
                    med.setPrix(rs.getBigDecimal("prix"));
                    med.setQuantiteStock(rs.getInt("quantite_stock"));
                    med.setSeuilAlerte(rs.getInt("seuil_alerte"));
                    med.setCategorie(rs.getString("categorie"));
                    med.setImagePath(rs.getString("image_path"));

                    Date expiryDateSql = rs.getDate("earliest_upcoming_expiry_date");
                    if (expiryDateSql != null) {
                        med.setEarliestActiveExpiryDate(expiryDateSql.toLocalDate());
                    } else {
                        med.setEarliestActiveExpiryDate(null);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération du médicament ID " + medicamentId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return med;
    }
}