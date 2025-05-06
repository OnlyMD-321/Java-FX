package org.example.taskmanager;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.time.LocalDateTime;
import java.util.Date;

@Entity("tasks")
public class Task {
    @Id
    private ObjectId id;

    @Property("title")
    private String title;

    @Property("description")
    private String description;

    @Property("completed")
    private boolean completed;

    @Property("deadline")
    private LocalDateTime deadline;

    @Property("priority")
    private int priority;

    public Task(String title, String description, boolean completed, Date deadline, int priority) {
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.deadline = deadline;
        this.priority = priority;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}