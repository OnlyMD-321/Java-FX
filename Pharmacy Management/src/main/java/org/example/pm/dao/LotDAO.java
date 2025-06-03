package org.example.pm.dao; // Changed package

import java.sql.Connection; // Changed import
import java.sql.Date; // Added import
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.example.pm.model.Lot;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
// Removed AlertUtils import as DAOs should ideally not directly call UI alerts.
// If error messages need to be user-facing, it's better to throw custom exceptions
// or let a service layer handle the translation to user alerts.

public class LotDAO {

    /**
     * Updates the total stock quantity for a given medicament in the 'medicaments' table.
     * This is calculated by summing the quantities of all its active lots (quantity > 0).
     * This method MUST be called within an active database transaction if multiple
     * operations depend on its success.
     *
     * @param conn The active database connection (part of a transaction).
     * @param medicamentId The ID of the medicament whose stock needs to be updated.
     * @throws SQLException if a database access error occurs.
     */
    void updateMedicamentTotalStock(Connection conn, int medicamentId) throws SQLException {
        // Sum only lots that have a positive quantity.
        // Use COALESCE to ensure 0 is returned if no lots exist or all have 0 quantity.
        String sumQuery = "SELECT COALESCE(SUM(quantite), 0) AS total_stock FROM lots WHERE medicament_id = ? AND quantite > 0";
        int totalStock = 0;
        try (PreparedStatement psSum = conn.prepareStatement(sumQuery)) {
            psSum.setInt(1, medicamentId);
            try (ResultSet rs = psSum.executeQuery()) {
                if (rs.next()) {
                    totalStock = rs.getInt("total_stock");
                }
            }
        }

        String updateQuery = "UPDATE medicaments SET quantite_stock = ? WHERE id = ?";
        try (PreparedStatement psUpdate = conn.prepareStatement(updateQuery)) {
            psUpdate.setInt(1, totalStock);
            psUpdate.setInt(2, medicamentId);
            psUpdate.executeUpdate();
        }
    }


