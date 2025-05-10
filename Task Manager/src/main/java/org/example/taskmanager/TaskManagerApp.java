package org.example.taskmanager;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskManagerApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(TaskManagerApp.class.getName());
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ListView<Task> taskListView = new ListView<>();
    private final ComboBox<String> filterBox = new ComboBox<>();
    private final ComboBox<String> sortBox = new ComboBox<>();

    private final String mongoUri = "mongodb+srv://onlymd:onlymd2003@cluster0.5pyzxc6.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> taskCollection;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @Override
    public void init() throws Exception {
        super.init();
        try {
            this.mongoClient = MongoClients.create(mongoUri);
            this.database = mongoClient.getDatabase("task_manager");
            this.taskCollection = database.getCollection("tasks");
            taskCollection.countDocuments();
            System.out.println("Successfully connected to MongoDB.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to MongoDB in init(): " + e.getMessage(), e);
            throw new Exception("MongoDB Connection Failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void start(Stage stage) {
        if (taskCollection == null) {
            AlertHelper.showErrorAlert("Fatal Error", "MongoDB connection not established. Application cannot start.");
            Platform.exit();
            return;
        }

        loadTasks();

        Label titleLabel = new Label("Pro Task Manager");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setPadding(new Insets(10, 0, 20, 0));

        Label filterLabel = new Label("Filter by:");
        filterBox.getItems().addAll("All", "Completed", "In Progress");
        filterBox.setValue("All");
        filterBox.setOnAction(_ -> refreshTaskList());
        filterBox.setMinWidth(120);

        Label sortLabel = new Label("Sort by:");
        sortBox.getItems().addAll("Default", "Deadline", "Priority (High-Low)", "Priority (Low-High)", "Title");
        sortBox.setValue("Default");
        sortBox.setOnAction(_ -> refreshTaskList());
        sortBox.setMinWidth(150);

        HBox filterSortBar = new HBox(10, filterLabel, filterBox, sortLabel, sortBox);
        filterSortBar.setAlignment(Pos.CENTER_LEFT);
        filterSortBar.setPadding(new Insets(0, 0, 10, 0));

        Button addButton = new Button("Add Task");
        Button editButton = new Button("Edit Task");
        Button deleteButton = new Button("Delete Task");

        String baseButtonStyle = "-fx-font-weight: bold; -fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 8 15;";
        addButton.setStyle(baseButtonStyle + "-fx-background-color: #28a745; -fx-text-fill: white;");
        editButton.setStyle(baseButtonStyle + "-fx-background-color: #007bff; -fx-text-fill: white;");
        deleteButton.setStyle(baseButtonStyle + "-fx-background-color: #dc3545; -fx-text-fill: white;");

        HBox buttonBar = new HBox(10, addButton, editButton, deleteButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(10, 0, 0, 0));

        VBox topControls = new VBox(10, titleLabel, filterSortBar);
        topControls.setAlignment(Pos.CENTER);

        VBox layout = new VBox(15, topControls, taskListView, buttonBar);
        layout.setPadding(new Insets(25));
        layout.setBackground(new Background(new BackgroundFill(Color.web("#ecf0f1"), CornerRadii.EMPTY, Insets.EMPTY)));
        VBox.setVgrow(taskListView, Priority.ALWAYS);

        taskListView.setItems(tasks);
        taskListView.setCellFactory(_ -> new TaskCell());
        taskListView.setPlaceholder(new Label("No tasks to display. Add a new task!"));

        addButton.setOnAction(_ -> {
            AddTaskDialog addTaskDialog = new AddTaskDialog();
            Optional<Task> result = addTaskDialog.showAndWait();
            result.ifPresent(this::saveTask);
        });

        editButton.setOnAction(_ -> {
            Task selected = taskListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Optional<Task> result = EditTaskDialog.showAndWait(selected);
                result.ifPresent(this::saveTask);
            } else {
                AlertHelper.showInfoAlert("No Task Selected", "Please select a task to edit.");
            }
        });

        deleteButton.setOnAction(_ -> {
            Task selected = taskListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Are you sure you want to delete this task?\n\"" + selected.getTitle() + "\"",
                        ButtonType.YES, ButtonType.NO);
                confirm.setTitle("Confirm Deletion");
                confirm.setHeaderText(null);
                confirm.showAndWait().ifPresent(bt -> {
                    if (bt == ButtonType.YES) {
                        deleteTask(selected);
                    }
                });
            } else {
                AlertHelper.showInfoAlert("No Task Selected", "Please select a task to delete.");
            }
        });

        Scene scene = new Scene(layout, 750, 650);
        stage.setScene(scene);
        stage.setTitle("Pro Task Manager - Advanced");
        stage.show();
    }

    private void loadTasks() {
        if (taskCollection == null) {
            AlertHelper.showErrorAlert("Error", "Task collection is not initialized.");
            return;
        }
        tasks.clear();
        try {
            List<Document> documents = taskCollection.find().into(new ArrayList<>());
            for (Document doc : documents) {
                LocalDateTime deadline = null;
                Date deadlineDate = doc.getDate("deadline");
                if (deadlineDate != null) {
                    deadline = deadlineDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                }

                Task task = new Task(
                        doc.getString("title"),
                        doc.getString("description"),
                        doc.getBoolean("completed", false),
                        deadline,
                        doc.getInteger("priority", 0));
                ObjectId docId = doc.getObjectId("_id");
                if (docId != null) {
                    task.setId(docId);
                }
                tasks.add(task);
            }
            System.out.println(tasks.size() + " tasks loaded from MongoDB.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not load tasks from database: " + e.getMessage(), e);
            AlertHelper.showErrorAlert("Error Loading Tasks", "Could not load tasks from database: " + e.getMessage());
        }
    }

    private void refreshTaskList() {
        loadTasks();

        String filterValue = filterBox.getValue();
        ObservableList<Task> filteredTasks = FXCollections.observableArrayList();

        switch (filterValue) {
            case "Completed":
                tasks.stream().filter(Task::isCompleted).forEach(filteredTasks::add);
                break;
            case "In Progress":
                tasks.stream().filter(task -> !task.isCompleted()).forEach(filteredTasks::add);
                break;
            case "All":
            default:
                filteredTasks.addAll(tasks);
                break;
        }

        String sortValue = sortBox.getValue();
        switch (sortValue) {
            case "Deadline":
                filteredTasks.sort(Comparator.comparing(Task::getDeadline, Comparator.nullsLast(LocalDateTime::compareTo)));
                break;
            case "Priority (High-Low)":
                filteredTasks.sort(Comparator.comparingInt(Task::getPriority));
                break;
            case "Priority (Low-High)":
                filteredTasks.sort(Comparator.comparingInt(Task::getPriority).reversed());
                break;
            case "Title":
                filteredTasks.sort(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Default":
            default:
                break;
        }

        taskListView.setItems(filteredTasks);
    }

    private void saveTask(Task task) {
        if (taskCollection == null) {
            AlertHelper.showErrorAlert("Error", "Task collection is not initialized.");
            return;
        }
        Document doc = new Document("title", task.getTitle())
                .append("description", task.getDescription())
                .append("completed", task.isCompleted())
                .append("priority", task.getPriority());

        if (task.getDeadline() != null) {
            doc.append("deadline", Date.from(task.getDeadline().atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            doc.append("deadline", null);
        }

        try {
            if (task.getId() == null) {
                InsertOneResult result = taskCollection.insertOne(doc);
                if (result != null && result.getInsertedId() != null && result.getInsertedId().isObjectId()) {
                    task.setId(result.getInsertedId().asObjectId().getValue());
                } else if (result != null && result.getInsertedId() == null) {
                    LOGGER.log(Level.WARNING, "Task inserted but no ID was returned. Check MongoDB configuration or write concern.");
                } else if (result == null) {
                    LOGGER.log(Level.WARNING, "InsertOneResult was null. Task may not have been inserted.");
                }
                AlertHelper.showInfoAlert("Task Added", "Task '" + task.getTitle() + "' has been successfully added.");
            } else {
                taskCollection.updateOne(Filters.eq("_id", task.getId()), new Document("$set", doc));
                AlertHelper.showInfoAlert("Task Updated", "Task '" + task.getTitle() + "' has been successfully updated.");
            }
            refreshTaskList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not save task to database: " + e.getMessage(), e);
            AlertHelper.showErrorAlert("Error Saving Task", "Could not save task to database: " + e.getMessage());
        }
    }

    private void deleteTask(Task task) {
        if (taskCollection == null) {
            AlertHelper.showErrorAlert("Error", "Task collection is not initialized.");
            return;
        }
        if (task.getId() == null) {
            AlertHelper.showErrorAlert("Error", "Cannot delete task without an ID.");
            return;
        }
        try {
            taskCollection.deleteOne(Filters.eq("_id", task.getId()));
            AlertHelper.showInfoAlert("Task Deleted", "Task '" + task.getTitle() + "' has been deleted.");
            refreshTaskList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not delete task from database: " + e.getMessage(), e);
            AlertHelper.showErrorAlert("Error Deleting Task", "Could not delete task from database: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed.");
        }
        super.stop();
    }

    private static class TaskCell extends ListCell<Task> {

        private final VBox contentBox = new VBox(8);
        private final Label titleLabel = new Label();
        private final Label descLabel = new Label();
        private final Label deadlineLabel = new Label();
        private final Label priorityLabel = new Label();
        private final HBox statusBox = new HBox(5);
        private final Label completedLabel = new Label();
        private final Region statusIndicator = new Region();

        public TaskCell() {
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            titleLabel.setTextFill(Color.web("#2c3e50"));

            descLabel.setWrapText(true);
            descLabel.setFont(Font.font("Arial", 13));
            descLabel.setTextFill(Color.web("#7f8c8d"));
            descLabel.setMaxHeight(60);

            deadlineLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            priorityLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            completedLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

            statusIndicator.setPrefSize(10, 10);
            statusIndicator.setMinSize(10, 10);
            statusIndicator.setMaxSize(10, 10);
            statusIndicator.setStyle("-fx-background-radius: 5;");

            statusBox.setAlignment(Pos.CENTER_LEFT);
            statusBox.getChildren().addAll(statusIndicator, completedLabel);

            HBox detailsBox = new HBox(20, deadlineLabel, priorityLabel);
            detailsBox.setAlignment(Pos.CENTER_LEFT);

            contentBox.getChildren().addAll(titleLabel, descLabel, detailsBox, statusBox);
            contentBox.setPadding(new Insets(10));
            contentBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0;");
        }

        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);
            if (empty || task == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                titleLabel.setText(task.getTitle());
                descLabel.setText(task.getDescription() != null && !task.getDescription().isEmpty() ? task.getDescription() : "No description.");

                if (task.getDeadline() != null) {
                    deadlineLabel.setText("Due: " + task.getDeadline().format(DATE_TIME_FORMATTER));
                    deadlineLabel.setTextFill(task.getDeadline().isBefore(LocalDateTime.now()) && !task.isCompleted() ? Color.RED : Color.web("#34495e"));
                } else {
                    deadlineLabel.setText("No Deadline");
                    deadlineLabel.setTextFill(Color.web("#7f8c8d"));
                }

                priorityLabel.setText("Priority: " + task.getPriority());
                priorityLabel.setTextFill(switch (task.getPriority()) {
                    case 1 ->
                        Color.RED;
                    case 2 ->
                        Color.ORANGE;
                    case 3 ->
                        Color.GOLD;
                    case 4 ->
                        Color.GREEN;
                    case 5 ->
                        Color.BLUE;
                    default ->
                        Color.web("#34495e");
                });

                if (task.isCompleted()) {
                    completedLabel.setText("Completed");
                    completedLabel.setTextFill(Color.GREEN);
                    statusIndicator.setStyle("-fx-background-radius: 5; -fx-background-color: green;");
                    contentBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0; -fx-background-color: #e8f5e9;");
                } else {
                    completedLabel.setText("In Progress");
                    completedLabel.setTextFill(Color.ORANGE);
                    statusIndicator.setStyle("-fx-background-radius: 5; -fx-background-color: orange;");
                    contentBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0; -fx-background-color: white;");
                }
                setGraphic(contentBox);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
