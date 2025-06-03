package org.example.pm.dao;

import org.example.pm.model.DetailVente;
import org.example.pm.model.Vente;
import org.example.pm.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class VenteDAO {

    // Method to add a complete sale (Vente + DetailVente items)
    public boolean addVente(Vente vente) {
        String insertVenteQuery = "INSERT INTO ventes (client_id, date_vente, montant_total) VALUES (?, ?, ?)";
        String insertDetailVenteQuery = "INSERT INTO details_vente (vente_id, medicament_id, lot_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert into ventes table
            try (PreparedStatement psVente = conn.prepareStatement(insertVenteQuery, Statement.RETURN_GENERATED_KEYS)) {
                if (vente.getClient() != null && vente.getClient().getId() > 0) {
                    psVente.setInt(1, vente.getClient().getId());
                } else {
                    psVente.setNull(1, Types.INTEGER);
                }
                psVente.setTimestamp(2, Timestamp.valueOf(vente.getDateVente()));
                psVente.setBigDecimal(3, vente.getMontantTotal());
                psVente.executeUpdate();

                try (ResultSet generatedKeys = psVente.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        vente.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating vente failed, no ID obtained.");
                    }
                }
            }

            // Insert into details_vente table for each item
            LotDAO lotDAO = new LotDAO(); // To call decrementLotQuantity
            Set<Integer> affectedMedicamentIds = new HashSet<>(); // To update stock only once per medicament

            for (DetailVente detail : vente.getDetailsVente()) {
                try (PreparedStatement psDetail = conn.prepareStatement(insertDetailVenteQuery)) {
                    psDetail.setInt(1, vente.getId());
                    psDetail.setInt(2, detail.getMedicament().getId());
                    psDetail.setInt(3, detail.getLot().getId());
                    psDetail.setInt(4, detail.getQuantite());
                    psDetail.setBigDecimal(5, detail.getPrixUnitaire());
                    psDetail.executeUpdate();

                    // Decrement stock from the specific lot
                    lotDAO.decrementLotQuantity(conn, detail.getLot().getId(), detail.getQuantite());
                    affectedMedicamentIds.add(detail.getMedicament().getId());
                }
            }

            // Update total stock for each affected medicament
            // This is a simplified update; LotDAO's internal updateMedicamentTotalStock might be better
            // if it can be called with a connection object for transaction.
            // For now, let's re-fetch and update based on LotDAO's logic.
            // A better way would be to have LotDAO.updateMedicamentTotalStock(Connection conn, int medicamentId)
            // which is now implemented in LotDAO.
            for (Integer medId : affectedMedicamentIds) {
                // Call the method in LotDAO that sums up lot quantities for a medicament
                // and updates the medicaments table, ensuring it uses the provided 'conn'
                // This logic is assumed to be in LotDAO.updateMedicamentTotalStock(Connection conn, int medicamentId)
                // This was added to LotDAO in previous responses.
                lotDAO.updateMedicamentTotalStock(conn, medId); // Re-using the method from LotDAO
            }

            conn.commit(); // Commit transaction
            success = true;

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur Vente", "Transaction échouée: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    // Don't close connection if managed globally or by caller
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    // Method to get all sales (for history view)
    public ObservableList<Vente> getAllVentes() {
        ObservableList<Vente> ventes = FXCollections.observableArrayList();
        // Query to get Vente and associated Client name
        String query = "SELECT v.id AS vente_id, v.date_vente, v.montant_total, "
                + "c.id AS client_id, c.nom AS client_nom, c.prenom AS client_prenom "
                + "FROM ventes v "
                + "LEFT JOIN clients c ON v.client_id = c.id "
                + "ORDER BY v.date_vente DESC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Vente vente = new Vente();
                vente.setId(rs.getInt("vente_id"));
                vente.setDateVente(rs.getTimestamp("date_vente").toLocalDateTime());
                vente.setMontantTotal(rs.getBigDecimal("montant_total"));

                int clientId = rs.getInt("client_id");
                if (!rs.wasNull()) { // Check if client_id was NULL
                    org.example.pm.model.Client client = new org.example.pm.model.Client();
                    client.setId(clientId);
                    client.setNom(rs.getString("client_nom"));
                    client.setPrenom(rs.getString("client_prenom"));
                    vente.setClient(client);
                } else {
                    vente.setClient(null); // Anonymous sale
                }
                ventes.add(vente);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur Historique", "Impossible de charger l'historique des ventes: " + e.getMessage());
        }
        return ventes;
    }

    // Method to get details for a specific sale
    public ObservableList<DetailVente> getDetailsForVente(int venteId) {
        ObservableList<DetailVente> details = FXCollections.observableArrayList();
        String query = "SELECT dv.id, dv.quantite, dv.prix_unitaire, "
                + "m.id AS medicament_id, m.nom AS medicament_nom, "
                + "l.id AS lot_id, l.numero_lot "
                + "FROM details_vente dv "
                + "JOIN medicaments m ON dv.medicament_id = m.id "
                + "LEFT JOIN lots l ON dv.lot_id = l.id "
                + // LEFT JOIN in case lot was deleted but sale record remains
                "WHERE dv.vente_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, venteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    org.example.pm.model.Medicament med = new org.example.pm.model.Medicament();
                    med.setId(rs.getInt("medicament_id"));
                    med.setNom(rs.getString("medicament_nom"));

                    org.example.pm.model.Lot lot = null;
                    int lotId = rs.getInt("lot_id");
                    if (!rs.wasNull()) {
                        lot = new org.example.pm.model.Lot();
                        lot.setId(lotId);
                        lot.setNumeroLot(rs.getString("numero_lot"));
                    }

                    DetailVente detail = new DetailVente(med, lot, rs.getInt("quantite"), rs.getBigDecimal("prix_unitaire"));
                    detail.setId(rs.getInt("id"));
                    details.add(detail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }
}
