package org.example.efm.model;

import java.time.LocalDate;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Loan {

    private final LongProperty id;
    private final LongProperty bookId;
    private final LongProperty userId;
    private final ObjectProperty<LocalDate> loanDate;
    private final ObjectProperty<LocalDate> dueDate;
    private final ObjectProperty<LocalDate> returnDate;

    public Loan(long id, long bookId, long userId, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate) {
        this.id = new SimpleLongProperty(id);
        this.bookId = new SimpleLongProperty(bookId);
        this.userId = new SimpleLongProperty(userId);
        this.loanDate = new SimpleObjectProperty<>(loanDate);
        this.dueDate = new SimpleObjectProperty<>(dueDate);
        this.returnDate = new SimpleObjectProperty<>(returnDate);
    }

    // ID
    public long getId() {
        return id.get();
    }

    public LongProperty idProperty() {
        return id;
    }

    public void setId(long id) {
        this.id.set(id);
    }

    // Book ID
    public long getBookId() {
        return bookId.get();
    }

    public LongProperty bookIdProperty() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId.set(bookId);
    }

    // User ID
    public long getUserId() {
        return userId.get();
    }

    public LongProperty userIdProperty() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId.set(userId);
    }

    // Loan Date
    public LocalDate getLoanDate() {
        return loanDate.get();
    }

    public ObjectProperty<LocalDate> loanDateProperty() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate.set(loanDate);
    }

    // Due Date
    public LocalDate getDueDate() {
        return dueDate.get();
    }

    public ObjectProperty<LocalDate> dueDateProperty() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate.set(dueDate);
    }

    // Return Date
    public LocalDate getReturnDate() {
        return returnDate.get();
    }

    public ObjectProperty<LocalDate> returnDateProperty() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate.set(returnDate);
    }
}
