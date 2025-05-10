package org.example.taskmanager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class AddTaskDialog extends Dialog<Task> {

    public AddTaskDialog() {
        super();
        this.setTitle("Add New Task");
        this.setHeaderText("Enter the details for the new task.");
        this.getDialogPane().getStylesheets().add(getClass().getResource("/styles/dialog-styles.css").toExternalForm());

        ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Title (e.g., Project Alpha Deadline)");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Detailed description of the task...");
        descriptionArea.setPrefRowCount(3);
        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Select a deadline");
        ComboBox<Integer> priorityComboBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        priorityComboBox.setPromptText("Priority (1-High, 5-Low)");
        priorityComboBox.setValue(3); // Default priority
        CheckBox completedCheckBox = new CheckBox("Mark as Completed");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Deadline:"), 0, 2);
        grid.add(deadlinePicker, 1, 2);
        grid.add(new Label("Priority:"), 0, 3);
        grid.add(priorityComboBox, 1, 3);
        grid.add(completedCheckBox, 1, 4);

        this.getDialogPane().setContent(grid);

        Node saveButton = this.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        Platform.runLater(titleField::requestFocus);

        this.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String title = titleField.getText().trim();
                String description = descriptionArea.getText().trim();
                LocalDate localDeadline = deadlinePicker.getValue();
                LocalDateTime deadline = null;
                if (localDeadline != null) {
                    deadline = LocalDateTime.of(localDeadline, LocalTime.MIDNIGHT); // Default to start of day
                }
                int priority = priorityComboBox.getValue() != null ? priorityComboBox.getValue() : 3;
                boolean completed = completedCheckBox.isSelected();

                if (title.isEmpty()) {
                    AlertHelper.showErrorAlert("Validation Error", "Title cannot be empty.");
                    return null; // Keep dialog open by returning null
                }
                return new Task(title, description, completed, deadline, priority);
            }
            return null;
        });
    }

    // Static method to show the dialog and get the result
    public static Optional<Task> showAndWaitForTask() {
        AddTaskDialog dialog = new AddTaskDialog();
        return dialog.showAndWait();
    }
}
