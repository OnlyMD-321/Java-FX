package org.example.taskmanager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class EditTaskDialog extends Dialog<Task> {

    private Task taskToEdit;

    public EditTaskDialog(Task taskToEdit) {
        super();
        if (taskToEdit == null) {
            throw new IllegalArgumentException("Task to edit cannot be null.");
        }
        this.taskToEdit = taskToEdit;
        this.setTitle("Edit Task");
        this.setHeaderText("Update the details for: " + taskToEdit.getTitle());
        this.getDialogPane().getStylesheets().add(getClass().getResource("/styles/dialog-styles.css").toExternalForm());

        ButtonType saveButtonType = new ButtonType("Save Changes", ButtonData.OK_DONE);
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
        CheckBox completedCheckBox = new CheckBox("Mark as Completed");

        // Pre-fill form
        titleField.setText(taskToEdit.getTitle());
        descriptionArea.setText(taskToEdit.getDescription());
        if (taskToEdit.getDeadline() != null) {
            deadlinePicker.setValue(taskToEdit.getDeadline().toLocalDate());
        }
        priorityComboBox.setValue(taskToEdit.getPriority());
        completedCheckBox.setSelected(taskToEdit.isCompleted());

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
        // Title should not be empty for an existing task either.
        saveButton.setDisable(titleField.getText().trim().isEmpty());
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
                    deadline = LocalDateTime.of(localDeadline, LocalTime.MIDNIGHT);
                }
                int priority = priorityComboBox.getValue() != null ? priorityComboBox.getValue() : 3;
                boolean completed = completedCheckBox.isSelected();

                if (title.isEmpty()) {
                    AlertHelper.showErrorAlert("Validation Error", "Title cannot be empty.");
                    return null; // Keep dialog open
                }

                // Update the existing task object
                this.taskToEdit.setTitle(title);
                this.taskToEdit.setDescription(description);
                this.taskToEdit.setCompleted(completed);
                this.taskToEdit.setDeadline(deadline);
                this.taskToEdit.setPriority(priority);
                return this.taskToEdit;
            }
            return null;
        });
    }

    // Static method to show the dialog and get the result
    public static Optional<Task> showAndWait(Task taskToEdit) {
        EditTaskDialog dialog = new EditTaskDialog(taskToEdit);
        return dialog.showAndWait();
    }
}
