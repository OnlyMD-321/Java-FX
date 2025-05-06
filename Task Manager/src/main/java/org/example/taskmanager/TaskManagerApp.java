
import com.mongodb.client.MongoClient;
import com.mongodb.client.;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.bson.Document;
import org.example.taskmanager.Task;

import java.util.List;

import static javafx.application.Application.launch;

private ObservableList<org.example.taskmanager.Task> tasks = FXCollections.observableArrayList();
    private ListView<org.example.taskmanager.Task> taskListView = new ListView<>();
    private ComboBox<String> filterBox = new ComboBox<>();
    private final String mongoUri = "mongodb+srv://onlymd:onlymd2003";
private SimpleObjectProperty<MongoClient> mongoClient = new SimpleObjectProperty<>(this, "mongoClient", new MongoClient(new MongoClientURI(mongoUri)));
    private MongoDatabase database;
    private MongoCollection<Document> taskCollection;

    public void start(Stage stage) {
        connectToMongoDB();
        loadTasks();

        Label titleLabel = new Label("Task Manager");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.DARKSLATEBLUE);
        titleLabel.setPadding(new Insets(0, 0, 10, 0));

        Button addButton = new Button("Add Task");
        Button deleteButton = new Button("Delete Task");
        Button editButton = new Button("Edit Task");

        String buttonStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;";
        addButton.setStyle(buttonStyle);
        editButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox buttonBar = new HBox(10, addButton, editButton, deleteButton);
        buttonBar.setAlignment(Pos.CENTER);

        filterBox.getItems().addAll("All", "Completed", "In Progress", "By Priority", "By Date");
        filterBox.setValue("All");
        filterBox.setOnAction(e -> refreshList());

        VBox topControls = new VBox(10, titleLabel, filterBox);
        topControls.setAlignment(Pos.CENTER);

        VBox layout = new VBox(15, topControls, taskListView, buttonBar);
        layout.setPadding(new Insets(20));
        layout.setBackground(new Background(new BackgroundFill(Color.web("#f4f4f4"), CornerRadii.EMPTY, Insets.EMPTY)));

        taskListView.setItems(tasks);
        taskListView.setCellFactory(lv -> new TaskCell());

        addButton.setOnAction(e -> showTaskDialog(null));
        editButton.setOnAction(e -> {
            org.example.taskmanager.Task selected = taskListView.getSelectionModel().getSelectedItem();
            if (selected != null) showTaskDialog(selected);
            else showAlert("No Task Selected", "Please select a task to edit.");
        });
        deleteButton.setOnAction(e -> {
            org.example.taskmanager.Task selected = taskListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this task?\n\"" + selected.getTitle() + "\"", ButtonType.YES, ButtonType.NO);
                confirm.setTitle("Confirm Deletion");
                confirm.showAndWait().ifPresent(bt -> {
                    if (bt == ButtonType.YES) {
                        deleteTask(selected);
                        refreshList();
                    }
                });
            } else {
                showAlert("No Task Selected", "Please select a task to delete.");
            }
        });

        Scene scene = new Scene(layout, 650, 550);
        stage.setScene(scene);
        stage.setTitle("Pro Task Manager");
        stage.show();
        refreshList();
    }

    private void connectToMongoDB() {
        database = mongoClient.get().getDatabase("task_manager");
        taskCollection = database.getCollection("tasks");
    }

    private void loadTasks() {
        List<Document> documents = taskCollection.find().into(new ArrayList<>());
        for (Document doc : documents) {
            org.example.taskmanager.Task task = new org.example.taskmanager.Task(doc.getString("title"), doc.getString("description"), doc.getBoolean("completed"),
                    doc.getDate("deadline"), doc.getInteger("priority"));
            tasks.add(task);
        }
    }

    private void saveTask(org.example.taskmanager.Task task) {
        Document doc = new Document("title", task.getTitle())
                .append("description", task.getDescription())
                .append("completed", task.isCompleted())
                .append("deadline", task.getDeadline())
                .append("priority", task.getPriority());
        taskCollection.insertOne(doc);
    }

    private void deleteTask(org.example.taskmanager.Task task) {
        taskCollection.deleteOne(new Document("title", task.getTitle()));
        tasks.remove(task);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showTaskDialog(org.example.taskmanager.Task taskToEdit) {
        // Existing dialog code remains unchanged
    }

    public void refreshList() {
        // Existing refresh logic remains unchanged
    }

    public static void main(String[] args) {
        launch(args);
    }

    static class TaskCell extends ListCell<Task> {
        // Existing TaskCell code remains unchanged
    }
}