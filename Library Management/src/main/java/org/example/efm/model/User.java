package org.example.efm.model;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {
    private final LongProperty id;
    private final StringProperty username;
    private final StringProperty role;
    // Add other properties like email if you extend your users table

    public User(long id, String username, String role) {
        this.id = new SimpleLongProperty(id);
        this.username = new SimpleStringProperty(username);
        this.role = new SimpleStringProperty(role);
    }

    public long getId() { return id.get(); }
    public LongProperty idProperty() { return id; }

    public String getUsername() { return username.get(); }
    public StringProperty usernameProperty() { return username; }

    public String getRole() { return role.get(); }
    public StringProperty roleProperty() { return role; }
}
