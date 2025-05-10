package org.example.efm.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

import org.example.efm.Main;
import org.example.efm.model.Book;
import org.example.efm.service.BookService;

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

    private final BookService bookService = new BookService();
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
        setupManagerControls();
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
            private final Button borrowButton = new Button("Borrow");

            {
                borrowButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    if (book.isAvailable()) {
                        handleBorrowAction(book);
                    } else {
                        messageLabel.setText("Book '" + book.getTitle() + "' is not available.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Book book = getTableView().getItems().get(getIndex());
                    borrowButton.setDisable(!book.isAvailable());
                    setGraphic(borrowButton);
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
        filteredBookList = new FilteredList<>(masterBookList, p -> true); // Initially show all

        SortedList<Book> sortedData = new SortedList<>(filteredBookList);
        sortedData.comparatorProperty().bind(bookTableView.comparatorProperty()); // Bind sorter to table

        bookTableView.setItems(sortedData);

        if (masterBookList.isEmpty()) {
            messageLabel.setText("No books found in the library.");
        } else {
            messageLabel.setText("");
        }
        applyFilters(); // Apply initial filters (e.g. if search field has text from previous state)
    }

    private void setupManagerControls() {
        boolean isManager = "manager".equalsIgnoreCase(Main.getCurrentUserRole());
        managerControlsBox.setVisible(isManager);
        managerControlsBox.setManaged(isManager); // So it doesn't take space when not visible
    }

    @FXML
    protected void handleClearFiltersAction() {
        searchField.clear();
        availabilityFilterChoiceBox.setValue(AvailabilityFilter.ALL.toString());
        // applyFilters() will be called by the listeners
    }

    private void handleBorrowAction(Book book) {
        // TODO: Implement actual borrowing logic (update DB, etc.)
        System.out.println("Borrowing book: " + book.getTitle());
        messageLabel.setText("Borrowing " + book.getTitle() + " (Not yet fully implemented).");
        // This is a placeholder. In a real app, you'd update the DB
        // and then refresh the masterBookList or the specific item.
        // For example, if borrowing makes it unavailable:
        // book.setAvailable(false); // This updates the Book object in the list
        // bookTableView.refresh(); // Refresh table view to reflect changes
        // To persist, you'd call bookService.updateBook(book) or similar
        // and then potentially reloadBooks() or update the masterBookList more selectively.
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
        alert.setContentText("Are you sure you want to delete this book? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (bookService.deleteBook(selectedBook.getId())) {
                loadBooks(); // Refresh list
                messageLabel.setText("Book deleted successfully.");
            } else {
                messageLabel.setText("Failed to delete book. It might be part of an active loan.");
            }
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
