package org.example.efm.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.efm.Main;
import org.example.efm.model.User; // Assuming you created User.java model
import org.example.efm.service.UserService;

import java.io.IOException;

public class MemberManagementController {

    @FXML
    private TableView<User> memberTableView;
    @FXML
    private TableColumn<User, Long> idColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private Label messageLabel;

    private final UserService userService = new UserService();
    private ObservableList<User> memberList;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        loadMembers();
    }

    private void loadMembers() {
        memberList = FXCollections.observableArrayList(userService.getAllBorrowers());
        memberTableView.setItems(memberList);
        if (memberList.isEmpty()) {
            messageLabel.setText("No borrowers found.");
        } else {
            messageLabel.setText("");
        }
    }

    @FXML
    protected void handleBackToBookListAction() {
        try {
            Main.showBookListView();
        } catch (IOException e) {
            messageLabel.setText("Error navigating back.");
            e.printStackTrace();
        }
    }

    // TODO: Add methods for Add/Edit/Delete members if required later
    // These would involve creating a MemberDialog.fxml and MemberDialogController
    // and corresponding methods in UserService.
}