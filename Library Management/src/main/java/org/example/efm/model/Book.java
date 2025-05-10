package org.example.efm.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Book {
    private final LongProperty id;
    private final StringProperty title;
    private final StringProperty author;
    private final StringProperty isbn;
    private final IntegerProperty year;
    private final BooleanProperty available;

    public Book(long id, String title, String author, String isbn, int year, boolean available) {
        this.id = new SimpleLongProperty(id);
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.isbn = new SimpleStringProperty(isbn);
        this.year = new SimpleIntegerProperty(year);
        this.available = new SimpleBooleanProperty(available);
    }

    // ID
    public long getId() { return id.get(); }
    public LongProperty idProperty() { return id; }
    public void setId(long id) { this.id.set(id); }

    // Title
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // Author
    public String getAuthor() { return author.get(); }
    public StringProperty authorProperty() { return author; }
    public void setAuthor(String author) { this.author.set(author); }

    // ISBN
    public String getIsbn() { return isbn.get(); }
    public StringProperty isbnProperty() { return isbn; }
    public void setIsbn(String isbn) { this.isbn.set(isbn); }

    // Year
    public int getYear() { return year.get(); }
    public IntegerProperty yearProperty() { return year; }
    public void setYear(int year) { this.year.set(year); }

    // Available
    public boolean isAvailable() { return available.get(); }
    public BooleanProperty availableProperty() { return available; }
    public void setAvailable(boolean available) { this.available.set(available); }

    @Override
    public String toString() {
        return "Book{" +
               "id=" + id.get() +
               ", title='" + title.get() + '\'' +
               ", author='" + author.get() + '\'' +
               ", available=" + available.get() +
               '}';
    }
}