    public ObservableList<Lot> getLotsForMedicament(int medicamentId) {
        ObservableList<Lot> lots = FXCollections.observableArrayList();
        String query = "SELECT l.id, l.medicament_id, l.numero_lot, l.date_expiration, l.quantite, m.nom as medicament_nom " +
                       "FROM lots l JOIN medicaments m ON l.medicament_id = m.id WHERE l.medicament_id = ? ORDER BY l.date_expiration ASC"; // Added ORDER BY
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, medicamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Lot lot = new Lot();
                    lot.setId(rs.getInt("id"));
                    lot.setMedicamentId(rs.getInt("medicament_id"));
                    lot.setNumeroLot(rs.getString("numero_lot"));
                    Date dateExpSql = rs.getDate("date_expiration");
                    if (dateExpSql != null) {
                        lot.setDateExpiration(dateExpSql.toLocalDate());
                    }
                    lot.setQuantite(rs.getInt("quantite"));
                    lot.setMedicamentNom(rs.getString("medicament_nom"));
                    lots.add(lot);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération des lots pour le médicament ID " + medicamentId + ": " + e.getMessage());
            // In a real app, might throw a custom exception or log more formally
            e.printStackTrace();
        }
        return lots;
    }

    public void addLot(Lot lot) throws SQLException { // Declare SQLException for better error propagation
        String query = "INSERT INTO lots (medicament_id, numero_lot, date_expiration, quantite) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, lot.getMedicamentId());
                ps.setString(2, lot.getNumeroLot());
                if (lot.getDateExpiration() == null) {
                    throw new SQLException("La date d'expiration du lot ne peut pas être nulle.");
                }
                ps.setDate(3, Date.valueOf(lot.getDateExpiration()));
                ps.setInt(4, lot.getQuantite());
                ps.executeUpdate();

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        lot.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Échec de la création du lot, aucun ID généré obtenu.");
                    }
                }
            }
            updateMedicamentTotalStock(conn, lot.getMedicamentId());
            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'ajout du lot: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur SQL lors du rollback de l'ajout du lot: " + ex.getMessage());
                }
            }
            throw e; // Re-throw the exception so the caller (Controller) can handle it (e.g., show an alert)
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    // Do not close connection if it's managed globally by DatabaseConnection
                } catch (SQLException e) {
                    System.err.println("Erreur SQL lors de la réinitialisation de auto-commit: " + e.getMessage());
                }
            }
        }
    }

    public void updateLot(Lot lot) throws SQLException { // Declare SQLException
        String query = "UPDATE lots SET numero_lot = ?, date_expiration = ?, quantite = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, lot.getNumeroLot());
                if (lot.getDateExpiration() == null) {
                    throw new SQLException("La date d'expiration du lot ne peut pas être nulle.");
                }
                ps.setDate(2, Date.valueOf(lot.getDateExpiration()));
                ps.setInt(3, lot.getQuantite());
                ps.setInt(4, lot.getId());
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Échec de la mise à jour du lot, aucun lot trouvé avec ID: " + lot.getId());
                }
            }
            updateMedicamentTotalStock(conn, lot.getMedicamentId());
            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la mise à jour du lot ID " + lot.getId() + ": " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur SQL lors du rollback de la mise à jour du lot: " + ex.getMessage());
                }
            }
            throw e;
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

    public void deleteLot(int lotId, int medicamentId) throws SQLException { // Declare SQLException
        String query = "DELETE FROM lots WHERE id = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, lotId);
                int affectedRows = ps.executeUpdate();
                 if (affectedRows == 0) {
                    // This might not be an error if the lot was already deleted by another process,
                    // but can be useful for debugging.
                    System.out.println("Avertissement: Tentative de suppression d'un lot non trouvé avec ID: " + lotId);
                }
            }
            updateMedicamentTotalStock(conn, medicamentId);
            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la suppression du lot ID " + lotId + ": " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur SQL lors du rollback de la suppression du lot: " + ex.getMessage());
                }
            }
            throw e;
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

    /**
     * Decrements the quantity of a specific lot. This method is intended to be called
     * as part of a larger transaction (e.g., when processing a sale).
     * It does NOT update the medicament's total stock; that is the responsibility
     * of the calling method managing the overall transaction.
     *
     * @param conn The active database connection (part of a transaction).
     * @param lotId The ID of the lot to decrement.
     * @param quantityToDecrement The amount to decrement.
     * @throws SQLException if a database access error occurs, or if stock is insufficient.
     */
    public void decrementLotQuantity(Connection conn, int lotId, int quantityToDecrement) throws SQLException {
        String selectQuery = "SELECT quantite FROM lots WHERE id = ?";
        int currentQuantity = 0;
        try (PreparedStatement psSelect = conn.prepareStatement(selectQuery)) {
            psSelect.setInt(1, lotId);
            try(ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) {
                    currentQuantity = rs.getInt("quantite");
                } else {
                    throw new SQLException("Lot non trouvé avec ID: " + lotId + " pour la décrémentation.");
                }
            }
        }

        if (currentQuantity < quantityToDecrement) {
            throw new SQLException("Stock insuffisant dans le lot ID " + lotId + ". Disponible: " + currentQuantity + ", Demandé: " + quantityToDecrement);
        }

        String updateQuery = "UPDATE lots SET quantite = quantite - ? WHERE id = ?";
        try (PreparedStatement psUpdate = conn.prepareStatement(updateQuery)) {
            psUpdate.setInt(1, quantityToDecrement);
            psUpdate.setInt(2, lotId);
            int affectedRows = psUpdate.executeUpdate();
            if (affectedRows == 0) {
                // This should ideally not happen if the selectQuery found the lot.
                throw new SQLException("Échec de la mise à jour de la quantité du lot (ID: " + lotId + "), lot non trouvé ou quantité inchangée lors de la mise à jour.");
            }
        }
    }
}