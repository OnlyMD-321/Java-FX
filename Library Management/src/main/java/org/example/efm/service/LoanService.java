package org.example.efm.service;

import org.example.efm.db.DatabaseManager;
import org.example.efm.model.Loan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanService {

    private final BookService bookService = new BookService(); // To update book availability

    public boolean borrowBook(long bookId, long userId) {
        // Check if book is available first (optional, BookListController might do this)
        // For robustness, could be checked here too.

        String sql = "INSERT INTO loans (book_id, user_id, loan_date, due_date) VALUES (?, ?, ?, ?)";
        LocalDate loanDate = LocalDate.now();
        LocalDate dueDate = loanDate.plusWeeks(2); // Example: 2 weeks loan period

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            conn.setAutoCommit(false); // Start transaction

            pstmt.setLong(1, bookId);
            pstmt.setLong(2, userId);
            pstmt.setDate(3, Date.valueOf(loanDate));
            pstmt.setDate(4, Date.valueOf(dueDate));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }

            // Update book availability
            boolean availabilityUpdated = bookService.updateBookAvailability(bookId, false);
            if (!availabilityUpdated) {
                conn.rollback();
                System.err.println("Failed to update book availability during borrow operation.");
                return false;
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error borrowing book: " + e.getMessage());
            e.printStackTrace();
            // Consider rolling back if conn was not null and auto-commit was false
            return false;
        }
    }

    public boolean returnBook(long bookId) {
        // Find active loan for the book
        Loan activeLoan = getActiveLoanByBookId(bookId);
        if (activeLoan == null) {
            System.err.println("No active loan found for book ID: " + bookId + " to return.");
            return false; // Or throw exception
        }

        String sql = "UPDATE loans SET return_date = ? WHERE id = ?";
        LocalDate returnDate = LocalDate.now();

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false); // Start transaction

            pstmt.setDate(1, Date.valueOf(returnDate));
            pstmt.setLong(2, activeLoan.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }

            // Update book availability
            boolean availabilityUpdated = bookService.updateBookAvailability(bookId, true);
            if (!availabilityUpdated) {
                conn.rollback();
                System.err.println("Failed to update book availability during return operation.");
                return false;
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error returning book: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Loan getActiveLoanByBookId(long bookId) {
        String sql = "SELECT id, user_id, loan_date, due_date, return_date FROM loans WHERE book_id = ? AND return_date IS NULL";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Loan(
                        rs.getLong("id"),
                        bookId,
                        rs.getLong("user_id"),
                        rs.getDate("loan_date").toLocalDate(),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active loan by book ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Loan> getLoansByUserId(long userId) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT id, book_id, loan_date, due_date, return_date FROM loans WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                loans.add(new Loan(
                        rs.getLong("id"),
                        rs.getLong("book_id"),
                        userId,
                        rs.getDate("loan_date").toLocalDate(),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching loans by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        return loans;
    }
}
