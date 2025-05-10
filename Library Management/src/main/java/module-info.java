module org.example.efm {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires jbcrypt;

    opens org.example.efm to javafx.fxml;
    opens org.example.efm.controller to javafx.fxml;
    opens org.example.efm.model to javafx.base, javafx.fxml; // Ensure javafx.fxml if models are directly used in FXML, and javafx.base for PropertyValueFactory
    exports org.example.efm;
}
