package org.example.fxml;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JdbcDao {

    private static final Logger LOGGER = Logger.getLogger(JdbcDao.class.getName());

    // Database URL below, username, and password with your actual database credentials
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/javafx_registration?useSSL=false&serverTimezone=UTC";
    private static final String DATABASE_USERNAME = "root";
    private static final String DATABASE_PASSWORD = "";
    private static final String INSERT_QUERY = "INSERT INTO inscription (nom_complet, email_id, pass) VALUES (?, ?, ?)";

    public void insertRecord(String fullName, String emailId, String password) throws SQLException {
        // Step 1: Establishing a Connection.
        // try-with-resource statement will automatically close the connection.
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD); // Step 2: Create a statement using the connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(INSERT_QUERY)) {

            preparedStatement.setString(1, fullName);
            preparedStatement.setString(2, emailId);
            preparedStatement.setString(3, password);

            LOGGER.log(Level.INFO, "Executing PreparedStatement: {0}", preparedStatement.toString());

            // Step 3: Execute the query or update query
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "A new user was inserted successfully! Rows affected: {0}", rowsAffected);
            } else {
                // This case should ideally not happen if no exception was thrown,
                // but it's good to be aware of.
                LOGGER.log(Level.WARNING, "User insertion may not have been successful. Rows affected: {0}", rowsAffected);
                // You might consider throwing an exception here if 0 rows affected is an error condition
                // throw new SQLException("User insertion failed, no rows affected.");
            }

        } catch (SQLException e) {
            // Log the exception and re-throw it so the caller can handle it.
            LOGGER.log(Level.SEVERE, "SQL Exception occurred during insertRecord: ", e);
            printSQLException(e); // Keep for detailed console output during development
            throw e; // Re-throw the exception to be handled by the calling method
        }
    }

    public static void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                LOGGER.log(Level.SEVERE, "SQL Exception Details:", e);
                Throwable t = ex.getCause();
                int causeCount = 1;
                while (t != null) {
                    LOGGER.log(Level.WARNING, "Cause #{0}: {1}", new Object[]{causeCount++, t});
                    t = t.getCause();
                }
            }
        }
    }
}
