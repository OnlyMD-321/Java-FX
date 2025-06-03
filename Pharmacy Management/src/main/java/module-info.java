module org.example.pm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Add this line to open your controller package to javafx.fxml
    opens org.example.pm.controller to javafx.fxml;

    opens org.example.pm to javafx.fxml;
    exports org.example.pm;
    exports org.example.pm.controller;
    exports org.example.pm.model;
    exports org.example.pm.dao;
}
