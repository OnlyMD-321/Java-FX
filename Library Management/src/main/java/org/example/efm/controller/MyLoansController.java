package org.example.efm.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.efm.Main;
import org.example.efm.model.Book;
import org.example.efm.model.Loan;
import org.example.efm.model.User;
import org.example.efm.service.BookService;
import org.example.efm.service.LoanService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class MyLoansController {

    @FXML
    private TableView<LoanDisplayWrapper> loansTableView;
    @FXML
    private TableColumn<LoanDisplayWrapper, String> bookTitleColumn;
    @FXML
    private TableColumn<LoanDisplayWrapper, LocalDate> loanDateColumn;
    @FXML
    private TableColumn<LoanDisplayWrapper, LocalDate> dueDateColumn;
    @FXML
    private TableColumn<LoanDisplayWrapper, LocalDate> returnDateColumn;
    @FXML
    private TableColumn<LoanDisplayWrapper, Void> actionColumn;
    @FXML
    private Label messageLabel;

    private final LoanService loanService = new LoanService();
    private final BookService bookService = new BookService();
    private ObservableList<LoanDisplayWrapper> loanDisplayList;

    @FXML
    public void initialize() {
        bookTitleColumn.setCellValueFactory(cellData -> cellData.getValue().bookTitleProperty());
        loanDateColumn.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));

        setupActionColumn();
        loadUserLoans();
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button returnButton = new Button("Return");

            {
                returnButton.setOnAction(event -> {
                    LoanDisplayWrapper loanWrapper = getTableView().getItems().get(getIndex());
                    handleReturnBookAction(loanWrapper.getLoan());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    LoanDisplayWrapper loanWrapper = getTableView().getItems().get(getIndex());
                    if (loanWrapper.getLoan().getReturnDate() == null) {
                        setGraphic(returnButton);
                    } else {
                        setGraphic(null); // No action if already returned
                    }
                }
            }
        });
    }

    private void loadUserLoans() {
        User currentUser = Main.getCurrentUser();
        if (currentUser == null) {
            messageLabel.setText("Error: No user logged in.");
            loanDisplayList = FXCollections.observableArrayList();
            loansTableView.setItems(loanDisplayList);
            return;
        }

        List<Loan> userLoans = loanService.getLoansByUserId(currentUser.getId());
        if (userLoans.isEmpty()) {
            messageLabel.setText("You have no borrowed books.");
            loanDisplayList = FXCollections.observableArrayList();
        } else {
            messageLabel.setText("");
            loanDisplayList = FXCollections.observableArrayList(
                    userLoans.stream().map(loan -> {
                        Book book = bookService.getBookById(loan.getBookId());
                        String title = (book != null) ? book.getTitle() : "Unknown Book";
                        return new LoanDisplayWrapper(loan, title);
                    }).collect(Collectors.toList())
            );
        }
        loansTableView.setItems(loanDisplayList);
    }

    private void handleReturnBookAction(Loan loan) {
        if (loan.getReturnDate() != null) {
            messageLabel.setText("This book has already been returned.");
            return;
        }

        Book book = bookService.getBookById(loan.getBookId());
        String bookTitle = (book != null) ? book.getTitle() : "Book ID: " + loan.getBookId();

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, "Return '" + bookTitle + "'?", ButtonType.YES, ButtonType.NO);
        confirmDialog.setHeaderText("Confirm Return");
        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            boolean success = loanService.returnBook(loan.getBookId());
            if (success) {
                messageLabel.setText("Book '" + bookTitle + "' returned successfully.");
                loadUserLoans(); // Refresh the list
            } else {
                messageLabel.setText("Failed to return book '" + bookTitle + "'.");
            }
        }
    }

    @FXML
    protected void handleBackToBookListAction() {
        try {
            Main.showBookListView();
        } catch (IOException e) {
            messageLabel.setText("Error navigating back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper wrapper class for TableView display
    public static class LoanDisplayWrapper {

        private final Loan loan;
        private final SimpleStringProperty bookTitle;

        public LoanDisplayWrapper(Loan loan, String bookTitle) {
            this.loan = loan;
            this.bookTitle = new SimpleStringProperty(bookTitle);
        }

        public Loan getLoan() {
            return loan;
        }

        public String getBookTitle() {
            return bookTitle.get();
        }

        public SimpleStringProperty bookTitleProperty() {
            return bookTitle;
        }

        public LocalDate getLoanDate() {
            return loan.getLoanDate();
        }

        public LocalDate getDueDate() {
            return loan.getDueDate();
        }

        public LocalDate getReturnDate() {
            return loan.getReturnDate();
        }
    }
}
