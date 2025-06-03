package org.example.pm.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import org.example.pm.model.Client;
import org.example.pm.util.AlertUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClientDAO {

    public ObservableList<Client> getAllClients() {
        ObservableList<Client> clients = FXCollections.observableArrayList();
        String query = "SELECT * FROM clients ORDER BY nom, prenom";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Client client = new Client();
                client.setId(rs.getInt("id"));
                client.setNom(rs.getString("nom"));
                client.setPrenom(rs.getString("prenom"));
                client.setTelephone(rs.getString("telephone"));
                client.setEmail(rs.getString("email"));
                Date dateCreationSql = rs.getDate("date_creation");
                if (dateCreationSql != null) {
                    client.setDateCreation(dateCreationSql.toLocalDate());
                }
                clients.add(client);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle more gracefully
            AlertUtils.showError("Erreur Base de Données", "Impossible de charger la liste des clients: " + e.getMessage());
        }
        return clients;
    }

    public void addClient(Client client) {
        String query = "INSERT INTO clients (nom, prenom, telephone, email, date_creation) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, client.getNom());
            ps.setString(2, client.getPrenom());
            ps.setString(3, client.getTelephone());
            ps.setString(4, client.getEmail());
            if (client.getDateCreation() == null) { // Set current date if not provided
                client.setDateCreation(LocalDate.now());
            }
            ps.setDate(5, Date.valueOf(client.getDateCreation()));
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    client.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur Base de Données", "Impossible d'ajouter le client: " + e.getMessage());
        }
    }

    public void updateClient(Client client) {
        String query = "UPDATE clients SET nom = ?, prenom = ?, telephone = ?, email = ?, date_creation = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, client.getNom());
            ps.setString(2, client.getPrenom());
            ps.setString(3, client.getTelephone());
            ps.setString(4, client.getEmail());
            ps.setDate(5, Date.valueOf(client.getDateCreation()));
            ps.setInt(6, client.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur Base de Données", "Impossible de modifier le client: " + e.getMessage());
        }
    }

    public void deleteClient(int clientId) {
        // Consider what happens to sales associated with this client.
        // The DB schema has ON DELETE SET NULL for ventes.client_id, which is a good approach.
        String query = "DELETE FROM clients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, clientId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur Base de Données", "Impossible de supprimer le client: " + e.getMessage());
        }
    }

    // Optional: Get a specific client by ID
    public Client getClientById(int clientId) {
        String query = "SELECT * FROM clients WHERE id = ?";
        Client client = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    client = new Client();
                    client.setId(rs.getInt("id"));
                    client.setNom(rs.getString("nom"));
                    client.setPrenom(rs.getString("prenom"));
                    client.setTelephone(rs.getString("telephone"));
                    client.setEmail(rs.getString("email"));
                    Date dateCreationSql = rs.getDate("date_creation");
                    if (dateCreationSql != null) {
                        client.setDateCreation(dateCreationSql.toLocalDate());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return client;
    }

    // TODO: Method to get purchase history for a client (joins with ventes and details_vente)
    // public ObservableList<Vente> getPurchaseHistoryForClient(int clientId) { ... }
}