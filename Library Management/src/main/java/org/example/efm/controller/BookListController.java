package org.example.efm.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

import org.example.efm.Main;
import org.example.efm.model.Book;
import org.example.efm.model.User;
import org.example.efm.service.BookService;
import org.example.efm.service.LoanService; // Import LoanService

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class BookListController {

    @FXML
    private TableView<Book> bookTableView;
    @FXML
    private TableColumn<Book, Long> idColumn;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, String> isbnColumn;
    @FXML
    private TableColumn<Book, Integer> yearColumn;
    @FXML
    private TableColumn<Book, Boolean> availableColumn;
    @FXML
    private TableColumn<Book, Void> actionColumn;

    @FXML
    private TextField searchField;
    @FXML
    private ChoiceBox<String> availabilityFilterChoiceBox;

    @FXML
    private Label messageLabel;

    @FXML
    private HBox managerControlsBox;
    @FXML
    private HBox userControlsBox; // Add this for the "My Borrows" button container

    private final BookService bookService = new BookService();
    private final LoanService loanService = new LoanService(); // Instantiate LoanService
    private ObservableList<Book> masterBookList;
    private FilteredList<Book> filteredBookList;

    private enum AvailabilityFilter {
        ALL("All"),
        AVAILABLE("Available"),
        UNAVAILABLE("Unavailable");

        private final String displayName;

        AvailabilityFilter(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadBooks();
        setupRoleBasedControls(); // Renamed for clarity
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("available"));

        availableColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Yes" : "No"));
            }
        });

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button actionButton = new Button();
            private Book currentBook = null;

            {
                actionButton.setOnAction(event -> {
                    if (currentBook != null) {
                        if (currentBook.isAvailable()) {
                            handleBorrowAction(currentBook);
                        } else {
                            // Only managers can return books directly from this list for now
                            if ("manager".equalsIgnoreCase(Main.getCurrentUserRole())) {
                                handleReturnAction(currentBook);
                            } else {
                                messageLabel.setText("Book is unavailable. Only managers can process returns here.");
                            }
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    currentBook = null;
                } else {
                    currentBook = getTableView().getItems().get(getIndex());
                    if (currentBook.isAvailable()) {
                        actionButton.setText("Borrow");
                        actionButton.setDisable(false);
                    } else {
                        // If book is not available, only manager sees "Return"
                        if ("manager".equalsIgnoreCase(Main.getCurrentUserRole())) {
                            actionButton.setText("Return");
                            actionButton.setDisable(false);
                        } else {
                            actionButton.setText("Unavailable"); // Or "Borrowed"
                            actionButton.setDisable(true);
                        }
                    }
                    setGraphic(actionButton);
                }
            }
        });
    }

    private void setupFilters() {
        availabilityFilterChoiceBox.getItems().addAll(
                AvailabilityFilter.ALL.toString(),
                AvailabilityFilter.AVAILABLE.toString(),
                AvailabilityFilter.UNAVAILABLE.toString()
        );
        availabilityFilterChoiceBox.setValue(AvailabilityFilter.ALL.toString()); // Default

        // Listener for search field and availability filter
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        availabilityFilterChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String availabilitySelection = availabilityFilterChoiceBox.getValue();

        Predicate<Book> textPredicate = book -> {
            if (searchText.isEmpty()) {
                return true;
            }
            return String.valueOf(book.getId()).contains(searchText)
                    || book.getTitle().toLowerCase().contains(searchText)
                    || book.getAuthor().toLowerCase().contains(searchText)
                    || book.getIsbn().toLowerCase().contains(searchText);
        };

        Predicate<Book> availabilityPredicate = book -> {
            if (availabilitySelection.equals(AvailabilityFilter.AVAILABLE.toString())) {
                return book.isAvailable();
            } else if (availabilitySelection.equals(AvailabilityFilter.UNAVAILABLE.toString())) {
                return !book.isAvailable();
            }
            return true; // ALL or invalid selection
        };

        if (filteredBookList != null) {
            filteredBookList.setPredicate(textPredicate.and(availabilityPredicate));
        }
    }

    private void loadBooks() {
        masterBookList = FXCollections.observableArrayList(bookService.getAllBooks());
        filteredBookList = new FilteredList<>(masterBookList, p -> true);

        SortedList<Book> sortedData = new SortedList<>(filteredBookList);
        sortedData.comparatorProperty().bind(bookTableView.comparatorProperty());

        bookTableView.setItems(sortedData);

        if (masterBookList.isEmpty()) {
            messageLabel.setText("No books found in the library.");
        } else {
            messageLabel.setText("");
        }
        applyFilters();
    }

    private void setupRoleBasedControls() {
        String currentUserRole = Main.getCurrentUserRole();
        boolean isManager = "manager".equalsIgnoreCase(currentUserRole);

        managerControlsBox.setVisible(isManager);
        managerControlsBox.setManaged(isManager);

        // "My Borrows" button should be visible to all logged-in users
        // If userControlsBox is always meant to be visible for logged-in users
        // no specific role check is needed here beyond being logged in.
        // If Main.getCurrentUserRole() can be null, add a check.
        userControlsBox.setVisible(currentUserRole != null);
        userControlsBox.setManaged(currentUserRole != null);
    }

    @FXML
    protected void handleClearFiltersAction() {
        searchField.clear();
        availabilityFilterChoiceBox.setValue(AvailabilityFilter.ALL.toString());
        // applyFilters() will be called by the listeners
    }

    private void handleBorrowAction(Book book) {
        User currentUser = Main.getCurrentUser();
        if (currentUser == null) {
            messageLabel.setText("Error: No user logged in.");
            return;
        }

        if (!book.isAvailable()) {
            messageLabel.setText("Book '" + book.getTitle() + "' is not available.");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, "Borrow '" + book.getTitle() + "'?", ButtonType.YES, ButtonType.NO);
        confirmDialog.setHeaderText("Confirm Borrow");
        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            boolean success = loanService.borrowBook(book.getId(), currentUser.getId());
            if (success) {
                messageLabel.setText("Book '" + book.getTitle() + "' borrowed successfully.");
                book.setAvailable(false); // Update local model
                bookTableView.refresh(); // Refresh table to update button text and availability
                // loadBooks(); // Or reload all books if more complex state changes
            } else {
                messageLabel.setText("Failed to borrow book '" + book.getTitle() + "'.");
            }
        }
    }

    private void handleReturnAction(Book book) {
        // This action is currently only available to managers via the action column
        if (!"manager".equalsIgnoreCase(Main.getCurrentUserRole())) {
            messageLabel.setText("Only managers can perform this return action.");
            return;
        }
        if (book.isAvailable()) {
            messageLabel.setText("Book '" + book.getTitle() + "' is already available.");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, "Return '" + book.getTitle() + "' to the library?", ButtonType.YES, ButtonType.NO);
        confirmDialog.setHeaderText("Confirm Return");
        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            boolean success = loanService.returnBook(book.getId());
            if (success) {
                messageLabel.setText("Book '" + book.getTitle() + "' returned successfully.");
                book.setAvailable(true); // Update local model
                bookTableView.refresh(); // Refresh table
                // loadBooks();
            } else {
                messageLabel.setText("Failed to return book '" + book.getTitle() + "'. No active loan or error occurred.");
            }
        }
    }

    @FXML
    protected void handleAddBookAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/efm/BookDialog.fxml"));
            DialogPane dialogPane = loader.load();

            BookDialogController controller = loader.getController();
            controller.setBook(null); // For new book

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Add New Book");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonType.OK.getButtonData()) {
                Book newBook = controller.getBookFromFields();
                if (bookService.addBook(newBook)) {
                    loadBooks(); // Refresh list
                    messageLabel.setText("Book added successfully.");
                } else {
                    messageLabel.setText("Failed to add book.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error opening add book dialog.");
        }
    }

    @FXML
    protected void handleEditBookAction() {
        Book selectedBook = bookTableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            messageLabel.setText("Please select a book to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/efm/BookDialog.fxml"));
            DialogPane dialogPane = loader.load();

            BookDialogController controller = loader.getController();
            controller.setBook(selectedBook);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Edit Book");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonType.OK.getButtonData()) {
                Book editedBook = controller.getBookFromFields();
                if (bookService.updateBook(editedBook)) {
                    loadBooks(); // Refresh list
                    messageLabel.setText("Book updated successfully.");
                } else {
                    messageLabel.setText("Failed to update book.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error opening edit book dialog.");
        }
    }

    @FXML
    protected void handleDeleteBookAction() {
        Book selectedBook = bookTableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            messageLabel.setText("Please select a book to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Book: " + selectedBook.getTitle());
        alert.setContentText("Are you sure you want to delete this book? This action cannot be undone. Books with active loans cannot be deleted.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (bookService.deleteBook(selectedBook.getId())) {
                loadBooks(); // Refresh list
                messageLabel.setText("Book deleted successfully.");
            } else {
                // BookService.deleteBook now prints a specific message if loan exists
                messageLabel.setText("Failed to delete book. It might have an active loan or another error occurred.");
            }
        }
    }

    @FXML
    protected void handleManageBorrowersAction() {
        try {
            Main.showMemberManagementView();
        } catch (IOException e) {
            messageLabel.setText("Error opening borrower management.");
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleMyBorrowsAction() {
        try {
            Main.showMyLoansView();
        } catch (IOException e) {
            messageLabel.setText("Error opening My Borrows: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleLogoutAction() {
        try {
            Main.showLoginView();
        } catch (IOException e) {
            messageLabel.setText("Error logging out.");
            e.printStackTrace();
        }
    }
}
